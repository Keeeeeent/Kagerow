package com.sakulabo.core.Kagerow.Contents.Impl;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import javax.naming.Binding;
import javax.naming.CompositeName;
import javax.naming.Name;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.NotContextException;
import javax.naming.directory.SchemaViolationException;

import com.sakulabo.core.Common.AppPathUtils;
import com.sakulabo.core.Common.NamingEnumerationImpl;
import com.sakulabo.core.Kagerow.Contents.KagerowCacheContent;
import com.sakulabo.core.Kagerow.Contents.KagerowCacheContent.KagerowCacheObject.KagerowExecutionCache;

/**
 * Kagerowアプリケーションのキャッシュコンテンツ実装クラスです
 * 
 * @author keeeeeent
 */
public final class KagerowCacheContentImpl extends BaseKagerowContent implements KagerowCacheContent {

	/** コンテンツメモリ */
	private final Map<Name, KagerowCacheObject> _CONTEXT = new ConcurrentHashMap<>();
	/** コンテンツ名称 */
	private String name;

	/**
	 * デフォルトコンストラクタ
	 * @param name キャッシュID
	 * @throws NamingException キャッシュファイルロード失敗
	 */
	public KagerowCacheContentImpl(String name) throws NamingException {

		// 名称初期化
		this.name = name;

		// キャッシュファイル保存先パスを生成
		Path path = AppPathUtils.createCacheDirPath().resolve(name);

		// キャッシュファイルが存在する場合、コンテキストに追加する
		if (Files.exists(path)) {

			try {

				for (Path file : Files.list(path).toList()) {

					// ディレクトリの場合早期リターンし処理を継続
					if (Files.isDirectory(file)) {
						continue;
					}

					// キャッシュ拡張子以外の場合、デシリアライズ不可能なため無視
					if (!file.toString().endsWith(AppPathUtils.CACHE_FILE_EXT)) {
						continue;
					}

					try (
							InputStream input = Files.newInputStream(file);
							ObjectInputStream oinput = new ObjectInputStream(input)) {
						// キャッシュファイルの読込
						KagerowCacheObject cacheFile = (KagerowCacheObject) oinput.readObject();
						// 名称の生成
						Name named = new CompositeName(Objects.toString(file.getFileName()));
						// コンテンツ登録
						_CONTEXT.put(named, cacheFile);
					}

				}

			} catch (Exception e) {
				// バインド不可として例外をスロー
				throw new SchemaViolationException(e.getMessage());
			}
		}

	}

	/** {@inheritDoc} */
	@Override
	public KagerowCacheObject lookup(Name name) throws NamingException {
		Object result = _CONTEXT.get(name);
		if (Objects.isNull(result)) {
			throw new NotContextException(Objects.toString(name));
		}
		return (KagerowCacheObject) result;
	}

	/** {@inheritDoc} */
	@Override
	public void bind(Name name, Object obj) throws NamingException {

		// 名称の検証
		if (!name.toString().endsWith(AppPathUtils.CACHE_FILE_EXT)) {
			// キャッシュ拡張子がない場合、付与する
			name = new CompositeName(name.toString().concat(AppPathUtils.CACHE_FILE_EXT));
		}

		Object result = _CONTEXT.get(name);
		if (Objects.nonNull(result)) {
			// 既にバインド済みの場合、例外をスロー
			throw new NameAlreadyBoundException(Objects.toString(name));
		} else if (obj instanceof KagerowCacheObject content) {
			try {

				// ファイルパスを生成する
				Path fromPath = Paths.get(content.cacheTargetFilePath());
				// 最終更新日取得
				FileTime time = Files.getLastModifiedTime(fromPath);

				// バインド用キャッシュオブジェクトを生成
				content = switch (content) {
				case KagerowExecutionCache cacheContent -> {
					// キャッシュファイル名称生成
					Path toTarget = KagerowExecutionCache.createCacheKDBPath(
							this.name,
							KagerowExecutionCache.CACHE_DB_FILE_NAME);
					// ファイルコピー
					Files.copy(fromPath, toTarget);
					// キャッシュオブジェクト生成
					KagerowExecutionCache newCache = new KagerowExecutionCache(
							Objects.toString(toTarget.getFileName()),
							Objects.nonNull(time) ? time.toString() : null,
							cacheContent.lordedFileList(),
							cacheContent.lordedFiledMap());
					yield newCache;
				}
				case KagerowCacheObject cache -> cache;
				};

				// キャッシュファイル保存先パスを生成
				Path path = KagerowExecutionCache.createCacheKDBPath(
						this.name,
						Objects.toString(name));
				try (
						OutputStream output = Files.newOutputStream(path);
						ObjectOutputStream ooutput = new ObjectOutputStream(output)) {
					// キャッシュファイルの格納
					ooutput.writeObject(content);
				}

			} catch (Exception e) {
				// バインド不可として例外をスロー
				SchemaViolationException exp = new SchemaViolationException(e.getMessage());
				exp.addSuppressed(e);
				throw exp;
			}

			// インスタンスをセット
			_CONTEXT.put(name, content);
		} else {
			// 上記以外の場合、バインド不可として例外をスロー
			throw new SchemaViolationException(obj.getClass().getCanonicalName());
		}

		// リスナー起動
		callListener(KagerowContentEventKind.CREATE, obj);

	}

	/** {@inheritDoc} */
	@Override
	public void rebind(Name name, Object obj) throws NamingException {

		// 名称の検証
		if (!name.toString().endsWith(AppPathUtils.CACHE_FILE_EXT)) {
			// キャッシュ拡張子がない場合、付与する
			name = new CompositeName(name.toString().concat(AppPathUtils.CACHE_FILE_EXT));
		}

		Object result = _CONTEXT.get(name);
		if (Objects.isNull(result)) {
			// 未バインドの場合、例外をスロー
			throw new NameNotFoundException(Objects.toString(name));
		} else if (obj instanceof KagerowCacheObject content) {

			try {

				// ファイルパスを生成する
				Path fromPath = Paths.get(content.cacheTargetFilePath());
				// 最終更新日取得
				FileTime time = Files.getLastModifiedTime(fromPath);

				// バインド用キャッシュオブジェクトを生成
				content = switch (content) {
				case KagerowExecutionCache cacheContent -> {
					// キャッシュファイル名称生成
					Path toTarget = KagerowExecutionCache.createCacheKDBPath(
							this.name,
							KagerowExecutionCache.CACHE_DB_FILE_NAME);
					// ファイルコピー
					Files.copy(fromPath, toTarget, StandardCopyOption.REPLACE_EXISTING);
					// キャッシュオブジェクト生成
					KagerowExecutionCache newCache = new KagerowExecutionCache(
							Objects.toString(toTarget.getFileName()),
							Objects.nonNull(time) ? time.toString() : null,
							cacheContent.lordedFileList(),
							cacheContent.lordedFiledMap());
					yield newCache;
				}
				case KagerowCacheObject cache -> cache;
				};

				// キャッシュファイル保存先パスを生成
				Path path = KagerowExecutionCache.createCacheKDBPath(
						this.name,
						Objects.toString(name));

				try (
						OutputStream output = Files.newOutputStream(path);
						ObjectOutputStream ooutput = new ObjectOutputStream(output)) {
					// キャッシュファイルを上書き
					ooutput.writeObject(content);
				}

			} catch (Exception e) {
				// バインド不可として例外をスロー
				SchemaViolationException exp = new SchemaViolationException(e.getMessage());
				exp.addSuppressed(e);
				throw exp;
			}

			// バインドされていない場合、インスタンスを検証しセット
			_CONTEXT.put(name, content);

		} else {
			// 上記以外の場合、バインド不可として例外をスロー
			throw new SchemaViolationException();
		}

		// リスナー起動
		callListener(KagerowContentEventKind.UPDATE, obj);

	}

	/** {@inheritDoc} */
	@Override
	public void unbind(Name name) throws NamingException {

		Object result = _CONTEXT.get(name);
		if (Objects.isNull(result)) {
			// 未バインドの場合、例外をスロー
			throw new NameNotFoundException(Objects.toString(name));
		} else {
			// バインドされている合、インスタンス消去
			Object obj = _CONTEXT.remove(name);
			// リスナー起動
			callListener(KagerowContentEventKind.DELETE, obj);
		}

	}

	/** {@inheritDoc} */
	@Override
	public void rename(Name oldName, Name newName) throws NamingException {

		// 名称の検証
		if (!newName.toString().endsWith(AppPathUtils.CACHE_FILE_EXT)) {
			// キャッシュ拡張子がない場合、付与する
			newName = new CompositeName(newName.toString().concat(AppPathUtils.CACHE_FILE_EXT));
		}

		if (!_CONTEXT.containsKey(oldName)) {
			// 未バインドの場合、例外をスロー
			throw new NameNotFoundException(Objects.toString(oldName));
		} else if (_CONTEXT.containsKey(newName)) {
			// リネーム先の名称が既にバインド済みの場合、例外をスロー
			throw new NameAlreadyBoundException(Objects.toString(oldName));
		} else {
			// 上記以外の場合、リネームを実行
			// まずは一度バインド済みのインスタンスを削除
			KagerowCacheObject instance = _CONTEXT.remove(oldName);
			// 新しいキーにてインスタンスを再設定
			_CONTEXT.put(newName, instance);
			// リスナー起動
			callListener(KagerowContentEventKind.RENAME, instance);
		}
	}

	/** {@inheritDoc} */
	@Override
	public NamingEnumeration<NameClassPair> list(Name name) throws NamingException {
		List<NameClassPair> tmpList = _CONTEXT.entrySet().stream()
				.map(m -> new NameClassPair(m.getKey().toString(), m.getValue().getClass().getCanonicalName()))
				.toList();
		return new NamingEnumerationImpl<>(tmpList);
	}

	/** {@inheritDoc} */
	@Override
	public NamingEnumeration<Binding> listBindings(Name name) throws NamingException {
		List<Binding> tmpList = _CONTEXT.entrySet().stream()
				.map(m -> new Binding(m.getKey().toString(), m.getValue().getClass().getCanonicalName(), m.getValue()))
				.toList();
		return new NamingEnumerationImpl<>(tmpList);
	}

	/** {@inheritDoc} */
	@Override
	public String getNameInNamespace() throws NamingException {
		return name;
	}

}

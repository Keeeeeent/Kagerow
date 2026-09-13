package com.sakulabo.core.Kagerow.Contents.Impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serial;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.io.UncheckedIOException;
import java.lang.System.Logger.Level;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.naming.Binding;
import javax.naming.CannotProceedException;
import javax.naming.CompositeName;
import javax.naming.MalformedLinkException;
import javax.naming.Name;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.NotContextException;
import javax.naming.directory.SchemaViolationException;

import com.sakulabo.core.Common.ErrorMessage;
import com.sakulabo.core.Common.NamingEnumerationImpl;
import com.sakulabo.core.Common.StringUtils;
import com.sakulabo.core.Common.URINameParser;
import com.sakulabo.core.Kagerow.Contents.KagerowSecurityContent;
import com.sakulabo.core.Kagerow.Contents.KagerowVirtualFileContent;
import com.sakulabo.core.Kagerow.Contents.KagerowVirtualFileContent.KagerowVirtualFileObject.SecureFileObject;
import com.sakulabo.core.Kagerow.Contents.Impl.KagerowSecurityContentImpl.KagerowMasterSecurityContentImpl;
import com.sakulabo.core.Kagerow.Context.Impl.KagerowSecurityContextImpl;
import com.sakulabo.core.Kagerow.Exception.AppLogicException;
import com.sakulabo.core.Kagerow.Exception.ApplicationError;
import com.sakulabo.core.Processor.log.CommonLogger;
import com.sakulabo.core.Processor.security.AESKeyCreater;
import com.sakulabo.core.Processor.security.AppAESPassEncrypter;

/**
 * Kagerowアプリケーションの仮想FSコンテンツ実装クラスです
 * 
 * @author keeeeeent
 */
public final class KagerowVirtualFileContentImpl extends BaseKagerowContent implements KagerowVirtualFileContent {

	/** コンテンツメモリ */
	private final Map<Name, KagerowVirtualFileObject> _CONTEXT = new ConcurrentHashMap<>();
	/** コンテンツ名称 */
	private String name;
	/** スキーム名称 */
	private URI uri;
	/** URIパーサー */
	private URINameParser parser;
	/** コンテキストアクセッサー */
	private static VarHandle OTHER_CONTEXT;
	static {
		// ルックアップインスタンス生成
		MethodHandles.Lookup lookup = MethodHandles.lookup();
		try {
			// プライベートハンドラ生成
			OTHER_CONTEXT = MethodHandles.privateLookupIn(KagerowSecurityContextImpl.class, lookup)
					.findStaticVarHandle(KagerowSecurityContextImpl.class, StringUtils.REF_CONTEXT, Map.class);
		} catch (Exception e) {
			throw new ApplicationError(e);
		}
	}

	/**
	 * シリアライズプロキシクラス
	 */
	private final static class SecureFileObjectProxy implements Serializable {

		/** シリアライズID */
		@Serial
		private static final long serialVersionUID = -1030956578227235713L;

		/** 暗号化済みバイト配列 */
		private final byte[] data;
		/** 初期化パラメータ */
		private final byte[] iv = AESKeyCreater.getIV(AppAESPassEncrypter.RAND);
		/** 複合化済みファイルオブジェクト */
		private SecureFileObject fileObject;

		/**
		 * デフォルトコンストラクタ
		 * 
		 * @param rowObject シリアライズ対象
		 * @throws Exception シリアライズ失敗
		 */
		private SecureFileObjectProxy(SecureFileObject rowObject) throws Exception {

			// マスターコンテンツ取得
			@SuppressWarnings("unchecked")
			Map<Name, KagerowSecurityContent> content = (Map<Name, KagerowSecurityContent>) OTHER_CONTEXT.getVolatile();

			// セキュアチェック
			if (Objects.isNull(content)) {
				throw new AppLogicException(ErrorMessage.CODE_014.getMessage());
			}

			// セキュリテイコンテンツ取得
			KagerowMasterSecurityContentImpl securityContent = (KagerowMasterSecurityContentImpl) content
					.get(new CompositeName(KagerowMasterSecurityContentImpl.MASTER_KEY));

			// 秘密鍵取得
			SecretKey secretKey = securityContent.getSecretKey();

			// 暗号化インスタンスを生成
			Cipher cipher = Cipher.getInstance(AppAESPassEncrypter.ALGORITHM);
			// 初期化パラメータ生成
			IvParameterSpec ivParam = new IvParameterSpec(iv);
			// 暗号化インスタンス初期化
			cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParam);

			// 暗号化&シリアライズ
			try (ByteArrayOutputStream byteData = new ByteArrayOutputStream();
					ObjectOutputStream byteObject = new ObjectOutputStream(byteData)) {
				byteObject.writeObject(rowObject);
				byteObject.flush();
				this.data = cipher.doFinal(byteData.toByteArray());
			}

		}

		/**
		 * 内部コンストラクタ
		 * 
		 * @param data バイト配列
		 * @param iv   初期化ベク取り
		 * @throws Exception デシリアライズ失敗
		 */
		private SecureFileObjectProxy(byte[] data, byte[] iv) throws Exception {

			// フィールと初期化
			this.data = new byte[0];

			// マスターコンテンツ取得
			@SuppressWarnings("unchecked")
			Map<Name, KagerowSecurityContent> content = (Map<Name, KagerowSecurityContent>) OTHER_CONTEXT.getVolatile();

			// セキュアチェック
			if (Objects.isNull(content)) {
				throw new AppLogicException(ErrorMessage.CODE_014.getMessage());
			}

			// セキュリテイコンテンツ取得
			KagerowMasterSecurityContentImpl securityContent = (KagerowMasterSecurityContentImpl) content
					.get(new CompositeName(KagerowMasterSecurityContentImpl.MASTER_KEY));

			// 秘密鍵取得
			SecretKey secretKey = securityContent.getSecretKey();

			// 暗号化インスタンスを生成
			Cipher cipher = Cipher.getInstance(AppAESPassEncrypter.ALGORITHM);
			// 初期化パラメータ生成
			IvParameterSpec ivParam = new IvParameterSpec(iv);
			// 暗号化インスタンス初期化
			cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParam);
			// 復号化
			data = cipher.doFinal(data);

			// デシリアライズ
			try (ByteArrayInputStream byteData = new ByteArrayInputStream(data);
					ObjectInputStream byteObject = new ObjectInputStream(byteData)) {
				fileObject = (SecureFileObject) byteObject.readObject();
			}

		}

		private void readObject(ObjectInputStream stream) throws InvalidObjectException {
			throw new UnsupportedOperationException();
		}

		private Object writeReplace() {
			return new SecureFileObjectProxy__Impl__(this.data, this.iv);
		}

		private final static class SecureFileObjectProxy__Impl__ implements Serializable {

			/** シリアライズID */
			@Serial
			private static final long serialVersionUID = 4068778074690929570L;

			/** 暗号化済みバイト配列 */
			private final byte[] data;
			/** 初期化パラメータ */
			private final byte[] iv;

			private SecureFileObjectProxy__Impl__(byte[] data, byte[] iv) {
				this.data = data;
				this.iv = iv;
			}

			private Object readResolve() {
				try {
					return new SecureFileObjectProxy(data, iv);
				} catch (Exception e) {
					throw new UncheckedIOException(new IOException(e));
				}
			}
		}

	}

	/**
	 * デフォルトコンストラクタ
	 * 
	 * @param name   論理ネームスペース
	 * @param parser 独自スキームURIパーサー
	 * @throws NamingException コンテキスト生成失敗
	 */
	public KagerowVirtualFileContentImpl(String name, URINameParser parser) throws NamingException {

		// コンテキストの初期化
		try {

			// フィールドの初期化
			this.name = name;
			this.uri = parser.toURI(name.concat(StringUtils.SLASH_DELIMIT));
			this.parser = parser;

			// 対象コンテキスト内部のエントリを全て取得
			List<Path> entries = Files.list(Paths.get(this.uri))
					.filter(Predicate.not(Files::isDirectory))
					.toList();

			// エントリ内部のファイルをデシリアライズ
			for (Path path : entries) {
				try (InputStream input = Files.newInputStream(path);
						ObjectInputStream reader = new ObjectInputStream(input)) {
					// デシリアライズの結果Kagerowリンクファイルの場合処理を実施
					Object dat = reader.readObject();
					// 名称生成
					Name contentName = toName(Objects.toString(path.getFileName()));
					if (dat instanceof KagerowVirtualFileObject content) {
						// 変換ができた場合、エントリに追加
						_CONTEXT.put(contentName, content);
					} else {
						if (dat instanceof SecureFileObjectProxy proxy) {
							// シリアライズプロキシ
							_CONTEXT.put(contentName, proxy.fileObject);
						}
					}
				}
			}

		} catch (StreamCorruptedException e) {
			CommonLogger.getInstance().log(Level.ERROR, name, e);
		} catch (Exception e) {
			MalformedLinkException exception = new MalformedLinkException();
			exception.setRootCause(e);
			throw exception;
		}

	}

	/** {@inheritDoc} */
	@Override
	public KagerowVirtualFileObject lookup(Name name) throws NamingException {
		Object result = _CONTEXT.get(name);
		if (Objects.isNull(result)) {
			throw new NotContextException(Objects.toString(name));
		}
		return (KagerowVirtualFileObject) result;
	}

	/** {@inheritDoc} */
	@Override
	public void bind(Name name, Object obj) throws NamingException {
		Object result = _CONTEXT.get(name);
		if (Objects.nonNull(result)) {
			// 既にバインド済みの場合、例外をスロー
			throw new NameAlreadyBoundException(Objects.toString(name));
		} else if (obj instanceof KagerowVirtualFileObject content) {
			try {

				// バインド先を生成
				Name named = new CompositeName(this.name);
				named.addAll(name);
				URI uri = parser.toURI(named);
				Path path = Paths.get(uri);
				// URI設定
				content.uri().set(uri.toString());

				// シリアライズプロキシ
				if (obj instanceof SecureFileObject rowObject) {
					obj = new SecureFileObjectProxy(rowObject);
				}

				try (OutputStream output = Files.newOutputStream(path);
						ObjectOutputStream ooutput = new ObjectOutputStream(output)) {
					// 仮想ファイルの格納
					ooutput.writeObject(obj);
				}

			} catch (Exception e) {
				// バインド不可として例外をスロー
				throw new SchemaViolationException(e.getMessage());
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
		Object result = _CONTEXT.get(name);
		if (Objects.isNull(result)) {
			// 未バインドの場合、例外をスロー
			throw new NameNotFoundException(Objects.toString(name));
		} else if (obj instanceof KagerowVirtualFileObject content) {
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
		if (!_CONTEXT.containsKey(oldName)) {
			// 未バインドの場合、例外をスロー
			throw new NameNotFoundException(Objects.toString(oldName));
		} else if (_CONTEXT.containsKey(newName)) {
			// リネーム先の名称が既にバインド済みの場合、例外をスロー
			throw new NameAlreadyBoundException(Objects.toString(oldName));
		} else {
			// 上記以外の場合、リネームを実行
			// まずは一度バインド済みのインスタンスを削除
			KagerowVirtualFileObject instance = _CONTEXT.remove(oldName);
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

	/** {@inheritDoc} */
	@Override
	public KagerowVirtualFileObject get(int index) throws NamingException {
		List<KagerowVirtualFileObject> list = _CONTEXT.values().stream().sorted().toList();
		KagerowVirtualFileObject item = null;
		try {
			item = list.get(index);
		} catch (IndexOutOfBoundsException e) {
			NameNotFoundException exp = new NameNotFoundException();
			exp.setRootCause(e);
			throw exp;
		}
		return item;
	}

	/** {@inheritDoc} */
	@Override
	public int contentSize() {
		return _CONTEXT.size();
	}

	/** {@inheritDoc} */
	@Override
	public void destroySubcontext(Name name) throws NamingException {
		Object result = _CONTEXT.get(name);
		if (Objects.isNull(result)) {
			// 未バインドの場合、例外をスロー
			throw new NameNotFoundException(Objects.toString(name));
		} else {
			// バインドされている合、インスタンス消去
			KagerowVirtualFileObject obj = _CONTEXT.remove(name);
			try {
				// 仮想ファイルを削除
				Path path = Paths.get(URI.create(obj.uri().get()));
				Files.delete(path);
				// 物理ファイルを削除
				Path dat = Paths.get(URI.create(obj.datAddr())), idx = Paths.get(URI.create(obj.idxAddr()));
				Files.delete(dat);
				Files.delete(idx);
			} catch (IOException e) {
				CannotProceedException exp = new CannotProceedException();
				exp.addSuppressed(e);
				throw exp;
			}
			// リスナー起動
			callListener(KagerowContentEventKind.DELETE, obj);
		}
	}

}

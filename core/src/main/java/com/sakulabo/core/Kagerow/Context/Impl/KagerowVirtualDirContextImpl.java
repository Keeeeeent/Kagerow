package com.sakulabo.core.Kagerow.Context.Impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.URI;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import javax.naming.CannotProceedException;
import javax.naming.CompositeName;
import javax.naming.Name;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.directory.SchemaViolationException;

import com.sakulabo.core.Common.AppPathUtils;
import com.sakulabo.core.Common.DataSize;
import com.sakulabo.core.Common.StringUtils;
import com.sakulabo.core.Common.URINameParser;
import com.sakulabo.core.Kagerow.Contents.KagerowContents;
import com.sakulabo.core.Kagerow.Contents.KagerowVirtualFileContent;
import com.sakulabo.core.Kagerow.Contents.KagerowVirtualFileContent.KagerowVirtualFileObject;
import com.sakulabo.core.Kagerow.Contents.Impl.KagerowVirtualFileContentImpl;
import com.sakulabo.core.Kagerow.Context.KagerowVirtualDirContext;
import com.sakulabo.core.Kagerow.Context.KagerowVirtualFileContext;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;
import com.sakulabo.core.Processor.config.ContextConfigurationLorder;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Kagerowが管理する仮想ディレクトリエントリアクセスコンテンツです
 * 
 * @author keeeeeent
 */
public final class KagerowVirtualDirContextImpl extends BaseKagerowContext<KagerowVirtualFileContent>
		implements KagerowVirtualDirContext, FileVisitor<Path> {

	/** コンテキスト環境変数メモリ */
	private static final Map<String, String> _ENV;
	static {
		_ENV = ContextConfigurationLorder.getInstance().VIRTUAL_FILE_CONTEXT_ENV();
	}

	/** スキーマファイル名称 */
	private Path kagerowPath;
	/** 親スキーマ */
	private KagerowVirtualFileContextImpl base;
	/** ZIPファイル拡張子 */
	private static final String ZIP_EXTENSION = ".zip";

	/** マッピングファイル名称 */
	private static final String MAP_FILE_NAME = "SynonymMapList.map";

	/**
	 * 内部コンストラクタ
	 * @param name コンテキスト名称
	 * @param base 親コンテキスト
	 * @throws NamingException コンテキスト生成失敗
	 */
	KagerowVirtualDirContextImpl(Name name, KagerowVirtualFileContextImpl base) throws NamingException {
		// コンテンツ格納メモリと、環境変数格納メモリをセットする
		super(new ConcurrentHashMap<>(), _ENV, Objects.toString(name));
		this.base = base;
		Path dir = AppPathUtils.createArchiveDirPath();
		this.kagerowPath = dir.resolve(Objects.toString(name).concat(ZIP_EXTENSION));
		// コンテキスト初期化処理実行
		initialize();
	}

	/**
	 * Kagerowサブコンテキストの初期化処理です
	 * @throws NamingException コンテキスト生成失敗
	 */
	private void initialize() throws NamingException {

		/**
		 * 仮想ファイルシステムのディレクトリを走査し、コンテキストに登録する
		 */

		// 名称格納先リスト生成
		List<String> nameList = new ArrayList<>();

		try {

			// URIを生成し、仮想ファイルシステム内部に接続するためのパスを生成する
			final URINameParser parser = new URINameParser(super._NAME);
			final URI uri = parser.toURI(StringUtils.SLASH_DELIMIT);
			final Path path = Paths.get(uri);

			// ファイル内部のエントリの直下を確認し、ディレクトリの場合リストに格納
			Files.list(path)
					.filter(Files::isDirectory)
					.map(Path::toString)
					.forEach(nameList::add);

			// ファイルディレクトリエントリをデシリアライズ
			for (String entry : nameList) {
				// コンテンツ生成
				KagerowContents content = new KagerowVirtualFileContentImpl(entry, parser);
				// スキーマに紐づいたコンテキストに追加
				bind(entry, content);
			}

			// KeyStoreを初期化
			KagerowSecurityContextImpl.initialize(uri, super._NAME.toString());

		} catch (Exception e) {
			CannotProceedException exception = new CannotProceedException(e.getMessage());
			exception.setRootCause(e);
			throw exception;
		}

	}

	/**
	 * セキュアブートに変更します
	 * @throws NamingException コンテキスト生成失敗
	 */
	void changeToSecureBoot() throws NamingException {

		try {
			// URIを生成し、仮想ファイルシステム内部に接続するためのパスを生成する
			final URINameParser parser = new URINameParser(super._NAME);
			final URI uri = parser.toURI(StringUtils.SLASH_DELIMIT);
			// KeyStoreを初期化
			KagerowSecurityContextImpl.initialize(uri, super._NAME.toString());
		} catch (Exception e) {
			CannotProceedException exception = new CannotProceedException(e.getMessage());
			exception.setRootCause(e);
			throw exception;
		}
	}

	/** {@inheritDoc} */
	@Override
	public Path getPath() {
		return kagerowPath;
	}

	/** {@inheritDoc} */
	@Override
	public KagerowVirtualFileContext getParent() {
		return base;
	}

	/** {@inheritDoc} */
	@Override
	public KagerowVirtualFileContent lookup(Name name) throws NamingException {
		return (KagerowVirtualFileContent) super.lookup(name);
	}

	/** {@inheritDoc} */
	@Override
	public KagerowVirtualFileContent lookup(String name) throws NamingException {
		return (KagerowVirtualFileContent) super.lookup(name);
	}

	/** {@inheritDoc} */
	@Override
	public void unbind(Name name) throws NamingException {
		try {
			// 元データ取得
			KagerowVirtualFileContent base = lookup(name);
			KagerowVirtualFileObject fileObject = base.get(0);
			// URIを生成し、仮想ファイルシステム内部に接続するためのパスを生成する
			final URINameParser parser = new URINameParser(super._NAME);
			final String rawName = parser.createHeaderBinaryPath(fileObject.binaryName());
			// シノニム削除
			Map<String, String> mapper = getSynonymMapList();
			if (mapper.containsValue(rawName)) {
				for (Map.Entry<String, String> entry : mapper.entrySet()) {
					if (rawName.equals(entry.getValue())) {
						removeSynonymMapList(entry.getKey());
						break;
					}
				}
			}
		} catch (Exception e) {
			NameNotFoundException exp = new NameNotFoundException();
			exp.addSuppressed(e);
			throw exp;
		} finally {
			// バインド解除
			super.unbind(name);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void destroySubcontext(Name name) throws NamingException {
		try {
			// URIを生成し、仮想ファイルシステム内部に接続するためのパスを生成する
			final URINameParser parser = new URINameParser(super._NAME);
			final URI uri = parser.toURI(name);
			// 仮想ファイルを削除
			Files.walkFileTree(Paths.get(uri), this);
		} catch (Exception e) {
			NameNotFoundException exp = new NameNotFoundException();
			exp.addSuppressed(e);
			throw exp;
		}
		// リスナー起動
		callListener(KagerowContextEventKind.DELETE_SUB_CONTEXT);
	}

	/** {@inheritDoc} */
	@Override
	public void destroySubcontext(String name) throws NamingException {
		Name named = new CompositeName(name);
		destroySubcontext(named);
	}

	/** {@inheritDoc} */
	@Override
	public KagerowVirtualFileContent createSubcontext(Name name) throws NamingException {
		// コンテンツ初期化
		KagerowVirtualFileContent content = null;
		try {
			// URIを生成し、仮想ファイルシステム内部に接続するためのパスを生成する
			final URINameParser parser = new URINameParser(super._NAME);
			final URI uri = parser.toURI(name);
			final Path path = Paths.get(uri);
			// 仮想FSにディレクトリ追加
			if (Files.notExists(path)) {
				Files.createDirectory(path);
			}
			// コンテンツ生成
			content = new KagerowVirtualFileContentImpl(name.toString(), parser);
			// コンテキストに追加
			bind(name, content);
		} catch (Exception e) {
			throw new SchemaViolationException(e.getMessage());
		}
		// リスナー起動
		callListener(KagerowContextEventKind.CREATE_SUB_CONTEXT);
		return content;
	}

	/** {@inheritDoc} */
	@Override
	public KagerowVirtualFileContent createSubcontext(String name) throws NamingException {
		Name named = new CompositeName(name);
		return createSubcontext(named);
	}

	/** {@inheritDoc} */
	@Override
	public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
		String name = dir.toString();
		try {
			// 元データ取得
			KagerowVirtualFileContent base = lookup(name);
			KagerowVirtualFileObject fileObject = base.get(0);
			// URIを生成し、仮想ファイルシステム内部に接続するためのパスを生成する
			final URINameParser parser = new URINameParser(super._NAME);
			final String rawName = parser.createHeaderBinaryPath(fileObject.binaryName());
			// シノニム削除
			Map<String, String> mapper = getSynonymMapList();
			if (mapper.containsValue(rawName)) {
				for (Map.Entry<String, String> entry : mapper.entrySet()) {
					if (rawName.equals(entry.getValue())) {
						removeSynonymMapList(entry.getKey());
						break;
					}
				}
			}
		} catch (NamingException e) {
			throw new IOException(e);
		}
		return FileVisitResult.CONTINUE;
	}

	/** {@inheritDoc} */
	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
		return FileVisitResult.CONTINUE;
	}

	/** {@inheritDoc} */
	@Override
	public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
		KagerowLogger.newAppLogger().err(exc);
		return FileVisitResult.CONTINUE;
	}

	/** {@inheritDoc} */
	@Override
	@SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE", justification = "構造上必ずファイルとなるようにしているため")
	public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
		String name = dir.toString();
		try {
			KagerowVirtualFileContent content = lookup(name);
			for (Path file : Files.list(dir).toList()) {
				String fileName = file.getFileName().toString();
				content.destroySubcontext(fileName);
			}
			Files.delete(dir);
			Name named = new CompositeName(name);
			super.unbind(named);
		} catch (NamingException e) {
			throw new IOException(e);
		}
		return FileVisitResult.CONTINUE;
	}

	/** {@inheritDoc} */
	@Override
	@SuppressWarnings("unchecked")
	public Map<String, String> getSynonymMapList() {
		Map<String, String> result = new HashMap<>();
		try {
			// URIを生成し、仮想ファイルシステム内部に接続するためのパスを生成する
			final URINameParser parser = new URINameParser(super._NAME);
			final URI uri = parser.toURI(MAP_FILE_NAME);
			final Path filePath = Paths.get(uri);
			// ファイルが存在する場合、デシリアライズ実施
			if (Files.exists(filePath)) {
				try (
						InputStream input = Files.newInputStream(filePath);
						ObjectInputStream oinput = new ObjectInputStream(input)) {
					result = (Map<String, String>) oinput.readObject();
				}
			}
		} catch (Exception e) {
			KagerowLogger.newAppLogger().err(e);
		}
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public void addSynonymMapList(String binaryHash, String synonym) throws NamingException {

		Map<String, String> mapper = getSynonymMapList();
		if (mapper.containsKey(synonym)) {
			NameAlreadyBoundException exp = new NameAlreadyBoundException();
			exp.setResolvedName(new CompositeName(synonym));
			throw exp;
		} else {
			mapper.put(synonym, binaryHash);
			try {
				registMap(mapper);
			} catch (Exception e) {
				throw new SchemaViolationException(e.getMessage());
			}
		}

	}

	/** {@inheritDoc} */
	@Override
	public void removeSynonymMapList(String synonym) throws NamingException {

		Map<String, String> mapper = getSynonymMapList();
		if (!mapper.containsKey(synonym)) {
			NameNotFoundException exp = new NameNotFoundException();
			exp.setResolvedName(new CompositeName(synonym));
			throw exp;
		} else {
			mapper.remove(synonym);
			try {
				registMap(mapper);
			} catch (Exception e) {
				throw new SchemaViolationException(e.getMessage());
			}
		}

	}

	/** {@inheritDoc} */
	@Override
	public void renameSynonymMapList(String oldSynonym, String newSynonym) throws NamingException {

		Map<String, String> mapper = getSynonymMapList();
		if (!mapper.containsKey(oldSynonym)) {
			NameNotFoundException exp = new NameNotFoundException();
			exp.setResolvedName(new CompositeName(oldSynonym));
			throw exp;
		} else {
			String value = mapper.remove(oldSynonym);
			mapper.put(newSynonym, value);
			try {
				registMap(mapper);
			} catch (Exception e) {
				throw new SchemaViolationException(e.getMessage());
			}
		}

	}

	/** {@inheritDoc} */
	@Override
	public boolean isExistSynonym(String synonym, String binaryHash) {
		// 既にシノニムが存在するか確認
		Map<String, String> mapper = getSynonymMapList();
		String bindedBinaryHash = mapper.get(synonym);
		if (Objects.nonNull(bindedBinaryHash)) {
			// 存在する場合、そのシノニムとFileObjectの物理名が一致しているか確認
			if (!bindedBinaryHash.equals(binaryHash)) {
				// バインドはされているが、別の物理ヘッダーに紐づけられている場合
				// 既にバインド済みとして判定
				return true;
			}
		}
		return false;
	}

	/**
	 * マップリストを生成します
	 * @param map シリアライズマップ
	 * @throws Exception マップ更新・取得処理失敗
	 */
	private void registMap(Map<String, String> map) throws Exception {
		// URIを生成し、仮想ファイルシステム内部に接続するためのパスを生成する
		final URINameParser parser = new URINameParser(super._NAME);
		final URI uri = parser.toURI(MAP_FILE_NAME);
		// シリアライズ
		try (
				OutputStream out = Files.newOutputStream(Paths.get(uri));
				ObjectOutputStream oout = new ObjectOutputStream(out)) {
			oout.writeObject(map);
		}
	}

	/** {@inheritDoc} */
	@Override
	public long getSchemaContextSize() throws IOException {
		long size = Files.size(kagerowPath);
		return DataSize.KB.toUnitSize(BigInteger.valueOf(size)).longValue();
	}

}

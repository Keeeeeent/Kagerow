package com.sakulabo.core.Kagerow.Context.Impl;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.naming.Binding;
import javax.naming.CannotProceedException;
import javax.naming.CompositeName;
import javax.naming.Name;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import com.sakulabo.core.Common.AppPathUtils;
import com.sakulabo.core.Common.URINameParser;
import com.sakulabo.core.Kagerow.Context.KagerowVirtualDirContext;
import com.sakulabo.core.Kagerow.Context.KagerowVirtualFileContext;
import com.sakulabo.core.Kagerow.Exception.ApplicationError;
import com.sakulabo.core.Processor.config.ContextConfigurationLorder;
import com.sakulabo.core.Processor.jmx.AppJMX;
import com.sakulabo.core.Processor.jmx.Context.KagerowVirtualFileContextImplMXBean;

/**
 * Kagerowが管理する仮想ファイルアクセスコンテンツです
 * 
 * @author keeeeeent
 */
@AppJMX(name = "Context", options = { "type=KagerowVirtualFileContext" })
public final class KagerowVirtualFileContextImpl extends BaseKagerowContext<KagerowVirtualDirContext>
		implements KagerowVirtualFileContext, KagerowVirtualFileContextImplMXBean {

	/** コンテキスト環境変数メモリ */
	private static final Map<String, String> _ENV;
	static {
		_ENV = ContextConfigurationLorder.getInstance().VIRTUAL_FILE_CONTEXT_ENV();
	}
	/** ZIPファイル拡張子 */
	private static final String ZIP_EXTENSION = ".zip";
	/** ZIP仮想ファイルパス */
	private static Path archivePath;

	/** コンテキスト共通設定 */
	{

		try {

			// 仮想ファイルを生成します
			Path path = AppPathUtils.createArchiveDirPath()
					.resolve(KagerowVirtualFileContext.SYSTEM_SCHEMA.concat(ZIP_EXTENSION));
			if (Files.notExists(path)) {
				try (ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(path.toFile()))) {
					ZipEntry entry = new ZipEntry("dummy/");
					zip.putNextEntry(entry);
					zip.closeEntry();
				}
			}

			// URIを生成し、仮想ファイルシステム内部に接続するためのパスを生成する
			final URINameParser parser = new URINameParser(KagerowVirtualFileContext.SYSTEM_SCHEMA);
			final String realDirName = parser.createHeaderRealPath();
			final URI dirUri = parser.toURI(realDirName, URINameParser.BINARY_HOST);
			final Path dirPath = Paths.get(dirUri);
			if (Files.notExists(dirPath)) {
				Files.createDirectory(dirPath);
			}

		} catch (Exception e) {
			throw new ApplicationError(e);
		}

	}

	/**
	 * デフォルトコンストラクタ
	 * @throws NamingException コンテキスト生成失敗
	 */
	KagerowVirtualFileContextImpl() throws NamingException {

		// スーパークラスコンストラクタ呼び出し
		super(new ConcurrentHashMap<>(), _ENV, KagerowVirtualFileContext._NAME);
		// MXBeanの登録
		registMXBean(this);

		// コンテキストの構築を実行
		KagerowVirtualFileContextImpl.archivePath = AppPathUtils.createArchiveDirPath();
		try {
			List<String> targets = Files
					.find(KagerowVirtualFileContextImpl.archivePath, 1,
							this::archiveTarget,
							FileVisitOption.FOLLOW_LINKS)
					.map(Path::getFileName)
					.map(Path::toString)
					.filter(Objects::nonNull)
					.toList();
			for (String p : targets) {
				String fileName = p.substring(0, p.length() - 4);
				Name name = new CompositeName(fileName);
				createSubcontext(name);
			}
		} catch (IOException e) {
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

		for (KagerowVirtualDirContext dirContext : _CONTEXT.values()) {
			if (dirContext instanceof KagerowVirtualDirContextImpl contextImpl) {
				contextImpl.changeToSecureBoot();
			}
		}

	}

	/**
	 * バインド可能な物理ファイルを生成します
	 * @param name 物理ファイル基底名
	 * @return 物理ファイルフルパス
	 */
	private Path toRealFilePath(String name) {
		Path dir = AppPathUtils.createArchiveDirPath();
		String fileName = name.concat(ZIP_EXTENSION);
		return dir.resolve(fileName).normalize().toAbsolutePath();
	}

	/**
	 * アーカイブ対象ファイル判定メソッド
	 * @param path 対象パス
	 * @param attr 対象属性
	 * @return 判定結果
	 */
	private boolean archiveTarget(Path path, BasicFileAttributes attr) {
		return Objects.toString(path.getFileName()).endsWith(ZIP_EXTENSION);
	}

	/** {@inheritDoc} */
	@Override
	public void destroySubcontext(Name name) throws NamingException {
		try {
			// 仮想FSを削除
			KagerowVirtualDirContext dir = lookup(name);
			NamingEnumeration<Binding> list = dir.listBindings((Name) null);
			while (list.hasMore()) {
				Binding data = list.next();
				String key = data.getName();
				dir.destroySubcontext(key);
			}
			// 物理ファイルを削除
			Files.delete(toRealFilePath(name.toString()));
			// URIを生成し、仮想ファイルシステム内部に接続するためのパスを生成する
			final URINameParser parser = new URINameParser(name);
			final String realDirName = parser.createHeaderRealPath();
			final URI dirUri = parser.toURI(realDirName, URINameParser.BINARY_HOST);
			final Path dirPath = Paths.get(dirUri);
			if (Files.exists(dirPath)) {
				Files.delete(dirPath);
			}
			// コンテキストから削除
			super.unbind(name);
		} catch (IOException | NoSuchAlgorithmException | URISyntaxException e) {
			CannotProceedException exception = new CannotProceedException(e.getMessage());
			exception.setRootCause(e);
			throw exception;
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
	public KagerowVirtualDirContext createSubcontext(Name name) throws NamingException {
		KagerowVirtualDirContext context = null;
		try {
			// URIを生成し、仮想ファイルシステム内部に接続するためのパスを生成する
			final URINameParser parser = new URINameParser(name);
			final String realDirName = parser.createHeaderRealPath();
			final URI dirUri = parser.toURI(realDirName, URINameParser.BINARY_HOST);
			final Path dirPath = Paths.get(dirUri);
			if (Files.notExists(dirPath)) {
				Files.createDirectory(dirPath);
			}
			// コンテキスト物理ディレクトリ作成
			context = new KagerowVirtualDirContextImpl(name, this);
			super.bind(name, context);
		} catch (Exception e) {
			CannotProceedException exception = new CannotProceedException(e.getMessage());
			exception.setRootCause(e);
			throw exception;
		}
		// リスナー起動
		callListener(KagerowContextEventKind.CREATE_SUB_CONTEXT);
		return context;
	}

	/** {@inheritDoc} */
	@Override
	public KagerowVirtualDirContext createSubcontext(String name) throws NamingException {
		Name named = new CompositeName(name);
		return createSubcontext(named);
	}

	/** {@inheritDoc} */
	@Override
	public KagerowVirtualDirContext lookup(Name name) throws NamingException {
		return (KagerowVirtualDirContext) super.lookup(name);
	}

	/** {@inheritDoc} */
	@Override
	public KagerowVirtualDirContext lookup(String name) throws NamingException {
		return (KagerowVirtualDirContext) super.lookup(name);
	}

	/** {@inheritDoc} */
	@Override
	public Map<String, String> getContext() {
		Map<String, String> jmxMap = new HashMap<>();
		for (Map.Entry<Name, ?> jmxTarget : _CONTEXT.entrySet()) {
			jmxMap.put(jmxTarget.getKey().toString(), jmxTarget.getValue().getClass().getCanonicalName());
		}
		return jmxMap;
	}

}

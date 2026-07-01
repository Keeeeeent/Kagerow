package com.sakulabo.core.Provides;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessMode;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.sakulabo.core.Common.AppPathUtils;
import com.sakulabo.core.Common.StringUtils;
import com.sakulabo.core.Common.URINameParser;
import com.sakulabo.core.Common.VMOption;
import com.sakulabo.core.Kagerow.Utilities.KagerowTransaction;
import com.sakulabo.core.Processor.transaction.FileTransaction;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * 独自スキーム実装を提供するプロバイダクラスです<br>
 * Kagerow仮想ファイルシステムとして機能します
 * 
 * @author keeeeeent
 */
public final class ArchiveSystemProvider extends FileSystemProvider {

	/** 仮想ファイルシステムのスキーム名称 */
	public static final String SCHOME = "kagerow";

	/** 仮想ファイルシステムの環境設定 */
	private static final Map<String, ?> ENV = new HashMap<>() {
		{
			// ファイルが存在しない場合新規作成
			put("create", true);
			// ZIPエンコードタイプ
			put("encoding", "UTF-8");
			// 一時ファイル使用
			put("useTempFile", true);
			// 圧縮形式
			put("compressionMethod", "DEFLATED");
		}
	};
	/** 仮想ファイルシステムFileSystemインスタンスキャッシュ */
	public static final Map<Path, FileSystem> cache = new ConcurrentHashMap<>();
	/** 仮想ファイルインスタンス */
	@SuppressFBWarnings("ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
	private static volatile ArchiveSystemProvider provider;

	/**
	 * デフォルトのコンストラクタです
	 */
	public ArchiveSystemProvider() {
		provider = this;
	}

	/**
	 * ファイルシステムプロバイダーを取得します
	 * @return ファイルシステムプロバイダー
	 */
	public static FileSystemProvider getFileSystemProvider() {
		if (Objects.isNull(provider)) {
			// 仮想ファイルインスタンスが存在しない場合、初期化をするよう例外をスロー
			throw new UncheckedIOException(
					new IOException("The initialization process is not yet complete."));
		}
		return provider;
	}

	/**
	 * ファイルシステムのトランザクションを取得します
	 * @param uri 対象URI
	 * @return トランザクション管理インスタンス
	 */
	public static KagerowTransaction getTransaction(URI uri) {
		Path zipFilePath = toZipPath(uri);
		KagerowTransaction tran = new FileTransaction(zipFilePath, uri);
		return tran;
	}

	/**
	 * 仮想ファイルシステムの実行に必要な初期化処理を実行します
	 * @param uri 対象URI
	 */
	public static void init(URI uri) {
		try {
			// 仮想ファイルインスタンスの存在確認
			if (Objects.nonNull(provider)) {
				provider.newFileSystem(uri, null);
			} else {
				// 仮想ファイルインスタンスが存在しない場合、初期化をするよう例外をスロー
				throw new UncheckedIOException(
						new IOException("The initialization process is not yet complete."));
			}
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	/**
	 * スキーマ名称規定
	 */
	@Override
	public String getScheme() {
		return SCHOME;
	}

	/**
	 * ファイルシステムを作成する時のみ<br>
	 * 呼び出しタイミング：FileSystems.newFileSystem(uri, env)
	 */
	@Override
	public FileSystem newFileSystem(URI uri, Map<String, ?> env) throws IOException {
		Path path = Paths.get(uri);
		FileSystem system = FileSystems.newFileSystem(path, ENV);
		cache.put(path, system);
		return system;
	}

	/**
	 * getPath() 経由で呼ばれる<br>
	 * 呼び出しタイミング：Paths.get(uri) の内部
	 */
	@Override
	public FileSystem getFileSystem(URI uri) {
		// ファイルシステム格納変数宣言
		FileSystem system;
		// 対象ファイルシステムを判別するためホスト名を取得
		String host = uri.getHost();
		if (URINameParser.DEFAULT_HOST.equals(host)) {
			// デフォルト設定の場合、仮想ファイルシステムを生成
			Path zipPath = toZipPath(uri);
			// キャッシュの存在確認
			system = cache.get(zipPath);
			if (Objects.isNull(system)) {
				try {
					// ファイルシステムが存在しない場合、新規で生成し本インスタンスで管理
					system = newFileSystem(zipPath.toUri(), ENV);
				} catch (IOException ioException) {
					throw new UncheckedIOException(ioException);
				}
			}
		} else if (URINameParser.BINARY_HOST.equals(host)) {
			// 物理アーカイブ設定の場合、通常のファイルシステムを生成
			system = FileSystems.getDefault();
		} else {
			// 非対応設定の場合例外をスロー
			IOException e = new IOException(host + " is not find host");
			throw new UncheckedIOException(e);
		}
		return system;
	}

	/**
	 * URIをパスに変換します
	 * @param uri KagerowURI
	 * @return ZIPファイルパス
	 */
	private static Path toZipPath(URI uri) {
		// ファイルパス生成のためのシステム設定を取得
		String archivePath = VMOption.APP_IO_ARCHIVEDATADIR.getVMoption();
		String userHome = AppPathUtils.getBasePath();
		// 物理ファイル名をURLエンコード
		String filePath = URLDecoder.decode(uri.getFragment(), StandardCharsets.UTF_8);
		// ファイルパスへ変換
		Path zipPath = Paths.get(userHome, archivePath)
				.normalize()
				.toAbsolutePath()
				.resolve(filePath);
		// ファイルパス返却
		return zipPath;
	}

	/**
	 * URIをパスに変換します
	 * @param uri KagerowURI
	 * @return ファイルパス
	 */
	private static Path toRealPath(URI uri) {
		// ファイルパス生成のためのシステム設定を取得
		String archivePath = VMOption.APP_IO_ARCHIVEDATADIR.getVMoption();
		String userHome = AppPathUtils.getBasePath();
		// URIのパス部分を取得
		String path = uri.getPath();
		// 絶帝パスの場合、相対パスへ変換
		if (path.startsWith(StringUtils.SLASH_DELIMIT)) {
			path = path.substring(1);
		}
		// ファイルパスへ変換
		Path realPath = Paths.get(userHome, archivePath)
				.normalize()
				.toAbsolutePath()
				.resolve(path);
		// ファイルパス返却
		return realPath;
	}

	/**
	 * URI→Path変換<br>
	 * 呼び出しタイミング：Paths.get(uri)
	 */
	@Override
	public Path getPath(URI uri) {
		String host = uri.getHost();
		FileSystem fs = getFileSystem(uri);
		if (URINameParser.BINARY_HOST.equals(host)) {
			return toRealPath(uri);
		} else {
			return fs.getPath(uri.getPath());
		}

	}

	/** {@inheritDoc} */
	@Override
	public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs)
			throws IOException {
		return Files.newByteChannel(path, options, attrs);
	}

	/** {@inheritDoc} */
	@Override
	public DirectoryStream<Path> newDirectoryStream(Path dir, Filter<? super Path> filter) throws IOException {
		return Files.newDirectoryStream(dir, filter);
	}

	/** {@inheritDoc} */
	@Override
	public void createDirectory(Path dir, FileAttribute<?>... attrs) throws IOException {
		Files.createDirectory(dir, attrs);
	}

	/** {@inheritDoc} */
	@Override
	public void delete(Path path) throws IOException {
		Files.delete(path);
	}

	/** {@inheritDoc} */
	@Override
	public void copy(Path source, Path target, CopyOption... options) throws IOException {
		Files.copy(source, target, options);
	}

	/** {@inheritDoc} */
	@Override
	public void move(Path source, Path target, CopyOption... options) throws IOException {
		Files.move(source, target, options);
	}

	/** {@inheritDoc} */
	@Override
	public boolean isSameFile(Path path, Path path2) throws IOException {
		return Files.isSameFile(path, path2);
	}

	/** {@inheritDoc} */
	@Override
	public boolean isHidden(Path path) throws IOException {
		return Files.isHidden(path);
	}

	/** {@inheritDoc} */
	@Override
	public FileStore getFileStore(Path path) throws IOException {
		return Files.getFileStore(path);
	}

	/** {@inheritDoc} */
	@Override
	@Deprecated
	public void checkAccess(Path path, AccessMode... modes) throws IOException {
		createException(path);
	}

	/** {@inheritDoc} */
	@Override
	public <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> type, LinkOption... options) {
		return Files.getFileAttributeView(path, type, options);
	}

	/** {@inheritDoc} */
	@Override
	public <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options)
			throws IOException {
		return Files.readAttributes(path, type, options);
	}

	/** {@inheritDoc} */
	@Override
	public Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options) throws IOException {
		return Files.readAttributes(path, attributes, options);
	}

	/** {@inheritDoc} */
	@Override
	public void setAttribute(Path path, String attribute, Object value, LinkOption... options) throws IOException {
		Files.setAttribute(path, attribute, value, options);
	}

	/**
	 * 未サポートのメソッド専用のメソッドです
	 * @param <T> 例外型
	 * @param path パスインスタンス
	 * @return ダミーの返却値
	 * @throws IOException 未サポート例外
	 */
	private <T> T createException(Path path) throws IOException {
		IllegalAccessException exception = new IllegalAccessException(Objects.toString(path));
		throw new IOException(exception);
	}

}

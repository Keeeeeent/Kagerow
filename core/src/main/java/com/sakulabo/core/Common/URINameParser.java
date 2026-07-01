package com.sakulabo.core.Common;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Hashtable;
import java.util.HexFormat;
import java.util.Objects;
import java.util.UUID;

import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NameParser;
import javax.naming.NamingException;
import javax.naming.NoPermissionException;

import com.sakulabo.core.Kagerow.Context.Impl.KagerowContextImpl;
import com.sakulabo.core.Provides.ArchiveSystemProvider;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Kagerowコンテキスト名称生成ファクトリクラスです<br>
 * 生成された名前はKagerow仮想ファイルシステムの検索キーとしても使用可能です
 * 
 * @author keeeeeent
 */
public final class URINameParser implements NameParser {

	/** デフォルトホスト名 */
	public static final String DEFAULT_HOST = "application";
	/** バイナリホスト名 */
	public static final String BINARY_HOST = "binary";
	/** zipファイル拡張子 */
	private static final String ZIP_FILE_EXT = ".zip";
	/** スキーマ名称 */
	private final String name;

	/**
	 * デフォルトコンストラクタ
	 * @param name スキーマ名称
	 */
	public URINameParser(String name) {
		this.name = Objects.requireNonNull(name);
	}

	/**
	 * デフォルトコンストラクタ
	 * @param name スキーマ名称
	 */
	public URINameParser(Name name) {
		Objects.requireNonNull(name);
		this.name = Objects.toString(name);
	}

	/**
	 * システムのデフォルト検索名称を返却します
	 * @return 名称インスタンス
	 * @throws NamingException コンテキスト生成失敗
	 */
	@SuppressFBWarnings({ "BC_IMPOSSIBLE_CAST" })
	public Name getSystemName() throws NamingException {
		// コンテキストインスタンス生成
		Hashtable<String, String> env = new Hashtable<>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, KagerowContextImpl.FACTORY_SPI_NAME);
		Context context = new InitialContext(env);
		KagerowContextImpl impl = (KagerowContextImpl) context;
		return (Name) impl.getName().clone();
	}

	/** {@inheritDoc} */
	@Override
	public Name parse(String name) throws NamingException {
		// 引数チェック
		String[] line = name.split(StringUtils.SHARP_DELIMIT);
		if (line.length != 2) {
			throw new NoPermissionException(ErrorMessage.CODE_006.getMessage(name));
		}
		// 名前インスタンス初期化
		Name nm = new CompositeName();
		for (String n : line) {
			nm.add(n);
		}
		return nm;
	}

	/**
	 * 指定された文字列形式のパスをKagerow仮想ファイルシステムに対応したURIに変換します
	 * @param path 対象内部パス
	 * @return URI
	 * @throws URISyntaxException URI解析失敗
	 */
	public URI toURI(Name path) throws URISyntaxException {
		return toURI(path.toString());
	}

	/**
	 * 指定された文字列形式のパスをKagerow仮想ファイルシステムに対応したURIに変換します
	 * @param path 対象内部パス
	 * @return URI
	 * @throws URISyntaxException URI解析失敗
	 */
	public URI toURI(String path) throws URISyntaxException {
		return toURI(path, DEFAULT_HOST);
	}

	/**
	 * 指定された文字列形式のパスをKagerow仮想ファイルシステムに対応したURIに変換します
	 * @param path 対象内部パス
	 * @param subSystem サブシステム
	 * @return URI
	 * @throws URISyntaxException URI解析失敗
	 */
	public URI toURI(String path, String subSystem) throws URISyntaxException {
		if (!path.startsWith(StringUtils.SLASH_DELIMIT)) {
			path = StringUtils.SLASH_DELIMIT.concat(path);
		}
		String fragment = null;
		if (DEFAULT_HOST.equals(subSystem)) {
			fragment = URLEncoder.encode(name.concat(ZIP_FILE_EXT), StandardCharsets.UTF_8);
		}
		return new URI(ArchiveSystemProvider.SCHOME, subSystem, path, fragment);
	}

	/**
	 * URIを形式を崩さずに結合します
	 * @param uri 結合元
	 * @param path 結合パス
	 * @return 結合結果URI
	 * @throws URISyntaxException URI解析失敗
	 */
	public static URI joinURI(URI uri, String path) throws URISyntaxException {
		// 基本情報を取得
		String schome = uri.getScheme();
		String fragment = uri.getFragment();
		String host = uri.getHost();
		// パスを結合
		String uriPath = uri.resolve(path).getPath();
		// 新たなURIを生成
		uri = new URI(schome, host, uriPath, fragment);
		return uri;
	}

	/**
	 * バイナリファイルパスを生成します
	 * @param algorithm アルゴリズム
	 * @param extension 拡張子
	 * @return バイナリファイルパス
	 * @throws NoSuchAlgorithmException アルゴリズム不正
	 */
	public String createBinaryPath(String algorithm, String extension) throws NoSuchAlgorithmException {
		MessageDigest digest = MessageDigest.getInstance(algorithm);
		digest.update(name.getBytes(StandardCharsets.UTF_8));
		String hash = HexFormat.of().formatHex(digest.digest());
		return String.join(StringUtils.SLASH_DELIMIT, hash, UUID.randomUUID().toString()).concat(extension);
	}

	/**
	 * 物理ヘッダー名称を論理ヘッダー名称に変換します
	 * @return 論理ヘッダー名称
	 * @throws NoSuchAlgorithmException アルゴリズム不正
	 */
	public String createHeaderRealPath() throws NoSuchAlgorithmException {
		MessageDigest digest = MessageDigest.getInstance(StringUtils.MD5);
		digest.update(name.getBytes(StandardCharsets.UTF_8));
		String hash = HexFormat.of().formatHex(digest.digest());
		return hash;
	}

	/**
	 * 物理ヘッダー名称を論理ヘッダー名称に変換します
	 * @param binaryHeader 物理ヘッダー名称
	 * @return 論理ヘッダー名称
	 */
	public String createHeaderBinaryPath(String binaryHeader) {
		String[] spliter = binaryHeader.split(StringUtils.SHARP_DELIMIT, 2);
		return StringUtils.SLASH_DELIMIT.concat(spliter[0]);
	}

}
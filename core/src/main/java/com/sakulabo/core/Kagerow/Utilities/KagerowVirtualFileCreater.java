package com.sakulabo.core.Kagerow.Utilities;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.function.Consumer;

import javax.naming.NameAlreadyBoundException;
import javax.naming.NamingException;

import com.sakulabo.core.Kagerow.Contents.KagerowVirtualFileContent.KagerowVirtualFileObject;
import com.sakulabo.core.Kagerow.Context.KagerowVirtualDirContext;
import com.sakulabo.core.Kagerow.Exception.AppLogicException;
import com.sakulabo.core.Kagerow.Exception.VirtualFileConstructionFailException;
import com.sakulabo.core.Kagerow.Utilities.KagerowChunkCreater.ChunkCreateMode;
import com.sakulabo.core.Processor.archive.BasicVirtualFileCreater;
import com.sakulabo.core.Processor.archive.SecureVirtualFileCreater;

/**
 * Kagerow専用仮想DB生成インターフェイスです
 * 
 * @author keeeeeent
 */
public interface KagerowVirtualFileCreater {

	/**
	 * 指定されたファイルパスから文字コードを判定します
	 * @param path 判定対象
	 * @return 判定結果文字コードインスタンス
	 * @throws IOException ファイル読み込み失敗、文字コード判定不可
	 */
	public Charset getCharset(Path path) throws IOException;

	/**
	 * 仮想ファイルオブジェクトを生成します
	 * @return 仮想ファイルオブジェクト
	 * @throws IOException データセット生成失敗、取得ファイル不正、初期化エラー
	 * @throws AppLogicException コンテキスト取得失敗
	 */
	public KagerowVirtualFileObject createVirtualFileObject() throws AppLogicException, IOException;

	/**
	 * Kagerowが管理する仮想ディレクトリコンテキストを取得します
	 * @return 仮想ディレクトリコンテキスト
	 * @throws NamingException コンテキストが見つからない場合
	 */
	public KagerowVirtualDirContext getVirtualDirContext() throws NamingException;

	/**
	 * 仮想DB物理ファイルを生成、管理下に配置します
	 * @return 物理ファイルURI
	 * @throws NameAlreadyBoundException 既に同等の仮想DB物理ファイルが生成されている場合
	 * @throws VirtualFileConstructionFailException 仮想DB物理ファイル生成失敗
	 */
	public URI construction() throws NameAlreadyBoundException, VirtualFileConstructionFailException;

	/**
	 * KDBの構築に必要なデータ構造の準備をします
	 * 
	 * @param mode 実行モード
	 * @param schema スキーマファイル名
	 * @param path 入力ファイル
	 * @param charset 入力ファイル文字コード
	 * @param isHeader ヘッダー有無
	 * @param synonym テーブル名称のシノニム
	 * @param isSecure セキュアフラグ
	 * @param observer 進捗更新オブザーバー
	 * @return Kagerow専用URI
	 * @throws IOException DBクリエイター初期化失敗
	 * @throws NameAlreadyBoundException 既に同等の仮想DB物理ファイルが生成されている場合
	 * @throws VirtualFileConstructionFailException 仮想DB物理ファイル生成失敗
	 */
	public static URI constructionKDB(
			ChunkCreateMode mode,
			String schema,
			Path path,
			Charset charset,
			boolean isHeader,
			String synonym,
			boolean isSecure,
			Consumer<Double> observer) throws IOException, NameAlreadyBoundException, VirtualFileConstructionFailException {

		// DBクリエイター初期化
		KagerowVirtualFileCreater creater;
		if (isSecure) {
			creater = new SecureVirtualFileCreater(mode, schema, path, charset, isHeader, synonym, observer);			
		} else {
			creater = new BasicVirtualFileCreater(mode, schema, path, charset, isHeader, synonym, observer);
		}

		// 返却用変数初期化
		URI result = creater.construction();

		// URIの返却
		return result;

	}

	/**
	 * KDBの構築に必要なデータ構造の準備をします
	 * 
	 * @param mode 実行モード
	 * @param schema スキーマファイル名
	 * @param path 入力ファイル
	 * @param charset 入力ファイル文字コード
	 * @param isHeader ヘッダー有無
	 * @param synonym テーブル名称のシノニム
	 * @param isSecure セキュアフラグ
	 * @return Kagerow専用URI
	 * @throws IOException DBクリエイター初期化失敗
	 * @throws NameAlreadyBoundException 既に同等の仮想DB物理ファイルが生成されている場合
	 * @throws VirtualFileConstructionFailException 仮想DB物理ファイル生成失敗
	 */
	public static URI constructionKDB(
			ChunkCreateMode mode,
			String schema,
			Path path,
			Charset charset,
			boolean isHeader,
			String synonym,
			boolean isSecure) throws IOException, NameAlreadyBoundException, VirtualFileConstructionFailException {
		return constructionKDB(
				mode,
				schema,
				path,
				charset,
				isHeader,
				synonym,
				isSecure,
				null);
	}

}

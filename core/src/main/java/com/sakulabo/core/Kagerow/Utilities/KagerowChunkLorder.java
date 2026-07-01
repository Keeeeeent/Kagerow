package com.sakulabo.core.Kagerow.Utilities;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Path;
import java.sql.SQLException;

import com.sakulabo.core.Kagerow.Contents.KagerowVirtualFileContent.KagerowVirtualFileObject;
import com.sakulabo.core.Kagerow.Contents.KagerowVirtualFileContent.KagerowVirtualFileObject.BasicFileObject;
import com.sakulabo.core.Kagerow.Exception.AppLogicException;
import com.sakulabo.core.Processor.archive.BasicChunkLorder;
import com.sakulabo.core.Processor.archive.SecureChunkLorder;

/**
 * Kagerowアプリケーションのチャンクロード機能規定インターフェイスです
 * 
 * @author keeeeeent
 */
public sealed interface KagerowChunkLorder<T extends KagerowVirtualFileObject>
		permits BasicChunkLorder, SecureChunkLorder {

	/**
	 * 標準のチャンクローダーを生成するファクトリメソッドです
	 * @param mode ロードモード
	 * @param path KDB出力先
	 * @return チャンクローダー
	 */
	public static KagerowChunkLorder<BasicFileObject> newBasicChunkLorder(
			KagerowDBMode mode,
			Path path) {
		return new BasicChunkLorder(mode, path);

	}

	/**
	 * 標準のチャンクローダーを生成するファクトリメソッドです
	 * @param mode ロードモード
	 * @param path KDB出力先
	 * @return チャンクローダー
	 */
	public static KagerowChunkLorder<KagerowVirtualFileObject> newSecureChunkLorder(
			KagerowDBMode mode,
			Path path) {
		return new SecureChunkLorder(mode, path);
	}

	/**
	 * チャンクからKDBを生成します
	 * @param chunk チャンクファイル
	 * @throws SQLException 初期化SQL実行失敗
	 * @throws IOException チャンクリーダー生成失敗
	 * @throws AppLogicException コンテキスト取得失敗
	 */
	void lord(T chunk) throws AppLogicException, IOException, SQLException;

	/**
	 * 処理に必要なスレッド数を計算します
	 * @param size データサイズ
	 * @return スレッド必要数
	 */
	public int canSeparate(BigInteger size);

}

package com.sakulabo.core.Kagerow.Utilities;

import com.sakulabo.core.Processor.archive.CSVFileReaderFactory;
import com.sakulabo.core.Processor.archive.TSVFileReaderFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;

/**
 * ファイルの読み取り実装提供を行う規定クラスです
 * @author keeeeeent
 */
public abstract class KagerowFileReaderFactory {

	/**
	 * ボディーリーダーを生成します
	 * @param path     入力元パス
	 * @param charset  読み込み文字コード
	 * @param isHeader ヘッダー有無
	 * @return 生成されたボディーリーダー
	 * @throws IOException 取得ファイル不正、初期化エラー
	 */
	public abstract KagerowFileBodyReader createFileBodyReader(Path path, Charset charset, boolean isHeader)
			throws IOException;

	/**
	 * ヘッダーリーダーを生成します
	 * @param path     入力元パス
	 * @param charset  読み込み文字コード
	 * @param isHeader ヘッダー有無
	 * @return 生成されたヘッダーリーダー
	 * @throws IOException 取得ファイル不正、初期化エラー
	 */
	public abstract KagerowFileHeaderReader createFileHeaderReader(Path path, Charset charset, boolean isHeader)
			throws IOException;


	/**
	 * CSVファイル読み取り専用ファクトリクラスを生成します
	 * @return 読み取りインスタンス
	 */
	public static KagerowFileReaderFactory newCSVFileReaderFactory() {
		return new CSVFileReaderFactory();
	}

	/**
	 * TSVファイル読み取り専用ファクトリクラスを生成します
	 * @return 読み取りインスタンス
	 */
	public static KagerowFileReaderFactory newTSVFileReaderFactory() {
		return new TSVFileReaderFactory();
	}

}

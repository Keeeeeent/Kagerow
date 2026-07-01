package com.sakulabo.core.Processor.archive;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;

import com.sakulabo.core.Common.StringUtils;

/**
 * CSVファイルの読み取り基底実装提供クラスです
 * 
 * @author keeeeeent
 */
public class CSVFileReader extends FileReader {

	/**
	 * デフォルトコンストラクタ
	 * @param path 入力元パス
	 * @param charset 読み込み文字コード
	 * @param isHeader ヘッダー有無
	 * @throws IOException 取得ファイル不正、初期化エラー
	 */
	CSVFileReader(Path path, Charset charset, boolean isHeader) throws IOException {
		super(StringUtils.COMMA, path, charset, isHeader);
	}

}

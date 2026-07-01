package com.sakulabo.core.Processor.archive;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;

import com.sakulabo.core.Common.StringUtils;
import com.sakulabo.core.Kagerow.Utilities.KagerowFileBodyReader;

/**
 * TSVファイルのボディ読み取り実装提供クラスです
 * 
 * @author keeeeeent
 */
public class TSVFileBodyReader extends KagerowFileBodyReader {

	/**
	 * デフォルトコンストラクタ
	 * @param path 入力元パス
	 * @param charset 読み込み文字コード
	 * @param isHeader ヘッダー有無
	 * @throws IOException 取得ファイル不正、初期化エラー
	 */
	protected TSVFileBodyReader(Path path, Charset charset, boolean isHeader) throws IOException {
		super(StringUtils.TAB, path, charset, isHeader);
	}

}

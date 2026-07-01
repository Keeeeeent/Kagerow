package com.sakulabo.core.Kagerow.Utilities;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Objects;

import com.sakulabo.core.Kagerow.Contents.KagerowVirtualFileContent.KagerowDataType;
import com.sakulabo.core.Processor.archive.DataSizeHandler;
import com.sakulabo.core.Processor.archive.DataTypeHandler;
import com.sakulabo.core.Processor.archive.FileReader;

/**
 * ファイルのボディ読み取り実装提供クラスです
 * 
 * @author keeeeeent
 */
public abstract class KagerowFileBodyReader extends FileReader {

	/** 初回呼び出しフラグ */
	private volatile boolean initFlug;
	/** データタイプ初期化 */
	private volatile DataTypeHandler dataTypeHandler;
	/** データサイズ初期化 */
	private volatile DataSizeHandler dataSizeHandler;

	/**
	 * デフォルトコンストラクタ
	 * @param delimit 区切り文字
	 * @param path 入力元パス
	 * @param charset 読み込み文字コード
	 * @param isHeader ヘッダー有無
	 * @throws IOException 取得ファイル不正、初期化エラー
	 */
	protected KagerowFileBodyReader(char delimit, Path path, Charset charset, boolean isHeader) throws IOException {
		super(delimit, path, charset, isHeader);
	}

	/**
	 * ボディー情報取得
	 * @return ボディー情報
	 * @throws IOException データ取得エラー、内部データ更新エラー
	 */
	@Override
	public synchronized String[] readLine() throws IOException {

		// 初回呼び出しで尚且つ、ヘッダーありのファイルの場合読み飛ばし
		if (Boolean.FALSE.compareTo(initFlug) == 0 && super.isHeader) {
			super.readLine();
			initFlug = true;
		}

		// データレコード取得
		String[] dataRecord = super.readLine();

		// 取得結果がnullの場合、更新処理はスキップ
		if (Objects.isNull(dataRecord)) {
			return dataRecord;
		}

		// データタイプ更新
		if (Objects.isNull(dataTypeHandler)) {
			dataTypeHandler = new DataTypeHandler(dataRecord);
		} else {
			dataTypeHandler.update(dataRecord);
		}
		// データサイズ更新
		if (Objects.isNull(dataSizeHandler)) {
			dataSizeHandler = new DataSizeHandler(dataRecord);
		} else {
			dataSizeHandler.update(dataRecord);
		}

		return dataRecord;
	}

	/**
	 * データタイプを取得します
	 * @return データタイプ
	 */
	public KagerowDataType[] getType() {
		return Objects.isNull(dataTypeHandler) ? new KagerowDataType[0] : dataTypeHandler.getDataType();
	}

	/**
	 * データサイズを取得します
	 * @return データサイズ
	 */
	public long[] getSize() {
		return Objects.isNull(dataSizeHandler) ? new long[0] : dataSizeHandler.getDataSize();
	}

}

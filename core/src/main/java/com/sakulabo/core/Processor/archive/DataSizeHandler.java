package com.sakulabo.core.Processor.archive;

import java.util.Arrays;
import java.util.Objects;

import com.sakulabo.core.Common.ErrorMessage;

/**
 * データ配列からSQLで対応可能なデータサイズを推測します
 * 
 * @author keeeeeent
 */
public final class DataSizeHandler {

	/** データタイプ保持メモリ */
	private long[] dataSizeList;

	/**
	 * デフォルトコンストラクタ
	 * @param data 初回データリスト
	 */
	public DataSizeHandler(String[] data) {
		dataSizeList = new long[data.length];
		Arrays.fill(dataSizeList, 1);
		update(data);
	}

	/**
	 * データサイズ更新メソッド
	 * @param data データリスト
	 */
	public void update(String[] data) {

		if (data.length != dataSizeList.length) {
			// 配列の要素数が違う場合
			throw new IllegalStateException(ErrorMessage.CODE_007.getMessage());
		}

		// フォーマットチェック処理
		for (int i = 0; i < data.length; i++) {
			long listSize = dataSizeList[i];
			long dataSize = Objects.isNull(data[i]) ? 1 : data[i].length();
			dataSizeList[i] = Math.max(listSize, dataSize);
		}

	}

	/**
	 * 現時点でのDataSizeを取得します
	 * @return DataSize配列
	 */
	public long[] getDataSize() {
		return dataSizeList;
	}

}

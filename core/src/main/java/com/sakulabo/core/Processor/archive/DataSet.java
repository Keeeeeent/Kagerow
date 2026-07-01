package com.sakulabo.core.Processor.archive;

import java.util.ArrayList;
import java.util.List;

import com.sakulabo.core.Kagerow.Contents.KagerowVirtualFileContent.KagerowDataType;

/**
 * インプットデータのデータ構造体です
 * 
 * @param schema スキーマ
 * @param table テーブル名
 * @param synonym シノニム
 * @param columnList カラムリスト
 * @param dataType データ種別リスト
 * @param dataSize データサイズリスト
 * @param rowList データリスト
 * 
 * @author keeeeeent
 * 
 */
public record DataSet(
		String schema,
		String table,
		String synonym,
		String[] columnList,
		KagerowDataType[] dataType,
		long[] dataSize,
		List<String[]> rowList) {

	/**
	 * コンストラクタ（データリスト短縮）
	 * 
	 * @param schema スキーマ
	 * @param table テーブル名
	 * @param columnList カラムリスト
	 * @param dataType データ種別リスト
	 * @param dataSize データサイズリスト
	 */
	public DataSet(
			String schema,
			String table,
			String[] columnList,
			KagerowDataType[] dataType,
			long[] dataSize) {
		this(schema, table, "", columnList, dataType, dataSize, new ArrayList<>());
	}

}
package com.sakulabo.core.Processor.database;

import java.sql.Connection;
import java.sql.SQLException;

import com.sakulabo.core.Processor.archive.DataSet;

/**
 * KSQL生成基底クラスです
 * 
 * @author keeeeeent
 */
public sealed abstract class AppKSQLCreater
		permits KDBCreater,
		KViewCreater,
		EmptyCreater,
		DDLCreater,
		DMLCreater {

	/** データ構造体 */
	protected DataSet dataSet;

	/**
	 * デフォルトコンストラクタ
	 * @param dataSet データ構造体インスタンス
	 */
	AppKSQLCreater(DataSet dataSet) {
		this.dataSet = dataSet;
	}

	/**
	 * SQL生成メソッド
	 * @return 生成されたSQL
	 */
	public abstract String toSql();

	/**
	 * KDB構築に向けた初期化処理を実施します
	 * @param connection コネクション
	 * @throws SQLException KDB構築時、SQL関連例外
	 */
	public abstract void init(Connection connection) throws SQLException;

	/**
	 * バイナリー名称をKDBテーブル名に変換します
	 * @param binaryName バイナリ名称
	 * @return　KDBテーブル名
	 */
	public static final String toTabelName(String binaryName) {
		return "\"" + "KDB_" + binaryName.toUpperCase() + "\"";
	}

}

package com.sakulabo.core.Kagerow.Utilities;

/**
 * KDBモード規定クラスです
 * 
 * @author keeeeeent
 */
public enum KagerowDBMode {

	/** Oracleモード */
	ORACLE("Oracle"),
	/** MySQLモード */
	MYSQL("MySQL"),
	/** H2モード */
	H2("REGULAR"),
	/** PostgreSQLモード */
	POSTGRESQL("PostgreSQL"),
	/** 不正・未定義 */
	ILLEGALITY("Illegality");

	/** KDBモード */
	private String mode;

	/**
	 * JVM向けコンストラクタ
	 * @param mode KDBモード
	 */
	private KagerowDBMode(String mode) {
		this.mode = mode;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return mode;
	}

	/**
	 * 受け取った文字列からモード選択的に取得します
	 * @param target 解析対象文字列
	 * @return 生成されたモード
	 */
	public static KagerowDBMode toMode(String target) {
		KagerowDBMode result = ILLEGALITY;
		for (KagerowDBMode m : values()) {
			if (m.mode.equalsIgnoreCase(target)) {
				result = m;
				break;
			}
		}
		return result;
	}

}

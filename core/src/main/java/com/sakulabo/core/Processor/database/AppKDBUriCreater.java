package com.sakulabo.core.Processor.database;

import com.sakulabo.core.Common.StringUtils;
import com.sakulabo.core.Kagerow.Utilities.KagerowDBMode;

/**
 * KDB接続用URL生成基底クラスです
 * 
 * @author keeeeeent
 */
abstract class AppKDBUriCreater {

	/** ファイルモードURIプレフィックス */
	private static final String JDBC_URI_PREFIX = "jdbc:h2:";
	/** DBモード指定サフィックス */
	private static final String JDBC_URI_SUFFIX = ";TRACE_LEVEL_FILE=0;MODE=";
	/** 実行モード */
	KagerowDBMode mode;

	/**
	 * デフォルトコンストラクタ
	 * @param mode KDBモード
	 */
	AppKDBUriCreater(KagerowDBMode mode) {
		this.mode = mode;
	}

	/**
	 * KDB接続用のURLを生成します
	 * @return URL
	 */
	String toUri() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(JDBC_URI_PREFIX);
		buffer.append(createMode() + ":");
		buffer.append(getDatabaseName());
		buffer.append(JDBC_URI_SUFFIX);
		buffer.append(mode.toString());
		buffer.append(option());
		return new String(buffer);
	}

	/**
	 * データベース名称を取得します
	 * @return データベース名称
	 */
	abstract String getDatabaseName();

	/**
	 * H2DBを構築する際の出力モードを設定します
	 * @return 出力モード
	 */
	abstract String createMode();

	/**
	 * H2DBを構築する時のモードを指定します
	 * @return オプション
	 */
	String option() {
		return StringUtils.EMPTY;
	}

}

package com.sakulabo.core.Processor.log;

import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * アプリケーションロガー専用コンソールハンドラー
 * 
 * @author keeeeeent
 */
final class AppLoggerFilter implements Filter {

	/** ログレベル */
	private Level level;

	/**
	 * デフォルトコンストラクタ
	 * @param level レベル
	 */
	AppLoggerFilter(String level) {
		this.level = Level.parse(level);
	}

	/** {@inheritDoc} */
	@Override
	public boolean isLoggable(LogRecord record) {
		return level.intValue() <= record.getLevel().intValue();
	}

}
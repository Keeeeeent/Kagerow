package com.sakulabo.core.Processor.log;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

/**
 * アプリケーションロガー専用コンソールハンドラー
 * 
 * @author keeeeeent
 */
final class AppConsoleHandler extends StreamHandler {

	/**
	 * デフォルトコンストラクタ
	 * @param formatter ログフォーマット
	 */
	public AppConsoleHandler(Formatter formatter) {
		super(System.out, formatter);
	}

	/** {@inheritDoc} */
	@Override
	public synchronized void publish(LogRecord record) {
		// 処理を移譲
		super.publish(record);
		flush();

	}

}

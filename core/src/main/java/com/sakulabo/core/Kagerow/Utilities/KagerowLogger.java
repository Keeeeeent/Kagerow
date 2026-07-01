package com.sakulabo.core.Kagerow.Utilities;

import java.lang.System.Logger;
import java.util.logging.Level;

import com.sakulabo.core.Processor.log.AppLogger;
import com.sakulabo.core.Processor.log.CommonLogger;

/**
 * Kagerowアプリケーションの汎用ログインターフェイスです
 * 
 * @author keeeeeent
 */
public sealed interface KagerowLogger permits AppLogger {

	/**
	 * Kagerowが生成したSystemロガーを返却します
	 * @return ロガーインスタンス
	 */
	public static Logger newSystemLogger() {
		return CommonLogger.getInstance();
	}

	/**
	 * Kagerow専用のロガーを生成します
	 * @return ロガーインスタンス
	 */
	public static KagerowLogger newAppLogger() {
		return AppLogger.getLogger();
	}

	/**
	 * 指定されたレベルに合わせてログを出力します
	 * @param level ログレベル
	 * @param msg 出力内容
	 * @param param パラメータ引数
	 */
	public void log(Level level, String msg, Object[] param);

	/**
	 * 例外ログを出力します
	 * @param error 例外
	 */
	public void err(Throwable error);

}

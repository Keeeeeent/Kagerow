package com.sakulabo.application.app.rpc.exception;

import java.net.HttpURLConnection;

/**
 * 認証失敗を表す例外クラスです
 */
public class IllegalCertificationException extends BaseServerException {

	/**
	 * デフォルトコンストラクタ
	 *
	 * @param message 送信メッセージ
	 * @param cause   原因例外
	 */
	public IllegalCertificationException(String message, Throwable cause) {
		super(message, HttpURLConnection.HTTP_UNAUTHORIZED, cause);
	}

	/**
	 * デフォルトコンストラクタ
	 *
	 * @param message 送信メッセージ
	 */
	public IllegalCertificationException(String message) {
		super(message, HttpURLConnection.HTTP_UNAUTHORIZED, null);
	}

}

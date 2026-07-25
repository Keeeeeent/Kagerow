package com.sakulabo.application.app.rpc.exception;

import java.net.HttpURLConnection;

/**
 * RPCエンドポイントで発生制した想定外自称を表す例外クラスです
 */
public class RpcRuntmeException extends BaseServerException {

	/**
	 * デフォルトコンストラクタ
	 *
	 * @param message 送信メッセージ
	 * @param cause   原因例外
	 */
	public RpcRuntmeException(String message, Throwable cause) {
		super(message, HttpURLConnection.HTTP_INTERNAL_ERROR, cause);
	}

	/**
	 * デフォルトコンストラクタ
	 *
	 * @param message 送信メッセージ
	 */
	public RpcRuntmeException(String message) {
		super(message, HttpURLConnection.HTTP_INTERNAL_ERROR, null);
	}

}
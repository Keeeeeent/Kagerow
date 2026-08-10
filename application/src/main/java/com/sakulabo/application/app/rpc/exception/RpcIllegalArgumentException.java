package com.sakulabo.application.app.rpc.exception;

import java.net.HttpURLConnection;

/**
 * RPCエンドポイントで発生制した引数不正を表す例外クラスです
 */
public class RpcIllegalArgumentException extends BaseServerException {

	/**
	 * デフォルトコンストラクタ
	 *
	 * @param message 送信メッセージ
	 * @param cause   原因例外
	 */
	public RpcIllegalArgumentException(String message, Throwable cause) {
		super(message, HttpURLConnection.HTTP_BAD_REQUEST, cause);
	}

	/**
	 * デフォルトコンストラクタ
	 *
	 * @param message 送信メッセージ
	 */
	public RpcIllegalArgumentException(String message) {
		super(message, HttpURLConnection.HTTP_BAD_REQUEST, null);
	}

	/**
	 * 対象の指定が必須であることを表す例外を生成します
	 * @param paramName パラメータ名称
	 * @return 生成された例外
	 */
	public static RpcIllegalArgumentException createNullArgument(String paramName) {
		return new RpcIllegalArgumentException(paramName + "is Required");
	}

}

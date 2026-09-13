package com.sakulabo.application.app.rpc.exception;

import com.sakulabo.application.app.rpc.RpcSendParam;
import com.sakulabo.application.app.rpc.datatype.send.IntegerSendDataType;
import com.sakulabo.application.app.rpc.datatype.send.StringSendDataType;

/**
 * レスポンス発生時にFALUTレスポンスを生成する例外クラスです
 */
public class BaseServerException extends RuntimeException {

	/** エラーメッセージ */
	@RpcSendParam("errorMessage")
	public final StringSendDataType sendMessage;
	/** エラーコード */
	@RpcSendParam("errorCode")
	public final IntegerSendDataType sendCode;

	/**
	 * デフォルトコンストラクタ
	 *
	 * @param message 送信メッセージ
	 * @param code    送信コード
	 * @param cause   原因例外
	 */
	public BaseServerException(String message, int code, Throwable cause) {
		super(message, cause);
		sendMessage = new StringSendDataType(message);
		sendCode = new IntegerSendDataType(String.valueOf(code));
	}

	/**
	 * デフォルトコンストラクタ
	 *
	 * @param message 送信メッセージ
	 */
	public BaseServerException(String message) {
		this(message, 900, null);
	}

}

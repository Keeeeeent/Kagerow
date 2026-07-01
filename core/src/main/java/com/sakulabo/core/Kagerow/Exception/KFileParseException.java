package com.sakulabo.core.Kagerow.Exception;

/**
 * KFileが不正であることを表す検査例外クラスです
 *
 * @author keeeeeent
 */
public class KFileParseException extends Exception {

	/**
	 * デフォルトコンストラクタ
	 * @param cause 原因例外
	 */
	public KFileParseException(Throwable cause) {
		super(cause.getMessage(), cause);
	}

	/**
	 * デフォルトコンストラクタ
	 * @param message 例外メッセージ
	 * @param cause 原因例外
	 */
	public KFileParseException(String message, Throwable cause) {
		super(message, cause);
	}

}

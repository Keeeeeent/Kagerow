package com.sakulabo.core.Kagerow.Exception;

/**
 * 仮想DB向け物理ファイルの生成失敗を表す検査例外クラスです
 * 
 * @author keeeeeent
 */
public class VirtualFileConstructionFailException extends Exception {

	/**
	 * デフォルトコンストラクタ
	 * @param cause 原因例外
	 */
	public VirtualFileConstructionFailException(Throwable cause) {
		super(cause.getMessage(), cause);
	}

	/**
	 * デフォルトコンストラクタ
	 * @param message 例外メッセージ
	 * @param cause 原因例外
	 */
	public VirtualFileConstructionFailException(String message, Throwable cause) {
		super(message, cause);
	}

}
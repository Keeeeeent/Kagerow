package com.sakulabo.core.Kagerow.Exception;

/**
 * アプリケーションが処理ロジックに不正が発生したことを通知する例外クラスです
 * 
 * @author keeeeeent
 */
public class AppLogicException extends Exception {

	/** 内包済みフラグ */
	private boolean isCause;

	/**
	 * デフォルトコンストラクタ
	 * @param msg エラーメッセージ
	 */
	public AppLogicException(String msg) {
		this(msg, null);
	}

	/**
	 * デフォルトコンストラクタ
	 * @param msg エラーメッセージ
	 * @param error 原因となった例外
	 */
	public AppLogicException(String msg, Throwable error) {
		super(msg, error);
	}

	/**
	 * 内包例外追加
	 * @param e 例外
	 */
	public final synchronized void addCause(Throwable e) {
		if (!isCause) {
			isCause = true;
		}
		addSuppressed(e);
	}

	/**
	 * 内包済みフラグを返却します
	 * @return 内包有無
	 */
	public final synchronized boolean isCause() {
		return isCause;
	}

}

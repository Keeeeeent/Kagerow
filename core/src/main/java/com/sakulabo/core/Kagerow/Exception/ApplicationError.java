package com.sakulabo.core.Kagerow.Exception;

import java.lang.StackWalker.Option;
import java.util.List;
import java.util.Vector;

/**
 * アプリケーションが継続不可能でであることを表す非検査例外クラスです
 * 
 * @author keeeeeent
 */
public class ApplicationError extends Error {

	/** 呼び出し元クラス */
	private Class<?> throwClass;

	/** アプリケーション終了時に実行される処理を記録するリストです */
	private static final List<Runnable> endProcessList = new Vector<>();

	/** 例外フラグ */
	private Object flug;

	/**
	 * 共通コンストラクタ
	 */
	{
		throwClass = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
				.getCallerClass();
	}

	/**
	 * アプリケーションが予期せぬエラーにて終了する時の処理を実行し、アプリケーションを終了します
	 * @param cause 内包される例外クラス
	 */
	public ApplicationError(Throwable cause) {
		super("Application Error throwed "
				+ StackWalker
						.getInstance(Option.RETAIN_CLASS_REFERENCE)
						.getCallerClass().getCanonicalName(),
				cause);
		endProcessList.forEach(Runnable::run);
	}

	/**
	 * 例外スロー元のクラス
	 * @return 例外スロー元のクラス定義情報
	 */
	public Class<?> getThrowClass() {
		return throwClass;
	}

	/**
	 * 例外フラグを取得します
	 * @return 例外フラグ
	 */
	public Object getFlug() {
		return flug;
	}

	/**
	 * 例外フラグを設定します
	 * @param flug 例外フラグ
	 */
	public void setFlug(Object flug) {
		this.flug = flug;
	}

	/**
	 * アプリケーションシャットダウンフックに処理を追加します
	 * @param hook シャットダウンフック
	 */
	public static void addApplicationShutdownHook(Runnable hook) {
		endProcessList.add(hook);
	}

	/**
	 * アプリケーションシャットダウンフックに処理を除去します
	 * @param hook シャットダウンフック
	 */
	public static void removeApplicationShutdownHook(Runnable hook) {
		endProcessList.remove(hook);
	}

}

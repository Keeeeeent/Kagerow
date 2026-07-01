package com.sakulabo.launcher.Initer;

import java.io.Serial;
import java.lang.StackWalker.Option;

/**
 * 初期化処理にて発生した例外をハンドリングするための例外クラスです
 * 
 * @author keeeeeent
 */
public class InitProcessFailedException extends Exception {

	/** シリアライズID */
	@Serial
	private static final long serialVersionUID = -5961404565733006656L;

	/**
	 * エラータイプ識別子
	 */
	public static enum FailType {

		/** 異常終了 */
		Reject(128),
		/** 正常終了 */
		Continue(0),
		/** スキップ終了（プログラム内部限定） */
		Skip(-1);

		/** 終了時に返却するリターンコード */
		private int exit;

		@SuppressWarnings("javadoc")
		FailType(int exit) {
			this.exit = exit;
		}

		/**
		 * プログラム終了コードを取得します
		 * @return 終了コード値
		 */
		public int getExitCode() {
			if (FailType.Skip.exit == exit)
				throw new RuntimeException("プログラム内部限定の為使用できません");
			return exit;
		}

	}

	/** エラータイプ */
	private final FailType type;
	/** エラースロークラス */
	private Class<?> clazz;
	/** 個別エラーフラグ */
	private Object flug;

	{
		// 呼び出し元クラス取得
		clazz = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).getCallerClass();
	}

	/**
	 * @param error エラーメッセージ
	 * @param type 例外処理タイプ
	 */
	public InitProcessFailedException(String error, FailType type) {
		super(error);
		this.type = type;
	}

	/**
	 * 初期化例外生成コンストラクタ
	 * 
	 * @param cause 原因となった例外クラス
	 * @param type 例外処理タイプ
	 */
	public InitProcessFailedException(Throwable cause, FailType type) {
		super(cause);
		this.type = type;
	}

	/**
	 * 初期化例外生成コンストラクタ
	 * 
	 * @param cause 原因となった例外クラス
	 * @param type 例外処理タイプ
	 * @param flug 個別例外フラグ
	 */
	public InitProcessFailedException(Throwable cause, FailType type, Object flug) {
		super(cause);
		this.type = type;
		this.flug = flug;
	}

	/**
	 * 初期化例外生成コンストラクタ
	 * 
	 * @param error エラーメッセージ
	 * @param cause 原因となった例外クラス
	 * @param type 例外処理タイプ
	 */
	public InitProcessFailedException(String error, Throwable cause, FailType type) {
		super(error, cause);
		this.type = type;
	}

	/**
	 * 例外処理タイプを返却します
	 * @return 例外処理タイプ
	 */
	public final FailType getType() {
		return type;
	}

	/**
	 * 呼び出し元クラスを返却します
	 * @return 呼び出し元クラス情報
	 */
	public final Class<?> getThrowClass() {
		return clazz;
	}

	/**
	 * 個別エラーフラグを返却します
	 * @return 個別エラーフラグ
	 */
	public final Object getFlug() {
		return flug;
	}

}

package com.sakulabo.core.Kagerow.Exception;

import java.io.InputStream;

/**
 * 実行計画の実行に失敗したことを表す検査例外クラスです
 *
 * @author keeeeeent
 */
public sealed class KagerowExecuteException extends Exception {

	/**
	 * デフォルトコンストラクタ
	 * @param cause 原因例外
	 */
	public KagerowExecuteException(Throwable cause) {
		super(cause.getMessage(), cause);
	}

	/**
	 * デフォルトコンストラクタ
	 * @param message 例外メッセージ
	 * @param cause 原因例外
	 */
	public KagerowExecuteException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * KDB構築処理に失敗したことを表す検査例外クラスです
	 *
	 * @author keeeeeent
	 */
	public static final class CreateKDBException extends KagerowExecuteException {

		/**
		 * デフォルトコンストラクタ
		 * @param cause 原因例外
		 */
		public CreateKDBException(Throwable cause) {
			super(cause.getMessage(), cause);
		}

		/**
		 * デフォルトコンストラクタ
		 * @param message 例外メッセージ
		 * @param cause 原因例外
		 */
		public CreateKDBException(String message, Throwable cause) {
			super(message, cause);
		}

	}

	/**
	 * 入力プラグイン処理に失敗したことを表す検査例外クラスです
	 *
	 * @author keeeeeent
	 */
	public static final class InputPluginException extends KagerowExecuteException {

		/**
		 * デフォルトコンストラクタ
		 * @param cause 原因例外
		 */
		public InputPluginException(Throwable cause) {
			super(cause.getMessage(), cause);
		}

		/**
		 * デフォルトコンストラクタ
		 * @param message 例外メッセージ
		 * @param cause 原因例外
		 */
		public InputPluginException(String message, Throwable cause) {
			super(message, cause);
		}

	}

	/**
	 * 出力プラグイン処理に失敗したことを表す検査例外クラスです
	 *
	 * @author keeeeeent
	 */
	public static final class OutputPluginException extends KagerowExecuteException {

		/**
		 * デフォルトコンストラクタ
		 * @param cause 原因例外
		 */
		public OutputPluginException(Throwable cause) {
			super(cause.getMessage(), cause);
		}

		/**
		 * デフォルトコンストラクタ
		 * @param message 例外メッセージ
		 * @param cause 原因例外
		 */
		public OutputPluginException(String message, Throwable cause) {
			super(message, cause);
		}

	}

	/**
	 * KSQL実行処理に失敗したことを表す検査例外クラスです
	 *
	 * @author keeeeeent
	 */
	public static final class KsqlException extends KagerowExecuteException {

		/**
		 * デフォルトコンストラクタ
		 * @param cause 原因例外
		 */
		public KsqlException(Throwable cause) {
			super(cause.getMessage(), cause);
		}

		/**
		 * デフォルトコンストラクタ
		 * @param message 例外メッセージ
		 * @param cause 原因例外
		 */
		public KsqlException(String message, Throwable cause) {
			super(message, cause);
		}

	}

	/**
	 * コマンド実行に失敗したことを表す検査例外クラスです
	 * 
	 * @author keeeeeent
	 */
	public static final class CommandException extends KagerowExecuteException {

		/** 標準エラー出力 */
		private final InputStream errorStream;

		/**
		 * デフォルトコンストラクタ
		 * @param errorStream 標準エラー出力
		 * @param msg エラーメッセージ
		 */
		public CommandException(InputStream errorStream, String msg) {
			this(errorStream, msg, null);
		}

		/**
		 * デフォルトコンストラクタ
		 * @param errorStream 標準エラー出力
		 * @param msg エラーメッセージ
		 * @param error 原因となった例外
		 */
		public CommandException(InputStream errorStream, String msg, Throwable error) {
			super(msg, error);
			this.errorStream = errorStream;
		}

		/**
		 * 標準エラーストリームを取得します
		 * @return 標準エラーストリーム
		 */
		public InputStream getError() {
			return errorStream;
		}

	}

}

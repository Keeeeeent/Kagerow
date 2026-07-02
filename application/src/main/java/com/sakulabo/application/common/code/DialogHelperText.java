package com.sakulabo.application.common.code;

import java.text.MessageFormat;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * GDialogHelperで表示するテキストを管理するコード値クラスです
 *
 * @author keeeeeent
 */
public enum DialogHelperText {

	/** システムエラー */
	DialogHelper_ERROR_MSG,
	/** 警告通知 */
	DialogHelper_WARN_MSG,
	/** 通知 */
	DialogHelper_INFO_MSG,
	/** アプリ起動 */
	DialogHelper_PASS_TITLE,
	/** アプリ起動パスワードを入力してください */
	DialogHelper_PASS_MSG,
	/** パスワードが不正です */
	DialogHelper_PASS_MISS,
	/** 予期せぬエラー */
	DialogHelper_ERROR_UNEXPECTED_MSG,
	/** コンパイルエラー */
	DialogHelper_COMPILE_ERROR_MSG,
	/** バリデーションエラー */
	DialogHelper_VALIDATION_ERROR_MSG,
	/** SQL実行エラー */
	DialogHelper_SQL_ERROR_MSG,
	/** コマンド実行エラー */
	DialogHelper_CMD_ERROR_MSG,
	/** アプリ初期化 */
	DialogHelper_RESET_APP_TITLE,
	/** アプリケーションの初期化を行いますか? */
	DialogHelper_RESET_APP_MSG,
	;

	/** リソースバンドル */
	private static final ResourceBundle message;
	static {
		message = BaseCode.createResourceBundle();
	}

	/**
	 * デフォルトコンストラクタ
	 */
	private DialogHelperText() {
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		if (Objects.isNull(message)) {
			BaseCode.createException();
		}
		return message.getString(name());
	}

	/**
	 * 指定されたパラメータを埋込文字として利用した文字列を生成します
	 * @param params 埋込文字リスト
	 * @return 生成文字列
	 */
	public String toString(Object[] params) {
		if (Objects.isNull(message)) {
			BaseCode.createException();
		}
		String msg = message.getString(name());
		msg = MessageFormat.format(msg, params);
		return msg;
	}

}

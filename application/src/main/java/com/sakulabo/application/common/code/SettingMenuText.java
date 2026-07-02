package com.sakulabo.application.common.code;

import java.text.MessageFormat;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * SettingMenuで表示するテキストを管理するコード値クラスです
 *
 * @author keeeeeent
 */
public enum SettingMenuText {

	/** エラーが発生したため表示ができません */
	ERROR_001,
	/** バックアップに失敗しました */
	ERROR_002,
	/** 復元に失敗しました */
	ERROR_003,

	/** バックアップの生成に成功しました */
	INFO_001,
	/** バックアップの取込に成功しました */
	INFO_002,

	/** パスワード設定 */
	INFO_003,
	/** セキュアブートに使用するパスワードを入力してください */
	INFO_004,
	/** 設定パスワード確認 */
	INFO_005,
	/** もう一度同じパスワードを入力してください */
	INFO_006,
	/** パスワードの設定が完了しました。セキュアブートに切り替えます */
	INFO_007,
	/** セキュアブートへの切り替えが完了しました */
	INFO_008,
	/** 入力されたパスワードが異なります */
	INFO_009,
	/** パスワードを入力してください */
	INFO_010,
	/** パスワードの変更が完了しました */
	INFO_011,

	;

	/** リソースバンドル */
	private static final ResourceBundle message;
	static {
		message = BaseCode.createResourceBundle();
	}

	/**
	 * デフォルトコンストラクタ
	 */
	private SettingMenuText() {
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
	 *
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

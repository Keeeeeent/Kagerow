package com.sakulabo.application.common.code;

import java.text.MessageFormat;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * NonComponentKsqlPopupEditPanelで表示するテキストを管理するコード値クラスです
 * 
 * @author keeeeeent
 */
public enum NonComponentKsqlPopupEditPanelText {

	/** 表示可能な実行結果がありませんでした */
	EMPTY_INFO_TEXT,
	/** KDB構築前のため個別実行は使用できません */
	EMPTY_SESSION_TEXT,
	/** キャッシュ無効化ダイアログタイトル */
	IGNORE_CHASH_TITLE,
	/** キャッシュ無効化ダイアログメッセージ */
	IGNORE_CHASH_TEXT;

	/** リソースバンドル */
	private static final ResourceBundle message;
	static {
		message = BaseCode.createResourceBundle();
	}

	/**
	 * デフォルトコンストラクタ
	 */
	private NonComponentKsqlPopupEditPanelText() {
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

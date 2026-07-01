package com.sakulabo.application.common.code;

import java.text.MessageFormat;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * NonComponentPluginEditPanelで表示するテキストを管理するコード値クラスです
 * 
 * @author keeeeeent
 */
public enum NonComponentPluginEditPanelText {

	/** 編集 */
	EDIT,
	/** --- 未選択 --- */
	NOT_SELECTED,
	/** 入力プラグイン設定追加 */
	ADD_INPUT,
	/** 出力プラグイン設定追加 */
	ADD_OUTPUT
	;
	
	/** リソースバンドル */
	private static final ResourceBundle message;
	static {
		message = BaseCode.createResourceBundle();
	}

	/**
	 * デフォルトコンストラクタ
	 */
	private NonComponentPluginEditPanelText() {
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

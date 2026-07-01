package com.sakulabo.application.common.code;

import java.text.MessageFormat;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * PluginSubContextPanelで表示するテキストを管理するコード値クラスです
 * 
 * @author keeeeeent
 */
public enum PluginSubContextPanelText {

	/** プラグインの無効化に成功しました */
	SUCCESS_DISABLE,
	/** プラグインの無効化に失敗しました */
	FILE_DISABLE,
	/** プラグインの無効化が予期せぬエラーによって失敗しました */
	FILE_DISABLE_SYSTEM_ERROR,
	/** プラグインの有効化に成功しました */
	SUCCESS_ENABLE,
	/** プラグインの有効化に失敗しました */
	FILE_ENABLE,
	/** プラグインの有効化が予期せぬエラーによって失敗しました */
	FILE_ENABLE_SYSTEM_ERROR

	;

	/** リソースバンドル */
	private static final ResourceBundle message;
	static {
		message = BaseCode.createResourceBundle();
	}

	/**
	 * デフォルトコンストラクタ
	 */
	private PluginSubContextPanelText() {
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

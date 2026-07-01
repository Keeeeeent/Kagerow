package com.sakulabo.application.common.code;

import java.text.MessageFormat;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * CacheSubContextPanelで表示するテキストを管理するコード値クラスです
 * 
 * @author keeeeeent
 */
public enum CacheSubContextPanelText {
	
	/** キャッシュの削除に成功しました */
	SUCCESS_DELETED,
	/** キャッシュの削除に失敗しました */
	FAIL_DELETED,
	/** キャッシュの削除が予期せぬエラーによって失敗しました */
	FAIL_DELETED_SYSTEM_ERROR;

	/** リソースバンドル */
	private static final ResourceBundle message;
	static {
		message = BaseCode.createResourceBundle();
	}

	/**
	 * デフォルトコンストラクタ
	 */
	private CacheSubContextPanelText() {
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

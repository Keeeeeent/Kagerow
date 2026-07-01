package com.sakulabo.application.common.code;

import java.text.MessageFormat;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * NonComponentDataImportJPanelで表示するテキストを管理するコード値クラスです
 * 
 * @author keeeeeent
 */
public enum NonComponentDataImportJPanelText {

	/** 取り込み完了 */
	IMPORT_SUCCESS,
	/** 取り込みに失敗しました */
	IMPROT_FAIL;

	/** リソースバンドル */
	private static final ResourceBundle message;
	static {
		message = BaseCode.createResourceBundle();
	}

	/**
	 * デフォルトコンストラクタ
	 */
	private NonComponentDataImportJPanelText() {
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

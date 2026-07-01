package com.sakulabo.application.common.code;

import java.text.MessageFormat;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * GUIExecutionPlanAdapterで表示するテキストを管理するコード値クラスです
 * 
 * @author keeeeeent
 */
public enum GUIExecutionPlanAdapterText {

	/** 測定不可能時のメッセージ(測定不能) */
	PROP_001,
	/** 計測時間フォーマット(%d日%d時間%d分%s秒) */
	PROP_002,
	/** 計測時間フォーマット(%d時間%d分%s秒) */
	PROP_003,
	/** 計測時間フォーマット(%s秒) */
	PROP_004;

	/** リソースバンドル */
	private static final ResourceBundle message;
	static {
		message = BaseCode.createResourceBundle();
	}

	/**
	 * デフォルトコンストラクタ
	 */
	private GUIExecutionPlanAdapterText() {
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

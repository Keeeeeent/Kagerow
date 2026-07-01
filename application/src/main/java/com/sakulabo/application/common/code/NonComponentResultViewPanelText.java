package com.sakulabo.application.common.code;

import java.text.MessageFormat;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * NonComponentResultViewPanelで表示するテキストを管理するコード値クラスです
 * 
 * @author keeeeeent
 */
public enum NonComponentResultViewPanelText {

	/** 未計測 */
	MEASURE_LABEL,
	/** 測定失敗 */
	FAIL_LABEL,
	/** スクリプト実行失敗 */
	ERROR_SCRIPT,
	/** [ {0} ]の結果ビューの生成に失敗しました */
	DISPLAY_FAILURE,
	/** {0}出力に失敗しました */
	FAIL_OUTPUT,
	/** {0}は既に存在します。上書きしますか？ */
	ALREADY_EXISTS_OUTPUT,
	/** 出力確認 */
	CONFIRMATION_OUTPUT,
	/** 出力完了 */
	SUCCESS_OUTPUT,
	/** ファイルを保存してください */
	NOT_SAVE_SCRIPT
	;

	/** リソースバンドル */
	private static final ResourceBundle message;
	static {
		message = BaseCode.createResourceBundle();
	}

	/**
	 * デフォルトコンストラクタ
	 */
	private NonComponentResultViewPanelText() {
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

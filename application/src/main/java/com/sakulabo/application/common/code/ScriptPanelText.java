package com.sakulabo.application.common.code;

import java.text.MessageFormat;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * ScriptPanelで表示するテキストを管理するコード値クラスです
 * 
 * @author keeeeeent
 */
public enum ScriptPanelText {

	/** 表示可能なタブは{0}までとなってます */
	ScriptPanel_MAX_TAB,
	/** スクリプトの保存が完了しました */
	ScriptPanel_SAVE_SUCCESS,
	/** スクリプトの保存に失敗しました */
	ScriptPanel_SAVE_FAIL,
	/** 既にファイルを編集中です【対象】: {0} */
	ScriptPanel_EXITST_FILE,
	/** Kagerowスクリプトを指定してください【対象】: {0} */
	ScriptPanel_NOT_KAGEROW_FILE,
	/** Kagerowスクリプトを保存しますか？【対象】: {0} */
	ScriptPanel_NOT_SAVE_KAGEROW_FILE,
	/** 変更した内容が反映されていませんが、タブを閉じますか？ */
	ScriptPanel_CLOSE_TAB,
	/** 確認 */
	ScriptPanel_INFO,
	;

	/** リソースバンドル */
	private static final ResourceBundle message;
	static {
		message = BaseCode.createResourceBundle();
	}

	/**
	 * デフォルトコンストラクタ
	 */
	private ScriptPanelText() {
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

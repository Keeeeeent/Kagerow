package com.sakulabo.core.Processor.log;

import java.util.Optional;
import java.util.ResourceBundle;

import com.sakulabo.core.Common.StringUtils;

/**
 * アプリケーション共通で使用されるロガー向けのメッセージクラスです
 * 
 * @author keeeeeent
 */
public enum AppLogMessage {

	// ####################################################
	// COMMON
	// ####################################################

	/** Success */
	COMMON_X0001(),
	/** Fail */
	COMMON_X0002(),
	/** ArchiveFile */
	COMMON_X0003(),

	// ####################################################
	// FINER
	// ####################################################

	/** [SessionID:{0}] KSQLの実行を開始します */
	FINER_MSG_4001(),
	/** [SessionID:{0}] KSQLの実行を終了します */
	FINER_MSG_4002(),

	// ####################################################
	// FINEST
	// ####################################################

	/** Lorded {0} >> Status {1} */
	FINEST_MSG_3001(),
	/** {0} {1} : {2} */
	FINEST_MSG_3002(),

	// ####################################################
	// WARNING
	// ####################################################

	/** [Entry:{0}] 不正なバックアップエントリーです */
	WARNING_MSG9001(),
	/** [SessionID:{0}] キャッシュは無効化されています【CacheID】: {1} */
	WARNING_MSG9002(),
	/** [SessionID:{0}] キャッシュが無効化されました【CacheID】: {1} */
	WARNING_MSG9003(),
	/** パラメータはシステム管理されているため上書きされました【ParamName】: {0} */
	WARNING_MSG9004(),
	/** スクリプト環境変数はシステム管理されているため上書きされました【ScriptEnvName】: {0} */
	WARNING_MSG9005(),
	/** 日付関連スクリプト環境変数の設定に失敗しました【Pattern】: {0} */
	WARNING_MSG9006(),
	/** システム管理下のスクリプト環境変数の設定に失敗しました【Env】: {0} */
	WARNING_MSG9007();

	/** ログメッセージ */
	private static final ResourceBundle message;
	static {
		// メッセージファイル取得
		Optional<Module> module = ModuleLayer.boot().findModule(StringUtils.MODULE_NAME);
		message = ResourceBundle.getBundle(
				AppLogger.RESOURCE_FILE_NAME,
				module.get());
	}

	/**
	 * メッセージファイルに設定した共通プロパティーの値を返却します
	 * @return 共通プロパティーの値
	 */
	public String toProp() {
		return message.getString(name());
	}

}

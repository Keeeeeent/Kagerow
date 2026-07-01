package com.sakulabo.core.Kagerow.Adapter;

import java.time.Instant;

/**
 * 実行計画のライフサイクルフックを提供するアダプターインターフェイスです
 * 
 * @author keeeeeent
 */
public interface KagerowExecutionPlanAdapter {

	/**
	 * 実行結果を表現する列挙クラスです
	 */
	public static enum ExitCode {
		/** 実行の成功を意味します */
		SUCCESS,
		/** 実行の失敗を意味します */
		FAIL;
	}

	/**
	 * KSQL実行結果データ構造です
	 * @param id KSQLID
	 * @param startTime 開始時刻 
	 * @param endTime 終了時刻
	 * @param exitCode 終了コード
	 */
	public static record ExecutKsqlInfo(
			String id,
			Instant startTime,
			Instant endTime,
			ExitCode exitCode) {
	};

	/**
	 * KSQL実行結果データ構造です
	 * @param pluginName プラグイン名称
	 * @param id KSQLID
	 * @param startTime 開始時刻 
	 * @param endTime 終了時刻
	 * @param exitCode 終了コード
	 * @param option オプション
	 */
	public static record ExecutPluginInfo(
			String pluginName,
			String id,
			Instant startTime,
			Instant endTime,
			ExitCode exitCode,
			Object option) {

		/**
		 * デフォルトコンストラクタ
		 * @param pluginName プラグイン名称
		 * @param id KSQLID
		 * @param startTime 開始時刻 
		 * @param endTime 終了時刻
		 * @param exitCode 終了コード
		 */
		public ExecutPluginInfo(String pluginName,
				String id,
				Instant startTime,
				Instant endTime,
				ExitCode exitCode) {
			this(pluginName, id, startTime, endTime, exitCode, null);
		}

	};

	/**
	 * 処理の開始直前にトリガー実行されます
	 */
	void start();

	/**
	 * 全ての処理が終了した直後にトリガー実行されます
	 */
	void end();

	/**
	 * バリデーション処理の直前にトリガー実行されます
	 */
	void startValidation();

	/**
	 * バリデーション処理の直後にトリガー実行されます
	 */
	void endValidation();

	/**
	 * 仮想DB構築処理の直前にトリガー実行されます
	 */
	void startCreateKDB();

	/**
	 * 仮想DB構築処理の直後にトリガー実行されます
	 */
	void endCreateKDB();

	/**
	 * 入力プラグイン全体実行処理の直前にトリガー実行されます
	 */
	void startDoInputPlugin();

	/**
	 * 入力プラグイン全体実行処理の直後にトリガー実行されます
	 */
	void endDoInputPlugin();

	/**
	 * 入力プラグイン個別実行処理の直前にトリガー実行されます
	 */
	void startDoInputPluginIndividual();

	/**
	 * 入力プラグイン個別実行処理の直後にトリガー実行されます
	 * @param info 実行情報
	 */
	void endDoInputPluginIndividual(ExecutPluginInfo info);

	/**
	 * SQL全体実行処理の直前にトリガー実行されます
	 */
	void startDoKsql();

	/**
	 * SQ全体L実行処理の直後にトリガー実行されます
	 */
	void endDoKsql();

	/**
	 * SQL個別実行処理の直前にトリガー実行されます
	 */
	void startDoKsqlIndividual();

	/**
	 * SQL個別実行処理の直後にトリガー実行されます
	 * @param info 実行情報
	 */
	void endDoKsqlIndividual(ExecutKsqlInfo info);

	/**
	 * 出力プラグイン全体実行処理の直前にトリガー実行されます
	 */
	void startDoOutputPlugin();

	/**
	 * 出力プラグイン全体実行処理の直後にトリガー実行されます
	 */
	void endDoOutputPlugin();

	/**
	 * 出力プラグイン個別実行処理の直前にトリガー実行されます
	 */
	void startDoOutputPluginIndividual();

	/**
	 * 出力プラグイン個別実行処理の直後にトリガー実行されます
	 * @param info 実行情報
	 */
	void endDoOutputPluginIndividual(ExecutPluginInfo info);

	/**
	 * コマンド実行処理の直前にトリガー実行されます
	 */
	void startDoCmd();

	/**
	 * コマンド実行処理の直後にトリガー実行されます
	 */
	void endDoCmd();

}

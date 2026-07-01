package com.sakulabo.core.Processor.jmx.Configuration;

import com.sakulabo.core.Processor.jmx.BaseKagerowJMX;

/**
 * アプリケーションコンフィグレーションオブジェクト監視インターフェイス
 * 
 * @author keeeeeent
 */
public interface AppConfigurationMXBean extends BaseKagerowJMX {

	/**
	 * JMX向けの専用メソッドです<br/>
	 * APP_ENTRY_POINT(アプリケーションのエントリポイントに関するクラス情報)を返却します
	 * @return アプリケーションのエントリポイントに関するクラス情報
	 */
	String getAPP_ENTRY_POINT();

	/**
	 * LOG_LEVEL(アプリケーションログレベル)を返却します
	 * @return アプリケーションログレベル
	 */
	String getLOG_LEVEL();

	/**
	 * LOG_CONSOLE(アプリケーションログコンソール)を返却します
	 * @return アプリケーションログコンソール
	 */
	boolean getLOG_CONSOLE();

	/**
	 * JMX向けの専用メソッドです<br/>
	 * WAIT_TIME(DIコンテキスト初期化待ち時間)を返却します
	 * @return DIコンテキスト初期化待ち時間
	 */
	long getWAIT_TIME();

	/**
	 * KDB_CHUNK_SIZE(データファイル作成時のチャンクサイズ)を返却します
	 * @return データファイル作成時のチャンクサイズ
	 */
	int getKDB_CHUNK_SIZE();

	/**
	 * KDB_BULK_INSERT_UNIT(バルクインサートサイズ)を返却します
	 * @return バルクインサートサイズ
	 */
	int getKDB_BULK_INSERT_UNIT();

	/**
	 * KDB_THREAD_POOL_SIZE(バルクインサート時に使用するスレッドプールの容量)を返却します
	 * @return バルクインサート時に使用するスレッドプールの容量
	 */
	int getKDB_THREAD_POOL_SIZE();

	/**
	 * JMX向けの専用メソッドです<br/>
	 * KDB_MAX_PLAN_TASK_COUNT(KSQL実行計画の最大許容容量)を返却します
	 * @return KSQL実行計画の最大許容容量
	 */
	int getKDB_MAX_PLAN_TASK_COUNT();

	/**
	 * KDB_MAX_FECTH_SIZE(SQL実行の最大フェッチサイズ)を返却します
	 * @return SQL実行の最大フェッチサイズ
	 */
	int getKDB_MAX_FECTH_SIZE();

	/**
	 * KDB_MAX_RECORD_SIZE(SQL実行の最大取得レコードサイズ)を返却します
	 * @return SQL実行の最大取得レコードサイズ
	 */
	int getKDB_MAX_RECORD_SIZE();
	
	/**
	 * KDB_QUERY_URL_CACHE_SIZE(クエリ専用SQL実行の最大メモリキャッシュサイズ)を返却します
	 * @return クエリ専用SQL実行の最大メモリキャッシュサイズ
	 */
	int getKDB_QUERY_URL_CACHE_SIZE();

	/**
	 * KDB_QUERY_URL_PAGE_SIZE(クエリ専用SQL実行の最大メモリページサイズ)を返却します
	 * @return クエリ専用SQL実行の最大メモリページサイズ
	 */
	int getKDB_QUERY_URL_PAGE_SIZE();

}

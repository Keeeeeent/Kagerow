package com.sakulabo.core.Kagerow.Utilities;

import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.Semaphore;

import com.sakulabo.core.Processor.config.AppConfiguration;
import com.sakulabo.core.Processor.config.AppConfigurationLorder;
import com.sakulabo.regulation.annotation.KagerowComponent.AppInitComponet;

/**
 * Kagerowアプリケーション専用コンフィグレーションインターフェイスです
 * 
 * @author keeeeeent
 */
public sealed interface KagerowConfiguration permits AppConfiguration {

	/**
	 * Kagerowアプリケーションのコンフィグレーションを取得します
	 * @return コンフィグインスタンス
	 */
	public static KagerowConfiguration getConfiguration() {
		return AppConfigurationLorder.getConfig();
	}

	/**
	 * APP_ENTRY_POINT(アプリケーションのエントリポイントに関するクラス情報)を返却します
	 * @return アプリケーションのエントリポイントに関するクラス情報
	 */
	Class<? extends AppInitComponet> APP_ENTRY_POINT();

	/**
	 * LOG_LEVEL(アプリケーションログレベル)を返却します
	 * @return アプリケーションログレベル
	 */
	String LOG_LEVEL();

	/**
	 * LOG_CONSOLE(アプリケーションログコンソール)を返却します
	 * @return アプリケーションログコンソール
	 */
	boolean LOG_CONSOLE();

	/**
	 * WAIT_TIME(DIコンテキスト初期化待ち時間)を返却します
	 * @return DIコンテキスト初期化待ち時間
	 */
	Duration WAIT_TIME();

	/**
	 * KDB_CHUNK_SIZE(データファイル作成時のチャンクサイズ)を返却します
	 * @return データファイル作成時のチャンクサイズ
	 */
	int KDB_CHUNK_SIZE();

	/**
	 * KDB_DATE_FORMAT(CSV日付フォーマットリスト)を返却します
	 * @return CSV日付フォーマットリスト
	 */
	List<DateTimeFormatter> KDB_DATE_FORMAT();

	/**
	 * KDB_BULK_INSERT_UNIT(バルクインサートサイズ)を返却します
	 * @return バルクインサートサイズ
	 */
	int KDB_BULK_INSERT_UNIT();

	/**
	 * KDB_THREAD_POOL_SIZE(バルクインサート時に使用するスレッドプールの容量)を返却します
	 * @return バルクインサート時に使用するスレッドプールの容量
	 */
	int KDB_THREAD_POOL_SIZE();

	/**
	 * KDB_MAX_PLAN_TASK_COUNT(KSQL実行計画の最大許容容量)を返却します
	 * @return KSQL実行計画の最大許容容量
	 */
	Semaphore KDB_MAX_PLAN_TASK_COUNT();

	/**
	 * KDB_MAX_FECTH_SIZE(SQL実行の最大フェッチサイズ)を返却します
	 * @return SQL実行の最大フェッチサイズ
	 */
	int KDB_MAX_FECTH_SIZE();

	/**
	 * KDB_MAX_RECORD_SIZE(SQL実行の最大取得レコードサイズ)を返却します
	 * @return SQL実行の最大取得レコードサイズ
	 */
	int KDB_MAX_RECORD_SIZE();

	/**
	 * KDB_QUERY_URL_CACHE_SIZE(クエリ専用SQL実行の最大メモリキャッシュサイズ)を返却します
	 * @return クエリ専用SQL実行の最大メモリキャッシュサイズ
	 */
	int KDB_QUERY_URL_CACHE_SIZE();

	/**
	 * KDB_QUERY_URL_PAGE_SIZE(クエリ専用SQL実行の最大メモリページサイズ)を返却します
	 * @return クエリ専用SQL実行の最大メモリページサイズ
	 */
	int KDB_QUERY_URL_PAGE_SIZE();

}

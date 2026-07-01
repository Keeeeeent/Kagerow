package com.sakulabo.core.Processor.config;

import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.Semaphore;

import com.sakulabo.core.Kagerow.Utilities.KagerowConfiguration;
import com.sakulabo.core.Processor.jmx.AppJMX;
import com.sakulabo.core.Processor.jmx.Configuration.AppConfigurationMXBean;
import com.sakulabo.regulation.annotation.KagerowComponent.AppInitComponet;

/**
 * Kagerowアプリケーション専用コンフィグレーションクラスです
 * 
 * @author keeeeeent
 * @param APP_ENTRY_POINT アプリケーションのエントリポイントに関するクラス情報
 * @param LOG_LEVEL アプリケーションログレベル
 * @param LOG_CONSOLE アプリケーションログコンソール
 * @param WAIT_TIME DIコンテキスト初期化待ち時間
 * @param KDB_CHUNK_SIZE データファイル作成時のチャンクサイズ
 * @param KDB_DATE_FORMAT CSV日付フォーマット
 * @param KDB_BULK_INSERT_UNIT バルクインサートサイズ
 * @param KDB_THREAD_POOL_SIZE バルクインサート時に使用するスレッドプールの容量
 * @param KDB_MAX_PLAN_TASK_COUNT KSQL実行計画の最大許容容量
 * @param KDB_MAX_FECTH_SIZE SQL実行の最大フェッチサイズ
 * @param KDB_MAX_RECORD_SIZE SQL実行の最大取得レコードサイズ
 * @param KDB_QUERY_URL_CACHE_SIZE クエリ専用SQL実行の最大メモリキャッシュサイズ
 * @param KDB_QUERY_URL_PAGE_SIZE クエリ専用SQL実行の最大メモリページサイズ
 */
@AppJMX(name = "Configuration", options = { "type=AppConfiguration" })
public record AppConfiguration(
		Class<? extends AppInitComponet> APP_ENTRY_POINT,
		String LOG_LEVEL,
		boolean LOG_CONSOLE,
		Duration WAIT_TIME,
		int KDB_CHUNK_SIZE,
		List<DateTimeFormatter> KDB_DATE_FORMAT,
		int KDB_BULK_INSERT_UNIT,
		int KDB_THREAD_POOL_SIZE,
		Semaphore KDB_MAX_PLAN_TASK_COUNT,
		int KDB_MAX_FECTH_SIZE,
		int KDB_MAX_RECORD_SIZE,
		int KDB_QUERY_URL_CACHE_SIZE,
		int KDB_QUERY_URL_PAGE_SIZE) implements KagerowConfiguration, AppConfigurationMXBean {

	/** {@inheritDoc} */
	@Override
	public String getAPP_ENTRY_POINT() {
		return APP_ENTRY_POINT.getCanonicalName();
	}

	/** {@inheritDoc} */
	@Override
	public String getLOG_LEVEL() {
		return LOG_LEVEL;
	}

	/** {@inheritDoc} */
	@Override
	public boolean getLOG_CONSOLE() {
		return LOG_CONSOLE;
	}

	/** {@inheritDoc} */
	@Override
	public long getWAIT_TIME() {
		return WAIT_TIME.toNanos();
	}

	/** {@inheritDoc} */
	@Override
	public int getKDB_CHUNK_SIZE() {
		return KDB_CHUNK_SIZE;
	}

	/** {@inheritDoc} */
	@Override
	public int getKDB_BULK_INSERT_UNIT() {
		return KDB_BULK_INSERT_UNIT;
	}

	/** {@inheritDoc} */
	@Override
	public int getKDB_THREAD_POOL_SIZE() {
		return KDB_THREAD_POOL_SIZE;
	}

	/** {@inheritDoc} */
	@Override
	public int getKDB_MAX_PLAN_TASK_COUNT() {
		return KDB_MAX_PLAN_TASK_COUNT.availablePermits();
	}

	/** {@inheritDoc} */
	@Override
	public int getKDB_MAX_FECTH_SIZE() {
		return KDB_MAX_FECTH_SIZE;
	}

	/** {@inheritDoc} */
	@Override
	public int getKDB_MAX_RECORD_SIZE() {
		return KDB_MAX_RECORD_SIZE;
	}

	/** {@inheritDoc} */
	@Override
	public int getKDB_QUERY_URL_CACHE_SIZE() {
		return KDB_QUERY_URL_CACHE_SIZE;
	}

	/** {@inheritDoc} */
	@Override
	public int getKDB_QUERY_URL_PAGE_SIZE() {
		return KDB_QUERY_URL_PAGE_SIZE;
	}

}

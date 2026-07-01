package com.sakulabo.core.Processor.config;

import java.lang.System.Logger.Level;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.Semaphore;

import javax.management.InstanceAlreadyExistsException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sakulabo.core.Kagerow.Utilities.KagerowConfiguration;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;
import com.sakulabo.core.Processor.jmx.AppJMXInitializer;
import com.sakulabo.regulation.annotation.KagerowComponent.AppInitComponet;

/**
 * Kagerowアプリケーション専用コンフィグレーションレコード生成クラスです
 * 
 * @author keeeeeent
 */
public final class AppConfigurationLorder extends ConfigurationLorder<KagerowConfiguration> {

	/** アプリケーションログレベル */
	private String LOG_LEVEL;
	/** アプリケーションログコンソール */
	private boolean LOG_CONSOLE;

	/** アプリケーションのエントリポイントに関するクラス情報 */
	private Class<? extends AppInitComponet> APP_ENTRY_POINT;
	/** DIコンテキスト初期化待ち時間 */
	private Duration WAIT_TIME;

	/** データファイル作成時のチャンクサイズ */
	private int KDB_CHUNK_SIZE;
	/** CSV日付フォーマット */
	private List<DateTimeFormatter> KDB_DATE_FORMAT;
	/** バルクインサートサイズ（単位：件） */
	private int KDB_BULK_INSERT_UNIT;
	/** バルクインサート時に使用するスレッドプールの容量 */
	private int KDB_THREAD_POOL_SIZE;
	/** 実行計画の最大許容容量 */
	private Semaphore KDB_MAX_PLAN_TASK_COUNT;
	/** SQL実行の最大フェッチサイズ */
	private int KDB_MAX_FECTH_SIZE;
	/** SQL実行の最大取得レコードサイズ */
	private int KDB_MAX_RECORD_SIZE;
	/** クエリ専用SQL実行の最大メモリキャッシュサイズ */
	private int KDB_QUERY_URL_CACHE_SIZE;
	/** クエリ専用SQL実行の最大メモリページサイズ */
	private int KDB_QUERY_URL_PAGE_SIZE;

	/**
	 * インスタンス生成不可
	 */
	private AppConfigurationLorder() {
		;
	}

	/**
	 * コンフィグレーションレコードを返却します
	 * @return コンフィグレーションレコード
	 */
	public static KagerowConfiguration getConfig() {
		AppConfigurationLorder config = new AppConfigurationLorder();
		return config.load();
	}

	/** {@inheritDoc} */
	@Override
	protected KagerowConfiguration build() {
		// コンフィグレーション生成
		AppConfiguration config = new AppConfiguration(
				APP_ENTRY_POINT,
				LOG_LEVEL,
				LOG_CONSOLE,
				WAIT_TIME,
				KDB_CHUNK_SIZE,
				KDB_DATE_FORMAT,
				KDB_BULK_INSERT_UNIT,
				KDB_THREAD_POOL_SIZE,
				KDB_MAX_PLAN_TASK_COUNT,
				KDB_MAX_FECTH_SIZE,
				KDB_MAX_RECORD_SIZE,
				KDB_QUERY_URL_CACHE_SIZE,
				KDB_QUERY_URL_PAGE_SIZE);
		// JMX登録
		try {
			AppJMXInitializer.registMXBean(config);
		} catch (Exception e) {
			if (!(e instanceof InstanceAlreadyExistsException)) {
				KagerowLogger.newSystemLogger().log(Level.ERROR, e);
			}
		}
		return config;
	}

	/** {@inheritDoc} */
	@Override
	@SuppressWarnings("unchecked")
	protected void lordConfigFile(Document document, XPath xPath) throws Exception {

		/** アプリケーションログレベル */
		expr = xPath.compile("/KagerowApplication/Logger/Level");
		node = (Node) expr.evaluate(document, XPathConstants.NODE);
		LOG_LEVEL = node.getTextContent();

		/** アプリケーションログコンソール */
		expr = xPath.compile("/KagerowApplication/Logger/Console");
		node = (Node) expr.evaluate(document, XPathConstants.NODE);
		LOG_CONSOLE = Boolean.valueOf(node.getTextContent());

		/**
		 * DIプロパティー
		 */

		/** DIコンテキスト初期化待ち時間 */
		expr = xPath.compile("/KagerowApplication/DIContext/Wait-Time");
		node = (Node) expr.evaluate(document, XPathConstants.NODE);
		{
			String tmp = node.getTextContent();
			WAIT_TIME = Duration.of(Long.valueOf(tmp), ChronoUnit.SECONDS);
		}

		/** アプリケーションのエントリポイントに関するクラス情報 */
		expr = xPath.compile("/KagerowApplication/DIContext/Entry-Point");
		node = (Node) expr.evaluate(document, XPathConstants.NODE);
		{
			if (Objects.nonNull(node)) {
				String classInfo = node.getTextContent();
				APP_ENTRY_POINT = (Class<? extends AppInitComponet>) Class.forName(classInfo);
			} else {
				APP_ENTRY_POINT = AppInitComponet.DefaultInitComponet.class;
			}
		}

		/**
		 * KDBプロパティー
		 */

		/** データファイル作成時のチャンクサイズ */
		expr = xPath.compile("/KagerowApplication/KDB/Chunk-Creater/Chunk-size");
		node = (Node) expr.evaluate(document, XPathConstants.NODE);
		KDB_CHUNK_SIZE = Integer.valueOf(node.getTextContent()).intValue();

		/** データファイル作成時のチャンクサイズ */
		expr = xPath.compile("/KagerowApplication/KDB/Chunk-Lorder/Bulk-Insert-Unit");
		node = (Node) expr.evaluate(document, XPathConstants.NODE);
		KDB_BULK_INSERT_UNIT = Integer.valueOf(node.getTextContent()).intValue();

		/** データファイル作成時のチャンクサイズ */
		expr = xPath.compile("/KagerowApplication/KDB/Chunk-Lorder/Thread-Pool-Size");
		node = (Node) expr.evaluate(document, XPathConstants.NODE);
		KDB_THREAD_POOL_SIZE = Integer.valueOf(node.getTextContent()).intValue();

		/** CSV日付フォーマット */
		expr = xPath.compile("/KagerowApplication/KDB/Chunk-Creater/DateFormat/Case");
		nodeList = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
		DateTimeFormatter formatter = null;
		List<DateTimeFormatter> tmpList = new ArrayList<>();
		for (int i = 0; i < nodeList.getLength(); i++) {
			if (nodeList.item(i) instanceof Element element) {
				String format = element.getAttribute("format");
				String locale = element.getAttribute("locale");
				if (!locale.isEmpty()) {
					String[] line = locale.split("(_|-)");
					if (line.length == 2) {
						Locale loc = new Locale.Builder()
								.setLanguage(line[0])
								.setRegion(line[1])
								.build();
						formatter = DateTimeFormatter.ofPattern(format, loc);
					} else if (line.length == 1) {
						Locale loc = new Locale.Builder().setLanguage(line[0]).build();
						formatter = DateTimeFormatter.ofPattern(format, loc);
					}
				} else {
					formatter = DateTimeFormatter.ofPattern(format);
				}
				if (Objects.nonNull(formatter)) {
					tmpList.add(formatter);
				}
			}
		}
		KDB_DATE_FORMAT = tmpList.stream().distinct().toList();

		/** 実行計画の最大許容容量 */
		expr = xPath.compile("/KagerowApplication/KDB/Execution-Plan/Max-Task-Size");
		node = (Node) expr.evaluate(document, XPathConstants.NODE);
		{
			int permitsCount = Integer.valueOf(node.getTextContent()).intValue();
			KDB_MAX_PLAN_TASK_COUNT = new Semaphore(permitsCount);
		}

		/** 実行計画の最大許容容量 */
		expr = xPath.compile("/KagerowApplication/KDB/Execution-Plan/Max-Fecth-Size");
		node = (Node) expr.evaluate(document, XPathConstants.NODE);
		KDB_MAX_FECTH_SIZE = Integer.valueOf(node.getTextContent()).intValue();

		/** 実行計画の最大許容容量 */
		expr = xPath.compile("/KagerowApplication/KDB/Execution-Plan/Max-Record-Size");
		node = (Node) expr.evaluate(document, XPathConstants.NODE);
		KDB_MAX_RECORD_SIZE = Integer.valueOf(node.getTextContent()).intValue();

		/** クエリ専用SQL実行の最大メモリキャッシュサイズ */
		expr = xPath.compile("/KagerowApplication/KDB/Query-Uri-Creater/Cache-Size");
		node = (Node) expr.evaluate(document, XPathConstants.NODE);
		KDB_QUERY_URL_CACHE_SIZE = Integer.valueOf(node.getTextContent()).intValue();

		/** クエリ専用SQL実行の最大メモリページサイズ */
		expr = xPath.compile("/KagerowApplication/KDB/Query-Uri-Creater/Page-Size");
		node = (Node) expr.evaluate(document, XPathConstants.NODE);
		KDB_QUERY_URL_PAGE_SIZE = Integer.valueOf(node.getTextContent()).intValue();

	}

}

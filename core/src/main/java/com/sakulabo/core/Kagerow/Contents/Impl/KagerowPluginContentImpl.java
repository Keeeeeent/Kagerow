package com.sakulabo.core.Kagerow.Contents.Impl;

import java.sql.Connection;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.ThreadFactory;

import javax.naming.Name;
import javax.naming.NamingException;

import com.sakulabo.core.Common.StringUtils;
import com.sakulabo.core.Kagerow.Adapter.KagerowExecutionPlanAdapter;
import com.sakulabo.core.Kagerow.Adapter.KagerowExecutionPlanAdapter.ExecutPluginInfo;
import com.sakulabo.core.Kagerow.Adapter.KagerowExecutionPlanAdapter.ExitCode;
import com.sakulabo.core.Kagerow.Contents.KagerowPluginContent;
import com.sakulabo.core.Kagerow.Contents.KagerowPluginContent.Data;
import com.sakulabo.core.Kagerow.Contents.KagerowPluginContent.Data.InputData;
import com.sakulabo.core.Kagerow.Contents.KagerowPluginContent.Data.OutputData;
import com.sakulabo.core.Kagerow.Utilities.KagerowDBMode;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;
import com.sakulabo.core.Processor.plugin.KagerowClassLoader;
import com.sakulabo.core.Processor.plugin.PluginParamCache;
import com.sakulabo.core.Processor.plugin.PluginParamParser;
import com.sakulabo.regulation.annotation.KagerowPlugin;
import com.sakulabo.regulation.annotation.KagerowPlugin.PluginType;
import com.sakulabo.regulation.spi.PluginAdapter;

/**
 * Kagerowアプリケーションのプラグインコンテンツ実装クラスです
 * @author keeeeeent
 */
public final class KagerowPluginContentImpl
		extends BaseKagerowContent
		implements KagerowPluginContent,
		Flow.Subscriber<Data>,
		ThreadFactory {

	/** プラグインアダプターインスタンス */
	private final PluginAdapter plugin;
	/** プラグイン名称 */
	private final String name;
	/** プラグイン実行可能数 */
	private final int multiSize;
	/** プラグイン種別 */
	private final List<PluginType> pluginType;
	/** パブリッシャー */
	private final SubmissionPublisher<Data> publisher;
	/** サブスクリプション */
	private Subscription subscription;
	/** エグゼキューター */
	private final ExecutorService executor;
	/** 専用クラスローダー */
	private final KagerowClassLoader classLoader;
	/** プラグインパラメータキャッシュ */
	private final PluginParamCache paramCache;

	/**
	 * デフォルトコンストラクタ
	 * @param plugin      プラグインアダプター
	 * @param classLoader 専用クラスローダー
	 */
	public KagerowPluginContentImpl(PluginAdapter plugin, KagerowClassLoader classLoader) {

		// アダプター保持
		this.plugin = plugin;
		// クラスローダー保持
		this.classLoader = classLoader;
		// 注釈情報取得
		KagerowPlugin pluginInfo = plugin.getClass().getAnnotation(KagerowPlugin.class);
		// プラグイン名称取得
		this.name = pluginInfo.name();
		// プラグイン種別取得
		this.pluginType = Arrays.asList(pluginInfo.types());
		// プラグイン実行可能数取得
		this.multiSize = pluginInfo.multiSize();
		// エグゼキューター生成
		this.executor = Executors.newThreadPerTaskExecutor(this);
		// パブリッシャー生成
		this.publisher = new SubmissionPublisher<>(executor, multiSize);
		// サブスクライバー登録
		this.publisher.subscribe(this);
		// プラグインパラメータキャッシュ
		this.paramCache = new PluginParamCache();

	}

	/** {@inheritDoc} */
	@Override
	public void input(Map<String, String> params, KagerowDBMode mode, Connection connection) {
		this.plugin.input(params, mode, connection);
	}

	/** {@inheritDoc} */
	@Override
	public void output(Map<String, String> params, List<KagerowRowSet> data) {
		this.plugin.output(params, data);
	}

	/** {@inheritDoc} */
	@Override
	public void validation(Map<String, String> params, PluginType type) throws PluginValidationException {
		this.plugin.validation(params, type);
	}

	/** {@inheritDoc} */
	@Override
	public String getNameInNamespace() throws NamingException {
		return name;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isSupportType(PluginType type) {
		return pluginType.contains(type);
	}

	/** {@inheritDoc} */
	@Override
	public void submit(Data data) {
		publisher.submit(data);
	}

	/** {@inheritDoc} */
	@Override
	public void onSubscribe(Subscription subscription) {
		this.subscription = subscription;
		this.subscription.request(multiSize);
		this.plugin.initialize();
	}

	/** {@inheritDoc} */
	@Override
	public void onError(Throwable throwable) {
		KagerowLogger.newAppLogger().err(throwable);
	}

	/** {@inheritDoc} */
	@Override
	public void onComplete() {
		// スレッドプールシャットダウン
		this.executor.shutdownNow();
	}

	/** {@inheritDoc} */
	@Override
	public void onNext(Data item) {

		// 通知向け情報格納メモリ
		Instant startTime = Instant.now();
		ExitCode exitCode = ExitCode.SUCCESS;
		Throwable error = null;

		try {
			// 実行処理振り分け
			switch (item) {
			case InputData input -> {
				// 開始リスナー実行
				input.planAdapter().ifPresent(KagerowExecutionPlanAdapter::startDoInputPluginIndividual);
				// プラグイン実行
				input(input.param(), input.mode(), input.connection());
			}
			case OutputData output -> {
				// 開始リスナー実行
				output.planAdapter().ifPresent(KagerowExecutionPlanAdapter::startDoOutputPluginIndividual);
				// プラグイン実行
				output(output.param(), output.data());
			}
			}
		} catch (Throwable e) {
			exitCode = ExitCode.FAIL;
			error = e;
			KagerowLogger.newAppLogger().err(e);
		} finally {
			// 通知データ生成
			ExecutPluginInfo info = new ExecutPluginInfo(
					name,
					item.id(),
					startTime,
					Instant.now(),
					exitCode,
					error);
			// 実行処理振り分け
			switch (item) {
			case InputData input -> {
				// 開始リスナー実行
				input.planAdapter().ifPresent(adp -> {
					adp.endDoInputPluginIndividual(info);
				});
			}
			case OutputData output -> {
				// 開始リスナー実行
				output.planAdapter().ifPresent(adp -> {
					adp.endDoOutputPluginIndividual(info);
				});
			}
			}
			// 通知実施
			item.notice();
			// リクエスト補充
			subscription.request(1);
		}

	}

	/** {@inheritDoc} */
	@Override
	public Thread newThread(Runnable r) {
		// KagerowClassLoarderを取得
		return this.classLoader.currentThread(name, r);
	}

	/** {@inheritDoc} */
	@Override
	public void close() {

		try {
			// ユーザスレッドを取得
			Thread thread = this.classLoader.shutdownThread(name, this.plugin);
			// スレッド起動
			thread.start();
			// 終了処理待機
			thread.join();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// コンテンツ終了
		this.publisher.close();
	}

	/**
	 * プラグインアダプター本体を返却します
	 * @return プラグインアダプター本体
	 */
	public PluginAdapter toAdapter() {
		return this.plugin;
	}

	/**
	 * 専用クラスローダーを返却します
	 * @return KagerowClassLoaderインスタンス
	 */
	public KagerowClassLoader getClassLoader() {
		return classLoader;
	}

	/** {@inheritDoc} */
	@Override
	public void addListener(KagerowContentEventKind kind, KagerowContentEventListener listener) {
		throw new IllegalCallerException();
	}

	/** {@inheritDoc} */
	@Override
	public PluginInfo toPluginInfo() {
		// パッケージ名称取得
		String packageName = classLoader.getPluginPkg().orElse(StringUtils.DEFAULT);
		// プラグイン名称取得
		String pluginName = name;
		// バージョン情報取得
		Optional<Name> version = classLoader.getPluginVersion();
		int majorVersion = 1, minorVersion = 0, patchVersion = 0;
		if (version.isPresent()) {
			Name rawVersion = version.get();
			majorVersion = Integer.parseInt(rawVersion.get(1));
			minorVersion = Integer.parseInt(rawVersion.get(2));
			patchVersion = Integer.parseInt(rawVersion.get(3));
		}
		// プラグイン情報取得
		KagerowPlugin pluginInfo = plugin.getClass().getAnnotation(KagerowPlugin.class);
		// パラメータ情報取得
		Map<String, PluginAdapter.Param[]> pluginParamInfo = paramCache.get(plugin.getClass());
		Map<PluginType, List<PluginParamInfo>> paramMap = new HashMap<>();
		List<PluginType> pluginInfoList = List.of(pluginInfo.types());
		// 入力パラメータの情報取得
		List<PluginParamInfo> inputParam = new ArrayList<>();
		if (pluginInfoList.contains(PluginType.INPUT)) {
			for (Param param : pluginParamInfo.get(PluginParamParser.INPUT)) {
				PluginParamInfo paramInfo = new PluginParamInfo(
						param.value(),
						param.defaultValue(),
						param.required());
				inputParam.add(paramInfo);
			}
			inputParam.sort(Comparator.comparing(PluginParamInfo::name, Comparator.naturalOrder()));
		}
		paramMap.put(PluginType.INPUT, Collections.unmodifiableList(inputParam));
		// 出力プラグインの情報取得
		List<PluginParamInfo> outputParam = new ArrayList<>();
		if (pluginInfoList.contains(PluginType.OUTPUT)) {
			for (Param param : pluginParamInfo.get(PluginParamParser.OUTPUT)) {
				PluginParamInfo paramInfo = new PluginParamInfo(
						param.value(),
						param.defaultValue(),
						param.required());
				outputParam.add(paramInfo);
			}
			outputParam.sort(Comparator.comparing(PluginParamInfo::name, Comparator.naturalOrder()));
		}
		paramMap.put(PluginType.OUTPUT, Collections.unmodifiableList(outputParam));
		// 返却向け情報生成
		PluginInfo info = new PluginInfo(
				packageName,
				pluginName,
				majorVersion,
				minorVersion,
				patchVersion,
				Collections.unmodifiableMap(paramMap));
		// 結果返却
		return info;
	}

}

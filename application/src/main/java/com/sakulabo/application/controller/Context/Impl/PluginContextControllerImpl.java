package com.sakulabo.application.controller.Context.Impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;

import javax.naming.NamingException;
import javax.sql.rowset.CachedRowSet;

import com.sakulabo.application.common.code.ApplicationConstProperty;
import com.sakulabo.application.controller.BaseController;
import com.sakulabo.application.controller.Context.PluginContextController;
import com.sakulabo.application.model.Script.KSQLScriptModel;
import com.sakulabo.application.service.Context.PluginContextService;
import com.sakulabo.application.service.Context.PluginContextService.PluginContextInfo;
import com.sakulabo.core.Kagerow.Contents.KagerowPluginContent;
import com.sakulabo.core.Kagerow.Utilities.KagerowScriptAccessor.KagerowSqlAccessor;
import com.sakulabo.core.Kagerow.Utilities.KagerowUtilities;
import com.sakulabo.regulation.annotation.KagerowComponent;
import com.sakulabo.regulation.annotation.KagerowInject;
import com.sakulabo.regulation.annotation.KagerowPlugin.PluginType;
import com.sakulabo.regulation.spi.PluginAdapter.KagerowRowSet;

/**
 * プラグインコンテキストコントローラーの実装クラスです
 * 
 * @author keeeeeent
 */
@KagerowComponent
public class PluginContextControllerImpl extends BaseController implements PluginContextController {

	/** プラグインコンテキストサービス */
	@KagerowInject
	private PluginContextService pluginContextService;

	/** CSVプラグインルックアップキー */
	private final String CSV_PLUGIN_LOOKUP_KEY;
	/** TSVプラグインルックアップキー */
	private final String TSV_PLUGIN_LOOKUP_KEY;
	/** CSVプラグイン */
	private final KagerowPluginContent CSV_PLUGIN;
	/** TSVプラグイン */
	private final KagerowPluginContent TSV_PLUGIN;

	/**
	 * デフォルトコンストラクタ
	 * @throws NamingException 初期化失敗
	 */
	public PluginContextControllerImpl() throws NamingException {

		// ルックアップキー初期化
		CSV_PLUGIN_LOOKUP_KEY = KagerowUtilities.getPluginENV(ApplicationConstProperty.CSV_PLUGIN_NAMED_KEY);
		TSV_PLUGIN_LOOKUP_KEY = KagerowUtilities.getPluginENV(ApplicationConstProperty.TSV_PLUGIN_NAMED_KEY);

		// プラグイン初期化
		CSV_PLUGIN = KagerowUtilities.getPlugin(CSV_PLUGIN_LOOKUP_KEY);
		TSV_PLUGIN = KagerowUtilities.getPlugin(TSV_PLUGIN_LOOKUP_KEY);
	}

	/** {@inheritDoc} */
	@Override
	public List<PluginContextInfo> getPluginList() {
		return pluginContextService.getPluginList();
	}

	/** {@inheritDoc} */
	@Override
	public void outputCSV(KSQLScriptModel model, Map<String, String> param, Map<String, CachedRowSet> result)
			throws SQLException {
		// プラグイン専用引数生成
		List<KagerowRowSet> rowSetList = new ArrayList<>();
		result.entrySet().forEach(k -> rowSetList.add(toKagerowRowSet(model, k)));
		// プラグイン実行
		executoKagerowPlugin(model, param, rowSetList, CSV_PLUGIN::output);
	}

	/** {@inheritDoc} */
	@Override
	public Map<String, String> createCSVPluginParam() throws Exception {
		return KagerowUtilities.createPluginParam(CSV_PLUGIN_LOOKUP_KEY, PluginType.OUTPUT);
	}

	/** {@inheritDoc} */
	@Override
	public Map<String, String> bindCSVPluginParam(Map<String, String> param) throws Exception {
		return KagerowUtilities.bindPluginParam(CSV_PLUGIN_LOOKUP_KEY, PluginType.OUTPUT, param);
	}

	/** {@inheritDoc} */
	@Override
	public void outputTSV(KSQLScriptModel model, Map<String, String> param, Map<String, CachedRowSet> result)
			throws SQLException {
		// プラグイン専用引数生成
		List<KagerowRowSet> rowSetList = new ArrayList<>();
		result.entrySet().forEach(k -> rowSetList.add(toKagerowRowSet(model, k)));
		// プラグイン実行
		executoKagerowPlugin(model, param, rowSetList, TSV_PLUGIN::output);
	}

	/** {@inheritDoc} */
	@Override
	public Map<String, String> createTSVPluginParam() throws Exception {
		return KagerowUtilities.createPluginParam(TSV_PLUGIN_LOOKUP_KEY, PluginType.OUTPUT);
	}

	/** {@inheritDoc} */
	@Override
	public Map<String, String> bindTSVPluginParam(Map<String, String> param) throws Exception {
		return KagerowUtilities.bindPluginParam(TSV_PLUGIN_LOOKUP_KEY, PluginType.OUTPUT, param);
	}

	/**
	 * マップエントリーをKagerowデータ構造に変換します
	 * @param model スクリプトモデル
	 * @param dat マップエントリー
	 * @return データ構造
	 */
	private KagerowRowSet toKagerowRowSet(KSQLScriptModel model, Entry<String, CachedRowSet> dat) {
		// 論理名称取得
		String name = model.scriptAccessor.getKsqls().stream()
				.filter(f -> f.getId().equals(dat.getKey()))
				.map(KagerowSqlAccessor::getName)
				.findFirst()
				.orElse("null");
		// データセット生成
		return new KagerowRowSet(
				model.scriptAccessor.getMode().toString(),
				dat.getKey(),
				name,
				dat.getValue());
	}

	/**
	 * プラグイン実行のラッパーメソッドです
	 * @param model 実行モデル
	 * @param params 実行パラメータ
	 * @param data 実行結果セット
	 * @param processor 実行プロセッサー
	 * @throws SQLException カーソル移動失敗
	 */
	private void executoKagerowPlugin(
			KSQLScriptModel model,
			Map<String, String> params,
			List<KagerowRowSet> data,
			BiConsumer<Map<String, String>, List<KagerowRowSet>> processor)
			throws SQLException {
		// カーソル修正(実行前)
		for (KagerowRowSet rowset : data) {
			rowset.data().beforeFirst();
		}
		// メイン処理実行
		processor.accept(params, data);
		// カーソル修正(実行後)
		for (KagerowRowSet rowset : data) {
			rowset.data().beforeFirst();
		}
	}

	/** {@inheritDoc} */
	@Override
	public List<PluginContextInfo> getPluginList(PluginType type) {
		return pluginContextService.getPluginList(type);
	}

}

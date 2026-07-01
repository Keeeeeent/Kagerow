package com.sakulabo.application.controller.Context;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.sql.rowset.CachedRowSet;

import com.sakulabo.application.model.Script.KSQLScriptModel;
import com.sakulabo.application.service.Context.PluginContextService.PluginContextInfo;
import com.sakulabo.regulation.annotation.KagerowPlugin.PluginType;

/**
 * プラグインコンテキストコントローラーの規定インターフェイスです
 * 
 * @author keeeeeent
 */
public interface PluginContextController {

	/**
	 * プラグイン一覧を取得します
	 * @return プラグイン情報一覧リスト
	 */
	public List<PluginContextInfo> getPluginList();

	/**
	 * 指定したプラグインタイプの一覧を取得します
	 * @param type プラグインタイプ
	 * @return プラグイン一覧リスト
	 */
	public List<PluginContextInfo> getPluginList(PluginType type);

	/**
	 * KSQLをモデルに従ってCSV出力を行います
	 * @param model 実行モデル
	 * @param param 実行パラメータ
	 * @param result KSQL実行結果セット
	 * @throws SQLException カーソル移動失敗
	 */
	public void outputCSV(KSQLScriptModel model, Map<String, String> param, Map<String, CachedRowSet> result)
			throws SQLException;

	/**
	 * CSV出力に必要な初期化パラメータを生成します
	 * @return 初期化パラメータ
	 * @throws Exception 生成失敗
	 */
	public Map<String, String> createCSVPluginParam() throws Exception;

	/**
	 * CSV出力の実行に向けたパラメータのバインドを実施します
	 * @param param パラメータバインド
	 * @return バインド後パラメータマップ
	 * @throws Exception バインド失敗
	 */
	public Map<String, String> bindCSVPluginParam(Map<String, String> param) throws Exception;

	/**
	 * KSQLをモデルに従ってTSV出力を行います
	 * @param model 実行モデル
	 * @param param 実行パラメータ
	 * @param result KSQL実行結果セット
	 * @throws SQLException カーソル移動失敗
	 */
	public void outputTSV(KSQLScriptModel model, Map<String, String> param, Map<String, CachedRowSet> result)
			throws SQLException;

	/**
	 * TSV出力に必要な初期化パラメータを生成します
	 * @return 初期化パラメータ
	 * @throws Exception 生成失敗
	 */
	public Map<String, String> createTSVPluginParam() throws Exception;

	/**
	 * TSV出力の実行に向けたパラメータのバインドを実施します
	 * @param param パラメータバインド
	 * @return バインド後パラメータマップ
	 * @throws Exception バインド失敗
	 */
	public Map<String, String> bindTSVPluginParam(Map<String, String> param) throws Exception;

}

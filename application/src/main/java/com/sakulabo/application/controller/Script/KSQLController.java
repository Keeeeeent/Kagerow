package com.sakulabo.application.controller.Script;

import java.util.Optional;

import javax.sql.rowset.CachedRowSet;

import com.sakulabo.application.model.Script.KSQLScriptModel;
import com.sakulabo.core.Kagerow.Utilities.KagerowExecutionPlanAccessor;
import com.sakulabo.core.Kagerow.Utilities.KagerowScriptAccessor;

/**
 * KSQLスクリプトコントローラーの規定インターフェイスです
 * 
 * @author keeeeeent
 */
public interface KSQLController {

	/**
	 * KSQLをモデルに従って実行します
	 * @param model 実行モデル
	 * @return 実行結果
	 * @throws Exception 実行失敗
	 */
	public KagerowExecutionPlanAccessor executionScript(KSQLScriptModel model) throws Exception;

	/**
	 * KSQLをモデルに従って実行します
	 * @param model 実行モデル
	 * @param accessor 実行セッション
	 * @return 実行結果
	 * @throws Exception 実行失敗
	 */
	public KagerowExecutionPlanAccessor executionScript(KSQLScriptModel model, KagerowExecutionPlanAccessor accessor)
			throws Exception;

	/**
	 * KSQLを個別実行します
	 * @param ksqlId 実行対象
	 * @param script 実行モデル
	 * @param accessor 実行セッション
	 * @return 実行結果
	 * @throws Exception 実行失敗
	 */
	public Optional<CachedRowSet> executionScript(String ksqlId, KagerowScriptAccessor script,
			KagerowExecutionPlanAccessor accessor)
			throws Exception;

	/**
	 * KSQLをモデルに従って保存します
	 * @param model 実行モデル
	 * @return 実行結果
	 * @throws Exception 実行失敗
	 */
	public boolean saveScript(KSQLScriptModel model) throws Exception;

}

package com.sakulabo.application.controller.Script.Impl;

import java.nio.file.Path;
import java.util.Optional;

import javax.sql.rowset.CachedRowSet;

import com.sakulabo.application.controller.BaseController;
import com.sakulabo.application.controller.Script.KSQLController;
import com.sakulabo.application.model.Script.KSQLScriptModel;
import com.sakulabo.application.service.Script.KSQLService;
import com.sakulabo.core.Kagerow.Adapter.KagerowExecutionPlanAdapter;
import com.sakulabo.core.Kagerow.Utilities.KagerowExecutionPlanAccessor;
import com.sakulabo.core.Kagerow.Utilities.KagerowScriptAccessor;
import com.sakulabo.regulation.annotation.KagerowComponent;
import com.sakulabo.regulation.annotation.KagerowInject;

/**
 * KSQLスクリプトコントローラーの実装クラスです
 * 
 * @author keeeeeent
 */
@KagerowComponent
public class KSQLControllerImpl extends BaseController implements KSQLController {

	/** KSQL実行サービス */
	@KagerowInject
	private KSQLService ksqlService;

	/** {@inheritDoc} */
	@Override
	public KagerowExecutionPlanAccessor executionScript(KSQLScriptModel model) throws Exception {

		// 引数確認
		@SuppressWarnings("unused")
		Path path = model.path;
		@SuppressWarnings("unused")
		KagerowExecutionPlanAdapter planAdapter = model.planAdapter;
		@SuppressWarnings("unused")
		boolean isSecure = model.isSecure;

		// スクリプト実行
		KagerowExecutionPlanAccessor accessor = ksqlService.executionScript(model);

		return accessor;
	}

	/** {@inheritDoc} */
	@Override
	public KagerowExecutionPlanAccessor executionScript(KSQLScriptModel model, KagerowExecutionPlanAccessor accessor)
			throws Exception {

		// スクリプト実行
		ksqlService.executionScript(model, accessor);

		return accessor;
	}

	/** {@inheritDoc} */
	@Override
	public Optional<CachedRowSet> executionScript(String ksqlId, KagerowScriptAccessor script,
			KagerowExecutionPlanAccessor accessor) throws Exception {
		// スクリプト実行
		Optional<CachedRowSet> result = ksqlService.executionScript(ksqlId, script, accessor);
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public boolean saveScript(KSQLScriptModel model) throws Exception {
		return ksqlService.saveScript(model);
	}

}

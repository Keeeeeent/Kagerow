package com.sakulabo.application.service.Script.Impl;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.UnaryOperator;

import javax.sql.rowset.CachedRowSet;

import com.sakulabo.application.model.Script.KSQLScriptModel;
import com.sakulabo.application.service.BaseService;
import com.sakulabo.application.service.Script.KSQLService;
import com.sakulabo.core.Kagerow.Adapter.KagerowExecutionPlanAdapter;
import com.sakulabo.core.Kagerow.Utilities.KagerowExecutionPlanAccessor;
import com.sakulabo.core.Kagerow.Utilities.KagerowScriptAccessor;
import com.sakulabo.core.Kagerow.Utilities.KagerowUtilities;
import com.sakulabo.regulation.annotation.KagerowComponent;

/**
 * KSQLスクリプトサービスの実装クラスです
 *
 * @author keeeeeent
 */
@KagerowComponent
public class KSQLServiceImpl extends BaseService implements KSQLService {

	/** {@inheritDoc} */
	@Override
	public KagerowExecutionPlanAccessor executionScript(KSQLScriptModel model) throws Exception {

		// 引数抽出
		Path path = model.path;
		KagerowExecutionPlanAdapter planAdapter = model.planAdapter;
		boolean isSecure = model.isSecure;

		// スクリプト実行
		KagerowExecutionPlanAccessor accessor = KagerowExecutionPlanAccessor.execute(path, planAdapter, isSecure);

		// モデル編集
		if (Objects.isNull(model.planAdapter)) {
			model.planAdapter = KagerowExecutionPlanAccessor.createDefaultPlanAdapter(accessor);
		}

		return accessor;
	}

	/** {@inheritDoc} */
	@Override
	public KagerowExecutionPlanAccessor executionScriptWithEdit(KSQLScriptModel model,
			UnaryOperator<KagerowScriptAccessor> editOperator) throws Exception {

		// 引数抽出
		KagerowExecutionPlanAdapter planAdapter = model.planAdapter;
		boolean isSecure = model.isSecure;
		KagerowScriptAccessor newAccessor = editOperator.apply(model.scriptAccessor);

		// モデルを再生成
		KSQLScriptModel tmpModel = new KSQLScriptModel();
		tmpModel.isSecure = isSecure;
		tmpModel.scriptAccessor = newAccessor;
		tmpModel.path = KagerowUtilities.createTemporaryPath(UUID.randomUUID().toString() + ".tmp", true);
		saveScript(tmpModel);

		// スクリプト実行
		KagerowExecutionPlanAccessor accessor = KagerowExecutionPlanAccessor.execute(tmpModel.path, planAdapter,
				isSecure);

		// モデル編集
		if (Objects.isNull(model.planAdapter)) {
			model.planAdapter = KagerowExecutionPlanAccessor.createDefaultPlanAdapter(accessor);
		}

		return accessor;
	}

	/** {@inheritDoc} */
	@Override
	public KagerowExecutionPlanAccessor executionScript(KSQLScriptModel model, KagerowExecutionPlanAccessor accessor)
			throws Exception {

		// 引数抽出
		KagerowScriptAccessor script = model.scriptAccessor;
		KagerowExecutionPlanAdapter planAdapter = model.planAdapter;

		// スクリプトロード
		accessor.lord(script, planAdapter);
		// バリデーションチェック
		accessor.validation();
		// 実行計画スタート
		accessor.execute();

		return accessor;
	}

	/** {@inheritDoc} */
	@Override
	public Optional<CachedRowSet> executionScript(String ksqlId, KagerowScriptAccessor script,
			KagerowExecutionPlanAccessor accessor) throws Exception {

		// スクリプトロード
		accessor.lord(script, KagerowExecutionPlanAccessor.createDefaultPlanAdapter(accessor));
		// 実行計画スタート
		Optional<CachedRowSet> result = accessor.execute(ksqlId);

		return result;

	}

	/** {@inheritDoc} */
	@Override
	public boolean saveScript(KSQLScriptModel model) throws Exception {

		// 引数抽出
		KagerowScriptAccessor script = model.scriptAccessor;
		Path output = model.path;

		// 保存実行
		try {
			KagerowScriptAccessor.toFile(script, output);
			return true;
		} catch (Exception e) {
			logger.err(e);
			return false;
		}

	}

}

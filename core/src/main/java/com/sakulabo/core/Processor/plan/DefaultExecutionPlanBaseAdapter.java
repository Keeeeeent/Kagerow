package com.sakulabo.core.Processor.plan;

import java.util.logging.Level;

import com.sakulabo.core.Kagerow.Adapter.KagerowExecutionPlanBaseAdapter;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;
import com.sakulabo.core.Processor.log.AppLogMessage;

/**
 * 実行計画のライフサイクルフックのデフォルト実装を提供するクラスです
 * 
 * @author keeeeeent
 */
public class DefaultExecutionPlanBaseAdapter extends KagerowExecutionPlanBaseAdapter {

	/** 実行計画インスタンス */
	private ExecutionPlan<?> plan;

	/**
	 * デフォルトコンストラクタ
	 * @param plan 実行計画インスタンス
	 */
	public DefaultExecutionPlanBaseAdapter(ExecutionPlan<?> plan) {
		this.plan = plan;
	}

	/** {@inheritDoc} */
	@Override
	public void start() {
		KagerowLogger.newAppLogger().log(Level.FINER, AppLogMessage.FINER_MSG_4001.name(),
				new Object[] { plan.getSessionID().toString() });
	}

	/** {@inheritDoc} */
	@Override
	public void end() {
		KagerowLogger.newAppLogger().log(Level.FINER, AppLogMessage.FINER_MSG_4002.name(),
				new Object[] { plan.getSessionID().toString() });
	}

}

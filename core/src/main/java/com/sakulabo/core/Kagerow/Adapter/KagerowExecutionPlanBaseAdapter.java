package com.sakulabo.core.Kagerow.Adapter;

/**
 * 実行計画のライフサイクルフックのデフォルトの実装を提供する基底クラスです
 *
 * @author keeeeeent
 */
public abstract class KagerowExecutionPlanBaseAdapter implements KagerowExecutionPlanAdapter {

	/** {@inheritDoc} */
	@Override
	public void startValidation() {
		;
	}

	/** {@inheritDoc} */
	@Override
	public void endValidation() {
		;
	}

	/** {@inheritDoc} */
	@Override
	public void startCreateKDB() {
		;
	}

	/** {@inheritDoc} */
	@Override
	public void endCreateKDB() {
		;
	}

	/** {@inheritDoc} */
	@Override
	public void startDoInputPlugin() {
		;
	}

	/** {@inheritDoc} */
	@Override
	public void endDoInputPlugin() {
		;
	}

	/** {@inheritDoc} */
	@Override
	public void startDoInputPluginIndividual() {
		;
	}

	/** {@inheritDoc} */
	@Override
	public void endDoInputPluginIndividual(ExecutPluginInfo info) {
		;
	}

	/** {@inheritDoc} */
	@Override
	public void startDoKsql() {
		;
	}

	/** {@inheritDoc} */
	@Override
	public void endDoKsql() {
		;
	}

	/** {@inheritDoc} */
	@Override
	public void startDoKsqlIndividual(String id) {
		;
	}

	/** {@inheritDoc} */
	@Override
	public void endDoKsqlIndividual(ExecutKsqlInfo info) {
		;
	}

	/** {@inheritDoc} */
	@Override
	public void startDoOutputPlugin() {
		;
	}

	/** {@inheritDoc} */
	@Override
	public void endDoOutputPlugin() {
		;
	}

	/** {@inheritDoc} */
	@Override
	public void startDoOutputPluginIndividual() {
		;
	}

	/** {@inheritDoc} */
	@Override
	public void endDoOutputPluginIndividual(ExecutPluginInfo info) {
		;
	}

	/** {@inheritDoc} */
	@Override
	public void startDoCmd() {
		;
	}

	/** {@inheritDoc} */
	@Override
	public void endDoCmd() {
		;
	}

	/** {@inheritDoc} */
	@Override
	public void start() {
		;
	}

	/** {@inheritDoc} */
	@Override
	public void end() {
		;
	}

}

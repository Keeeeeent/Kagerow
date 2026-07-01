package com.sakulabo.core.Processor.manager.state;

/**
 * 次のアクションを受付可能でであることを表す状態クラスです
 * 
 * @author keeeeeent
 */
public final class ReadyState extends AbstractState implements State {

	/**
	 * デフォルトコンストラクタ
	 * @param manager 状態管理インスタンス
	 */
	public ReadyState(StateManager manager) {
		super(manager);
	}

	/** {@inheritDoc} */
	@Override
	public void begin() {
		manager.setState(new EditingState(manager));
	}

	/** {@inheritDoc} */
	@Override
	public void commit() {
		;
	}

	/** {@inheritDoc} */
	@Override
	public void rollback() {
		;
	}

	/** {@inheritDoc} */
	@Override
	public void end() {
		;
	}

}

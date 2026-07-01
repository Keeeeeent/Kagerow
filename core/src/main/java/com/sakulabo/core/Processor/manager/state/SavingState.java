package com.sakulabo.core.Processor.manager.state;

/**
 * 保存中であることを表す状態クラスです
 * 
 * @author keeeeeent
 */
public final class SavingState extends AbstractState implements State {

	/**
	 * デフォルトコンストラクタ
	 * @param manager 状態管理インスタンス
	 */
	public SavingState(StateManager manager) {
		super(manager);
	}

	/** {@inheritDoc} */
	@Override
	public void begin() {
		;
	}

	/** {@inheritDoc} */
	@Override
	public void commit() {
		manager.setState(new ReadyState(manager));
	}

	/** {@inheritDoc} */
	@Override
	public void rollback() {
		manager.setState(new EditedState(manager));
	}

	/** {@inheritDoc} */
	@Override
	public void end() {
		;
	}

}

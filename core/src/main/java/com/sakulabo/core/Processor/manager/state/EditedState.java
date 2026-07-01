package com.sakulabo.core.Processor.manager.state;

/**
 * 編集済みであることを表す状態クラスです
 * 
 * @author keeeeeent
 */
public final class EditedState extends AbstractState implements State {

	/**
	 * デフォルトコンストラクタ
	 * @param manager 状態管理インスタンス
	 */
	public EditedState(StateManager manager) {
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
		manager.setState(new SavingState(manager));
	}

}
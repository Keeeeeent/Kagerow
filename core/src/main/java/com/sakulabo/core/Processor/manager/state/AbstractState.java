package com.sakulabo.core.Processor.manager.state;

import java.util.Objects;

/**
 * ファイル状態を管理する規定実装クラスです
 * 
 * @author keeeeeent
 */
public abstract class AbstractState {

	/** ステート管理コンテキストです */
	protected final StateManager manager;

	/**
	 * デフォルトコンストラクタ
	 * @param manager 状態管理インスタンス
	 */
	protected AbstractState(StateManager manager) {
		this.manager = Objects.requireNonNull(manager);
	}

}
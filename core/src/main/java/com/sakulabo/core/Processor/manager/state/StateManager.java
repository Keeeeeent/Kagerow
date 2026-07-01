package com.sakulabo.core.Processor.manager.state;

import java.util.Objects;

import com.sakulabo.core.Kagerow.Utilities.KagerowFileState;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * ファイルマネージャ実装クラスです
 *
 * @author keeeeeent
 */
public final class StateManager implements Cloneable {

	/** ステート保持メモリ */
	private volatile State state;

	/**
	 * デフォルトコンストラクタ
	 */
	public StateManager() {
		this.state = new ReadyState(this);
	}

	/**
	 * ステートを設定します
	 * @param state ステート
	 */
	synchronized void setState(State state) {
		this.state = Objects.requireNonNull(state);
	}

	/**
	 * 現在のステートを取得します
	 * @return ステート
	 */
	@SuppressFBWarnings("UG_SYNC_SET_UNSYNC_GET")
	public KagerowFileState getState() {
		return switch (state) {
		case ReadyState _ -> KagerowFileState.Ready;
		case EditingState _,EditedState _ -> KagerowFileState.Editing;
		case SavingState _ -> KagerowFileState.Saving;
		case null, default -> KagerowFileState.Fail;
		};
	}

	/**
	 * トランザクションを開始します
	 */
	public synchronized void begin() {
		state.begin();
	};

	/**
	 * 変更を確定します
	 */
	public synchronized void commit() {
		state.commit();
	};

	/**
	 * 変更を破棄します
	 */
	public synchronized void rollback() {
		state.rollback();
	};

	/**
	 * トランザクションを終了します
	 */
	public synchronized void end() {
		state.end();
	};

	/** {@inheritDoc} */
	@Override
	public synchronized StateManager clone() throws CloneNotSupportedException {
		return (StateManager) super.clone();
	}

}

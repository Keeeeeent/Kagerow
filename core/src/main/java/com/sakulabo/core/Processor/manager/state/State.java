package com.sakulabo.core.Processor.manager.state;

/**
 * ファイル状態を規定するインターフェイスです
 * 
 * @author keeeeeent
 */
public sealed interface State
		permits SavingState, ReadyState, EditingState, EditedState {

	/**
	 * トランザクションを開始します
	 */
	public void begin();

	/**
	 * 変更を確定します
	 */
	public void commit();

	/**
	 * 変更を破棄します
	 */
	public void rollback();

	/**
	 * トランザクションを終了します
	 */
	public void end();

}

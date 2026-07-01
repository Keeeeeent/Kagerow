package com.sakulabo.core.Processor.jmx.ExecutionPlan;

import com.sakulabo.core.Processor.jmx.BaseKagerowJMX;

/**
 * 実行計画オブジェクト監視基底インターフェイス
 * 
 * @author keeeeeent
 */
public interface ExecutionPlanMXBean extends BaseKagerowJMX {

	/**
	 * セッションID監視
	 * @return セッションID
	 */
	String getSessionId();

	/**
	 * 履歴保持数を監視
	 * @return 履歴サイズ
	 */
	int getHistorySize();

	/**
	 * キャッシュされた履歴保持数を監視<br/>
	 * このサイズはJVMのガベージコレクションにより変化する可能性があります
	 * @return 履歴サイズ
	 */
	int getHistoryCacheSize();

	/**
	 * 実行可能な残りの空き容量を監視
	 * @return 空き容量
	 */
	int getCanExecutionSize();

}

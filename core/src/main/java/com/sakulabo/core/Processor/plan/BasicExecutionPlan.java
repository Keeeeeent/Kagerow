package com.sakulabo.core.Processor.plan;

import java.io.IOException;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import com.sakulabo.core.Kagerow.Contents.KagerowVirtualFileContent.KagerowVirtualFileObject.BasicFileObject;
import com.sakulabo.core.Kagerow.Exception.AppLogicException;
import com.sakulabo.core.Kagerow.Utilities.KagerowChunkLorder;
import com.sakulabo.core.Processor.jmx.ExecutionPlan.BasicExecutionPlanMXBean;

/**
 * Kagerowスクリプトの通常実行計画を実行するクラスです
 * 
 * @author keeeeeent
 */
public final class BasicExecutionPlan extends ExecutionPlan<BasicFileObject> implements BasicExecutionPlanMXBean {

	/**
	 * デフォルトコンストラクタ
	 * @throws IOException 一時KDB物理ファイル生成失敗
	 * @throws AppLogicException 監視設定失敗
	 */
	public BasicExecutionPlan() throws AppLogicException, IOException {
		super();
	}

	/**
	 * キャッシュロードを行うコンストラクタ
	 * @param cacheId キャッシュID
	 * @throws IOException 一時KDB物理ファイル生成失敗
	 * @throws AppLogicException 監視設定失敗
	 */
	public BasicExecutionPlan(String cacheId) throws AppLogicException, IOException {
		super(cacheId);
	}

	/** {@inheritDoc} */
	@Override
	protected KagerowChunkLorder<BasicFileObject> createChunkLorder() {
		// チャンクローダー生成
		KagerowChunkLorder<BasicFileObject> chunkLorder = KagerowChunkLorder.newBasicChunkLorder(
				super.script.getMode(), super.kdbPath);
		// チャンクローダー返却
		return chunkLorder;
	}

	/** {@inheritDoc} */
	@Override
	protected void setJMX(MBeanServer mBeanServer, ObjectName objectName) {
		;
	}

}

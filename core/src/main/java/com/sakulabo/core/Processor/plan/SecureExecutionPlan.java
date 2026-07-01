package com.sakulabo.core.Processor.plan;

import java.io.IOException;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import com.sakulabo.core.Kagerow.Contents.KagerowVirtualFileContent.KagerowVirtualFileObject;
import com.sakulabo.core.Kagerow.Exception.AppLogicException;
import com.sakulabo.core.Kagerow.Utilities.KagerowChunkLorder;
import com.sakulabo.core.Processor.jmx.ExecutionPlan.SecureExecutionPlanMXBean;

/**
 * Kagerowスクリプトのセキュア実行計画をサポート、実行するクラスです
 * 
 * @author keeeeeent
 */
public final class SecureExecutionPlan extends ExecutionPlan<KagerowVirtualFileObject>
		implements SecureExecutionPlanMXBean {

	/**
	 * デフォルトコンストラクタ
	 * @throws IOException 一時KDB物理ファイル生成失敗
	 * @throws AppLogicException 監視設定失敗
	 */
	public SecureExecutionPlan() throws AppLogicException, IOException {
		super();
	}

	/**
	 * キャッシュロードを行うコンストラクタ
	 * @param cacheId キャッシュID
	 * @throws IOException 一時KDB物理ファイル生成失敗
	 * @throws AppLogicException 監視設定失敗
	 */
	public SecureExecutionPlan(String cacheId) throws AppLogicException, IOException {
		super(cacheId);
	}

	/** {@inheritDoc} */
	@Override
	protected void setJMX(MBeanServer mBeanServer, ObjectName ObjectName) {
		;
	}

	/** {@inheritDoc} */
	@Override
	protected KagerowChunkLorder<KagerowVirtualFileObject> createChunkLorder() {
		return KagerowChunkLorder.newSecureChunkLorder(super.script.getMode(), super.kdbPath);
	}

}

package com.sakulabo.core.Processor.migration;

import org.junit.jupiter.api.extension.ExtendWith;

import com.sakulabo.BaseTest;
import com.sakulabo.BaseTest.KagerowContainerRunner;
import com.sakulabo.core.Common.AppPathUtils;

/**
 * Kagerowアプリケーション専用データ移行テストクラスです
 *
 * @author keeeeeent
 */
@ExtendWith(KagerowContainerRunner.class)
public class BasicDataDumpTest extends BaseTest<BasicDataDump> {

	/** テスト対象 */
	private BasicDataDump testTarget;

	/**
	 * [試験観点] : データバックアップ
	 * [期待される結果] : 正常終了すること
	 */
	// @Test
	public void Test001() throws Throwable {
		testTarget = new BasicDataDump();
		testTarget.exportDump(AppPathUtils.createKagerowHomePath().resolve("test.backup"));
	}

	/**
	 * [試験観点] : データ復元
	 * [期待される結果] : 正常終了すること
	 */
	// @Test
	public void Test002() throws Throwable {
		testTarget = new BasicDataDump();
		testTarget.importDump(AppPathUtils.createKagerowHomePath().resolve("test.backup"));
	}
}

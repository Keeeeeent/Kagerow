package com.sakulabo.core.Processor.migration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import com.sakulabo.BaseTest;
import com.sakulabo.core.Common.AppPathUtils;

/**
 * Kagerowアプリケーション専用データ移行テストクラスです
 *
 * @author keeeeeent
 */
public class BasicDataDumpTest extends BaseTest<BasicDataDump> {

	/** テスト対象 */
	private BasicDataDump testTarget;

	/**
	 * デフォルトコンストラクタ
	 */
	protected BasicDataDumpTest() {
		super(BasicDataDumpTest.class);
	}

	@BeforeEach
	void initService() {
	}

	@AfterEach
	void closeService() throws Exception {
	}

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

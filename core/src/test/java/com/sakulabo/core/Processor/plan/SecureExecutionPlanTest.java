package com.sakulabo.core.Processor.plan;

import java.net.URI;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.sakulabo.BaseTest;
import com.sakulabo.BaseTest.KagerowContainerRunner.KagerowSecureContainerRunner;
import com.sakulabo.BaseTest.KagerowDBRunner;
import com.sakulabo.BaseTest.KagerowDBRunner.KDB;
import com.sakulabo.core.Kagerow.Utilities.KagerowExecutionPlanAccessor;

/**
 * Kagerowセキュアスクリプトファイル実行のテストクラスです
 */
@ExtendWith(KagerowSecureContainerRunner.class)
@ExtendWith(KagerowDBRunner.class)
public class SecureExecutionPlanTest extends BaseTest<SecureExecutionPlan> {

	/** テスト対象 */
	@SuppressWarnings("unused")
	private SecureExecutionPlan testTarget;

	/**
	 * [試験観点] : 通常KSQLファイル
	 * [期待される結果] : 以下である
	 * ・正常終了すること
	 * ・ファイルの内容が正しく読み込みていること
	 * ・セキュアモード
	 * ・暗号化ファイルが含まれない
	 */
	@Test
	public void Test001(@KDB(path = "test1.csv", schema = "test", synonym = "Test002") URI test1) throws Throwable {
		// テストデータ準備
		Path testScript = getTestDir().resolve("test1.ksql");
		// スクリプト実行
		KagerowExecutionPlanAccessor.execute(testScript, null, false);
	}

	/**
	 * [試験観点] : 通常KSQLファイル
	 * [期待される結果] : 以下である
	 * ・正常終了すること
	 * ・ファイルの内容が正しく読み込みていること
	 * ・セキュアモード
	 * ・暗号化ファイルが含まれる
	 */
	@Test
	public void Test002(@KDB(path = "test2.csv", schema = "test0x", synonym = "Test0x9", isSecure = true) URI test2)
			throws Throwable {
		// テストデータ準備
		Path testScript = getTestDir().resolve("test2.ksql");
		// スクリプト実行
		KagerowExecutionPlanAccessor.execute(testScript, null, true);
	}

}

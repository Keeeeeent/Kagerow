package com.sakulabo.core.Processor.plan;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sakulabo.BaseTest;
import com.sakulabo.core.Kagerow.KagerowApplication;
import com.sakulabo.core.Kagerow.Utilities.KagerowChunkCreater.ChunkCreateMode;
import com.sakulabo.core.Kagerow.Utilities.KagerowExecutionPlanAccessor;
import com.sakulabo.core.Kagerow.Utilities.KagerowVirtualFileCreater;

/**
 * Kagerowセキュアスクリプトファイル実行のテストクラスです
 */
public class SecureExecutionPlanTest extends BaseTest<SecureExecutionPlan> {

	/** テスト対象 */
	@SuppressWarnings("unused")
	private SecureExecutionPlan testTarget;

	/**
	 * デフォルトコンストラクタ
	 */
	protected SecureExecutionPlanTest() {
		super(SecureExecutionPlanTest.class);
	}

	@BeforeEach
	void initService() {
	}

	@AfterEach
	void closeService() {
	}

	// @BeforeAll
	public static void beforeAll() throws Throwable {

		// コンテキストをリセット
		VarHandle handle = MethodHandles.privateLookupIn(KagerowApplication.class, MethodHandles.lookup())
				.findStaticVarHandle(KagerowApplication.class, "application", KagerowApplication.class);

		handle.setVolatile(null);

		// セキュアコンテキスト生成
		KagerowApplication.getInstance("test");

	}

	/**
	 * [試験観点] : 通常KSQLファイル
	 * [期待される結果] : 以下である
	 * ・正常終了すること
	 * ・ファイルの内容が正しく読み込みていること
	 * ・セキュアモード
	 * ・暗号化ファイルが含まれない
	 */
	@Test
	public void Test001() throws Throwable {

		// セキュアコンテキスト生成
		KagerowApplication.getInstance("test");

		// テストデータ準備
		Path testScript = testDir.resolve("test1.ksql");

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
	public void Test002() throws Throwable {

		KagerowApplication.getInstance("test");
		try {
			// データインポート
			Path testData = testDir.resolve("test1.csv");
			KagerowVirtualFileCreater.constructionKDB(ChunkCreateMode.CSV, "test0x", testData, StandardCharsets.UTF_8,
					false,
					"Test0x9", true);
		} catch (Exception e) {
			;
		}

		// テストデータ準備
		Path testScript = testDir.resolve("test2.ksql");

		try {
			// スクリプト実行
			KagerowExecutionPlanAccessor.execute(testScript, null, true);
		} catch (Error e) {
			e.printStackTrace();
		}

	}
}

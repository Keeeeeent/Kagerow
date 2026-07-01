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
 * Kagerowスクリプトファイル実行のテストクラスです 
 */
@SuppressWarnings("javadoc")
public class BasicExecutionPlanTest extends BaseTest<BasicExecutionPlan> {

	/** テスト対象 */
	@SuppressWarnings("unused")
	private BasicExecutionPlan testTarget;

	/**
	 * デフォルトコンストラクタ
	 */
	protected BasicExecutionPlanTest() {
		super(BasicExecutionPlanTest.class);
	}

	@BeforeEach
	void initService() throws Throwable {

		// TODO セキュアブートを一度でもするとセキュア実行が強制になるため、テストの際は環境を分けれるよう今後工夫予定

		// コンテキストをリセット
		VarHandle handle = MethodHandles.privateLookupIn(KagerowApplication.class, MethodHandles.lookup())
				.findStaticVarHandle(KagerowApplication.class, "application", KagerowApplication.class);

		handle.setVolatile(null);

		// セキュアコンテキスト生成
		KagerowApplication.getInstance("test");

	}

	@AfterEach
	void closeService() throws Exception {
	}

	/**
	 * [試験観点]      : 通常KSQLファイル,Oracleモード
	 * [期待される結果] : 以下である
	 *                  ・正常終了すること
	 *                  ・ファイルの内容が正しく読み込みていること
	 */
	@Test
	public void Test001() throws Throwable {

		try {
			// データインポート
			Path testData = testDir.resolve("test1.csv");
			KagerowVirtualFileCreater.constructionKDB(ChunkCreateMode.CSV, "test", testData, StandardCharsets.UTF_8, false,
					"Test002", false);
		} catch (Exception e) {
			;
		}

		// テストデータ準備
		Path testScript = testDir.resolve("test1.ksql");

		// スクリプト実行
		KagerowExecutionPlanAccessor.execute(testScript);

	}

	/**
	 * [試験観点]      : 通常KSQLファイル,MySQLモード
	 * [期待される結果] : 以下である
	 *                  ・正常終了すること
	 *                  ・ファイルの内容が正しく読み込みていること
	 */
	@Test
	public void Test002() throws Throwable {

		// テストデータ準備
		Path testScript = testDir.resolve("test2.ksql");

		// スクリプト実行
		KagerowExecutionPlanAccessor.execute(testScript);

	}

	/**
	 * [試験観点]      : 通常KSQLファイル,PostgreSQLモード
	 * [期待される結果] : 以下である
	 *                  ・正常終了すること
	 *                  ・ファイルの内容が正しく読み込みていること
	 */
	@Test
	public void Test003() throws Throwable {

		// テストデータ準備
		Path testScript = testDir.resolve("test3.ksql");

		// スクリプト実行
		KagerowExecutionPlanAccessor.execute(testScript);

	}

	/**
	 * [試験観点]      : 通常KSQLファイル,H2モード
	 * [期待される結果] : 以下である
	 *                  ・正常終了すること
	 *                  ・ファイルの内容が正しく読み込みていること
	 */
	@Test
	public void Test004() throws Throwable {

		// テストデータ準備
		Path testScript = testDir.resolve("test4.ksql");

		// スクリプト実行
		KagerowExecutionPlanAccessor.execute(testScript);

	}

}

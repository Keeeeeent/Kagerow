package com.sakulabo.core.Processor.plan;

import java.net.URI;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.sakulabo.BaseTest;
import com.sakulabo.BaseTest.KagerowContainerRunner;
import com.sakulabo.BaseTest.KagerowDBRunner;
import com.sakulabo.BaseTest.KagerowDBRunner.KDB;
import com.sakulabo.core.Kagerow.Utilities.KagerowExecutionPlanAccessor;

/**
 * Kagerowスクリプトファイル実行のテストクラスです
 */
@ExtendWith(KagerowContainerRunner.class)
@ExtendWith(KagerowDBRunner.class)
public class BasicExecutionPlanTest extends BaseTest<BasicExecutionPlan> {

	/** テスト対象 */
	@SuppressWarnings("unused")
	private BasicExecutionPlan testTarget;

	/**
	 * [試験観点] : 通常KSQLファイル,Oracleモード
	 * [期待される結果] : 以下である
	 * ・正常終了すること
	 * ・ファイルの内容が正しく読み込みていること
	 */
	@Test
	public void Test001(@KDB(path = "test1.csv", schema = "test", synonym = "Test002") URI test1) throws Throwable {
		// テストデータ準備
		Path testScript = getTestDir().resolve("test1.ksql");
		// スクリプト実行
		KagerowExecutionPlanAccessor.execute(testScript);
	}

	/**
	 * [試験観点] : 通常KSQLファイル,MySQLモード
	 * [期待される結果] : 以下である
	 * ・正常終了すること
	 * ・ファイルの内容が正しく読み込みていること
	 */
	@Test
	public void Test002(@KDB(path = "test1.csv", schema = "test", synonym = "Test002") URI test1) throws Throwable {
		// テストデータ準備
		Path testScript = getTestDir().resolve("test2.ksql");
		// スクリプト実行
		KagerowExecutionPlanAccessor.execute(testScript);
	}

	/**
	 * [試験観点] : 通常KSQLファイル,PostgreSQLモード
	 * [期待される結果] : 以下である
	 * ・正常終了すること
	 * ・ファイルの内容が正しく読み込みていること
	 */
	@Test
	public void Test003(@KDB(path = "test1.csv", schema = "test", synonym = "Test002") URI test1) throws Throwable {
		// テストデータ準備
		Path testScript = getTestDir().resolve("test3.ksql");
		// スクリプト実行
		KagerowExecutionPlanAccessor.execute(testScript);
	}

	/**
	 * [試験観点] : 通常KSQLファイル,H2モード
	 * [期待される結果] : 以下である
	 * ・正常終了すること
	 * ・ファイルの内容が正しく読み込みていること
	 */
	@Test
	public void Test004(@KDB(path = "test1.csv", schema = "test", synonym = "Test002") URI test1) throws Throwable {
		// テストデータ準備
		Path testScript = getTestDir().resolve("test4.ksql");
		// スクリプト実行
		KagerowExecutionPlanAccessor.execute(testScript);
	}

}

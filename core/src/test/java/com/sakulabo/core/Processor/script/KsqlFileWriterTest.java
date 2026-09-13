package com.sakulabo.core.Processor.script;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sakulabo.BaseTest;
import com.sakulabo.core.Kagerow.Utilities.KagerowCommandMode;
import com.sakulabo.core.Kagerow.Utilities.KagerowDBMode;
import com.sakulabo.core.Kagerow.Utilities.KagerowScriptAccessor;

/**
 * Kagerowスクリプトファイル解析のテストクラスです
 */
public class KsqlFileWriterTest extends BaseTest<KagerowScriptAccessor> {

	/** テスト対象 */
	private KagerowScriptAccessor testTarget;

	/**
	 * デフォルトコンストラクタ
	 */
	protected KsqlFileWriterTest() {
		super(KsqlFileWriterTest.class);
	}

	@BeforeEach
	void initService() {
	}

	@AfterEach
	void closeService() throws IOException {
		Path resultPath = getOutputPath("result.ksql");
		Files.deleteIfExists(resultPath);
	}

	/**
	 * [試験観点] : 通常KSQLファイル
	 * [期待される結果] : 以下である
	 * ・正常終了すること
	 * ・ファイルの内容が正しく書き出せていること
	 * ・デフォルトプラグイン
	 */
	@Test
	public void Test001() throws Throwable {

		// テストデータ準備
		Path testData = testDir.resolve("test1.ksql");
		testTarget = KagerowScriptAccessor.getInstance(testData);

		// テスト実行
		Path resultPath = getOutputPath("result.ksql");
		try {
			KagerowScriptAccessor.toFile(testTarget, resultPath);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// 検証

		// 出力データ読み込み
		testTarget = KagerowScriptAccessor.getInstance(resultPath);

		// configuration
		assertThat(testTarget.getName(), is("テスト"));
		assertThat(testTarget.getSummary(), is("テスト向けのKSQLです"));
		assertThat(testTarget.getMode(), is(KagerowDBMode.ORACLE));
		assertThat(testTarget.getSchema(), is("test"));
		assertThat(testTarget.getCacheId(), is("test-test-test-test"));

		// environment
		assertThat(testTarget.getEnv().size(), is(6));
		assertThat(testTarget.getEnv().get("key"), is("1"));
		assertThat(testTarget.getEnv().get("key2"), is("2"));
		assertThat(testTarget.getEnv().get("key3"), is("test"));
		assertThat(testTarget.getEnv().get("key4"), is("Oracle"));
		assertThat(testTarget.getEnv().get("key5"), is("テスト"));
		assertThat(testTarget.getEnv().get("key6"), is("FROM"));

		// 要素数
		assertThat(testTarget.getInputPlugins().size(), is(2));
		assertThat(testTarget.getOutputPlugins().size(), is(2));
		assertThat(testTarget.getKsqls().size(), is(2));
		assertThat(testTarget.getCommand().size(), is(1));

		// 共通フラグ
		assertTrue(testTarget.hasCmd());
		assertTrue(testTarget.hasPlugin());

		// plugins(input)
		{
			var plugin = testTarget.getInputPlugins().get(0);
			assertThat(plugin.getId(), is("xxx001"));
			assertThat(plugin.getPackageName(), is("test"));
			assertThat(plugin.getName(), is("test"));
			assertTrue(plugin.hasNext());
			assertThat(plugin.next(), is("xxx002"));
			var param = plugin.getParam();
			assertThat(param.size(), is(3));
			assertThat(param.get("param1"), is("param1"));
			assertThat(param.get("param2"), is("param2"));
			assertThat(param.get("param3"), is("param1"));
		}
		{
			var plugin = testTarget.getInputPlugins().get(1);
			assertThat(plugin.getId(), is("xxx002"));
			assertThat(plugin.getPackageName(), is("default"));
			assertThat(plugin.getName(), is("test"));
			assertFalse(plugin.hasNext());
			assertThat(plugin.next(), is(""));
			var param = plugin.getParam();
			assertThat(param.size(), is(0));
		}

		// plugins(output)
		{
			var plugin = testTarget.getOutputPlugins().get(0);
			assertThat(plugin.getId(), is("xxx001"));
			assertThat(plugin.getPackageName(), is("test"));
			assertThat(plugin.getName(), is("test"));
			assertTrue(plugin.hasNext());
			assertThat(plugin.next(), is("xxx002"));
			var param = plugin.getParam();
			assertThat(param.size(), is(3));
			assertThat(param.get("param1"), is("param1"));
			assertThat(param.get("param2"), is("param2"));
			assertThat(param.get("param3"), is("param1"));
		}
		{
			var plugin = testTarget.getOutputPlugins().get(1);
			assertThat(plugin.getId(), is("xxx002"));
			assertThat(plugin.getPackageName(), is("default"));
			assertThat(plugin.getName(), is("test"));
			assertFalse(plugin.hasNext());
			assertThat(plugin.next(), is(""));
			var param = plugin.getParam();
			assertThat(param.size(), is(0));
		}

		// ksql
		{
			var ksql = testTarget.getKsqls().get(0);
			assertThat(ksql.getId(), is("zzz001"));
			assertThat(ksql.getName(), is("sqlno1"));
			assertTrue(ksql.hasNext());
			assertThat(ksql.next(), is("zzz002"));
			var variable = ksql.variable();
			assertThat(variable.size(), is(3));
			assertThat(variable.get("PARAM_01"), is("DUAL1"));
			assertThat(variable.get("PARAM_02"), is("DUAL2"));
			assertThat(variable.get("PARAM_03"), is("DUAL3"));
			var sql = ksql.getSql();
			var expsql = """
					SELECT * FROM @{PARAM_01}
										WHERE X = @{PARAM_02}
									ORDER BY 1 , @{PARAM_03};""";
			assertThat(sql, is(expsql));
		}
		{
			var ksql = testTarget.getKsqls().get(1);
			assertThat(ksql.getId(), is("zzz002"));
			assertThat(ksql.getName(), is("sqlno2"));
			assertFalse(ksql.hasNext());
			assertThat(ksql.next(), is(""));
			var variable = ksql.variable();
			assertThat(variable.size(), is(0));
			var sql = ksql.getSql();
			var expsql = "SELECT * FROM DUAL;";
			assertThat(sql, is(expsql));
		}

		// cmd
		{
			var cmd = testTarget.getCommand().get(0);
			var environmental = cmd.environmental();
			assertThat(environmental.size(), is(2));
			assertThat(environmental.get("PARAM_01"), is("test1"));
			assertThat(environmental.get("PARAM_02"), is("test2"));
			assertThat(cmd.getMode(), is(KagerowCommandMode.Shell));
			var expcmd = "sh test.sh ${PARAM_01} ${PARAM_02}";
			assertThat(cmd.getCmd(), is(expcmd));
		}

	}

	/**
	 * [試験観点] : 通常KSQLファイル
	 * [期待される結果] : 以下である
	 * ・正常終了すること
	 * ・ファイルの内容が正しく書き出せていること
	 * ・プラグインタグなし
	 * ・コマンドタグなし
	 */
	@Test
	public void Test002() throws Throwable {

		// テストデータ準備
		Path testData = testDir.resolve("test2.ksql");
		testTarget = KagerowScriptAccessor.getInstance(testData);

		// テスト実行
		Path resultPath = getOutputPath("result.ksql");
		KagerowScriptAccessor.toFile(testTarget, resultPath);

		// 検証

		// 出力データ読み込み
		testTarget = KagerowScriptAccessor.getInstance(resultPath);

		// configuration
		assertThat(testTarget.getName(), is("テスト"));
		assertThat(testTarget.getSummary(), is("テスト向けのKSQLです"));
		assertThat(testTarget.getMode(), is(KagerowDBMode.ORACLE));
		assertThat(testTarget.getSchema(), is("test"));

		// 要素数
		assertThat(testTarget.getInputPlugins().size(), is(0));
		assertThat(testTarget.getOutputPlugins().size(), is(0));
		assertThat(testTarget.getKsqls().size(), is(2));
		assertThat(testTarget.getCommand().size(), is(0));

		// 共通フラグ
		assertFalse(testTarget.hasCmd());
		assertFalse(testTarget.hasPlugin());

		// ksql
		{
			var ksql = testTarget.getKsqls().get(0);
			assertThat(ksql.getId(), is("zzz001"));
			assertThat(ksql.getName(), is("sqlno1"));
			assertTrue(ksql.hasNext());
			assertThat(ksql.next(), is("zzz002"));
			var variable = ksql.variable();
			assertThat(variable.size(), is(3));
			assertThat(variable.get("PARAM_01"), is("DUAL1"));
			assertThat(variable.get("PARAM_02"), is("DUAL2"));
			assertThat(variable.get("PARAM_03"), is("DUAL3"));
			var sql = ksql.getSql();
			var expsql = """
					SELECT * FROM @{PARAM_01}
										WHERE X = @{PARAM_02}
									ORDER BY 1 , @{PARAM_03};""";
			assertThat(sql, is(expsql));
		}
		{
			var ksql = testTarget.getKsqls().get(1);
			assertThat(ksql.getId(), is("zzz002"));
			assertThat(ksql.getName(), is("sqlno2"));
			assertFalse(ksql.hasNext());
			assertThat(ksql.next(), is(""));
			var variable = ksql.variable();
			assertThat(variable.size(), is(0));
			var sql = ksql.getSql();
			var expsql = "SELECT * FROM DUAL;";
			assertThat(sql, is(expsql));
		}

	}

	/**
	 * [試験観点] : 通常KSQLファイル
	 * [期待される結果] : 以下である
	 * ・正常終了すること
	 * ・ファイルの内容が正しく書き出せていること
	 * ・プラグインタグ(Inputのみ)
	 * ・コマンドタグなし
	 */
	@Test
	public void Test003() throws Throwable {

		// テストデータ準備
		Path testData = testDir.resolve("test3.ksql");
		testTarget = KagerowScriptAccessor.getInstance(testData);

		// テスト実行
		Path resultPath = getOutputPath("result.ksql");
		KagerowScriptAccessor.toFile(testTarget, resultPath);

		// 検証

		// 出力データ読み込み
		testTarget = KagerowScriptAccessor.getInstance(resultPath);

		// configuration
		assertThat(testTarget.getName(), is("テスト"));
		assertThat(testTarget.getSummary(), is("テスト向けのKSQLです"));
		assertThat(testTarget.getMode(), is(KagerowDBMode.ORACLE));
		assertThat(testTarget.getSchema(), is("test"));

		// 要素数
		assertThat(testTarget.getInputPlugins().size(), is(2));
		assertThat(testTarget.getOutputPlugins().size(), is(0));
		assertThat(testTarget.getKsqls().size(), is(2));
		assertThat(testTarget.getCommand().size(), is(0));

		// 共通フラグ
		assertFalse(testTarget.hasCmd());
		assertTrue(testTarget.hasPlugin());

		// plugins(input)
		{
			var plugin = testTarget.getInputPlugins().get(0);
			assertThat(plugin.getId(), is("xxx001"));
			assertThat(plugin.getPackageName(), is("default"));
			assertThat(plugin.getName(), is("test"));
			assertTrue(plugin.hasNext());
			assertThat(plugin.next(), is("xxx002"));
			var param = plugin.getParam();
			assertThat(param.size(), is(3));
			assertThat(param.get("param1"), is("param1"));
			assertThat(param.get("param2"), is("param2"));
			assertThat(param.get("param3"), is("param1"));
		}
		{
			var plugin = testTarget.getInputPlugins().get(1);
			assertThat(plugin.getId(), is("xxx002"));
			assertThat(plugin.getPackageName(), is("test"));
			assertThat(plugin.getName(), is("test"));
			assertFalse(plugin.hasNext());
			assertThat(plugin.next(), is(""));
			var param = plugin.getParam();
			assertThat(param.size(), is(0));
		}

		// ksql
		{
			var ksql = testTarget.getKsqls().get(0);
			assertThat(ksql.getId(), is("zzz001"));
			assertThat(ksql.getName(), is("sqlno1"));
			assertTrue(ksql.hasNext());
			assertThat(ksql.next(), is("zzz002"));
			var variable = ksql.variable();
			assertThat(variable.size(), is(3));
			assertThat(variable.get("PARAM_01"), is("DUAL1"));
			assertThat(variable.get("PARAM_02"), is("DUAL2"));
			assertThat(variable.get("PARAM_03"), is("DUAL3"));
			var sql = ksql.getSql();
			var expsql = """
					SELECT * FROM @{PARAM_01}
										WHERE X = @{PARAM_02}
									ORDER BY 1 , @{PARAM_03};""";
			assertThat(sql, is(expsql));
		}
		{
			var ksql = testTarget.getKsqls().get(1);
			assertThat(ksql.getId(), is("zzz002"));
			assertThat(ksql.getName(), is("sqlno2"));
			assertFalse(ksql.hasNext());
			assertThat(ksql.next(), is(""));
			var variable = ksql.variable();
			assertThat(variable.size(), is(0));
			var sql = ksql.getSql();
			var expsql = "SELECT * FROM DUAL;";
			assertThat(sql, is(expsql));
		}
	}

	/**
	 * [試験観点] : 通常KSQLファイル
	 * [期待される結果] : 以下である
	 * ・正常終了すること
	 * ・ファイルの内容が正しく書き出せていること
	 * ・プラグインタグ(outputのみ)
	 * ・コマンドタグなし
	 */
	@Test
	public void Test004() throws Throwable {

		// テストデータ準備
		Path testData = testDir.resolve("test4.ksql");
		testTarget = KagerowScriptAccessor.getInstance(testData);

		// テスト実行
		Path resultPath = getOutputPath("result.ksql");
		KagerowScriptAccessor.toFile(testTarget, resultPath);

		// 検証

		// 出力データ読み込み
		testTarget = KagerowScriptAccessor.getInstance(resultPath);

		// configuration
		assertThat(testTarget.getName(), is("テスト"));
		assertThat(testTarget.getSummary(), is("テスト向けのKSQLです"));
		assertThat(testTarget.getMode(), is(KagerowDBMode.ORACLE));
		assertThat(testTarget.getSchema(), is("test"));

		// 要素数
		assertThat(testTarget.getInputPlugins().size(), is(0));
		assertThat(testTarget.getOutputPlugins().size(), is(2));
		assertThat(testTarget.getKsqls().size(), is(2));
		assertThat(testTarget.getCommand().size(), is(0));

		// 共通フラグ
		assertFalse(testTarget.hasCmd());
		assertTrue(testTarget.hasPlugin());

		// plugins(output)
		{
			var plugin = testTarget.getOutputPlugins().get(0);
			assertThat(plugin.getId(), is("xxx001"));
			assertThat(plugin.getPackageName(), is("default"));
			assertThat(plugin.getName(), is("test"));
			assertTrue(plugin.hasNext());
			assertThat(plugin.next(), is("xxx002"));
			var param = plugin.getParam();
			assertThat(param.size(), is(3));
			assertThat(param.get("param1"), is("param1"));
			assertThat(param.get("param2"), is("param2"));
			assertThat(param.get("param3"), is("param1"));
		}
		{
			var plugin = testTarget.getOutputPlugins().get(1);
			assertThat(plugin.getId(), is("xxx002"));
			assertThat(plugin.getPackageName(), is("test"));
			assertThat(plugin.getName(), is("test"));
			assertFalse(plugin.hasNext());
			assertThat(plugin.next(), is(""));
			var param = plugin.getParam();
			assertThat(param.size(), is(0));
		}

		// ksql
		{
			var ksql = testTarget.getKsqls().get(0);
			assertThat(ksql.getId(), is("zzz001"));
			assertThat(ksql.getName(), is("sqlno1"));
			assertTrue(ksql.hasNext());
			assertThat(ksql.next(), is("zzz002"));
			var variable = ksql.variable();
			assertThat(variable.size(), is(3));
			assertThat(variable.get("PARAM_01"), is("DUAL1"));
			assertThat(variable.get("PARAM_02"), is("DUAL2"));
			assertThat(variable.get("PARAM_03"), is("DUAL3"));
			var sql = ksql.getSql();
			var expsql = """
					SELECT * FROM @{PARAM_01}
										WHERE X = @{PARAM_02}
									ORDER BY 1 , @{PARAM_03};""";
			assertThat(sql, is(expsql));
		}
		{
			var ksql = testTarget.getKsqls().get(1);
			assertThat(ksql.getId(), is("zzz002"));
			assertThat(ksql.getName(), is("sqlno2"));
			assertFalse(ksql.hasNext());
			assertThat(ksql.next(), is(""));
			var variable = ksql.variable();
			assertThat(variable.size(), is(0));
			var sql = ksql.getSql();
			var expsql = "SELECT * FROM DUAL;";
			assertThat(sql, is(expsql));
		}

	}

	/**
	 * [試験観点] : 通常KSQLファイル
	 * [期待される結果] : 以下である
	 * ・正常終了すること
	 * ・ファイルの内容が正しく書き出せていること
	 * ・コマンドタグあり
	 * ・環境変数指定なし
	 */
	@Test
	public void Test005() throws Throwable {

		// テストデータ準備
		Path testData = testDir.resolve("test5.ksql");
		testTarget = KagerowScriptAccessor.getInstance(testData);

		// テスト実行
		Path resultPath = getOutputPath("result.ksql");
		KagerowScriptAccessor.toFile(testTarget, resultPath);

		// 検証

		// 出力データ読み込み
		testTarget = KagerowScriptAccessor.getInstance(resultPath);

		// 検証
		{
			var cmd = testTarget.getCommand().get(0);
			var environmental = cmd.environmental();
			assertThat(environmental.size(), is(0));
			var expcmd = "sh test.sh ${PARAM_01} ${PARAM_02}";
			assertThat(cmd.getCmd(), is(expcmd));
		}

	}

}

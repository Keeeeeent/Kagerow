package com.sakulabo.core.Processor.script;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sakulabo.BaseTest;

/**
 * KSQLに存在する置換文字列を処理するスクリプトのテストクラスです
 */
public class KsqlReplaceWordParserTest extends BaseTest<KsqlReplaceWordParser> {

	/** テスト対象 */
	private KsqlReplaceWordParser testTarget;

	/**
	 * デフォルトコンストラクタ
	 */
	protected KsqlReplaceWordParserTest() {
		super(KsqlReplaceWordParserTest.class);
	}

	@BeforeEach
	void initService() {
	}

	@AfterEach
	void closeService() {
	}

	/**
	 * [試験観点] : 置換対象なし
	 * [期待される結果] : 以下である
	 * ・正常終了すること
	 * ・文字列に変化がないこと
	 */
	@Test
	public void Test001() throws Throwable {

		// テストデータ準備
		Map<String, String> replaceWordDictionary = Collections.emptyMap();
		testTarget = new KsqlReplaceWordParser(replaceWordDictionary);
		String sql = """
				SELECT * FROM DUAL;
				""";

		// テスト実施
		String result = testTarget.transform(sql);

		// 結果検証
		assertThat(result, is(sql));

	}

	/**
	 * [試験観点] : 置換対象あり
	 * [期待される結果] : 以下である
	 * ・正常終了すること
	 * ・対象文字列が置換されていること(単一ワード)
	 */
	@Test
	public void Test002() throws Throwable {

		// テストデータ準備
		Map<String, String> replaceWordDictionary = new HashMap<>() {
			{
				put("target", "DUAL");
			}
		};
		testTarget = new KsqlReplaceWordParser(replaceWordDictionary);
		String sql = """
				SELECT * FROM @{target};
				""";

		// 期待値
		String exp = """
				SELECT * FROM DUAL;
				""";

		// テスト実施
		String result = testTarget.transform(sql);

		// 結果検証
		assertThat(result, is(exp));

	}

	/**
	 * [試験観点] : 置換対象あり
	 * [期待される結果] : 以下である
	 * ・正常終了すること
	 * ・対象文字列が置換されていること(複数単一ワード)
	 */
	@Test
	public void Test003() throws Throwable {

		// テストデータ準備
		Map<String, String> replaceWordDictionary = new HashMap<>() {
			{
				put("target", "DUAL");
			}
		};
		testTarget = new KsqlReplaceWordParser(replaceWordDictionary);
		String sql = """
				SELECT * FROM @{target} INNER JOIN @{target} USIND(X);
				""";

		// 期待値
		String exp = """
				SELECT * FROM DUAL INNER JOIN DUAL USIND(X);
				""";

		// テスト実施
		String result = testTarget.transform(sql);

		// 結果検証
		assertThat(result, is(exp));

	}

	/**
	 * [試験観点] : 置換対象あり
	 * [期待される結果] : 以下である
	 * ・正常終了すること
	 * ・対象文字列が置換されていること(複数ワード)
	 */
	@Test
	public void Test004() throws Throwable {

		// テストデータ準備
		Map<String, String> replaceWordDictionary = new HashMap<>() {
			{
				put("target1", "DUAL1");
				put("target2", "DUAL2");
			}
		};
		testTarget = new KsqlReplaceWordParser(replaceWordDictionary);
		String sql = """
				SELECT * FROM @{target1} INNER JOIN @{target2} USIND(X);
				""";

		// 期待値
		String exp = """
				SELECT * FROM DUAL1 INNER JOIN DUAL2 USIND(X);
				""";

		// テスト実施
		String result = testTarget.transform(sql);

		// 結果検証
		assertThat(result, is(exp));

	}

}

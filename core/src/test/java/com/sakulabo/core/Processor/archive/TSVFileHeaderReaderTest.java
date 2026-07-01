package com.sakulabo.core.Processor.archive;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sakulabo.BaseTest;
import com.sakulabo.core.Kagerow.KagerowApplication;
import com.sakulabo.core.Kagerow.Utilities.KagerowFileHeaderReader;
import com.sakulabo.core.Kagerow.Utilities.KagerowUtilities;

/**
 * TSVファイルのヘッダー読み取り実装提供クラスのテストクラスです 
 */
@SuppressWarnings("javadoc")
public class TSVFileHeaderReaderTest extends BaseTest<TSVFileHeaderReader> {

	/** テスト対象 */
	private TSVFileHeaderReader testTarget;

	/**
	 * デフォルトコンストラクタ
	 */
	protected TSVFileHeaderReaderTest() {
		super(TSVFileHeaderReaderTest.class);
	}

	@BeforeEach
	void initService() {
		// セキュアコンテキスト生成
		KagerowApplication.getInstance("test");
	}

	@AfterEach
	void closeService() throws Exception {
	}

	/**
	 * [試験観点]      : ヘッダー生成、ヘッダーなしファイル
	 * [期待される結果] : 正常終了すること、デフォルトヘッダーが生成されること
	 */
	@Test
	public void Test001() throws Throwable {
		// インスタンス初期化
		testTarget = new TSVFileHeaderReader(testDir.resolve("testdata1.csv"), StandardCharsets.UTF_8, false);
		String[] result = testTarget.readLine();
		String[] exp = {
				"COLUMN_1", "COLUMN_2", "COLUMN_3", "COLUMN_4", "COLUMN_5", "COLUMN_6", "COLUMN_7"
		};
		// 検証
		for (int i = 0; i < exp.length; i++) {
			assertThat(result[i], is(exp[i]));
		}

	}

	/**
	 * [試験観点]      : ヘッダー生成、ヘッダーありファイル、ダブルクオーテーションあり
	 * [期待される結果] : ダブルクオーテーションが含まれていないこと
	 */
	@Test
	public void Test002() throws Throwable {
		// インスタンス初期化
		testTarget = new TSVFileHeaderReader(testDir.resolve("testdata2.csv"), StandardCharsets.UTF_8, true);
		String[] result = testTarget.readLine();
		String[] exp = {
				"COL_1", "COL_2", "COL_3", "COL_4", "COL_5", "COL_6", "COL_7"
		};
		// 検証
		for (int i = 0; i < exp.length; i++) {
			assertThat(result[i], is(exp[i]));
		}

	}

	/**
	 * [試験観点]      : ヘッダー生成、ヘッダーありファイル、ダブルクオーテーションあり、エスケープあり
	 * [期待される結果] : カラム名が正しく生成されること、エスケープ処理がされていること
	 */
	@Test
	public void Test003() throws Throwable {
		// インスタンス初期化
		testTarget = new TSVFileHeaderReader(testDir.resolve("testdata3.csv"), StandardCharsets.UTF_8, true);
		String[] result = testTarget.readLine();
		String[] exp = {
				"COL\"_1", "COL_2", "COL_3", "COL\"_4", "COL_5", "COL_6", "COL\"_7"
		};
		// 検証
		for (int i = 0; i < exp.length; i++) {
			assertThat(result[i], is(exp[i]));
		}

	}

	/**
	 * [試験観点]      : ヘッダー生成、ヘッダーありファイル、ダブルクオーテーションあり、エスケープあり、改行あり
	 * [期待される結果] : カラムが正しく生成されていること、改行含めたヘッダーになっていること
	 */
	@Test
	public void Test004() throws Throwable {
		// インスタンス初期化
		testTarget = new TSVFileHeaderReader(testDir.resolve("testdata4.csv"), StandardCharsets.UTF_8, true);
		String[] result = testTarget.readLine();
		String[] exp = {
				"COL_1", "COL_2", "COL_3", "COL_\n4", "COL_5", "COL_6", "COL_7"
		};
		// 検証
		for (int i = 0; i < exp.length; i++) {
			assertThat(result[i], is(exp[i]));
		}

	}

	/**
	 * [試験観点]      : 複数回呼び出し
	 * [期待される結果] : 結果に変化がないこと
	 */
	@Test
	public void Test005() throws Throwable {
		// インスタンス初期化
		testTarget = new TSVFileHeaderReader(testDir.resolve("testdata4.csv"), StandardCharsets.UTF_8, true);
		String[] result = testTarget.readLine();
		String[] exp = {
				"COL_1", "COL_2", "COL_3", "COL_\n4", "COL_5", "COL_6", "COL_7"
		};
		// 検証
		for (int i = 0; i < exp.length; i++) {
			assertThat(result[i], is(exp[i]));
		}
		result = testTarget.readLine();
		// 検証
		for (int i = 0; i < exp.length; i++) {
			assertThat(result[i], is(exp[i]));
		}

	}

	/**
	 * [試験観点]      : ヘッダー生成、ヘッダーありファイル、ダブルクオーテーションありを含む
	 * [期待される結果] : タブが含まれた状態でヘッダーが生成されること
	 */
	@Test
	public void Test006() throws Throwable {
		// インスタンス初期化
		testTarget = new TSVFileHeaderReader(testDir.resolve("testdata5.csv"), StandardCharsets.UTF_8, true);
		String[] result = testTarget.readLine();
		String[] exp = {
				"COL_	1", "COL_2", "COL_3", "COL_4", "COL_5", "COL_6", "COL_7"
		};
		// 検証
		for (int i = 0; i < exp.length; i++) {
			assertThat(result[i], is(exp[i]));
		}

	}

	/**
	 * [試験観点]      : ヘッダー生成、ヘッダーありファイル、ダブルクオーテーションありを含む、予約語を含む
	 * [期待される結果] : 予約語が変換されていること
	 */
	@Test
	public void Test007() throws Throwable {
		// インスタンス初期化
		testTarget = new TSVFileHeaderReader(testDir.resolve("testdata7.csv"), StandardCharsets.UTF_8, true);
		String[] result = testTarget.readLine();
		String[] exp = {
				"K_IN", "COL_2", "COL_3", "COL_4", "COL_5", "COL_6", "COL_7"
		};
		// 検証
		for (int i = 0; i < exp.length; i++) {
			assertThat(result[i], is(exp[i]));
		}

	}

	/**
	 * [試験観点]      : 予約語を含む、プレフィックス指定あり
	 * [期待される結果] : 指定したプレフィックスで予約語が変換されていること
	 */
	@Test
	public void Test008() throws Throwable {
		// プレフィックス指定
		KagerowUtilities.setSetting(KagerowFileHeaderReader.class.getName(), "DEFAULT_COLUMN_PREFIX", "TEST_");
		try {

			// 前提の確認
			String setting = KagerowUtilities.getSetting(KagerowFileHeaderReader.class.getName(),
					"DEFAULT_COLUMN_PREFIX");
			if (!"TEST_".equals(setting)) {
				fail("テストの前提条件を満たしていません");
			}

			// インスタンス初期化
			testTarget = new TSVFileHeaderReader(testDir.resolve("testdata7.csv"), StandardCharsets.UTF_8, true);
			String[] result = testTarget.readLine();
			String[] exp = {
					"TEST_IN", "COL_2", "COL_3", "COL_4", "COL_5", "COL_6", "COL_7"
			};
			// 検証
			for (int i = 0; i < exp.length; i++) {
				assertThat(result[i], is(exp[i]));
			}
		} finally {
			// プレフィックスリカバリ
			KagerowUtilities.setSetting(KagerowFileHeaderReader.class.getName(), "DEFAULT_COLUMN_PREFIX", "K_");
		}
	}

}

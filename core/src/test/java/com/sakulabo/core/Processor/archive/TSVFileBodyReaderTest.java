package com.sakulabo.core.Processor.archive;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sakulabo.BaseTest;

/**
 * TSVファイルのボディー読み取り実装提供クラスのテストクラスです 
 */
@SuppressWarnings("javadoc")
public class TSVFileBodyReaderTest extends BaseTest<TSVFileBodyReader> {

	/** テスト対象 */
	private TSVFileBodyReader testTarget;

	/**
	 * デフォルトコンストラクタ
	 */
	protected TSVFileBodyReaderTest() {
		super(TSVFileHeaderReaderTest.class);
	}

	@BeforeEach
	void initService() {
	}

	@AfterEach
	void closeService() throws Exception {
	}

	/**
	 * [試験観点]      : データ読み取り、ヘッダーなしファイル、最終行改行なし
	 * [期待される結果] : 正常終了すること、最終行まで読み込めてること
	 */
	@Test
	public void Test001() throws Throwable {
		// インスタンス初期化
		Path path = testDir.resolve("testdata1.csv");
		testTarget = new TSVFileBodyReader(path, StandardCharsets.UTF_8, false);
		@SuppressWarnings("unused")
		String[] result;
		int count = 0;
		// 検証
		while ((result = testTarget.readLine()) != null) {
			count++;
		}
		assertThat(count, is(7));
	}

	/**
	 * [試験観点]      : ヘッダーありファイル、最終行改行あり、ダブルクオーテーションあり
	 * [期待される結果] : ヘッダーが含まれていないこと、最終行まで読み込めてること、ダブルクオーテーションが含まれていないこと
	 */
	@Test
	public void Test002() throws Throwable {
		// インスタンス初期化
		Path path = testDir.resolve("testdata2.csv");
		testTarget = new TSVFileBodyReader(path, StandardCharsets.UTF_8, true);
		String[] result;
		String[][] exp = {
				new String[] { "tr-20240101-1", "オンサイト", "09:30", "19:30", "01:00", "00:00", "none" },
				new String[] { "tr-20240102-1", "オンサイト", "09:30", "19:30", "01:00", "00:00", "none" },
				new String[] { "tr-20240103-1", "オンサイト", "09:30", "19:30", "01:00", "00:00", "none" },
				new String[] { "tr-20240104-1", "オンサイト", "09:30", "19:30", "01:00", "00:00", "none" },
				new String[] { "tr-20240105-1", "オンサイト", "09:30", "19:30", "01:00", "00:00", "none" },
				new String[] { "tr-20240106-1", "オンサイト", "09:30", "19:30", "01:00", "00:00", "none" },
				new String[] { "tr-20240107-1", "オンサイト", "09:30", "19:30", "01:00", "00:00", "none" }
		};
		int count = 0;
		// 検証
		while ((result = testTarget.readLine()) != null) {
			for (int i = 0; i < exp[count].length; i++) {
				assertThat(result[i], is(exp[count][i]));
			}
			count++;
		}

	}

	/**
	 * [試験観点]      : ヘッダーありファイル、ダブルクオーテーションあり、エスケープあり
	 * [期待される結果] : カラム名が正しく生成されること、エスケープ処理がされていること
	 */
	@Test
	public void Test003() throws Throwable {
		// インスタンス初期化
		Path path = testDir.resolve("testdata3.csv");
		testTarget = new TSVFileBodyReader(path, StandardCharsets.UTF_8, true);
		String[] result = testTarget.readLine();
		String[] exp = {
				"tr-\"20240101-1", "\"オンサイト", "09:30\"", "19:30", "01:00", "00:00", "none\""
		};
		// 検証
		for (int i = 0; i < exp.length; i++) {
			assertThat(result[i], is(exp[i]));
		}

	}

	/**
	 * [試験観点]      : ダブルクオーテーションあり、エスケープあり、改行あり
	 * [期待される結果] : カラムが正しく生成されていること、改行含めたヘッダーになっていること
	 */
	@Test
	public void Test004() throws Throwable {
		// インスタンス初期化
		Path path = testDir.resolve("testdata4.csv");
		testTarget = new TSVFileBodyReader(path, StandardCharsets.UTF_8, true);
		String[] result = testTarget.readLine();
		String[] exp = {
				"tr-20240101-1", "オン\nサイト", "09:30", "19:30", "01:00", "00:00", "none"
		};
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
	public void Test005() throws Throwable {
		// インスタンス初期化
		Path path = testDir.resolve("testdata5.csv");
		testTarget = new TSVFileBodyReader(path, StandardCharsets.UTF_8, true);
		String[] result = testTarget.readLine();
		String[] exp = {
				"tr-20240101-1", "オンサイト", "09:30", "19:30", "01:00", "00:00", "no	ne"
		};
		// 検証
		for (int i = 0; i < exp.length; i++) {
			assertThat(result[i], is(exp[i]));
		}

	}

	/**
	 * [試験観点]      : ヘッダーありファイル、最終行改行あり、ダブルクオーテーションあり、最終フィールドが空文字
	 * [期待される結果] : ヘッダーが含まれていないこと、最終行まで読み込めてること、ダブルクオーテーションが含まれていないこと
	 */
	@Test
	public void Test006() throws Throwable {
		// インスタンス初期化
		Path path = testDir.resolve("testdata6.csv");
		testTarget = new TSVFileBodyReader(path, StandardCharsets.UTF_8, true);
		String[] result;
		String[][] exp = {
				new String[] { "tr-20240101-1", "オンサイト", "09:30", "19:30", "01:00", "00:00", "none", "" },
				new String[] { "tr-20240102-1", "オンサイト", "09:30", "19:30", "01:00", "00:00", "none", "" },
				new String[] { "tr-20240103-1", "オンサイト", "09:30", "19:30", "01:00", "00:00", "none", "" },
				new String[] { "tr-20240104-1", "オンサイト", "09:30", "19:30", "01:00", "00:00", "none", "" },
				new String[] { "tr-20240105-1", "オンサイト", "09:30", "19:30", "01:00", "00:00", "none", "" },
				new String[] { "tr-20240106-1", "オンサイト", "09:30", "19:30", "01:00", "00:00", "none", "" },
				new String[] { "tr-20240107-1", "オンサイト", "09:30", "19:30", "01:00", "00:00", "none", "" }
		};
		int count = 0;
		// 検証
		while ((result = testTarget.readLine()) != null) {
			for (int i = 0; i < exp[count].length; i++) {
				assertThat(result[i], is(exp[count][i]));
			}
			count++;
		}

	}

}

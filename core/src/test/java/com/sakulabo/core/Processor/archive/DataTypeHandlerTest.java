package com.sakulabo.core.Processor.archive;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sakulabo.BaseTest;
import com.sakulabo.core.Common.ErrorMessage;
import com.sakulabo.core.Kagerow.Contents.KagerowVirtualFileContent.KagerowDataType;

/**
 * データ配列からJavaで対応可能なデータフォーマットを推測するクラスのテストクラスです
 * 
 * @author keeeeeent
 */
public class DataTypeHandlerTest extends BaseTest<DataTypeHandlerTest> {

	/**
	 * デフォルトコンストラクタ
	 */
	protected DataTypeHandlerTest() {
		super(DataTypeHandlerTest.class);
	}

	@BeforeEach
	void initService() {
	}

	@AfterEach
	void closeService() throws Exception {
	}

	/** テスト対象 */
	private DataTypeHandler testTarget;

	/**
	 * [試験観点] : データ初期化
	 * [期待される結果] : 正常終了すること、タイプが全てNULLであること、要素が5つであること
	 * 
	 * @throws Throwable
	 */
	@Test
	public void Test001() throws Throwable {
		// テストクラス初期化
		String[] data = new String[5];
		testTarget = new DataTypeHandler(data);
		// 検証
		KagerowDataType[] result = testTarget.getDataType();
		assertThat(result.length, is(5));
		for (KagerowDataType type : result) {
			assertThat(type, is(KagerowDataType.NULL));
		}
	}

	/**
	 * [試験観点] : データ初期化、真偽値文字列
	 * [期待される結果] : 正常終了すること、タイプがBOOLEANであること、要素が5つであること
	 * 
	 * @throws Throwable
	 */
	@Test
	public void Test002() throws Throwable {
		// テストクラス初期化
		String[] data = new String[5];
		data[0] = "false";
		data[1] = "true";
		data[2] = "0";
		data[3] = "1";

		testTarget = new DataTypeHandler(data);
		// 検証
		KagerowDataType[] result = testTarget.getDataType();
		assertThat(result[0], is(KagerowDataType.BOOLEAN));
		assertThat(result[1], is(KagerowDataType.BOOLEAN));
		assertThat(result[2], is(KagerowDataType.BOOLEAN));
		assertThat(result[3], is(KagerowDataType.BOOLEAN));
		assertThat(result[4], is(KagerowDataType.NULL));
	}

	/**
	 * [試験観点] : データ初期化、数値文字列
	 * [期待される結果] : 正常終了すること、タイプがNUMBERであること、要素が5つであること
	 * 
	 * @throws Throwable
	 */
	@Test
	public void Test003() throws Throwable {
		// テストクラス初期化
		String[] data = new String[5];
		data[0] = "1200";
		data[1] = "-12";
		data[2] = "-0";
		data[3] = "-1";
		data[4] = "TRUE";

		testTarget = new DataTypeHandler(data);
		// 検証
		KagerowDataType[] result = testTarget.getDataType();
		assertThat(result[0], is(KagerowDataType.NUMBER));
		assertThat(result[1], is(KagerowDataType.NUMBER));
		assertThat(result[2], is(KagerowDataType.NUMBER));
		assertThat(result[3], is(KagerowDataType.NUMBER));
		assertThat(result[4], is(KagerowDataType.BOOLEAN));
	}

	/**
	 * [試験観点] : データ初期化、数値文字列（先頭0を含む）
	 * [期待される結果] : 正常終了すること、タイプがNUMBERであること、先頭0の文字列はタイプがVARCHARであること
	 * 
	 * @throws Throwable
	 */
	@Test
	public void Test004() throws Throwable {
		// テストクラス初期化
		String[] data = new String[5];
		data[0] = "1200";
		data[1] = "-1200";
		data[2] = "01200";
		data[3] = "-01200";
		data[4] = "FALSE";

		testTarget = new DataTypeHandler(data);
		// 検証
		KagerowDataType[] result = testTarget.getDataType();
		assertThat(result[0], is(KagerowDataType.NUMBER));
		assertThat(result[1], is(KagerowDataType.NUMBER));
		assertThat(result[2], is(KagerowDataType.VARCHAR));
		assertThat(result[3], is(KagerowDataType.VARCHAR));
		assertThat(result[4], is(KagerowDataType.BOOLEAN));
	}

	/**
	 * [試験観点] : データ初期化、少数文字列
	 * [期待される結果] : 正常終了すること、タイプがDECIMALであること
	 * 
	 * @throws Throwable
	 */
	@Test
	public void Test005() throws Throwable {
		// テストクラス初期化
		String[] data = new String[5];
		data[0] = "1200.0";
		data[1] = "-1.2";
		data[2] = "-0.01";
		data[3] = "-1e8";
		data[4] = "True";

		testTarget = new DataTypeHandler(data);
		// 検証
		KagerowDataType[] result = testTarget.getDataType();
		assertThat(result[0], is(KagerowDataType.DECIMAL));
		assertThat(result[1], is(KagerowDataType.DECIMAL));
		assertThat(result[2], is(KagerowDataType.DECIMAL));
		assertThat(result[3], is(KagerowDataType.DECIMAL));
		assertThat(result[4], is(KagerowDataType.BOOLEAN));
	}

	/**
	 * [試験観点] : データ初期化、少数文字列(少数点から開始の文字列)
	 * [期待される結果] : 正常終了すること、タイプがDECIMALであること
	 * 
	 * @throws Throwable
	 */
	@Test
	public void Test006() throws Throwable {
		// テストクラス初期化
		String[] data = new String[5];
		data[0] = ".01";
		data[1] = ".2";
		data[2] = "-.01";
		data[3] = "1e8";
		data[4] = "-.2";

		testTarget = new DataTypeHandler(data);
		// 検証
		KagerowDataType[] result = testTarget.getDataType();
		assertThat(result[0], is(KagerowDataType.DECIMAL));
		assertThat(result[1], is(KagerowDataType.DECIMAL));
		assertThat(result[2], is(KagerowDataType.DECIMAL));
		assertThat(result[3], is(KagerowDataType.DECIMAL));
		assertThat(result[4], is(KagerowDataType.DECIMAL));
	}

	/**
	 * [試験観点] : データ初期化、日付文字列(共通フォーマット)
	 * [期待される結果] : 正常終了すること、タイプがDATEであること
	 * 
	 * @throws Throwable
	 */
	@Test
	public void Test007() throws Throwable {

		// テストクラス初期化
		String[] data = new String[13];

		// yyyy-MM-dd['T'HH[:mm[:ss]]]形式
		// 日付のみ
		data[0] = "2025-07-31";
		// 時間あり
		data[1] = "2025-07-31T14";
		// 時分あり
		data[2] = "2025-07-31T14:30";
		// 時分秒あり
		data[3] = "2025-07-31T14:30:59";

		// yyyy/MM/dd[ HH[:mm[:ss]]]形式
		// 日付のみ
		data[4] = "2025/07/31";
		// 時間あり
		data[5] = "2025/07/31 14";
		// 時分あり
		data[6] = "2025/07/31 14:30";
		// 時分秒あり
		data[7] = "2025/07/31 14:30:59";

		// yyyyMMdd[ HHmmss]形式
		// 日付のみ※ここはアプリケーションでカスタマイズする
		data[8] = "20250731";
		// 日付 + 時分秒
		data[9] = "20250731 143059";

		// dd/MM/yyyy[ HH[:mm[:ss]]]形式
		// 日付のみ
		data[10] = "31/07/2025";
		// 時分あり
		data[11] = "31/07/2025 14";
		// 時分秒あり
		data[12] = "31/07/2025 14:30:59";

		testTarget = new DataTypeHandler(data);
		// 検証
		KagerowDataType[] result = testTarget.getDataType();
		assertThat(result[0], is(KagerowDataType.DATE));
		assertThat(result[1], is(KagerowDataType.TIMESTAMP));
		assertThat(result[2], is(KagerowDataType.TIMESTAMP));
		assertThat(result[3], is(KagerowDataType.TIMESTAMP));
		assertThat(result[4], is(KagerowDataType.DATE));
		assertThat(result[5], is(KagerowDataType.TIMESTAMP));
		assertThat(result[6], is(KagerowDataType.TIMESTAMP));
		assertThat(result[7], is(KagerowDataType.TIMESTAMP));
		assertThat(result[8], is(KagerowDataType.NUMBER));
		assertThat(result[9], is(KagerowDataType.TIMESTAMP));
		assertThat(result[10], is(KagerowDataType.DATE));
		assertThat(result[11], is(KagerowDataType.TIMESTAMP));
		assertThat(result[12], is(KagerowDataType.TIMESTAMP));
	}

	/**
	 * [試験観点] : データ初期化、日付文字列(英語フォーマット)
	 * [期待される結果] : 正常終了すること、タイプがDATEであること
	 * 
	 * @throws Throwable
	 */
	@Test
	public void Test008() throws Throwable {

		// テストクラス初期化
		String[] data = new String[4];

		// dd-MMM-yyyy[ HH[:mm[:ss]]]形式
		// 英語の月表記
		data[0] = "31-Jul-2025";
		// 英語 + 時分
		data[1] = "31-Jul-2025 14:30";

		// EEE, dd MMM yyyy HH:mm[:ss]形式
		// 曜日・月名あり
		data[2] = "Thu, 31 Jul 2025 14:30";
		// 秒付き
		data[3] = "Thu, 31 Jul 2025 14:30:59";

		testTarget = new DataTypeHandler(data);
		// 検証
		KagerowDataType[] result = testTarget.getDataType();
		assertThat(result[0], is(KagerowDataType.DATE));
		assertThat(result[1], is(KagerowDataType.TIMESTAMP));
		assertThat(result[2], is(KagerowDataType.TIMESTAMP));
		assertThat(result[3], is(KagerowDataType.TIMESTAMP));
	}

	/**
	 * [試験観点] : データ初期化、日付文字列(日本語フォーマット)
	 * [期待される結果] : 正常終了すること、タイプがDATEであること
	 * 
	 * @throws Throwable
	 */
	@Test
	public void Test009() throws Throwable {

		// テストクラス初期化
		String[] data = new String[10];

		// yyyy年M月d日[ H時m分[s秒]]形式
		// 和文日付
		data[0] = "2025年7月31日";
		// 和文時間付き
		data[1] = "2025年7月31日 14時30分";
		// 和文時間 + 秒
		data[2] = "2025年7月31日 14時30分59秒";

		// M月d日[ H時m分[s秒]]形式
		// ※年がないため文字列として扱う
		// 和文日付
		data[3] = "7月31日";
		// 和文時間付き
		data[4] = "7月31日 14:30";
		// 和文時間 + 秒
		data[5] = "7月31日 14:30:59";

		// yyyy年M月d日(E) [H:mm[:ss]]形式
		// 和文日付
		data[6] = "2025年7月31日(木)";
		// 和文時間付き
		data[7] = "2025年7月31日(木) 14時30分";
		// 和文時間 + 秒
		data[8] = "2025年7月31日(木) 14時30分59秒";

		// uuuu年MM月dd日'T'HH時mm分ss秒形式
		data[9] = "2025年07月31日T14時30分59秒";

		testTarget = new DataTypeHandler(data);
		// 検証
		KagerowDataType[] result = testTarget.getDataType();
		assertThat(result[0], is(KagerowDataType.DATE));
		assertThat(result[1], is(KagerowDataType.TIMESTAMP));
		assertThat(result[2], is(KagerowDataType.TIMESTAMP));
		assertThat(result[3], is(KagerowDataType.VARCHAR));
		assertThat(result[4], is(KagerowDataType.VARCHAR));
		assertThat(result[5], is(KagerowDataType.VARCHAR));
		assertThat(result[6], is(KagerowDataType.DATE));
		assertThat(result[7], is(KagerowDataType.TIMESTAMP));
		assertThat(result[8], is(KagerowDataType.TIMESTAMP));
		assertThat(result[9], is(KagerowDataType.TIMESTAMP));
	}

	/**
	 * [試験観点] : データ更新
	 * [期待される結果] : 正常終了すること、タイプが更新されていないこと。
	 * 
	 * @throws Throwable
	 */
	@Test
	public void Test010() throws Throwable {
		// テストクラス初期化
		String[] data = new String[5];
		data[0] = "1200.0";
		data[1] = "-1.2";
		data[2] = "-0.01";
		data[3] = "-1e8";
		data[4] = "True";

		testTarget = new DataTypeHandler(data);
		// 検証
		KagerowDataType[] result = testTarget.getDataType();
		assertThat(result[0], is(KagerowDataType.DECIMAL));
		assertThat(result[1], is(KagerowDataType.DECIMAL));
		assertThat(result[2], is(KagerowDataType.DECIMAL));
		assertThat(result[3], is(KagerowDataType.DECIMAL));
		assertThat(result[4], is(KagerowDataType.BOOLEAN));

		data[0] = "200.0";
		data[1] = "-0.2";
		data[2] = "-1.01";
		data[3] = "-1e4";
		data[4] = "True";

		testTarget.update(data);

		result = testTarget.getDataType();
		assertThat(result[0], is(KagerowDataType.DECIMAL));
		assertThat(result[1], is(KagerowDataType.DECIMAL));
		assertThat(result[2], is(KagerowDataType.DECIMAL));
		assertThat(result[3], is(KagerowDataType.DECIMAL));
		assertThat(result[4], is(KagerowDataType.BOOLEAN));
	}

	/**
	 * [試験観点] : データ更新
	 * [期待される結果] : 正常終了すること、タイプが更新されていること
	 * 
	 * @throws Throwable
	 */
	@Test
	public void Test011() throws Throwable {
		// テストクラス初期化
		String[] data = new String[5];
		data[0] = "1200.0";
		data[1] = "-1.2";
		data[2] = "-0.01";
		data[3] = "-1e8";
		data[4] = "True";

		testTarget = new DataTypeHandler(data);
		// 検証
		KagerowDataType[] result = testTarget.getDataType();
		assertThat(result[0], is(KagerowDataType.DECIMAL));
		assertThat(result[1], is(KagerowDataType.DECIMAL));
		assertThat(result[2], is(KagerowDataType.DECIMAL));
		assertThat(result[3], is(KagerowDataType.DECIMAL));
		assertThat(result[4], is(KagerowDataType.BOOLEAN));

		data[0] = "テスト200.0";
		data[1] = "--0.2";
		data[2] = "-1.01--";
		data[3] = "¥-1e4";
		data[4] = "True or False";

		testTarget.update(data);

		result = testTarget.getDataType();
		assertThat(result[0], is(KagerowDataType.VARCHAR));
		assertThat(result[1], is(KagerowDataType.VARCHAR));
		assertThat(result[2], is(KagerowDataType.VARCHAR));
		assertThat(result[3], is(KagerowDataType.VARCHAR));
		assertThat(result[4], is(KagerowDataType.VARCHAR));
	}

	/**
	 * [試験観点] : データ更新、不正
	 * [期待される結果] : 例外が発生すること
	 * 
	 * @throws Throwable
	 */
	@Test
	public void Test012() throws Throwable {
		// テストクラス初期化
		String[] data = new String[5];
		data[0] = "1200.0";
		data[1] = "-1.2";
		data[2] = "-0.01";
		data[3] = "-1e8";
		data[4] = "True";

		testTarget = new DataTypeHandler(data);
		// 検証
		KagerowDataType[] result = testTarget.getDataType();
		assertThat(result[0], is(KagerowDataType.DECIMAL));
		assertThat(result[1], is(KagerowDataType.DECIMAL));
		assertThat(result[2], is(KagerowDataType.DECIMAL));
		assertThat(result[3], is(KagerowDataType.DECIMAL));
		assertThat(result[4], is(KagerowDataType.BOOLEAN));

		data = new String[4];
		data[0] = "テスト200.0";
		data[1] = "--0.2";
		data[2] = "-1.01--";
		data[3] = "¥-1e4";

		try {
			testTarget.update(data);
			fail();
		} catch (IllegalStateException e) {
			assertThat(e.getMessage(), is(ErrorMessage.CODE_007.getMessage()));
		}

	}

}

package com.sakulabo.library.text;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.rowset.CachedRowSet;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.sakulabo.BaseTest;
import com.sakulabo.core.Kagerow.KagerowApplication;
import com.sakulabo.core.Kagerow.Utilities.KagerowDBMode;
import com.sakulabo.library.common.DefaultPluginMessage;
import com.sakulabo.regulation.annotation.KagerowPlugin.PluginType;
import com.sakulabo.regulation.spi.PluginAdapter.KagerowRowSet;

/**
 * TSV出力プラグインのテストクラスです
 */
public class JSONDefaultPluginTest extends BaseTest<JSONDefaultPlugin> {

	/** テスト対象 */
	@InjectMocks
	protected JSONDefaultPlugin testTarget;

	@Mock
	private Connection connection;
	@Mock
	private Statement statement;
	@Mock
	private PreparedStatement preparedStatement;
	@Mock
	private CachedRowSet cachedRowSet;
	@Mock
	private ResultSetMetaData metaData;

	/**
	 * デフォルトコンストラクタ
	 */
	protected JSONDefaultPluginTest() {
		super(JSONDefaultPluginTest.class);
	}

	@BeforeEach
	void initService() {
		// セキュアコンテキスト生成
		KagerowApplication.getInstance("test");
		closeable = MockitoAnnotations.openMocks(this);
	}

	@AfterEach
	void closeService() throws Exception {
		closeable.close();
	}

	/**
	 * [試験観点] : 通常JSONファイル
	 * [期待される結果] : 以下である
	 * ・正常終了すること
	 * ・ファイルの内容が正しく出力できていること
	 * ・複数行
	 * ・日付フォーマット未指定
	 */
	@Test
	public void Test001() throws Throwable {

		// 引数準備
		KagerowRowSet targetData = new KagerowRowSet(KagerowDBMode.ORACLE.toString(), "test", "testName", cachedRowSet);

		// モック準備
		Object data1 = "test";
		Object data2 = Date.from(LocalDate.of(2025, 10, 6).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
		Object data3 = BigDecimal.valueOf(1.11d);
		Object data4 = Integer.valueOf(1);
		Object data5 = Boolean.TRUE;
		Object[] dataList = { data1, data2, data3, data4, data5 };
		String[] labelList = { "col1", "col2", "col3", "col4", "col5" };

		doAnswer(new Answer<String>() {

			private int count = 0;

			@Override
			public String answer(InvocationOnMock invocation) throws Throwable {
				String obj = labelList[count++];
				if (4 < count) {
					count = 0;
				}
				return obj;
			}

		}).when(metaData).getColumnLabel(anyInt());

		doAnswer(new Answer<Object>() {

			private int count = 0;

			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				Object obj = dataList[count++];
				if (4 < count) {
					count = 0;
				}
				return obj;
			}

		}).when(cachedRowSet).getObject(anyInt());

		doAnswer(new Answer<Boolean>() {

			private int count = 0;
			private int maxCount = 2;

			@Override
			public Boolean answer(InvocationOnMock invocation) throws Throwable {
				if (maxCount < count++) {
					return false;
				}
				return true;
			}

		}).when(cachedRowSet).next();

		doReturn(5).when(metaData).getColumnCount();

		doReturn(metaData).when(cachedRowSet).getMetaData();

		// データ準備
		Path resultFile = getOutputPath("Test001.json");
		Map<String, String> params = new HashMap<>();
		params.put("OutputPath", resultFile.toString());
		params.put("KsqlId", "test");
		params.put("DateFormat", "YYYY-MM-dd");

		// テスト実行
		testTarget.output(params, List.of(targetData));

		try {
			// 結果検証
			Path expect = testDir.resolve("expect_001.json");
			long result = Files.mismatch(resultFile, expect);
			// バイト単位で比較を行い、完全一致の場合は-1L
			// そうでない場合は一致しないバイト位置が返却される
			assertThat(result, is(-1L));
		} finally {
			Files.delete(resultFile);
		}

	}

	/**
	 * [試験観点] : 通常JSONファイル
	 * [期待される結果] : 以下である
	 * ・正常終了すること
	 * ・ファイルの内容が正しく出力できていること
	 * ・複数行
	 * ・日付フォーマット指定
	 */
	@Test
	public void Test002() throws Throwable {

		// 引数準備
		KagerowRowSet targetData = new KagerowRowSet(KagerowDBMode.ORACLE.toString(), "test", "testName", cachedRowSet);

		// モック準備
		Object data1 = "test";
		Object data2 = Date.from(LocalDate.of(2025, 10, 6).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
		Object data3 = BigDecimal.valueOf(1.11d);
		Object data4 = Integer.valueOf(1);
		Object data5 = Boolean.TRUE;
		Object[] dataList = { data1, data2, data3, data4, data5 };
		String[] labelList = { "col1", "col2", "col3", "col4", "col5" };

		doAnswer(new Answer<String>() {

			private int count = 0;

			@Override
			public String answer(InvocationOnMock invocation) throws Throwable {
				String obj = labelList[count++];
				if (4 < count) {
					count = 0;
				}
				return obj;
			}

		}).when(metaData).getColumnLabel(anyInt());

		doAnswer(new Answer<Object>() {

			private int count = 0;

			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				Object obj = dataList[count++];
				if (4 < count) {
					count = 0;
				}
				return obj;
			}

		}).when(cachedRowSet).getObject(anyInt());

		doAnswer(new Answer<Boolean>() {

			private int count = 0;
			private int maxCount = 2;

			@Override
			public Boolean answer(InvocationOnMock invocation) throws Throwable {
				if (maxCount < count++) {
					return false;
				}
				return true;
			}

		}).when(cachedRowSet).next();

		doReturn(5).when(metaData).getColumnCount();

		doReturn(metaData).when(cachedRowSet).getMetaData();

		// データ準備
		Path resultFile = getOutputPath("Test002.json");
		Map<String, String> params = new HashMap<>();
		params.put("OutputPath", resultFile.toString());
		params.put("KsqlId", "test");
		params.put("DateFormat", "YYYY/MM/dd hh:mm:ss.SSS");

		// テスト実行
		testTarget.output(params, List.of(targetData));

		try {
			// 結果検証
			Path expect = testDir.resolve("expect_002.json");
			long result = Files.mismatch(resultFile, expect);
			// バイト単位で比較を行い、完全一致の場合は-1L
			// そうでない場合は一致しないバイト位置が返却される
			assertThat(result, is(-1L));
		} finally {
			Files.delete(resultFile);
		}

	}

	/**
	 * [試験観点] : 出力バリデーションチェック
	 * [期待される結果] : 以下である
	 * ・存在しないパス
	 * ・バリデーションエラーが発生する
	 * ・メッセージが期待通りであること
	 */
	@Test
	public void Test003() throws Throwable {

		// データ準備
		Path testPath = getOutputPath("Test008/Test.json");
		Map<String, String> params = new HashMap<>();
		params.put("OutputPath", testPath.toString());
		params.put("KsqlId", "test");
		params.put("DateFormat", "YYYY-MM-dd");

		// 期待値
		String expMsg = DefaultPluginMessage.E0004.toString(new Object[] { testPath.getParent() });

		// テスト実行
		try {
			testTarget.validation(params, PluginType.OUTPUT);
			fail();
		} catch (Exception e) {
			assertThat(e.getMessage(), is(expMsg));
		}

	}

	/**
	 * [試験観点] : 出力バリデーションチェック
	 * [期待される結果] : 以下である
	 * ・不正パス
	 * ・バリデーションエラーが発生する
	 * ・メッセージが期待通りであること
	 */
	@Test
	public void Test004() throws Throwable {

		// データ準備
		Map<String, String> params = new HashMap<>();
		params.put("KsqlId", "test");
		params.put("DateFormat", "YYYY-MM-dd");

		// 期待値
		String expMsg = DefaultPluginMessage.E0002.toString(new Object[] { "OutputPath" });

		// テスト実行
		try {
			testTarget.validation(params, PluginType.OUTPUT);
			fail();
		} catch (Exception e) {
			assertThat(e.getMessage(), is(expMsg));
		}

	}

}

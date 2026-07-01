package com.sakulabo.library.tsv;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.BufferedReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.rowset.CachedRowSet;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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
@SuppressWarnings("javadoc")
public class TSVDefaultPluginTest extends BaseTest<TSVDefaultPlugin> {

	/** テスト対象 */
	@InjectMocks
	protected TSVDefaultPlugin testTarget;

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
	protected TSVDefaultPluginTest() {
		super(TSVDefaultPluginTest.class);
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
	 * [試験観点]      : 通常CSVファイル
	 * [期待される結果] : 以下である
	 *                  ・正常終了すること
	 *                  ・ファイルの内容が正しく読み込みていること
	 *                  ・DDLが正しく生成されていること
	 *                  ・DMLが正しく生成されていること
	 */
	@Test
	public void Test001() throws Throwable {

		// 引数のキャプチャ
		ArgumentCaptor<String> argCaptor_statement = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> argCaptor_preparedStatement = ArgumentCaptor.forClass(String.class);

		// モック準備
		doReturn(statement).when(connection).createStatement();
		doReturn(true).when(statement).execute(argCaptor_statement.capture());

		doReturn(preparedStatement).when(connection).prepareStatement(any());
		doNothing().when(preparedStatement).setString(anyInt(), argCaptor_preparedStatement.capture());

		// データ準備
		String initDDL = """
					CREATE OR REPLACE VIEW V_TEST \
					AS SELECT * FROM temporary;
				""";
		Map<String, String> params = new HashMap<>();
		params.put("InputPath", testDir.resolve("test1.csv").toString());
		params.put("IsHeader", "true");
		params.put("TableName", "temporary");
		params.put("IsEscape", "false");
		params.put("Charset", StandardCharsets.UTF_8.name());
		params.put("InitDDL", initDDL);

		// テスト実行
		testTarget.input(params, KagerowDBMode.ORACLE, connection);

		// 結果検証
		assertThat(argCaptor_statement.getAllValues().get(0), is("DROP TABLE IF EXISTS temporary;"));
		assertThat(argCaptor_statement.getAllValues().get(1), is("""
				CREATE TABLE temporary ( \
				CLOB col1 , \
				CLOB col2 , \
				CLOB col3 , \
				CLOB col4 , \
				CLOB col5 , \
				CLOB col6 \
				);"""));
		assertThat(argCaptor_statement.getAllValues().get(2), is(initDDL));

		assertThat(argCaptor_preparedStatement.getAllValues().get(0), is("1"));
		assertThat(argCaptor_preparedStatement.getAllValues().get(1), is("http://sample.com"));
		assertThat(argCaptor_preparedStatement.getAllValues().get(2), is("ffCs9qN0@test.co.jp"));
		assertThat(argCaptor_preparedStatement.getAllValues().get(3), is("9.238.120.223"));
		assertThat(argCaptor_preparedStatement.getAllValues().get(4), is("WeVvJxcc"));
		assertThat(argCaptor_preparedStatement.getAllValues().get(5), is("1974/12/27 10:50:36"));

	}

	/**
	 * [試験観点]      : バリデーションチェック
	 * [期待される結果] : 以下である
	 *                  ・正常終了すること
	 */
	@Test
	public void Test002() throws Throwable {

		// データ準備
		String initDDL = """
					CREATE OR REPLACE VIEW V_TEST \
					AS SELECT * FROM temporary;
				""";
		Map<String, String> params = new HashMap<>();
		params.put("InputPath", testDir.resolve("test1.csv").toString());
		params.put("IsHeader", "true");
		params.put("TableName", "temporary");
		params.put("IsEscape", "false");
		params.put("Charset", StandardCharsets.UTF_8.name());
		params.put("InitDDL", initDDL);

		// テスト実行
		testTarget.validation(params, PluginType.INPUT);

	}

	/**
	 * [試験観点]      : 入力バリデーションチェック
	 * [期待される結果] : 以下である
	 *                  ・存在しないパス
	 *                  ・バリデーションエラーが発生する
	 *                  ・メッセージが期待通りであること
	 */
	@Test
	public void Test003() throws Throwable {

		// データ準備
		String initDDL = """
					CREATE OR REPLACE VIEW V_TEST \
					AS SELECT * FROM temporary;
				""";
		Map<String, String> params = new HashMap<>();
		// 試験観点対象データ
		String testPath = testDir.resolve("test99.csv").toString();
		params.put("InputPath", testPath);
		params.put("IsHeader", "true");
		params.put("TableName", "temporary");
		params.put("IsEscape", "false");
		params.put("Charset", StandardCharsets.UTF_8.name());
		params.put("InitDDL", initDDL);

		// 期待値
		String expMsg = DefaultPluginMessage.E0004.toString(new Object[] { testPath });

		// テスト実行
		try {
			testTarget.validation(params, PluginType.INPUT);
			fail();
		} catch (Exception e) {
			assertThat(e.getMessage(), is(expMsg));
		}

	}

	/**
	 * [試験観点]      : 入力バリデーションチェック
	 * [期待される結果] : 以下である
	 *                  ・不正パス
	 *                  ・バリデーションエラーが発生する
	 *                  ・メッセージが期待通りであること
	 */
	@Test
	public void Test004() throws Throwable {

		// データ準備
		String initDDL = """
					CREATE OR REPLACE VIEW V_TEST \
					AS SELECT * FROM temporary;
				""";
		Map<String, String> params = new HashMap<>();
		params.put("IsHeader", "true");
		params.put("TableName", "temporary");
		params.put("IsEscape", "false");
		params.put("Charset", StandardCharsets.UTF_8.name());
		params.put("InitDDL", initDDL);

		// 期待値
		String expMsg = DefaultPluginMessage.E0002.toString(new Object[] { "InputPath" });

		// テスト実行
		try {
			testTarget.validation(params, PluginType.INPUT);
			fail();
		} catch (Exception e) {
			assertThat(e.getMessage(), is(expMsg));
		}

	}

	/**
	 * [試験観点]      : 通常CSVファイル
	 * [期待される結果] : 以下である
	 *                  ・正常終了すること
	 *                  ・ファイルの内容が正しく出力できていること
	 *                  ・単体行
	 */
	@Test
	public void Test005() throws Throwable {

		// 引数準備
		KagerowRowSet targetData = new KagerowRowSet(KagerowDBMode.ORACLE.toString(), "test", "testName", cachedRowSet);

		// モック準備
		Object data1 = "test";
		Object data2 = Date.from(LocalDate.of(2025, 10, 6).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
		Object data3 = BigDecimal.valueOf(1.11d);
		Object data4 = Integer.valueOf(1);
		Object data5 = Boolean.TRUE;
		doReturn(data1).when(cachedRowSet).getObject(1);
		doReturn(data2).when(cachedRowSet).getObject(2);
		doReturn(data3).when(cachedRowSet).getObject(3);
		doReturn(data4).when(cachedRowSet).getObject(4);
		doReturn(data5).when(cachedRowSet).getObject(5);

		doAnswer(new Answer<Boolean>() {

			private boolean isFirst = true;

			@Override
			public Boolean answer(InvocationOnMock invocation) throws Throwable {
				Boolean result = isFirst;
				if (result)
					isFirst ^= true;
				return result;
			}

		}).when(cachedRowSet).next();

		doReturn(5).when(metaData).getColumnCount();

		doReturn(metaData).when(cachedRowSet).getMetaData();

		// データ準備
		Path resultFile = getOutputPath("Test005.csv");
		Map<String, String> params = new HashMap<>();
		params.put("OutputPath", resultFile.toString());
		params.put("IsEscape", "false");
		params.put("IsHeader", "false");
		params.put("KsqlId", "test");
		params.put("DateFormat", "YYYY-MM-dd");
		params.put("Charset", StandardCharsets.UTF_8.name());

		// テスト実行
		testTarget.output(params, List.of(targetData));

		// 結果検証
		List<String> resultList = new ArrayList<>();
		try (BufferedReader reader = Files.newBufferedReader(resultFile, StandardCharsets.UTF_8)) {
			String line;
			while ((line = reader.readLine()) != null)
				resultList.add(line);
		} finally {
			Files.delete(resultFile);
		}

		assertThat(resultList.size(), is(1));
		assertThat(resultList.get(0), is("test	2025-10-06	1.11	1	true"));

	}

	/**
	 * [試験観点]      : 通常CSVファイル
	 * [期待される結果] : 以下である
	 *                  ・正常終了すること
	 *                  ・ファイルの内容が正しく出力できていること
	 *                  ・複数行
	 */
	@Test
	public void Test006() throws Throwable {

		// 引数準備
		KagerowRowSet targetData = new KagerowRowSet(KagerowDBMode.ORACLE.toString(), "test", "testName", cachedRowSet);

		// モック準備
		Object data1 = "test";
		Object data2 = Date.from(LocalDate.of(2025, 10, 6).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
		Object data3 = BigDecimal.valueOf(1.11d);
		Object data4 = Integer.valueOf(1);
		Object data5 = Boolean.TRUE;
		Object[] dataList = { data1, data2, data3, data4, data5 };

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
		Path resultFile = getOutputPath("Test006.csv");
		Map<String, String> params = new HashMap<>();
		params.put("OutputPath", resultFile.toString());
		params.put("IsEscape", "false");
		params.put("IsHeader", "false");
		params.put("KsqlId", "test");
		params.put("DateFormat", "YYYY-MM-dd");
		params.put("Charset", StandardCharsets.UTF_8.name());

		// テスト実行
		testTarget.output(params, List.of(targetData));

		// 結果検証
		List<String> resultList = new ArrayList<>();
		try (BufferedReader reader = Files.newBufferedReader(resultFile, StandardCharsets.UTF_8)) {
			String line;
			while ((line = reader.readLine()) != null)
				resultList.add(line);
		} finally {
			Files.delete(resultFile);
		}

		assertThat(resultList.size(), is(3));
		for (int i = 0; i < resultList.size(); i++)
			assertThat(resultList.get(i), is("test	2025-10-06	1.11	1	true"));

	}

	/**
	 * [試験観点]      : 通常CSVファイル
	 * [期待される結果] : 以下である
	 *                  ・正常終了すること
	 *                  ・ファイルの内容が正しく出力できていること
	 *                  ・複数行
	 *                  ・エスケープON
	 */
	@Test
	public void Test007() throws Throwable {

		// 引数準備
		KagerowRowSet targetData = new KagerowRowSet(KagerowDBMode.ORACLE.toString(), "test", "testName", cachedRowSet);

		// モック準備
		Object data1 = "test";
		Object data2 = Date.from(LocalDate.of(2025, 10, 6).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
		Object data3 = BigDecimal.valueOf(1.11d);
		Object data4 = Integer.valueOf(1);
		Object data5 = Boolean.TRUE;
		Object[] dataList = { data1, data2, data3, data4, data5 };

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
		Path resultFile = getOutputPath("Test007.csv");
		Map<String, String> params = new HashMap<>();
		params.put("OutputPath", resultFile.toString());
		params.put("IsEscape", "true");
		params.put("IsHeader", "false");
		params.put("KsqlId", "test");
		params.put("DateFormat", "YYYY-MM-dd");
		params.put("Charset", StandardCharsets.UTF_8.name());

		// テスト実行
		testTarget.output(params, List.of(targetData));

		// 結果検証
		List<String> resultList = new ArrayList<>();
		try (BufferedReader reader = Files.newBufferedReader(resultFile, StandardCharsets.UTF_8)) {
			String line;
			while ((line = reader.readLine()) != null)
				resultList.add(line);
		} finally {
			Files.delete(resultFile);
		}

		assertThat(resultList.size(), is(3));
		for (int i = 0; i < resultList.size(); i++)
			assertThat(resultList.get(i), is("\"test\"	\"2025-10-06\"	\"1.11\"	\"1\"	\"true\""));

	}

	/**
	 * [試験観点]      : 出力バリデーションチェック
	 * [期待される結果] : 以下である
	 *                  ・存在しないパス
	 *                  ・バリデーションエラーが発生する
	 *                  ・メッセージが期待通りであること
	 */
	@Test
	public void Test008() throws Throwable {

		// データ準備
		Path testPath = getOutputPath("Test008/Test.tsv");
		Map<String, String> params = new HashMap<>();
		params.put("OutputPath", testPath.toString());
		params.put("IsEscape", "true");
		params.put("IsHeader", "false");
		params.put("KsqlId", "test");
		params.put("DateFormat", "YYYY-MM-dd");
		params.put("Charset", StandardCharsets.UTF_8.name());

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
	 * [試験観点]      : 出力バリデーションチェック
	 * [期待される結果] : 以下である
	 *                  ・不正パス
	 *                  ・バリデーションエラーが発生する
	 *                  ・メッセージが期待通りであること
	 */
	@Test
	public void Test009() throws Throwable {

		// データ準備
		Map<String, String> params = new HashMap<>();
		params.put("IsEscape", "true");
		params.put("IsHeader", "false");
		params.put("KsqlId", "test");
		params.put("DateFormat", "YYYY-MM-dd");
		params.put("Charset", StandardCharsets.UTF_8.name());

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

	/**
	 * [試験観点]      : 通常TSVファイル
	 * [期待される結果] : 以下である
	 *                  ・正常終了すること
	 *                  ・ファイルの内容が正しく出力できていること
	 *                  ・単体行
	 *                  ・ヘッダーあり
	 */
	@Test
	public void Test010() throws Throwable {

		// 引数準備
		KagerowRowSet targetData = new KagerowRowSet(KagerowDBMode.ORACLE.toString(), "test", "testName", cachedRowSet);

		// モック準備
		Object data1 = "test";
		Object data2 = Date.from(LocalDate.of(2025, 10, 6).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
		Object data3 = BigDecimal.valueOf(1.11d);
		Object data4 = Integer.valueOf(1);
		Object data5 = Boolean.TRUE;
		doReturn(data1).when(cachedRowSet).getObject(1);
		doReturn(data2).when(cachedRowSet).getObject(2);
		doReturn(data3).when(cachedRowSet).getObject(3);
		doReturn(data4).when(cachedRowSet).getObject(4);
		doReturn(data5).when(cachedRowSet).getObject(5);

		doAnswer(new Answer<Boolean>() {

			private boolean isFirst = true;

			@Override
			public Boolean answer(InvocationOnMock invocation) throws Throwable {
				Boolean result = isFirst;
				if (result)
					isFirst ^= true;
				return result;
			}

		}).when(cachedRowSet).next();

		doReturn(5).when(metaData).getColumnCount();
		doReturn("test1").when(metaData).getColumnLabel(1);
		doReturn("test2").when(metaData).getColumnLabel(2);
		doReturn("test3").when(metaData).getColumnLabel(3);
		doReturn("test4").when(metaData).getColumnLabel(4);
		doReturn("test5").when(metaData).getColumnLabel(5);

		doReturn(metaData).when(cachedRowSet).getMetaData();

		// データ準備
		Path resultFile = getOutputPath("Test010.csv");
		Map<String, String> params = new HashMap<>();
		params.put("OutputPath", resultFile.toString());
		params.put("IsEscape", "false");
		params.put("IsHeader", "true");
		params.put("KsqlId", "test");
		params.put("DateFormat", "YYYY-MM-dd");
		params.put("Charset", StandardCharsets.UTF_8.name());

		// テスト実行
		testTarget.output(params, List.of(targetData));

		// 結果検証
		List<String> resultList = new ArrayList<>();
		try (BufferedReader reader = Files.newBufferedReader(resultFile, StandardCharsets.UTF_8)) {
			String line;
			while ((line = reader.readLine()) != null)
				resultList.add(line);
		} finally {
			Files.delete(resultFile);
		}

		assertThat(resultList.size(), is(2));
		assertThat(resultList.get(0), is("test1	test2	test3	test4	test5"));
		assertThat(resultList.get(1), is("test	2025-10-06	1.11	1	true"));

	}

	/**
	 * [試験観点]      : 通常TSVファイル
	 * [期待される結果] : 以下である
	 *                  ・正常終了すること
	 *                  ・ファイルの内容が正しく出力できていること
	 *                  ・複数行
	 *                  ・エスケープON
	 *                  ・ヘッダーあり
	 */
	@Test
	public void Test011() throws Throwable {

		// 引数準備
		KagerowRowSet targetData = new KagerowRowSet(KagerowDBMode.ORACLE.toString(), "test", "testName", cachedRowSet);

		// モック準備
		Object data1 = "test";
		Object data2 = Date.from(LocalDate.of(2025, 10, 6).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
		Object data3 = BigDecimal.valueOf(1.11d);
		Object data4 = Integer.valueOf(1);
		Object data5 = Boolean.TRUE;
		Object[] dataList = { data1, data2, data3, data4, data5 };

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
		doReturn("test1").when(metaData).getColumnLabel(1);
		doReturn("test2").when(metaData).getColumnLabel(2);
		doReturn("test3").when(metaData).getColumnLabel(3);
		doReturn("test4").when(metaData).getColumnLabel(4);
		doReturn("test5").when(metaData).getColumnLabel(5);

		doReturn(metaData).when(cachedRowSet).getMetaData();

		// データ準備
		Path resultFile = getOutputPath("Test011.csv");
		Map<String, String> params = new HashMap<>();
		params.put("OutputPath", resultFile.toString());
		params.put("IsEscape", "true");
		params.put("IsHeader", "true");
		params.put("KsqlId", "test");
		params.put("DateFormat", "YYYY-MM-dd");
		params.put("Charset", StandardCharsets.UTF_8.name());

		// テスト実行
		testTarget.output(params, List.of(targetData));

		// 結果検証
		List<String> resultList = new ArrayList<>();
		try (BufferedReader reader = Files.newBufferedReader(resultFile, StandardCharsets.UTF_8)) {
			String line;
			while ((line = reader.readLine()) != null)
				resultList.add(line);
		} finally {
			Files.delete(resultFile);
		}

		assertThat(resultList.size(), is(4));

		assertThat(resultList.get(0), is("\"test1\"	\"test2\"	\"test3\"	\"test4\"	\"test5\""));

		for (int i = 1; i < resultList.size(); i++)
			assertThat(resultList.get(i), is("\"test\"	\"2025-10-06\"	\"1.11\"	\"1\"	\"true\""));

	}

}

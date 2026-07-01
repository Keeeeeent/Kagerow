package com.sakulabo.library.sql;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sakulabo.BaseTest;
import com.sakulabo.core.Kagerow.KagerowApplication;

/**
 * JDBCプラグインのテストクラスです 
 */
@SuppressWarnings("javadoc")
public class JDBCDefaultPluginTest extends BaseTest<JDBCDefaultPlugin> {

	/** テスト対象 */
	@InjectMocks
	protected JDBCDefaultPlugin testTarget;

	@Mock
	private Connection connection;
	@Mock
	private Statement statement;

	private static MethodHandle createTabelName;
	static {
		MethodHandles.Lookup lookup = MethodHandles.lookup();
		MethodType type = MethodType.methodType(String.class, Map.class);
		try {
			createTabelName = MethodHandles.privateLookupIn(JDBCDefaultPlugin.class, lookup)
					.findVirtual(JDBCDefaultPlugin.class, "createTabelName", type);
		} catch (NoSuchMethodException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	private static MethodHandle getTransactionLevel;
	static {
		MethodHandles.Lookup lookup = MethodHandles.lookup();
		MethodType type = MethodType.methodType(int.class, Map.class);
		try {
			getTransactionLevel = MethodHandles.privateLookupIn(JDBCDefaultPlugin.class, lookup)
					.findVirtual(JDBCDefaultPlugin.class, "getTransactionLevel", type);
		} catch (NoSuchMethodException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	private static MethodHandle getKDBFilePath;
	static {
		MethodHandles.Lookup lookup = MethodHandles.lookup();
		MethodType type = MethodType.methodType(Path.class, String.class);
		try {
			getKDBFilePath = MethodHandles.privateLookupIn(JDBCDefaultPlugin.class, lookup)
					.findVirtual(JDBCDefaultPlugin.class, "getKDBFilePath", type);
		} catch (NoSuchMethodException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	/**
	 * デフォルトコンストラクタ
	 */
	public JDBCDefaultPluginTest() {
		super(JDBCDefaultPluginTest.class);
	}

	@BeforeEach
	void initService() {
		closeable = MockitoAnnotations.openMocks(this);
		KagerowApplication.getInstance("test");
	}

	@AfterEach
	void closeService() throws Exception {
		closeable.close();
	}

	/**
	 * [試験観点]      : テーブル生成
	 * [期待される結果] : パラメータ指定、指定されたパラメータが返却されること
	 */
	@Test
	public void Test001() throws Throwable {

		// 引数準備
		Map<String, String> params = new HashMap<>() {
			{
				put("table", "test");
			}
		};

		// テスト実行
		String result = (String) createTabelName.invokeExact(testTarget, params);

		// 結果検証
		assertThat(result, is("test"));

	}

	/**
	 * [試験観点]      : テーブル生成
	 * [期待される結果] : パラメータ未指定、指定されたパラメータが返却されること
	 */
	@Test
	public void Test002() throws Throwable {

		// 引数準備
		Map<String, String> params = new HashMap<>() {
			{
				put("id", "ksqlid");
			}
		};

		// テスト実行
		String result = (String) createTabelName.invokeExact(testTarget, params);

		// 結果検証
		assertThat(result, is("tid_ksqlid"));

	}

	/**
	 * [試験観点]      : トランザクション制御レベル
	 * [期待される結果] : パラメータにNONEを指定、Connection.TRANSACTION_NONEが返却されること
	 */
	@Test
	public void Test003() throws Throwable {

		// 引数準備
		Map<String, String> params = new HashMap<>() {
			{
				put("level", "NONE");
			}
		};

		// テスト実行
		int result = (int) getTransactionLevel.invokeExact(testTarget, params);

		// 結果検証
		assertThat(result, is(Connection.TRANSACTION_NONE));

	}

	/**
	 * [試験観点]      : トランザクション制御レベル
	 * [期待される結果] : パラメータにREAD_COMMITTEDを指定、Connection.TRANSACTION_READ_COMMITTEDが返却されること
	 */
	@Test
	public void Test004() throws Throwable {

		// 引数準備
		Map<String, String> params = new HashMap<>() {
			{
				put("level", "READ_COMMITTED");
			}
		};

		// テスト実行
		int result = (int) getTransactionLevel.invokeExact(testTarget, params);

		// 結果検証
		assertThat(result, is(Connection.TRANSACTION_READ_COMMITTED));

	}

	/**
	 * [試験観点]      : トランザクション制御レベル
	 * [期待される結果] : パラメータにREAD_UNCOMMITTEDを指定、Connection.TRANSACTION_READ_UNCOMMITTEDが返却されること
	 */
	@Test
	public void Test005() throws Throwable {

		// 引数準備
		Map<String, String> params = new HashMap<>() {
			{
				put("level", "READ_UNCOMMITTED");
			}
		};

		// テスト実行
		int result = (int) getTransactionLevel.invokeExact(testTarget, params);

		// 結果検証
		assertThat(result, is(Connection.TRANSACTION_READ_UNCOMMITTED));

	}

	/**
	 * [試験観点]      : トランザクション制御レベル
	 * [期待される結果] : パラメータにREPEATABLE_READを指定、Connection.TRANSACTION_REPEATABLE_READが返却されること
	 */
	@Test
	public void Test006() throws Throwable {

		// 引数準備
		Map<String, String> params = new HashMap<>() {
			{
				put("level", "REPEATABLE_READ");
			}
		};

		// テスト実行
		int result = (int) getTransactionLevel.invokeExact(testTarget, params);

		// 結果検証
		assertThat(result, is(Connection.TRANSACTION_REPEATABLE_READ));

	}

	/**
	 * [試験観点]      : トランザクション制御レベル
	 * [期待される結果] : パラメータにSERIALIZABLEを指定、Connection.TRANSACTION_SERIALIZABLEが返却されること
	 */
	@Test
	public void Test007() throws Throwable {

		// 引数準備
		Map<String, String> params = new HashMap<>() {
			{
				put("level", "SERIALIZABLE");
			}
		};

		// テスト実行
		int result = (int) getTransactionLevel.invokeExact(testTarget, params);

		// 結果検証
		assertThat(result, is(Connection.TRANSACTION_SERIALIZABLE));

	}

	/**
	 * [試験観点]      : トランザクション制御レベル
	 * [期待される結果] : パラメータ未指定、Connection.TRANSACTION_READ_COMMITTEDが返却されること
	 */
	@Test
	public void Test008() throws Throwable {

		// 引数準備
		Map<String, String> params = new HashMap<>() {
			{
			}
		};

		// テスト実行
		int result = (int) getTransactionLevel.invokeExact(testTarget, params);

		// 結果検証
		assertThat(result, is(Connection.TRANSACTION_READ_COMMITTED));

	}

	/**
	 * [試験観点]      : KDBファイル生成
	 * [期待される結果] : 期待通りのバスが生成されること
	 */
	@Test
	public void Test009() throws Throwable {

		// 引数準備
		String params = "jdbc:h2:file:/Users/kagerow/Desktop/Kagerow/.kagerow/tmp/kdb14235715985130630006";

		// 期待値用意
		Path exp = Paths.get("/Users/kagerow/Desktop/Kagerow/.kagerow/tmp/kdb14235715985130630006.mv.db");

		// テスト実行
		Path result = (Path) getKDBFilePath.invokeExact(testTarget, params);

		// 結果検証
		assertThat(result, is(exp));

	}

}

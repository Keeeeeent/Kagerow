package com.sakulabo.core.Processor.script;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigInteger;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sakulabo.BaseTest;
import com.sakulabo.core.Kagerow.Contents.KagerowVirtualFileContent.KagerowDataType;
import com.sakulabo.core.Kagerow.Contents.KagerowVirtualFileContent.KagerowVirtualFileObject.BasicFileObject;
import com.sakulabo.core.Kagerow.Contents.Impl.KagerowVirtualFileContentImpl;
import com.sakulabo.core.Kagerow.Context.Impl.KagerowVirtualDirContextImpl;
import com.sakulabo.core.Kagerow.Context.Impl.KagerowVirtualFileContextImpl;

/**
 * KSQLに存在するリンク可能文字列を処理するスクリプトのテストクラスです
 */
public class KsqlDatabaseLinkerTest extends BaseTest<KsqlDatabaseLinker> {

	/** テスト対象 */
	private KsqlDatabaseLinker testTarget;

	/** mock対象 */
	private AutoCloseable closeable;
	@Mock
	private KagerowVirtualFileContextImpl baseContext;
	@Mock
	private KagerowVirtualDirContextImpl context;
	@Mock
	private KagerowVirtualFileContentImpl content;

	/**
	 * デフォルトコンストラクタ
	 */
	protected KsqlDatabaseLinkerTest() {
		super(KsqlDatabaseLinkerTest.class);
	}

	@BeforeEach
	public void setUp() {
		closeable = MockitoAnnotations.openMocks(this);
	}

	@AfterEach
	public void tearDown() throws Exception {
		closeable.close();
	}

	/**
	 * [試験観点] : 対象対象なし
	 * [期待される結果] : 以下である
	 * ・正常終了すること
	 * ・文字列に変化がないこと
	 */
	@Test
	public void Test001() throws Throwable {

		// モック設定
		doReturn(Collections.emptyMap()).when(context).getSynonymMapList();
		doReturn(context).when(baseContext).lookup((String) any());

		// テストデータ準備
		testTarget = new KsqlDatabaseLinker("test", baseContext);
		String sql = """
				SELECT * FROM DUAL;
				""";

		// テスト実施
		String result = testTarget.transform(sql);

		// 結果検証
		assertThat(result, is(sql));

	}

	/**
	 * [試験観点] : 置対象あり
	 * [期待される結果] : 以下である
	 * ・正常終了すること
	 * ・対象が置換されていること(単一)
	 */
	@Test
	public void Test002() throws Throwable {

		// モック設定
		Map<String, String> contextMap = new HashMap<>();
		contextMap.put("target", "test");
		doReturn(contextMap).when(context).getSynonymMapList();
		doReturn(content).when(context).lookup(anyString());
		BasicFileObject fileObject = new BasicFileObject(
				Instant.now(),
				new String[0],
				new KagerowDataType[0],
				new long[0],
				"datAddr",
				BigInteger.ZERO,
				"idxAddr",
				BigInteger.ZERO,
				"binaryName",
				"synonym",
				"test",
				new AtomicReference<>());
		doReturn(fileObject).when(content).get(anyInt());
		doReturn(context).when(baseContext).lookup((String) any());

		// テストデータ準備
		testTarget = new KsqlDatabaseLinker("test", baseContext);
		String sql = """
				SELECT * FROM ${target[1]};
				""";

		// 期待値
		String exp = """
				SELECT * FROM test."KDB_BINARYNAME";
				""";

		// テスト実施
		String result = testTarget.transform(sql);

		// 結果検証
		assertThat(result, is(exp));

	}

	/**
	 * [試験観点] : 置対象あり
	 * [期待される結果] : 以下である
	 * ・正常終了すること
	 * ・対象が置換されていること(単一複数箇所)
	 */
	@Test
	public void Test003() throws Throwable {

		// モック設定
		Map<String, String> contextMap = new HashMap<>();
		contextMap.put("target", "test");
		doReturn(contextMap).when(context).getSynonymMapList();
		doReturn(content).when(context).lookup(anyString());
		BasicFileObject fileObject = new BasicFileObject(
				Instant.now(),
				new String[0],
				new KagerowDataType[0],
				new long[0],
				"datAddr",
				BigInteger.ZERO,
				"idxAddr",
				BigInteger.ZERO,
				"binaryName",
				"synonym",
				"test",
				new AtomicReference<>());
		doReturn(fileObject).when(content).get(anyInt());
		doReturn(context).when(baseContext).lookup((String) any());

		// テストデータ準備
		testTarget = new KsqlDatabaseLinker("test", baseContext);
		String sql = """
				SELECT * FROM ${target[1]} LEFT OUTER JOIN ${target[1]} USING(COL);
				""";

		// 期待値
		String exp = """
				SELECT * FROM test."KDB_BINARYNAME" LEFT OUTER JOIN test."KDB_BINARYNAME" USING(COL);
				""";

		// テスト実施
		String result = testTarget.transform(sql);

		// 結果検証
		assertThat(result, is(exp));

	}

	/**
	 * [試験観点] : 置対象あり
	 * [期待される結果] : 以下である
	 * ・正常終了すること
	 * ・対象が置換されていること(単一複数箇所)
	 * ・エスケープなし
	 * ・シングルクオートあり
	 */
	@Test
	public void Test004() throws Throwable {

		// モック設定
		Map<String, String> contextMap = new HashMap<>();
		contextMap.put("target", "test");
		doReturn(contextMap).when(context).getSynonymMapList();
		doReturn(content).when(context).lookup(anyString());
		BasicFileObject fileObject = new BasicFileObject(
				Instant.now(),
				new String[0],
				new KagerowDataType[0],
				new long[0],
				"datAddr",
				BigInteger.ZERO,
				"idxAddr",
				BigInteger.ZERO,
				"binaryName",
				"synonym",
				"test",
				new AtomicReference<>());
		doReturn(fileObject).when(content).get(anyInt());
		doReturn(context).when(baseContext).lookup((String) any());

		// テストデータ準備
		testTarget = new KsqlDatabaseLinker("test", baseContext);
		String sql = """
				SELECT COL AS "${target[1]}" FROM ${test.target[1]} LEFT OUTER JOIN ${target[1]} USING(COL)
					WHERE COL_NM = 'ID_${target[1]}_001';
				""";

		// 期待値
		String exp = """
				SELECT COL AS "test."KDB_BINARYNAME"" FROM test."KDB_BINARYNAME" LEFT OUTER JOIN test."KDB_BINARYNAME" USING(COL)
					WHERE COL_NM = 'ID_${target[1]}_001';
				""";

		// テスト実施
		String result = testTarget.transform(sql);

		// 結果検証
		assertThat(result, is(exp));

	}

	/**
	 * [試験観点] : 置対象あり
	 * [期待される結果] : 以下である
	 * ・正常終了すること
	 * ・対象が置換されていること(単一複数箇所)
	 * ・エスケープあり
	 * ・シングルクオートあり
	 */
	@Test
	public void Test005() throws Throwable {

		// モック設定
		Map<String, String> contextMap = new HashMap<>();
		contextMap.put("target", "test");
		doReturn(contextMap).when(context).getSynonymMapList();
		doReturn(content).when(context).lookup(anyString());
		BasicFileObject fileObject = new BasicFileObject(
				Instant.now(),
				new String[0],
				new KagerowDataType[0],
				new long[0],
				"datAddr",
				BigInteger.ZERO,
				"idxAddr",
				BigInteger.ZERO,
				"binaryName",
				"synonym",
				"test",
				new AtomicReference<>());
		doReturn(fileObject).when(content).get(anyInt());
		doReturn(context).when(baseContext).lookup((String) any());

		// テストデータ準備
		testTarget = new KsqlDatabaseLinker("test", baseContext);
		String sql = """
				SELECT COL AS "${target[1]}" FROM ${target[1]} LEFT OUTER JOIN ${target[1]} USING(COL)
					WHERE COL_NM = 'ID_''${target[1]}_001';
				""";

		// 期待値
		String exp = """
				SELECT COL AS "test."KDB_BINARYNAME"" FROM test."KDB_BINARYNAME" LEFT OUTER JOIN test."KDB_BINARYNAME" USING(COL)
					WHERE COL_NM = 'ID_''${target[1]}_001';
				""";

		// テスト実施
		String result = testTarget.transform(sql);

		// 結果検証
		assertThat(result, is(exp));

	}

	/**
	 * [試験観点] : 置対象あり
	 * [期待される結果] : 以下である
	 * ・正常終了すること
	 * ・対象が置換されていること(複数)
	 * ・エスケープあり
	 * ・シングルクオートあり
	 */
	@Test
	public void Test006() throws Throwable {

		// モック設定
		Map<String, String> contextMap = new HashMap<>();
		contextMap.put("target", "test");

		doReturn(contextMap).when(context).getSynonymMapList();
		doReturn(content).when(context).lookup(anyString());
		BasicFileObject fileObject1 = new BasicFileObject(
				Instant.now(),
				new String[0],
				new KagerowDataType[0],
				new long[0],
				"datAddr",
				BigInteger.ZERO,
				"idxAddr",
				BigInteger.ZERO,
				"binaryName1",
				"synonym",
				"test",
				new AtomicReference<>());
		BasicFileObject fileObject2 = new BasicFileObject(
				Instant.now(),
				new String[0],
				new KagerowDataType[0],
				new long[0],
				"datAddr",
				BigInteger.ZERO,
				"idxAddr",
				BigInteger.ZERO,
				"binaryName2",
				"synonym",
				"test2",
				new AtomicReference<>());
		doReturn(fileObject1, fileObject2).when(content).get(anyInt());
		doReturn(context).when(baseContext).lookup((String) any());

		// テストデータ準備
		testTarget = new KsqlDatabaseLinker("test", baseContext);
		String sql = """
				SELECT COL FROM ${target[1]} LEFT OUTER JOIN ${test2.target[2]} USING(COL)
					WHERE COL_NM = 'ID_''${target[1]}_001';
				""";

		// 期待値
		String exp = """
				SELECT COL FROM test."KDB_BINARYNAME1" LEFT OUTER JOIN test2."KDB_BINARYNAME2" USING(COL)
					WHERE COL_NM = 'ID_''${target[1]}_001';
				""";

		// テスト実施
		String result = testTarget.transform(sql);

		// 結果検証
		assertThat(result, is(exp));

	}

	/**
	 * [試験観点] : 置対象あり、範囲指定含む
	 * [期待される結果] : 以下である
	 * ・正常終了すること
	 * ・対象が置換されていること(単一複数箇所)
	 * ・エスケープなし
	 * ・シングルクオートあり
	 */
	@Test
	public void Test007() throws Throwable {

		// モック設定
		Map<String, String> contextMap = new HashMap<>();
		contextMap.put("target", "test");
		doReturn(contextMap).when(context).getSynonymMapList();
		doReturn(content).when(context).lookup(anyString());
		BasicFileObject fileObject = new BasicFileObject(
				Instant.now(),
				new String[0],
				new KagerowDataType[0],
				new long[0],
				"datAddr",
				BigInteger.ZERO,
				"idxAddr",
				BigInteger.ZERO,
				"binaryName",
				"synonym",
				"test",
				new AtomicReference<>());
		doReturn(fileObject).when(content).get(anyInt());
		doReturn(context).when(baseContext).lookup((String) any());

		// テストデータ準備
		testTarget = new KsqlDatabaseLinker("test", baseContext);
		String sql = """
				SELECT COL AS "${target[0..1]}" FROM ${test.target[1]} LEFT OUTER JOIN ${target[0..1]} USING(COL)
					WHERE COL_NM = '${target[0..1]}_001';
				""";

		// 期待値
		String exp = """
				SELECT COL AS "test.KV_target_0_1" FROM test."KDB_BINARYNAME" LEFT OUTER JOIN test.KV_target_0_1 USING(COL)
					WHERE COL_NM = '${target[0..1]}_001';
				""";

		// テスト実施
		String result = testTarget.transform(sql);

		// 結果検証
		assertThat(result, is(exp));

	}

}

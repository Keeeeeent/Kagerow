package com.sakulabo.core.Processor.archive;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sakulabo.BaseTest;
import com.sakulabo.core.Common.AppPathUtils;
import com.sakulabo.core.Kagerow.KagerowApplication;
import com.sakulabo.core.Kagerow.Contents.KagerowVirtualFileContent.KagerowVirtualFileObject.BasicFileObject;
import com.sakulabo.core.Kagerow.Utilities.KagerowDBMode;
import com.sakulabo.core.Processor.database.DDLConnectionHandler;

/**
 * バイナリファイル読み取り実装提供クラスのテストクラスです
 */
public class BasicChunkLorderTest extends BaseTest<BasicChunkLorder> {

	/**
	 * デフォルトコンストラクタ
	 */
	protected BasicChunkLorderTest() {
		super(BasicChunkLorderTest.class);
	}

	@BeforeEach
	void initService() {
		// セキュアコンテキスト生成
		KagerowApplication.getInstance("test");
	}

	@AfterEach
	void closeService() throws Exception {
	}

	/** テスト対象 */
	private BasicChunkLorder testTarget;

	/**
	 * [試験観点] : チャンク読み取り
	 * [期待される結果] : 正常終了すること
	 */
	@Test
	public void Test001() throws Throwable {

		try {
			// インスタンス初期化
			Path path = testDir.resolve("test1.csv");
			BasicChunkCreater creater = new BasicChunkCreater("test", path, StandardCharsets.UTF_8, true,
					new CSVFileReaderFactory());
			BasicFileObject result = creater.create("Test001");

			// KDB出力先初期化
			Path kdbPath = Files.createTempFile(AppPathUtils.createTemporaryDirPath(),
					AppPathUtils.KDB_FILE_PREFIX,
					AppPathUtils.KDB_FILE_EXT);

			// KDB初期化処理実行
			DDLConnectionHandler.initDDLConnectionHandler(kdbPath, KagerowDBMode.ORACLE, Set.of(result));

			testTarget = new BasicChunkLorder(KagerowDBMode.ORACLE, kdbPath);
			testTarget.lord(result);

		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}

	}

	/**
	 * [試験観点] : チャンク読み取り,2コア
	 * [期待される結果] : 正常終了すること
	 */
	@Test
	public void Test002() throws Throwable {

		try {
			// インスタンス初期化
			Path path = testDir.resolve("test1.csv");
			BasicChunkCreater creater = new BasicChunkCreater("test", path, StandardCharsets.UTF_8, true,
					new CSVFileReaderFactory());
			BasicFileObject result = creater.create("Test001");

			// KDB出力先初期化
			Path kdbPath = Files.createTempFile(AppPathUtils.createTemporaryDirPath(),
					AppPathUtils.KDB_FILE_PREFIX,
					AppPathUtils.KDB_FILE_EXT);

			// KDB初期化処理実行
			DDLConnectionHandler.initDDLConnectionHandler(kdbPath, KagerowDBMode.ORACLE, Set.of(result));

			// モック設定
			testTarget = new BasicChunkLorder(KagerowDBMode.ORACLE, kdbPath);
			testTarget = spy(testTarget);
			doReturn(2).when(testTarget).canSeparate(any());

			// テスト実施
			testTarget.lord(result);

		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}

	}

	/**
	 * [試験観点] : チャンク読み取り,4コア
	 * [期待される結果] : 正常終了すること
	 */
	@Test
	public void Test003() throws Throwable {

		try {
			// インスタンス初期化
			Path path = testDir.resolve("test1.csv");
			BasicChunkCreater creater = new BasicChunkCreater("test", path, StandardCharsets.UTF_8, true,
					new CSVFileReaderFactory());
			BasicFileObject result = creater.create("Test001");

			// KDB出力先初期化
			Path kdbPath = Files.createTempFile(AppPathUtils.createTemporaryDirPath(),
					AppPathUtils.KDB_FILE_PREFIX,
					AppPathUtils.KDB_FILE_EXT);

			// KDB初期化処理実行
			DDLConnectionHandler.initDDLConnectionHandler(kdbPath, KagerowDBMode.ORACLE, Set.of(result));

			// モック設定
			testTarget = new BasicChunkLorder(KagerowDBMode.ORACLE, kdbPath);
			testTarget = spy(testTarget);
			doReturn(4).when(testTarget).canSeparate(any());

			// テスト実施
			testTarget.lord(result);

		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}

	}

	/**
	 * [試験観点] : チャンク読み取り,8コア
	 * [期待される結果] : 正常終了すること
	 */
	@Test
	public void Test004() throws Throwable {

		try {
			// インスタンス初期化
			Path path = testDir.resolve("test1.csv");
			BasicChunkCreater creater = new BasicChunkCreater("test", path, StandardCharsets.UTF_8, true,
					new CSVFileReaderFactory());
			BasicFileObject result = creater.create("Test001");

			// KDB出力先初期化
			Path kdbPath = Files.createTempFile(AppPathUtils.createTemporaryDirPath(),
					AppPathUtils.KDB_FILE_PREFIX,
					AppPathUtils.KDB_FILE_EXT);

			// KDB初期化処理実行
			DDLConnectionHandler.initDDLConnectionHandler(kdbPath, KagerowDBMode.ORACLE, Set.of(result));

			// モック設定
			testTarget = new BasicChunkLorder(KagerowDBMode.ORACLE, kdbPath);
			testTarget = spy(testTarget);
			doReturn(8).when(testTarget).canSeparate(any());

			// テスト実施
			testTarget.lord(result);

		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}

	}

	/**
	 * [試験観点] : チャンク読み取り,16コア
	 * [期待される結果] : 正常終了すること
	 */
	@Test
	public void Test005() throws Throwable {

		try {
			// インスタンス初期化
			Path path = testDir.resolve("test1.csv");
			BasicChunkCreater creater = new BasicChunkCreater("test", path, StandardCharsets.UTF_8, true,
					new CSVFileReaderFactory());
			BasicFileObject result = creater.create("Test001");

			// KDB出力先初期化
			Path kdbPath = Files.createTempFile(AppPathUtils.createTemporaryDirPath(),
					AppPathUtils.KDB_FILE_PREFIX,
					AppPathUtils.KDB_FILE_EXT);

			// KDB初期化処理実行
			DDLConnectionHandler.initDDLConnectionHandler(kdbPath, KagerowDBMode.ORACLE, Set.of(result));

			// モック設定
			testTarget = new BasicChunkLorder(KagerowDBMode.ORACLE, kdbPath);
			testTarget = spy(testTarget);
			doReturn(16).when(testTarget).canSeparate(any());

			// テスト実施
			testTarget.lord(result);

		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}

	}

	/**
	 * [試験観点] : チャンク読み取り,全てのパターンのデータ
	 * [期待される結果] : 正常終了すること
	 */
	@Test
	public void Test006() throws Throwable {

		try {
			// インスタンス初期化
			Path path = testDir.resolve("test6.csv");
			BasicChunkCreater creater = new BasicChunkCreater("test", path, StandardCharsets.UTF_8, true,
					new CSVFileReaderFactory());
			BasicFileObject result = creater.create("Test001");

			// KDB出力先初期化
			Path kdbPath = Files.createTempFile(AppPathUtils.createTemporaryDirPath(),
					AppPathUtils.KDB_FILE_PREFIX,
					AppPathUtils.KDB_FILE_EXT);

			// KDB初期化処理実行
			DDLConnectionHandler.initDDLConnectionHandler(kdbPath, KagerowDBMode.ORACLE, Set.of(result));

			// モック設定
			testTarget = new BasicChunkLorder(KagerowDBMode.ORACLE, kdbPath);

			// テスト実施
			testTarget.lord(result);

		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}

	}

}

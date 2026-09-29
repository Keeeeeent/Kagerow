package com.sakulabo.core.Processor.archive;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.naming.CompositeName;
import javax.naming.Name;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sakulabo.BaseTest;
import com.sakulabo.BaseTest.KagerowContainerRunner.KagerowSecureContainerRunner;
import com.sakulabo.BaseTest.KagerowSchemaCreateRunner;
import com.sakulabo.BaseTest.KagerowSchemaCreateRunner.NeedsKagerowSchema;
import com.sakulabo.core.Common.ErrorMessage;
import com.sakulabo.core.Kagerow.Contents.KagerowVirtualFileContent.KagerowVirtualFileObject.BasicFileObject;
import com.sakulabo.core.Kagerow.Exception.AppLogicException;

/**
 * バイナリファイル実装提供クラスのテストクラスです
 */
@ExtendWith(KagerowSecureContainerRunner.class)
@ExtendWith(KagerowSchemaCreateRunner.class)
public class BasicChunkCreaterTest extends BaseTest {

	@Nested
	@ExtendWith(MockitoExtension.class)
	public class BasicChunkCreaterTest_createTest extends BaseTest {

		@Mock
		public BasicChunkWriter chunkWriter;

		/**
		 * [試験観点] : チャンク生成
		 * [期待される結果] : 正常終了すること、ファイルが生成されること
		 */
		@Test
		@NeedsKagerowSchema("test")
		public void Test001() throws Throwable {

			// インスタンス初期化
			Path path = getTestDir().resolve("test1.csv");
			BasicChunkCreater testTarget = new BasicChunkCreater("test", path, StandardCharsets.UTF_8, true,
					new CSVFileReaderFactory());
			BasicFileObject result = testTarget.create("Test001");

			// シノニムの検証
			assertThat(result.synonym(), is("Test001"));

			// スキーマの検証
			assertThat(result.schema(), is("test"));

			// バイナリ名称の検証
			assertThat(result.binaryName(), is(not(nullValue())));

			// ファイルの検証(dat)
			path = Paths.get(URI.create(result.datAddr()));
			assertTrue(Files.exists(path));
			assertThat(Files.size(path), is(result.datSize().longValue()));

			// ファイルの検証(idx)
			path = Paths.get(URI.create(result.idxAddr()));
			assertTrue(Files.exists(path));
			assertThat(Files.size(path), is(result.idxSize().longValue()));

			// ヘッダーデータの検証
			String[] header = {
					"col1", "col2", "col3", "col4", "col5", "col6"
			};
			for (int i = 0; i < header.length; i++)
				assertThat(result.headerData()[i], is(header[i]));

		}

		/**
		 * [試験観点] : 不正パス
		 * [期待される結果] : 例外が発生すること、エラーメッセージが期待通りである
		 */
		@Test
		@NeedsKagerowSchema("test")
		public void Test002() throws Throwable {

			Path path = getTestDir().resolve("test2.csv");

			try {
				// インスタンス初期化
				BasicChunkCreater _ = new BasicChunkCreater("test", path, StandardCharsets.UTF_8, true,
						new CSVFileReaderFactory());
				fail();
			} catch (AppLogicException e) {
				assertThat(e.getMessage(), is(ErrorMessage.CODE_005.getMessage(path)));
			}

		}

		/**
		 * [試験観点] : チャンクデータ作成中にIOException発生
		 * [期待される結果] : 正常終了すること、ファイルが生成されること
		 */
		@Test
		@NeedsKagerowSchema("test")
		public void Test003() throws Throwable {

			// インスタンス初期化
			Path path = getTestDir().resolve("test1.csv");
			BasicChunkCreater testTarget = new BasicChunkCreater("test", path, StandardCharsets.UTF_8, true,
					new CSVFileReaderFactory());

			// モック設定
			doThrow(IOException.class).when(chunkWriter).write(any());
			BasicChunkCreater spy = spy(testTarget);
			doReturn(chunkWriter).when(spy).createChunkWriter(any(), any(), any());

			// テスト実行
			IOException _ = assertThrows(IOException.class, () -> {
				spy.create("Test001");
			});

			verify(chunkWriter, times(1)).rollback();

		}

	}

	@Nested
	public class BasicChunkCreaterTest_toTableNameTest extends BaseTest {

		public MethodHandle toTableName;

		public BasicChunkCreaterTest_toTableNameTest() throws Exception {
			MethodHandles.Lookup lookup = MethodHandles.lookup();
			MethodType param = MethodType.methodType(String.class, Name.class);
			toTableName = MethodHandles.privateLookupIn(AppChunkCreater.class, lookup)
					.findVirtual(AppChunkCreater.class, "toTableName", param);
		}

		/**
		 * [試験観点] : 単一テーブル名称生成
		 * [期待される結果] : 期待通りのテーブル名称が生成されること
		 */
		@Test
		public void Test001() throws Throwable {

			// インスタンス初期化
			Path path = getTestDir().resolve("test1.csv");
			BasicChunkCreater testTarget = new BasicChunkCreater("test", path, StandardCharsets.UTF_8, true,
					new CSVFileReaderFactory());

			// テスト実行
			Name name = new CompositeName("Test001");
			String result = (String) toTableName.invoke(testTarget, name);

			// 結果検証
			assertThat(result, is("Test001"));
		}

		/**
		 * [試験観点] : 複合テーブル名称生成
		 * [期待される結果] : 期待通りのテーブル名称が生成されること
		 */
		@Test
		public void Test002() throws Throwable {

			// インスタンス初期化
			Path path = getTestDir().resolve("test1.csv");
			BasicChunkCreater testTarget = new BasicChunkCreater("test", path, StandardCharsets.UTF_8, true,
					new CSVFileReaderFactory());

			// テスト実行
			Name name = new CompositeName("Test002/test");
			String result = (String) toTableName.invoke(testTarget, name);

			// 結果検証
			assertThat(result, is("Test002#test"));
		}

	}

}

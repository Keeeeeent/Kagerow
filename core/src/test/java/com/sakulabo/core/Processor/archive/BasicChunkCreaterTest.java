package com.sakulabo.core.Processor.archive;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.sakulabo.BaseTest;
import com.sakulabo.BaseTest.KagerowContainerRunner;
import com.sakulabo.core.Common.ErrorMessage;
import com.sakulabo.core.Kagerow.Contents.KagerowVirtualFileContent.KagerowVirtualFileObject.BasicFileObject;
import com.sakulabo.core.Kagerow.Context.KagerowVirtualFileContext;
import com.sakulabo.core.Kagerow.Exception.AppLogicException;
import com.sakulabo.core.Kagerow.Utilities.KagerowUtilities;

/**
 * バイナリファイル実装提供クラスのテストクラスです
 */
@ExtendWith(KagerowContainerRunner.class)
public class BasicChunkCreaterTest extends BaseTest<BasicChunkCreater> {

	@BeforeAll
	public static void initService() throws Exception {
		// スキーマ作成
		KagerowVirtualFileContext ctx = KagerowUtilities.getContext(KagerowVirtualFileContext._NAME);
		ctx.createSubcontext("test");
	}

	/** テスト対象 */
	private BasicChunkCreater testTarget;

	/**
	 * [試験観点] : チャンク生成
	 * [期待される結果] : 正常終了すること、ファイルが生成されること
	 */
	@Test
	public void Test001() throws Throwable {

		// インスタンス初期化
		Path path = getTestDir().resolve("test1.csv");
		testTarget = new BasicChunkCreater("test", path, StandardCharsets.UTF_8, true, new CSVFileReaderFactory());
		BasicFileObject result = testTarget.create("Test001");

		// シノニムの検証
		assertThat(result.synonym(), is("Test001"));

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
	public void Test002() throws Throwable {

		Path path = getTestDir().resolve("test2.csv");

		try {
			// インスタンス初期化
			testTarget = new BasicChunkCreater("test", path, StandardCharsets.UTF_8, true, new CSVFileReaderFactory());
			fail();
		} catch (AppLogicException e) {
			assertThat(e.getMessage(), is(ErrorMessage.CODE_005.getMessage(path)));
		}

	}

}

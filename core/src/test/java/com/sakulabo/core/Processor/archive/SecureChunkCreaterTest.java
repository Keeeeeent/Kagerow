package com.sakulabo.core.Processor.archive;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
import com.sakulabo.core.Kagerow.Contents.KagerowVirtualFileContent.KagerowVirtualFileObject.SecureFileObject;
import com.sakulabo.core.Kagerow.Exception.AppLogicException;

@ExtendWith(KagerowSecureContainerRunner.class)
@ExtendWith(KagerowSchemaCreateRunner.class)
public class SecureChunkCreaterTest extends BaseTest {

    @Nested
    @ExtendWith(MockitoExtension.class)
    public class SecureChunkCreaterTest_createTest extends BaseTest {

        @Mock
        public BasicChunkWriter chunkWriter;

        /**
         * [試験観点] : チャンク生成、セキュリティコンテンツあり
         * [期待される結果] : 正常終了すること、ファイルが生成されること
         */
        @Test
        @NeedsKagerowSchema("test")
        public void Test001() throws Throwable {

            // インスタンス初期化
            Path path = getTestDir().resolve("test1.csv");
            SecureChunkCreater testTarget = new SecureChunkCreater("test", path, StandardCharsets.UTF_8, true,
                    new CSVFileReaderFactory());
            SecureFileObject result = testTarget.create("Test001");

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

            // 秘密鍵の検証
            assertThat(result.alias(), is("NXsCIWbHY5Fbuc4a8TimewYcyA8yKLoLRrTy0jNb8y0="));

            // パスワードの検証
            assertThat(result.password().length(), is(60));

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
                SecureChunkCreater _ = new SecureChunkCreater("test", path, StandardCharsets.UTF_8, true,
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
            SecureChunkCreater testTarget = new SecureChunkCreater("test", path, StandardCharsets.UTF_8, true,
                    new CSVFileReaderFactory());

            // モック設定
            doThrow(IOException.class).when(chunkWriter).write(any());
            SecureChunkCreater spy = spy(testTarget);
            doReturn(chunkWriter).when(spy).createChunkWriter(any(), any(), any());

            // テスト実行
            IOException _ = assertThrows(IOException.class, () -> {
                spy.create("Test001");
            });

            verify(chunkWriter, times(1)).rollback();

        }

    }

}

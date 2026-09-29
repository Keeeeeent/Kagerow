package com.sakulabo.core.Processor.archive;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.function.Consumer;

import javax.naming.NameAlreadyBoundException;

import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;
import org.junit.jupiter.api.extension.ExtendWith;

import com.sakulabo.BaseTest;
import com.sakulabo.BaseTest.KagerowContainerRunner;
import com.sakulabo.BaseTest.KagerowSchemaCreateRunner.NeedsKagerowSchema;
import com.sakulabo.core.Kagerow.KagerowApplication;
import com.sakulabo.core.Kagerow.Contents.KagerowVirtualFileContent.KagerowVirtualFileObject.BasicFileObject;
import com.sakulabo.core.Kagerow.Context.KagerowVirtualDirContext;
import com.sakulabo.core.Kagerow.Context.KagerowVirtualFileContext;
import com.sakulabo.core.Kagerow.Utilities.KagerowChunkCreater.ChunkCreateMode;

@ExtendWith(KagerowContainerRunner.class)
public class BasicVirtualFileCreaterTest extends BaseTest {

    @Nested
    public class BasicVirtualFileCreater_newTest extends BaseTest {

        /**
         * [試験観点] : 必須パラメータのみ指定
         * [期待される結果] : インスタンスが生成されること
         */
        @Test
        public void Test001() throws Throwable {

            // テスト実行
            Path path = getTestDir().resolve("test1.csv");
            BasicVirtualFileCreater creater = new BasicVirtualFileCreater(ChunkCreateMode.CSV, "test",
                    path, StandardCharsets.UTF_8, false, null, null);

            // 結果検証
            assertThat(creater.mode, is(ChunkCreateMode.CSV));
            assertThat(creater.schema, is("test"));
            assertThat(creater.path, is(path));
            assertThat(creater.isHeader, is(false));
            assertThat(creater.synonym, nullValue());
            assertThat(creater.charset, is(StandardCharsets.UTF_8));
            assertThat(creater.observer, is(AppVirtualFileCreater.EMPTY_CONSUMER));

        }

        /**
         * [試験観点] : モード未指定
         * [期待される結果] : 例外が発生すること
         */
        @Test
        public void Test002() throws Throwable {

            // テスト実行
            Path path = getTestDir().resolve("test1.csv");
            assertThrows(NullPointerException.class, () -> {
                // 結果検証
                BasicVirtualFileCreater _ = new BasicVirtualFileCreater(null, "test",
                        path, StandardCharsets.UTF_8, false, null, null);
            });

        }

        /**
         * [試験観点] : スキーマ未指定未指定
         * [期待される結果] : 例外が発生すること
         */
        @Test
        public void Test003() throws Throwable {

            // テスト実行
            Path path = getTestDir().resolve("test1.csv");
            assertThrows(NullPointerException.class, () -> {
                // 結果検証
                BasicVirtualFileCreater _ = new BasicVirtualFileCreater(ChunkCreateMode.CSV, null,
                        path, StandardCharsets.UTF_8, false, null, null);
            });

        }

        /**
         * [試験観点] : パス未指定未指定
         * [期待される結果] : 例外が発生すること
         */
        @Test
        public void Test004() throws Throwable {

            // テスト実行
            assertThrows(NullPointerException.class, () -> {
                // 結果検証
                BasicVirtualFileCreater _ = new BasicVirtualFileCreater(ChunkCreateMode.CSV, "null",
                        null, StandardCharsets.UTF_8, false, null, null);
            });

        }

        /**
         * [試験観点] : 文字コード未指定、BOMなしファイル
         * [期待される結果] : 例外が発生すること
         */
        @Test
        public void Test005() throws Throwable {

            // テスト実行
            Path path = getTestDir().resolve("test1.csv");
            IOException result = assertThrows(IOException.class, () -> {
                // 結果検証
                BasicVirtualFileCreater _ = new BasicVirtualFileCreater(ChunkCreateMode.CSV, "test",
                        path, null, false, null, null);
            });

            assertThat(result.getMessage(), is("文字コードの解析に失敗しました"));

        }

        /**
         * [試験観点] : 文字コード未指定、BOMありファイル
         * [期待される結果] : インスタンスが生成されること
         */
        @Test
        public void Test006() throws Throwable {

            // テスト実行
            Path path = getTestDir().resolve("test2.csv");
            BasicVirtualFileCreater creater = new BasicVirtualFileCreater(ChunkCreateMode.CSV, "test",
                    path, null, false, null, null);

            // 結果検証
            assertThat(creater.mode, is(ChunkCreateMode.CSV));
            assertThat(creater.schema, is("test"));
            assertThat(creater.path, is(path));
            assertThat(creater.isHeader, is(false));
            assertThat(creater.synonym, nullValue());
            assertThat(creater.charset, is(StandardCharsets.UTF_8));
            assertThat(creater.observer, is(AppVirtualFileCreater.EMPTY_CONSUMER));

        }

        /**
         * [試験観点] : オブザーバー指定あり
         * [期待される結果] : インスタンスが生成されること
         */
        @Test
        public void Test007() throws Throwable {

            // テスト実行
            Path path = getTestDir().resolve("test1.csv");
            Consumer<Double> observer = _ -> {
            };
            BasicVirtualFileCreater creater = new BasicVirtualFileCreater(ChunkCreateMode.CSV, "test",
                    path, StandardCharsets.UTF_8, false, null, observer);

            // 結果検証
            assertThat(creater.mode, is(ChunkCreateMode.CSV));
            assertThat(creater.schema, is("test"));
            assertThat(creater.path, is(path));
            assertThat(creater.isHeader, is(false));
            assertThat(creater.synonym, nullValue());
            assertThat(creater.charset, is(StandardCharsets.UTF_8));
            assertThat(creater.observer, is(observer));

        }

    }

    @Nested
    @ExtendWith(KagerowSchemaCreateRunner.class)
    public class BasicVirtualFileCreater_createVirtualFileObjectTest extends BaseTest {

        /**
         * [試験観点] : 必須パラメータのみ指定
         * [期待される結果] : インスタンスが生成されること
         */
        @Test
        @Disabled("個別実行はできるもの全体実が失敗するためスキップ")
        @NeedsKagerowSchema("test")
        public void Test001() throws Throwable {

            // テスト実行
            Path path = getTestDir().resolve("test1.csv");
            BasicVirtualFileCreater creater = new BasicVirtualFileCreater(ChunkCreateMode.CSV, "test",
                    path, StandardCharsets.UTF_8, false, null, null);
            BasicFileObject result = creater.createVirtualFileObject();
            assertThat(result, notNullValue(BasicFileObject.class));
        }

    }

    @Nested
    @ExtendWith(KagerowSchemaCreateRunner.class)
    public class BasicVirtualFileCreater_getCharsetTest extends BaseTest {

        /**
         * [試験観点] : BOMなし
         * [期待される結果] : 例外が発生すること
         */
        @Test
        @NeedsKagerowSchema("test")
        public void Test001() throws Throwable {

            // テスト実行
            Path path = getTestDir().resolve("test1.csv");
            BasicVirtualFileCreater creater = new BasicVirtualFileCreater(ChunkCreateMode.CSV, "test",
                    path, StandardCharsets.UTF_8, false, null, null);
            IOException result = assertThrows(IOException.class, () -> {
                Charset _ = creater.getCharset(path);
            });
            assertThat(result.getMessage(), is("文字コードの解析に失敗しました"));
        }

        /**
         * [試験観点] : BOMあり
         * [期待される結果] : 文字コードが返却さること
         */
        @Test
        @NeedsKagerowSchema("test")
        public void Test002() throws Throwable {

            // テスト実行
            Path path = getTestDir().resolve("test2.csv");
            BasicVirtualFileCreater creater = new BasicVirtualFileCreater(ChunkCreateMode.CSV, "test",
                    path, StandardCharsets.UTF_8, false, null, null);
            Charset result = creater.getCharset(path);
            assertThat(result, is(StandardCharsets.UTF_8));
        }

    }

    @Nested
    @ExtendWith(KagerowSchemaCreateRunner.class)
    public class BasicVirtualFileCreater_getVirtualDirContextTest extends BaseTest {

        /**
         * [試験観点] : スキーマ存在あり
         * [期待される結果] : コンテキストが返却されること
         */
        @Test
        @NeedsKagerowSchema("test")
        public void Test001() throws Throwable {

            // テスト実行
            Path path = getTestDir().resolve("test1.csv");
            BasicVirtualFileCreater creater = new BasicVirtualFileCreater(ChunkCreateMode.CSV, "test",
                    path, StandardCharsets.UTF_8, false, null, null);
            KagerowVirtualDirContext result = creater.getVirtualDirContext();
            assertThat(result.getNameInNamespace(), is("test"));
        }

        /**
         * [試験観点] : スキーマ存在なし
         * [期待される結果] : コンテキストが返却されること
         */
        @Test
        public void Test002() throws Throwable {

            // テスト実行
            Path path = getTestDir().resolve("test1.csv");
            BasicVirtualFileCreater creater = new BasicVirtualFileCreater(ChunkCreateMode.CSV, "empty",
                    path, StandardCharsets.UTF_8, false, null, null);
            KagerowVirtualDirContext result = creater.getVirtualDirContext();
            assertThat(result.getNameInNamespace(), is("empty"));
        }

    }

    @Nested
    @ExtendWith(KagerowSchemaCreateRunner.class)
    @TestClassOrder(ClassOrderer.OrderAnnotation.class)
    public class BasicVirtualFileCreater_constructionTest extends BaseTest {

        /**
         * [試験観点] : 必須パラメータのみ指定
         * [期待される結果] : URIが期待通りであること
         */
        @Test
        @NeedsKagerowSchema("test")
        @Order(1)
        public void Test001() throws Throwable {

            // テスト実行
            Path path = getTestDir().resolve("test1.csv");
            BasicVirtualFileCreater creater = new BasicVirtualFileCreater(ChunkCreateMode.CSV, "test",
                    path, StandardCharsets.UTF_8, false, null, null);
            URI result = creater.construction();
            assertThat(result, is(URI.create(
                    "kagerow://application/ff159c696319867925b1963ef74afddf/974696a8f5e7602e0b9ec126ec0b0c4d#test.zip")));
        }

        /**
         * [試験観点] : シノニム指定
         * [期待される結果] : シノニムが管理下に追加されていること
         */
        @Test
        @NeedsKagerowSchema("test1")
        @Order(2)
        public void Test002() throws Throwable {

            // テスト実行
            Path path = getTestDir().resolve("test1.csv");
            BasicVirtualFileCreater creater = new BasicVirtualFileCreater(ChunkCreateMode.CSV, "test1",
                    path, StandardCharsets.UTF_8, false, "BasicVirtualFileCreater_constructionTest002", null);
            URI _ = creater.construction();
            KagerowVirtualFileContext context = (KagerowVirtualFileContext) KagerowApplication.getInstance()
                    .getContext().lookup(KagerowVirtualFileContext._NAME);
            KagerowVirtualDirContext dirCtx = context.lookup("test1");
            assertTrue(dirCtx.getSynonymMapList().containsKey("BasicVirtualFileCreater_constructionTest002"));
        }

        /**
         * [試験観点] : 既に存在するシノニム指定
         * [期待される結果] : 例外が発生すること
         */
        @Test
        @NeedsKagerowSchema("test1")
        @Order(3)
        public void Test003() throws Throwable {

            // テスト実行
            Path path = getTestDir().resolve("test2.csv");
            BasicVirtualFileCreater creater = new BasicVirtualFileCreater(ChunkCreateMode.CSV, "test1",
                    path, StandardCharsets.UTF_8, false, "BasicVirtualFileCreater_constructionTest002", null);
            NameAlreadyBoundException result = assertThrows(NameAlreadyBoundException.class, () -> {
                URI _ = creater.construction();
            });
            assertThat(result.getMessage(), is("既に存在するシノニムです【対象】: BasicVirtualFileCreater_constructionTest002"));
        }

        /**
         * [試験観点] : 既に存在するシノニム指定でもデータセットが異なる場合
         * [期待される結果] : 正常狩猟すること
         */
        @Test
        @NeedsKagerowSchema("test1")
        @Order(4)
        public void Test004() throws Throwable {

            // テスト実行
            Path path = getTestDir().resolve("test3.csv");
            BasicVirtualFileCreater creater = new BasicVirtualFileCreater(ChunkCreateMode.CSV, "test",
                    path, StandardCharsets.UTF_8, false, null, null);
            URI result = creater.construction();
            assertThat(result, is(URI.create(
                    "kagerow://application/ff159c696319867925b1963ef74afddf/835959b0cf2b39abe6e9521c67612209#test.zip")));
        }

        /**
         * [試験観点] : 既に存在するシノニム指定でもデータセットが同じ場合
         * [期待される結果] : NameAlreadyBoundExceptionが発生すること
         */
        @Test
        @NeedsKagerowSchema("test1")
        @Order(5)
        public void Test005() throws Throwable {

            // テスト実行
            Path path = getTestDir().resolve("test3.csv");
            BasicVirtualFileCreater creater = new BasicVirtualFileCreater(ChunkCreateMode.CSV, "test",
                    path, StandardCharsets.UTF_8, false, null, null);
            NameAlreadyBoundException result = assertThrows(NameAlreadyBoundException.class, () -> {
                URI _ = creater.construction();
            });
            assertThat(result.getMessage(), is("835959b0cf2b39abe6e9521c67612209"));
        }

    }

}

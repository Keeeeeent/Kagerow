package com.sakulabo.core.Processor.archive;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.sakulabo.BaseTest;
import com.sakulabo.BaseTest.KagerowContainerRunner;

@ExtendWith(KagerowContainerRunner.class)
public class BomHandlerTest extends BaseTest {

    @Nested
    public class BomHandlerTest_getCharsetTest extends BaseTest {

        /**
         * [試験観点] : BOMなし,UTF-8
         * [期待される結果] : 結果がnullであること
         */
        @Test
        public void Test001() throws Throwable {
            // 引数準備
            Path path = getTestDir().resolve("test1.csv");
            // テスト実行
            BomHandler bomHandler = new BomHandler(path);
            Charset result = bomHandler.getCharset();
            // 結果検証
            assertThat(result, nullValue());
        }

        /**
         * [試験観点] : BOMあり,UTF-8
         * [期待される結果] : 結果がUTF-8であること
         */
        @Test
        public void Test002() throws Throwable {
            // 引数準備
            Path path = getTestDir().resolve("test2.csv");
            // テスト実行
            BomHandler bomHandler = new BomHandler(path);
            Charset result = bomHandler.getCharset();
            // 結果検証
            assertThat(result, is(StandardCharsets.UTF_8));
        }

        /**
         * [試験観点] : BOMあり,UTF-16BE
         * [期待される結果] : 結果がUTF-16BEであること
         */
        @Test
        public void Test003() throws Throwable {
            // 引数準備
            Path path = getTestDir().resolve("test3.csv");
            // テスト実行
            BomHandler bomHandler = new BomHandler(path);
            Charset result = bomHandler.getCharset();
            // 結果検証
            assertThat(result, is(StandardCharsets.UTF_16BE));
        }

        /**
         * [試験観点] : BOMあり,UTF-16LE
         * [期待される結果] : 結果がUTF-16LEであること
         */
        @Test
        public void Test004() throws Throwable {
            // 引数準備
            Path path = getTestDir().resolve("test4.csv");
            // テスト実行
            BomHandler bomHandler = new BomHandler(path);
            Charset result = bomHandler.getCharset();
            // 結果検証
            assertThat(result, is(StandardCharsets.UTF_16LE));
        }

        /**
         * [試験観点] : BOMあり,UTF-32BE
         * [期待される結果] : 結果がUTF-32BEであること
         */
        @Test
        public void Test005() throws Throwable {
            // 引数準備
            Path path = getTestDir().resolve("test5.csv");
            // テスト実行
            BomHandler bomHandler = new BomHandler(path);
            Charset result = bomHandler.getCharset();
            // 結果検証
            assertThat(result, is(StandardCharsets.UTF_32BE));
        }

        /**
         * [試験観点] : BOMあり,UTF-32LE
         * [期待される結果] : 結果がUTF-32LEであること
         */
        @Test
        public void Test006() throws Throwable {
            // 引数準備
            Path path = getTestDir().resolve("test6.csv");
            // テスト実行
            BomHandler bomHandler = new BomHandler(path);
            Charset result = bomHandler.getCharset();
            // 結果検証
            assertThat(result, is(StandardCharsets.UTF_32LE));
        }

        /**
         * [試験観点] : 不正なBOM,printf '\x00\x00\xFE\x00' > test7.csv
         * [期待される結果] : 結果がnullであること
         */
        @Test
        public void Test007() throws Throwable {
            // 引数準備
            Path path = getTestDir().resolve("test7.csv");
            // テスト実行
            BomHandler bomHandler = new BomHandler(path);
            Charset result = bomHandler.getCharset();
            // 結果検証
            assertThat(result, nullValue());
        }

        /**
         * [試験観点] : 不正なBOM,printf '\xFF\xBB' > test8.csv
         * [期待される結果] : 結果がnullであること
         */
        @Test
        public void Test008() throws Throwable {
            // 引数準備
            Path path = getTestDir().resolve("test8.csv");
            // テスト実行
            BomHandler bomHandler = new BomHandler(path);
            Charset result = bomHandler.getCharset();
            // 結果検証
            assertThat(result, nullValue());
        }

        /**
         * [試験観点] : 不正なBOM,printf '\xFE\xBB' > test9.csv
         * [期待される結果] : 結果がnullであること
         */
        @Test
        public void Test009() throws Throwable {
            // 引数準備
            Path path = getTestDir().resolve("test9.csv");
            // テスト実行
            BomHandler bomHandler = new BomHandler(path);
            Charset result = bomHandler.getCharset();
            // 結果検証
            assertThat(result, nullValue());
        }

        /**
         * [試験観点] : 不正なBOM,printf '\xEF' > test10.csv
         * [期待される結果] : 結果がnullであること
         */
        @Test
        public void Test010() throws Throwable {
            // 引数準備
            Path path = getTestDir().resolve("test10.csv");
            // テスト実行
            BomHandler bomHandler = new BomHandler(path);
            Charset result = bomHandler.getCharset();
            // 結果検証
            assertThat(result, nullValue());
        }

    }

    @Nested
    public class BomHandlerTest_skipByteTest extends BaseTest {

        public BomHandler bomHandler;

        @BeforeEach
        public void init() throws IOException {
            // 引数準備
            Path path = getTestDir().resolve("test1.csv");
            // テスト実行
            bomHandler = spy(new BomHandler(path));
        }

        /**
         * [試験観点] : BOMなし,UTF-8
         * [期待される結果] : 結果がnullであること
         */
        @Test
        public void Test001() throws Throwable {
            // モック設定
            doReturn(null).when(bomHandler).getCharset();
            // テスト実行
            int result = bomHandler.skipByte();
            // 結果検証
            assertThat(result, is(0));
        }

        /**
         * [試験観点] : BOMあり,UTF-8
         * [期待される結果] : 結果がUTF-8であること
         */
        @Test
        public void Test002() throws Throwable {
            // モック設定
            doReturn(StandardCharsets.UTF_8).when(bomHandler).getCharset();
            // テスト実行
            int result = bomHandler.skipByte();
            // 結果検証
            assertThat(result, is(3));
        }

        /**
         * [試験観点] : BOMあり,UTF-16BE
         * [期待される結果] : 結果がUTF-16BEであること
         */
        @Test
        public void Test003() throws Throwable {
            // モック設定
            doReturn(StandardCharsets.UTF_16BE).when(bomHandler).getCharset();
            // テスト実行
            int result = bomHandler.skipByte();
            // 結果検証
            assertThat(result, is(2));
        }

        /**
         * [試験観点] : BOMあり,UTF-16LE
         * [期待される結果] : 結果がUTF-16LEであること
         */
        @Test
        public void Test004() throws Throwable {
            // モック設定
            doReturn(StandardCharsets.UTF_16LE).when(bomHandler).getCharset();
            // テスト実行
            int result = bomHandler.skipByte();
            // 結果検証
            assertThat(result, is(2));
        }

        /**
         * [試験観点] : BOMあり,UTF-32BE
         * [期待される結果] : 結果がUTF-32BEであること
         */
        @Test
        public void Test005() throws Throwable {
            // モック設定
            doReturn(StandardCharsets.UTF_32BE).when(bomHandler).getCharset();
            // テスト実行
            int result = bomHandler.skipByte();
            // 結果検証
            assertThat(result, is(4));
        }

        /**
         * [試験観点] : BOMあり,UTF-32LE
         * [期待される結果] : 結果がUTF-32LEであること
         */
        @Test
        public void Test006() throws Throwable {
            // モック設定
            doReturn(StandardCharsets.UTF_32LE).when(bomHandler).getCharset();
            // テスト実行
            int result = bomHandler.skipByte();
            // 結果検証
            assertThat(result, is(4));
        }

        /**
         * [試験観点] : SHIFT-JIS
         * [期待される結果] : 結果が0であること
         */
        @Test
        public void Test007() throws Throwable {
            // モック設定
            doReturn(Charset.forName("shift-jis")).when(bomHandler).getCharset();
            // テスト実行
            int result = bomHandler.skipByte();
            // 結果検証
            assertThat(result, is(0));
        }

    }

}

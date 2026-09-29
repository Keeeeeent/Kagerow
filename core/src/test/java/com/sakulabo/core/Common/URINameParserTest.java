package com.sakulabo.core.Common;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.net.URI;
import java.security.NoSuchAlgorithmException;

import javax.naming.CompositeName;
import javax.naming.Name;
import javax.naming.NoPermissionException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.sakulabo.BaseTest;
import com.sakulabo.BaseTest.KagerowContainerRunner;

@ExtendWith(KagerowContainerRunner.class)
public class URINameParserTest extends BaseTest {

    @Nested
    public class URINameParser_newTest extends BaseTest {

        private Field name;

        public URINameParser_newTest() throws Throwable {
            name = URINameParser.class.getDeclaredField("name");
            name.setAccessible(true);
        }

        /**
         * [試験観点] : インスタンス生成、引数指定あり
         * [期待される結果] : インスタンスが期待通り生成されること
         */
        @Test
        public void Test_001() throws Throwable {
            URINameParser parser = new URINameParser("test");
            assertThat(name.get(parser), is("test"));
        }

        /**
         * [試験観点] : インスタンス生成、引数指定あり
         * [期待される結果] : インスタンスが期待通り生成されること
         */
        @Test
        public void Test_002() throws Throwable {
            Name name = new CompositeName("test");
            URINameParser parser = new URINameParser(name);
            assertThat(this.name.get(parser), is("test"));
        }

        /**
         * [試験観点] : インスタンス生成、引数にnullを指定
         * [期待される結果] : インスタンスが期待通り生成されること
         */
        @Test
        public void Test_003() throws Throwable {
            assertThrows(NullPointerException.class, () -> {
                new URINameParser((String) null);
            });
        }

        /**
         * [試験観点] : インスタンス生成、引数にnullを指定
         * [期待される結果] : インスタンスが期待通り生成されること
         */
        @Test
        public void Test_004() throws Throwable {
            assertThrows(NullPointerException.class, () -> {
                new URINameParser((Name) null);
            });
        }

    }

    @Nested
    public class URINameParser_parseTest extends BaseTest {

        private URINameParser nameParser;

        @BeforeEach
        public void init() {
            nameParser = new URINameParser("test");
        }

        /**
         * [試験観点] : 引数にheader#table形式のデータを指定
         * [期待される結果] : Nameインスタンスが生成されること
         */
        @Test
        public void Test_001() throws Throwable {
            Name result = nameParser.parse("test001#table");
            assertThat(result.toString(), is("test001/table"));
        }

        /**
         * [試験観点] : 引数にheader#table形式の以外のデータを指定
         * [期待される結果] : 例外が発生すること
         */
        @Test
        public void Test_002() throws Throwable {
            NoPermissionException result = assertThrows(NoPermissionException.class, () -> {
                nameParser.parse("test001");
            });
            assertThat(result.getMessage(), is("test001は不正な名称です"));
        }

        /**
         * [試験観点] : 引数にnullを指定
         * [期待される結果] : 例外が発生すること
         */
        @Test
        public void Test_003() throws Throwable {
            NoPermissionException result = assertThrows(NoPermissionException.class, () -> {
                nameParser.parse(null);
            });
            assertThat(result.getMessage(), is("nullは不正な名称です"));
        }

    }

    @Nested
    public class URINameParser_toURIfromStringTest extends BaseTest {

        private URINameParser nameParser;

        @BeforeEach
        public void init() {
            nameParser = new URINameParser("test");
        }

        /**
         * [試験観点] : 単一名称指定(文字列)
         * [期待される結果] : URIインスタンスが生成されること
         */
        @Test
        public void Test_001() throws Throwable {
            URI result = nameParser.toURI("test");
            assertThat(result, is(URI.create("kagerow://application/test#test.zip")));
        }

        /**
         * [試験観点] : 複合名称指定(文字列)
         * [期待される結果] : URIインスタンスが生成されること
         */
        @Test
        public void Test_002() throws Throwable {
            URI result = nameParser.toURI("test/001/kagerow");
            assertThat(result, is(URI.create("kagerow://application/test/001/kagerow#test.zip")));
        }

        /**
         * [試験観点] : nullを指定(文字列)
         * [期待される結果] : IllegalArgumentExceptionがスローされること
         */
        @Test
        public void Test_003() throws Throwable {
            IllegalArgumentException result = assertThrows(IllegalArgumentException.class, () -> {
                nameParser.toURI((String) null);
            });
            assertThat(result.getMessage(), is("nullは不正な名称です"));
        }

    }

    @Nested
    public class URINameParser_toURIfromNameTest extends BaseTest {

        private URINameParser nameParser;

        @BeforeEach
        public void init() {
            nameParser = new URINameParser("test");
        }

        /**
         * [試験観点] : 単一名称指定(名称)
         * [期待される結果] : URIインスタンスが生成されること
         */
        @Test
        public void Test_001() throws Throwable {
            Name name = new CompositeName("test");
            URI result = nameParser.toURI(name);
            assertThat(result, is(URI.create("kagerow://application/test#test.zip")));
        }

        /**
         * [試験観点] : 複合名称指定(名称)
         * [期待される結果] : URIインスタンスが生成されること
         */
        @Test
        public void Test_002() throws Throwable {
            Name name = new CompositeName("test/001/kagerow");
            URI result = nameParser.toURI(name);
            assertThat(result, is(URI.create("kagerow://application/test/001/kagerow#test.zip")));
        }

        /**
         * [試験観点] : nullを指定(名称)
         * [期待される結果] : IllegalArgumentExceptionがスローされること
         */
        @Test
        public void Test_003() throws Throwable {
            IllegalArgumentException result = assertThrows(IllegalArgumentException.class, () -> {
                nameParser.toURI((Name) null);
            });
            assertThat(result.getMessage(), is("nullは不正な名称です"));
        }

    }

    @Nested
    public class URINameParser_baseToURITest extends BaseTest {

        private URINameParser nameParser;

        @BeforeEach
        public void init() {
            nameParser = new URINameParser("test");
        }

        /**
         * [試験観点] : 単一名称指定、DEFAULT_HOST
         * [期待される結果] : URIインスタンスが生成されること
         */
        @Test
        public void Test_001() throws Throwable {
            URI result = nameParser.toURI("test", URINameParser.DEFAULT_HOST);
            assertThat(result, is(URI.create("kagerow://application/test#test.zip")));
        }

        /**
         * [試験観点] : 単一名称指定、BINARY_HOST
         * [期待される結果] : URIインスタンスが生成されること
         */
        @Test
        public void Test_002() throws Throwable {
            URI result = nameParser.toURI("test", URINameParser.BINARY_HOST);
            assertThat(result, is(URI.create(String.format("kagerow://binary/test"))));
        }

        /**
         * [試験観点] : 複合名称指定、DEFAULT_HOST
         * [期待される結果] : URIインスタンスが生成されること
         */
        @Test
        public void Test_003() throws Throwable {
            URI result = nameParser.toURI("test/001/kagerow", URINameParser.DEFAULT_HOST);
            assertThat(result, is(URI.create("kagerow://application/test/001/kagerow#test.zip")));
        }

        /**
         * [試験観点] : 複合名称指定、BINARY_HOST
         * [期待される結果] : URIインスタンスが生成されること
         */
        @Test
        public void Test_004() throws Throwable {
            URI result = nameParser.toURI("test/001/kagerow", URINameParser.BINARY_HOST);
            assertThat(result, is(URI.create("kagerow://binary/test/001/kagerow")));
        }

        /**
         * [試験観点] : nullを指定、DEFAULT_HOST
         * [期待される結果] : IllegalArgumentExceptionがスローされること
         */
        @Test
        public void Test_005() throws Throwable {
            IllegalArgumentException result = assertThrows(IllegalArgumentException.class, () -> {
                nameParser.toURI(null, URINameParser.DEFAULT_HOST);
            });
            assertThat(result.getMessage(), is("nullは不正な名称です"));
        }

        /**
         * [試験観点] : nullを指定、BINARY_HOST
         * [期待される結果] : IllegalArgumentExceptionがスローされること
         */
        @Test
        public void Test_006() throws Throwable {
            IllegalArgumentException result = assertThrows(IllegalArgumentException.class, () -> {
                nameParser.toURI(null, URINameParser.BINARY_HOST);
            });
            assertThat(result.getMessage(), is("nullは不正な名称です"));
        }

    }

    @Nested
    public class URINameParser_joinURITest extends BaseTest {

        /**
         * [試験観点] : URIの結合
         * [期待される結果] : URIインスタンスが生成されること
         */
        @ParameterizedTest
        @CsvSource({
                "kagerow://application/test/target#test.zip,Test_001,kagerow://application/test/Test_001#test.zip",
                "kagerow://binary/test/target#test.zip,Test_001,kagerow://binary/test/Test_001#test.zip",
                "kagerow://application/test#test.zip,Test_001,kagerow://application/Test_001#test.zip",
                "kagerow://binary/test#test.zip,Test_001,kagerow://binary/Test_001#test.zip",
        })
        public void Test_001(URI base, String target, URI expect) throws Throwable {
            URI result = URINameParser.joinURI(base, target);
            assertThat(result, is(expect));
        }

        /**
         * [試験観点] : URIにnullを指定
         * [期待される結果] : IllegalArgumentExceptionがスローされること
         */
        @Test
        public void Test_002() throws Throwable {
            IllegalArgumentException result = assertThrows(IllegalArgumentException.class, () -> {
                URINameParser.joinURI(null, "test");
            });
            assertThat(result.getMessage(), is("nullは不正な名称です"));
        }

        /**
         * [試験観点] : Pathにnullを指定
         * [期待される結果] : IllegalArgumentExceptionがスローされること
         */
        @Test
        public void Test_003() throws Throwable {
            IllegalArgumentException result = assertThrows(IllegalArgumentException.class, () -> {
                URINameParser.joinURI(URI.create("kagerow://application/test#test.zip"), null);
            });
            assertThat(result.getMessage(), is("nullは不正な名称です"));
        }

        /**
         * [試験観点] : Path/URI共にnullを指定
         * [期待される結果] : IllegalArgumentExceptionがスローされること
         */
        @Test
        public void Test_004() throws Throwable {
            IllegalArgumentException result = assertThrows(IllegalArgumentException.class, () -> {
                URINameParser.joinURI(null, null);
            });
            assertThat(result.getMessage(), is("nullは不正な名称です"));
        }

    }

    @Nested
    public class URINameParser_createBinaryPathTest extends BaseTest {

        private URINameParser nameParser;

        @BeforeEach
        public void init() {
            nameParser = new URINameParser("test");
        }

        /**
         * [試験観点] : バイナリパス生成
         * [期待される結果] : バイナリパスが生成されこと
         */
        @Test
        public void Test_001() throws Throwable {
            String result = nameParser.createBinaryPath("MD5", ".dat");
            assertThat(result, containsString("098f6bcd4621d373cade4e832627b4f6"));
            assertThat(result, endsWith(".dat"));
        }

        /**
         * [試験観点] : 不正なアルゴリズム指定
         * [期待される結果] : NoSuchAlgorithmExceptionがスローされうること
         */
        @Test
        public void Test_002() throws Throwable {
            assertThrows(NoSuchAlgorithmException.class, () -> {
                nameParser.createBinaryPath("MD3", ".dat");
            });
        }

        /**
         * [試験観点] : 不正なアルゴリズムにnullを指定
         * [期待される結果] : NoSuchAlgorithmExceptionがスローされうること
         */
        @Test
        public void Test_003() throws Throwable {
            IllegalArgumentException result = assertThrows(IllegalArgumentException.class, () -> {
                nameParser.createBinaryPath(null, ".dat");
            });
            assertThat(result.getMessage(), is("アルゴリズムの指定は必須です"));
        }

        /**
         * [試験観点] : 拡張子にnullを指定
         * [期待される結果] : NoSuchAlgorithmExceptionがスローされうること
         */
        @Test
        public void Test_004() throws Throwable {
            assertThrows(NullPointerException.class, () -> {
                nameParser.createBinaryPath("MD5", null);
            });
        }

    }

    @Nested
    public class URINameParser_createHeaderRealPathTest extends BaseTest {

        private URINameParser nameParser;

        @BeforeEach
        public void init() {
            nameParser = new URINameParser("test");
        }

        /**
         * [試験観点] : バイナリパス生成
         * [期待される結果] : バイナリパスが生成されこと
         */
        @Test
        public void Test_001() throws Throwable {
            String result = nameParser.createHeaderRealPath();
            assertThat(result, containsString("098f6bcd4621d373cade4e832627b4f6"));
        }
    }

    @Nested
    public class URINameParser_createHeaderBinaryPathTest extends BaseTest {

        private URINameParser nameParser;

        @BeforeEach
        public void init() {
            nameParser = new URINameParser("test");
        }

        /**
         * [試験観点] : バイナリパス生成
         * [期待される結果] : バイナリパスが生成されこと
         */
        @Test
        public void Test_001() throws Throwable {
            String header = nameParser.createBinaryPath("MD5", ".dat");
            String result = nameParser.createHeaderBinaryPath(header);
            assertThat(result, containsString("098f6bcd4621d373cade4e832627b4f6"));
        }

        /**
         * [試験観点] : nullを指定
         * [期待される結果] : IllegalArgumentExceptionがスローされること
         */
        @Test
        public void Test_002() throws Throwable {
            IllegalArgumentException result = assertThrows(IllegalArgumentException.class, () -> {
                nameParser.createHeaderBinaryPath(null);
            });
            assertThat(result.getMessage(), is("nullは不正な名称です"));
        }

    }

}

package com.sakulabo.core.Common;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.provider.CsvSource;

import com.sakulabo.BaseTest;
import com.sakulabo.BaseTest.KagerowContainerRunner;
import com.sakulabo.BaseTest.KagerowSystemPropertyRunner.SystemProperty;

@ExtendWith(KagerowContainerRunner.class)
public class VMOptionTest extends BaseTest {

    /**
     * [試験観点] : Enum定義確認
     * [期待される結果] : 期待通りの結果セットであること
     */
    @ParameterizedTest
    @CsvSource({
            "INSTANCE,instance",
            "APP_IO_TMPDIR,app.io.tmpdir",
            "AGENTJAR,agentjar",
            "APP_IO_ARCHIVEDATADIR,app.io.archivedatadir",
            "APP_LOGSDIR,app.logs.dir",
            "LOARDDIBEANS_ADAPTER,LoardDIBeans.Adapter",
            "APP_VERSION,app.version",
            "LAUNCHER_FILE_KEY,app.launcher.config",
            "APP_INIT_MODE,app.init.mode",
            "APP_HOME,app.home",
            "JAVA_VERSION,java.version",
            "JAVA_VENDOR,java.vendor",
            "JAVA_VENDOR_URL,java.vendor.url",
            "JAVA_HOME,java.home",
            "JAVA_VM_SPECIFICATION_VERSION,java.vm.specification.version",
            "JAVA_VM_SPECIFICATION_VENDOR,java.vm.specification.vendor",
            "JAVA_VM_SPECIFICATION_NAME,java.vm.specification.name",
            "JAVA_VM_VERSION,java.vm.version",
            "JAVA_VM_VENDOR,java.vm.vendor",
            "JAVA_VM_NAME,java.vm.name",
            "JAVA_SPECIFICATION_VERSION,java.specification.version",
            "JAVA_SPECIFICATION_VENDOR,java.specification.vendor",
            "JAVA_SPECIFICATION_NAME,java.specification.name",
            "JAVA_CLASS_VERSION,java.class.version",
            "JAVA_CLASS_PATH,java.class.path",
            "JAVA_LIBRARY_PATH,java.library.path",
            "JAVA_IO_TMPDIR,java.io.tmpdir",
            "JAVA_COMPILER,java.compiler",
            "JAVA_EXT_DIRS,java.ext.dirs",
            "OS_NAME,os.name",
            "OS_ARCH,os.arch",
            "OS_VERSION,os.version",
            "FILE_SEPARATOR,file.separator",
            "PATH_SEPARATOR,path.separator",
            "LINE_SEPARATOR,line.separator",
            "USER_NAME,user.name",
            "USER_HOME,user.home",
            "USER_DIR,user.dir",
    })
    public void toStringTest_001(String name, @ConvertWith(ToNotNullableString.class) String vmoption) {
        assertThat(VMOption.valueOf(name).toString(), is(vmoption));
    }

    @Nested
    public class VMOption_UNKNOWNTest extends BaseTest {

        /**
         * [試験観点] : Enum定義確認(UNKNOWN)
         * [期待される結果] : 期待通りの結果であること
         */
        @Test
        @SuppressWarnings("all")
        public void toStringTest_001() {
            assertThat(VMOption.UNKNOWN.toString(), nullValue());
        }

        /**
         * [試験観点] : Enum定義確認(UNKNOWN)
         * [期待される結果] : 期待通りの結果であること
         */
        @Test
        @SuppressWarnings("all")
        public void getVMoptionTest_001() {
            assertThat(VMOption.UNKNOWN.getVMoption(), nullValue());
        }

        /**
         * [試験観点] : Enum定義確認(UNKNOWN)
         * [期待される結果] : 期待通りの結果であること
         */
        @Test
        @SuppressWarnings("all")
        public void toVMOptionTest_001() {
            assertThat(VMOption.UNKNOWN.toVMOption(), nullValue());
        }

        /**
         * [試験観点] : Enum定義確認(UNKNOWN)
         * [期待される結果] : 期待通りの結果であること
         */
        @Test
        @SuppressWarnings("all")
        public void getVMoptionTest_002() {
            assertThat(VMOption.UNKNOWN.getVMoption("test"), nullValue());
        }

    }

    @Nested
    public class VMOption_toVMOptionTest extends BaseTest {

        /**
         * [試験観点] : Enum定義確認
         * [期待される結果] : 期待通りの結果セットであること
         */
        @ParameterizedTest
        @CsvSource({
                "INSTANCE,-Dinstance",
                "APP_IO_TMPDIR,-Dapp.io.tmpdir",
                "AGENTJAR,-Dagentjar",
                "APP_IO_ARCHIVEDATADIR,-Dapp.io.archivedatadir",
                "APP_LOGSDIR,-Dapp.logs.dir",
                "LOARDDIBEANS_ADAPTER,-DLoardDIBeans.Adapter",
                "APP_VERSION,-Dapp.version",
                "LAUNCHER_FILE_KEY,-Dapp.launcher.config",
                "APP_INIT_MODE,-Dapp.init.mode",
                "APP_HOME,-Dapp.home",
                "JAVA_VERSION,-Djava.version",
                "JAVA_VENDOR,-Djava.vendor",
                "JAVA_VENDOR_URL,-Djava.vendor.url",
                "JAVA_HOME,-Djava.home",
                "JAVA_VM_SPECIFICATION_VERSION,-Djava.vm.specification.version",
                "JAVA_VM_SPECIFICATION_VENDOR,-Djava.vm.specification.vendor",
                "JAVA_VM_SPECIFICATION_NAME,-Djava.vm.specification.name",
                "JAVA_VM_VERSION,-Djava.vm.version",
                "JAVA_VM_VENDOR,-Djava.vm.vendor",
                "JAVA_VM_NAME,-Djava.vm.name",
                "JAVA_SPECIFICATION_VERSION,-Djava.specification.version",
                "JAVA_SPECIFICATION_VENDOR,-Djava.specification.vendor",
                "JAVA_SPECIFICATION_NAME,-Djava.specification.name",
                "JAVA_CLASS_VERSION,-Djava.class.version",
                "JAVA_CLASS_PATH,-Djava.class.path",
                "JAVA_LIBRARY_PATH,-Djava.library.path",
                "JAVA_IO_TMPDIR,-Djava.io.tmpdir",
                "JAVA_COMPILER,-Djava.compiler",
                "JAVA_EXT_DIRS,-Djava.ext.dirs",
                "OS_NAME,-Dos.name",
                "OS_ARCH,-Dos.arch",
                "OS_VERSION,-Dos.version",
                "FILE_SEPARATOR,-Dfile.separator",
                "PATH_SEPARATOR,-Dpath.separator",
                "LINE_SEPARATOR,-Dline.separator",
                "USER_NAME,-Duser.name",
                "USER_HOME,-Duser.home",
                "USER_DIR,-Duser.dir",
        })
        public void toVMOptionTest_001(String name, @ConvertWith(ToNotNullableString.class) String vmoption) {
            assertThat(VMOption.valueOf(name).toVMOption(), is(vmoption));
        }

        /**
         * [試験観点] : 空文字が設定済み
         * [期待される結果] : nullであること
         */
        @Test
        public void toVMOptionTest_002() throws Throwable {
            VMOption target = createEnum(VMOption.class);
            Field field = VMOption.class.getDeclaredField("vmoption");
            field.setAccessible(true);
            field.set(target, "");
            assertThat(target.toVMOption(), nullValue());
        }

    }

    @Nested
    @ExtendWith(KagerowSystemPropertyRunner.class)
    public class VMOption_getVMoptionTest extends BaseTest {

        /**
         * [試験観点] : Enum定義確認
         * [期待される結果] : 期待通りの結果セットであること
         */
        @ParameterizedTest
        @CsvSource({
                "INSTANCE,test",
                "APP_IO_TMPDIR,test",
                "AGENTJAR,test",
                "APP_IO_ARCHIVEDATADIR,test",
                "APP_LOGSDIR,test",
                "LOARDDIBEANS_ADAPTER,test",
                "APP_VERSION,test",
                "LAUNCHER_FILE_KEY,test",
                "APP_INIT_MODE,test",
                "APP_HOME,test",
                "JAVA_VERSION,test",
                "JAVA_VENDOR,test",
                "JAVA_VENDOR_URL,test",
                "JAVA_HOME,test",
                "JAVA_VM_SPECIFICATION_VERSION,test",
                "JAVA_VM_SPECIFICATION_VENDOR,test",
                "JAVA_VM_SPECIFICATION_NAME,test",
                "JAVA_VM_VERSION,test",
                "JAVA_VM_VENDOR,test",
                "JAVA_VM_NAME,test",
                "JAVA_SPECIFICATION_VERSION,test",
                "JAVA_SPECIFICATION_VENDOR,test",
                "JAVA_SPECIFICATION_NAME,test",
                "JAVA_CLASS_VERSION,test",
                "JAVA_CLASS_PATH,test",
                "JAVA_LIBRARY_PATH,test",
                "JAVA_IO_TMPDIR,test",
                "JAVA_COMPILER,test",
                "JAVA_EXT_DIRS,test",
                "OS_NAME,test",
                "OS_ARCH,test",
                "OS_VERSION,test",
                "FILE_SEPARATOR,test",
                "PATH_SEPARATOR,test",
                "LINE_SEPARATOR,test",
                "USER_NAME,test",
                "USER_HOME,test",
                "USER_DIR,test",
        })
        @SystemProperty(key = "instance", value = "test")
        @SystemProperty(key = "app.io.tmpdir", value = "test")
        @SystemProperty(key = "agentjar", value = "test")
        @SystemProperty(key = "app.io.archivedatadir", value = "test")
        @SystemProperty(key = "app.logs.dir", value = "test")
        @SystemProperty(key = "LoardDIBeans.Adapter", value = "test")
        @SystemProperty(key = "app.version", value = "test")
        @SystemProperty(key = "app.launcher.config", value = "test")
        @SystemProperty(key = "app.init.mode", value = "test")
        @SystemProperty(key = "app.home", value = "test")
        @SystemProperty(key = "java.version", value = "test")
        @SystemProperty(key = "java.vendor", value = "test")
        @SystemProperty(key = "java.vendor.url", value = "test")
        @SystemProperty(key = "java.home", value = "test")
        @SystemProperty(key = "java.vm.specification.version", value = "test")
        @SystemProperty(key = "java.vm.specification.vendor", value = "test")
        @SystemProperty(key = "java.vm.specification.name", value = "test")
        @SystemProperty(key = "java.vm.version", value = "test")
        @SystemProperty(key = "java.vm.vendor", value = "test")
        @SystemProperty(key = "java.vm.name", value = "test")
        @SystemProperty(key = "java.specification.version", value = "test")
        @SystemProperty(key = "java.specification.vendor", value = "test")
        @SystemProperty(key = "java.specification.name", value = "test")
        @SystemProperty(key = "java.class.version", value = "test")
        @SystemProperty(key = "java.class.path", value = "test")
        @SystemProperty(key = "java.library.path", value = "test")
        @SystemProperty(key = "java.io.tmpdir", value = "test")
        @SystemProperty(key = "java.compiler", value = "test")
        @SystemProperty(key = "java.ext.dirs", value = "test")
        @SystemProperty(key = "os.name", value = "test")
        @SystemProperty(key = "os.arch", value = "test")
        @SystemProperty(key = "os.version", value = "test")
        @SystemProperty(key = "file.separator", value = "test")
        @SystemProperty(key = "path.separator", value = "test")
        @SystemProperty(key = "line.separator", value = "test")
        @SystemProperty(key = "user.name", value = "test")
        @SystemProperty(key = "user.home", value = "test")
        @SystemProperty(key = "user.dir", value = "test")
        public void getVMoptionTest_001(String name, @ConvertWith(ToNotNullableString.class) String vmoption) {
            assertThat(VMOption.valueOf(name).getVMoption(), is(vmoption));
        }

        /**
         * [試験観点] : Enum定義確認、システムプロパティー未設定
         * [期待される結果] : 期待通りの結果セットであること
         */
        @ParameterizedTest
        @CsvSource({
                "INSTANCE,test",
                "APP_IO_TMPDIR,test",
                "AGENTJAR,test",
                "APP_IO_ARCHIVEDATADIR,test",
                "APP_LOGSDIR,test",
                "LOARDDIBEANS_ADAPTER,test",
                "APP_VERSION,test",
                "LAUNCHER_FILE_KEY,test",
                "APP_INIT_MODE,test",
                "APP_HOME,test",
                "JAVA_VERSION,test",
                "JAVA_VENDOR,test",
                "JAVA_VENDOR_URL,test",
                "JAVA_HOME,test",
                "JAVA_VM_SPECIFICATION_VERSION,test",
                "JAVA_VM_SPECIFICATION_VENDOR,test",
                "JAVA_VM_SPECIFICATION_NAME,test",
                "JAVA_VM_VERSION,test",
                "JAVA_VM_VENDOR,test",
                "JAVA_VM_NAME,test",
                "JAVA_SPECIFICATION_VERSION,test",
                "JAVA_SPECIFICATION_VENDOR,test",
                "JAVA_SPECIFICATION_NAME,test",
                "JAVA_CLASS_VERSION,test",
                "JAVA_CLASS_PATH,test",
                "JAVA_LIBRARY_PATH,test",
                "JAVA_IO_TMPDIR,test",
                "JAVA_COMPILER,test",
                "JAVA_EXT_DIRS,test",
                "OS_NAME,test",
                "OS_ARCH,test",
                "OS_VERSION,test",
                "FILE_SEPARATOR,test",
                "PATH_SEPARATOR,test",
                "LINE_SEPARATOR,test",
                "USER_NAME,test",
                "USER_HOME,test",
                "USER_DIR,test",
        })
        @SystemProperty(key = "instance")
        @SystemProperty(key = "app.io.tmpdir")
        @SystemProperty(key = "agentjar")
        @SystemProperty(key = "app.io.archivedatadir")
        @SystemProperty(key = "app.logs.dir")
        @SystemProperty(key = "LoardDIBeans.Adapter")
        @SystemProperty(key = "app.version")
        @SystemProperty(key = "app.launcher.config")
        @SystemProperty(key = "app.init.mode")
        @SystemProperty(key = "app.home")
        @SystemProperty(key = "java.version")
        @SystemProperty(key = "java.vendor")
        @SystemProperty(key = "java.vendor.url")
        @SystemProperty(key = "java.home")
        @SystemProperty(key = "java.vm.specification.version")
        @SystemProperty(key = "java.vm.specification.vendor")
        @SystemProperty(key = "java.vm.specification.name")
        @SystemProperty(key = "java.vm.version")
        @SystemProperty(key = "java.vm.vendor")
        @SystemProperty(key = "java.vm.name")
        @SystemProperty(key = "java.specification.version")
        @SystemProperty(key = "java.specification.vendor")
        @SystemProperty(key = "java.specification.name")
        @SystemProperty(key = "java.class.version")
        @SystemProperty(key = "java.class.path")
        @SystemProperty(key = "java.library.path")
        @SystemProperty(key = "java.io.tmpdir")
        @SystemProperty(key = "java.compiler")
        @SystemProperty(key = "java.ext.dirs")
        @SystemProperty(key = "os.name")
        @SystemProperty(key = "os.arch")
        @SystemProperty(key = "os.version")
        @SystemProperty(key = "file.separator")
        @SystemProperty(key = "path.separator")
        @SystemProperty(key = "line.separator")
        @SystemProperty(key = "user.name")
        @SystemProperty(key = "user.home")
        @SystemProperty(key = "user.dir")
        public void getVMoptionTest_002(String name, @ConvertWith(ToNotNullableString.class) String vmoption) {
            assertThat(VMOption.valueOf(name).getVMoption(), nullValue());
            assertThat(VMOption.valueOf(name).getVMoption("test"), is(vmoption));
        }

    }

    @Nested
    @ExtendWith(KagerowSystemPropertyRunner.class)
    public class VMOption_setVMoptionTest extends BaseTest {

        /**
         * [試験観点] : Enum定義確認
         * [期待される結果] : 期待通りの結果セットであること
         */
        @ParameterizedTest
        @CsvSource({
                "INSTANCE,instance",
                "APP_IO_TMPDIR,app.io.tmpdir",
                "AGENTJAR,agentjar",
                "APP_IO_ARCHIVEDATADIR,app.io.archivedatadir",
                "APP_LOGSDIR,app.logs.dir",
                "LOARDDIBEANS_ADAPTER,LoardDIBeans.Adapter",
                "APP_VERSION,app.version",
                "LAUNCHER_FILE_KEY,app.launcher.config",
                "APP_INIT_MODE,app.init.mode",
                "APP_HOME,app.home",
                "JAVA_VERSION,java.version",
                "JAVA_VENDOR,java.vendor",
                "JAVA_VENDOR_URL,java.vendor.url",
                "JAVA_HOME,java.home",
                "JAVA_VM_SPECIFICATION_VERSION,java.vm.specification.version",
                "JAVA_VM_SPECIFICATION_VENDOR,java.vm.specification.vendor",
                "JAVA_VM_SPECIFICATION_NAME,java.vm.specification.name",
                "JAVA_VM_VERSION,java.vm.version",
                "JAVA_VM_VENDOR,java.vm.vendor",
                "JAVA_VM_NAME,java.vm.name",
                "JAVA_SPECIFICATION_VERSION,java.specification.version",
                "JAVA_SPECIFICATION_VENDOR,java.specification.vendor",
                "JAVA_SPECIFICATION_NAME,java.specification.name",
                "JAVA_CLASS_VERSION,java.class.version",
                "JAVA_CLASS_PATH,java.class.path",
                "JAVA_LIBRARY_PATH,java.library.path",
                "JAVA_IO_TMPDIR,java.io.tmpdir",
                "JAVA_COMPILER,java.compiler",
                "JAVA_EXT_DIRS,java.ext.dirs",
                "OS_NAME,os.name",
                "OS_ARCH,os.arch",
                "OS_VERSION,os.version",
                "FILE_SEPARATOR,file.separator",
                "PATH_SEPARATOR,path.separator",
                "LINE_SEPARATOR,line.separator",
                "USER_NAME,user.name",
                "USER_HOME,user.home",
                "USER_DIR,user.dir",
        })
        @SystemProperty(key = "instance")
        @SystemProperty(key = "app.io.tmpdir")
        @SystemProperty(key = "agentjar")
        @SystemProperty(key = "app.io.archivedatadir")
        @SystemProperty(key = "app.logs.dir")
        @SystemProperty(key = "LoardDIBeans.Adapter")
        @SystemProperty(key = "app.version")
        @SystemProperty(key = "app.launcher.config")
        @SystemProperty(key = "app.init.mode")
        @SystemProperty(key = "app.home")
        @SystemProperty(key = "java.version")
        @SystemProperty(key = "java.vendor")
        @SystemProperty(key = "java.vendor.url")
        @SystemProperty(key = "java.home")
        @SystemProperty(key = "java.vm.specification.version")
        @SystemProperty(key = "java.vm.specification.vendor")
        @SystemProperty(key = "java.vm.specification.name")
        @SystemProperty(key = "java.vm.version")
        @SystemProperty(key = "java.vm.vendor")
        @SystemProperty(key = "java.vm.name")
        @SystemProperty(key = "java.specification.version")
        @SystemProperty(key = "java.specification.vendor")
        @SystemProperty(key = "java.specification.name")
        @SystemProperty(key = "java.class.version")
        @SystemProperty(key = "java.class.path")
        @SystemProperty(key = "java.library.path")
        @SystemProperty(key = "java.io.tmpdir")
        @SystemProperty(key = "java.compiler")
        @SystemProperty(key = "java.ext.dirs")
        @SystemProperty(key = "os.name")
        @SystemProperty(key = "os.arch")
        @SystemProperty(key = "os.version")
        @SystemProperty(key = "file.separator")
        @SystemProperty(key = "path.separator")
        @SystemProperty(key = "line.separator")
        @SystemProperty(key = "user.name")
        @SystemProperty(key = "user.home")
        @SystemProperty(key = "user.dir")
        public void setVMoptionTest_001(String name, @ConvertWith(ToNotNullableString.class) String vmoption) {
            VMOption.valueOf(name).setVMoption("test");
            assertThat(System.getProperty(vmoption), is("test"));
        }

    }

    @Nested
    @ExtendWith(KagerowSystemPropertyRunner.class)
    public class VMOption_staticToVMOptionTest extends BaseTest {

        /**
         * [試験観点] : Enum定義確認
         * [期待される結果] : 期待通りの結果セットであること
         */
        @ParameterizedTest
        @CsvSource({
                "INSTANCE,instance",
                "APP_IO_TMPDIR,app.io.tmpdir",
                "AGENTJAR,agentjar",
                "APP_IO_ARCHIVEDATADIR,app.io.archivedatadir",
                "APP_LOGSDIR,app.logs.dir",
                "LOARDDIBEANS_ADAPTER,LoardDIBeans.Adapter",
                "APP_VERSION,app.version",
                "LAUNCHER_FILE_KEY,app.launcher.config",
                "APP_INIT_MODE,app.init.mode",
                "APP_HOME,app.home",
                "JAVA_VERSION,java.version",
                "JAVA_VENDOR,java.vendor",
                "JAVA_VENDOR_URL,java.vendor.url",
                "JAVA_HOME,java.home",
                "JAVA_VM_SPECIFICATION_VERSION,java.vm.specification.version",
                "JAVA_VM_SPECIFICATION_VENDOR,java.vm.specification.vendor",
                "JAVA_VM_SPECIFICATION_NAME,java.vm.specification.name",
                "JAVA_VM_VERSION,java.vm.version",
                "JAVA_VM_VENDOR,java.vm.vendor",
                "JAVA_VM_NAME,java.vm.name",
                "JAVA_SPECIFICATION_VERSION,java.specification.version",
                "JAVA_SPECIFICATION_VENDOR,java.specification.vendor",
                "JAVA_SPECIFICATION_NAME,java.specification.name",
                "JAVA_CLASS_VERSION,java.class.version",
                "JAVA_CLASS_PATH,java.class.path",
                "JAVA_LIBRARY_PATH,java.library.path",
                "JAVA_IO_TMPDIR,java.io.tmpdir",
                "JAVA_COMPILER,java.compiler",
                "JAVA_EXT_DIRS,java.ext.dirs",
                "OS_NAME,os.name",
                "OS_ARCH,os.arch",
                "OS_VERSION,os.version",
                "FILE_SEPARATOR,file.separator",
                "PATH_SEPARATOR,path.separator",
                "LINE_SEPARATOR,line.separator",
                "USER_NAME,user.name",
                "USER_HOME,user.home",
                "USER_DIR,user.dir",
        })
        public void toVMOptionTest_001(String name, @ConvertWith(ToNotNullableString.class) String vmoption) {
            assertThat(VMOption.toVMOption(vmoption), is(VMOption.valueOf(name)));
        }

        /**
         * [試験観点] : 存在しないVMoption、デフォルト値がUNKNOWN
         * [期待される結果] : UNKNOWNであること
         */
        @Test
        @SuppressWarnings("all")
        public void toVMOptionTest_002() {
            assertThat(VMOption.toVMOption("test", VMOption.UNKNOWN), is(VMOption.UNKNOWN));
        }

        /**
         * [試験観点] : 存在しないVMoption、デフォルト値がUNKNOWN以外
         * [期待される結果] : 指定したEnumであること
         */
        @Test
        public void toVMOptionTest_003() {
            assertThat(VMOption.toVMOption("test", VMOption.JAVA_HOME), is(VMOption.JAVA_HOME));
        }

        /**
         * [試験観点] : 存在するVMoption
         * [期待される結果] : 検知したEnumが返却されること
         */
        @Test
        @SuppressWarnings("all")
        public void toVMOptionTest_004() {
            assertThat(VMOption.toVMOption("java.home", VMOption.UNKNOWN), is(VMOption.JAVA_HOME));
        }

    }

    @Nested
    @ExtendWith(KagerowSystemPropertyRunner.class)
    public class VMOption_searchVMOptionTest extends BaseTest {

        /**
         * [試験観点] : Enum定義確認
         * [期待される結果] : 期待通りの結果セットであること
         */
        @ParameterizedTest
        @CsvSource({
                "INSTANCE,test1",
                "APP_IO_TMPDIR,test2",
                "AGENTJAR,test3",
                "APP_IO_ARCHIVEDATADIR,test4",
                "APP_LOGSDIR,test5",
                "LOARDDIBEANS_ADAPTER,test6",
                "APP_VERSION,test7",
                "LAUNCHER_FILE_KEY,test8",
                "APP_INIT_MODE,test9",
                "APP_HOME,test10",
                "JAVA_VERSION,test11",
                "JAVA_VENDOR,test12",
                "JAVA_VENDOR_URL,test13",
                "JAVA_HOME,test14",
                "JAVA_VM_SPECIFICATION_VERSION,test15",
                "JAVA_VM_SPECIFICATION_VENDOR,test16",
                "JAVA_VM_SPECIFICATION_NAME,test17",
                "JAVA_VM_VERSION,test18",
                "JAVA_VM_VENDOR,test19",
                "JAVA_VM_NAME,test20",
                "JAVA_SPECIFICATION_VERSION,test21",
                "JAVA_SPECIFICATION_VENDOR,test22",
                "JAVA_SPECIFICATION_NAME,test23",
                "JAVA_CLASS_VERSION,test24",
                "JAVA_CLASS_PATH,test25",
                "JAVA_LIBRARY_PATH,test26",
                "JAVA_IO_TMPDIR,test27",
                "JAVA_COMPILER,test28",
                "JAVA_EXT_DIRS,test29",
                "OS_NAME,test30",
                "OS_ARCH,test31",
                "OS_VERSION,test32",
                "FILE_SEPARATOR,test33",
                "PATH_SEPARATOR,test34",
                "LINE_SEPARATOR,test35",
                "USER_NAME,test36",
                "USER_HOME,test37",
                "USER_DIR,test38",
        })
        @SystemProperty(key = "instance", value = "test1")
        @SystemProperty(key = "app.io.tmpdir", value = "test2")
        @SystemProperty(key = "agentjar", value = "test3")
        @SystemProperty(key = "app.io.archivedatadir", value = "test4")
        @SystemProperty(key = "app.logs.dir", value = "test5")
        @SystemProperty(key = "LoardDIBeans.Adapter", value = "test6")
        @SystemProperty(key = "app.version", value = "test7")
        @SystemProperty(key = "app.launcher.config", value = "test8")
        @SystemProperty(key = "app.init.mode", value = "test9")
        @SystemProperty(key = "app.home", value = "test10")
        @SystemProperty(key = "java.version", value = "test11")
        @SystemProperty(key = "java.vendor", value = "test12")
        @SystemProperty(key = "java.vendor.url", value = "test13")
        @SystemProperty(key = "java.home", value = "test14")
        @SystemProperty(key = "java.vm.specification.version", value = "test15")
        @SystemProperty(key = "java.vm.specification.vendor", value = "test16")
        @SystemProperty(key = "java.vm.specification.name", value = "test17")
        @SystemProperty(key = "java.vm.version", value = "test18")
        @SystemProperty(key = "java.vm.vendor", value = "test19")
        @SystemProperty(key = "java.vm.name", value = "test20")
        @SystemProperty(key = "java.specification.version", value = "test21")
        @SystemProperty(key = "java.specification.vendor", value = "test22")
        @SystemProperty(key = "java.specification.name", value = "test23")
        @SystemProperty(key = "java.class.version", value = "test24")
        @SystemProperty(key = "java.class.path", value = "test25")
        @SystemProperty(key = "java.library.path", value = "test26")
        @SystemProperty(key = "java.io.tmpdir", value = "test27")
        @SystemProperty(key = "java.compiler", value = "test28")
        @SystemProperty(key = "java.ext.dirs", value = "test29")
        @SystemProperty(key = "os.name", value = "test30")
        @SystemProperty(key = "os.arch", value = "test31")
        @SystemProperty(key = "os.version", value = "test32")
        @SystemProperty(key = "file.separator", value = "test33")
        @SystemProperty(key = "path.separator", value = "test34")
        @SystemProperty(key = "line.separator", value = "test35")
        @SystemProperty(key = "user.name", value = "test36")
        @SystemProperty(key = "user.home", value = "test37")
        @SystemProperty(key = "user.dir", value = "test38")
        public void searchVMOptionTest_001(String name, @ConvertWith(ToNotNullableString.class) String vmoption) {
            assertThat(VMOption.searchVMOption(vmoption), is(VMOption.valueOf(name)));
        }

        /**
         * [試験観点] : 存在しないVMOption設定値
         * [期待される結果] : UNKNOWNであること
         */
        @Test
        @SuppressWarnings("all")
        public void searchVMOptionTest_002() {
            assertThat(VMOption.searchVMOption("test"), is(VMOption.UNKNOWN));
        }
    }

}

package com.sakulabo.core.Common;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.sakulabo.BaseTest;
import com.sakulabo.BaseTest.KagerowContainerRunner;
import com.sakulabo.BaseTest.KagerowSystemPropertyRunner.SystemProperty;

@ExtendWith(KagerowContainerRunner.class)
public class AppPathUtilsTest extends BaseTest {

    @Nested
    @ExtendWith(KagerowSystemPropertyRunner.class)
    public class AppPathUtilsTest_getBasePathTest extends BaseTest {

        /**
         * [試験観点] : APP_HOMEが取得可能
         * [期待される結果] : APP_HOMEが返却されること
         */
        @Test
        @SystemProperty(key = "app.home", value = "getBasePath_001")
        public void getBasePath_001() {
            String result = AppPathUtils.getBasePath();
            assertThat(result, is("getBasePath_001"));
        }

        /**
         * [試験観点] : APP_HOMEが不可能
         * [期待される結果] : USER_HOMEが返却されること
         */
        @Test
        @SystemProperty(key = "app.home")
        @SystemProperty(key = "user.home", value = "getBasePath_002")
        public void getBasePath_002() {
            String result = AppPathUtils.getBasePath();
            assertThat(result, is("getBasePath_002"));
        }

    }

    @Nested
    @ExtendWith(KagerowSystemPropertyRunner.class)
    public class AppPathUtilsTest_createArchiveDirPathTest extends BaseTest {

        /**
         * [試験観点] : APP_HOMEが取得可能
         * [期待される結果] : APP_HOMEが返却されること
         */
        @Test
        @SystemProperty(key = "app.home", value = "createArchiveDirPath_001")
        public void createArchiveDirPath_001() {
            Path result = AppPathUtils.createArchiveDirPath();
            Path expect = Paths.get("createArchiveDirPath_001/.kagerow/database")
                    .normalize()
                    .toAbsolutePath();
            assertThat(result, is(expect));
        }

        /**
         * [試験観点] : APP_HOMEが不可能
         * [期待される結果] : USER_HOMEが返却されること
         */
        @Test
        @SystemProperty(key = "app.home")
        @SystemProperty(key = "user.home", value = "createArchiveDirPath_002")
        public void createArchiveDirPath_002() {
            Path result = AppPathUtils.createArchiveDirPath();
            Path expect = Paths.get("createArchiveDirPath_002/.kagerow/database")
                    .normalize()
                    .toAbsolutePath();
            assertThat(result, is(expect));
        }

    }

    @Nested
    @ExtendWith(KagerowSystemPropertyRunner.class)
    public class AppPathUtilsTest_createPluginDirPathTest extends BaseTest {

        /**
         * [試験観点] : APP_HOMEが取得可能
         * [期待される結果] : APP_HOMEが返却されること
         */
        @Test
        @SystemProperty(key = "app.home", value = "createPluginDirPath_001")
        public void createPluginDirPath_001() {
            Path result = AppPathUtils.createPluginDirPath();
            Path expect = Paths.get("createPluginDirPath_001/.kagerow/plugin")
                    .normalize()
                    .toAbsolutePath();
            assertThat(result, is(expect));
        }

        /**
         * [試験観点] : APP_HOMEが不可能
         * [期待される結果] : USER_HOMEが返却されること
         */
        @Test
        @SystemProperty(key = "app.home")
        @SystemProperty(key = "user.home", value = "createPluginDirPath_002")
        public void createPluginDirPath_002() {
            Path result = AppPathUtils.createPluginDirPath();
            Path expect = Paths.get("createPluginDirPath_002/.kagerow/plugin")
                    .normalize()
                    .toAbsolutePath();
            assertThat(result, is(expect));
        }

    }

    @Nested
    @ExtendWith(KagerowSystemPropertyRunner.class)
    public class AppPathUtilsTest_createCacheDirPathTest extends BaseTest {

        /**
         * [試験観点] : APP_HOMEが取得可能
         * [期待される結果] : APP_HOMEが返却されること
         */
        @Test
        @SystemProperty(key = "app.home", value = "createCacheDirPath_001")
        public void createCacheDirPath_001() {
            Path result = AppPathUtils.createCacheDirPath();
            Path expect = Paths.get("createCacheDirPath_001/.kagerow/cache")
                    .normalize()
                    .toAbsolutePath();
            assertThat(result, is(expect));
        }

        /**
         * [試験観点] : APP_HOMEが不可能
         * [期待される結果] : USER_HOMEが返却されること
         */
        @Test
        @SystemProperty(key = "app.home")
        @SystemProperty(key = "user.home", value = "createCacheDirPath_002")
        public void createCacheDirPath_002() {
            Path result = AppPathUtils.createCacheDirPath();
            Path expect = Paths.get("createCacheDirPath_002/.kagerow/cache")
                    .normalize()
                    .toAbsolutePath();
            assertThat(result, is(expect));
        }

    }

    @Nested
    @ExtendWith(KagerowSystemPropertyRunner.class)
    public class AppPathUtilsTest_createLogDirPathTest extends BaseTest {

        /**
         * [試験観点] : APP_HOMEが取得可能
         * [期待される結果] : APP_HOMEが返却されること
         */
        @Test
        @SystemProperty(key = "app.home", value = "createLogDirPath_001")
        public void createLogDirPath_001() {
            Path result = AppPathUtils.createLogDirPath();
            Path expect = Paths.get("createLogDirPath_001/.kagerow/logs")
                    .normalize()
                    .toAbsolutePath();
            assertThat(result, is(expect));
        }

        /**
         * [試験観点] : APP_HOMEが不可能
         * [期待される結果] : USER_HOMEが返却されること
         */
        @Test
        @SystemProperty(key = "app.home")
        @SystemProperty(key = "user.home", value = "createLogDirPath_002")
        public void createLogDirPath_002() {
            Path result = AppPathUtils.createLogDirPath();
            Path expect = Paths.get("createLogDirPath_002/.kagerow/logs")
                    .normalize()
                    .toAbsolutePath();
            assertThat(result, is(expect));
        }

        /**
         * [試験観点] : APP_LOGSDIRが設定済み
         * [期待される結果] : APP_LOGSDIRが適用されること
         */
        @Test
        @SystemProperty(key = "app.home", value = "createLogDirPath_003")
        @SystemProperty(key = "app.logs.dir", value = "/testLog")
        public void createLogDirPath_003() {
            Path result = AppPathUtils.createLogDirPath();
            Path expect = Paths.get("createLogDirPath_003/testLog")
                    .normalize()
                    .toAbsolutePath();
            assertThat(result, is(expect));
        }

    }

    @Nested
    @ExtendWith(KagerowSystemPropertyRunner.class)
    public class AppPathUtilsTest_createTemporaryDirPathTest extends BaseTest {

        /**
         * [試験観点] : APP_HOMEが取得可能
         * [期待される結果] : APP_HOMEが返却されること
         */
        @Test
        @SystemProperty(key = "app.home", value = "createTemporaryDirPath_001")
        public void createTemporaryDirPath_001() {
            Path result = AppPathUtils.createTemporaryDirPath();
            Path expect = Paths.get("createTemporaryDirPath_001/.kagerow/tmp")
                    .normalize()
                    .toAbsolutePath();
            assertThat(result, is(expect));
        }

        /**
         * [試験観点] : APP_HOMEが不可能
         * [期待される結果] : USER_HOMEが返却されること
         */
        @Test
        @SystemProperty(key = "app.home")
        @SystemProperty(key = "user.home", value = "createTemporaryDirPath_002")
        public void createTemporaryDirPath_002() {
            Path result = AppPathUtils.createTemporaryDirPath();
            Path expect = Paths.get("createTemporaryDirPath_002/.kagerow/tmp")
                    .normalize()
                    .toAbsolutePath();
            assertThat(result, is(expect));
        }

        /**
         * [試験観点] : APP_IO_TMPDIRが設定済み
         * [期待される結果] : APP_IO_TMPDIRが適用されること
         */
        @Test
        @SystemProperty(key = "app.home", value = "createTemporaryDirPath_003")
        @SystemProperty(key = "app.io.tmpdir", value = "/testLog")
        public void createTemporaryDirPath_003() {
            Path result = AppPathUtils.createTemporaryDirPath();
            Path expect = Paths.get("createTemporaryDirPath_003/testLog")
                    .normalize()
                    .toAbsolutePath();
            assertThat(result, is(expect));
        }

    }

    @Nested
    @ExtendWith(KagerowSystemPropertyRunner.class)
    public class AppPathUtilsTest_createRuntimeDirPathTest extends BaseTest {

        /**
         * [試験観点] : APP_HOMEが取得可能
         * [期待される結果] : APP_HOMEが返却されること
         */
        @Test
        @SystemProperty(key = "app.home", value = "createRuntimeDirPath_001")
        public void createRuntimeDirPath_001() {
            Path result = AppPathUtils.createRuntimeDirPath();
            Path expect = Paths.get("createRuntimeDirPath_001/.kagerow/runtime")
                    .normalize()
                    .toAbsolutePath();
            assertThat(result, is(expect));
        }

        /**
         * [試験観点] : APP_HOMEが不可能
         * [期待される結果] : USER_HOMEが返却されること
         */
        @Test
        @SystemProperty(key = "app.home")
        @SystemProperty(key = "user.home", value = "createRuntimeDirPath_002")
        public void createRuntimeDirPath_002() {
            Path result = AppPathUtils.createRuntimeDirPath();
            Path expect = Paths.get("createRuntimeDirPath_002/.kagerow/runtime")
                    .normalize()
                    .toAbsolutePath();
            assertThat(result, is(expect));
        }

    }

    @Nested
    @ExtendWith(KagerowSystemPropertyRunner.class)
    public class AppPathUtilsTest_createSettingDirPathTest extends BaseTest {

        /**
         * [試験観点] : APP_HOMEが取得可能
         * [期待される結果] : APP_HOMEが返却されること
         */
        @Test
        @SystemProperty(key = "app.home", value = "createSettingDirPath_001")
        public void createSettingDirPath_001() {
            Path result = AppPathUtils.createSettingDirPath();
            Path expect = Paths.get("createSettingDirPath_001/.kagerow/setting")
                    .normalize()
                    .toAbsolutePath();
            assertThat(result, is(expect));
        }

        /**
         * [試験観点] : APP_HOMEが不可能
         * [期待される結果] : USER_HOMEが返却されること
         */
        @Test
        @SystemProperty(key = "app.home")
        @SystemProperty(key = "user.home", value = "createSettingDirPath_002")
        public void createSettingDirPath_002() {
            Path result = AppPathUtils.createSettingDirPath();
            Path expect = Paths.get("createSettingDirPath_002/.kagerow/setting")
                    .normalize()
                    .toAbsolutePath();
            assertThat(result, is(expect));
        }

    }

    @Nested
    @ExtendWith(KagerowSystemPropertyRunner.class)
    public class AppPathUtilsTest_createKagerowHomePathTest extends BaseTest {

        /**
         * [試験観点] : APP_HOMEが取得可能
         * [期待される結果] : APP_HOMEが返却されること
         */
        @Test
        @SystemProperty(key = "app.home", value = "createKagerowHomePath_001")
        public void createKagerowHomePath_001() {
            Path result = AppPathUtils.createKagerowHomePath();
            Path expect = Paths.get("createKagerowHomePath_001/.kagerow")
                    .normalize()
                    .toAbsolutePath();
            assertThat(result, is(expect));
        }

        /**
         * [試験観点] : APP_HOMEが不可能
         * [期待される結果] : USER_HOMEが返却されること
         */
        @Test
        @SystemProperty(key = "app.home")
        @SystemProperty(key = "user.home", value = "createKagerowHomePath_002")
        public void createKagerowHomePath_002() {
            Path result = AppPathUtils.createKagerowHomePath();
            Path expect = Paths.get("createKagerowHomePath_002/.kagerow")
                    .normalize()
                    .toAbsolutePath();
            assertThat(result, is(expect));
        }

    }

    @Nested
    @ExtendWith(KagerowSystemPropertyRunner.class)
    public class AppPathUtilsTest_createAppDirPathTest extends BaseTest {

        /**
         * [試験観点] : クラスファイルパスの取得が可能
         * [期待される結果] : インストールディレクトリが取得できること
         */
        @Test
        public void createAppDirPath_001() {
            Path result = AppPathUtils.createAppDirPath();
            Path expect = Paths.get("").normalize().toAbsolutePath();
            assertThat(result, is(expect));
        }

    }

    @Nested
    @ExtendWith(KagerowSystemPropertyRunner.class)
    public class AppPathUtilsTest_createConfigDirPathTest extends BaseTest {

        /**
         * [試験観点] : クラスファイルパスの取得が可能
         * [期待される結果] : インストールディレクトリが取得できること
         */
        @Test
        public void createConfigDirPath_001() {
            Path result = AppPathUtils.createConfigDirPath();
            Path expect = Paths.get("").normalize().toAbsolutePath().resolve("config");
            assertThat(result, is(expect));
        }

    }

    @Nested
    @ExtendWith(KagerowSystemPropertyRunner.class)
    public class AppPathUtilsTest_createDefaultPluginDirPathTest extends BaseTest {

        /**
         * [試験観点] : クラスファイルパスの取得が可能
         * [期待される結果] : インストールディレクトリが取得できること
         */
        @Test
        public void createDefaultPluginDirPath_001() {
            Path result = AppPathUtils.createDefaultPluginDirPath();
            Path expect = Paths.get("").normalize().toAbsolutePath().resolve("plugin");
            assertThat(result, is(expect));
        }

    }

}

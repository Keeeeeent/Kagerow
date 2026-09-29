package com.sakulabo.core.Common;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.sakulabo.BaseTest;
import com.sakulabo.BaseTest.KagerowContainerRunner;

@ExtendWith(KagerowContainerRunner.class)
public class StringUtilsTest extends BaseTest {

    @Nested
    @ExtendWith(KagerowSystemPropertyRunner.class)
    public class StringUtils_toClassName extends BaseTest {

        /**
         * [試験観点] : 引数を指定
         * [期待される結果] : クラス名が返却されること
         */
        @Test
        public void getBasePath_001() {
            String result = StringUtils.toClassName(this);
            assertThat(result, is("com.sakulabo.core.Common.StringUtilsTest.StringUtils_toClassName"));
        }

        /**
         * [試験観点] : 引数にnullを指定
         * [期待される結果] : 文字列nullが返却されること
         */
        @Test
        public void getBasePath_002() {
            String result = StringUtils.toClassName(null);
            assertThat(result, is("null"));
        }

    }

}

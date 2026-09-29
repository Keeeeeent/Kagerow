package com.sakulabo.core.Common;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.sakulabo.BaseTest;
import com.sakulabo.BaseTest.KagerowContainerRunner;

@ExtendWith(KagerowContainerRunner.class)
public class NamingEnumerationImplTest extends BaseTest {

    @Nested
    @ExtendWith(KagerowSystemPropertyRunner.class)
    public class NamingEnumerationImpl_Iterator extends BaseTest {

        private NamingEnumerationImpl<Integer> testTarget;

        @BeforeEach
        public void refresh() {
            testTarget = new NamingEnumerationImpl<>(List.of(1, 1, 1, 1, 1));
        }

        /**
         * [試験観点] : 要素数以内の呼び出し
         * [期待される結果] : 例外が発生しないこと、結果がtrueであること
         */
        @Test
        @RepeatedTest(5)
        public void hasMore_001() throws Throwable {
            assertThat(testTarget.hasMore(), is(true));
            testTarget.next();
        }

        /**
         * [試験観点] : 要素数オーバーの呼び出し
         * [期待される結果] : 例外が発生しないこと、結果がfalseであること
         */
        @Test
        public void hasMore_002() throws Throwable {
            for (int i = 0; i < 5; i++) {
                testTarget.next();
            }
            assertThat(testTarget.hasMore(), is(false));
        }

        /**
         * [試験観点] : 要素数以内の呼び出し
         * [期待される結果] : 例外が発生しないこと、結果が1であること
         */
        @Test
        @RepeatedTest(5)
        public void next_001() throws Throwable {
            assertThat(testTarget.next(), is(1));
        }

        /**
         * [試験観点] : 要素数オーバーの呼び出し
         * [期待される結果] : 例外が発生すること
         */
        @Test
        public void next_002() throws Throwable {
            for (int i = 0; i < 5; i++) {
                testTarget.next();
            }
            assertThrows(NoSuchElementException.class, () -> testTarget.next());
        }

        /**
         * [試験観点] : 終了処理
         * [期待される結果] : 例外が発生しないこと
         */
        @Test
        public void close_001() throws Throwable {
            testTarget.close();
        }

    }

    @Nested
    @ExtendWith(KagerowSystemPropertyRunner.class)
    public class NamingEnumerationImpl_Element extends BaseTest {

        private NamingEnumerationImpl<Integer> testTarget;

        @BeforeEach
        public void refresh() {
            testTarget = new NamingEnumerationImpl<>(List.of(1, 1, 1, 1, 1));
        }

        /**
         * [試験観点] : 要素数以内の呼び出し
         * [期待される結果] : 例外が発生しないこと、結果がtrueであること
         */
        @Test
        @RepeatedTest(5)
        public void hasMore_001() throws Throwable {
            assertThat(testTarget.hasMoreElements(), is(true));
            testTarget.nextElement();
        }

        /**
         * [試験観点] : 要素数オーバーの呼び出し
         * [期待される結果] : 例外が発生しないこと、結果がfalseであること
         */
        @Test
        public void hasMore_002() throws Throwable {
            for (int i = 0; i < 5; i++) {
                testTarget.nextElement();
            }
            assertThat(testTarget.hasMoreElements(), is(false));
        }

        /**
         * [試験観点] : 要素数以内の呼び出し
         * [期待される結果] : 例外が発生しないこと、結果が1であること
         */
        @Test
        @RepeatedTest(5)
        public void next_001() throws Throwable {
            assertThat(testTarget.nextElement(), is(1));
        }

        /**
         * [試験観点] : 要素数オーバーの呼び出し
         * [期待される結果] : 例外が発生すること
         */
        @Test
        public void next_002() throws Throwable {
            for (int i = 0; i < 5; i++) {
                testTarget.nextElement();
            }
            assertThrows(NoSuchElementException.class, () -> testTarget.nextElement());
        }

        /**
         * [試験観点] : 終了処理
         * [期待される結果] : 例外が発生しないこと
         */
        @Test
        public void close_001() throws Throwable {
            testTarget.close();
        }

    }

}

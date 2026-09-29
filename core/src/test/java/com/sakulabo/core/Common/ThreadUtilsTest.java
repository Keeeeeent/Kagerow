package com.sakulabo.core.Common;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sakulabo.BaseTest;
import com.sakulabo.BaseTest.KagerowContainerRunner;

@ExtendWith(KagerowContainerRunner.class)
public class ThreadUtilsTest extends BaseTest {

    public static final ThreadGroup TEST_GROUP = new ThreadGroup("TestGroup");

    @Nested
    public class AppPathUtilsTest_newUserThreadFactoryTest extends BaseTest {

        /**
         * [試験観点] : スレッド生成
         * [期待される結果] : 期待通りスレッドが生成されること
         */
        @Test
        public void newUserThreadFactory_001() {
            ThreadFactory result = ThreadUtils.newUserThreadFactory(TEST_GROUP);
            Thread thread = result.newThread(ThreadUtils.EMPTY_RUNNABLE);
            thread.run();
            assertThat(thread.getName(), is("TestGroup"));
            assertThat(thread.isDaemon(), is(false));
            assertThat(thread.isVirtual(), is(false));
        }

    }

    @Nested
    public class AppPathUtilsTest_newDemonThreadFactoryTest extends BaseTest {

        /**
         * [試験観点] : スレッド生成
         * [期待される結果] : 期待通りデーモンスレッドが生成されること
         */
        @Test
        public void newDemonThreadFactory_001() {
            ThreadFactory result = ThreadUtils.newDemonThreadFactory(TEST_GROUP);
            Thread thread = result.newThread(ThreadUtils.EMPTY_RUNNABLE);
            thread.run();
            assertThat(thread.getName(), is("TestGroup"));
            assertThat(thread.isDaemon(), is(true));
            assertThat(thread.isVirtual(), is(false));
        }

    }

    @Nested
    public class AppPathUtilsTest_newVirtualThreadFactoryTest extends BaseTest {

        /**
         * [試験観点] : スレッド生成
         * [期待される結果] : 期待通りバーチャルスレッドが生成されること
         */
        @Test
        public void newVirtualThreadFactory_001() {
            ThreadFactory result = ThreadUtils.newVirtualThreadFactory(TEST_GROUP);
            Thread thread = result.newThread(ThreadUtils.EMPTY_RUNNABLE);
            thread.run();
            assertThat(thread.getName(), is("TestGroup"));
            assertThat(thread.isDaemon(), is(true));
            assertThat(thread.isVirtual(), is(true));
        }

    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    public class AppPathUtilsTest_addShutdownHookTest extends BaseTest {

        @Captor
        private ArgumentCaptor<Thread> thread;
        @Mock
        private ExecutorService executor;
        @Mock
        private Runtime runtimeMock;

        /**
         * [試験観点] : スレッド生成
         * [期待される結果] : 期待通りバーチャルスレッドが生成されること
         */
        @Test
        public void addShutdownHook_001() {
            try (MockedStatic<Runtime> runtime = Mockito.mockStatic(Runtime.class)) {
                runtime.when(Runtime::getRuntime).thenReturn(runtimeMock);
                ThreadUtils.addShutdownHook(executor);
                verify(runtimeMock).addShutdownHook(thread.capture());
                assertThat(thread.getValue().getClass().getSimpleName(), is("endHook"));
                thread.getValue().run();
                verify(executor, times(1)).shutdownNow();
            }
        }

    }

}

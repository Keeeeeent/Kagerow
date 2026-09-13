package com.sakulabo.core.Processor.aop;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Proxy;

import org.junit.jupiter.api.Test;

import com.sakulabo.core.Kagerow.Utilities.KagerowAOP;
import com.sakulabo.core.Kagerow.Utilities.KagerowAOPProcessors;

/**
 * Proxyのハンドラ実装を提供するクラスのテストクラスです
 */
public class CommonAOPInvocationHandlProcessorTest {

	/** テスト対象 */
	private CommonAOPInvocationHandlProcessor processor;

	/** モックインターフェイス */
	private interface TestIF {
		@KagerowAOP(KagerowAOPProcessors.SIMPLE)
		String Test();

		@KagerowAOP(KagerowAOPProcessors.SIMPLE)
		default String Args(String args) {
			return args;
		}

		@KagerowAOP(value = KagerowAOPProcessors.SIMPLE, startAOP = false)
		default String Start(String args) {
			return args;
		}

		@KagerowAOP(value = KagerowAOPProcessors.SIMPLE, endAOP = false)
		default String End(String args) {
			return args;
		}

		@KagerowAOP(value = KagerowAOPProcessors.SIMPLE, errorAOP = false)
		default String Error(String args) {
			throw new RuntimeException();
		}
	}

	/**
	 * [試験観点] : AOP対象インスタンスを指定、返却値がnull以外
	 * [期待される結果] : 正常終了すること
	 */
	@Test
	public void Test001() {

		// モッククラス生成
		TestIF mock = new TestIF() {
			@Override
			public String Test() {
				return "Test";
			}
		};

		// テスト対象クラス生成
		try {

			this.processor = new CommonAOPInvocationHandlProcessor(mock);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// proxy生成
		TestIF proxy = (TestIF) Proxy.newProxyInstance(TestIF.class.getClassLoader(),
				processor.getAOPInterface(), processor);

		// テスト実行
		String result = (String) proxy.Test();

		// 結果比較
		assertThat(result, is("Test"));
		assertThat(processor.getInstance(), is(not(nullValue())));
		assertEquals(processor.getAOPInterface().length, 1);
		assertEquals(processor.getAOPProcessor().length, 1);
		assertEquals(processor.getInterface().length, 1);
		assertTrue(processor.isAOPInstance());

	}

	/**
	 * [試験観点] : AOP対象インスタンスを指定、返却値がnull
	 * [期待される結果] : 正常終了すること
	 */
	@Test
	public void Test002() {

		// モッククラス生成
		TestIF mock = new TestIF() {
			@Override
			public String Test() {
				return null;
			}
		};

		// テスト対象クラス生成
		this.processor = new CommonAOPInvocationHandlProcessor(mock);

		// proxy生成
		TestIF proxy = (TestIF) Proxy.newProxyInstance(TestIF.class.getClassLoader(),
				processor.getAOPInterface(), processor);

		// テスト実行
		String result = (String) proxy.Test();

		// 結果比較
		assertThat(result, is(nullValue()));

	}

	/**
	 * [試験観点] : AOP対象インスタンスを指定、例外が発生
	 * [期待される結果] : ログが出力され例外が発生すること
	 */
	@Test
	public void Test003() {

		// モッククラス生成
		final RuntimeException exception = new RuntimeException();
		TestIF mock = new TestIF() {
			@Override
			public String Test() {
				throw exception;
			}
		};

		// テスト対象クラス生成
		this.processor = new CommonAOPInvocationHandlProcessor(mock);

		// proxy生成
		TestIF proxy = (TestIF) Proxy.newProxyInstance(TestIF.class.getClassLoader(),
				processor.getAOPInterface(), processor);

		// テスト実行
		try {
			proxy.Test();
			fail();
		} catch (RuntimeException e) {
			;
		}

	}

	/**
	 * [試験観点] : AOP対象インスタンスを指定、引数あり
	 * [期待される結果] : 引数が認識されていること
	 */
	@Test
	public void Test004() {

		// モッククラス生成
		TestIF mock = new TestIF() {
			@Override
			public String Test() {
				return null;
			}
		};

		// テスト対象クラス生成
		this.processor = new CommonAOPInvocationHandlProcessor(mock);

		// proxy生成
		TestIF proxy = (TestIF) Proxy.newProxyInstance(TestIF.class.getClassLoader(),
				processor.getAOPInterface(), processor);

		// テスト実行
		String result = (String) proxy.Args("Test");

		// 結果比較
		assertThat(result, is("Test"));

	}

	/**
	 * [試験観点] : AOP停止（スタート）
	 * [期待される結果] : 正常終了すること
	 */
	@Test
	public void Test005() {

		// モッククラス生成
		TestIF mock = new TestIF() {
			@Override
			public String Test() {
				return null;
			}
		};

		// テスト対象クラス生成
		this.processor = new CommonAOPInvocationHandlProcessor(mock);

		// proxy生成
		TestIF proxy = (TestIF) Proxy.newProxyInstance(TestIF.class.getClassLoader(),
				processor.getAOPInterface(), processor);

		// テスト実行
		String result = (String) proxy.Start("Test");

		// 結果比較
		assertThat(result, is("Test"));

	}

	/**
	 * [試験観点] : AOP停止（エンド）
	 * [期待される結果] : 正常終了すること
	 */
	@Test
	public void Test006() {

		// モッククラス生成
		TestIF mock = new TestIF() {
			@Override
			public String Test() {
				return null;
			}
		};

		// テスト対象クラス生成
		this.processor = new CommonAOPInvocationHandlProcessor(mock);

		// proxy生成
		TestIF proxy = (TestIF) Proxy.newProxyInstance(TestIF.class.getClassLoader(),
				processor.getAOPInterface(), processor);

		// テスト実行
		String result = (String) proxy.End("Test");

		// 結果比較
		assertThat(result, is("Test"));

	}

	/**
	 * [試験観点] : AOP停止（エラー）
	 * [期待される結果] : ログが出力され例外が発生すること
	 */
	@Test
	public void Test007() {

		// モッククラス生成
		TestIF mock = new TestIF() {
			@Override
			public String Test() {
				return null;
			}
		};

		// テスト対象クラス生成
		this.processor = new CommonAOPInvocationHandlProcessor(mock);

		// proxy生成
		TestIF proxy = (TestIF) Proxy.newProxyInstance(TestIF.class.getClassLoader(),
				processor.getAOPInterface(), processor);

		// テスト実行
		try {
			proxy.Error("Test");
			fail();
		} catch (RuntimeException e) {
			;
		}

	}
}

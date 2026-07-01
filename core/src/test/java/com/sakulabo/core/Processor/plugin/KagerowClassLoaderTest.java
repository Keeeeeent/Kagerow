package com.sakulabo.core.Processor.plugin;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.sakulabo.BaseTest;
import com.sakulabo.core.Kagerow.Exception.ApplicationError;

/**
 * アプリケーション共通で使用されるプラグイン専用クラスローダーのテストクラスです 
 */
@SuppressWarnings("javadoc")
public class KagerowClassLoaderTest extends BaseTest<KagerowClassLoader> {

	/**
	 * デフォルトコンストラクタ
	 */
	protected KagerowClassLoaderTest() {
		super(KagerowClassLoaderTest.class);
	}

	@BeforeEach
	void initService() {
	}

	@AfterEach
	void closeService() {
	}

	/**
	 * [試験観点]      : karファイル読み込み
	 * [期待される結果] : 以下である
	 *                  ・正常終了すること
	 *                  ・クラスロードができていること
	 */
	@Test
	@Disabled
	public void Test001() throws Throwable {
		// karファイルが読み込みできている
		KagerowClassLoader target = new KagerowClassLoader(testDir.resolve("test.plugin"));
		Class<?> clazz = target.loadClass("com.sakulabo.library.common.FileDefaultOutputer");
		// Class情報がnullではない
		assertThat(clazz, is(not(nullValue())));
		// クラスローダーが期待通りである
		assertThat(clazz.getClassLoader(), is(target));
	}

	/**
	 * [試験観点]      : karファイル読み込み
	 * [期待される結果] : リソースが正常に読み込めること
	 */
	@Test
	@Disabled
	public void Test002() throws Throwable {
		// karファイルが読み込みできている
		@SuppressWarnings("resource")
		KagerowClassLoader target = new KagerowClassLoader(testDir.resolve("test.plugin"));
		// リソースロードができている
		String line = null;
		try (
				InputStream input = target.getResourceAsStream("config/message/app-logger.properties");
				InputStreamReader converter = new InputStreamReader(input);
				BufferedReader reader = new BufferedReader(converter);) {
			while ((line = reader.readLine()) != null) {
				if (line.contains("com.sakulabo.core.Logger.AppLogger")) {
					break;
				} else {
					line = null;
				}
			}
		}
		// ロード結果の検証
		assertThat(line, is(containsString("com.sakulabo.core.Logger.AppLogger")));
	}

	/**
	 * [試験観点]      : スレッドの生成
	 * [期待される結果] : 以下の状態でスレッドが生成できている
	 *                ・デーモンスレッドで生成されている
	 *                ・スレッドの名称が期待通り
	 *                ・クラスローダーが期待通り
	 */
	@Test
	@Disabled
	public void Test003() throws Throwable {
		// クラスローダー生成
		KagerowClassLoader target = new KagerowClassLoader(testDir.resolve("test.plugin"));
		// スレッドの生成
		Thread thread = target.currentThread("test");
		// デーモンスレッドで生成されている
		assertThat(thread, is(not(nullValue())));
		assertTrue(thread.isDaemon());
		// スレッドの名称が期待通り
		assertThat(thread.getName(), is("PluginThread@test-1"));
		// クラスローダーが期待通り
		assertThat(thread.getContextClassLoader(), is(target));
	}

	/**
	 * [試験観点]      : ファイル読み取り失敗
	 * [期待される結果] : アプリケーションエラーがスローされる
	 */
	@Test
	@Disabled
	@SuppressWarnings("resource")
	public void Test004() throws Throwable {
		try {
			// クラスローダー生成
			new KagerowClassLoader(testDir.resolve("test.nonplugin"));
			fail("テスト失敗");
		} catch (ApplicationError e) {
			assertThat(e.getThrowClass(), is(KagerowClassLoader.class));
		}

	}

}

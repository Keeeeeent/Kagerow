package com.sakulabo.core.Provides;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import javax.naming.Binding;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sakulabo.BaseTest;
import com.sakulabo.core.Kagerow.Exception.AlreadyLoadedBeanException;
import com.sakulabo.core.Kagerow.Utilities.KagerowAOP;
import com.sakulabo.core.Kagerow.Utilities.KagerowAOPProcessors;
import com.sakulabo.core.Provides.LoardDIBeansProvider.Key;

/**
 * DIのBeanロード実装を提供するプロバイダクラスのテストクラスです
 * 
 * @author keeeeeent
 */
@SuppressWarnings("javadoc")
public class LoardDIBeansProviderTest extends BaseTest<LoardDIBeansProvider> {

	/**
	 * デフォルトコンストラクタ
	 */
	protected LoardDIBeansProviderTest() {
		super(LoardDIBeansProviderTest.class);
	}

	@BeforeEach
	void initService() {
	}

	@AfterEach
	void closeService() throws Exception {
	}

	/** テスト対象 */
	private LoardDIBeansProvider provider;

	/** モックインターフェイス */
	public interface LoardDIBeansProviderMockIF {
		@KagerowAOP(KagerowAOPProcessors.APPLOGGER)
		String Test();

		@KagerowAOP(KagerowAOPProcessors.APPLOGGER)
		default String Args(String args) {
			return args;
		}

		@KagerowAOP(value = KagerowAOPProcessors.APPLOGGER, startAOP = false)
		default String Start(String args) {
			return args;
		}

		@KagerowAOP(value = KagerowAOPProcessors.APPLOGGER, endAOP = false)
		default String End(String args) {
			return args;
		}

		@KagerowAOP(value = KagerowAOPProcessors.APPLOGGER, errorAOP = false)
		default String Error(String args) {
			throw new RuntimeException();
		}

	}

	/**
	 * [試験観点]      : Bean生成
	 * [期待される結果] : 正常終了すること
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void Test001() {

		// テスト対象クラス生成
		provider = new LoardDIBeansProvider();
		// コンテキスト生成
		Map<Binding, Object> context = new HashMap<>();
		// Beanロード
		Optional<LoardDIBeansProviderMock> bean = (Optional<LoardDIBeansProviderMock>) provider
				.load(LoardDIBeansProviderMock.class);
		// Bean登録
		provider.regist(context, bean.get());

		// 結果比較
		assertThat(context.values().size(), is(2));

		for (Entry<Binding, Object> entry : context.entrySet()) {
			Binding key = entry.getKey();
			if (key.getClassName().equals(LoardDIBeansProviderMock.class.getName())) {
				assertThat(key.getClassName(), is(LoardDIBeansProviderMock.class.getName()));
			} else {
				assertThat(key.getClassName(), is(LoardDIBeansProviderMockIF.class.getName()));
			}
			assertThat(key.getName(), is("default"));
			assertThat(key.getObject(), is(bean.get()));
		}

	}

	/**
	 * [試験観点]      : Bean検索(クラス検索)
	 * [期待される結果] : 正常終了すること
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void Test002() {

		// テスト対象クラス生成
		provider = new LoardDIBeansProvider();
		// コンテキスト生成
		Map<Binding, Object> context = new HashMap<>();
		// Beanロード
		Optional<LoardDIBeansProviderMock> bean = (Optional<LoardDIBeansProviderMock>) provider
				.load(LoardDIBeansProviderMock.class);
		// Bean登録
		provider.regist(context, bean.get());

		// 検索キー生成
		Key key = new Key("default", LoardDIBeansProviderMock.class.getName(), null);
		// コンテキスト検索
		Optional<LoardDIBeansProviderMock> target = (Optional<LoardDIBeansProviderMock>) provider.lookUp(context, key);

		// 結果比較
		assertTrue(target.isPresent());

	}

	/**
	 * [試験観点]      : Bean検索(インターフェイス検索)
	 * [期待される結果] : 正常終了すること
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void Test003() {

		// テスト対象クラス生成
		provider = new LoardDIBeansProvider();
		// コンテキスト生成
		Map<Binding, Object> context = new HashMap<>();
		// Beanロード
		Optional<LoardDIBeansProviderMock> bean = (Optional<LoardDIBeansProviderMock>) provider
				.load(LoardDIBeansProviderMock.class);
		// Bean登録
		provider.regist(context, bean.get());

		// 検索キー生成
		Key key = new Key("default", LoardDIBeansProviderMockIF.class.getName(), null);
		// コンテキスト検索
		Optional<LoardDIBeansProviderMock> target = (Optional<LoardDIBeansProviderMock>) provider.lookUp(context, key);

		// 結果比較
		assertTrue(target.isPresent());

	}

	/**
	 * [試験観点]      : Bean検索(検索不可)
	 * [期待される結果] : 正常終了すること
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void Test004() {

		// テスト対象クラス生成
		provider = new LoardDIBeansProvider();
		// コンテキスト生成
		Map<Binding, Object> context = new HashMap<>();
		// Beanロード
		Optional<LoardDIBeansProviderMock> bean = (Optional<LoardDIBeansProviderMock>) provider
				.load(LoardDIBeansProviderMock.class);
		// Bean登録
		provider.regist(context, bean.get());

		// 検索キー生成
		Key key = new Key("default", Object.class.getName(), null);
		// コンテキスト検索
		Optional<LoardDIBeansProviderMock> target = (Optional<LoardDIBeansProviderMock>) provider.lookUp(context, key);

		// 結果比較
		assertTrue(target.isEmpty());

	}

	/**
	 * [試験観点]      : Bean生成、名称付き、Bean内部初期化
	 * [期待される結果] : 正常終了すること
	 * @throws Exception 
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void Test005() throws Exception {

		// テスト対象クラス生成
		provider = new LoardDIBeansProvider();
		// コンテキスト生成
		Map<Binding, Object> context = (Map<Binding, Object>) provider.createContext();

		try {
			// インジェクションインスタンス登録
			Optional<LoardDIBeansProviderMock> logger = (Optional<LoardDIBeansProviderMock>) provider
					.load(LoardDIBeansProviderMock.class);
			provider.regist(context, logger.get());

			// Beanロード
			Optional<LoardDIBeansProviderInject> bean = (Optional<LoardDIBeansProviderInject>) provider
					.load(LoardDIBeansProviderInject.class);
			// Bean登録
			provider.regist(context, bean.get());
		} catch (Exception e) {
			e.printStackTrace();
		}

		// 検索キー生成
		Key key = new Key("mock", LoardDIBeansProviderInject.class.getName(), null);
		// コンテキスト検索
		Optional<LoardDIBeansProviderMockIF> target = (Optional<LoardDIBeansProviderMockIF>) provider
				.lookUp(context, key);
		// インジェクション実行
		synchronized (this) {
			provider.init();
		}

		// 結果比較
		assertTrue(target.isPresent());
		assertThat(target.get().Test(), is(not(nullValue())));

		try {
			target.get().Error("error");
			fail();
		} catch (Exception e) {
			;
		}

	}

	/**
	 * [試験観点]      : 重複Bean登録
	 * [期待される結果] : アプリケーションが異常終了すること
	 * @throws Exception 
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void Test006() {

		// テスト対象クラス生成
		provider = new LoardDIBeansProvider();
		// コンテキスト生成
		Map<Binding, Object> context = new HashMap<>();
		// Beanロード
		Optional<LoardDIBeansProviderInject> bean = (Optional<LoardDIBeansProviderInject>) provider
				.load(LoardDIBeansProviderMock.class);
		provider.regist(context, bean.get());

		// Bean登録
		try {
			provider.regist(context, bean.get());
			fail();
		} catch (AlreadyLoadedBeanException e) {
			;
		}

	}

	/**
	 * [試験観点]      : Bean失敗登録
	 * [期待される結果] : アプリケーションが異常終了すること
	 * @throws Exception 
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void Test007() {

		// テスト対象クラス生成
		provider = new LoardDIBeansProvider();
		// コンテキスト生成
		Map<Binding, Object> context = new HashMap<>();

		// Beanロード
		Optional<LoardDIBeansProviderMockFail> bean = (Optional<LoardDIBeansProviderMockFail>) provider
				.load(LoardDIBeansProviderMockFail.class);
		provider.regist(context, bean.get());

		// Bean登録
		try {
			provider.regist(context, bean.get());
			fail();
		} catch (AlreadyLoadedBeanException e) {
			;
		}

	}

	/**
	 * [試験観点]      : Bean生成、Bean対象外
	 * [期待される結果] : 正常終了すること
	 * @throws Exception 
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void Test008() throws Exception {

		// テスト対象クラス生成
		provider = new LoardDIBeansProvider();

		// Beanロード
		Optional<Object> bean = (Optional<Object>) provider.load(Object.class);

		// 結果比較
		assertTrue(bean.isEmpty());

	}

	/**
	 * [試験観点]      : Bean尊く、Bean対象外
	 * [期待される結果] : 正常終了すること
	 * @throws Exception 
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void Test009() throws Exception {

		// テスト対象クラス生成
		provider = new LoardDIBeansProvider();
		// コンテキスト生成
		Map<Binding, Object> context = new HashMap<>();

		// インスタンス生成
		Object obj = new Object();

		// Bean登録
		provider.regist(context, obj);

		// 検索キー生成
		Key key = new Key("default", obj.getClass().getName(), null);
		// コンテキスト検索
		Optional<Object> target = (Optional<Object>) provider.lookUp(context, key);

		// 結果比較
		assertTrue(target.isPresent());
		assertThat(obj, is(target.get()));

	}

	/**
	 * [試験観点]      : Bean生成中に例外発生
	 * [期待される結果] : 正常終了すること、空のOptionalが返却されること
	 * @throws Exception 
	 */
	@Test
	public void Test010() throws Exception {

		// テスト対象クラス生成
		provider = new LoardDIBeansProvider();

		// Beanロード
		Optional<?> result = provider.load(LoardDIBeansProviderMockException.class);

		// 結果比較
		assertTrue(result.isEmpty());

	}

	/**
	 * [試験観点]      : Key判定ロジックの妥当性
	 * [期待される結果] : 期待通り動作すること
	 * @throws Exception 
	 */
	@Test
	public void Test011() throws Exception {

		// インスタンス生成
		Object obj = new Object();

		// テスト対象クラス生成
		Key key = new Key("default", obj.getClass().getName(), null);
		Key key2 = new Key("mock", obj.getClass().getName(), null);
		Key key3 = new Key("default", "mock", null);

		// 結果比較
		assertTrue(key.equals(key));
		assertFalse(key.equals(obj));
		assertFalse(key.equals(null));

		assertFalse(key.equals(key2));
		assertFalse(key2.equals(key));
		assertFalse(key.equals(key3));
		assertFalse(key3.equals(key));

	}
}

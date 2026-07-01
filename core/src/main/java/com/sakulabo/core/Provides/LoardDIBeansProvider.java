package com.sakulabo.core.Provides;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.ServiceLoader.Provider;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.naming.Binding;

import com.sakulabo.core.Common.StringUtils;
import com.sakulabo.core.Kagerow.KagerowApplication;
import com.sakulabo.core.Kagerow.Adapter.KagerowDIContextAdapter;
import com.sakulabo.core.Kagerow.Exception.AlreadyLoadedBeanException;
import com.sakulabo.core.Kagerow.Exception.BeanNotFoundException;
import com.sakulabo.core.Kagerow.Utilities.KagerowConfiguration;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;
import com.sakulabo.core.Processor.aop.CommonAOPInvocationHandlProcessor;
import com.sakulabo.core.Processor.jmx.AppJMX;
import com.sakulabo.core.Processor.jmx.AppJMXInitializer;
import com.sakulabo.core.Processor.jmx.Context.LoardDIBeansProviderMXBean;
import com.sakulabo.regulation.annotation.KagerowComponent;
import com.sakulabo.regulation.annotation.KagerowComponent.AppInitComponet;
import com.sakulabo.regulation.annotation.KagerowInject;
import com.sakulabo.regulation.spi.InitDIBeansProcessorAdapter;
import com.sakulabo.regulation.spi.LoardDIBeansAdapter;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * DIのBeanロード実装を提供するプロバイダクラスです
 * 
 * @author keeeeeent
 */
@AppJMX(name = "Context", options = { "type=DIContext" })
public final class LoardDIBeansProvider
		implements LoardDIBeansAdapter<Binding>,
		Iterable<Map.Entry<Binding, Object>>,
		LoardDIBeansProviderMXBean {

	/**
	 * Bean格納コンテキスト
	 * 容量を32ベクトル、拡張閾値を90%に設定
	 */
	private final static Map<Binding, Object> CONTEXT = new ConcurrentHashMap<Binding, Object>(32, 0.9f);

	/**
	 * Beanロード判定アダプター
	 */
	private volatile static KagerowDIContextAdapter diContextAdapter = new DefaultDiContextAdapter();

	/**
	 * デフォルトのBeanロード判定アダプターです
	 */
	private static class DefaultDiContextAdapter implements KagerowDIContextAdapter {

		/** {@inheritDoc} */
		@Override
		public boolean isLord(Class<?> target) {
			return true;
		}

	}

	/**
	 * Bean一覧をキーと共に返却する機構備えたクラスです
	 */
	private final static class Value implements Iterator<Entry<Binding, Object>> {

		/** イテレート対象 */
		private final List<Entry<Binding, Object>> values;
		/** カウンター */
		private int count;

		/**
		 * デフォルトのコンストラクタです
		 * @param map コンテキストへの参照
		 */
		Value(Map<Binding, Object> map) {
			values = new ArrayList<>(map.entrySet());
		}

		/** {@inheritDoc} */
		@Override
		public boolean hasNext() {
			return count < values.size();
		}

		/** {@inheritDoc} */
		@Override
		public Entry<Binding, Object> next() {
			return values.get(count++);
		}

	}

	/**
	 * Beanを識別するためのキーとなるクラス 
	 */
	public final static class Key extends Binding {

		/**
		 * デフォルトのコンストラクタ
		 * @param name Bean名称
		 * @param className クラス名称
		 * @param obj Beanインスタンス
		 */
		public Key(String name, String className, Object obj) {
			super(name, className, obj);
		}

		/** {@inheritDoc} */
		@Override
		public boolean equals(Object obj) {
			if (Objects.nonNull(obj)) {
				if (obj instanceof Key key) {
					String baseName = getName(), className = getClassName();
					String actName = key.getName(), actClassName = key.getClassName();
					if (baseName.equals(actName) && className.equals(actClassName)) {
						return true;
					}
				}
			}
			return false;
		}

		/** {@inheritDoc} */
		@Override
		public int hashCode() {
			return Objects.hash(getName(), getClassName());
		}
	}

	/**
	 * コンテキストをKagerow向けに初期化します<br/>
	 * ※呼び出しの時点ででアプリケーションコンテキストは生成済みである必要があります
	 * @param diContextAdapter Beanロード判定機能アダプター
	 */
	public final static void initContext(KagerowDIContextAdapter diContextAdapter) {
		// アダプター初期化
		if (Objects.nonNull(diContextAdapter)) {
			LoardDIBeansProvider.diContextAdapter = diContextAdapter;
		}
		// 一時格納先初期化
		Object target = null;
		// アプリケーションインスタンス登録
		// ロガーインスタンス登録
		target = KagerowLogger.newAppLogger();
		CONTEXT.put(toBinding(KagerowLogger.class, target), target);
		// コンフィグレーションインスタンス登録
		target = KagerowConfiguration.getConfiguration();
		CONTEXT.put(toBinding(KagerowConfiguration.class, target), target);
	}

	/**
	 * エントリーポイントの発火処理を実行します
	 */
	public final void startEntryPoint() {
		Class<?> clazz = KagerowApplication.getConfig().APP_ENTRY_POINT();
		if (clazz != AppInitComponet.DefaultInitComponet.class) {
			Key key = new Key(StringUtils.DEFAULT, clazz.getName(), null);
			Optional<?> target = lookUp(CONTEXT, key);
			if (target.isPresent()) {
				AppInitComponet initComponet = (AppInitComponet) target.get();
				initComponet.initialize();
			}
		}
	}

	/**
	 * クラス定義から検索キーを生成します
	 * @param clazz クラス宣言
	 * @param bean インスタンス
	 * @return キー
	 */
	private final static Binding toBinding(Class<?> clazz, Object bean) {
		return new Key(StringUtils.DEFAULT, clazz.getName(), bean);
	}

	/** {@inheritDoc} */
	@Override
	public Map<Binding, ?> createContext() {
		return CONTEXT;
	}

	/** {@inheritDoc} */
	@Override
	public Optional<?> load(Class<?> target) {

		// ロード対象か判定
		if (!diContextAdapter.isLord(target)) {
			return Optional.empty();
		}

		try {
			// 返却値を初期化
			Object bean = null;
			// Beanインスタンスの対象か判定
			KagerowComponent beanClass = target.getDeclaredAnnotation(KagerowComponent.class);
			if (Objects.nonNull(beanClass)) {
				/**
				 * デフォルトコンストラクタを実行しインスタンスを生成
				 * AppComponentアノテーションが付与されているクラスは、
				 * デフォルトコンストラクタの存在が保証されている。
				 * この挙動はAPTにて提供される。
				 */
				bean = target.getDeclaredConstructor().newInstance();
			}
			return Optional.ofNullable(bean);
		} catch (Exception e) {
			KagerowLogger.newAppLogger().err(e);
			return Optional.empty();
		}

	}

	/** {@inheritDoc} */
	@Override
	@SuppressWarnings("unchecked")
	public void regist(Map<Binding, ?> context, Object bean) {

		// 名称の生成
		KagerowComponent component = bean.getClass().getDeclaredAnnotation(KagerowComponent.class);
		String name = Objects.nonNull(component) ? component.value() : StringUtils.DEFAULT;
		// 変数初期化
		Key key = null;
		// Proxy対象インスタンスでラップ
		Object proxy = wrapProxy(bean);

		/**
		 * クラスベースのインスタンス
		 */
		// コンテキスト登録のキーを生成
		key = new Key(name, bean.getClass().getName(), bean);
		if (context.containsKey(key)) {
			// Beanが衝突した場合例外をスロー
			throw new AlreadyLoadedBeanException(key);
		} else {
			// コンテキストへ登録
			((Map<Binding, Object>) context).put(key, bean);
		}

		/**
		 * インターフェイスベースのインスタンス（プロキシー）
		 */
		Optional<CommonAOPInvocationHandlProcessor> handler = getRaw(proxy);
		if (handler.isPresent()) {
			for (Class<?> interf : handler.get().getAOPInterface()) {
				// コンテキスト登録のキーを生成
				key = new Key(name, interf.getName(), bean);
				if (context.containsKey(key)) {
					// Beanが衝突した場合例外をスロー
					throw new AlreadyLoadedBeanException(key);
				} else {
					// コンテキストへ登録
					((Map<Binding, Object>) context).put(key, proxy);
				}
			}
		}

		/**
		 * インターフェイスベースのインスタンス（RAW）
		 */
		List<Class<?>> interfAop = Collections.emptyList();
		if (handler.isPresent()) {
			interfAop = Arrays.asList(handler.get().getAOPInterface());
		}
		for (Class<?> interf : bean.getClass().getInterfaces()) {
			if (interfAop.contains(interf)) {
				continue;
			}
			// コンテキスト登録のキーを生成
			key = new Key(name, interf.getName(), bean);
			if (context.containsKey(key)) {
				// Beanが衝突した場合例外をスロー
				throw new AlreadyLoadedBeanException(key);
			} else {
				// コンテキストへ登録
				((Map<Binding, Object>) context).put(key, bean);
			}
		}

	}

	/**
	 * Proxyインスタンスを生成します
	 * @param rawInstance 生のインスタンス
	 * @return Proxyインスタンス
	 */
	private Object wrapProxy(Object rawInstance) {
		// ハンドラ生成
		CommonAOPInvocationHandlProcessor handler = new CommonAOPInvocationHandlProcessor(rawInstance);
		Object proxy;
		if (handler.isAOPInstance()) {
			// AOP対象の場合
			proxy = Proxy.newProxyInstance(rawInstance.getClass().getClassLoader(),
					handler.getAOPInterface(), handler);
		} else {
			// AOP非対象の場合
			proxy = rawInstance;
		}
		return proxy;
	}

	/**
	 * Proxyインスタンスから生のインスタンスを抽出します
	 * @param proxy プロキシ
	 * @return 生のインスタンス
	 */
	private Optional<CommonAOPInvocationHandlProcessor> getRaw(Object proxy) {
		if (!Proxy.isProxyClass(proxy.getClass())) {
			return Optional.empty();
		}
		CommonAOPInvocationHandlProcessor raw = (CommonAOPInvocationHandlProcessor) Proxy.getInvocationHandler(proxy);
		return Optional.of(raw);
	}

	/** {@inheritDoc} */
	@Override
	public Optional<?> lookUp(Map<Binding, ?> context, Binding key) {
		// 対象を取得
		Object value = context.get(key);
		return Optional.ofNullable(value);
	}

	/**
	 * Beanコンテナを使用可能な状態に初期化します
	 * @throws Exception Bean生成時に想定外のエラーが発生した場合
	 */
	@Override
	@SuppressFBWarnings(justification = "想定通りのwait呼び出しのため問題なしとする", value = {
			"UW_UNCOND_WAIT",
			"WA_NOT_IN_LOOP"
	})
	public void init() throws Exception {

		try {
			// Beanロード処理停止
			LoardDIBeansAdapter.LOCK.lock();
			// SPIから実装を取得
			ServiceLoader<InitDIBeansProcessorAdapter> loader = ServiceLoader.load(InitDIBeansProcessorAdapter.class);
			// Bean対象のクラスを全てロード
			Class<?>[] clazz = loader.stream()
					.map(Provider<InitDIBeansProcessorAdapter>::get)
					.map(InitDIBeansProcessorAdapter::init)
					.filter(Objects::nonNull)
					.toArray(Class<?>[]::new);
			// Bean内部のInject対象クラスを全てロード
			for (Class<?> claz : clazz) {
				for (Field field : claz.getDeclaredFields()) {
					// インジェクション対象ある場合、トランスフォーマーへ通知
					KagerowInject inject = field.getAnnotation(KagerowInject.class);
					if (Objects.nonNull(inject)) {
						field.getType();
					}
				}
			}
		} catch (Exception e) {
			throw e;
		} finally {
			// Beanロード処理開始
			LoardDIBeansAdapter.LOCK.unlock();
		}

		// 初期化終了を待機
		synchronized (this) {
			wait(KagerowApplication.getConfig().WAIT_TIME().toMillis());
		}

		// Beanの構築
		for (Entry<Binding, Object> entry : this) {
			Object bean = entry.getKey().getObject();
			injection(bean, bean.getClass());
		}

		// JMXの登録
		AppJMXInitializer.registMXBean(this);

	}

	/**
	 * DIインジェクション処理を再帰的に行います
	 * @param target インジェクション先
	 * @param clazz クラス定義
	 * @throws Exception 既にバインド済みの場合、もしくはリフレクション処理失敗時
	 */
	private void injection(Object target, Class<?> clazz) throws Exception {
		for (Field field : clazz.getDeclaredFields()) {
			// インジェクション対象の取得
			KagerowInject inject = field.getAnnotation(KagerowInject.class);
			if (Objects.nonNull(inject)) {
				field.setAccessible(true);
				// Keyの生成
				Binding key = createKey(field, inject);
				// Beanの検索
				Optional<?> injectBean = lookUp(CONTEXT, key);
				// Beanが見つからなかった場合
				if (injectBean.isEmpty()) {
					throw new BeanNotFoundException(key);
				}
				// Beanが見つかった場合処理を続行
				field.set(target, injectBean.get());
			}
		}
		// 親クラスの確認
		clazz = clazz.getSuperclass();
		if (Objects.nonNull(clazz)) {
			// 親クラスがある場合、再起処理継続
			injection(target, clazz);
		} else {
			// 親クラスがない場合再起処理を終了
			return;
		}
	}

	/**
	 * 取得したフィールドからKeyを生成します
	 * @param field フィールドインスタンス
	 * @param inject インジェクションアノテーション
	 * @return 検索キー
	 */
	private Binding createKey(Field field, KagerowInject inject) {
		// Nameの取得
		String name = inject.value();
		// Typeの取得
		Class<?> typed = field.getType();
		String type = typed.getName();
		// Keyの生成
		Key key = new Key(name, type, null);
		return key;
	}

	/** {@inheritDoc} */
	@Override
	public Iterator<Entry<Binding, Object>> iterator() {
		return new Value(CONTEXT);
	}

	/** {@inheritDoc} */
	@Override
	public Map<String, String> getContext() {
		return CONTEXT.keySet().stream()
				.collect(Collectors.toMap(
						Binding::getClassName,
						Binding::getName));
	}

}

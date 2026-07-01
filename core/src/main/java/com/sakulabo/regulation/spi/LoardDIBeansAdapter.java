package com.sakulabo.regulation.spi;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

/**
 * DIのBeanロード実装を制御するためのアダプターインターフェイスです
 * @author keeeeeent
 * @param <K> 
 */
public interface LoardDIBeansAdapter<K> {

	/** ロックインスタンス */
	public final static ReentrantLock LOCK = new ReentrantLock();

	/**
	 * DIのBeanロード実装を制御するためのデフォルトの実装です<br/>
	 * このインスタンスのロード処理は全て何も行われません<br/>
	 * 原則コンテナへの処理はアダプターの実装で提供することが推奨されるため、このクラスの直接的な使用は推奨されません
	 */
	@Deprecated
	@SuppressWarnings("rawtypes")
	public final static LoardDIBeansAdapter<?> DEFAULT_ADAPTER = new LoardDIBeansAdapter() {
		/** {@inheritDoc} */
		@Override
		public Optional load(Class target) {
			return Optional.empty();
		}

		/** {@inheritDoc} */
		@Override
		public void regist(Map context, Object bean) {
			;
		}

		/** {@inheritDoc} */
		@Override
		public Optional lookUp(Map context, Object key) {
			return Optional.empty();
		}

		/** {@inheritDoc} */
		@Override
		public void init() throws Exception {
			;
		}

		/** {@inheritDoc} */
		@Override
		public String toString() {
			return "Empty process";
		};
	};

	/**
	 * Beanを保持するためのコンテキスト生成をします
	 * @return コンテキスト
	 */
	default Map<K, ?> createContext() {
		return new HashMap<>();
	}

	/**
	 * Beanインスタンスを生成する処理を、コンテナクラスに提供します
	 * @param target クラス情報
	 * @return Beanインスタンス
	 */
	Optional<?> load(Class<?> target);

	/**
	 * loadメソッドにて取得したインスタンスをコンテキストへ登録します
	 * @param context
	 * @param bean
	 */
	void regist(Map<K, ?> context, Object bean);

	/**
	 * コンテキストからBeanを検索します
	 * @param context コンテキストインスタンス
	 * @param key ルックアップキー
	 * @return 検索結果
	 */
	Optional<?> lookUp(Map<K, ?> context, K key);

	/**
	 * 初期化処理を提供します
	 * @throws Exception 初期化処理に失敗した場合
	 */
	void init() throws Exception;

}

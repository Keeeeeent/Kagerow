package com.sakulabo.core.Kagerow.Utilities;

import static com.sakulabo.core.Processor.log.CommonLogger.*;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.sakulabo.core.Kagerow.Exception.AlreadyRegistedException;
import com.sakulabo.core.Processor.log.AppLogger;
import com.sakulabo.core.Processor.log.CommonLogger;

/**
 * 横断的関心事の一覧を表す列挙クラスです
 * 
 * @author keeeeeent
 */
public enum KagerowAOPProcessors implements KagerowAOPProcessor {

	/**
	 * 最もシンプルなAOP実装です
	 */
	SIMPLE() {

		/** ロガーインスタンス */
		private final Logger logger = CommonLogger.getInstance();

		/** {@inheritDoc} */
		@Override
		public void start(Object raw, Object proxy, Method method, Object[] args) {
			String msg = createMsg("START", raw, null, method, args);
			logger.log(Level.INFO, msg);
		}

		/** {@inheritDoc} */
		@Override
		public void end(Object raw, Object result, Method method, Object[] args) {
			String msg = createMsg("END", raw, result, method, args);
			logger.log(Level.INFO, msg);
		}

		/** {@inheritDoc} */
		@Override
		public void error(Object raw, Throwable error, Method method, Object[] args) {
			logger.log(Level.INFO, error.getClass().getCanonicalName(), error);
		}

	},

	/**
	 * マーカで指定されたオブザーバーを起動します
	 */
	MARKERED() {

		/** {@inheritDoc} */
		@Override
		public void start(Object raw, Object proxy, Method method, Object[] args) {
			for (KagerowAOPProcessor processor : getObserver(method, START_FLG))
				processor.start(raw, proxy, method, args);

		}

		/** {@inheritDoc} */
		@Override
		public void end(Object raw, Object result, Method method, Object[] args) {
			for (KagerowAOPProcessor processor : getObserver(method, END_FLG))
				processor.end(raw, result, method, args);
		}

		/** {@inheritDoc} */
		@Override
		public void error(Object raw, Throwable error, Method method, Object[] args) {
			for (KagerowAOPProcessor processor : getObserver(method, ERROR_FLG))
				processor.error(raw, error, method, args);
		}

	},

	/**
	 * 共通のアプリケーションロガーを実行します
	 */
	APPLOGGER() {

		/** ロガー */
		private final AppLogger logger = AppLogger.getLogger();

		/** {@inheritDoc} */
		@Override
		public void start(Object raw, Object proxy, Method method, Object[] args) {
			logger.start(raw, proxy, method, args);
		}

		/** {@inheritDoc} */
		@Override
		public void end(Object raw, Object result, Method method, Object[] args) {
			logger.end(raw, result, method, args);
		}

		/** {@inheritDoc} */
		@Override
		public void error(Object raw, Throwable error, Method method, Object[] args) {
			logger.error(raw, error, method, args);
		}

	},

	;

	/** オブザーバーリスト */
	private final static Map<String, KagerowAOPProcessor> OBSERVER_LIST = new ConcurrentHashMap<>();
	/** スタートフラグ（AOP） */
	private final static int START_FLG = 0x01;
	/** エンドフラグ（AOP） */
	private final static int END_FLG = 0x01 << 1;
	/** エラーフラグ（AOP） */
	private final static int ERROR_FLG = 0x01 << 2;

	/**
	 * デフォルトコンストラクタ
	 */
	private KagerowAOPProcessors() {
		;
	};

	/**
	 * マーカーに関連するオブザーバーを追加します
	 * @param maker マーカー
	 * @param observer オブザーバー
	 * @throws AlreadyRegistedException 既にオブザーバーが登録済みである場合
	 * @throws NullPointerException オブザーバーがnullの場合
	 */
	public final static void addObserver(String maker, KagerowAOPProcessor observer)
			throws AlreadyRegistedException {
		Objects.requireNonNull(observer);
		if (OBSERVER_LIST.containsKey(maker)) {
			throw new AlreadyRegistedException(maker);
		}
		OBSERVER_LIST.put(maker, observer);
	}

	/**
	 * 対象インスタンスからオブザーバーを取得します
	 * @param method メソッドインスタンス
	 * @param flg 処理種別
	 * @return オブザーバーリスト
	 */
	protected List<KagerowAOPProcessor> getObserver(Method method, int flg) {
		Set<String> keySet = new HashSet<>();
		List<KagerowAOPProcessor> result = new ArrayList<>();
		for (KagerowAOP aop : method.getDeclaredAnnotationsByType(KagerowAOP.class)) {
			int aopFlg = convertFlg(aop);
			if ((aopFlg & flg) == flg) {
				for (String marker : aop.marker()) {
					keySet.add(marker);
				}
			}
		}
		for (String key : keySet) {
			KagerowAOPProcessor observer = OBSERVER_LIST.get(key);
			if (Objects.nonNull(observer)) {
				result.add(observer);
			}
		}
		return result;
	}

	/**
	 * AOPアノテーションをフラグに変換します
	 * @param aop アノテーション
	 * @return フラグ（論理和）
	 */
	private int convertFlg(KagerowAOP aop) {
		int result = 0x00;
		result |= aop.startAOP() ? START_FLG : 0x00;
		result |= aop.endAOP() ? END_FLG : 0x00;
		result |= aop.errorAOP() ? ERROR_FLG : 0x00;
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public abstract void start(Object raw, Object proxy, Method method, Object[] args);

	/** {@inheritDoc} */
	@Override
	public abstract void end(Object raw, Object result, Method method, Object[] args);

	/** {@inheritDoc} */
	@Override
	public abstract void error(Object raw, Throwable error, Method method, Object[] args);

}

package com.sakulabo.core.Processor.aop;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import com.sakulabo.core.Kagerow.Utilities.KagerowAOP;
import com.sakulabo.core.Kagerow.Utilities.KagerowAOPProcessor;
import com.sakulabo.core.Kagerow.Utilities.KagerowAOPProcessors;

/**
 * Proxyのハンドラ実装を提供するクラスです 
 */
public final class CommonAOPInvocationHandlProcessor implements InvocationHandler, KagerowAOPProcessor {

	/** 実現インターフェイス一覧 */
	private final Class<?>[] interfaces;
	/** 生のインスタンス */
	private final Object rawInstance;
	/** AOPインターフェイス一覧 */
	private final Class<?>[] AOPInterface;
	/** AOPプロセッサー一覧 */
	private final KagerowAOPProcessors[] AOPProcessor;

	/**
	 * デフォルトのコンストラクタ
	 * @param rawInstance 生のインスタンス
	 */
	public CommonAOPInvocationHandlProcessor(Object rawInstance) {

		// フィールドの初期化
		this.interfaces = rawInstance.getClass().getInterfaces();
		this.rawInstance = rawInstance;

		// AOP対象の取得
		Set<Class<?>> AOPInterface = new HashSet<>();
		Set<KagerowAOPProcessors> AOPProcessor = new HashSet<>();
		for (Class<?> target : this.interfaces) {
			for (Method method : target.getDeclaredMethods()) {
				KagerowAOP[] aop = method.getDeclaredAnnotationsByType(KagerowAOP.class);
				if (0 < aop.length) {
					AOPInterface.add(target);
				}
				for (KagerowAOP a : aop)
					AOPProcessor.add(a.value());
			}
		}

		// 残りのフィールドを初期化
		this.AOPInterface = AOPInterface.stream().toArray(Class<?>[]::new);
		this.AOPProcessor = AOPProcessor.stream().toArray(KagerowAOPProcessors[]::new);

	}

	/** {@inheritDoc} */
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

		// メソッドの実行結果格納変数
		Object result = null;

		try {

			// AOP事前処理
			start(getRawInstance(), proxy, method, args);

			// メソッド実行
			result = method.invoke(rawInstance, args);

			// AOP事後処理
			end(getRawInstance(), result, method, args);

		} catch (Throwable e) {

			// AOP例外処理
			error(getRawInstance(), e, method, args);

			throw e;
		}

		// メソッドの実行結果返却
		return result;
	}

	/**
	 * 実現インターフェイス一覧を返却します
	 * @return 実現インターフェイス一覧
	 */
	public Class<?>[] getInterface() {
		return interfaces;
	}

	/**
	 * 実現インターフェイスの中でAOP対象となっているインターフェイスを一覧で返却します
	 * @return 実現インターフェイス一覧
	 */
	public Class<?>[] getAOPInterface() {
		return AOPInterface.clone();
	}

	/**
	 * 実現インターフェイスの中でAOP対象となっているプロセッサーを一覧で返却します
	 * @return 実現プロセッサー一覧
	 */
	public KagerowAOPProcessors[] getAOPProcessor() {
		return AOPProcessor.clone();
	}

	/**
	 * AOP対象か判定します
	 * @return 判定結果
	 */
	public boolean isAOPInstance() {
		return 0 < getAOPInterface().length;
	}

	/**
	 * ラップをしている生のインスタンスを返却します
	 * @return 生のインスタンス
	 */
	public Object getRawInstance() {
		return rawInstance;
	}

	/**
	 * メソッド実行直前に行われる全ての対象となる処理を実行するオブザーバーメソッドです
	 * @param raw 対象の生のインスタンス
	 * @param proxy インスタンスをラップしているAOPインスタンス
	 * @param method 実行対象メソッド
	 * @param args 実行対象メソッド向けのメソッド引数
	 */
	@Override
	public void start(Object raw, Object proxy, Method method, Object[] args) {
		for (KagerowAOP aop : method.getAnnotationsByType(KagerowAOP.class)) {
			if (aop.startAOP()) {
				aop.value().start(raw, proxy, method, args);
			}
		}
	}

	/**
	 * メソッド実行直後に行われる全ての対象となる処理を実行するオブザーバーメソッドです
	 * @param raw 対象の生のインスタンス
	 * @param result メソッド実行結果
	 * @param method 実行対象メソッド
	 * @param args 実行対象メソッド向けのメソッド引数
	 */
	@Override
	public void end(Object raw, Object result, Method method, Object[] args) {
		for (KagerowAOP aop : method.getAnnotationsByType(KagerowAOP.class)) {
			if (aop.endAOP()) {
				aop.value().end(raw, result, method, args);
			}
		}
	}

	/**
	 * メソッド実行により例外が発生した場合行われる処理を実行するオブザーバーメソッドです
	 * @param raw 対象の生のインスタンス
	 * @param error スローされた例外
	 * @param method 実行対象メソッド
	 * @param args 実行対象メソッド向けのメソッド引数
	 */
	@Override
	public void error(Object raw, Throwable error, Method method, Object[] args) {
		for (KagerowAOP aop : method.getAnnotationsByType(KagerowAOP.class)) {
			if (aop.errorAOP()) {
				aop.value().error(raw, error, method, args);
			}
		}
	}

}

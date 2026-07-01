package com.sakulabo.core.Kagerow.Utilities;

import java.lang.reflect.Method;

/**
 * 横断的関心事で処理される実装を規定するクラスです
 * 
 * @author keeeeeent
 */
public interface KagerowAOPProcessor {

	/**
	 * メソッド実行直前に行われる処理を定義します
	 * @param raw 対象の生のインスタンス
	 * @param proxy インスタンスをラップしているAOPインスタンス
	 * @param method 実行対象メソッド
	 * @param args 実行対象メソッド向けのメソッド引数
	 */
	void start(Object raw, Object proxy, Method method, Object[] args);

	/**
	 * メソッド実行直後に行われる処理を定義します
	 * @param raw 対象の生のインスタンス
	 * @param result メソッド実行結果
	 * @param method 実行対象メソッド
	 * @param args 実行対象メソッド向けのメソッド引数
	 */
	void end(Object raw, Object result, Method method, Object[] args);

	/**
	 * メソッド実行により例外が発生した場合行われる処理を定義します
	 * @param raw 対象の生のインスタンス
	 * @param error スローされた例外
	 * @param method 実行対象メソッド
	 * @param args 実行対象メソッド向けのメソッド引数
	 */
	void error(Object raw, Throwable error, Method method, Object[] args);

	/**
	 * AOPインスタンス自身を返却します
	 * @return インスタンス
	 */
	default KagerowAOPProcessor getInstance() {
		return this;
	}

}

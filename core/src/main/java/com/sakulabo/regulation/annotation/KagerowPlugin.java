package com.sakulabo.regulation.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * プラグイン実装クラスか識別するためのアノテーション
 * 
 * @author keeeeeent
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface KagerowPlugin {

	/**
	 * プラグイン種別を表す列挙子です
	 */
	public static enum PluginType {

		/** インプット処理対応 */
		INPUT,
		/** アウトプット処理対応 */
		OUTPUT;
	}

	/**
	 * プラグイン名称
	 * @return 名称
	 */
	String name();

	/**
	 * プラグイン種別
	 * @return 種別
	 */
	PluginType[] types();

	/**
	 * 同時実行可能サイズ<br/>
	 * デフォルトはシングルスレッドサイズ
	 * @return サイズ
	 */
	int multiSize() default 1;

}

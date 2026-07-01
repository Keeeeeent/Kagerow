package com.sakulabo.regulation.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * プラグインSPIインジェクション先か識別するためのクラスです
 * 
 * @author keeeeeent
 */
@Documented
@Target(ElementType.MODULE)
@Retention(RetentionPolicy.SOURCE)
public @interface KagerowPluginSpiModule {

	/**
	 * プラグインパッケージ名称を設定します
	 * @return プラグインパッケージ名称
	 */
	String value();

	/**
	 * メジャーバージョン番号
	 * @return バージョン番号
	 */
	int majorVersion() default 1;

	/**
	 * マイナーバージョン番号
	 * @return バージョン番号
	 */
	int minorVersion() default 0;

	/**
	 * パッチバージョン番号
	 * @return バージョン番号
	 */
	int patchVersion() default 0;

}

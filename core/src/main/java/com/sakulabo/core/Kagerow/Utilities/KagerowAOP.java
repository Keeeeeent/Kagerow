package com.sakulabo.core.Kagerow.Utilities;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.sakulabo.core.Kagerow.Utilities.KagerowAOP.AOPList;

/**
 * 横断的関心事を付与する対象であることを表す注釈クラスです
 * 
 * @author keeeeeent
 */
@Documented
@Repeatable(AOPList.class)
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface KagerowAOP {

	/**
	 * AOPで実行する横断的関心事の種類
	 * @return CommonAOPProcessor
	 */
	KagerowAOPProcessors value();

	/**
	 * AOP事前処理の有無
	 * @return 処理の有無
	 */
	boolean startAOP() default true;

	/**
	 * AOP事後処理の有無
	 * @return 処理の有無
	 */
	boolean endAOP() default true;

	/**
	 * AOP例外処理の有無
	 * @return 処理の有無
	 */
	boolean errorAOP() default true;

	/**
	 * AOP処理のマーカです
	 * @return マーカ
	 */
	String[] marker() default {};

	/**
	 * AOP処理のオプションです
	 * @return オプション
	 */
	String option() default "";

	/**
	 * AOPアノテーションのコンテナアノテーションです
	 */
	@Documented
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface AOPList {

		/**
		 * AOPアノテーション一覧を返却します
		 * @return AOPインスタンス配列
		 */
		KagerowAOP[] value();
	}
}

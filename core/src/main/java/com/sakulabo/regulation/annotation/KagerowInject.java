package com.sakulabo.regulation.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.sakulabo.core.Common.StringUtils;

/**
 * インジェクト対象のフィールドか識別するためのアノテーション
 * 
 * @author keeeeeent
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface KagerowInject {

	/**
	 * コンポーネント名称
	 * @return 名称
	 */
	String value() default StringUtils.DEFAULT;
}

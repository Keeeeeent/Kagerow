package com.sakulabo.core.Processor.jmx;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * JMX監視設定定義アノテーション
 * 
 * @author keeeeeent
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AppJMX {

	/**
	 * JMX監視オブジェクト名称
	 * @return 名称
	 */
	String name();

	/**
	 * JMX監視オブジェクトオプション
	 * @return オプション
	 */
	String[] options();

}

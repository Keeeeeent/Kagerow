package com.sakulabo.regulation.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * JCTree使用中のアノテーションプロセッサーか識別するためのクラスです
 * 
 * @author keeeeeent
 */
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface AnnotationProcessor {

}

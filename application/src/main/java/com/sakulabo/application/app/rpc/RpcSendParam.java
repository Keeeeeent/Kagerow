package com.sakulabo.application.app.rpc;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * RPCメソッドパラメータマッピングアノテーションです
 *
 * @author keeeeeent
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(RpcSendParam.List.class)
public @interface RpcSendParam {

	/**
	 * マッピング後のパラメータ名称
	 */
	String value();

	/**
	 * パラメータ必須フラグ
	 */
	boolean required() default false;

	/**
	 * RPCメソッドパラメータコンテナノテーションです
	 *
	 * @author keeeeeent
	 */
	@Documented
	@Target(ElementType.PARAMETER)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface List {

		/**
		 * RPCメソッドパラメータマッピングリストを取得します
		 */
		RpcSendParam[] value();
	}

}

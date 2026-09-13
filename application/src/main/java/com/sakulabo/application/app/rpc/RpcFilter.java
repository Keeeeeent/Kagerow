package com.sakulabo.application.app.rpc;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.sun.net.httpserver.Filter;

/**
 * RPCフィルタ追加アノテーションです
 *
 * @author keeeeeent
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(RpcFilter.List.class)
public @interface RpcFilter {

	/**
	 * 追加するフィルタ
	 */
	Class<? extends Filter> filter();

	/**
	 * 必須フラグ
	 */
	boolean required() default false;

	/**
	 * RPCフィルタ追加コンテナノテーションです
	 *
	 * @author keeeeeent
	 */
	@Documented
	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface List {

		/**
		 * RPCフィルタ追加リストを取得します
		 */
		RpcFilter[] value();
	}

}

package com.sakulabo.application.app.rpc;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * RPCメソッドマッピングアノテーションです
 *
 * @author keeeeeent
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RpcMethod {

	/**
	 * XSDファイルパス
	 */
	String xsd();

	/**
	 * RPC呼び出し時名称
	 */
	String methodName();

}

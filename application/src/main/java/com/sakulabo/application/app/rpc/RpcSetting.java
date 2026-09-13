package com.sakulabo.application.app.rpc;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * RPCコンテキスト設定アノテーションです
 *
 * @author keeeeeent
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RpcSetting {

	/**
	 * マッピングURL
	 */
	String value();

}

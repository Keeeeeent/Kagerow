package com.sakulabo.application.app.rpc;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * アプリケーション共通で使用されるRPCエラーハンドリング定義実装です
 *
 * @author keeeeeent
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RpcException {

	/**
	 * 処理対象例外
	 */
	Class<? extends Throwable> value();

}

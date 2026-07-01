package com.sakulabo.regulation.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.sakulabo.regulation.annotation.KagerowComponent.AppInitComponet;
import com.sakulabo.regulation.annotation.KagerowComponent.AppInitComponet.DefaultInitComponet;

/**
 * SPIプロバイダインジェクション先か識別するためのクラスです
 * 
 * @author keeeeeent
 */
@Documented
@Target(ElementType.MODULE)
@Retention(RetentionPolicy.SOURCE)
public @interface KagerowSpiModule {

	/**
	 * 初期化エントリクラス情報
	 * @return クラス情報
	 */
	Class<? extends AppInitComponet> value() default DefaultInitComponet.class;

}

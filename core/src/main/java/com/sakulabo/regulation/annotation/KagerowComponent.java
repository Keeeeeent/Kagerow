package com.sakulabo.regulation.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.sakulabo.core.Common.StringUtils;

/**
 * Bean対象のクラスか識別するためのアノテーション
 * 
 * @author keeeeeent
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface KagerowComponent {

	/**
	 * 初期化対象のBeanクラスか識別するためのインターフェイスです
	 */
	public interface AppInitComponet {

		/**
		 * デフォルトの初期化処理実装クラスです<br/>
		 * DIプロセッサーの実装によってはこのクラスは無視されます
		 */
		public final class DefaultInitComponet implements AppInitComponet {

			/** {@inheritDoc} */
			@Override
			public void initialize() {
				;
			}

		}

		/**
		 * 初期化処理にて呼び出しが行われるメソッドです
		 */
		void initialize();

	}

	/**
	 * コンポーネント名称
	 * @return 名称
	 */
	String value() default StringUtils.DEFAULT;
}

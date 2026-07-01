package com.sakulabo.regulation.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 対象メソッドのJitCompilerOptionを識別するためのアノテーション
 * 
 * @author keeeeeent
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface UseJITCompiler {

	/**
	 * JitCompilerOptionを指定するための列挙子です
	 * 
	 * @author keeeeeent
	 */
	public enum JITCompilerOption {

		/** 指定されたメソッドのインライン化を防ぎます */
		DONTINLINE("dontinline"),
		/** 指定されたメソッドのインライン化を試みます */
		INLINE("inline"),
		/** 指定されたメソッドをコンパイルから除外します */
		EXCLUDE("exclude"),
		/**
		 * 指定されたメソッドを除くすべてのメソッドのコンパイルのロギングを(-XX:+LogCompilationオプションを使用して)除外します。<br/>
		 * デフォルトで、ロギングはすべてのコンパイル済メソッドに対して実行されます
		 */
		LOG("log"),
		/**
		 * 指定されたメソッドを除くすべてのメソッドをコンパイルから除外します。<br/>
		 * かわりに、-XX:CompileOnlyオプションを使用でき、これにより複数のメソッドを指定できます。
		 */
		COMPILEONLY("compileonly");

		/** JitCompilerオプション */
		private String option;

		/**
		 * コンストラクタ
		 * @param option オプション文字列
		 */
		private JITCompilerOption(String option) {
			this.option = option;
		}

		/** {@inheritDoc} */
		@Override
		public String toString() {
			return option;
		}
	}

	/**
	 * Jitコンパイラー向けのオプションを指定します
	 * @return JitCompilerOption
	 */
	JITCompilerOption[] value();

}

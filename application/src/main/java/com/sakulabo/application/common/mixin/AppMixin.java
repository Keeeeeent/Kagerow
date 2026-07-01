package com.sakulabo.application.common.mixin;

import java.awt.Component;
import java.awt.Dimension;
import java.lang.StackWalker.Option;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.sakulabo.regulation.annotation.UseJITCompiler;
import com.sakulabo.regulation.annotation.UseJITCompiler.JITCompilerOption;

/**
 * ミックスイン基底インターフェイスです
 * 
 * @author keeeeeent
 */
public interface AppMixin {

	/**
	 * サイズを指定します
	 */
	@Documented
	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Size {

		/**
		 * 幅を指定します
		 * @return px単位の幅
		 */
		int width();

		/**
		 * 高さを指定します
		 * @return px単位の高さ
		 */
		int height();
	}

	/**
	 * コンポーネントのサイズを設定します
	 * @param conponent コンポーネント
	 */
	@UseJITCompiler(JITCompilerOption.DONTINLINE)
	public default void setSize(Component conponent) {
		// 呼び出し元クラス情報取得
		Class<?> clazz = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).getCallerClass();
		// サイズデフォルト値の初期化
		int width = 600, heigth = 400;
		// アノテーション確認
		if (clazz.isAnnotationPresent(Size.class)) {
			Size size = clazz.getDeclaredAnnotation(Size.class);
			width = size.width();
			heigth = size.height();
		}
		// コンポーネントのサイズ設定
		conponent.setSize(width, heigth);
	}

	/**
	 * コンポーネントの幅サイズを取得します<br/>
	 * サイズが未指定の場合-1を返却します
	 * 
	 * @param conponent コンポーネント
	 * @return 幅サイズ
	 */
	@UseJITCompiler(JITCompilerOption.DONTINLINE)
	public default int getWidthSize(Component conponent) {
		// 呼び出し元クラス情報取得
		Class<?> clazz = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).getCallerClass();
		// サイズデフォルト値の初期化
		int target = -1;
		// アノテーション確認
		if (clazz.isAnnotationPresent(Size.class)) {
			Size size = clazz.getDeclaredAnnotation(Size.class);
			target = size.width();
		}
		return target;
	}

	/**
	 * コンポーネントの高さサイズを取得します<br/>
	 * サイズが未指定の場合-1を返却します
	 * 
	 * @param conponent コンポーネント
	 * @return 高さサイズ
	 */
	@UseJITCompiler(JITCompilerOption.DONTINLINE)
	public default int getHeightSize(Component conponent) {
		// 呼び出し元クラス情報取得
		Class<?> clazz = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).getCallerClass();
		// サイズデフォルト値の初期化
		int target = -1;
		// アノテーション確認
		if (clazz.isAnnotationPresent(Size.class)) {
			Size size = clazz.getDeclaredAnnotation(Size.class);
			target = size.height();
		}
		return target;
	}

	/**
	 * 推奨サイズを指定します
	 */
	@Documented
	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface PreferredSize {

		/**
		 * 幅を指定します
		 * @return px単位の幅
		 */
		int width();

		/**
		 * 高さを指定します
		 * @return px単位の高さ
		 */
		int height();

	}

	/**
	 * コンポーネントのサイズを設定します
	 * @param conponent コンポーネント
	 */
	@UseJITCompiler(JITCompilerOption.DONTINLINE)
	public default void setPreferredSize(Component conponent) {
		// 呼び出し元クラス情報取得
		Class<?> clazz = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).getCallerClass();
		// サイズデフォルト値の初期化
		int width = 600, heigth = 400;
		// アノテーション確認
		if (clazz.isAnnotationPresent(PreferredSize.class)) {
			PreferredSize size = clazz.getDeclaredAnnotation(PreferredSize.class);
			width = size.width();
			heigth = size.height();
		}
		// コンポーネントのサイズ設定
		conponent.setPreferredSize(new Dimension(width, heigth));
	}

	/**
	 * コンポーネントの推奨幅サイズを取得します<br/>
	 * サイズが未指定の場合-1を返却します
	 * 
	 * @param conponent コンポーネント
	 * @return 推奨幅サイズ
	 */
	@UseJITCompiler(JITCompilerOption.DONTINLINE)
	public default int getWidthPreferredSize(Component conponent) {
		// 呼び出し元クラス情報取得
		Class<?> clazz = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).getCallerClass();
		// サイズデフォルト値の初期化
		int target = -1;
		// アノテーション確認
		if (clazz.isAnnotationPresent(PreferredSize.class)) {
			PreferredSize size = clazz.getDeclaredAnnotation(PreferredSize.class);
			target = size.width();
		}
		return target;
	}

	/**
	 * コンポーネントの推奨高さサイズを取得します<br/>
	 * サイズが未指定の場合-1を返却します
	 * 
	 * @param conponent コンポーネント
	 * @return 推奨高さサイズ
	 */
	@UseJITCompiler(JITCompilerOption.DONTINLINE)
	public default int getHeightPreferredSize(Component conponent) {
		// 呼び出し元クラス情報取得
		Class<?> clazz = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).getCallerClass();
		// サイズデフォルト値の初期化
		int target = -1;
		// アノテーション確認
		if (clazz.isAnnotationPresent(PreferredSize.class)) {
			PreferredSize size = clazz.getDeclaredAnnotation(PreferredSize.class);
			target = size.height();
		}
		return target;
	}

}

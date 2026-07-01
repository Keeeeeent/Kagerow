package com.sakulabo.application.common.mixin;

import java.awt.Component;
import java.lang.StackWalker.Option;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Objects;

import javax.swing.Box;
import javax.swing.JToolBar;

import com.sakulabo.regulation.annotation.UseJITCompiler;
import com.sakulabo.regulation.annotation.UseJITCompiler.JITCompilerOption;

/**
 * GUI向けJToolBarミックスイン実装インターフェイスです
 * 
 * @author keeeeeent
 */
public interface JToolBarMixin extends AppMixin {

	/**
	 * JToolBarの設定を指定します
	 */
	@Documented
	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Setting {

		/**
		 * ツールバーが移動可能か設定します
		 * @return 設定値
		 */
		boolean isFloatable() default false;
	}

	/**
	 * JToolBarインスタンスを生成します
	 * @return JToolBarインスタンス
	 */
	@UseJITCompiler(JITCompilerOption.DONTINLINE)
	public default JToolBar createJToolBar() {

		// 呼び出し元クラス情報取得
		Class<?> clazz = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).getCallerClass();

		// コンポーネント生成
		JToolBar toolBar = new JToolBar();

		// アノテーション確認
		if (clazz.isAnnotationPresent(JToolBarMixin.Setting.class)) {
			// アノテーション取得
			JToolBarMixin.Setting setting = clazz.getDeclaredAnnotation(JToolBarMixin.Setting.class);
			// 設定値取得
			boolean isFloatable = setting.isFloatable();
			// 移動設定
			toolBar.setFloatable(isFloatable);
		}

		// 左側のツール
		Component[] leftComponent = getLeftComponent();
		if (Objects.nonNull(leftComponent)) {
			for (Component cmp : leftComponent) {
				if (Objects.nonNull(cmp))
					toolBar.add(cmp);
			}
		}

		// セパレーター
		toolBar.add(Box.createHorizontalGlue());

		// 右側のツール
		Component rightComponent = getRightComponent();
		if (Objects.nonNull(rightComponent)) {
			toolBar.add(rightComponent);
		}

		return toolBar;
	}

	/**
	 * 左側に配置するコンポーネント
	 * @return コンポーネント
	 */
	public Component[] getLeftComponent();

	/**
	 * 右側に配置するコンポーネント
	 * @return コンポーネント
	 */
	public Component getRightComponent();
}

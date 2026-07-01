package com.sakulabo.application.common.mixin;

import java.awt.Component;
import java.lang.StackWalker.Option;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.swing.JSplitPane;

import com.sakulabo.regulation.annotation.UseJITCompiler;
import com.sakulabo.regulation.annotation.UseJITCompiler.JITCompilerOption;

/**
 * GUI向けJSplitPaneミックスイン実装インターフェイスです
 * 
 * @author keeeeeent
 */
public interface JSplitPanelMixin extends AppMixin {

	/**
	 * JSplitPaneの設定を指定します
	 */
	@Documented
	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Setting {

		/**
		 * 分割方法の指定
		 * @return 分割方法指定の定数値
		 */
		int orientation();
	}

	/**
	 * JSplitPaneインスタンスを生成します
	 * @return JSplitPaneインスタンス
	 */
	@UseJITCompiler(JITCompilerOption.DONTINLINE)
	public default JSplitPane createSplitPane() {

		// 呼び出し元クラス情報取得
		Class<?> clazz = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).getCallerClass();

		// アノテーション確認
		int orientation = JSplitPane.HORIZONTAL_SPLIT;
		if (clazz.isAnnotationPresent(JSplitPanelMixin.Setting.class)) {
			JSplitPanelMixin.Setting setting = clazz.getDeclaredAnnotation(JSplitPanelMixin.Setting.class);
			orientation = setting.orientation();
		}

		// コンポーネント生成
		JSplitPane splitPane = new JSplitPane(orientation, getLeftComponent(), getRightComponent());

		return splitPane;
	}

	/**
	 * 左側に配置するコンポーネント
	 * @return コンポーネント
	 */
	public Component getLeftComponent();

	/**
	 * 右側に配置するコンポーネント
	 * @return コンポーネント
	 */
	public Component getRightComponent();

}

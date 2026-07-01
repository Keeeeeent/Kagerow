package com.sakulabo.application.common.mixin;

import java.lang.StackWalker.Option;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.swing.JTabbedPane;

import com.sakulabo.regulation.annotation.UseJITCompiler;
import com.sakulabo.regulation.annotation.UseJITCompiler.JITCompilerOption;

/**
 * GUI向けJTabbedPaneミックスイン実装インターフェイスです
 * 
 * @author keeeeeent
 */
public interface JTabbedPaneMixin extends AppMixin {

	/**
	 * JTabbedPaneの設定を指定します
	 */
	@Documented
	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Setting {

		/**
		 * 表示方法の指定
		 * @return 表示方法指定の定数値
		 */
		int setTabLayoutPolicy();
	}

	/**
	 * JTabbedPaneインスタンスを生成します
	 * @return JSplitPaneインスタンス
	 */
	@UseJITCompiler(JITCompilerOption.DONTINLINE)
	public default JTabbedPane createTabbedPane() {

		// 呼び出し元クラス情報取得
		Class<?> clazz = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).getCallerClass();

		// アノテーション確認
		int setTabLayoutPolicy = JTabbedPane.WRAP_TAB_LAYOUT;
		if (clazz.isAnnotationPresent(JTabbedPaneMixin.Setting.class)) {
			JTabbedPaneMixin.Setting setting = clazz.getDeclaredAnnotation(JTabbedPaneMixin.Setting.class);
			setTabLayoutPolicy = setting.setTabLayoutPolicy();
		}

		// コンポーネント生成
		JTabbedPane tabbedPane = new JTabbedPane();
		// タブをスワップ表示
		tabbedPane.setTabLayoutPolicy(setTabLayoutPolicy);

		return tabbedPane;
	}

}

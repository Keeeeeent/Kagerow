package com.sakulabo.application.common.mixin;

import java.awt.Color;
import java.awt.LayoutManager;
import java.lang.StackWalker.Option;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.swing.JPanel;

import com.sakulabo.regulation.annotation.UseJITCompiler;
import com.sakulabo.regulation.annotation.UseJITCompiler.JITCompilerOption;

/**
 * GUI向けJPanelミックスイン実装インターフェイスです
 * 
 * @author keeeeeent
 */
public interface JPanelMixin extends AppMixin {

	/**
	 * JPanelレイアウト指定のための列挙クラスです
	 */
	public enum Layout {

		/** ボーダーレイアウト */
		BorderLayout {

			/** {@inheritDoc} */
			@Override
			public LayoutManager getLayout() {
				return new java.awt.BorderLayout();
			}
		},

		/** カードレイアウト */
		CardLayout {
			/** {@inheritDoc} */
			@Override
			public LayoutManager getLayout() {
				return new java.awt.CardLayout();
			}
		},

		/** フローレイアウト */
		FlowLayout {
			/** {@inheritDoc} */
			@Override
			public LayoutManager getLayout() {
				return new java.awt.FlowLayout(java.awt.FlowLayout.LEFT);
			}
		};

		/**
		 * レイアウト生成のためのクラスボディです
		 * @return レイアウトマネージャー
		 */
		public abstract LayoutManager getLayout();
	}

	/**
	 * JPanelの設定を指定します
	 */
	@Documented
	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Setting {

		/**
		 * レイアウトを設定します
		 * @return レイアウト列挙型
		 */
		JPanelMixin.Layout layout();

		/**
		 * 背景色を設定します
		 * @return 16進数表現カラーコード
		 */
		int backgroudColor() default -1;

	}

	/**
	 * JMenuItemをJMenuに追加します
	 * @param panel パネル
	 */
	@UseJITCompiler(JITCompilerOption.DONTINLINE)
	public default void setJPanel(JPanel panel) {

		// 呼び出し元クラス情報取得
		Class<?> clazz = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).getCallerClass();

		// アノテーション確認
		Setting[] settingList = clazz.getDeclaredAnnotationsByType(Setting.class);
		for (Setting setting : settingList) {
			// アノテーション解析
			int backgroudColor = setting.backgroudColor();
			if (0 < backgroudColor) {
				// 背景色設定
				Color color = new Color(backgroudColor);
				panel.setBackground(color);
			}
			// レイアウト設定
			LayoutManager layoutManager = setting.layout().getLayout();
			panel.setLayout(layoutManager);
		}

	}

}

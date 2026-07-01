package com.sakulabo.application.common.mixin;

import java.lang.StackWalker.Option;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.swing.JMenuItem;

import com.sakulabo.application.app.gui.MenuPanelParts.AppMenu;
import com.sakulabo.application.common.code.ApplicationConstProperty;
import com.sakulabo.application.common.code.GUIText;
import com.sakulabo.regulation.annotation.UseJITCompiler;
import com.sakulabo.regulation.annotation.UseJITCompiler.JITCompilerOption;

/**
 * GUI向けJMenuItemミックスイン実装インターフェイスです
 * 
 * @author keeeeeent
 */
public interface JMenuItemMixin extends ActionListenerMixin {

	/**
	 * JMenuItemの設定を指定します
	 */
	@Documented
	@Repeatable(JMenuItemMixin.Setting.SettingList.class)
	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Setting {

		/**
		 * メニュー項目に表示する文字列を設定します
		 * @return 表示文字列
		 */
		GUIText title();

		/**
		 * アクションコマンドを設定します
		 * @return アクションコマンド文字列
		 */
		String actionCommand();

		/**
		 * JMenuItemの設定コンテナです
		 */
		@Documented
		@Target(ElementType.TYPE)
		@Retention(RetentionPolicy.RUNTIME)
		public @interface SettingList {

			/**
			 * JMenuItemの設定項目保管コンテナ
			 * @return コンテナ
			 */
			Setting[] value();
		}
	}

	/**
	 * JMenuItemをJMenuに追加します
	 * @param menu 追加先メニュー
	 * @throws IllegalAccessException ハンドラーアクセスエラー
	 * @throws NoSuchMethodException メソッド不明
	 */
	@UseJITCompiler(JITCompilerOption.DONTINLINE)
	public default void setJMenuItem(AppMenu menu) throws IllegalAccessException, NoSuchMethodException {

		// 呼び出し元クラス情報取得
		Class<?> clazz = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).getCallerClass();

		// アノテーション確認
		Setting[] settingList = clazz.getDeclaredAnnotationsByType(Setting.class);
		for (Setting setting : settingList) {
			// アノテーション解析
			String title = setting.title().toString();
			String actionCommand = setting.actionCommand();
			int mnemonic = setting.title().toMnemonic();
			// メニュー生成
			JMenuItem item = new JMenuItem(title);
			// ニーモック設定
			if (ApplicationConstProperty.DEFAULT_MNEMONIC != mnemonic) {
				// ニーモックの指定がある場合のみ設定を適用する
				item.setMnemonic(mnemonic);
			}
			// ハンドラー生成
			addCache(menu, item, actionCommand);
			// メニュー追加
			menu.add(item);
		}

	}

}

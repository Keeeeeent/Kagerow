package com.sakulabo.application.common.mixin;

import java.lang.StackWalker.Option;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.ref.Cleaner.Cleanable;
import java.lang.reflect.Field;
import java.util.Optional;
import java.util.stream.Stream;

import javax.swing.BorderFactory;
import javax.swing.JButton;

import com.sakulabo.application.common.code.GUIText;
import com.sakulabo.application.common.image.ButtonImage;
import com.sakulabo.regulation.annotation.UseJITCompiler;
import com.sakulabo.regulation.annotation.UseJITCompiler.JITCompilerOption;

/**
 * GUI向けJButtonミックスイン実装インターフェイスです
 * 
 * @author keeeeeent
 */
public interface JButtonMixin extends ActionListenerMixin {

	/**
	 * JButtonの設定を指定します
	 */
	@Documented
	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Setting {

		/**
		 * 表示する文字列を設定します
		 * @return 表示文字列
		 */
		GUIText title();

		/**
		 * 表示するアイコンを設定します
		 * @return アイコンイメージ
		 */
		ButtonImage icon() default ButtonImage.EMPTY;

		/**
		 * アクションコマンドを設定します
		 * @return アクションコマンド文字列
		 */
		String actionCommand();

		/**
		 * ボタンがフォーカス可能か設定します
		 * @return フォーカス設定真偽値
		 */
		boolean isFocusable() default false;

	}

	/**
	 * JButtonの設定をこないます
	 * @param target アクション実装クラス 
	 * @param btn アクション登録先
	 * @throws IllegalAccessException ハンドラーアクセスエラー
	 * @throws NoSuchMethodException メソッド不明
	 * @return クリーナー
	 */
	@UseJITCompiler(JITCompilerOption.DONTINLINE)
	public default Optional<Cleanable> setJButton(JButtonMixin target, JButton btn)
			throws IllegalAccessException, NoSuchMethodException {

		// 呼び出し元クラス情報取得
		Class<?> clazz = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).getCallerClass();

		// アノテーション確認
		Field field = Stream.of(clazz.getDeclaredFields())
				.filter(f -> f.isAnnotationPresent(JButtonMixin.Setting.class))
				.peek(f -> f.setAccessible(true))
				.filter(f -> searchField(f, target, btn))
				.findFirst()
				.get();
		JButtonMixin.Setting setting = field.getDeclaredAnnotation(JButtonMixin.Setting.class);

		// アノテーション解析
		String title = setting.title().toString();
		String actionCommand = setting.actionCommand();
		boolean isFocusable = setting.isFocusable();
		ButtonImage icon = setting.icon();

		// ボタン文字設定
		btn.setText(title);
		// アイコン設定
		if (icon != ButtonImage.EMPTY) {
			btn.setIcon(icon.toImageIcon());
			btn.setBorder(BorderFactory.createEmptyBorder());
			btn.setContentAreaFilled(false);
			btn.setFocusPainted(false);
			btn.setFocusable(false);
		}
		// フォーカス設定
		btn.setFocusable(isFocusable);

		// ハンドラー生成
		Optional<Cleanable> cleaner = addCache(target, btn, actionCommand);
		return cleaner;
	}

	/**
	 * 対象フィールドが解析対象か判定します
	 * @param field フィールドリフレクションインスタンス
	 * @param target リフレクション先
	 * @param btn 比較用インスタンス
	 * @return 実行結果
	 */
	private boolean searchField(Field field, JButtonMixin target, JButton btn) {
		try {
			return field.get(target) == btn;
		} catch (IllegalArgumentException | IllegalAccessException e) {
			return false;
		}
	}

}

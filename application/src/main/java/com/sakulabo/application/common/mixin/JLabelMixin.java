package com.sakulabo.application.common.mixin;

import java.awt.Dimension;
import java.lang.StackWalker.Option;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Objects;
import java.util.stream.Stream;

import javax.swing.JLabel;

import com.sakulabo.application.common.code.GUIText;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;
import com.sakulabo.regulation.annotation.UseJITCompiler;
import com.sakulabo.regulation.annotation.UseJITCompiler.JITCompilerOption;

/**
 * GUI向けJLabelミックスイン実装インターフェイスです
 * 
 * @author keeeeeent
 */
public interface JLabelMixin extends AppMixin {

	/**
	 * JLabelの設定を指定します
	 */
	@Documented
	@Target({ ElementType.TYPE, ElementType.FIELD })
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Setting {

		/**
		 * JLabelのラベルを指定します
		 * @return ラベル
		 */
		String label() default "";

		/**
		 * JLabelのラベルを指定します
		 * @return ラベル
		 */
		GUIText text() default GUIText.EMPTY;

		/**
		 * ラベルの幅を設定します
		 * @return 幅px
		 */
		int width() default -1;

		/**
		 * ラベルの高さを設定します
		 * @return 高さpx
		 */
		int heigth() default -1;

	}

	/**
	 * JLabelインスタンスを生成します
	 * @return JLabelインスタンス
	 */
	@UseJITCompiler(JITCompilerOption.DONTINLINE)
	public default JLabel createLabel() {

		// 呼び出し元クラス情報取得
		Class<?> clazz = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).getCallerClass();

		String labelText = null;

		// アノテーション確認
		if (clazz.isAnnotationPresent(JLabelMixin.Setting.class)) {
			JLabelMixin.Setting setting = clazz.getDeclaredAnnotation(JLabelMixin.Setting.class);
			labelText = setting.label();
		}

		// JFrame生成&タイトル設定
		JLabel label;
		if (Objects.isNull(labelText)) {
			label = new JLabel();
		} else {
			label = new JLabel(labelText);
		}

		return label;
	}

	/**
	 * JLabelの設定を行います
	 * @param target 設定対象
	 */
	@UseJITCompiler(JITCompilerOption.DONTINLINE)
	public default void setJLabel(JLabel target) {

		// 呼び出し元クラス情報取得
		Class<?> clazz = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).getCallerClass();

		// アノテーション確認
		JLabelMixin.Setting setting = Stream.of(clazz.getDeclaredFields())
				.filter(f -> f.isAnnotationPresent(JLabelMixin.Setting.class))
				.peek(f -> f.setAccessible(true))
				.filter(f -> {
					try {
						Object obj = f.get(this);
						return obj == target;
					} catch (IllegalArgumentException | IllegalAccessException e) {
						KagerowLogger.newAppLogger().err(e);
					}
					return false;
				})
				.map(f -> f.getDeclaredAnnotation(JLabelMixin.Setting.class))
				.findFirst()
				.get();

		// 設定初期化
		String label = setting.label();
		GUIText text = setting.text();
		int width = setting.width(), heigth = setting.heigth();

		// 設定反映
		if (!GUIText.EMPTY.equals(text)) {
			target.setText(text.toString());
		} else {
			target.setText(label);
		}
		if (width != -1 && heigth != -1) {
			target.setPreferredSize(new Dimension(width, heigth));
		}

	}

}

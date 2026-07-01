package com.sakulabo.application.common.mixin;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.lang.StackWalker.Option;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Objects;

import javax.swing.InputMap;
import javax.swing.JFrame;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.text.DefaultEditorKit;

import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;
import com.sakulabo.regulation.annotation.UseJITCompiler;
import com.sakulabo.regulation.annotation.UseJITCompiler.JITCompilerOption;

/**
 * GUI向けJFrameミックスイン実装インターフェイスです
 * 
 * @author keeeeeent
 */
public interface JFrameMixin extends AppMixin {

	/**
	 * JFrameの設定を指定します
	 */
	@Documented
	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Setting {

		/**
		 * JFrameのタイトルを指定します
		 * @return タイトル
		 */
		String title();

		/**
		 * JFrameのクローズボタンの挙動を指定します
		 * @return クローズボタンモード定数値
		 */
		int closeOperation();

		/**
		 * JFrameのL&Fの挙動を指定します
		 * @return L&F
		 */
		String lookAndFeel() default "javax.swing.plaf.nimbus.NimbusLookAndFeel";

	}

	/**
	 * JFrameインスタンスを生成します
	 * @return JFrameインスタンス
	 */
	@UseJITCompiler(JITCompilerOption.DONTINLINE)
	public default JFrame createFrame() {

		// 呼び出し元クラス情報取得
		Class<?> clazz = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).getCallerClass();

		// 設定値初期化
		int closeOperation = JFrame.DISPOSE_ON_CLOSE;
		String title = null;
		String lookAndFeel = null;

		// アノテーション確認
		if (clazz.isAnnotationPresent(JFrameMixin.Setting.class)) {
			JFrameMixin.Setting setting = clazz.getDeclaredAnnotation(JFrameMixin.Setting.class);
			closeOperation = setting.closeOperation();
			title = setting.title();
			lookAndFeel = setting.lookAndFeel();
		}

		// L&Fの設定
		try {
			UIManager.setLookAndFeel(lookAndFeel);
		} catch (Exception e) {
			KagerowLogger.newAppLogger().err(e);
		}

		// ショートカットマスクの設定
		installMacKeyBindings();

		// JFrame生成&タイトル設定
		JFrame frame;
		if (Objects.isNull(title)) {
			frame = new JFrame();
		} else {
			frame = new JFrame(title);
		}
		frame.setDefaultCloseOperation(closeOperation);

		return frame;
	}

	/**
	 * ショートカットマスクを適用します
	 */
	private void installMacKeyBindings() {
		// ショートカットマスク取得
		int mask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
		// マスク対象キー取得
		Object[] keys = {
				"TextField.focusInputMap",
				"TextArea.focusInputMap",
				"TextPane.focusInputMap",
				"EditorPane.focusInputMap",
				"PasswordField.focusInputMap",
				"Table.ancestorInputMap"
		};
		// ショートカットマスク適用
		for (Object key : keys) {
			// UIマネージャーへ適用し、アプリケーション全体にショートカットマスクを適用する
			InputMap map = (InputMap) UIManager.get(key);
			if (map == null) {
				continue;
			}
			// 対象が見つかった場合、ショートカットマスク上書き
			map.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, mask), DefaultEditorKit.copyAction);
			map.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, mask), DefaultEditorKit.pasteAction);
			map.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, mask), DefaultEditorKit.cutAction);
			map.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, mask), DefaultEditorKit.selectAllAction);
		}
	}

	/**
	 * 対象フレームの要素をリフレッシュします
	 * @param target 対象フレーム
	 * @param children 子要素
	 */
	public default void refreshJFrame(JFrame target, Component... children) {
		// フレーム要素のクリア
		target.getContentPane().removeAll();
		for (Component child : children) {
			// フレームへ追加
			target.getContentPane().add(child);
		}
		// 再度レンダリング
		target.revalidate();
		target.repaint();
	}

	/**
	 * 対象フレームを画面の最前面に移動します
	 * @param target 対象フレーム
	 */
	public default void onTopJFrame(JFrame target) {
		target.setAlwaysOnTop(true);
		target.toFront();
		target.requestFocus();
		target.setAlwaysOnTop(false);
	}

}

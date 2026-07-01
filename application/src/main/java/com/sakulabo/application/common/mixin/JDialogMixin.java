package com.sakulabo.application.common.mixin;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.lang.StackWalker.Option;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import javax.swing.JDialog;
import javax.swing.JPanel;

import com.sakulabo.application.common.code.GUIText;
import com.sakulabo.regulation.annotation.UseJITCompiler;
import com.sakulabo.regulation.annotation.UseJITCompiler.JITCompilerOption;

/**
 * GUI向けJDialogミックスイン実装インターフェイスです
 * 
 * @author keeeeeent
 */
public interface JDialogMixin extends AppMixin {

	/**
	 * JDialogの設定を指定します
	 */
	@Documented
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Setting {

		/**
		 * パネル生成メソッドマーカー
		 * @return マーカー
		 */
		String value();
	}

	/**
	 * メソッドハンドラーキャッシュインスタンス
	 */
	public static Map<Class<?>, Map<String, MethodHandle>> METHOD_HANDLER = new ConcurrentHashMap<>();

	/** メソッドタイプ識別子 */
	public static MethodType METHOD_TYPE = MethodType.methodType(JPanel.class, JDialog.class);

	/**
	 * JDialogインスタンスを生成します
	 * @param panelName パネル生成メソッド名称
	 * @param title パネルタイトル
	 * @return JDialogインスタンス
	 * @throws Throwable ダイアログ生成失敗
	 */
	@UseJITCompiler(JITCompilerOption.DONTINLINE)
	public default JDialog createJDialog(String panelName, GUIText title) throws Throwable {

		// 呼び出し元クラス情報取得
		Class<?> clazz = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).getCallerClass();

		// コンポーネント生成
		JDialog dialog = new JDialog(getParentFrame(), title.toString(), true);

		// メソッドハンドル取得
		Map<String, MethodHandle> targetClass = METHOD_HANDLER.get(clazz);
		if (!METHOD_HANDLER.containsKey(clazz) || !targetClass.containsKey(panelName)) {
			addCache(clazz, panelName);
		}

		// キャッシュ追加後、ルックアップ可能な状態になるため再度キャッシュを検索
		if (Objects.isNull(targetClass)) {
			targetClass = METHOD_HANDLER.get(clazz);
		}
		// メソッドハンドル取得
		MethodHandle handle = targetClass.get(panelName);

		// 書き込みフェンス
		VarHandle.storeStoreFence();
		// パネル生成
		JPanel panel = (JPanel) handle.invokeExact(dialog);
		// 読み込みフェンス
		VarHandle.loadLoadFence();

		// レイアウト設定
		dialog.setLayout(new BorderLayout());
		dialog.getContentPane().add(panel, BorderLayout.CENTER);
		dialog.setResizable(false);
		dialog.setSize(600, 400);
		dialog.setLocationRelativeTo(getParentFrame());

		return dialog;
	}

	/**
	 * クラスを解析しメソッドハンドルを生成します
	 * @param clazz 解析対象クラス
	 * @param panelName パネル名称
	 * @throws Exception 解析失敗
	 */
	private void addCache(Class<?> clazz, String panelName) throws Exception {

		// メソッド情報取得
		Method method = Stream.of(clazz.getDeclaredMethods())
				.filter(f -> f.isAnnotationPresent(JDialogMixin.Setting.class))
				.filter(f -> f.getDeclaredAnnotation(JDialogMixin.Setting.class).value().equals(panelName))
				.findFirst()
				.get();

		// メソッドハンドル生成
		MethodHandle handle = MethodHandles.privateLookupIn(clazz, MethodHandles.lookup())
				.findVirtual(clazz, method.getName(), METHOD_TYPE)
				.bindTo(this);

		// キャッシュ追加
		if (!METHOD_HANDLER.containsKey(clazz)) {
			Map<String, MethodHandle> entry = new HashMap<>();
			entry.put(panelName, handle);
			METHOD_HANDLER.put(clazz, entry);
		} else {
			METHOD_HANDLER.get(clazz).put(panelName, handle);
		}

	}

	/**
	 * 親とするコンポーネント
	 * @return コンポーネント
	 */
	public Frame getParentFrame();

}

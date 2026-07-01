package com.sakulabo.application.common.mixin;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VolatileCallSite;
import java.lang.ref.Cleaner;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import javax.swing.AbstractButton;
import javax.swing.JComboBox;

import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;

/**
 * GUI向けActionListenerミックスイン実装インターフェイスです
 * 
 * @author keeeeeent
 */
public interface ActionListenerMixin extends AppMixin, ActionListener {

	/**
	 * ActionListenerの設定を指定します
	 */
	@Documented
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface ActionCommand {

		/**
		 * アクションコマンドを設定します
		 * @return アクションコマンド文字列
		 */
		String value();

	}

	/**
	 * メソッドハンドラーキャッシュインスタンス
	 */
	public static Map<Component, CallSite> METHOD_HANDLER = new ConcurrentHashMap<>();

	/**
	 * クリーナーインスタンス
	 */
	public static Cleaner CLEANER = Cleaner.create();

	/**
	 * メソッドハンドラーを解析しキャッシュに追加します
	 * @param target 追加先
	 * @param item 
	 * @param actionCommand アクションコマンド
	 * @throws IllegalAccessException ハンドラーアクセスエラー
	 * @throws NoSuchMethodException メソッド不明
	 * @return クリーナー
	 */
	public default Optional<Cleaner.Cleanable> addCache(ActionListener target, AbstractButton item,
			String actionCommand)
			throws IllegalAccessException, NoSuchMethodException {
		// 本来ありえないが既に生成済みの場合は処理をスキップ
		if (METHOD_HANDLER.containsKey(item)) {
			return Optional.empty();
		}
		return initComponent(target, item, actionCommand);
	}

	/**
	 * メソッドハンドラーを解析しキャッシュに追加します
	 * @param target 追加先
	 * @param item 
	 * @param actionCommand アクションコマンド
	 * @throws IllegalAccessException ハンドラーアクセスエラー
	 * @throws NoSuchMethodException メソッド不明
	 * @return クリーナー
	 */
	public default Optional<Cleaner.Cleanable> addCache(ActionListener target, JComboBox<?> item, String actionCommand)
			throws IllegalAccessException, NoSuchMethodException {
		// 本来ありえないが既に生成済みの場合は処理をスキップ
		if (METHOD_HANDLER.containsKey(item)) {
			return Optional.empty();
		}
		return initComponent(target, item, actionCommand);
	}

	/**
	 * メソッドハンドラーを解析しキャッシュに追加します
	 * @param target 追加先
	 * @param item 
	 * @param actionCommand アクションコマンド
	 * @throws IllegalAccessException ハンドラーアクセスエラー
	 * @throws NoSuchMethodException メソッド不明
	 * @return クリーナー
	 */
	private Optional<Cleaner.Cleanable> initComponent(ActionListener target, Component item, String actionCommand)
			throws IllegalAccessException, NoSuchMethodException {
		// クラス情報解析
		Class<?> methodClazz = target.getClass();
		// メソッド解析
		Method methodName = Stream.of(methodClazz.getDeclaredMethods())
				.filter(f -> f.isAnnotationPresent(JMenuItemMixin.ActionCommand.class))
				.filter(f -> actionCommand.equals(f.getDeclaredAnnotation(JMenuItemMixin.ActionCommand.class).value()))
				.findFirst()
				.get();
		// タイプ生成
		MethodType methodType;
		boolean isNoParam = methodName.getParameterCount() == 0;
		if (isNoParam) {
			methodType = MethodType.methodType(void.class);
		} else {
			methodType = MethodType.methodType(void.class, ActionEvent.class);
		}
		// ハンドラー取得
		MethodHandle methodHandle = MethodHandles.privateLookupIn(methodClazz, MethodHandles.lookup())
				.findVirtual(methodClazz, methodName.getName(), methodType)
				.bindTo(target);
		// パラメータがない場合、仮のパラメータを設定
		if (isNoParam) {
			// 仮のパラメータは無視されるため、invoke呼び出しが統一できる
			methodHandle = MethodHandles.dropArguments(methodHandle, 0, ActionEvent.class);
		}
		// コールサイト生成
		CallSite site = new VolatileCallSite(methodHandle);
		// キャッシュ追加
		METHOD_HANDLER.put(item, site);
		// アクション設定
		if (item instanceof AbstractButton actionitem) {
			actionitem.setActionCommand(actionCommand);
			actionitem.addActionListener(target);
		} else if (item instanceof JComboBox<?> actionitem) {
			actionitem.setActionCommand(actionCommand);
			actionitem.addActionListener(target);
		} else {
			throw new IllegalArgumentException();
		}
		// クリーナー設定
		Cleaner.Cleanable cleanable = CLEANER.register(target, () -> METHOD_HANDLER.remove(item));
		return Optional.ofNullable(cleanable);
	}

	/** {@inheritDoc} */
	@Override
	public default void actionPerformed(ActionEvent e) {

		// コールサイト取得
		CallSite site = METHOD_HANDLER.get(e.getSource());

		if (Objects.nonNull(site)) {

			// ハンドラー取得
			MethodHandle methodHandle = site.dynamicInvoker();

			// ボタン非活性化
			Object component = e.getSource();
			if (component instanceof AbstractButton btn) {
				btn.setEnabled(false);
			}

			try {
				// ハンドラー実行
				methodHandle.invoke(e);
			} catch (Throwable e1) {
				KagerowLogger.newAppLogger().err(e1);
			} finally {
				// ボタン活性化
				if (component instanceof AbstractButton btn) {
					btn.setEnabled(true);
				}
			}
		}

	}

}
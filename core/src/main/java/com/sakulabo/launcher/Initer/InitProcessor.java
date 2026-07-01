package com.sakulabo.launcher.Initer;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import com.sakulabo.launcher.Main;

/**
 * 初期化処理の実装を提供する規定インターフェイスです
 * @author keeeeeent
 */
@FunctionalInterface
public interface InitProcessor {

	/** オブザーバーリスト */
	public final static List<Runnable> observer = new ArrayList<>();

	/** 出力ログフォーマット(開始文字) */
	public final String start = "\u001b[00;35m";
	/** 出力ログフォーマット(終了文字) */
	public final String end = "\u001b[00m";
	/** 出力ログフォーマット(形式) */
	public final String format = "[%sINIT%s] InitProsesser<%s> (%d/%d) -- ";

	/**
	 * AOP出力ログメッセージ設定アノテーション
	 */
	@Documented
	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface LogMessage {
		/**
		 * ログメッセージ
		 * @return ログメッセージ
		 */
		String value() default "";
	}

	/** 初期化カウンター */
	public final static AtomicInteger COUNT = new AtomicInteger();

	/** 初期化カウンター(最大値) */
	public final static AtomicInteger MAX_COUNT = new AtomicInteger();

	/**
	 *  初期化処理の実装を実行します
	 * @throws InitProcessFailedException 
	 */
	public abstract void init() throws InitProcessFailedException;

	/**
	 * Proxyインスタンス生成メソッド
	 * @param clazz インスタンス生成処理
	 * @return Proxyインスタンス
	 */
	public static InitProcessor create(Supplier<InitProcessor> clazz) {
		// プロセッサーインスタンス取得
		InitProcessor processor = clazz.get();
		// インスタンス数インクリメント
		MAX_COUNT.incrementAndGet();
		// プロキシインスタンス生成
		return (InitProcessor) Proxy.newProxyInstance(
				processor.getClass().getClassLoader(),
				new Class<?>[] { InitProcessor.class },
				getProxy(processor));
	}

	/**
	 * proxyハンドラー生成メソッド
	 * @param target プロキシされるインスタンス
	 * @return　プロキシインスタンスにラップされたtargetインスタンス
	 */
	private static InvocationHandler getProxy(InitProcessor target) {

		// Invokeハンドラー局所クラス
		class proxyHandler implements InvocationHandler {

			// プロキシ対象
			private InitProcessor target;

			proxyHandler(InitProcessor target) {
				proxyHandler.this.target = target;
			}

			@Override
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				// メソッドが初期化メソッドの場合、カウントアップ
				if (method.getName().equals("init"))
					COUNT.incrementAndGet();
				// メッセージ出力の場合AOP処理を呼び出さない
				if (method.getName().equals("createLogMessage"))
					return target.createLogMessage();
				// 事前処理
				target.startLog(method, args, target.createLogMessage());
				// メソッド呼び出し
				Object result = null;
				try {
					result = method.invoke(target, args);
				} catch (Throwable e) {
					target.errorLod(method, args, e);
					throw e;
				}
				// 事後処理
				target.endLod(method, args, result);
				// 初期化処理終了通知
				observer.stream().forEach(Runnable::run);
				return result;
			}
		}
		;
		return new proxyHandler(target);
	}

	/**
	 * 初期化ログメッセージを生成します
	 * @return 初期化ログメッセージ
	 */
	public default String createLogMessage() {
		Module module = Main.module.orElseThrow(
				() -> new RuntimeException(Initer.createMesssage(Initer.PREFIX, Initer.NO_MODULE)));
		ResourceBundle messages = ResourceBundle.getBundle("config.message.Initer-Message", Locale.getDefault(), module);
		LogMessage logMessage = getClass().getAnnotation(LogMessage.class);
		String target = Objects.isNull(logMessage) ? getClass().getSimpleName() : logMessage.value();
		return messages.getString(target);
	}

	/**
	 * 処理事前処理
	 * @param method メソッド呼び出しハンドラー
	 * @param args メソッド呼び出し引数
	 * @param message ログメッセージ
	 */
	public default void startLog(Method method, Object[] args, String message) {
		final Object[] param = { start, end, message, COUNT.get(), MAX_COUNT.get() };
		String msg = String.format(format, param);
		System.out.print(msg);
	}

	/**
	 * 処理事後処理<br/>
	 * @param method メソッド呼び出しハンドラー
	 * @param args メソッド呼び出し引数
	 * @param result メソッド呼び出し結果
	 */
	public default void endLod(Method method, Object[] args, Object result) {
		System.out.println("complete");
	}

	/**
	 * エラー処理<br/>
	 * @param method メソッド呼び出しハンドラー
	 * @param args メソッド呼び出し引数
	 * @param error スローされた例外
	 */
	public default void errorLod(Method method, Object[] args, Throwable error) {
		System.out.println(error);
	}

}

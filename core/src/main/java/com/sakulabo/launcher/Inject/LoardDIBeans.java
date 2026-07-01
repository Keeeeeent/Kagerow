package com.sakulabo.launcher.Inject;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.ServiceLoader.Provider;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Stream;

import com.sakulabo.core.Common.VMOption;
import com.sakulabo.regulation.spi.LoardDIBeansAdapter;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * DIのBeanロード実装を提供する規定クラスです
 * @author keeeeeent
 * @param <K> 
 */
public final class LoardDIBeans<K> implements ClassFileTransformer, Runnable {

	/** JVM監視インスタンス取得 */
	private static Instrumentation inst;
	/** アダプターインスタンス */
	private final LoardDIBeansAdapter<K> adapter;
	/** Beanインスタンス保持メモリ空間 */
	private volatile Map<K, ?> context;
	/** Beanインスタンス生成メモリ空間 */
	private final LinkedBlockingQueue<Target> queue;
	/** Beanインスタンス生成スレッド */
	private final Thread thread;
	/** 出力ログフォーマット(開始文字) */
	private static final String start = "\u001b[00;35m";
	/** 出力ログフォーマット(終了文字) */
	private static final String end = "\u001b[00m";
	/** 出力ログフォーマット(形式) */
	private static final String format = "[%sINIT%s] DIProsesser<%s> --> %s";

	/**
	 * Beanインスタンス生成用レコードクラス
	 * @param name クラス名称
	 * @param loader ロードしたクラスローダー
	 */
	private static record Target(
			String name,
			ClassLoader loader) {
		/**
		 * デフォルトコンストラクタ
		 */
		Target {
			Objects.requireNonNull(name);
			Objects.requireNonNull(loader);
		}
	}

	/** インスタンス生成禁止 */
	@SuppressWarnings({ "unchecked", "deprecation" })
	private LoardDIBeans() {
		// VMオプション取得
		final String target = VMOption.LOARDDIBEANS_ADAPTER.getVMoption();
		// キューの追加
		queue = new LinkedBlockingQueue<>();
		// スレッド追加
		thread = new Thread(this);
		thread.setDaemon(true);
		thread.start();
		// SPIを検索
		@SuppressWarnings("rawtypes")
		final ServiceLoader<LoardDIBeansAdapter> loader = ServiceLoader.load(LoardDIBeansAdapter.class);
		// プロバイダーインスタンス取得処理
		if (Objects.isNull(target)) {
			// アダプターオプションの指定がない場合、最初に見つかったアダプターを採用
			// 万が一アダプターが存在していない場合、デフォルトの空のアダプターを設定
			adapter = loader.findFirst().orElse(LoardDIBeansAdapter.DEFAULT_ADAPTER);
		} else {
			// アダプターオプションの指定がある場合、最初に見つかった対象アダプターを採用
			// 万が一アダプターが存在していない場合、デフォルトの空のアダプターを設定
			adapter = loader.stream().map(Provider::get)
					.filter(f -> f.getClass().getName().equals(target))
					.findFirst()
					.orElse(LoardDIBeansAdapter.DEFAULT_ADAPTER);
		}
		// 使用するプロバイダーをコンソールに通知として表示
		printLog(adapter);
		// コンテキストの初期化
		context = adapter.createContext();
	}

	/**
	 * JVM監視新スタンスを設定します
	 * @param inst JVM監視インスタンス
	 */
	public static void init(Instrumentation inst) {
		if (Objects.nonNull(LoardDIBeans.inst)) {
			throw new IllegalCallerException("既にInstrumentationは設定済みです");
		}
		Objects.requireNonNull(inst, "Instrumentationは必須です");
		// インスタンスをメモリで保持
		LoardDIBeans.inst = inst;
	}

	/**
	 * Bean生成処理を開始します
	 * @return 管理しているアダプター
	 * @throws Exception 初期化中に例外が発生した場合
	 */
	public static LoardDIBeansAdapter<?> start() throws Exception {
		// トランスフォーマー生成
		@SuppressWarnings("rawtypes")
		LoardDIBeans transformer = new LoardDIBeans();
		// ログメッセージ初期化
		String message = "EmptyRun", prefix = "Initialize";
		// クラス監視インスタンスが存在する場合
		if (Objects.nonNull(inst)) {
			// トランスフォーマーの登録
			LoardDIBeans.inst.addTransformer(transformer);
			// 既にロード済みクラスのBean生成
			Stream.of(LoardDIBeans.inst.getAllLoadedClasses())
					.map(Class::getName)
					.forEach(transformer::setBean);
			// 初期化処理実行
			transformer.adapter.init();
			// メッセージ変更
			message = "Running";
		}
		// ログ出力
		final Object[] param = { start, end, prefix, message };
		String msg = String.format(format, param);
		System.out.println(msg);
		// アダプターを返却
		return transformer.adapter;
	}

	/**
	 * 処理実行ログ
	 * @param target 接続アダプター
	 */
	@SuppressWarnings("deprecation")
	private void printLog(LoardDIBeansAdapter<?> target) {
		String preFix = "EmptyRun";
		String message = LoardDIBeansAdapter.DEFAULT_ADAPTER.toString();
		if (LoardDIBeansAdapter.DEFAULT_ADAPTER != target) {
			preFix = "Connected";
			message = target.getClass().getCanonicalName();
		}
		final Object[] param = { start, end, preFix, message };
		String msg = String.format(format, param);
		System.out.println(msg);
	}

	/** {@inheritDoc} */
	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		registTarget(className, loader);
		return ClassFileTransformer.super.transform(loader, className, classBeingRedefined,
				protectionDomain, classfileBuffer);
	}

	/** {@inheritDoc} */
	@Override
	public byte[] transform(Module module, ClassLoader loader, String className, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		registTarget(className, loader);
		return ClassFileTransformer.super.transform(module, loader, className, classBeingRedefined,
				protectionDomain, classfileBuffer);
	}

	/**
	 * 対象をキューに追加します
	 * @param name 名称
	 * @param loader クラスローダー
	 */
	private void registTarget(String name, ClassLoader loader) {
		// 登録対象外の場合、キューに追加しない
		if (name.startsWith("com/sakulabo/launcher")
				|| name.startsWith("com/sakulabo/regulation")
				|| name.startsWith("com/sakulabo/core")
				|| name.startsWith("java/")
				|| name.startsWith("javax/")
				|| name.startsWith("jakarta/")
				|| name.startsWith("jdk/")
				|| name.startsWith("sun/misc")
				|| name.startsWith("sun/reflect")
				|| name.startsWith("sun/awt")
				|| name.startsWith("com/sun")) {
			return;
		}
		// 対象の場合キューに追加
		Target target = new Target(name, loader);
		try {
			queue.put(target);
		} catch (InterruptedException e) {
			;
		}
	}

	/**
	 * @param <V>
	 * @param key 検索キー
	 * @return 検索結果
	 */
	@SuppressWarnings("unchecked")
	public final <V> Optional<V> lookUp(K key) {
		return (Optional<V>) adapter.lookUp(context, key);
	}

	/**
	 * DI対象インスタンス格納処理テンプレートメソッド
	 * @param className DI対象クラス名
	 */
	private void setBean(String className) {
		// クラスインスタンス取得
		Class<?> clazz = searchClass(className);
		// インスタンスが取得できた場合、アダプターに処理を移譲
		if (Objects.nonNull(clazz)) {
			// アダプターよりBeanを取得
			Optional<?> bean = adapter.load(clazz);
			// Benaのインスタンスが取得できた場合、Beanの登録をする
			if (bean.isPresent()) {
				adapter.regist(context, bean.get());
			}
		}
	}

	/**
	 * 完全修飾クラス名変換メソッド
	 * @param className クラス名称
	 * @return  完全修飾クラス名
	 */
	private Class<?> searchClass(String className) {
		// ロード対象の場合、ロードできる形式に変換
		final String target = className.replace("/", ".");
		try {
			Class<?> result = Class.forName(target);
			return result;
		} catch (ClassNotFoundException e) {
			return null;
		}
	}

	/** {@inheritDoc} */
	@Override
	@SuppressFBWarnings(justification = "if分岐によってロック解除有無が別れているが、キューが空になった時点で必ずロック解除をしているため問題なし", value = {
			"UL_UNRELEASED_LOCK_EXCEPTION_PATH" })
	public void run() {
		// 処理開始フラグ
		boolean roopFlg = false;
		while (true) {
			try {
				// キューが全て処理された場合、waitセットで待機しているスレッドに通知
				if (queue.isEmpty() && Objects.nonNull(adapter)) {
					// 処理終了を通知
					synchronized (adapter) {
						adapter.notify();
					}
				}
				// 処理対象を取得
				Target target = queue.take();
				if (LoardDIBeansAdapter.LOCK.getHoldCount() == 0) {
					// キューに処理対象が存在し、ロックが未取得の場合
					// ロックを取得しBean生成処理を開始
					LoardDIBeansAdapter.LOCK.lock();
					roopFlg = true;
				}
				String className = target.name();
				// Beanの生成処理実行
				setBean(className);
			} catch (Exception e) {
				;
			} finally {
				// 処理後に処理対象が存在、しない場合
				if (queue.isEmpty()) {
					if (roopFlg) {
						// ロックを保持している場合ロックを解放し、フラグをおる
						LoardDIBeansAdapter.LOCK.unlock();
						roopFlg = false;
					}
				}
			}
		}
	}

}

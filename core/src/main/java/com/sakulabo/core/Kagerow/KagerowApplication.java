package com.sakulabo.core.Kagerow;

import java.util.Hashtable;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.sakulabo.core.Common.ErrorMessage;
import com.sakulabo.core.Common.VMOption;
import com.sakulabo.core.Kagerow.Adapter.KagerowDIContextAdapter;
import com.sakulabo.core.Kagerow.Contents.Impl.KagerowSecurityContentImpl;
import com.sakulabo.core.Kagerow.Context.Impl.KagerowContextImpl;
import com.sakulabo.core.Kagerow.Context.Impl.KagerowSecurityContextImpl;
import com.sakulabo.core.Kagerow.Context.Impl.KagerowSettingContextImpl;
import com.sakulabo.core.Kagerow.Exception.ApplicationError;
import com.sakulabo.core.Kagerow.Spi.KagerowAutomaticStarter;
import com.sakulabo.core.Kagerow.Utilities.KagerowConfiguration;
import com.sakulabo.core.Kagerow.Utilities.KagerowFileHeaderReader;
import com.sakulabo.core.Kagerow.Utilities.KagerowJMX;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;
import com.sakulabo.core.Processor.config.AppConfigurationLorder;
import com.sakulabo.core.Processor.config.AppObjectInputFilter;
import com.sakulabo.core.Processor.jmx.AppJMXInitializer;
import com.sakulabo.core.Processor.log.AppLogger;
import com.sakulabo.core.Provides.LoardDIBeansProvider;
import com.sakulabo.launcher.Inject.LoardDIBeans;
import com.sakulabo.regulation.spi.LoardDIBeansAdapter;

/**
 * Kagerowアプリケーションの基幹クラスです
 * 
 * @author keeeeeent
 */
public final class KagerowApplication {

	// ##########################################################################
	// # 共通外部向け定義
	// ##########################################################################

	/**
	 * Kagerowアプリケーション起動モードを表す列挙クラスです
	 */
	public static enum Mode {

		/** GUIモード */
		GUI("GUI"),
		/** CLIモード */
		CLI("CLI");

		/** 起動モード文字列表現 */
		private String name;

		/**
		 * デフォルトコンストラクタ
		 * @param name 起動モード文字列表現
		 */
		private Mode(String name) {
			this.name = name;
		}

		/** {@inheritDoc} */
		@Override
		public String toString() {
			return name;
		}

	}

	// ##########################################################################
	// # 内部共通定義
	// ##########################################################################

	/** Kagerowコンテキスト保持フィールド */
	private volatile Context context;
	/** Kagerowコンフィグレーション保持フィールド */
	private static final KagerowConfiguration config = AppConfigurationLorder.getConfig();
	/** Kagerowインスタンス保持フィールド */
	private static volatile KagerowApplication application;
	/** KagerowDIコンテキスト保持フィールド */
	private static volatile LoardDIBeansAdapter<Binding> DIContext;
	/** JMX管理インスタンス */
	private static volatile KagerowJMX jmx;
	/** 初期化済みフラグ */
	private static final AtomicBoolean initFlag = new AtomicBoolean();

	/**
	 * デフォルトコンストラクタ<br/>
	 * ※外部からの実行は禁止
	 * 
	 * @param password アプリケーション暗号化解除パスワード
	 */
	private KagerowApplication(String... password) {

		try {

			// デシリアライズホワイトリスト設定
			AppObjectInputFilter.initialize();

			// JMX初期化
			jmx = AppJMXInitializer.initialize();

			// コンテキストインスタンス生成
			Hashtable<String, String> env = new Hashtable<>();
			env.put(Context.INITIAL_CONTEXT_FACTORY, KagerowContextImpl.FACTORY_SPI_NAME);
			// セキュリティコンテキスト生成設定
			if (password.length == 1) {
				String pass = password[0];
				if (Objects.nonNull(pass)) {
					env.put(KagerowContextImpl.SECURE_KEY, pass);
				}
			}
			context = new InitialContext(env);

		} catch (Exception e) {
			// コンテキスト設定ファイル取得失敗
			// アプリケーションを終了
			throw new ApplicationError(e);
		}
	}

	// ##########################################################################
	// # アクセッサーメソッド
	// ##########################################################################

	/**
	 * アプリケーションコンフィグの取得
	 * @return アプリケーションコンフィグ
	 */
	public static KagerowConfiguration getConfig() {
		return config;
	}

	/**
	 * アプリケーションコンテキストの取得
	 * @return アプリケーションコンテキスト
	 */
	public Context getContext() {
		return context;
	}

	/**
	 * DIコンテキストの取得
	 * @return DIコンテキスト
	 */
	public LoardDIBeansAdapter<Binding> getDIContext() {
		return DIContext;
	}

	/**
	 * アプリケーションロガーの取得
	 * @return アプリケーションロガーインスタンス
	 */
	public KagerowLogger getLogger() {
		return AppLogger.getLogger();
	}

	/**
	 * アプリケーション監視インスタンスの取得<br/>
	 * アプリケーション初期化前に呼び出しを行うとnullを返却します
	 * @return アプリケーション監視インスタンス
	 */
	public synchronized static KagerowJMX getJMX() {
		Objects.requireNonNull(jmx, ErrorMessage.CODE_022.toString());
		return jmx;
	}

	/**
	 * アプリケーションのバージョン情報を取得します
	 * @return バージョン情報
	 */
	public static String getVersion() {
		return VMOption.APP_VERSION.getVMoption();
	}

	// ##########################################################################
	// # ファクトリメソッド
	// ##########################################################################

	/**
	 * Kagerowインスタンスを取得します
	 * @param password アプリケーション暗号化解除パスワード
	 * @return Kagerowインスタンス
	 */
	public static KagerowApplication getInstance(String... password) {
		return KagerowApplication.getInstance(null, password);
	}

	/**
	 * Kagerowインスタンスを取得します
	 * @param adapter Beanロード判定機能アダプター
	 * @param password アプリケーション暗号化解除パスワード
	 * @return Kagerowインスタンス
	 */
	@SuppressWarnings("unchecked")
	public static KagerowApplication getInstance(KagerowDIContextAdapter adapter, String... password) {

		if (Objects.isNull(application)) {
			synchronized (KagerowApplication.class) {
				if (Objects.isNull(application)) {
					// アプリケーションコンテキスト生成
					application = new KagerowApplication(password);
					// コンテキスト初期化
					LoardDIBeansProvider.initContext(adapter);
					// 初期値の設定を安定して行うためクラスをロード
					try {
						Class.forName(KagerowFileHeaderReader.class.getName());
					} catch (Exception _) {
						/** ignore */
					}
					try {
						// DIコンテキスト初期化
						DIContext = (LoardDIBeansAdapter<Binding>) LoardDIBeans.start();
						// エントリポイント発火
						((LoardDIBeansProvider) DIContext).startEntryPoint();
					} catch (Exception e) {
						// DIコンテキスト初期化失敗
						// アプリケーションを終了
						throw new ApplicationError(e);
					}
				}
			}
		}

		return application;
	}

	/**
	 * Kagerowアプリケーションを最適な状態で起動します
	 */
	public synchronized static void automaticInstance() {
		// 既に初期化済みか確認
		if (initFlag.get()) {
			// 初期化済みの場合、メソッドを終了
			return;
		}
		// 初期化フラグを立てる
		initFlag.set(true);
		// SPI読み込み
		ServiceLoader<KagerowAutomaticStarter> spi = ServiceLoader.load(KagerowAutomaticStarter.class);
		// SPI実装取得
		Optional<KagerowAutomaticStarter> starter = spi.findFirst();
		// Kagerow初期化
		if (starter.isPresent()) {
			// 実装本体取得
			KagerowAutomaticStarter automaticStarter = starter.get();
			if (KagerowSettingContextImpl.isSecure()) {
				// セキュア起動可能な場合
				String pass = automaticStarter.getPassword();
				if (!automaticStarter.isCancel()) {
					try {
						// パスワードの入力が正常に行われた場合、初期化実施
						KagerowApplication.getInstance(automaticStarter.getAdapter(), pass);
					} catch (ApplicationError e) {
						// パスワード失敗で発生した例外か確認
						Object flug = e.getFlug();
						if (KagerowSecurityContentImpl.PASSWORD_MISS.equals(flug)) {
							// パスワードミスの場合
							automaticStarter.mistake();
						} else {
							// 想定外のエラー
							automaticStarter.unexpected(e);
						}
						// JMX監視停止
						KagerowApplication.getJMX().stop();
					}
				}
			} else {
				// セキュア起動不可能な場合
				KagerowApplication.getInstance(automaticStarter.getAdapter());
			}
		} else {
			// SPI未提供の場合
			KagerowApplication.getInstance();
		}
	}

	// ##########################################################################
	// # ユーティリティメソッド
	// ##########################################################################

	/**
	 * セキュアブートに変更します
	 * @param password パスワード
	 * @throws NamingException コンテキスト生成失敗
	 */
	public void changeToSecureBoot(String password) throws NamingException {
		Objects.requireNonNull(password);
		KagerowContextImpl contextImpl = (KagerowContextImpl) context.lookup(KagerowContextImpl._NAME);
		contextImpl.changeToSecureBoot(password);
	}

	/**
	 * アプリケーションで設定されているパスワードを変更します
	 * @param password 変更後パスワード
	 * @throws NamingException セキュアブート未設定
	 */
	public void changePassword(String password) throws NamingException {
		Objects.requireNonNull(password);
		KagerowSecurityContextImpl contextImpl = (KagerowSecurityContextImpl) context
				.lookup(KagerowSecurityContextImpl._NAME);
		contextImpl.changePass(password);
	}

	/**
	 * アプリケーション起動モードを取得します
	 * @return アプリケーション起動モード列挙クラス
	 */
	public static Mode getApplicationMode() {
		String vmoption = VMOption.APP_INIT_MODE.getVMoption();
		if (Mode.GUI.name.equals(vmoption)) {
			return Mode.GUI;
		} else {
			return Mode.CLI;
		}
	}

}

package com.sakulabo.core.Processor.jmx;

import java.io.IOException;
import java.lang.System.Logger.Level;
import java.lang.management.ManagementFactory;
import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import com.sakulabo.core.Common.StringUtils;
import com.sakulabo.core.Kagerow.KagerowApplication;
import com.sakulabo.core.Kagerow.KagerowApplication.Mode;
import com.sakulabo.core.Kagerow.Exception.AppLogicException;
import com.sakulabo.core.Kagerow.Utilities.KagerowJMX;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;

/**
 * JMX監視設定初期化実装クラス
 *
 * @author keeeeeent
 */
@AppJMX(name = "Notification", options = { "type=AppJMXInitializer" })
public final class AppJMXInitializer extends NotificationBroadcasterSupport
		implements KagerowJMX, Callable<Void>, AppJMXInitializerMXBean {

	/** 監視インスタンス */
	private static volatile AppJMXInitializer APP_JMX_INITIALIZER;

	/** JMXコネクションURI */
	private static final String SERVICE_URI_FORMAT = "service:jmx:rmi://localhost:{0}/jndi/rmi://localhost:{0}/jmxrmi";
	/** JMXコネクション */
	private JMXConnectorServer cs;
	/** JMXレジストリ */
	private Registry registry;

	/** SSL使用有無フラグ */
	private static final boolean JMXREMOTE_SSL_FLUG = Boolean.getBoolean("com.sun.management.jmxremote.ssl");
	/** 認証要否フラグ */
	private static final boolean JMXREMOTE_AUTO_FLUG = Boolean.getBoolean("com.sun.management.jmxremote.authenticate");

	/** JMX管理ポート */
	private static final String JMX_PORT_PROP = "com.sun.management.jmxremote.port";
	/** JMX認証 */
	private static final String JMX_AUTO_PROP = "com.sun.jmx.remote.authenticate";
	/** JMXリモートSSL */
	private static final String JMX_REMOTE_SSL_PROP = "com.sun.jmx.remote.ssl";
	/** JMXサーバーCTX */
	private static final String JMX_FACTORY_CLASS_PROP = "java.naming.factory.initial";
	/** JMXサーバーCTX(完全修飾名) */
	private static final String JMX_FACTORY_CLASS_VAL = "com.sun.jndi.rmi.registry.RegistryContextFactory";

	/** JMX監視オブジェクト（登録時フォーマット） */
	private static final String JMX_TYPE_FORMAT = "{0}:{1}";

	/** ガベージコレクション対象リファレンス格納先 */
	private static final ReferenceQueue<Object> REF_QUE = new ReferenceQueue<>();
	/** ガベージコレクション監視用ファントム参照 */
	private static final Map<PhantomReference<Object>, ObjectName> REF_MAP = new ConcurrentHashMap<>();
	/** ガベージコレクションシーケンサー */
	private static final AtomicLong REF_SEQ = new AtomicLong();
	/** ガベージコレクション対応スレッド */
	private static final ExecutorService EXE_SERVICE = Executors.newSingleThreadExecutor();

	/**
	 * デフォルトコンストラクタ
	 * @param cs JMXコネクション
	 * @param registry JMXレジストリ
	 */
	private AppJMXInitializer(JMXConnectorServer cs, Registry registry) {
		this.cs = cs;
		this.registry = registry;
	}

	/**
	 * JMX初期化処理
	 * @return JMXイニシャライザ
	 * @throws IOException ソケット接続失敗
	 * @throws AppLogicException JMX名称オブジェクト生成失敗
	 */
	public static AppJMXInitializer initialize() throws IOException, AppLogicException {

		if (Objects.isNull(APP_JMX_INITIALIZER)) {
			synchronized (AppJMXInitializer.class) {
				if (Objects.isNull(APP_JMX_INITIALIZER)) {
					// JMXサーバーインスタンス取得
					MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

					// ポート取得
					int port = Integer.getInteger(JMX_PORT_PROP, 0);

					// ポート番号未指定の場合自動割当
					if (port == 0) {
						// ダミーソケットを生成し、空いているポートを取得する
						try (ServerSocket socket = new ServerSocket(port, 0, InetAddress.getLoopbackAddress())) {
							// 直後に再利用できるようTIME_WAIT状態を回避
							socket.setReuseAddress(true);
							// ポート取得(JMX)
							port = socket.getLocalPort();
						}
					}

					// ポートバインド（RMIレジストリ）
					Registry registry = LocateRegistry.createRegistry(port);

					// 自動割当ポートのJMX専用URL生成
					String uriStr = MessageFormat.format(SERVICE_URI_FORMAT, String.valueOf(port));
					JMXServiceURL url = new JMXServiceURL(uriStr);

					// JMX設定初期化
					HashMap<String, Object> env = new HashMap<>();
					env.put(JMX_REMOTE_SSL_PROP, JMXREMOTE_SSL_FLUG);
					env.put(JMX_AUTO_PROP, JMXREMOTE_AUTO_FLUG);
					env.put(JMX_FACTORY_CLASS_PROP, JMX_FACTORY_CLASS_VAL);

					// JMXコネクタ生成
					JMXConnectorServer cs = JMXConnectorServerFactory.newJMXConnectorServer(url, env, mbs);
					// コネクタ開始
					Thread jmxThread = new Thread(() -> {
						try {
							cs.start();
						} catch (IOException e) {
							KagerowLogger.newSystemLogger().log(Level.ERROR, e.getMessage());
						}
					});
					jmxThread.setDaemon(true);
					jmxThread.start();

					// JMXログ
					if (KagerowApplication.getApplicationMode() != Mode.CLI)
						KagerowLogger.newSystemLogger().log(Level.INFO, "JMX Binding Port " + port);

					// 監視インスタンス生成
					AppJMXInitializer appJMXInitializer = new AppJMXInitializer(cs, registry);
					// ガベージコレクション対応開始
					EXE_SERVICE.submit(appJMXInitializer);
					// 監視対象に自分自身を追加（通知を行うために追加する）
					AppJMXInitializer.registMXBean(appJMXInitializer);
					// 監視用インスタンス設置
					APP_JMX_INITIALIZER = appJMXInitializer;
				}
			}
		}

		return APP_JMX_INITIALIZER;

	}

	/** {@inheritDoc} */
	@Override
	public final void stop() {

		// コネクタストップ
		try {
			if (cs != null) {
				cs.stop();
			}
		} catch (Exception e) {
			KagerowLogger.newSystemLogger().log(Level.ERROR, e.getMessage());
		}

		// レジストリ解除
		try {
			if (registry != null) {
				UnicastRemoteObject.unexportObject(registry, true);
			}
		} catch (Exception e) {
			KagerowLogger.newSystemLogger().log(Level.ERROR, e.getMessage());
		}

		// エグゼキューター終了
		try {
			EXE_SERVICE.shutdownNow();
		} catch (Exception e) {
			KagerowLogger.newSystemLogger().log(Level.ERROR, e.getMessage());
		}

	}

	/**
	 * JMXの登録をします
	 * @param target 登録対象
	 * @throws AppLogicException JMX名称オブジェクト生成失敗
	 */
	public static void registMXBean(BaseKagerowJMX target) throws AppLogicException {

		// アノテーション取得
		AppJMX appJMX = target.getClass().getAnnotation(AppJMX.class);

		// 名称へ変換
		StringJoiner joiner = new StringJoiner(StringUtils.COMMA_STR);
		for (String option : appJMX.options()) {
			joiner.add(option);
		}

		try {
			// 名称オブジェクト生成
			ObjectName objectName = new ObjectName(MessageFormat.format(JMX_TYPE_FORMAT,
					new Object[] {
							appJMX.name(),
							joiner.toString()
					}));
			// JMX登録
			registMXBean(target, objectName);
		} catch (MalformedObjectNameException e) {
			throw new AppLogicException("ObjectName can not initialize", e);
		}

	}

	/**
	 * JMXの登録をします
	 * @param target 登録対象
	 * @param objectName トルク名称
	 */
	public static void registMXBean(Object target, ObjectName objectName) {

		// JMXサーバー取得
		MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();

		if (!mBeanServer.isRegistered(objectName)) {
			try {
				// JMX登録処理
				mBeanServer.registerMBean(target, objectName);
				// 登録完了後、ガベージコレクション監視
				PhantomReference<Object> ref = new PhantomReference<Object>(target, REF_QUE);
				REF_MAP.put(ref, objectName);
			} catch (InstanceAlreadyExistsException | MBeanRegistrationException | NotCompliantMBeanException e) {
				// JMXの登録が失敗したとしても実行には影響がないためログ出力のみ実施
				KagerowLogger.newAppLogger().err(e);
			}
		}

	}

	/** {@inheritDoc} */
	@Override
	public Void call() throws Exception {

		try {
			// 開始ログ出力
			if (KagerowApplication.getApplicationMode() != Mode.CLI)
				KagerowLogger.newSystemLogger().log(Level.INFO, "GCMonitor For JMX Start");
			while (true) {
				// ガベージコレクション対象取得
				Reference<?> ref = REF_QUE.remove();
				// 名称取得
				ObjectName name = REF_MAP.remove(ref);
				// 元インスタンス参照削除
				ref.clear();
				// JMX登録解除
				if (Objects.nonNull(name)) {
					// JMXサーバー取得
					MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
					// 登録解除
					mBeanServer.unregisterMBean(name);
					// 登録解除通知
					Notification notif = new Notification(
							"JVM.GC.Notification",
							name.toString(),
							REF_SEQ.getAndIncrement(),
							System.currentTimeMillis(),
							"Success");
					sendNotification(notif);
				}
				// GC促進準備（GC呼び出しはスループットが落ちるため行わない）
				name = null;
				ref = null;
				return null;
			}
		} catch (Exception e) {
			// 終了ログ出力
			if (KagerowApplication.getApplicationMode() != Mode.CLI)
				KagerowLogger.newSystemLogger().log(Level.INFO, "GCMonitor For JMX Stop");
			throw e;
		}

	}

}

package com.sakulabo.application.app.rpc;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.logging.Level;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

import com.sun.net.httpserver.HttpsServer;
import com.sakulabo.application.app.rpc.filters.LoggerFilter;
import com.sakulabo.application.common.spi.RpcTarget;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;
import com.sakulabo.core.Kagerow.Utilities.KagerowUtilities;
import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;

/**
 * アプリケーション共通で使用されるRPCサーバ実装です
 *
 * @author keeeeeent
 */
public final class RpcServer extends HttpsConfigurator {

	/** 暗号化方式規定 */
	private final static SSLContext TLS;
	static {
		try {
			// キーストア生成
			KeyStore ks = KeyStore.getInstance("PKCS12");
			// サーバSSL取込
			Path path = KagerowUtilities.createAppDirPath().resolve("config", "server.p12");
			try (InputStream input = Files.newInputStream(path)) {
				ks.load(input, System.getProperty("instance", "").toCharArray());
			}
			// キーマネージャー生成
			KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			// キーマネージャー初期化
			kmf.init(ks, System.getProperty("instance", "").toCharArray());
			// TLSコンテキスト取得
			SSLContext sslContext = SSLContext.getInstance("TLS");
			// TLSコンテキスト初期化
			sslContext.init(kmf.getKeyManagers(), null, null);
			// TLSコンテキスト保持
			TLS = sslContext;
		} catch (Exception e) {
			throw new Error(e);
		}
	}

	/** Httpサーバインスタンス */
	private final HttpsServer server;
	/** サーバアドレス */
	private final InetSocketAddress address;
	/** 共通フィルター */
	private final List<Filter> commonFilter = List.of(new LoggerFilter());

	/**
	 * デフォルトコンストラクタ
	 *
	 * @param hostname   ホスト名
	 * @param portNumber ポート番号
	 *
	 * @throws NoSuchAlgorithmException SSLContext.getInstance()の呼出しが失敗した場合
	 * @throws IOException              httpsサーバインスタンス生成失敗
	 */
	public RpcServer(final String hostname, final Integer portNumber) throws NoSuchAlgorithmException, IOException {
		super(TLS);
		// ホストを設定
		InetAddress localhost = null;
		if (Objects.nonNull(hostname)) {
			localhost = InetAddress.getByName(hostname);
		} else {
			localhost = InetAddress.getLoopbackAddress();
		}
		// ポート番号を設定
		int port = 0;
		if (Objects.nonNull(portNumber)) {
			port = portNumber.intValue();
		}
		// バインドアドレスを生成
		InetSocketAddress addr = new InetSocketAddress(localhost, port);
		// サーバ生成
		server = HttpsServer.create(addr, -1);
		// バインドされたアドレスを取得
		address = server.getAddress();
		// サーバ設定情報初期化
		server.setHttpsConfigurator(this);
		// サーバ初期設定
		setServer();
	}

	/**
	 * サーバの初期設定を行います
	 */
	private void setServer() {
		// コンテキスト取得
		ServiceLoader<RpcTarget> rpcContextList = ServiceLoader.load(RpcTarget.class);
		// コンテキスト登録
		for (RpcTarget ctx : rpcContextList) {
			// クラス情報取得
			Class<?> ctxClazz = ctx.getClass();
			// RPC対象か判定
			if (ctxClazz.isAnnotationPresent(RpcSetting.class)) {
				// RPC設定情報取得
				RpcSetting setting = ctxClazz.getDeclaredAnnotation(RpcSetting.class);
				// 呼び出しメソッド確認
				for (Method method : ctxClazz.getDeclaredMethods()) {
					// RPC対象メソッドの場合処理
					if (method.isAnnotationPresent(RpcMethod.class)) {
						// RPCメソッド設定情報取得
						RpcMethod rpcMethod = method.getDeclaredAnnotation(RpcMethod.class);
						try {
							// ハンドラ生成
							RpcHttpHandler handler = new RpcHttpHandler(ctx, setting, rpcMethod, method);
							// ハンドラ登録
							HttpContext httpContext = server.createContext(handler.createURL(), handler);
							// 登録ログ出力
							KagerowLogger.newAppLogger()
									.log(Level.INFO, String.format("[BindedBy]:%s [URL]:https://%s:%d%s",
											ctxClazz.getSimpleName(), getHost(), getPort(), handler.createURL()),
											new Object[0]);
							// コンテキスト初期設定
							setHttpContext(httpContext, handler);
						} catch (IllegalAccessException e) {
							// 登録失敗ログ出力
							KagerowLogger.newAppLogger().err(e);
						}
					}
				}
			}
		}
	}

	/**
	 * コンテキストの初期設定を行います
	 *
	 * @param ctx     コンテキスト
	 * @param handler ハンドラ
	 */
	private void setHttpContext(HttpContext ctx, RpcHttpHandler handler) {
		// フィルター一覧取得
		List<Filter> filters = ctx.getFilters();
		// 共通フィルタ設定
		filters.addAll(commonFilter);
		// 固有フィルタ設定
		handler.setFilter(filters);
	}

	/**
	 * サーバを起動します
	 */
	public void start() {
		// サーバ起動
		server.start();
		// 起動完了メッセージ
		KagerowLogger.newAppLogger().log(Level.INFO,
				String.format("Start RPC Server [host]:%s [port]:%d", getHost(), getPort()), new Object[0]);
	}

	/**
	 * サーバを停止します
	 */
	public void stop() {
		// 停止完了メッセージ
		KagerowLogger.newAppLogger().log(Level.INFO, String.format("Stop Request RPC Server", getHost(), getPort()),
				new Object[0]);
		// サーバ起動
		server.stop(10);
		// 停止完了メッセージ
		KagerowLogger.newAppLogger().log(Level.INFO,
				String.format("Stop RPC Server [host]:%s [port]:%d", getHost(), getPort()), new Object[0]);
	}

	/** {@inheritDoc} */
	@Override
	public void configure(HttpsParameters params) {
		super.configure(params);
	}

	/**
	 * ポート番号を取得します
	 *
	 * @return ポート番号
	 */
	public int getPort() {
		return address.getPort();
	}

	/**
	 * ホスト名を取得します
	 *
	 * @return ホスト名
	 */
	public String getHost() {
		return address.getHostName();
	}

}

package com.sakulabo.application.app.rpc;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.ServiceLoader;

import javax.net.ssl.SSLContext;

import com.sun.net.httpserver.HttpsServer;
import com.sakulabo.application.common.spi.RpcTarget;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;

/**
 * アプリケーション共通で使用されるRPCサーバ実装です
 *
 * @author keeeeeent
 */
public final class RpcServer extends HttpsConfigurator {

	/** Httpサーバインスタンス */
	private final HttpsServer server;
	/** サーバアドレス */
	private final InetSocketAddress address;

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
		super(SSLContext.getDefault());
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
						// ハンドラ生成
						RpcHttpHandler handler = new RpcHttpHandler(setting, rpcMethod, method);
						// ハンドラ登録
						server.createContext(handler.createURL(), handler);
					}
				}
			}
		}
	}

	/**
	 * サーバを起動します
	 */
	public void start() {
		server.start();
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

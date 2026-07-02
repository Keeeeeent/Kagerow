package com.sakulabo.application.app.rpc;

import java.io.IOException;
import java.lang.reflect.Method;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * アプリケーション共通で使用されるRPCメソッド呼び出し実装です
 *
 * @author keeeeeent
 */
public class RpcHttpHandler implements HttpHandler {

	/** RPC設定情報 */
	private final RpcSetting rpcSetting;
	/** RPCメソッド設定情報 */
	private final RpcMethod rpcMethod;
	/** 呼び出しメソッド */
	private final Method method;

	/**
	 * デフォルトコンストラクタ
	 *
	 * @param rpcSetting RPC設定情報
	 * @param rpcMethod  RPCメソッド設定情報
	 * @param method     呼び出しメソッド
	 */
	public RpcHttpHandler(RpcSetting rpcSetting, RpcMethod rpcMethod, Method method) {
		this.rpcSetting = rpcSetting;
		this.rpcMethod = rpcMethod;
		this.method = method;
	}

	/**
	 * コンテキスト登録向けURLを生成します
	 *
	 * @return コンテキスト登録向けURL
	 */
	public String createURL() {
		return String.format("/%s/%s", rpcSetting.value(), rpcMethod.methodName());
	}

	/** {@inheritDoc} */
	@Override
	public void handle(HttpExchange exchange) throws IOException {
		// TODO 自動生成されたメソッド・スタブ
	}

}

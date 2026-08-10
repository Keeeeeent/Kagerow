package com.sakulabo.application.app.rpc;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.sakulabo.application.app.rpc.datatype.BaseDataType;
import com.sakulabo.application.common.spi.RpcTarget;
import com.sun.net.httpserver.HttpExchange;

/**
 * アプリケーション共通で使用されるRPCメソッド呼び出し実装です
 *
 * @author keeeeeent
 */
public class RpcHttpHandler {

	/** RPC設定情報 */
	@SuppressWarnings("unused")
	private final RpcSetting rpcSetting;
	/** RPCメソッド設定情報 */
	@SuppressWarnings("unused")
	private final RpcMethod rpcMethod;
	/** 呼び出し対象 */
	@SuppressWarnings("unused")
	private final RpcTarget ctx;
	/** 呼び出しメソッド */
	private final Method method;
	/** 呼び出しハンドラー */
	private final MethodHandle handle;
	/** XSDファイルパス */
	public final String xsdFileName;

	/**
	 * デフォルトコンストラクタ
	 *
	 * @param ctx        呼び出し対象
	 * @param rpcSetting RPC設定情報
	 * @param rpcMethod  RPCメソッド設定情報
	 * @param method     呼び出しメソッド
	 * @throws IllegalAccessException
	 */
	public RpcHttpHandler(RpcTarget ctx, RpcSetting rpcSetting, RpcMethod rpcMethod, Method method)
			throws IllegalAccessException {
		// フィールド初期化
		this.rpcSetting = rpcSetting;
		this.rpcMethod = rpcMethod;
		this.method = method;
		this.ctx = ctx;
		// 呼び出しハンドラー生成
		MethodHandles.Lookup lookup = MethodHandles.lookup();
		handle = lookup.unreflect(method).bindTo(ctx);
		// XSDファイルパス生成
		xsdFileName = "/rpc-xsd/" + String.format("request-%s-%s.xsd", rpcSetting.value(), rpcMethod.value());
	}

	/**
	 * RPCメソッド呼び出し実装です
	 *
	 * @param exchange リクエスト
	 * @param rawData  メソッドパラメータ
	 * @return 実行結果
	 * @throws Throwable RPCメソッド呼び出し失敗
	 */
	public Map<String, BaseDataType<?>> handle(HttpExchange exchange, Map<String, String> rawData) throws Throwable {

		// パラメータマッピング
		List<BaseDataType<?>> params = new ArrayList<>();
		for (Parameter param : method.getParameters()) {
			if (!param.isAnnotationPresent(RpcMethodParam.class)) {
				throw new IllegalStateException("Not RPC Method");
			}
			// RPCマッピングパラメータ名称取得
			RpcMethodParam methodParam = param.getDeclaredAnnotation(RpcMethodParam.class);
			// パラメータ型定義取得
			Class<?> paramClazz = param.getType();
			// クライアントからのパラメータを取得
			String clientParam = methodParam.value();
			// コンストラクタ向けパラメータ取得
			String forConstructorParam = rawData.get(clientParam);
			// 必須パラメータチェック
			if (methodParam.required()) {
				if (Objects.isNull(forConstructorParam)) {
					throw new IllegalArgumentException(String.format("%s is Required", clientParam));
				}
			}
			// パラメータインスタンス取得
			BaseDataType<?> paramInstance = (BaseDataType<?>) paramClazz.getConstructor(String.class)
					.newInstance(forConstructorParam);
			// インスタンス保管
			params.add(paramInstance);
		}

		// メソッド呼び出し
		Object result = handle.invokeWithArguments(params.toArray());

		// 結果のマップ変換
		final Map<String, BaseDataType<?>> resultMap = new HashMap<>();
		if (Objects.nonNull(result)) {
			// クラス情報取得
			Class<?> resultClazz = result.getClass();
			while (Object.class != resultClazz) {
				for (Field field : resultClazz.getDeclaredFields()) {
					if (field.isAnnotationPresent(RpcSendParam.class)) {
						// 返却パラメータ取得
						final RpcSendParam resultParam = field.getDeclaredAnnotation(RpcSendParam.class);
						// アクセス許可
						field.setAccessible(true);
						// パラメータインスタンス取得
						BaseDataType<?> paramInstance = (BaseDataType<?>) field.get(result);
						// インスタンス保管
						resultMap.put(resultParam.value(), paramInstance);
					}
				}
				resultClazz = resultClazz.getSuperclass();
			}
		}

		// 結果返却
		return resultMap;

	}

}

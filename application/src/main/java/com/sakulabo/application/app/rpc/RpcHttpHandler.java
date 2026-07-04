package com.sakulabo.application.app.rpc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.Document;

import com.sakulabo.application.app.rpc.datatype.BaseDataType;
import com.sakulabo.application.common.spi.RpcTarget;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;
import com.sun.net.httpserver.Filter;
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
	/** 呼び出し対象 */
	private final RpcTarget ctx;
	/** 呼び出しメソッド */
	private final Method method;
	/** 呼び出しハンドラー */
	private final MethodHandle handle;

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

		// リクエストをXMLへ変換
		try (InputStream input = exchange.getRequestBody()) {

			// DOMファクトリ生成
			DocumentBuilderFactory factory = DocumentBuilderFactory.newDefaultInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document dom = builder.parse(input);

			// XSDファクトリ生成
			SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			// TODO XSDによるバリデーション
			// スキーマインスタンス
			Schema xmlSchema = null;
			// XSD取得
			String xsdFileName = "/" + "";
			// XSD読み込み
			try (InputStream xsd = getClass().getResourceAsStream(xsdFileName)) {
				// XSDストリーム生成
				StreamSource source = new StreamSource(xsd);
				// セキュアプロセッシング有効化
				schemaFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
				// 外部DTDアクセス禁止
				schemaFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
				// 外部Schemaアクセス禁止
				schemaFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
				// スキーマ生成
				xmlSchema = schemaFactory.newSchema(source);
				// Validatorの生成
				Source xmlSource = new DOMSource(dom);
				Validator validator = xmlSchema.newValidator();
				validator.validate(xmlSource);
			}

			// TODO データ変換
			Map<String, String> rawData = new HashMap<>();

			// パラメータマッピング
			List<BaseDataType<?>> params = new ArrayList<>();
			for (Parameter param : method.getParameters()) {
				if (!param.isAnnotationPresent(RpcMethodParam.class)) {
					return;
				}
				// RPCマッピングパラメータ名称取得
				RpcMethodParam methodParam = param.getDeclaredAnnotation(RpcMethodParam.class);
				// パラメータ型定義取得
				Class<?> paramClazz = param.getClass();
				// コンストラクタ向けパラメータ取得
				String forConstructorParam = rawData.get(methodParam.value());
				// パラメータインスタンス取得
				BaseDataType<?> paramInstance = (BaseDataType<?>) paramClazz.getConstructor(String.class)
						.newInstance(forConstructorParam);
				// インスタンス保管
				params.add(paramInstance);
			}

			// メソッド呼び出し
			Object result = handle.invoke(params.toArray());

			// TODO レスポンスをXMLへ変換（成功時）
			try (OutputStream output = exchange.getResponseBody()) {

			}

			// TODO レスポンスコード設定
			exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);

		} catch (Throwable e) {
			// ロガー書き出し
			KagerowLogger.newAppLogger().err(e);
			// TODO レスポンスをXMLへ変換（失敗時）
			// TODO レスポンスコード設定
			exchange.sendResponseHeaders(HttpURLConnection.HTTP_INTERNAL_ERROR, 0);
		}
	}

	/**
	 * フィルタの固有設定を行います
	 *
	 * @param filters フィルターリスト
	 */
	public void setFilter(final List<Filter> filters) {
		for (RpcFilter rpcFilter : method.getDeclaredAnnotationsByType(RpcFilter.class)) {
			// フィルタ情報取得
			Class<? extends Filter> filterClazz = rpcFilter.value();
			try {
				// インスタンス生成
				Filter filter = filterClazz.getConstructor().newInstance();
				// フィルタ追加
				filters.add(filter);
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException e) {
				// インスタンス生成失敗の場合、ログを記録し処理を続行
				KagerowLogger.newAppLogger().err(e);
				// 必須フィルタの場合サーバ起動を強制終了
				if (rpcFilter.required()) {
					throw new IllegalStateException(e);
				}
			}
		}
	}

}

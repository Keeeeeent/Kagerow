package com.sakulabo.application.app.rpc;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import com.sakulabo.application.app.rpc.datatype.BaseDataType;
import com.sakulabo.application.app.rpc.datatype.RpcDataTypes;
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

	/** 共通レスポンスXMLファイルパス */
	private static final String COMMON_RESPONSE_XML = "rpc-xml/common-response.xml";
	/** 共通FaultレスポンンスXMLファイルパス */
	private static final String COMMON_FAULT_XML = "rpc-xml/common-fault.xml";

	/** 共通レスポンスXMLファイル文字列 */
	private static String COMMON_RESPONSE_XML_STR;
	static {
		try (InputStream input = ClassLoader.getSystemClassLoader().getResourceAsStream(COMMON_RESPONSE_XML);
				InputStreamReader converter = new InputStreamReader(input, StandardCharsets.UTF_8);
				BufferedReader reader = new BufferedReader(converter)) {
			COMMON_RESPONSE_XML_STR = reader.readAllAsString();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	/** 共通FaultレスポンンスXMLファイル文字列 */
	private static String COMMON_FAULT_XML_STR;
	static {
		try (InputStream input = ClassLoader.getSystemClassLoader().getResourceAsStream(COMMON_FAULT_XML);
				InputStreamReader converter = new InputStreamReader(input, StandardCharsets.UTF_8);
				BufferedReader reader = new BufferedReader(converter)) {
			COMMON_FAULT_XML_STR = reader.readAllAsString();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

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
	/** XSDファイルパス */
	private final String xsdFileName;

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
		xsdFileName = "/rpc-xsd/" + rpcMethod.xsd();
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
				// バリデーターの生成
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
				// クライアントからのパラメータを取得
				String clientParam = methodParam.value();
				// 必須パラメータチェック
				if (methodParam.required()) {
					if (Objects.isNull(clientParam)) {
						throw new IllegalArgumentException(String.format("%s is Required", methodParam.value()));
					}
				}
				// コンストラクタ向けパラメータ取得
				String forConstructorParam = rawData.get(clientParam);
				// パラメータインスタンス取得
				BaseDataType<?> paramInstance = (BaseDataType<?>) paramClazz.getConstructor(String.class)
						.newInstance(forConstructorParam);
				// インスタンス保管
				params.add(paramInstance);
			}

			// メソッド呼び出し
			Object result = handle.invoke(params.toArray());
			// 結果のマップ変換
			final Map<String, BaseDataType<?>> resultMap = new HashMap<>();
			if (Objects.nonNull(result)) {
				// クラス情報取得
				Class<?> resultClazz = result.getClass();
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
			}

			// TODO レスポンスをXMLへ変換（成功時）
			try (OutputStream output = exchange.getResponseBody()) {

				// レスポンスRPC-XMLファクトリ生成
				DocumentBuilderFactory responseXMLFactory = DocumentBuilderFactory.newDefaultInstance();
				DocumentBuilder responseXMLBuilder = responseXMLFactory.newDocumentBuilder();
				StringReader baseXML = new StringReader(COMMON_RESPONSE_XML_STR);
				InputSource responseInputSource = new InputSource(baseXML);
				Document responseXML = responseXMLBuilder.parse(responseInputSource);

				// Xpath取得
				XPathFactory xPathFactory = XPathFactory.newInstance();
				XPath xPath = xPathFactory.newXPath();
				XPathExpression expr = xPath.compile("/methodResponse/params/param/struct");
				Node node = (Node) expr.evaluate(responseXML, XPathConstants.NODE);

				// 返却パラメータ追加
				for (Entry<String, BaseDataType<?>> structParam : resultMap.entrySet()) {

					// メンバー追加
					Element memberElem = responseXML.createElement("member");

					// パラメータ名称追加
					Element nameElem = responseXML.createElement("name");
					nameElem.setTextContent(structParam.getKey());
					// サブメンバー追加
					memberElem.appendChild(nameElem);

					// パラメータバリュー追加
					BaseDataType<?> dataType = structParam.getValue();
					Element valueElem = responseXML.createElement("value");
					Element valueSubElem = responseXML.createElement(dataType.toRpcDataType().toString());
					// 取得データがnilか判定
					Optional<?> dat = dataType.getRawType();
					if (dat.isPresent()) {
						valueSubElem.setTextContent(dat.get().toString());
					} else {
						Element nilElem = responseXML.createElement(RpcDataTypes.NIL.toString());
						valueSubElem.appendChild(nilElem);
					}
					// 生成要素追加
					valueElem.appendChild(valueSubElem);
					// サブメンバー追加
					memberElem.appendChild(valueElem);

					// 要素を構造体として追加
					node.appendChild(memberElem);

				}

				/**
				 * XML書き出し
				 */
				try (ByteArrayOutputStream tmpOutput = new ByteArrayOutputStream()) {

					Source xmlSource = new DOMSource(responseXML);
					Result xmlResult = new StreamResult(tmpOutput);
					Transformer transformer = TransformerFactory.newDefaultInstance().newTransformer();
					transformer.setOutputProperty(OutputKeys.INDENT, "yes");
					transformer.setOutputProperty(OutputKeys.ENCODING, StandardCharsets.UTF_8.toString());
					transformer.transform(xmlSource, xmlResult);

					// レスポンス書き出し
					tmpOutput.flush();
					output.write(tmpOutput.toByteArray());

				}

			}

			// TODO レスポンスコード設定
			exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);

		} catch (Throwable e) {

			// ロガー書き出し
			KagerowLogger.newAppLogger().err(e);

			// レスポンスをXMLへ変換（失敗時）
			long size = 0;
			try {
				// 結果のマップ変換
				final Map<String, BaseDataType<?>> resultMap = new HashMap<>();
				// クラス情報取得
				Class<?> resultClazz = e.getClass();
				for (Field field : resultClazz.getDeclaredFields()) {
					if (field.isAnnotationPresent(RpcSendParam.class)) {
						// 返却パラメータ取得
						final RpcSendParam resultParam = field.getDeclaredAnnotation(RpcSendParam.class);
						// アクセス許可
						field.setAccessible(true);
						// パラメータインスタンス取得
						BaseDataType<?> paramInstance = (BaseDataType<?>) field.get(e);
						// インスタンス保管
						resultMap.put(resultParam.value(), paramInstance);
					}
				}

				// TODO レスポンスをXMLへ変換（成功時）
				try (OutputStream output = exchange.getResponseBody()) {

					// レスポンスRPC-XMLファクトリ生成
					DocumentBuilderFactory responseXMLFactory = DocumentBuilderFactory.newDefaultInstance();
					DocumentBuilder responseXMLBuilder = responseXMLFactory.newDocumentBuilder();
					StringReader baseXML = new StringReader(COMMON_FAULT_XML_STR);
					InputSource responseInputSource = new InputSource(baseXML);
					Document responseXML = responseXMLBuilder.parse(responseInputSource);

					// Xpath取得
					XPathFactory xPathFactory = XPathFactory.newInstance();
					XPath xPath = xPathFactory.newXPath();
					XPathExpression expr = xPath.compile("/methodResponse/params/param/struct");
					Node node = (Node) expr.evaluate(responseXML, XPathConstants.NODE);

					// 返却パラメータ追加
					for (Entry<String, BaseDataType<?>> structParam : resultMap.entrySet()) {

						// メンバー追加
						Element memberElem = responseXML.createElement("member");

						// パラメータ名称追加
						Element nameElem = responseXML.createElement("name");
						nameElem.setTextContent(structParam.getKey());
						// サブメンバー追加
						memberElem.appendChild(nameElem);

						// パラメータバリュー追加
						BaseDataType<?> dataType = structParam.getValue();
						Element valueElem = responseXML.createElement("value");
						Element valueSubElem = responseXML.createElement(dataType.toRpcDataType().toString());
						// 取得データがnilか判定
						Optional<?> dat = dataType.getRawType();
						if (dat.isPresent()) {
							valueSubElem.setTextContent(dat.get().toString());
						} else {
							Element nilElem = responseXML.createElement(RpcDataTypes.NIL.toString());
							valueSubElem.appendChild(nilElem);
						}
						// 生成要素追加
						valueElem.appendChild(valueSubElem);
						// サブメンバー追加
						memberElem.appendChild(valueElem);

						// 要素を構造体として追加
						node.appendChild(memberElem);

					}

					/**
					 * XML書き出し
					 */
					try (ByteArrayOutputStream tmpOutput = new ByteArrayOutputStream()) {

						Source xmlSource = new DOMSource(responseXML);
						Result xmlResult = new StreamResult(tmpOutput);
						Transformer transformer = TransformerFactory.newDefaultInstance().newTransformer();
						transformer.setOutputProperty(OutputKeys.INDENT, "yes");
						transformer.setOutputProperty(OutputKeys.ENCODING, StandardCharsets.UTF_8.toString());
						transformer.transform(xmlSource, xmlResult);

						// レスポンス書き出し
						tmpOutput.flush();
						output.write(tmpOutput.toByteArray());

					}

				}
			} catch (Exception exp) {
				// ロガー書き出し
				KagerowLogger.newAppLogger().err(e);
			}
			// レスポンスコード設定
			exchange.sendResponseHeaders(HttpURLConnection.HTTP_INTERNAL_ERROR, size);
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

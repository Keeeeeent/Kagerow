package com.sakulabo.application.app.rpc;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.logging.Level;

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
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.sakulabo.application.app.rpc.datatype.BaseDataType;
import com.sakulabo.application.app.rpc.datatype.RpcDataTypes;
import com.sakulabo.application.common.spi.RpcTarget;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;
import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * アプリケーション共通で使用されるRPCコンテキスト実装です
 *
 * @author keeeeeent
 */
public class RpcHttpHandlerContext implements HttpHandler {

	/** 共通レスポンスXMLファイルパス */
	private static final String COMMON_RESPONSE_XML = "rpc-xml/common-response.xml";

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

	/** RPCメソッドマッピング */
	private final Map<String, RpcHttpHandler> handleMapper = new HashMap<>();
	/** 呼び出し対象 */
	private final RpcTarget ctx;

	/**
	 * デフォルトコンストラクタ
	 *
	 * @param server サーバインスタンス
	 * @param ctx    RPC実装クラスインスタンス
	 */
	public RpcHttpHandlerContext(RpcServer server, RpcTarget ctx) {
		// フィールド初期化
		this.ctx = ctx;
		// クラス情報取得
		Class<?> ctxClazz = ctx.getClass();
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
					handleMapper.put(rpcMethod.value(), handler);
					// 登録ログ出力
					KagerowLogger
							.newAppLogger().log(
									Level.INFO, String.format("[BindedBy]:%s [Path]:/rpc/%s [Method]:%s",
											ctxClazz.getSimpleName(), setting.value(), rpcMethod.value()),
									new Object[0]);
				} catch (IllegalAccessException e) {
					// 登録失敗ログ出力
					KagerowLogger.newAppLogger().err(e);
				}
			}
		}

	}

	/** {@inheritDoc} */
	@Override
	public void handle(HttpExchange exchange) throws IOException {

		// リクエストをXMLへ変換
		try {

			// DOMファクトリ生成
			InputStream input = exchange.getRequestBody();
			DocumentBuilderFactory factory = DocumentBuilderFactory.newDefaultInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document dom = builder.parse(input);

			// Xpath取得
			RpcHttpHandler handler = null;
			{
				XPathFactory xPathFactory = XPathFactory.newInstance();
				XPath xPath = xPathFactory.newXPath();
				XPathExpression expr = xPath.compile("/methodCall/methodName");
				Node node = (Node) expr.evaluate(dom, XPathConstants.NODE);
				handler = handleMapper.get(node.getTextContent());
			}

			// RPC呼び出し不可の場合
			if (Objects.isNull(handler)) {
				throw new IllegalAccessException("RPC target Not Found");
			}

			// XSDファクトリ生成
			SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			// スキーマインスタンス
			Schema xmlSchema = null;
			// XSD読み込み
			try (InputStream xsd = getClass().getResourceAsStream(handler.xsdFileName)) {
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

			// リクエストをマップへ変換（成功時）
			Map<String, String> rawData = readRequestXML(exchange, dom);
			// メイン処理呼びだし
			final Map<String, BaseDataType<?>> resultMap = handler.handle(exchange, rawData);
			// レスポンスXML生成
			byte[] response = createResponseXML(exchange, resultMap);
			// レスポンスコード設定
			exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
			// レスポンスボディ設定
			exchange.getResponseBody().write(response);

		} catch (Throwable e) {
			// ハンドラー起動
			RpcExceptionHandler.handleException(exchange, e);
		}

	}

	/**
	 * フィルタの固有設定を行います
	 *
	 * @param filters フィルターリスト
	 */
	public void setFilter(final List<Filter> filters) {
		for (RpcFilter rpcFilter : ctx.getClass().getDeclaredAnnotationsByType(RpcFilter.class)) {
			// フィルタ情報取得
			Class<? extends Filter> filterClazz = rpcFilter.filter();
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

	/**
	 * リクエストを解析しRPC呼び出しパラメータを生成します
	 *
	 * @param exchange リクエスト
	 * @param requestXML XML-RPCリクエスト
	 * @return RPC呼び出しパラメータ
	 * @throws Exception 解析失敗
	 */
	public Map<String, String> readRequestXML(HttpExchange exchange, Document requestXML) throws Exception {

		// リクエストをマップへ変換（成功時）
		Map<String, String> rawData = new HashMap<>();

		// Xpath取得
		XPathFactory xPathFactory = XPathFactory.newInstance();
		XPath xPath = xPathFactory.newXPath();
		XPathExpression expr = xPath.compile("/methodCall/params/param/value/struct");
		Node node = (Node) expr.evaluate(requestXML, XPathConstants.NODE);
		NodeList nodeList = node.getChildNodes();

		// メンバ取得向けのXpathを取得
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node member = nodeList.item(i);
			NodeList memberNodeList = member.getChildNodes();
			String name = null, value = null;
			for (int j = 0; j < memberNodeList.getLength(); j++) {
				Node memberNode = memberNodeList.item(j);
				// メンバーの解析
				if (memberNode instanceof Element element) {
					// パラメータ名称の解析
					String tagName = element.getTagName();
					if (Objects.equals("name", tagName)) {
						name = element.getTextContent();
					} else if (Objects.equals("value", tagName)) {
						// パラメータバリューの解析
						NodeList valueNode = element.getChildNodes();
						for (int k = 0; k < valueNode.getLength(); k++) {
							if (valueNode.item(k) instanceof Element valueElement) {
								String valueTagName = valueElement.getTagName();
								// NULL変換
								if (RpcDataTypes.NIL.toString().equals(valueTagName)) {
									value = null;
								} else {
									// 値を取得
									value = valueElement.getTextContent();
									if (value.isEmpty()) {
										// 空文字の場合、NULL変換
										value = null;
									}
								}
							}
						}
					}
				}
			}
			if (Objects.nonNull(name)) {
				rawData.put(name, value);
			}
		}
		return rawData;
	}

	/**
	 * 処理結果からレスポンス向けのXMLをボディーに設定します
	 *
	 * @param exchange  リクエスト
	 * @param resultMap 結果マップ
	 * @return レスポンスデータサイズ
	 * @throws Exception レスポンス失敗
	 */
	public byte[] createResponseXML(HttpExchange exchange, Map<String, BaseDataType<?>> resultMap) throws Exception {

		// レスポンスをXMLへ変換（成功時）
		OutputStream output = exchange.getResponseBody();

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

			// サイズ返却
			return tmpOutput.toByteArray();

		}

	}

}

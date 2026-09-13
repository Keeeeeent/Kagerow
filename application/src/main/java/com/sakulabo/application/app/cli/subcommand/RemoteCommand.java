package com.sakulabo.application.app.cli.subcommand;

import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.sakulabo.application.app.cli.subcommand.RemoteCommand.RpcResult.Fail;
import com.sakulabo.application.app.cli.subcommand.RemoteCommand.RpcResult.Success;
import com.sakulabo.application.app.rpc.datatype.BaseDataType;
import com.sakulabo.application.app.rpc.datatype.RpcDataTypes;
import com.sakulabo.application.app.rpc.filters.CertificationFilter;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;

/**
 * リモート実行機能規定クラスです
 *
 * @author keeeeeent
 */
public abstract class RemoteCommand implements Callable<Integer> {

	/** リモート実行 */
	protected URI remote = null;
	/** 証明書 */
	protected Path cacert = null;

	/**
	 * リクエスト結果
	 */
	protected sealed interface RpcResult {
		/**
		 * リクエスト成功
		 * @param response レスポンスデータ
		 */
		record Success(Map<String, String> response) implements RpcResult {
		}

		/**
		 * リクエスト失敗
		 * @param response レスポンスデータ
		 * @param statusCode レスポンスコード
		 */
		record Fail(Map<String, String> response, int statusCode) implements RpcResult {
		}
	}

	/** {@inheritDoc} */
	@Override
	public Integer call() throws Exception {
		try {
			if (Objects.isNull(remote)) {
				return local();
			} else {
				return remote();
			}
		} catch (ConnectException e) {
			System.err.println(e.getMessage());
			KagerowLogger.newAppLogger().err(e);
			return Integer.valueOf(1);
		} catch (Exception e) {
			KagerowLogger.newAppLogger().err(e);
			return Integer.valueOf(1);
		}
	}

	/**
	 * ローカル実行コマンド
	 * @return リターンコード
	 * @throws Exception 実行時例外
	 */
	protected abstract Integer local() throws Exception;

	/**
	 * リモート実行コマンド
	 * @return リターンコード
	 * @throws Exception 実行時例外
	 */
	protected abstract Integer remote() throws Exception;

	/**
	 * RPCメソッド呼び出しを行います
	 * @param methodName RPCメソッド明瞭
	 * @param rpcPath RPCパス
	 * @param createRequestBody リクエストXML生成関数
	 * @param isAuthentication 認証フラグ
	 * @return 呼び出し結果
	 * @throws Exception リクエスト失敗
	 */
	protected final RpcResult doRpcMethodCall(String methodName, String rpcPath,
			Supplier<Map<String, BaseDataType<?>>> createRequestBody,
			boolean isAuthentication)
			throws Exception {
		// クライアントビルダー生成
		HttpClient.Builder builder = HttpClient.newBuilder()
				.version(Version.HTTP_1_1)
				.followRedirects(Redirect.ALWAYS);
		// 証明書の指定が必要か判定
		if (Objects.nonNull(cacert)) {
			try (InputStream in = Files.newInputStream(cacert)) {
				// 証明書読み込み
				CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
				X509Certificate certificate = (X509Certificate) certificateFactory.generateCertificate(in);
				// キーストア生成
				KeyStore trustStore = KeyStore.getInstance("PKCS12");
				// キーストア初期化
				trustStore.load(null, null);
				// 証明書登録
				trustStore.setCertificateEntry("kagerow-server", certificate);
				// 証明書チェーン作成
				TrustManagerFactory tmf = TrustManagerFactory.getInstance("PKIX");
				// 証明書チェーン初期化
				tmf.init(trustStore);
				// TLSコンテキスト取得
				SSLContext sslContext = SSLContext.getInstance("TLS");
				// TLSコンテキスト初期化
				sslContext.init(null, tmf.getTrustManagers(), null);
				// クライアントのTLSハンドシェイクの設定を変更
				builder = builder.sslContext(sslContext);
			}
		}
		// クライアント生成
		try (HttpClient client = builder.build()) {
			// ベースリクエストXML生成
			DocumentBuilderFactory requestXMLFactory = DocumentBuilderFactory.newDefaultInstance();
			DocumentBuilder requestXMLBuilder = requestXMLFactory.newDocumentBuilder();
			InputStream baseRequest = ClassLoader.getSystemResourceAsStream("rpc-xml/common-request.xml");
			Document requestXML = requestXMLBuilder.parse(baseRequest);
			// リクエストパラメータ取得
			Map<String, BaseDataType<?>> requestParam = createRequestBody.get();
			// リクエストXML生成
			requestXML = createRequestXML(requestXML, requestParam, methodName);
			// リクエスト生成
			String body = null;
			try (StringWriter tmpOutput = new StringWriter()) {
				Source xmlSource = new DOMSource(requestXML);
				Result xmlResult = new StreamResult(tmpOutput);
				Transformer transformer = TransformerFactory.newDefaultInstance().newTransformer();
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				transformer.setOutputProperty(OutputKeys.ENCODING, StandardCharsets.UTF_8.toString());
				transformer.transform(xmlSource, xmlResult);
				tmpOutput.flush();
				body = tmpOutput.toString();
			}
			URI uri = getUri(rpcPath);
			HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
					.uri(uri)
					.POST(BodyPublishers.ofString(body))
					.setHeader("Content-Type", "text/xml");
			// トークン設定
			if (isAuthentication) {
				String token = getToken();
				requestBuilder = requestBuilder.setHeader(CertificationFilter.AUTH_KEY,
						String.format("Bearer %s", token));
			}
			HttpRequest request = requestBuilder.build();
			try {
				// リクエスト送信
				HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
				// レスポンス解析
				DocumentBuilderFactory responseXMLFactory = DocumentBuilderFactory.newDefaultInstance();
				DocumentBuilder responseXMLBuilder = responseXMLFactory.newDocumentBuilder();
				StringReader baseXML = new StringReader(response.body());
				InputSource responseInputSource = new InputSource(baseXML);
				Document responseXML = responseXMLBuilder.parse(responseInputSource);
				return convertRequestXML(responseXML, response.statusCode());
			} catch (ConnectException e) {
				throw new ConnectException(
						String.format("An attempt to connect to the RPC server failed. Hostname: %s Port number: %d",
								uri.getHost(), uri.getPort()));
			}
		}
	}

	/**
	 * トークンの取得を行います
	 * @return トークン
	 */
	private String getToken() {
		String params = remote.getQuery();
		if (Objects.isNull(params)) {
			throw new IllegalStateException("No parameters were specified");
		}
		for (String param : params.split("&", -1)) {
			String[] target = param.split("=", -1);
			if ("token".equals(target[0])) {
				return target[1];
			}
		}
		throw new IllegalStateException("No token has been specified");
	}

	/**
	 * RPCエンドポイント接続URIを生成します
	 * @param rpcPath 呼び出しメソッドパス
	 * @return 生成されたURI
	 * @throws URISyntaxException URI変換失敗
	 */
	private URI getUri(String rpcPath) throws URISyntaxException {
		String host = remote.getHost();
		int port = remote.getPort();
		return new URI("https", null, host, port, rpcPath, null, null);
	}

	/**
	 * リクエストを解析します
	 * @param responseXML 解析対象
	 * @param responseCode ステータスコード
	 * @return 解析結果
	 * @throws Exception 解析失敗
	 */
	private RpcResult convertRequestXML(Document responseXML, int responseCode) throws Exception {
		// 返却用変数初期化
		Map<String, String> result = new HashMap<>();
		if (responseCode == HttpURLConnection.HTTP_OK) {
			// リクエスト成功の場合
			XPathFactory xPathFactory = XPathFactory.newInstance();
			XPath xPath = xPathFactory.newXPath();
			XPathExpression expr = xPath.compile("/methodResponse/params/param/struct/member");
			NodeList node = (NodeList) expr.evaluate(responseXML, XPathConstants.NODESET);
			for (int i = 0; i < node.getLength(); i++) {
				Element member = (Element) node.item(i);
				String name = xPath.evaluate("name", member);
				Node value = (Node) xPath.evaluate("value", member, XPathConstants.NODE);
				NodeList valueList = value.getChildNodes();
				for (int el = 0; el < valueList.getLength(); el++) {
					Node targetElement = valueList.item(el);
					if (targetElement.getNodeType() == Node.ELEMENT_NODE) {
						if (RpcDataTypes.ARRAY.toString().equals(((Element) targetElement).getTagName())) {
							// 配列変換(カンマ区切りのリスト形式にする)
							NodeList arrayNode = (NodeList) xPath.evaluate("data/value/string", targetElement,
									XPathConstants.NODESET);
							StringJoiner joiner = new StringJoiner(",");
							for (int l = 0; l < arrayNode.getLength(); l++) {
								String arrayItemValue = arrayNode.item(l).getTextContent();
								arrayItemValue = URLEncoder.encode(arrayItemValue, StandardCharsets.UTF_8);
								if (!arrayItemValue.isEmpty()) {
									joiner.add(arrayItemValue);
								}
							}
							result.put(name, joiner.toString());
						} else {
							NodeList items = targetElement.getChildNodes();
							for (int l = 0; l < items.getLength(); l++) {
								Node item = items.item(l);
								String val = item.getTextContent();
								if (!val.isEmpty()) {
									result.put(name, val);
									break;
								}
							}
						}
					}
				}
			}
			return new Success(result);
		} else

		{
			// リクエスト失敗の場合
			XPathFactory xPathFactory = XPathFactory.newInstance();
			XPath xPath = xPathFactory.newXPath();
			XPathExpression expr = xPath.compile("/methodResponse/fault/value/struct/member");
			NodeList node = (NodeList) expr.evaluate(responseXML, XPathConstants.NODESET);
			for (int i = 0; i < node.getLength(); i++) {
				Element member = (Element) node.item(i);
				String name = xPath.evaluate("name", member);
				String value = xPath.evaluate("value", member);
				result.put(name, Objects.toString(value, "").strip());
			}
			return new Fail(result, responseCode);
		}
	}

	/**
	 * リクエストを構築します
	 * @param dom XMLリクエスト
	 * @param requestParam リクエストパラメータ
	 * @param methodName メソッドパス
	 * @return 構築されたXML
	 * @throws Exception XML構築失敗
	 */
	private Document createRequestXML(Document dom, Map<String, BaseDataType<?>> requestParam, String methodName)
			throws Exception {
		// 呼び出しメソッド設定
		Element methodCall = dom.getDocumentElement();
		Element methodNameNode = dom.createElement("methodName");
		methodNameNode.setTextContent(methodName);
		methodCall.insertBefore(methodNameNode, methodCall.getFirstChild());
		// Xpath取得
		XPathFactory xPathFactory = XPathFactory.newInstance();
		XPath xPath = xPathFactory.newXPath();
		XPathExpression expr = xPath.compile("/methodCall/params/param/value/struct");
		Node node = (Node) expr.evaluate(dom, XPathConstants.NODE);
		// 返却パラメータ追加
		for (Entry<String, BaseDataType<?>> structParam : requestParam.entrySet()) {
			// メンバー追加
			Element memberElem = dom.createElement("member");
			// パラメータ名称追加
			Element nameElem = dom.createElement("name");
			nameElem.setTextContent(structParam.getKey());
			// サブメンバー追加
			memberElem.appendChild(nameElem);
			// パラメータバリュー追加
			BaseDataType<?> dataType = structParam.getValue();
			Element valueElem = dom.createElement("value");
			RpcDataTypes dataTypes = dataType.toRpcDataType();
			Element valueSubElem = dom.createElement(dataTypes.toString());
			// 取得データがnilか判定
			String dat = dataType.getData();
			if (Objects.nonNull(dat) && !dat.isEmpty()) {
				if (RpcDataTypes.ARRAY == dataTypes) {
					// 配列の場合
					Element arrayDataElem = dom.createElement("data");
					@SuppressWarnings("unchecked")
					Optional<List<String>> line = (Optional<List<String>>) dataType.getRawType();
					if (line.isPresent()) {
						for (String val : line.get()) {
							Element arrayDataValueElem = dom.createElement("value");
							Element arrayRawDataElem = dom.createElement(RpcDataTypes.STRING.toString());
							arrayRawDataElem.setTextContent(val);
							arrayDataValueElem.appendChild(arrayRawDataElem);
							arrayDataElem.appendChild(arrayDataValueElem);
						}
					}
					valueSubElem.appendChild(arrayDataElem);
				} else {
					// 単一の場合
					valueSubElem.setTextContent(dat);
				}
			} else {
				Element nilElem = dom.createElement(RpcDataTypes.NIL.toString());
				valueSubElem.appendChild(nilElem);
			}
			// 生成要素追加
			valueElem.appendChild(valueSubElem);
			// サブメンバー追加
			memberElem.appendChild(valueElem);
			// 要素を構造体として追加
			node.appendChild(memberElem);
		}
		return dom;
	}
}

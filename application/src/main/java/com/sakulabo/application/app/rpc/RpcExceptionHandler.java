package com.sakulabo.application.app.rpc;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

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
import org.xml.sax.InputSource;

import com.sakulabo.application.app.rpc.datatype.BaseDataType;
import com.sakulabo.application.app.rpc.datatype.RpcDataTypes;
import com.sakulabo.application.app.rpc.exception.IllegalCertificationException;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;
import com.sun.net.httpserver.HttpExchange;

/**
 * RPCリクエスト処理の内部で発生した例外を処理する例外ハンドラー実装です
 *
 * @author keeeeeent
 */
public final class RpcExceptionHandler {

	/** 共通FaultレスポンンスXMLファイルパス */
	private static final String COMMON_FAULT_XML = "rpc-xml/common-fault.xml";
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
	/** 例外ハンドラーインスタンス */
	private static final RpcExceptionHandler exceptionHandler = new RpcExceptionHandler();
	/** 例外ハンドラマッピング */
	private static final Map<Class<? extends Throwable>, BiConsumer<Throwable, HttpExchange>> exceptionMapper = new ConcurrentHashMap<>();

	static {
		// 例外ハンドラー生成
		for (Method exceptionHandler : RpcExceptionHandler.class.getDeclaredMethods()) {
			if (exceptionHandler.isAnnotationPresent(RpcException.class)) {
				// 例外ハンドラー定義メソッドの場合
				RpcException rpcException = exceptionHandler.getDeclaredAnnotation(RpcException.class);
				// アクセッサ生成
				MethodHandles.Lookup lookup = MethodHandles.lookup();
				try {
					// 呼び出しメソッドタイプ生成
					MethodType methodType = MethodType.methodType(void.class, rpcException.value(), HttpExchange.class);
					// 呼び出しハンドラ生成
					CallSite callSite = LambdaMetafactory.metafactory(lookup, "accept",
							MethodType.methodType(BiConsumer.class, RpcExceptionHandler.class), methodType.erase(),
							lookup.unreflect(exceptionHandler), methodType);
					// ラムダインスタンス生成
					BiConsumer<Throwable, HttpExchange> lamda = (BiConsumer<Throwable, HttpExchange>) callSite
							.getTarget().invoke(RpcExceptionHandler.exceptionHandler);
					// 例外ハンドラー登録
					exceptionMapper.put(rpcException.value(), lamda);
				} catch (Throwable e) {
					// 登録失敗ログ出力
					KagerowLogger.newAppLogger().err(e);
				}
			}
		}
	}

	/**
	 * ハンドラーを取得します
	 *
	 * @param target 処理対象例外
	 * @return ハンドラー
	 */
	public static BiConsumer<Throwable, HttpExchange> getHandler(Class<? extends Throwable> target) {
		return exceptionMapper.get(target);
	}

	/**
	 * ハンドラ呼び出しにて例外が発生した場合のレスポンスXMLを生成します
	 *
	 * @param exchange リクエスト
	 * @param e        スローされた例外
	 * @return レスポンスデータサイズ
	 */
	public static long createFalutResponseXML(HttpExchange exchange, Throwable e) {
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

			// レスポンスをXMLへ変換（成功時）
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

					// サイズ設定
					size = tmpOutput.size();
				}

			}
		} catch (Exception exp) {
			// ロガー書き出し
			KagerowLogger.newAppLogger().err(exp);
		}
		return size;
	}

	/**
	 * 認証失敗の場合
	 *
	 * @param e        スローされた例外
	 * @param exchange リクエスト
	 * @throws IOException レスポンス書き込み失敗
	 */
	@RpcException(IllegalCertificationException.class)
	public void commonHandler(IllegalCertificationException e, HttpExchange exchange) throws IOException {
		// レスポンスをXMLへ変換（失敗時）
		long size = RpcExceptionHandler.createFalutResponseXML(exchange, e);
		// レスポンスコード設定
		exchange.sendResponseHeaders(HttpURLConnection.HTTP_UNAUTHORIZED, size);
	}

}

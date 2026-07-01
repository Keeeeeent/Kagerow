package com.sakulabo.core.Processor.script;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.sakulabo.core.Common.ErrorMessage;
import com.sakulabo.core.Common.StringUtils;
import com.sakulabo.core.Kagerow.Exception.AppLogicException;
import com.sakulabo.core.Kagerow.Exception.KFileParseException;
import com.sakulabo.core.Kagerow.Exception.KSQLParseException;
import com.sakulabo.core.Kagerow.Utilities.KagerowCommandMode;
import com.sakulabo.core.Kagerow.Utilities.KagerowDBMode;
import com.sakulabo.core.Kagerow.Utilities.KagerowFileVersion;

/**
 * Kagerowスクリプトファイル共通で使用される解析基底クラスです
 * 
 * @author keeeeeent
 */
public sealed abstract class KsqlFileReader extends FileReader permits BasicKsqlFileReader {

	/** 環境変数パーサー */
	protected KsqlReplaceEnvParser envParser = new KsqlReplaceEnvParser(env);
	/** 読み取りストラテジー */
	protected final KsqlReaderStrategy strategy;

	// ###########################################################################
	// # メイン実装
	// ###########################################################################

	/**
	 * デフォルトコンストラクタ 
	 * @param path 解析対象
	 * @param strategy 読み取りストラテジー
	 * @throws KFileParseException KFile解析エラー
	 * @throws KSQLParseException KSQL解析エラー
	 * @throws AppLogicException アプリケーションロジック不正
	 */
	public KsqlFileReader(Path path, KsqlReaderStrategy strategy)
			throws KFileParseException, KSQLParseException, AppLogicException {

		// スーパークラス初期化
		super(path);

		// フィールド初期化
		this.strategy = strategy;

		/**
		 * XML解析
		 */
		try {
			// DocumentFactory生成
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			// namespace対応
			factory.setNamespaceAware(true);
			// secure processing
			factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			// DOCTYPE禁止
			factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			// 外部Entity禁止
			factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
			// 外部パラメータEntity禁止
			factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
			// 外部DTD禁止
			factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			// XInclude禁止
			factory.setXIncludeAware(false);
			// Entity展開抑止
			factory.setExpandEntityReferences(false);
			// ビルダー生成
			DocumentBuilder builder = factory.newDocumentBuilder();
			// DOM取得
			try (InputStream input = Files.newInputStream(path)) {

				// DOM生成
				Document document = builder.parse(input);

				/**
				 * XMLバリデーション
				 */
				// SchemaFactory生成
				SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
				// スキーマインスタンス
				Schema xmlSchema = null;
				// XSD取得
				Element root = document.getDocumentElement();
				final String rawXsdFileName = root.getAttributeNS(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI,
						"noNamespaceSchemaLocation");
				// xsd正規化実施
				String xsdFileName = KagerowFileVersion.fromString(rawXsdFileName).toString();
				// クラスパスへ変換
				xsdFileName = XSD_DIR_NAME + xsdFileName;
				// XSD読み込み
				try (InputStream xsd = getClass().getResourceAsStream(xsdFileName)) {
					// XSDストリーム生成
					StreamSource source = new StreamSource(xsd);
					// セキュアプロセッシング有効化
					schemaFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
					// 外部DTDアクセス禁止
					schemaFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, StringUtils.EMPTY);
					// 外部Schemaアクセス禁止
					schemaFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, StringUtils.EMPTY);
					// 独自Resolver設定
					schemaFactory.setResourceResolver(new KFileXSDClasspathResourceResolver());
					try {
						// スキーマ生成
						xmlSchema = schemaFactory.newSchema(source);
						// Validatorの生成
						Validator validator = xmlSchema.newValidator();
						validator.validate(new StreamSource(path.toFile()));
					} catch (IOException | SAXException e) {
						if (e instanceof SAXParseException exp) {
							// 必須要素不足
							throw toKFileParseException(exp);
						}
						// XMLファイル不正
						throw new KFileParseException(ErrorMessage.CODE_024.getMessage(), e);
					}
				} catch (IOException | SAXException e) {
					// XSDファイル読み込み失敗
					throw new AppLogicException(ErrorMessage.CODE_023.getMessage(), e);
				}

				/**
				 * XML解析
				 */
				// Xpath取得
				XPathFactory xPathFactory = XPathFactory.newInstance();
				XPath xPath = xPathFactory.newXPath();
				// 各種フィールド初期化
				/** 環境変数初期化 */
				// 内部的に環境変数の適用を安全に行うため
				// このメソッドは他のどのXML解析メソッドよりも早く呼ばれる必要があります。
				env = setEnv(document, xPath);
				/** スクリプト名称保持フィールド */
				name = setName(document, xPath, true);
				rawname = setName(document, xPath, false);
				/** スクリプトバージョン保持フィールド */
				version = KagerowFileVersion.fromString(rawXsdFileName);
				rawversion = version.toString();
				/** スクリプト概要保持フィールド */
				summary = setSummary(document, xPath, true);
				rawsummary = setSummary(document, xPath, false);
				/** スクリプト実行モード保持フィールド */
				mode = KagerowDBMode.toMode(setMode(document, xPath, true));
				rawmode = setMode(document, xPath, false);
				/** スクリプトカレントスキーマ保持フィールド */
				schema = setSchema(document, xPath, true);
				rawschema = setSchema(document, xPath, false);
				/** スクリプトキャッシュID保持フィールド */
				cacheId = setCacheId(document, xPath, true);
				rawcacheId = setCacheId(document, xPath, false);
				/** スクリプト入力プラグインリスト保持フィールド */
				inputPlugins = setInputPlugins(document, xPath, true);
				rawinputPlugins = setInputPlugins(document, xPath, false);
				/** スクリプト出力プラグインリスト保持フィールド */
				outputPlugins = setOutputPlugins(document, xPath, true);
				rawoutputPlugins = setOutputPlugins(document, xPath, false);
				/** スクリプトKSQL一覧リスト保持フィールド */
				ksqls = setKsqls(document, xPath, true);
				rawksqls = setKsqls(document, xPath, false);
				/** スクリプト実行コマンド一覧リスト保持フィールド */
				command = setCommand(document, xPath, true);
				rawcommand = setCommand(document, xPath, false);

			} catch (IOException | SAXException e) {
				// インプットファイル不正
				throw new KFileParseException(ErrorMessage.CODE_024.getMessage(), e);
			}
		} catch (ParserConfigurationException e) {
			// DOM生成失敗
			throw new KFileParseException(e);
		}

	}

	/**
	 * 対象を環境変数込みに表現に変換する処理を提供します
	 * @param target 処理対象
	 * @return 処理結果
	 * @throws KSQLParseException 変換失敗
	 */
	protected abstract String parseEnv(String target) throws KSQLParseException;

	/**
	 * 対象を環境変数込みに表現に変換する処理を提供します<br/>
	 * このメソッドの処理は対象の文字列が長くなる可能性が高い処理に使用されます
	 * @param target 処理対象
	 * @return 処理結果
	 * @throws KSQLParseException 変換失敗
	 */
	protected abstract String longParseEnv(String target) throws KSQLParseException;

	/**
	 * スクリプト環境変数保持フィールドを設定します
	 * @param document 解析対象
	 * @param xPath xPath検索インスタンス
	 * @return 処理結果
	 * @throws KFileParseException 解析失敗
	 */
	protected Map<String, String> setEnv(Document document, XPath xPath) throws KFileParseException {

		// 解析結果格納変数宣言
		XPathExpression expr;
		Node node;
		try {
			expr = xPath.compile(ENV_ROOT_PATH);
			node = (Node) expr.evaluate(document, XPathConstants.NODE);
		} catch (XPathExpressionException e) {
			// XPath不正
			throw new KFileParseException(e);
		}

		// 環境変数格納レジスタ
		Map<String, String> result = new ConcurrentHashMap<>();

		// 解析の結果対象のタグが存在しない場合処理を終了
		if (Objects.isNull(node)) {
			return result;
		}

		// 要素がある場合、子要素を走査
		NodeList nodeList = node.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			// コメントは除外する
			if (nodeList.item(i) instanceof Element element) {
				// 本来ありえないが、対象のタグ以外に反応しないようタグ名称を比較
				if (element.getTagName().equals(ENV)) {
					String envName = element.getAttribute(ENV_NAME);
					String envValue = element.getAttribute(ENV_VALUE);
					result.put(envName, envValue);
				}
			}
		}
		// 環境変数初期化
		envParser = new KsqlReplaceEnvParser(result);
		return result;
	}

	/**
	 * スクリプト名称保持フィールドを設定します
	 * @param document 解析対象
	 * @param xPath xPath検索インスタンス
	 * @param isTransform 変換有無
	 * @return スクリプト名称
	 * @throws KSQLParseException パース処理失敗
	 * @throws KFileParseException 解析失敗
	 */
	protected String setName(Document document, XPath xPath, boolean isTransform)
			throws KSQLParseException, KFileParseException {

		// 解析結果格納変数宣言
		XPathExpression expr;
		Node node;
		try {
			expr = xPath.compile(CONFIGURATION_NAME);
			node = (Node) expr.evaluate(document, XPathConstants.NODE);
		} catch (XPathExpressionException e) {
			// XPath不正
			throw new KFileParseException(e);
		}

		String result = node.getTextContent().strip();
		result = isTransform ? parseEnv(result) : result;
		return result;
	}

	/**
	 * スクリプト概要保持フィールドを設定します
	 * @param document 解析対象
	 * @param xPath xPath検索インスタンス
	 * @param isTransform 変換有無
	 * @return スクリプト概要
	 * @throws KSQLParseException パース処理失敗
	 * @throws KFileParseException 解析失敗
	 */
	protected String setSummary(Document document, XPath xPath, boolean isTransform)
			throws KSQLParseException, KFileParseException {

		// 解析結果格納変数宣言
		XPathExpression expr;
		Node node;
		try {
			expr = xPath.compile(CONFIGURATION_SUMMARY);
			node = (Node) expr.evaluate(document, XPathConstants.NODE);
		} catch (XPathExpressionException e) {
			// XPath不正
			throw new KFileParseException(e);
		}

		String result = node.getTextContent().strip();
		result = isTransform ? parseEnv(result) : result;
		return result;
	}

	/**
	 * キャッシュID保持フィールドを設定します
	 * @param document 解析対象
	 * @param xPath xPath検索インスタンス
	 * @param isTransform 変換有無
	 * @return スクリプト概要
	 * @throws KSQLParseException パース処理失敗
	 * @throws KFileParseException 解析失敗
	 */
	protected String setCacheId(Document document, XPath xPath, boolean isTransform)
			throws KSQLParseException, KFileParseException {

		// 解析結果格納変数宣言
		XPathExpression expr;
		Node node;
		try {
			expr = xPath.compile(CONFIGURATION_CACHE);
			node = (Node) expr.evaluate(document, XPathConstants.NODE);
		} catch (XPathExpressionException e) {
			// XPath不正
			throw new KFileParseException(e);
		}

		if (Objects.isNull(node)) {
			return StringUtils.DEFAULT;
		} else {
			String tmpStr = node.getTextContent().strip();
			String result = tmpStr.isEmpty() ? StringUtils.DEFAULT : tmpStr;
			result = isTransform ? parseEnv(result) : result;
			return result;
		}

	}

	/**
	 * スクリプト実行モード保持フィールドを設定します
	 * @param document 解析対象
	 * @param xPath xPath検索インスタンス
	 * @param isTransform 変換有無
	 * @return スクリプト実行モード
	 * @throws KSQLParseException パース処理失敗
	 * @throws KFileParseException 解析失敗
	 */
	protected String setMode(Document document, XPath xPath, boolean isTransform)
			throws KSQLParseException, KFileParseException {

		// 解析結果格納変数宣言
		XPathExpression expr;
		Node node;
		try {
			expr = xPath.compile(CONFIGURATION_MODE);
			node = (Node) expr.evaluate(document, XPathConstants.NODE);
		} catch (XPathExpressionException e) {
			// XPath不正
			throw new KFileParseException(e);
		}

		String mode = node.getTextContent().strip();
		mode = isTransform ? parseEnv(mode) : mode;
		return mode;
	}

	/**
	 * スクリプトカレントスキーマ保持フィールドを設定します
	 * @param document 解析対象
	 * @param xPath xPath検索インスタンス
	 * @param isTransform 変換有無
	 * @return スクリプトカレントスキーマ
	 * @throws KSQLParseException パース処理失敗
	 * @throws KFileParseException 解析失敗
	 */
	protected String setSchema(Document document, XPath xPath, boolean isTransform)
			throws KSQLParseException, KFileParseException {

		// 解析結果格納変数宣言
		XPathExpression expr;
		Node node;
		try {
			expr = xPath.compile(CONFIGURATION_SCHEMA);
			node = (Node) expr.evaluate(document, XPathConstants.NODE);
		} catch (XPathExpressionException e) {
			// XPath不正
			throw new KFileParseException(e);
		}

		String result = node.getTextContent().strip();
		result = isTransform ? parseEnv(result) : result;
		return result;
	}

	/**
	 * スクリプト入力プラグインリスト保持フィールドを設定します
	 * @param document 解析対象
	 * @param xPath xPath検索インスタンス
	 * @param isTransform 変換有無
	 * @return スクリプト入力プラグインリスト
	 * @throws KSQLParseException パース処理失敗
	 * @throws KFileParseException 解析失敗
	 */
	protected List<KagerowPluginAccessor> setInputPlugins(Document document, XPath xPath, boolean isTransform)
			throws KSQLParseException, KFileParseException {

		// 解析結果格納変数宣言
		XPathExpression expr;
		Node node;
		try {
			expr = xPath.compile(INPUT_PLUGINS);
			node = (Node) expr.evaluate(document, XPathConstants.NODE);
		} catch (XPathExpressionException e) {
			// XPath不正
			throw new KFileParseException(e);
		}

		// プラグインアクセッサ格納レジスタ
		List<KagerowPluginAccessor> result = new Vector<>();

		// 解析の結果対象のタグが存在しない場合処理を終了
		if (Objects.isNull(node)) {
			return result;
		}

		// 要素がある場合、子要素を走査
		NodeList nodeList = node.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			// コメントは除外する
			if (nodeList.item(i) instanceof Element element) {
				// 本来ありえないが、対象のタグ以外に反応しないようタグ名称を比較
				if (element.getTagName().equals(PLUGIN)) {
					// データ構造初期化
					KagerowPluginAccessorImpl accessor = new KagerowPluginAccessorImpl();
					// データ取得・格納（プラグインパッケージ名称・プラグイン名称・プラグインID・後続プラグイン）
					accessor.pkg = element.getAttribute(PLUGIN_PKG_NM);
					accessor.name = element.getAttribute(PLUGIN_NM);
					accessor.id = element.getAttribute(PLUGIN_ID);
					accessor.next = element.getAttribute(PLUGIN_NID);
					// 環境変数パース
					if (isTransform) {
						accessor.pkg = parseEnv(accessor.pkg);
						accessor.name = parseEnv(accessor.name);
						accessor.id = parseEnv(accessor.id);
						accessor.next = parseEnv(accessor.next);
					}
					// パッケージ名称の変換
					if (Objects.isNull(accessor.pkg) || accessor.pkg.isEmpty()) {
						accessor.pkg = StringUtils.DEFAULT;
					}
					// プラグインパラメータ取得
					NodeList paramList = element.getChildNodes();
					// コメントは除外する
					for (int j = 0; j < paramList.getLength(); j++) {
						if (paramList.item(j) instanceof Element param) {
							// 本来ありえないが、対象のタグ以外に反応しないようタグ名称を比較
							if (param.getTagName().equals(PLUGIN_PARAM)) {
								// データ取得・格納（パラメータ名称・パラメータ引数）
								String paramName = param.getAttribute(PLUGIN_PARAM_NM);
								String paramValue = param.getTextContent().strip();
								// 環境変数パース
								if (isTransform) {
									paramName = parseEnv(paramName);
									paramValue = longParseEnv(paramValue);
								}
								// 値セット
								accessor.param.put(paramName, paramValue);
							}
						}
					}
					// データ構造追加、そのまま次の要素の処理に入る
					result.add(accessor);
				}
			}
		}
		// 解析結果を返却
		return result;
	}

	/**
	 * スクリプト出力プラグインリスト保持フィールドを設定します
	 * @param document 解析対象
	 * @param xPath xPath検索インスタンス
	 * @param isTransform 変換有無
	 * @return スクリプト出力プラグインリスト
	 * @throws KSQLParseException パース処理失敗
	 * @throws KFileParseException 解析失敗
	 */
	protected List<KagerowPluginAccessor> setOutputPlugins(Document document, XPath xPath, boolean isTransform)
			throws KSQLParseException, KFileParseException {

		// 解析結果格納変数宣言
		XPathExpression expr;
		Node node;
		try {
			expr = xPath.compile(OUTPUT_PLUGINS);
			node = (Node) expr.evaluate(document, XPathConstants.NODE);
		} catch (XPathExpressionException e) {
			// XPath不正
			throw new KFileParseException(e);
		}

		// プラグインアクセッサ格納レジスタ
		List<KagerowPluginAccessor> result = new Vector<>();

		// 解析の結果対象のタグが存在しない場合処理を終了
		if (Objects.isNull(node)) {
			return result;
		}

		// 要素がある場合、子要素を走査
		NodeList nodeList = node.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			// コメントは除外する
			if (nodeList.item(i) instanceof Element element) {
				// 本来ありえないが、対象のタグ以外に反応しないようタグ名称を比較
				if (element.getTagName().equals(PLUGIN)) {
					// データ構造初期化
					KagerowPluginAccessorImpl accessor = new KagerowPluginAccessorImpl();
					// データ取得・格納（プラグイン名称・プラグインID・後続プラグイン）
					accessor.pkg = element.getAttribute(PLUGIN_PKG_NM);
					accessor.name = element.getAttribute(PLUGIN_NM);
					accessor.id = element.getAttribute(PLUGIN_ID);
					accessor.next = element.getAttribute(PLUGIN_NID);
					// 環境変数パース
					if (isTransform) {
						accessor.pkg = parseEnv(accessor.pkg);
						accessor.name = parseEnv(accessor.name);
						accessor.id = parseEnv(accessor.id);
						accessor.next = parseEnv(accessor.next);
					}
					// パッケージ名称の変換
					if (Objects.isNull(accessor.pkg) || accessor.pkg.isEmpty()) {
						accessor.pkg = StringUtils.DEFAULT;
					}
					// プラグインパラメータ取得
					NodeList paramList = element.getChildNodes();
					for (int j = 0; j < paramList.getLength(); j++) {
						// コメントは除外する
						if (paramList.item(j) instanceof Element param) {
							// 本来ありえないが、対象のタグ以外に反応しないようタグ名称を比較
							if (param.getTagName().equals(PLUGIN_PARAM)) {
								String paramName = param.getAttribute(PLUGIN_PARAM_NM);
								String paramValue = param.getTextContent().strip();
								// 環境変数パース
								if (isTransform) {
									paramName = parseEnv(paramName);
									paramValue = longParseEnv(paramValue);
								}
								// 値セット
								accessor.param.put(paramName, paramValue);
							}
						}
					}
					// データ構造追加、そのまま次の要素の処理に入る
					result.add(accessor);
				}
			}
		}

		return result;
	}

	/**
	 * スクリプトKSQL一覧リスト保持フィールドを設定します
	 * @param document 解析対象
	 * @param xPath xPath検索インスタンス
	 * @param isTransform 変換有無
	 * @return スクリプトKSQL一覧リスト
	 * @throws KSQLParseException パース処理失敗
	 * @throws KFileParseException 解析失敗
	 */
	protected List<KagerowSqlAccessor> setKsqls(Document document, XPath xPath, boolean isTransform)
			throws KSQLParseException, KFileParseException {

		// 解析結果格納変数宣言
		XPathExpression expr;
		Node node;
		try {
			expr = xPath.compile(KSQLS);
			node = (Node) expr.evaluate(document, XPathConstants.NODE);
		} catch (XPathExpressionException e) {
			// XPath不正
			throw new KFileParseException(e);
		}

		// SQLアクセッサ格納レジスタ
		List<KagerowSqlAccessor> result = new Vector<>();

		// ksqlタグは必須タグのため、そのまま処理を続行する
		NodeList nodeList = node.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			// コメントは除外する
			if (nodeList.item(i) instanceof Element element) {
				// 本来ありえないが、対象のタグ以外に反応しないようタグ名称を比較
				if (element.getTagName().equals(KSQL)) {
					// データ構造初期化
					KagerowSqlAccessorImpl accessor = new KagerowSqlAccessorImpl();
					// データ取得・格納（KSQL名称・KSQLID・後続KSQL）
					accessor.name = element.getAttribute(KSQL_NM);
					accessor.id = element.getAttribute(KSQL_ID);
					accessor.next = element.getAttribute(KSQL_NID);
					// 環境変数パース
					if (isTransform) {
						accessor.name = parseEnv(accessor.name);
						accessor.id = parseEnv(accessor.id);
						accessor.next = parseEnv(accessor.next);
					}
					// KSQL宣言取得
					NodeList paramList = element.getChildNodes();
					for (int j = 0; j < paramList.getLength(); j++) {
						if (paramList.item(j) instanceof Element param) {
							// 対象のタグ以外に反応しないようタグ名称を比較（変数宣言）
							if (param.getTagName().equals(KSQL_VAR_DEC)) {
								// 変数宣言一覧取得
								NodeList variableList = param.getChildNodes();
								for (int k = 0; k < variableList.getLength(); k++) {
									if (variableList.item(k) instanceof Element variable) {
										String paramName = variable.getAttribute(KSQL_VAR_NM);
										String paramValue = variable.getAttribute(KSQL_VAR_VAL);
										// 環境変数パース
										if (isTransform) {
											paramName = parseEnv(paramName);
											paramValue = longParseEnv(paramValue);
										}
										// 値セット
										accessor.variable.put(paramName, paramValue);
									}
								}
							}
							// 対象のタグ以外に反応しないようタグ名称を比較（SQL宣言）
							if (param.getTagName().equals(KSQL_SQL)) {
								accessor.sql = param.getTextContent().strip();
								// 環境変数パース
								if (isTransform) {
									accessor.sql = longParseEnv(accessor.sql);
								}
							}
						}
					}
					// データ構造追加、そのまま次の要素の処理に入る
					result.add(accessor);
				}
			}
		}

		return result;
	}

	/**
	 * スクリプト実行コマンド一覧リスト保持フィールドを設定します
	 * @param document 解析対象
	 * @param xPath xPath検索インスタンス
	 * @param isTransform 変換有無
	 * @return スクリプト実行コマンド一覧リスト
	 * @throws KSQLParseException パース処理失敗
	 * @throws KFileParseException 解析失敗
	 */
	protected List<KagerowCmdAccessor> setCommand(Document document, XPath xPath, boolean isTransform)
			throws KSQLParseException, KFileParseException {

		// 解析結果格納変数宣言
		XPathExpression expr;
		Node node;
		try {
			expr = xPath.compile(CMDS);
			node = (Node) expr.evaluate(document, XPathConstants.NODE);
		} catch (XPathExpressionException e) {
			// XPath不正
			throw new KFileParseException(e);
		}

		// CMDアクセッサ格納レジスタ
		List<KagerowCmdAccessor> result = new Vector<>();

		// 解析の結果対象のタグが存在しない場合処理を終了
		if (Objects.isNull(node)) {
			return result;
		}

		// 要素がある場合、子要素を走査
		NodeList nodeList = node.getChildNodes();
		// 初期化フラグ
		boolean initFlug = false;
		// データ構造初期化
		KagerowCmdAccessorImpl accessor = new KagerowCmdAccessorImpl();
		for (int i = 0; i < nodeList.getLength(); i++) {
			// コメントは除外する
			if (nodeList.item(i) instanceof Element element) {
				// 対象のタグ以外に反応しないようタグ名称を比較（環境変数）
				if (element.getTagName().equals(COMMAND_ENV)) {
					// 環境変数一覧取得
					NodeList envList = element.getChildNodes();
					for (int j = 0; j < nodeList.getLength(); j++) {
						if (envList.item(j) instanceof Element env) {
							// 本来ありえないが、対象のタグ以外に反応しないようタグ名称を比較
							if (env.getTagName().equals(COMMAND_ENV_VAR)) {
								String envName = env.getAttribute(COMMAND_ENV_VAR_NM);
								String envValue = env.getAttribute(COMMAND_ENV_VAR_VAL);
								// 環境変数パース
								if (isTransform) {
									envName = parseEnv(envName);
									envValue = longParseEnv(envValue);
								}
								// 値セット
								accessor.environmental.put(envName, envValue);
							}
						}
					}
				}
				// 対象のタグ以外に反応しないようタグ名称を比較（コマンド本体）
				if (element.getTagName().equals(COMMAND_CMD)) {
					accessor.mode = KagerowCommandMode.toMode(element.getAttribute(COMMAND_MODE));
					accessor.cmd = element.getTextContent().strip();
					// 環境変数パース
					if (isTransform) {
						accessor.cmd = longParseEnv(accessor.cmd);
					}
					initFlug = true;
				}
			}
		}
		// コマンドの取得があった場合はデータ追加
		if (initFlug) {
			result.add(accessor);
		}
		return result;
	}

}

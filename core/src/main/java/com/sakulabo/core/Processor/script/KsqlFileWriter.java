package com.sakulabo.core.Processor.script;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.sakulabo.core.Common.ErrorMessage;
import com.sakulabo.core.Common.StringUtils;
import com.sakulabo.core.Kagerow.Exception.AppLogicException;
import com.sakulabo.core.Kagerow.Exception.KFileParseException;
import com.sakulabo.core.Kagerow.Utilities.KagerowScriptAccessor;

/**
 * Kagerowスクリプトファイル生成規定クラスです
 * @author keeeeeent
 */
public abstract sealed class KsqlFileWriter extends FileWriter permits BasicKsqlFileWriter {

	/** スクリプト実行モード保持フィールド */
	private String mode;
	/** 書き込みストラテジー */
	protected final KsqlWriterStrategy strategy;

	// ###########################################################################
	// # メイン実装
	// ###########################################################################

	/**
	 * デフォルトコンストラクタ
	 * @param scriptAccessor スクリプトインスタンス
	 * @param path 出力先
	 * @param strategy 書き込みストラテジー
	 */
	public KsqlFileWriter(KagerowScriptAccessor scriptAccessor, Path path, KsqlWriterStrategy strategy) {

		// スーパークラス初期化
		super(path);

		// フィールド初期化
		this.strategy = strategy;

		// フィールド初期化
		if (scriptAccessor instanceof KFile kFile) {
			super.rawname = kFile.rawname;
			super.rawversion = kFile.rawversion;
			super.rawsummary = kFile.rawsummary;
			super.rawmode = kFile.rawmode;
			super.mode = kFile.getMode();
			super.rawschema = kFile.rawschema;
			super.rawcacheId = kFile.rawcacheId;
			super.rawinputPlugins = kFile.rawinputPlugins;
			super.rawoutputPlugins = kFile.rawoutputPlugins;
			super.rawksqls = kFile.rawksqls;
			super.rawcommand = kFile.rawcommand;
		}
		super.name = scriptAccessor.getName();
		super.version = scriptAccessor.getVersion();
		super.summary = scriptAccessor.getSummary();
		this.mode = scriptAccessor.getMode().toString();
		super.mode = scriptAccessor.getMode();
		super.schema = scriptAccessor.getSchema();
		super.cacheId = scriptAccessor.getCacheId();
		super.inputPlugins = scriptAccessor.getInputPlugins();
		super.outputPlugins = scriptAccessor.getOutputPlugins();
		super.ksqls = scriptAccessor.getKsqls();
		super.command = scriptAccessor.getCommand();
		super.env = scriptAccessor.getEnv();
	}

	/**
	 * 対象を変換する処理を提供します
	 * @param target 処理対象
	 * @return 処理結果
	 * @throws KFileParseException 変換失敗
	 */
	protected abstract String parse(String target) throws KFileParseException;

	/**
	 * 対象を変換する処理を提供します<br/>
	 * このメソッドの処理は対象の文字列が長くなる可能性が高い処理に使用されます
	 * @param target 処理対象
	 * @return 処理結果
	 * @throws KFileParseException 変換失敗
	 */
	protected abstract String longParse(String target) throws KFileParseException;

	/** {@inheritDoc} */
	@Override
	public void outputXML() throws KFileParseException, AppLogicException {

		// 編集終了
		stateManager.end();

		try {
			// トランザクション開始
			stateManager.begin();
			/**
			 * XML解析
			 */
			try {

				// DocumentFactory生成
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				DocumentBuilder builder = factory.newDocumentBuilder();
				// DOM生成
				Document document = builder.newDocument();

				/** 環境変数初期化 */
				Element env = setEnv(document, super.env);

				/** コンフィグレーションルート要素生成 */
				Element confRootElement = document.createElement(CONF_ROOT);
				/** スクリプト名称保持フィールド */
				Element name = setName(document, super.rawname);
				confRootElement.appendChild(name);
				/** スクリプト概要保持フィールド */
				Element summary = setSummary(document, super.rawsummary);
				confRootElement.appendChild(summary);
				/** スクリプト実行モード保持フィールド */
				Element mode = setMode(document, this.mode);
				confRootElement.appendChild(mode);
				/** スクリプトカレントスキーマ保持フィールド */
				Element schema = setSchema(document, super.rawschema);
				confRootElement.appendChild(schema);
				/** スクリプトキャッシュID保持フィールド */
				Element cacheId = setCacheId(document, super.rawcacheId);
				if (Objects.nonNull(cacheId)) {
					confRootElement.appendChild(cacheId);
				}

				/** プラグインルート要素生成 */
				Element pluginRootElement = document.createElement(PLUGIN_ROOT);
				/** スクリプト入力プラグインリスト保持フィールド */
				Element inputPlugins = setInputPlugins(document, super.rawinputPlugins);
				if (Objects.nonNull(inputPlugins)) {
					pluginRootElement.appendChild(inputPlugins);
				}
				/** スクリプト出力プラグインリスト保持フィールド */
				Element outputPlugins = setOutputPlugins(document, super.rawoutputPlugins);
				if (Objects.nonNull(outputPlugins)) {
					pluginRootElement.appendChild(outputPlugins);
				}

				/** スクリプトKSQL一覧リスト保持フィールド */
				Element ksqls = setKsqls(document, super.rawksqls);
				/** スクリプト実行コマンド一覧リスト保持フィールド */
				Element command = setCommand(document, super.rawcommand);

				/** DOM構築 */
				// ルート要素生成
				Element rootElement = document.createElement(ROOT);
				// xmlns:xsi を定義
				rootElement.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
				// xsi:noNamespaceSchemaLocation を設定
				rootElement.setAttributeNS(
						"http://www.w3.org/2001/XMLSchema-instance",
						"xsi:noNamespaceSchemaLocation",
						super.rawversion);
				// コンフィグレーション追加
				rootElement.appendChild(confRootElement);
				// 環境変数追加
				if (Objects.nonNull(env)) {
					rootElement.appendChild(env);
				}
				// プラグイン追加
				if (Objects.nonNull(inputPlugins) || Objects.nonNull(outputPlugins)) {
					rootElement.appendChild(pluginRootElement);
				}
				// KSQL追加
				rootElement.appendChild(ksqls);
				// コマンド追加
				if (Objects.nonNull(command)) {
					rootElement.appendChild(command);
				}
				// DOMに追加
				document.appendChild(rootElement);

				try {
					/**
					 * XML書き出し準備
					 */
					Source xmlSource = new DOMSource(document);
					Result xmlResult = new StreamResult(path.toFile());

					/**
					 * XMLバリデーション
					 */
					// SchemaFactory生成
					SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
					// XSDクラスパスへ変換
					String xsdFileName = XSD_DIR_NAME + super.rawversion;
					// XSD読み込み
					try (InputStream xsd = getClass().getResourceAsStream(xsdFileName)) {
						StreamSource xsdSource = new StreamSource(xsd);
						Schema xmlSchema = schemaFactory.newSchema(xsdSource);
						try {
							// Validatorの生成
							Validator validator = xmlSchema.newValidator();
							validator.validate(xmlSource);
						} catch (SAXException e) {
							if (e instanceof SAXParseException exp) {
								// 必須要素不足
								throw toKFileParseException(exp);
							}
							// XMLファイル不正
							throw new AppLogicException(ErrorMessage.CODE_024.getMessage(), e);
						}
					}

					/**
					 * XML書き出し
					 */
					Transformer transformer = TransformerFactory.newDefaultInstance().newTransformer();
					transformer.setOutputProperty(OutputKeys.INDENT, StringUtils.YES_STR);
					transformer.setOutputProperty(OutputKeys.ENCODING, StandardCharsets.UTF_8.toString());
					transformer.transform(xmlSource, xmlResult);

					// 変更確定
					stateManager.commit();

				} catch (IOException | SAXException | TransformerException e) {
					// XMLファイル不正
					throw new KFileParseException(ErrorMessage.CODE_027.getMessage(), e);
				} catch (DOMException e) {
					// DOM要素生成失敗
					throw new KFileParseException(ErrorMessage.CODE_028.getMessage(), e);
				}

			} catch (ParserConfigurationException e) {
				// DOM生成失敗
				throw new KFileParseException(e);
			}

		} catch (Exception e) {
			// ロールバック
			stateManager.rollback();
			// 再スロー
			throw e;
		} finally {
			// トランザクション終了
			stateManager.end();
		}

	}

	/**
	 * スクリプト環境変数をDOMに設定します<br/>
	 * 任意の要素のため、設定されていない場合nullを返却します
	 * @param document 解析対象
	 * @param env      環境変数一覧
	 * @return 生成された要素
	 * @throws KFileParseException 変換失敗
	 */
	protected Element setEnv(Document document, Map<String, String> env) throws KFileParseException {

		// 任意要素のため、コマンドがない場合は即時リターン
		if (env.isEmpty()) {
			return null;
		}

		// 環境変数用格納先ルートエレメントを生成
		Element rootElement = document.createElement(ENV_ROOT);
		// 子要素の設定
		for (Map.Entry<String, String> entry : env.entrySet()) {
			// 環境変数格納先エレメントを生成
			Element envElement = document.createElement(ENV);
			// 変換処理
			String envName = entry.getKey(), envValue = entry.getValue();
			envName = parse(envName);
			envValue = parse(envValue);
			// 環境変数セット
			envElement.setAttribute(ENV_NAME, envName);
			envElement.setAttribute(ENV_VALUE, envValue);
			// ルートに追加
			rootElement.appendChild(envElement);
		}
		return rootElement;
	}

	/**
	 * スクリプト名称をDOMに設定します
	 * @param document 解析対象
	 * @param value    設定値
	 * @return 生成された要素
	 * @throws KFileParseException 変換失敗
	 */
	protected Element setName(Document document, String value) throws KFileParseException {
		// エレメントを生成
		Element element = document.createElement(CONF_NAME);
		// 変換処理
		value = parse(value);
		// 設定値セット
		element.setTextContent(value);
		return element;
	}

	/**
	 * スクリプト概要をDOMに設定します
	 * @param document 解析対象
	 * @param value    設定値
	 * @return 生成された要素
	 * @throws KFileParseException 変換失敗
	 */
	protected Element setSummary(Document document, String value) throws KFileParseException {
		// エレメントを生成
		Element element = document.createElement(CONF_SUMMARY);
		// 変換処理
		value = parse(value);
		// 設定値セット
		element.setTextContent(value);
		return element;
	}

	/**
	 * キャッシュIDをDOMに設定します<br/>
	 * 任意の要素のため、設定されていない場合nullを返却します
	 * @param document 解析対象
	 * @param value    設定値
	 * @return 生成された要素
	 * @throws KFileParseException 変換失敗
	 */
	protected Element setCacheId(Document document, String value) throws KFileParseException {

		// キャッシュID確認
		if (Objects.isNull(value) || value.isEmpty()) {
			return null;
		}

		// エレメントを生成
		Element element = document.createElement(CONF_CACHE);
		// 変換処理
		value = parse(value);
		// 設定値セット
		element.setTextContent(value);
		return element;
	}

	/**
	 * スクリプトカレントスキーマをDOMに設定します
	 * @param document 解析対象
	 * @param value    設定値
	 * @return 生成された要素
	 * @throws KFileParseException 変換失敗
	 */
	protected Element setSchema(Document document, String value) throws KFileParseException {
		// エレメントを生成
		Element element = document.createElement(CONF_SCHEMA);
		// 変換処理
		value = parse(value);
		// 設定値セット
		element.setTextContent(value);
		return element;
	}

	/**
	 * スクリプト実行モードをDOMに設定します
	 * @param document 解析対象
	 * @param value    設定値
	 * @return 生成された要素
	 * @throws KFileParseException 変換失敗
	 */
	protected Element setMode(Document document, String value) throws KFileParseException {
		// エレメントを生成
		Element element = document.createElement(CONF_MODE);
		// 変換処理
		value = parse(value);
		// 設定値セット
		element.setTextContent(value);
		return element;
	}

	/**
	 * スクリプト入力プラグインリストをDOMに設定します<br/>
	 * 任意の要素のため、設定されていない場合nullを返却します
	 * @param document     解析対象
	 * @param inputPlugins 環境変数一覧
	 * @return 生成された要素
	 * @throws KFileParseException 変換失敗
	 */
	protected Element setInputPlugins(Document document, List<KagerowPluginAccessor> inputPlugins)
			throws KFileParseException {

		// 任意要素のため、コマンドがない場合は即時リターン
		if (Objects.isNull(inputPlugins) || inputPlugins.isEmpty()) {
			return null;
		}

		// エレメントを生成
		Element element = document.createElement(PLUGIN_INPUT);

		// 子要素追加
		for (KagerowPluginAccessor plugin : inputPlugins) {
			// プラグインエレメントを生成
			Element pluginElement = document.createElement(PLUGIN);
			// 変換処理
			String id = plugin.getId(), name = plugin.getName(), pkg = plugin.getPackageName();
			id = parse(id);
			name = parse(name);
			// デフォルトパッケージの場合属性は設定しない
			if (!StringUtils.DEFAULT.equals(pkg)) {
				pkg = parse(pkg);
				pluginElement.setAttribute(PLUGIN_PKG_NM, pkg);
			}
			// プラグイン設定を追加
			pluginElement.setAttribute(PLUGIN_ID, id);
			pluginElement.setAttribute(PLUGIN_NM, name);
			String next = plugin.next();
			if (Objects.nonNull(next) && !next.isEmpty()) {
				pluginElement.setAttribute(PLUGIN_NID, next);
			}

			// プラグインパラメータ追加
			Map<String, String> params = plugin.getParam();
			for (Map.Entry<String, String> entry : params.entrySet()) {
				// パラメータエレメントを生成
				Element paramElement = document.createElement(PLUGIN_PARAM);
				// 変換処理
				String paramName = entry.getKey(), paramValue = entry.getValue();
				paramName = parse(paramName);
				paramValue = longParse(paramValue);
				// パラメータセット
				paramElement.setAttribute(PLUGIN_PARAM_NM, paramName);
				paramElement.setTextContent(paramValue);
				// プラグインエレメントに追加
				pluginElement.appendChild(paramElement);
			}

			// ルートに追加
			element.appendChild(pluginElement);

		}
		return element;
	}

	/**
	 * スクリプト出力プラグインリストをDOMに設定します<br/>
	 * 任意の要素のため、設定されていない場合nullを返却します
	 * @param document      解析対象
	 * @param outputPlugins 環境変数一覧
	 * @return 生成された要素
	 * @throws KFileParseException 変換失敗
	 */
	protected Element setOutputPlugins(Document document, List<KagerowPluginAccessor> outputPlugins)
			throws KFileParseException {

		// 任意要素のため、コマンドがない場合は即時リターン
		if (Objects.isNull(outputPlugins) || outputPlugins.isEmpty()) {
			return null;
		}

		// エレメントを生成
		Element element = document.createElement(PLUGIN_OUTPUT);

		// 子要素追加
		for (KagerowPluginAccessor plugin : outputPlugins) {

			// プラグインエレメントを生成
			Element pluginElement = document.createElement(PLUGIN);
			// 変換処理
			String id = plugin.getId(), name = plugin.getName(), pkg = plugin.getPackageName();
			id = parse(id);
			name = parse(name);
			// デフォルトパッケージの場合属性は設定しない
			if (!StringUtils.DEFAULT.equals(pkg)) {
				pkg = parse(pkg);
				pluginElement.setAttribute(PLUGIN_PKG_NM, pkg);
			}
			// プラグイン設定を追加
			pluginElement.setAttribute(PLUGIN_ID, id);
			pluginElement.setAttribute(PLUGIN_NM, name);
			String next = plugin.next();
			if (Objects.nonNull(next) && !next.isEmpty()) {
				pluginElement.setAttribute(PLUGIN_NID, next);
			}

			// プラグインパラメータ追加
			Map<String, String> params = plugin.getParam();
			for (Map.Entry<String, String> entry : params.entrySet()) {
				// パラメータエレメントを生成
				Element paramElement = document.createElement(PLUGIN_PARAM);
				// 変換処理
				String paramName = entry.getKey(), paramValue = entry.getValue();
				paramName = parse(paramName);
				paramValue = longParse(paramValue);
				// パラメータセット
				paramElement.setAttribute(PLUGIN_PARAM_NM, paramName);
				paramElement.setTextContent(paramValue);
				// プラグインエレメントに追加
				pluginElement.appendChild(paramElement);
			}

			// ルートに追加
			element.appendChild(pluginElement);

		}
		return element;
	}

	/**
	 * スクリプトKSQL一覧リストをDOMに設定します
	 * @param document 解析対象
	 * @param ksqls    環境変数一覧
	 * @return 生成された要素
	 * @throws KFileParseException 変換失敗
	 */
	protected Element setKsqls(Document document, List<KagerowSqlAccessor> ksqls) throws KFileParseException {

		// 環境変数用格納先ルートエレメントを生成
		Element rootElement = document.createElement(KSQL_ROOT);

		// 子要素追加
		for (KagerowSqlAccessor ksql : ksqls) {

			// スクリプトエレメントを生成
			Element ksqlElement = document.createElement(KSQL);

			// 変換処理
			String id = ksql.getId(), name = ksql.getName(), next = ksql.next();
			id = parse(id);
			name = parse(name);
			next = parse(next);
			// KSQL設定を追加
			ksqlElement.setAttribute(KSQL_ID, id);
			ksqlElement.setAttribute(KSQL_NM, name);
			if (Objects.nonNull(next) && !next.isEmpty()) {
				ksqlElement.setAttribute(KSQL_NID, next);
			}

			// バインド変数追加
			if (!ksql.variable().isEmpty()) {
				// バインド変数ルートエレメントを生成
				Element varriableRootElement = document.createElement(KSQL_VAR_DEC);
				for (Map.Entry<String, String> varriable : ksql.variable().entrySet()) {
					// バインド変数エレメントを生成
					Element varriableElement = document.createElement(KSQL_VAR);
					// 変換処理
					String varriableName = varriable.getKey(), varriableValue = varriable.getValue();
					varriableName = parse(varriableName);
					varriableValue = parse(varriableValue);
					// バインド変数設定
					varriableElement.setAttribute(KSQL_VAR_NM, varriableName);
					varriableElement.setAttribute(KSQL_VAR_VAL, varriableValue);
					// バインド変数追加
					varriableRootElement.appendChild(varriableElement);
				}
				// スクリプトエレメントにバインド変数を追加
				ksqlElement.appendChild(varriableRootElement);
			}

			// KSQL本体追加
			// KSQL本体エレメントを生成
			Element ksqlScriptElement = document.createElement(KSQL_SQL);
			// 変換処理
			String sqlText = ksql.getSql();
			sqlText = longParse(sqlText);
			// SQL追加
			ksqlScriptElement.setTextContent(sqlText);
			// スクリプトエレメントにKSQL本体を追加
			ksqlElement.appendChild(ksqlScriptElement);

			// ルートエレメントに追加
			rootElement.appendChild(ksqlElement);
		}
		return rootElement;
	}

	/**
	 * スクリプト実行コマンド一覧リストをDOMに設定します<br/>
	 * 任意の要素のため、設定されていない場合nullを返却します
	 * @param document 解析対象
	 * @param command  環境変数一覧
	 * @return 生成された要素
	 * @throws KFileParseException 変換失敗
	 */
	protected Element setCommand(Document document, List<KagerowCmdAccessor> command) throws KFileParseException {

		// 任意要素のため、コマンドがない場合は即時リターン
		if (command.isEmpty()) {
			return null;
		}

		// 先頭の要素を取得
		KagerowCmdAccessor cmd = command.getFirst();

		// コマンドエレメントを生成
		Element rootElement = document.createElement(COMMAND_ROOT);

		// コマンド環境変数追加
		if (!cmd.environmental().isEmpty()) {
			// コマンド環境変数ルートエレメントを生成
			Element environmentalRootElement = document.createElement(COMMAND_ENV);
			for (Map.Entry<String, String> environmental : cmd.environmental().entrySet()) {
				// コマンド環境変数エレメントを生成
				Element environmentalElement = document.createElement(COMMAND_ENV_VAR);
				// 変換処理
				String environmentalName = environmental.getKey(), environmentalValue = environmental.getValue();
				environmentalName = parse(environmentalName);
				environmentalValue = parse(environmentalValue);
				// コマンド環境変数設定
				environmentalElement.setAttribute(COMMAND_ENV_VAR_NM, environmentalName);
				environmentalElement.setAttribute(COMMAND_ENV_VAR_VAL, environmentalValue);
				// コマンド環境変数追加
				environmentalRootElement.appendChild(environmentalElement);
			}
			// ルートエレメントに追加
			rootElement.appendChild(environmentalRootElement);
		}

		// コマンド本体追加

		// 変換処理
		String cmdMode = cmd.getMode().toString(), cmdText = cmd.getCmd();
		cmdMode = parse(cmdMode);
		cmdText = longParse(cmdText);
		// コマンド本体ルートエレメントを生成
		Element cmdElement = document.createElement(COMMAND_CMD);
		// コマンド設定追加
		cmdElement.setAttribute(COMMAND_MODE, cmdMode);
		// コマンド本体追加
		cmdElement.setTextContent(cmdText);
		// ルートエレメントに追加
		rootElement.appendChild(cmdElement);

		return rootElement;
	}
}

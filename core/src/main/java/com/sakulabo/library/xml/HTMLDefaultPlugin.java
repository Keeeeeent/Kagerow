package com.sakulabo.library.xml;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

import javax.sql.RowSet;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.sakulabo.core.Kagerow.Utilities.KagerowDBMode;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;
import com.sakulabo.library.common.DefaultPlugin;
import com.sakulabo.library.common.FileDefaultOutputer;
import com.sakulabo.regulation.annotation.KagerowPlugin;
import com.sakulabo.regulation.annotation.KagerowPlugin.PluginType;
import com.sakulabo.regulation.spi.PluginAdapter;

/**
 * プラグインデフォルトHTMLプラグインクラス
 * 
 * @author keeeeeent
 */
@KagerowPlugin(name = "KagerowHTMLPlugin", types = { PluginType.OUTPUT })
public final class HTMLDefaultPlugin extends DefaultPlugin implements PluginAdapter {

	/** パラメータ名称（出力パス） */
	private static final String OUTPUT_PATH = "OutputPath";
	/** パラメータ名称（SQLID） */
	private static final String KSQL_ID = "KsqlId";

	/** 日付フォーマッター */
	private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("YYYY-MM-dd");
	/** HTMLスタイル */
	private static final String HTML_STYLE = """
			table {
			        width: 100%;
			        border-collapse: collapse;
			        margin-bottom: 1em;
			      }
			      tr:first-child {
			         background-color: #cdefff;
			      }
			      th, td {
			        padding: 0.5em;
			        border: 1px solid #ddd;
			        text-align: left;
			      }
			      @media screen and (max-width: 600px) {
			        table {
			          display: block;
			          overflow-x: auto;
			          white-space: nowrap;
			        }
			      }""";
	/** HTML雛形 */
	private static final String HTML_FORMAT = """
			<!DOCTYPE html>
			<html lang="ja">
				<head>
				    <meta charset="UTF-8" />
				    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
				    <title>{0}</title>
				    <style>
				    {1}
				    </style>
				</head>
				<body></body>
			</html>""";

	/** {@inheritDoc} */
	@Override
	@Deprecated
	public void input(Map<String, String> params, KagerowDBMode mode, Connection connection) {
		;
	}

	/** {@inheritDoc} */
	@Override
	@Param(value = OUTPUT_PATH, required = true)
	@Param(value = KSQL_ID, required = true)
	public void output(Map<String, String> params, List<KagerowRowSet> data) {

		// パラメータ初期化
		Path outputPath = getPath(params, OUTPUT_PATH);
		String ksqlId = params.get(KSQL_ID);
		KagerowRowSet targetData = select(ksqlId, data);

		// 雛形フォーマット
		String html = MessageFormat.format(HTML_FORMAT, targetData.name(), HTML_STYLE);

		// HTML生成
		try {
			// XMLパースインスタンス生成
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			// DOM取得
			InputSource source = new InputSource(new StringReader(html));
			Document document = builder.parse(source);
			// Body取得
			NodeList bodys = document.getElementsByTagName("body");
			Node body = bodys.item(0);
			// データ差し込み
			Node table = insertData(document, targetData);
			body.appendChild(table);
			// ファイル出力
			Source xmlSource = new DOMSource(document);
			Result xmlResult = new StreamResult(outputPath.toFile());
			Transformer transformer = TransformerFactory.newDefaultInstance().newTransformer();
			//			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.ENCODING, StandardCharsets.UTF_8.toString());
			transformer.setOutputProperty(OutputKeys.METHOD, "html");
			transformer.transform(xmlSource, xmlResult);
		} catch (Exception e) {
			KagerowLogger.newAppLogger().err(e);
		}

	}

	/** {@inheritDoc} */
	@Override
	public void validation(Map<String, String> params, PluginType type) throws PluginValidationException {
		if (PluginType.OUTPUT.equals(type)) {
			validParentPath(params, OUTPUT_PATH);
		}
	}

	/**
	 * HTMLを生成します
	 * @param document ドキュメント
	 * @param targetData 格納データ
	 * @return 生成要素
	 * @throws SQLException 実データアクセス失敗
	 */
	private Node insertData(Document document, KagerowRowSet targetData) throws SQLException {

		// 実データ参照
		RowSet rowset = targetData.data();
		// テーブル要素作成
		Node table = document.createElement("table");
		// ヘッダー生成
		Node header = document.createElement("tr");

		// 実データのヘッダーを取得
		ResultSetMetaData metaData = rowset.getMetaData();
		for (int i = 1; i <= metaData.getColumnCount(); i++) {
			String rawName = metaData.getColumnLabel(i);
			Node headerData = document.createElement("th");
			headerData.setTextContent(rawName);
			header.appendChild(headerData);
		}
		// テーブルにデータ追加
		table.appendChild(header);

		// 実データのデータを取得
		while (rowset.next()) {

			// データ格納要素生成
			Node data = document.createElement("tr");

			for (int i = 1; i <= metaData.getColumnCount(); i++) {
				// データ取得
				Object dat = rowset.getObject(i);
				// データ文字列変換
				String outputStr = FileDefaultOutputer.parseRowSet(dat, DATE_FORMATTER, false);
				// HTML変換
				Node td = document.createElement("td");
				td.setTextContent(outputStr);
				// データ追加
				data.appendChild(td);
			}

			// テーブルにデータを追加
			table.appendChild(data);

		}

		return table;
	}

}
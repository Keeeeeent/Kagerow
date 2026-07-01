package com.sakulabo.library.xml;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.JDBCType;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.sakulabo.core.Kagerow.Utilities.KagerowDBMode;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;
import com.sakulabo.library.common.DefaultPlugin;
import com.sakulabo.regulation.annotation.KagerowPlugin;
import com.sakulabo.regulation.annotation.KagerowPlugin.PluginType;
import com.sakulabo.regulation.spi.PluginAdapter;

/**
 * プラグインデフォルトEXCELプラグインクラス
 * 
 * @author keeeeeent
 */
@KagerowPlugin(name = "KagerowEXCELPlugin", types = { PluginType.OUTPUT })
public final class EXCELDefaultPlugin extends DefaultPlugin implements PluginAdapter {

	/** パラメータ名称（出力パス） */
	private static final String OUTPUT_PATH = "OutputPath";
	/** パラメータ名称（SQLID） */
	private static final String KSQL_ID = "KsqlId";
	/** Zip構成格納クラスパス */
	private static final String ROOT_FORMAT_FILE_PATH = "template/xlsx/";
	/** Zip構成ファイル（編集対象） */
	private static final String EDIT_FILE_PATH = "xl/worksheets/sheet1.xml";
	/** Zip構成ファイル一覧リスト */
	private static final List<String> ZIP_FORMAT_FILE_LIST = List.of(
			"[Content_Types].xml",
			"_rels/.rels",
			"xl/workbook.xml",
			"xl/worksheets/",
			EDIT_FILE_PATH,
			"xl/styles.xml",
			"xl/_rels/workbook.xml.rels");

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

		// Excel生成
		try (
				// 出力Excelファイル
				OutputStream output = Files.newOutputStream(outputPath);
				// Zipストリーム
				ZipOutputStream excel = new ZipOutputStream(output);) {
			// Zipファイル構築
			createExcelFileFormat(excel);
		} catch (Exception e) {
			KagerowLogger.newAppLogger().err(e);
			return;
		}

		// Excel編集
		String targetFile = ROOT_FORMAT_FILE_PATH.concat(EDIT_FILE_PATH);
		try (
				FileSystem fileSystem = FileSystems.newFileSystem(outputPath);
				InputStream editFile = Thread.currentThread().getContextClassLoader()
						.getResourceAsStream(targetFile)) {
			// Excelファイルシート編集
			editExcelFile(fileSystem.getPath(EDIT_FILE_PATH), targetData, editFile);
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
	 * Excelファイルを編集します
	 * @param outputPath 仮想ファイルシステムパス
	 * @param targetData 出力対象データ
	 * @param baseData ベースフォーマット
	 * @throws Exception 出力失敗
	 */
	private void editExcelFile(Path outputPath, KagerowRowSet targetData, InputStream baseData) throws Exception {
		// XMLパースインスタンス生成
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		// DOM取得
		Reader reader = new InputStreamReader(baseData, StandardCharsets.UTF_8);
		InputSource source = new InputSource(reader);
		Document document = builder.parse(source);
		// Sheet取得
		NodeList sheets = document.getElementsByTagName("sheetData");
		Node sheet = sheets.item(0);
		// データ差し込み
		insertData(document, sheet, targetData);
		// ファイル出力
		try (OutputStream output = Files.newOutputStream(outputPath)) {
			Source xmlSource = new DOMSource(document);
			Result xmlResult = new StreamResult(output);
			Transformer transformer = TransformerFactory.newDefaultInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.ENCODING, StandardCharsets.UTF_8.toString());
			transformer.transform(xmlSource, xmlResult);
		}
	}

	/**
	 * Excelのデータ構造を構築します
	 * @param excel 構築先メモリ
	 * @throws Exception 構築失敗
	 */
	private void createExcelFileFormat(ZipOutputStream excel) throws Exception {
		// Zipファイルの構築
		for (String entry : ZIP_FORMAT_FILE_LIST) {

			// Zipエントリー作成
			ZipEntry zipEntry = new ZipEntry(entry);
			excel.putNextEntry(zipEntry);

			// ファイル読み込み
			String path = ROOT_FORMAT_FILE_PATH.concat(entry);
			// クラスローダーから対象のバイトデータを取得
			try (InputStream target = Thread.currentThread().getContextClassLoader().getResourceAsStream(path)) {
				// データ読み取り
				target.transferTo(excel);
			}

			// 事後処理
			excel.closeEntry();
		}
	}

	/**
	 * Excelテーブル構造を生成します
	 * @param document ワークブック
	 * @param sheet ワークシート
	 * @param targetData 格納データ
	 * @throws SQLException 実データアクセス失敗
	 */
	private void insertData(Document document, Node sheet, KagerowRowSet targetData) throws SQLException {

		// 実データ参照
		RowSet rowset = targetData.data();
		// 行カーソル変数
		int rowCounter = 1;

		// ヘッダー生成
		Element headerRow = document.createElement("row");
		headerRow.setAttribute("r", String.valueOf(rowCounter++));
		// 実データのヘッダーを取得
		ResultSetMetaData metaData = rowset.getMetaData();
		for (int colCounter = 1; colCounter <= metaData.getColumnCount(); colCounter++) {
			String rawName = metaData.getColumnLabel(colCounter);
			// セル定義追加
			Element cell = document.createElement("c");
			// セルの属性追加
			cell.setAttribute("r", toColumnName(colCounter, rowCounter - 1));
			cell.setAttribute("t", "inlineStr");
			cell.setAttribute("s", "1");
			// ネスト要素追加
			Node is = document.createElement("is");
			Node target = document.createElement("t");
			cell.appendChild(is).appendChild(target);
			// データ設定
			target.setTextContent(rawName);
			// ヘッダーとして追加
			headerRow.appendChild(cell);
		}
		// シートにデータ追加
		sheet.appendChild(headerRow);

		// 実データのデータを取得
		while (rowset.next()) {

			// ヘッダー生成
			Element dataRow = document.createElement("row");
			dataRow.setAttribute("r", String.valueOf(rowCounter++));

			for (int colCounter = 1; colCounter <= metaData.getColumnCount(); colCounter++) {

				// セル定義追加
				Element cell = document.createElement("c");
				// セルの属性追加
				cell.setAttribute("r", toColumnName(colCounter, rowCounter - 1));

				// データ取得
				Object dat = rowset.getObject(colCounter);
				// データ型情報取得
				int type = metaData.getColumnType(colCounter);

				// データ設定
				convertExcelValue(dat, type, document, cell);
				// ヘッダーとして追加
				dataRow.appendChild(cell);

			}

			// シートにデータ追加
			sheet.appendChild(dataRow);

		}

	}

	/**
	 * セルをデータ設定可能な状態にし、セル設定可能な文字列を生成します
	 * @param dat 設定データ
	 * @param type データタイプ
	 * @param document xmlインスタンス
	 * @param cell セル参照
	 */
	private void convertExcelValue(Object dat, int type, Document document, Element cell) {
		// nullの場合<NULL>として返却
		if (Objects.isNull(dat)) {
			// 属性設定
			cell.setAttribute("t", "inlineStr");
			// データ設定
			String data = "<NULL>";
			setCell(document, cell, data, true);
			return;
		}
		// 非nullの場合、データ種別によって対応
		JDBCType sqlType = JDBCType.valueOf(type);
		switch (sqlType) {
		case DATE -> {
			// 属性設定
			cell.setAttribute("s", "3");
			// データ変換
			java.sql.Date date = (java.sql.Date) dat;
			LocalDate ld = date.toLocalDate();
			// データ設定
			String data = toExcelDateTime(ld.atStartOfDay());
			setCell(document, cell, data, false);
		}
		case TIMESTAMP -> {
			// 属性設定
			cell.setAttribute("s", "4");
			// データ変換
			java.sql.Timestamp timestamp = (java.sql.Timestamp) dat;
			LocalDateTime ldt = timestamp.toLocalDateTime();
			// データ設定
			String data = toExcelDateTime(ldt);
			setCell(document, cell, data, false);
		}
		case TIMESTAMP_WITH_TIMEZONE -> {
			// 属性設定
			cell.setAttribute("s", "4");
			// データ変換
			java.time.OffsetDateTime timestamp = (java.time.OffsetDateTime) dat;
			LocalDateTime ldt = timestamp.toLocalDateTime();
			// データ設定
			String data = toExcelDateTime(ldt);
			setCell(document, cell, data, false);
		}
		case DECIMAL, NUMERIC -> {
			// 属性設定
			cell.setAttribute("s", "5");
			// データ設定
			String data = ((BigDecimal) dat).toPlainString();
			setCell(document, cell, data, false);
		}
		case FLOAT, DOUBLE -> {
			// 属性設定
			cell.setAttribute("s", "5");
			// データ設定
			String data = Objects.toString(dat);
			setCell(document, cell, data, false);
		}
		case BIGINT, INTEGER, TINYINT -> {
			// 属性設定
			cell.setAttribute("s", "6");
			// データ設定
			String data = Objects.toString(dat);
			setCell(document, cell, data, false);
		}
		case BOOLEAN -> {
			// 属性設定
			cell.setAttribute("t", "b");
			// データ設定
			String data = ((Boolean) dat) ? "1" : "0";
			setCell(document, cell, data, false);
		}
		default -> {
			// 属性設定
			cell.setAttribute("t", "inlineStr");
			// データ設定
			String data = Objects.toString(dat);
			setCell(document, cell, data, true);
		}
		}
		;
	}

	/**
	 * セルにスタイルとデータを適用します
	 * @param document xmlインスタンス
	 * @param cell セル参照
	 * @param data データ
	 * @param isInlineStr インライン文字列フラグ
	 */
	private void setCell(Document document, Element cell, String data, boolean isInlineStr) {
		// データ設定
		if (isInlineStr) {
			Node target = document.createElement("t");
			Node is = document.createElement("is");
			cell.appendChild(is).appendChild(target);
			target.setTextContent(data);
		} else {
			Node target = document.createElement("v");
			cell.appendChild(target);
			target.setTextContent(data);
		}
	}

	/**
	 * Excel標準の日付型に変換します
	 * @param dt 変換対象
	 * @return 変換後
	 */
	private String toExcelDateTime(LocalDateTime dt) {
		LocalDateTime base = LocalDateTime.of(1899, 12, 30, 0, 0);
		long days = ChronoUnit.DAYS.between(base, dt);
		long seconds = ChronoUnit.SECONDS.between(base.plusDays(days), dt);
		return String.valueOf(days + (seconds / 86400.0));
	}

	/**
	 * Excel標準の列番号を生成します
	 * @param column 列インデックス
	 * @param row 行インデックス
	 * @return Excel標準の列番号
	 */
	public String toColumnName(int column, int row) {
		StringBuilder sb = new StringBuilder();
		while (column > 0) {
			sb.append((char) ('A' + (--column % 26)));
			column /= 26;
		}
		String colStr = sb.reverse().toString();
		String rowStr = String.valueOf(row);
		return colStr.concat(rowStr);
	}

}
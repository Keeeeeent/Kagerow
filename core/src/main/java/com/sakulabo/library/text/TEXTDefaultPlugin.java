package com.sakulabo.library.text;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSetMetaData;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

import javax.sql.RowSet;

import com.sakulabo.core.Kagerow.Utilities.KagerowDBMode;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;
import com.sakulabo.library.common.DefaultPlugin;
import com.sakulabo.library.common.FileDefaultOutputer;
import com.sakulabo.regulation.annotation.KagerowPlugin;
import com.sakulabo.regulation.annotation.KagerowPlugin.PluginType;
import com.sakulabo.regulation.spi.PluginAdapter;

/**
 * プラグインデフォルトJsonプラグインクラス
 * @author keeeeeent
 */
@KagerowPlugin(name = "KagerowTEXTPlugin", types = { PluginType.OUTPUT })
public final class TEXTDefaultPlugin extends DefaultPlugin implements PluginAdapter {

	/** パラメータ名称（出力パス） */
	private static final String OUTPUT_PATH = "OutputPath";
	/** パラメータ名称（SQLID） */
	private static final String KSQL_ID = "KsqlId";
	/** パラメータ名称（日付フォーマット） */
	private static final String DATE_FORMAT = "DateFormat";

	/** エスケープ不要リスト */
	private static final List<Class<?>> NON_ESCAPE_LIST = List.of(
			byte.class, short.class, int.class, long.class, float.class, double.class,
			Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class,
			Number.class, BigDecimal.class, BigInteger.class,
			boolean.class, Boolean.class);

	/** {@inheritDoc} */
	@Override
	@Deprecated
	public void input(Map<String, String> params, KagerowDBMode mode, Connection connection) {
	}

	/** {@inheritDoc} */
	@Override
	@Param(value = OUTPUT_PATH, required = true)
	@Param(value = KSQL_ID, required = true)
	@Param(value = DATE_FORMAT, defaultValue = "YYYY-MM-dd")
	public void output(Map<String, String> params, List<KagerowRowSet> data) {

		// パラメータ初期化
		Path outputPath = getPath(params, OUTPUT_PATH);
		String ksqlId = params.get(KSQL_ID);
		KagerowRowSet targetData = select(ksqlId, data);
		String dateFormat = params.get(DATE_FORMAT);

		// 日付フォーマッター生成
		SimpleDateFormat dateFormater = new SimpleDateFormat(dateFormat);

		// Json構築ビルダー生成
		StringJoiner stringArray = new StringJoiner(System.lineSeparator());

		try {

			// 結果セット取得
			RowSet rowSet = targetData.data();
			ResultSetMetaData metaData = rowSet.getMetaData();
			String[] heders = new String[metaData.getColumnCount()];
			for (int i = 1; i <= metaData.getColumnCount(); i++) {
				heders[i - 1] = metaData.getColumnLabel(i);
			}

			// データ構築
			while (rowSet.next()) {

				// ビルダー初期化
				StringJoiner stringData = new StringJoiner(" ");

				for (int i = 1; i <= metaData.getColumnCount(); i++) {
					// データ取得
					Object rowData = rowSet.getObject(i);
					String keyName = heders[i - 1] + "=";
					String formatedData = null;
					if (Objects.isNull(rowData)) {
						formatedData = FileDefaultOutputer.parseRowSet(rowData, dateFormater, false);
					} else if (NON_ESCAPE_LIST.contains(rowData.getClass())) {
						formatedData = FileDefaultOutputer.parseRowSet(rowData, dateFormater, false);
					} else {
						formatedData = FileDefaultOutputer.parseRowSet(rowData, dateFormater, true);
					}
					stringData.add(keyName.concat(formatedData));
				}
				stringArray.add(stringData.toString());
			}

			// データ書き出し
			Files.writeString(outputPath, stringArray.toString(), StandardCharsets.UTF_8);
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

}

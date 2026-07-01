package com.sakulabo.library.tsv;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

import com.sakulabo.core.Kagerow.Utilities.KagerowDBMode;
import com.sakulabo.core.Kagerow.Utilities.KagerowFileReaderFactory;
import com.sakulabo.library.common.DefaultPlugin;
import com.sakulabo.library.common.FileDefaultInputer;
import com.sakulabo.library.common.FileDefaultOutputer;
import com.sakulabo.regulation.annotation.KagerowPlugin;
import com.sakulabo.regulation.annotation.KagerowPlugin.PluginType;
import com.sakulabo.regulation.spi.PluginAdapter;

/**
 * プラグインデフォルトTSVプラグインクラス
 * @author keeeeeent
 */
@KagerowPlugin(name = "KagerowTSVPlugin", types = { PluginType.INPUT, PluginType.OUTPUT })
public final class TSVDefaultPlugin extends DefaultPlugin implements PluginAdapter {

	/** パラメータ名称（入力パス） */
	private static final String INPUT_PATH = "InputPath";
	/** パラメータ名称（入力ファイルヘッダー有無） */
	private static final String IS_HEADER = "IsHeader";
	/** パラメータ名称（入力データ変換DDL） */
	private static final String INIT_DDL = "InitDDL";
	/** パラメータ名称（一時テーブル名称） */
	private static final String TABLE_NAME = "TableName";
	/** パラメータ名称（出力パス） */
	private static final String OUTPUT_PATH = "OutputPath";
	/** パラメータ名称（エスケープ有無） */
	private static final String IS_ESCAPE = "IsEscape";
	/** パラメータ名称（文字コード） */
	private static final String CHARSET = "Charset";
	/** パラメータ名称（SQLID） */
	private static final String KSQL_ID = "KsqlId";
	/** パラメータ名称（日付フォーマット） */
	private static final String DATE_FORMAT = "DateFormat";

	/** {@inheritDoc} */
	@Override
	public void initialize() {
		;
	}

	/** {@inheritDoc} */
	@Override
	public void close() throws Exception {
		;
	}

	/** {@inheritDoc} */
	@Override
	@Param(value = INPUT_PATH, required = true)
	@Param(value = TABLE_NAME, defaultValue = "temporary")
	@Param(value = CHARSET, defaultValue = "UTF-8")
	@Param(value = IS_HEADER, required = true)
	@Param(value = INIT_DDL, required = true)
	public void input(Map<String, String> params, KagerowDBMode mode, Connection connection) {

		// パラメータ初期化
		Path inputPath = getPath(params, INPUT_PATH);
		Boolean isHeader = getBoolean(params, IS_HEADER);
		String initDDL = params.get(INIT_DDL);
		String tableName = params.get(TABLE_NAME);
		Charset charset = getCharset(params, CHARSET);

		// データインプットインスタンス生成
		FileDefaultInputer inputer = new FileDefaultInputer(
				KagerowFileReaderFactory.newTSVFileReaderFactory(),
				inputPath,
				isHeader,
				initDDL,
				tableName,
				mode,
				charset);

		// データインプット
		inputer.input(connection);

	}

	/** {@inheritDoc} */
	@Override
	@Param(value = DATE_FORMAT, defaultValue = "YYYY-MM-dd")
	@Param(value = OUTPUT_PATH, required = true)
	@Param(value = IS_ESCAPE, defaultValue = "false")
	@Param(value = CHARSET, defaultValue = "UTF-8")
	@Param(value = KSQL_ID, required = true)
	@Param(value = IS_HEADER, defaultValue = "false")
	public void output(Map<String, String> params, List<KagerowRowSet> data) {

		// パラメータ初期化
		Path outputPath = getPath(params, OUTPUT_PATH);
		Boolean isEscape = getBoolean(params, IS_ESCAPE);
		Boolean isHeader = getBoolean(params, IS_HEADER);
		Charset charset = getCharset(params, CHARSET);
		String ksqlId = params.get(KSQL_ID);
		KagerowRowSet targetData = select(ksqlId, data);
		String dateFormat = params.get(DATE_FORMAT);

		// データアウトプットインスタンス生成
		FileDefaultOutputer outputer = new FileDefaultOutputer(outputPath, isEscape, isHeader,
				charset, targetData, dateFormat);

		// データアウトプット
		outputer.output('\t');

	}

	/** {@inheritDoc} */
	@Override
	public void validation(Map<String, String> params, PluginType type) throws PluginValidationException {
		switch (type) {
		case INPUT:
			validPath(params, INPUT_PATH);
			validCharset(params, CHARSET);
			break;
		case OUTPUT:
			validParentPath(params, OUTPUT_PATH);
			validCharset(params, CHARSET);
			break;
		}
	}

}

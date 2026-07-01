package com.sakulabo.library.xml;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetProvider;
import javax.sql.rowset.WebRowSet;

import com.sakulabo.core.Kagerow.Utilities.KagerowDBMode;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;
import com.sakulabo.library.common.DefaultPlugin;
import com.sakulabo.regulation.annotation.KagerowPlugin;
import com.sakulabo.regulation.annotation.KagerowPlugin.PluginType;
import com.sakulabo.regulation.spi.PluginAdapter;

/**
 * プラグインデフォルトXMLプラグインクラス
 * 
 * @author keeeeeent
 */
@KagerowPlugin(name = "KagerowXMLPlugin", types = { PluginType.OUTPUT })
public final class XMLDefaultPlugin extends DefaultPlugin implements PluginAdapter {

	/** パラメータ名称（出力パス） */
	private static final String OUTPUT_PATH = "OutputPath";
	/** パラメータ名称（文字コード） */
	private static final String CHARSET = "Charset";
	/** パラメータ名称（SQLID） */
	private static final String KSQL_ID = "KsqlId";

	/** {@inheritDoc} */
	@Override
	@Deprecated
	public void input(Map<String, String> params, KagerowDBMode mode, Connection connection) {
		;
	}

	/** {@inheritDoc} */
	@Override
	@Param(value = OUTPUT_PATH, required = true)
	@Param(value = CHARSET, defaultValue = "UTF-8")
	@Param(value = KSQL_ID, required = true)
	public void output(Map<String, String> params, List<KagerowRowSet> data) {

		// パラメータ初期化
		Path outputPath = getPath(params, OUTPUT_PATH);
		Charset charset = getCharset(params, CHARSET);
		String ksqlId = params.get(KSQL_ID);
		KagerowRowSet targetData = select(ksqlId, data);

		try (
				OutputStream output = Files.newOutputStream(outputPath);
				OutputStreamWriter converter = new OutputStreamWriter(output, charset);
				BufferedWriter writer = new BufferedWriter(converter)) {
			// RowSetFactory生成
			RowSetFactory factory = RowSetProvider.newFactory();
			// 空のWebRowSetを生成
			WebRowSet rowset = factory.createWebRowSet();
			// WebRowSet初期化
			rowset.populate(targetData.data());
			// XML出力
			rowset.writeXml(rowset, writer);
		} catch (Exception e) {
			KagerowLogger.newAppLogger().err(e);
		}

	}

	/** {@inheritDoc} */
	@Override
	public void validation(Map<String, String> params, PluginType type) throws PluginValidationException {
		if (PluginType.OUTPUT.equals(type)) {
			validParentPath(params, OUTPUT_PATH);
			validCharset(params, CHARSET);
		}
	}

}

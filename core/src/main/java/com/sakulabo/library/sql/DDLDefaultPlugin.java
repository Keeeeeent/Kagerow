package com.sakulabo.library.sql;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import com.sakulabo.core.Kagerow.Context.KagerowVirtualFileContext;
import com.sakulabo.core.Kagerow.Utilities.KagerowDBMode;
import com.sakulabo.core.Kagerow.Utilities.KagerowKsqlTransformer;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;
import com.sakulabo.core.Kagerow.Utilities.KagerowUtilities;
import com.sakulabo.library.common.DefaultPlugin;
import com.sakulabo.regulation.annotation.KagerowPlugin;
import com.sakulabo.regulation.annotation.KagerowPlugin.PluginType;
import com.sakulabo.regulation.spi.PluginAdapter;

/**
 * プラグインデフォルトCSVプラグインクラス
 * @author keeeeeent
 */
@KagerowPlugin(name = "KagerowDDLPlugin", types = { PluginType.INPUT }, multiSize = 10)
public class DDLDefaultPlugin extends DefaultPlugin implements PluginAdapter {

	/** パラメータ名称（入力DDL） */
	private static final String DDL = "DDL";

	/** {@inheritDoc} */
	@Override
	@Param(value = DDL, required = true)
	public void input(Map<String, String> params, KagerowDBMode mode, Connection connection) {

		// 実行DDL取得
		String ddl = params.get(DDL);

		/**
		 * H2ではコネクションをクローズしないとDDLの変更が反映されてないため必ずクローズする
		 * この仕様はFileモードのみのため、オンメモリでは意識する必要はない
		 */
		try (connection) {
			// カレント取得
			String schema = connection.getSchema();
			// コンテキスト取得
			KagerowVirtualFileContext context = KagerowUtilities.getContext(KagerowVirtualFileContext._NAME);
			// KDBリンク
			KagerowKsqlTransformer linker = KagerowKsqlTransformer.createDataBaseLinker(schema, context);
			ddl = linker.transform(ddl);
			try (Statement statement = connection.createStatement()) {
				// DDL実行
				statement.execute(ddl);
				// 変更確定
				connection.commit();
			} catch (Exception e) {
				// ロールバック
				connection.rollback();
				throw e;
			}
		} catch (Exception e) {
			KagerowLogger.newAppLogger().err(e);
		}

	}

	/** {@inheritDoc} */
	@Override
	@Deprecated
	public void output(Map<String, String> params, List<KagerowRowSet> data) {
		;
	}

	/** {@inheritDoc} */
	@Override
	@Deprecated
	public void validation(Map<String, String> params, PluginType type) throws PluginValidationException {
		;
	}

}

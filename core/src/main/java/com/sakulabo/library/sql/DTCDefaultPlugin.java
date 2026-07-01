package com.sakulabo.library.sql;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import com.sakulabo.core.Kagerow.Utilities.KagerowDBMode;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;
import com.sakulabo.library.common.DefaultPlugin;
import com.sakulabo.regulation.annotation.KagerowPlugin;
import com.sakulabo.regulation.annotation.KagerowPlugin.PluginType;
import com.sakulabo.regulation.spi.PluginAdapter;

/**
 * プラグインデフォルトDUALテーブル生成プラグインクラス
 * DTC= Dual Table Creater
 * @author keeeeeent
 */
@KagerowPlugin(name = "KagerowDTCPlugin", types = { PluginType.INPUT }, multiSize = 10)
public class DTCDefaultPlugin extends DefaultPlugin implements PluginAdapter {

	/** 対象外モード */
	private static final EnumSet<KagerowDBMode> IGNORE_MODE;
	static {
		IGNORE_MODE = EnumSet.of(
				KagerowDBMode.ILLEGALITY,
				KagerowDBMode.ORACLE);
	}

	/** {@inheritDoc} */
	@Override
	public void input(Map<String, String> params, KagerowDBMode mode, Connection connection) {
		// 対象外モードでない場合、Dual表を生成
		// TODO 他のモードを追加出来次第、テストを追加予定
		if (!IGNORE_MODE.contains(mode)) {
			try {
				try (Statement statment = connection.createStatement()) {
					statment.execute("""
							CREATE TABLE DUAL AS
								SELECT * FROM (VALUES('X')) AS D(DUMMY)
							""");
					connection.commit();
				} catch (SQLException e) {
					connection.rollback();
					throw e;
				}
			} catch (SQLException e) {
				KagerowLogger.newAppLogger().err(e);
			}
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

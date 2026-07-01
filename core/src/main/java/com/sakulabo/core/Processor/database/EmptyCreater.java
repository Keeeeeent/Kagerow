package com.sakulabo.core.Processor.database;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * SQL空生成クラスです
 * 
 * @author keeeeeent
 */
public final class EmptyCreater extends AppKSQLCreater {

	/**
	 * デフォルトコンストラクタ
	 */
	public EmptyCreater() {
		super(null);
	}

	/** {@inheritDoc} */
	@Override
	public String toSql() {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public void init(Connection connection) throws SQLException {
	}

}

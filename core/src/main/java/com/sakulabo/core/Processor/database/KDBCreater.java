package com.sakulabo.core.Processor.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;

import com.sakulabo.core.Kagerow.Contents.KagerowVirtualFileContent.KagerowVirtualFileObject;

/**
 * KDB初期化処理クラスです
 * 
 * @author keeeeeent
 */
public final class KDBCreater extends AppKSQLCreater {

	/** スキーマ一覧 */
	private final Set<String> schemaSet = new HashSet<>();
	/** スキーマ作成（SQLフォーマット） */
	private static final String CREATE_SCHEMA = "CREATE SCHEMA IF NOT EXISTS {0};";

	/**
	 * デフォルトコンストラクタ
	 * 
	 * @param contentsSet ロードファイル一覧
	 */
	public KDBCreater(Set<? extends KagerowVirtualFileObject> contentsSet) {
		super(null);
		for (KagerowVirtualFileObject file : contentsSet)
			schemaSet.add(file.schema());
	}

	/** {@inheritDoc} */
	@Deprecated
	@Override
	public String toSql() {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public void init(Connection connection) throws SQLException {

		// コネクション設定
		connection.setAutoCommit(true);

		// DDL実行
		try (Statement statement = connection.createStatement()) {
			// マスタースキーマ作成
			statement.execute(MessageFormat.format(CREATE_SCHEMA, AppConnectionHandler.schemaName.toUpperCase()));
			for (String schemaName : schemaSet) {
				// スキーマ作成
				statement.execute(MessageFormat.format(CREATE_SCHEMA, schemaName.toUpperCase()));
			}
		} catch (SQLException e) {
			connection.rollback();
			throw e;
		}

	}

}

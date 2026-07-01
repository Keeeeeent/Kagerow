package com.sakulabo.core.Processor.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringJoiner;

import com.sakulabo.core.Kagerow.Contents.KagerowVirtualFileContent.KagerowVirtualFileObject;

/**
 * KVIEW初期化処理クラスです
 * 
 * @author keeeeeent
 */
public final class KViewCreater extends AppKSQLCreater {

	/** ロードファイル一覧*/
	private final Map<String, ? extends Set<? extends KagerowVirtualFileObject>> fileMap;
	/** VIEW生成（DDLフォーマット） */
	private static final String CREATE_VIEW = "CREATE OR REPLACE VIEW {0}.{1} AS {2};";
	/** VIEW生成（SELECTフォーマット） */
	private static final String SELECT = "SELECT * FROM {0}.{1}";
	/** VIEW生成（UNION句） */
	private static final String UNION = " UNION ALL ";
	/** カレントスキーマ */
	private final String schema;

	/**
	 * デフォルトコンストラクタ
	 * @param fileMap ロードファイル一覧
	 * @param schema カレントスキーマ
	 */
	public KViewCreater(Map<String, ? extends Set<? extends KagerowVirtualFileObject>> fileMap, String schema) {
		super(null);
		this.fileMap = fileMap;
		this.schema = schema;
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

		try (Statement statement = connection.createStatement()) {
			for (Entry<String, ? extends Set<? extends KagerowVirtualFileObject>> entry : fileMap.entrySet()) {
				// SELECT部作成
				String select = toSelect(entry.getValue());
				// DDL生成
				String ddl = MessageFormat.format(CREATE_VIEW, schema, entry.getKey(), select);
				// DDL実行
				statement.execute(ddl);
			}
		} catch (SQLException e) {
			connection.rollback();
			throw e;
		}

	}

	/**
	 * ファイル一覧をUNION付きのSELECT文に変換します
	 * @param fileSet ファイル一覧
	 * @return SELECT文
	 */
	private String toSelect(Set<? extends KagerowVirtualFileObject> fileSet) {
		// 文字列連結インスタンス生成
		StringJoiner joiner = new StringJoiner(UNION);
		for (KagerowVirtualFileObject file : fileSet) {
			// SELECT文生成
			String select = MessageFormat.format(SELECT, file.schema(), AppKSQLCreater.toTabelName(file.binaryName()));
			// UNION句追加
			joiner.add(select);
		}
		return joiner.toString();
	}

}

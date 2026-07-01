package com.sakulabo.core.Processor.database;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetProvider;

import com.sakulabo.core.Common.ErrorMessage;
import com.sakulabo.core.Kagerow.KagerowApplication;
import com.sakulabo.core.Kagerow.Exception.KSQLExecuteException;
import com.sakulabo.core.Kagerow.Utilities.KagerowConfiguration;
import com.sakulabo.core.Kagerow.Utilities.KagerowDBMode;

/**
 * KSQL専用SQL実行インスタンスです
 * 
 * @author keeeeeent
 */
public class QueryConnectionHandler extends AppConnectionHandler<List<CachedRowSet>> {

	/** SQL実行の最大フェッチサイズ */
	private static final int KDB_MAX_FECTH_SIZE;
	/** SQL実行の最大取得レコードサイズ */
	private static final int KDB_MAX_RECORD_SIZE;
	static {
		KagerowConfiguration config = KagerowApplication.getConfig();
		KDB_MAX_FECTH_SIZE = config.KDB_MAX_FECTH_SIZE();
		KDB_MAX_RECORD_SIZE = config.KDB_MAX_RECORD_SIZE();
	}

	/**
	 * デフォルトコンストラクタ
	 * @param mode KDB起動モード
	 * @param path KDB物理ファイルパス
	 * @throws SQLException コネクション構築失敗
	 */
	public QueryConnectionHandler(KagerowDBMode mode, Path path) throws SQLException {
		super(new QueryKDBUriCreater(mode, path), new EmptyCreater());
	}

	/** {@inheritDoc} */
	@Override
	@SafeVarargs
	public final List<CachedRowSet> transaction(String... bindData) throws SQLException {

		// 返却用リスト初期化
		List<CachedRowSet> result = new ArrayList<>();

		// コネクション生成
		Connection connection = conn.get();
		if (Objects.isNull(connection)) {
			throw new SQLException(ErrorMessage.CODE_009.getMessage());
		}

		// KDB接続
		try (connection) {
			// コネクションの設定変更
			// オートコミットON
			connection.setAutoCommit(true);
			// カレントスキーマ変更
			connection.setSchema(schemaName);
			for (String sqlText : bindData) {
				try (Statement statement = connection.createStatement()) {
					// 実行設定(フェッチサイズ)
					statement.setFetchSize(KDB_MAX_FECTH_SIZE);
					// 実行設定(最大行数)
					statement.setMaxRows(KDB_MAX_RECORD_SIZE);
					// SQL実行
					boolean mode = false;
					try {
						mode = statement.execute(sqlText);
					} catch (SQLException e) {
						createException(e);
					}
					if (mode) {
						// Queryの場合
						// 結果取得
						ResultSet resultSet = statement.getResultSet();
						// 結果格納
						RowSetFactory factory = RowSetProvider.newFactory();
						CachedRowSet crs = factory.createCachedRowSet();
						crs.populate(resultSet);
						result.add(crs);
					}
				}
			}
		}
		return result;

	}

	/**
	 * コネクションを生成
	 * @return コネクション
	 * @throws SQLException
	 */
	public Connection getConnection() throws SQLException {
		// コネクション生成
		Connection connection = conn.get();
		if (Objects.isNull(connection)) {
			throw new SQLException(ErrorMessage.CODE_009.getMessage());
		}
		return connection;
	}

	/**
	 * SQL実行時例外を例外翻訳します
	 * @param e SQL実行時例外
	 * @throws KSQLExecuteException 翻訳例外
	 */
	private void createException(SQLException e) throws KSQLExecuteException {
		// SQLState確認
		String sqlState = e.getSQLState();
		if (Objects.isNull(sqlState)) {
			// SQLStateが取得不可の場合、予期せぬ例外として再スロー
			throw new KSQLExecuteException(ErrorMessage.CODE_029.getMessage(), e);
		} else {
			// 取得できた場合、先頭2文字（カテゴリ）を取得
			sqlState = sqlState.substring(0, 2);
		}
		// SQLState毎に例外を翻訳
		String state = e.getSQLState();
		String msg = e.getMessage();
		switch (sqlState) {
		case "23":
			if (state.equals("23505")) {
				// UNIQUE違反 23505
				msg = ErrorMessage.CODE_033.getMessage(msg);
			} else if (state.equals("23502")) {
				// NOT NULL違反 23502
				msg = ErrorMessage.CODE_035.getMessage(msg);
			} else if (state.equals("23503")) {
				// FK違反 23503
				msg = ErrorMessage.CODE_034.getMessage(msg);
			}
			throw new KSQLExecuteException(e.getMessage(), e);
		case "22":
			if (state.equals("22001")) {
				// 文字列長超過 22001	
				msg = ErrorMessage.CODE_030.getMessage(msg);
			} else if (state.equals("22003")) {
				// 数値オーバー 22003
				msg = ErrorMessage.CODE_031.getMessage(msg);
			} else if (state.equals("22007")) {
				// 日付不正 22007
				msg = ErrorMessage.CODE_032.getMessage(msg);
			}
			throw new KSQLExecuteException(msg, e);
		case "42":
			// 構文エラー
			// エラーメッセージが長いため、原因となった部分飲みにする
			String[] cleaned = e.getMessage().split(";");
			if (cleaned.length < 1) {
				throw new KSQLExecuteException(e.getMessage(), e);
			} else {
				throw new KSQLExecuteException(cleaned[0], e);
			}
		default:
			// 予期せぬ例外の場合
			throw new KSQLExecuteException(ErrorMessage.CODE_029.getMessage(), e);
		}
	}

}

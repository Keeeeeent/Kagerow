package com.sakulabo.core.Processor.database;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

import com.sakulabo.core.Common.ErrorMessage;
import com.sakulabo.core.Kagerow.KagerowApplication;
import com.sakulabo.core.Kagerow.Contents.KagerowVirtualFileContent.KagerowVirtualFileObject;
import com.sakulabo.core.Kagerow.Utilities.KagerowDBMode;
import com.sakulabo.core.Processor.archive.DataSet;
import com.sakulabo.core.Processor.database.impl.H2DDLCreater;
import com.sakulabo.core.Processor.database.impl.MySQLDDLCreater;
import com.sakulabo.core.Processor.database.impl.OracleDDLCreater;
import com.sakulabo.core.Processor.database.impl.PostgreSQLDDLCreater;

/**
 * KDB専用DDL実行インスタンスです
 * 
 * @author keeeeeent
 */
public final class DDLConnectionHandler extends AppConnectionHandler<Void> {

	/**
	 * 自動的に選択されたDDL生成ハンドラーを構築します
	 * @param mode ロードモード
	 * @param dataSet データセット
	 * @param path 物理データパス
	 * @return ハンドラー
	 * @throws SQLException 不正モード指定時
	 */
	public final static DDLConnectionHandler autoSelectionDDLConnectionHandler(
			KagerowDBMode mode, DataSet dataSet, Path path) throws SQLException {
		return switch (mode) {
		case ORACLE -> oracleDDLConnectionHandler(dataSet, path);
		case H2 -> h2DDLConnectionHandler(dataSet, path);
		case MYSQL -> mysqlDDLConnectionHandler(dataSet, path);
		case POSTGRESQL -> postgresqlDDLConnectionHandler(dataSet, path);
		default -> throw new IllegalArgumentException("Unexpected value: " + mode);
		};
	}

	/**
	 * Oracle向けのDDL生成ハンドラーを構築します
	 * @param dataSet データセット
	 * @param path 物理データパス
	 * @return ハンドラー
	 * @throws SQLException コネクション構築失敗
	 */
	public final static DDLConnectionHandler oracleDDLConnectionHandler(DataSet dataSet, Path path)
			throws SQLException {
		AppKDBUriCreater uriCreater = new DefaultKDBUriCreater(KagerowDBMode.ORACLE, path);
		DDLCreater sqlCreater = new OracleDDLCreater(dataSet);
		DDLConnectionHandler handler = new DDLConnectionHandler(uriCreater, sqlCreater);
		return handler;
	}

	/**
	 * H2向けのDDL生成ハンドラーを構築します
	 * @param dataSet データセット
	 * @param path 物理データパス
	 * @return ハンドラー
	 * @throws SQLException コネクション構築失敗
	 */
	public final static DDLConnectionHandler h2DDLConnectionHandler(DataSet dataSet, Path path) throws SQLException {
		AppKDBUriCreater uriCreater = new DefaultKDBUriCreater(KagerowDBMode.H2, path);
		DDLCreater sqlCreater = new H2DDLCreater(dataSet);
		DDLConnectionHandler handler = new DDLConnectionHandler(uriCreater, sqlCreater);
		return handler;
	}

	/**
	 * MySQL向けのDDL生成ハンドラーを構築します
	 * @param dataSet データセット
	 * @param path 物理データパス
	 * @return ハンドラー
	 * @throws SQLException コネクション構築失敗 
	 */
	public final static DDLConnectionHandler mysqlDDLConnectionHandler(DataSet dataSet, Path path) throws SQLException {
		AppKDBUriCreater uriCreater = new DefaultKDBUriCreater(KagerowDBMode.MYSQL, path);
		DDLCreater sqlCreater = new MySQLDDLCreater(dataSet);
		DDLConnectionHandler handler = new DDLConnectionHandler(uriCreater, sqlCreater);
		return handler;
	}

	/**
	 * PostgreSQLDDLCreater向けのDDL生成ハンドラーを構築します
	 * @param dataSet データセット
	 * @param path 物理データパス
	 * @return ハンドラー
	 * @throws SQLException コネクション構築失敗 
	 */
	public final static DDLConnectionHandler postgresqlDDLConnectionHandler(DataSet dataSet, Path path)
			throws SQLException {
		AppKDBUriCreater uriCreater = new DefaultKDBUriCreater(KagerowDBMode.POSTGRESQL, path);
		DDLCreater sqlCreater = new PostgreSQLDDLCreater(dataSet);
		DDLConnectionHandler handler = new DDLConnectionHandler(uriCreater, sqlCreater);
		return handler;
	}

	/**
	 * 初期化DDL生成ハンドラーを構築します
	 * @param path 物理データバス
	 * @param mode データベースモード
	 * @param fileSet ロードコンテンツ
	 * @throws SQLException コネクション構築失敗
	 */
	public final static void initDDLConnectionHandler(
			Path path,
			KagerowDBMode mode,
			Set<? extends KagerowVirtualFileObject> fileSet) throws SQLException {
		// DB接続構築インスタンス生成
		AppKDBUriCreater uriCreater = new DefaultKDBUriCreater(mode, path);
		// DB初期化DDL構築インスタンス生成
		KDBCreater sqlCreater = new KDBCreater(fileSet);
		// コネクション生成
		try (DDLConnectionHandler handler = new DDLConnectionHandler(uriCreater)) {
			final Connection connection = handler.conn.get();
			if (Objects.isNull(connection)) {
				throw new SQLException(ErrorMessage.CODE_009.getMessage());
			}
			// KDB構築
			sqlCreater.init(connection);
		}
	}

	/**
	 * 完了作業DDL生成ハンドラーを構築します
	 * @param path 物理データバス
	 * @param mode データベースモード
	 * @param fileMap ロードコンテンツ
	 * @param schema カレントスキーマ
	 * @throws SQLException コネクション構築失敗
	 */
	public final static void finishDDLConnectionHandler(
			Path path,
			KagerowDBMode mode,
			Map<String, ? extends Set<? extends KagerowVirtualFileObject>> fileMap,
			String schema) throws SQLException {
		// KDB接続構築インスタンス生成
		AppKDBUriCreater uriCreater = new DefaultKDBUriCreater(mode, path);
		// KVIEW初期化DDL構築インスタンス生成
		KViewCreater sqlCreater = new KViewCreater(fileMap, schema);
		// コネクション生成
		try (DDLConnectionHandler handler = new DDLConnectionHandler(uriCreater)) {
			final Connection connection = handler.conn.get();
			if (Objects.isNull(connection)) {
				throw new SQLException(ErrorMessage.CODE_009.getMessage());
			}
			// KVIEW構築
			sqlCreater.init(connection);
		}
	}

	/**
	 * ファイルマップをファイルセットに変換します
	 * @param fileMap ファイルマップ
	 * @return ファイルセット
	 */
	public final static Set<? extends KagerowVirtualFileObject> toFileSet(
			Map<String, Set<? extends KagerowVirtualFileObject>> fileMap) {
		return fileMap.values()
				.stream()
				.flatMap(Set::stream)
				.collect(Collectors.toSet());
	}

	/**
	 * デフォルトコンストラクタ
	 * @param uriCreater URIクリエイター
	 * @param sqlCreater SQLクリエイター
	 * @throws SQLException KDB構築時、SQL関連例外
	 */
	DDLConnectionHandler(AppKDBUriCreater uriCreater, DDLCreater sqlCreater) throws SQLException {
		super(uriCreater, sqlCreater);
	}

	/**
	 * プライベートコンストラクタ
	 * @param uriCreater URIクリエイター
	 * @throws SQLException KDB構築時、SQL関連例外
	 */
	private DDLConnectionHandler(AppKDBUriCreater uriCreater) throws SQLException {
		super(uriCreater, new EmptyCreater());
	}

	/** {@inheritDoc} */
	@SafeVarargs
	@Override
	public final Void transaction(String... bindData) throws SQLException {

		// 引数チェック
		if (bindData.length != 0) {
			// 引数がある場合、使用されない旨警告出す
			KagerowApplication.getInstance().getLogger().log(Level.WARNING,
					ErrorMessage.CODE_008.getMessage(new Object[] { Arrays.toString(bindData) }),
					new Object[] {});
		}

		// SQL生成
		String ddl = sqlCreater.toSql();

		// コネクション生成
		final Connection connection = conn.get();
		if (Objects.isNull(connection)) {
			throw new SQLException(ErrorMessage.CODE_009.getMessage());
		}

		// スキーマ変更
		connection.setSchema(schemaName);

		// DDL実行
		try (Statement statement = connection.createStatement()) {
			// DDL実行
			statement.execute(ddl);
			connection.commit();
		} catch (SQLException e) {
			connection.rollback();
			throw e;
		} finally {
			connection.close();
		}

		return null;
	}

}

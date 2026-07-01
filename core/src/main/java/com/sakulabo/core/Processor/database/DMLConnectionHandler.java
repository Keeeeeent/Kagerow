package com.sakulabo.core.Processor.database;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import java.util.stream.IntStream;

import com.sakulabo.core.Common.ErrorMessage;
import com.sakulabo.core.Kagerow.KagerowApplication;
import com.sakulabo.core.Kagerow.Contents.KagerowVirtualFileContent.KagerowDataType;
import com.sakulabo.core.Kagerow.Utilities.KagerowDBMode;
import com.sakulabo.core.Processor.archive.DataSet;
import com.sakulabo.core.Processor.database.impl.DefaultDMLCreater;

/**
 * KDB専用DML実行インスタンスです
 * 
 * @author keeeeeent
 */
public class DMLConnectionHandler extends AppConnectionHandler<Integer> {

	/** statement */
	private PreparedStatement statement;
	/** DBコネクション */
	private Connection connection;
	/** DML文 */
	private String dml;
	/** 最大カーソル位置 */
	private static final int COUNT_MAX_SIZE;
	static {
		COUNT_MAX_SIZE = KagerowApplication.getConfig().KDB_BULK_INSERT_UNIT();
	}
	/** 現在カーソル位置 */
	private int count;

	/**
	 * 自動的に選択されたDML生成ハンドラーを構築します
	 * @param mode ロードモード
	 * @param dataSet データセット
	 * @param path 物理データパス
	 * @return ハンドラー
	 * @throws SQLException 不正モード指定時
	 */
	public final static DMLConnectionHandler autoSelectionDMLConnectionHandler(
			KagerowDBMode mode, DataSet dataSet, Path path) throws SQLException {
		return switch (mode) {
		case ORACLE -> oracleDMLConnectionHandler(dataSet, path);
		case H2 -> h2DMLConnectionHandler(dataSet, path);
		case MYSQL -> mysqlDMLConnectionHandler(dataSet, path);
		case POSTGRESQL -> postgresqlDMLConnectionHandler(dataSet, path);
		case ILLEGALITY -> throw new UnsupportedOperationException("Unimplemented case: " + mode);
		};
	}

	/**
	 * 汎用DDL生成ハンドラーを構築します
	 * @param mode DBモード
	 * @param dataSet データセット
	 * @param path 物理データパス
	 * @return ハンドラー
	 * @throws SQLException コネクション構築失敗
	 */
	public final static DMLConnectionHandler commonDMLConnectionHandler(KagerowDBMode mode, DataSet dataSet, Path path)
			throws SQLException {
		AppKDBUriCreater uriCreater = new DefaultKDBUriCreater(mode, path);
		DMLCreater sqlCreater = new DefaultDMLCreater(dataSet);
		DMLConnectionHandler handler = new DMLConnectionHandler(uriCreater, sqlCreater);
		return handler;
	}

	/**
	 * Oracle向けのDML生成ハンドラーを構築します
	 * @param dataSet データセット
	 * @param path 物理データパス
	 * @return ハンドラー
	 * @throws SQLException コネクション構築失敗
	 */
	public final static DMLConnectionHandler oracleDMLConnectionHandler(DataSet dataSet, Path path)
			throws SQLException {
		return commonDMLConnectionHandler(KagerowDBMode.ORACLE, dataSet, path);
	}

	/**
	 * H2向けのDML生成ハンドラーを構築します
	 * @param dataSet データセット
	 * @param path 物理データパス
	 * @return ハンドラー
	 * @throws SQLException コネクション構築失敗
	 */
	public final static DMLConnectionHandler h2DMLConnectionHandler(DataSet dataSet, Path path) throws SQLException {
		return commonDMLConnectionHandler(KagerowDBMode.H2, dataSet, path);
	}

	/**
	 * MySQL向けのDML生成ハンドラーを構築します
	 * @param dataSet データセット
	 * @param path 物理データパス
	 * @return ハンドラー
	 * @throws SQLException コネクション構築失敗
	 */
	public final static DMLConnectionHandler mysqlDMLConnectionHandler(DataSet dataSet, Path path) throws SQLException {
		return commonDMLConnectionHandler(KagerowDBMode.MYSQL, dataSet, path);
	}

	/**
	 * PostgreSQLDMLCreater向けのDDL生成ハンドラーを構築します
	 * @param dataSet データセット
	 * @param path 物理データパス
	 * @return ハンドラー
	 * @throws SQLException コネクション構築失敗
	 */
	public final static DMLConnectionHandler postgresqlDMLConnectionHandler(DataSet dataSet, Path path)
			throws SQLException {
		return commonDMLConnectionHandler(KagerowDBMode.POSTGRESQL, dataSet, path);
	}

	/**
	 * デフォルトコンストラクタ
	 * @param uriCreater URIクリエイター
	 * @param sqlCreater SQLクリエイター
	 * @throws SQLException @throws SQLException KDB構築時、SQL関連例外
	 */
	DMLConnectionHandler(AppKDBUriCreater uriCreater, DMLCreater sqlCreater) throws SQLException {

		super(uriCreater, sqlCreater);

		// コネクション生成
		connection = conn.get();
		if (Objects.isNull(connection)) {
			throw new SQLException(ErrorMessage.CODE_009.getMessage());
		}

		// SQL生成
		dml = sqlCreater.toSql();

		// スキーマ変更
		connection.setSchema(schemaName);

		// コネクション初期化
		try (Statement _ = connection.createStatement()) {
			connection.commit();
		}

		// Statement生成
		statement = connection.prepareStatement(dml);

	}

	/** {@inheritDoc} */
	@Override
	public Integer transaction(String... bindData) throws SQLException {

		// 引数チェック
		if (bindData.length != sqlCreater.dataSet.dataType().length) {
			throw new SQLException(ErrorMessage.CODE_007.getMessage());
		}

		// 返却変数初期化
		int result = 0;

		// DML実行
		try {

			// カラムリスト取得
			KagerowDataType[] columnList = sqlCreater.dataSet.dataType();
			// SQLクリエイターダウンキャスト
			DMLCreater sqlCreater = (DMLCreater) super.sqlCreater;

			// DML更新
			for (int i = 0; i < bindData.length; i++) {
				KagerowDataType type = columnList[i];
				String data = bindData[i];
				switch (type) {
				case BOOLEAN -> sqlCreater.isBOOLEAN(statement, data, i + 1);
				case DATE -> sqlCreater.isDATE(statement, data, i + 1);
				case DECIMAL -> sqlCreater.isDECIMAL(statement, data, i + 1);
				case NULL -> sqlCreater.isNULL(statement, data, i + 1);
				case NUMBER -> sqlCreater.isNUMBER(statement, data, i + 1);
				case VARCHAR -> sqlCreater.isVARCHAR(statement, data, i + 1);
				case TIMESTAMP -> sqlCreater.isTIMESTAMP(statement, data, i + 1);
				}
				;
			}

			// SQL追加
			statement.addBatch();
			if (COUNT_MAX_SIZE <= count) {
				result = flash();
			}

		} catch (SQLException e) {
			connection.rollback();
			throw e;
		}

		return Integer.valueOf(result);
	}

	/**
	 * バッファリングされているトランザクションを確定します
	 * @return 更新件数
	 * @throws SQLException
	 */
	public int flash() throws SQLException {
		count = 0;
		int[] result = statement.executeBatch();
		connection.commit();
		statement.clearBatch();
		return IntStream.of(result).sum();
	}

	/** {@inheritDoc} */
	@Override
	public void close() throws SQLException {
		flash();
		statement.close();
		super.close();
	}

}

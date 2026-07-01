package com.sakulabo.core.Processor.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;

import com.sakulabo.core.Kagerow.KagerowApplication;

/**
 * KDB操作インスタンスの規定クラスです
 * 
 * @author keeeeeent
 * @param <T> 返却する際のクエリ結果の型情報
 */
public abstract class AppConnectionHandler<T> implements AutoCloseable {

	/** コネクションプール */
	final ThreadLocal<Connection> conn;
	/** URIクリエイター */
	final AppKDBUriCreater uriCreater;
	/** SQLクリエイター */
	final AppKSQLCreater sqlCreater;
	/** Kagerowスキーマ名称 */
	public final static String schemaName = "KAGEROW";

	/**
	 * スーパーコンストラクタ
	 * @param uriCreater JDBC向けURL生成クラス
	 * @param sqlCreater SQL生成クラス
	 * @throws SQLException KDB構築時、SQL関連例外
	 */
	AppConnectionHandler(AppKDBUriCreater uriCreater, AppKSQLCreater sqlCreater) throws SQLException {
		this.uriCreater = uriCreater;
		this.sqlCreater = sqlCreater;
		conn = ThreadLocal.withInitial(this::createConnections);
		this.sqlCreater.init(conn.get());
	}

	/**
	 * コネクションファクトリメソッド
	 * @return コネクション
	 */
	private final Connection createConnections() {
		Connection con = null;
		try {
			con = DriverManager.getConnection(uriCreater.toUri());
			con.setAutoCommit(false);
		} catch (SQLException e) {
			KagerowApplication.getInstance().getLogger().err(e);
		}
		return con;
	}

	/**
	 * SQL実行トランザクションメソッド
	 * @param bindData SQLへバインドするデータ
	 * @return SQL実行結果
	 * @throws SQLException KDBクエリエラー
	 */
	abstract T transaction(String... bindData) throws SQLException;

	/** {@inheritDoc}  */
	@Override
	public void close() throws SQLException {
		Connection con = conn.get();
		if (Objects.nonNull(con) && !con.isClosed()) {
			con.close();
		}
		conn.remove();
	}

}

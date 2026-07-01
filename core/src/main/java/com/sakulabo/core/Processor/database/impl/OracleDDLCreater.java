package com.sakulabo.core.Processor.database.impl;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.HexFormat;

import com.sakulabo.core.Common.StringUtils;
import com.sakulabo.core.Kagerow.KagerowApplication;
import com.sakulabo.core.Processor.archive.DataSet;
import com.sakulabo.core.Processor.database.AppConnectionHandler;
import com.sakulabo.core.Processor.database.DDLCreater;

/**
 * Oracleモード向けのDDLを生成します
 * 
 * @author keeeeeent
 */
public final class OracleDDLCreater extends DDLCreater {

	/** シーケンス名称 */
	private String seqName = "SEQ";

	/**
	 * デフォルトコンストラクタ
	 * @param dataSet データ構造体
	 */
	public OracleDDLCreater(DataSet dataSet) {
		super(dataSet);
	}

	/** {@inheritDoc} */
	@Override
	public String isPK() {
		String colName = "KDB_PK_NO";
		String definition = MessageFormat.format(" NUMBER DEFAULT {0}.{1}.NEXTVAL PRIMARY KEY",
				dataSet.schema(), seqName);
		return colName.concat(definition);
	}

	/** {@inheritDoc} */
	@Override
	public String isNULL() {
		String colName = dataSet.columnList()[index++];
		String definition = " CHAR(1) DEFAULT NULL";
		return colName.concat(definition);
	}

	/** {@inheritDoc} */
	@Override
	public String isBOOLEAN() {
		String colName = dataSet.columnList()[index++];
		String definition = " NUMBER(1) DEFAULT 0";
		return colName.concat(definition);
	}

	/** {@inheritDoc} */
	@Override
	public String isNUMBER() {
		String colName = dataSet.columnList()[index++];
		String definition = " NUMBER";
		return colName.concat(definition);
	}

	/** {@inheritDoc} */
	@Override
	public String isDECIMAL() {
		String colName = dataSet.columnList()[index++];
		String definition = " NUMBER";
		return colName.concat(definition);
	}

	/** {@inheritDoc} */
	@Override
	public String isDATE() {
		String colName = dataSet.columnList()[index++];
		String definition = " DATE DEFAULT SYSDATE";
		return colName.concat(definition);
	}

	/** {@inheritDoc} */
	@Override
	public String isTIMESTAMP() {
		String colName = dataSet.columnList()[index++];
		String definition = " TIMESTAMP DEFAULT LOCALTIMESTAMP";
		return colName.concat(definition);
	}

	/** {@inheritDoc} */
	@Override
	public String isVARCHAR() {
		int idx = index++;
		String colName = dataSet.columnList()[idx];
		long size = dataSet.dataSize()[idx];
		String definition;
		if (500 < size) {
			definition = " CLOB";
		} else {
			definition = " VARCHAR2(%d)".formatted(size);
		}
		return colName.concat(definition);
	}

	/** {@inheritDoc} */
	@Override
	public void init(Connection connection) throws SQLException {

		// シーケンス作成
		String seqDdl = """
				CREATE SEQUENCE IF NOT EXISTS {0}.{1} \
				INCREMENT BY 1 \
				START WITH 0 \
				NOMAXVALUE \
				NOMINVALUE \
				NOCYCLE \
				CACHE 1\
				;""";
		try {
			// ハッシュ値取得
			MessageDigest digest = MessageDigest.getInstance(StringUtils.MD5);
			digest.update(dataSet.table().concat("seq").getBytes());
			// シーケンス名称設定
			this.seqName += HexFormat.of().formatHex(digest.digest());
			// DDL生成（シーケンス）
			seqDdl = MessageFormat.format(seqDdl, this.dataSet.schema(), this.seqName);
		} catch (NoSuchAlgorithmException e) {
			// ここではシステム内部で固定したアルゴリズム遠使用するため例外は発生しない
			KagerowApplication.getInstance().getLogger().err(e);
		}

		// スキーマ切り替え
		connection.setSchema(AppConnectionHandler.schemaName);
		// DDL実行
		try (Statement statement = connection.createStatement()) {
			// シーケンス生成
			statement.execute(seqDdl);
			connection.commit();
		} catch (SQLException e) {
			connection.rollback();
			throw e;
		}

	}

}

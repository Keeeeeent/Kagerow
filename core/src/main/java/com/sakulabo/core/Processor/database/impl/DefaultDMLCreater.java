package com.sakulabo.core.Processor.database.impl;

import java.math.BigDecimal;
import java.sql.JDBCType;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.sakulabo.core.Processor.archive.DataSet;
import com.sakulabo.core.Processor.database.DMLCreater;

/**
 * 以下モード向けののDMLを生成します
 * [対応モード]
 * ・Oracl
 * ・MySQL
 * ・H2
 * ・PostgreSQL
 * 
 * @author keeeeeent
 */
public final class DefaultDMLCreater extends DMLCreater {

	/**
	 * デフォルトコンストラクタ
	 * @param dataSet データ構造体
	 */
	public DefaultDMLCreater(DataSet dataSet) {
		super(dataSet);
	}

	/** {@inheritDoc} */
	@Override
	public void isNULL(PreparedStatement statement, String data, int index) throws SQLException {
		statement.setObject(index, JDBCType.NULL.getVendorTypeNumber());
	}

	/** {@inheritDoc} */
	@Override
	public void isBOOLEAN(PreparedStatement statement, String data, int index) throws SQLException {
		if (nullCheck(data)) {
			statement.setNull(index, Types.BOOLEAN);
			return;
		}
		Boolean bool = data.equals("1") || data.equalsIgnoreCase("true");
		statement.setObject(index, bool, JDBCType.BOOLEAN);
	}

	/** {@inheritDoc} */
	@Override
	public void isNUMBER(PreparedStatement statement, String data, int index) throws SQLException {
		if (nullCheck(data)) {
			statement.setNull(index, Types.NUMERIC);
			return;
		}
		statement.setObject(index, Long.parseLong(data), JDBCType.NUMERIC);
	}

	/** {@inheritDoc} */
	@Override
	public void isDECIMAL(PreparedStatement statement, String data, int index) throws SQLException {
		if (nullCheck(data)) {
			statement.setNull(index, Types.DECIMAL);
			return;
		}
		statement.setObject(index, new BigDecimal(data), JDBCType.DECIMAL);
	}

	/** {@inheritDoc} */
	@Override
	public void isDATE(PreparedStatement statement, String data, int index) throws SQLException {
		if (nullCheck(data)) {
			statement.setNull(index, Types.DATE);
			return;
		}
		LocalDate date = toLocalDate(data);
		statement.setObject(index, date, JDBCType.DATE);
	}

	/** {@inheritDoc} */
	@Override
	public void isTIMESTAMP(PreparedStatement statement, String data, int index) throws SQLException {
		if (nullCheck(data)) {
			statement.setNull(index, Types.TIMESTAMP);
			return;
		}
		LocalDateTime timestamp = toLocalDateTime(data);
		statement.setObject(index, timestamp, JDBCType.TIMESTAMP);
	}

	/** {@inheritDoc} */
	@Override
	public void isVARCHAR(PreparedStatement statement, String data, int index) throws SQLException {
		int sizeIndex = index - 1;
		long size = dataSet.dataSize()[sizeIndex];
		if (isCLOB(size)) {
			if (nullCheck(data)) {
				statement.setNull(index, Types.CLOB);
				return;
			}
			statement.setObject(index, data, JDBCType.CLOB);
		} else {
			if (nullCheck(data)) {
				statement.setNull(index, Types.VARCHAR);
				return;
			}
			statement.setObject(index, data, JDBCType.VARCHAR);
		}
	}

	/** {@inheritDoc} */
	@Override
	public boolean isCLOB(long size) {
		return 500 < size;
	}

}

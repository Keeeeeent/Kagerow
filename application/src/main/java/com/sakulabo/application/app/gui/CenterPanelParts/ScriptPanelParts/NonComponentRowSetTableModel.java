package com.sakulabo.application.app.gui.CenterPanelParts.ScriptPanelParts;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import javax.sql.rowset.CachedRowSet;
import javax.swing.table.AbstractTableModel;

/**
 * KSQL実行結果セットテーブルモデルクラスです
 * 
 * @author keeeeeent
 */
public class NonComponentRowSetTableModel extends AbstractTableModel {

	/** 実行結果 */
	private final CachedRowSet rowSet;
	/** 実行結果メタデータ */
	private ResultSetMetaData metaData;
	/** 行数 */
	private int rowCount;

	/**
	 * デフォルトコンストラクタ
	 * @param rowSet 結果セット
	 * @throws SQLException SQLエラー
	 */
	public NonComponentRowSetTableModel(CachedRowSet rowSet) throws SQLException {
		this.rowSet = rowSet;
		this.metaData = rowSet.getMetaData();
		this.rowSet.beforeFirst();
		while (rowSet.next())
			rowCount++;
		this.rowSet.beforeFirst();
	}

	/** {@inheritDoc} */
	@Override
	public int getRowCount() {
		return rowCount;
	}

	/** {@inheritDoc} */
	@Override
	public int getColumnCount() throws IllegalStateException {
		try {
			return metaData.getColumnCount();
		} catch (SQLException e) {
			throw new IllegalStateException(e);
		}
	}

	/** {@inheritDoc} */
	@Override
	public String getColumnName(int column) {
		try {
			return metaData.getColumnLabel(column + 1);
		} catch (SQLException e) {
			return "?";
		}
	}

	/** {@inheritDoc} */
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		try {
			rowSet.absolute(rowIndex + 1);
			return rowSet.getObject(columnIndex + 1);
		} catch (SQLException e) {
			return null;
		}
	}

	/** {@inheritDoc} */
	@Override
	public boolean isCellEditable(int row, int column) {
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public void setValueAt(Object value, int row, int column) {
		try {
			rowSet.absolute(row + 1);
			rowSet.updateObject(column + 1, value);
			rowSet.updateRow();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
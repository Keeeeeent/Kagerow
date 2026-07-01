package com.sakulabo.application.app.gui.CenterPanelParts.ScriptPanelParts;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.swing.table.AbstractTableModel;

/**
 * KagerowScriptテーブルモデルクラスです
 * 
 * @author keeeeeent
 */
public abstract class NonComponentMapTableModel extends AbstractTableModel {

	/** マップアクセスキー */
	protected final List<String> keys;
	/** データ */
	protected final Map<String, String> map;

	/**
	 * デフォルトコンストラクタ
	 * @param map データ
	 */
	public NonComponentMapTableModel(Map<String, String> map) {
		this.map = map;
		this.keys = new ArrayList<>(map.keySet());
		this.keys.sort(Comparator.naturalOrder());
	}

	/** {@inheritDoc} */
	@Override
	public int getRowCount() {
		return keys.size();
	}

	/** {@inheritDoc} */
	@Override
	public int getColumnCount() {
		return 2;
	}

	/** {@inheritDoc} */
	@Override
	public Object getValueAt(int row, int col) {
		String key = keys.get(row);
		return col == 0 ? key : map.get(key);
	}

	/** {@inheritDoc} */
	@Override
	public boolean isCellEditable(int row, int col) {
		return col == 1;
	}

	/** {@inheritDoc} */
	@Override
	public void setValueAt(Object value, int row, int col) {
		if (col == 1) {
			map.put(keys.get(row), Objects.toString(value));
			fireTableCellUpdated(row, col);
			refresh();
		}
	}

	/** {@inheritDoc} */
	@Override
	public String getColumnName(int col) {
		return col == 0 ? "Key" : "Value";
	}

	/**
	 * データリフレッシュリスナー
	 */
	protected abstract void refresh();

}

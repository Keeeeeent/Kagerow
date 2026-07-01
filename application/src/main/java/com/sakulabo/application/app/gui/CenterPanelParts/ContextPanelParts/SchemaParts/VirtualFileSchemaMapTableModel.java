package com.sakulabo.application.app.gui.CenterPanelParts.ContextPanelParts.SchemaParts;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

import com.sakulabo.application.common.code.GUIText;

/**
 * Kagerowテーブルスキーマ情報表示用モデルクラスです
 * 
 * @author keeeeeent
 */
public class VirtualFileSchemaMapTableModel extends AbstractTableModel {

	/** マップアクセスキー */
	private final List<String> keys;
	/** データ */
	protected final Map<String, String> map;
	/** 項目名（画面表示用） */
	private static final String KEY = GUIText.VirtualFileSchemaMapTableModel_001.toString();
	/** 情報（画面表示用） */
	private static final String VALUE = GUIText.VirtualFileSchemaMapTableModel_002.toString();

	/**
	 * デフォルトコンストラクタ
	 * @param map データ
	 */
	public VirtualFileSchemaMapTableModel(Map<String, String> map) {
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
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public void setValueAt(Object value, int row, int col) {
		;
	}

	/** {@inheritDoc} */
	@Override
	public String getColumnName(int col) {
		return col == 0 ? KEY : VALUE;
	}

}

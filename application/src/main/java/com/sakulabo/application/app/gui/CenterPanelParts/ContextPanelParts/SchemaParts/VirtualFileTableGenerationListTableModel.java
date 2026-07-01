package com.sakulabo.application.app.gui.CenterPanelParts.ContextPanelParts.SchemaParts;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import com.sakulabo.application.common.code.GUIText;

/**
 * Kagerowテーブル世代表示用モデルクラスです
 * 
 * @author keeeeeent
 */
public class VirtualFileTableGenerationListTableModel extends AbstractTableModel {

	/** データ */
	protected final List<String[]> list;
	/** ヘッダー */
	protected final List<String> headerList = List.of(
			GUIText.VirtualFileTableGenerationListTableModel_001.toString(),
			GUIText.VirtualFileTableGenerationListTableModel_002.toString(),
			GUIText.VirtualFileTableGenerationListTableModel_003.toString(),
			GUIText.VirtualFileTableGenerationListTableModel_004.toString(),
			GUIText.VirtualFileTableGenerationListTableModel_005.toString());

	/**
	 * デフォルトコンストラクタ
	 * @param list データ
	 */
	public VirtualFileTableGenerationListTableModel(List<String[]> list) {
		this.list = list;
	}

	/** {@inheritDoc} */
	@Override
	public int getRowCount() {
		return this.list.size();
	}

	/** {@inheritDoc} */
	@Override
	public int getColumnCount() {
		return this.list.getFirst().length;
	}

	/** {@inheritDoc} */
	@Override
	public Object getValueAt(int row, int col) {
		String[] data = this.list.get(row);
		return data[col];
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
		return headerList.get(col);
	}

}

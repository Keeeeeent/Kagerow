package com.sakulabo.application.app.gui.CenterPanelParts.ContextPanelParts.PluginParts;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import com.sakulabo.application.common.code.GUIText;
import com.sakulabo.core.Kagerow.Contents.KagerowPluginContent.PluginParamInfo;

/**
 * Kagerowプラグインパラメータ情報表示用モデルクラスです
 * 
 * @author keeeeeent
 */
public class PluginParamInfoListTableModel extends AbstractTableModel {

	/** データ */
	protected final List<PluginParamInfo> list;
	/** 項目名（画面表示用） */
	private static final String NAME = GUIText.PluginParamInfoListTableModel_001.toString();
	/** デフォルト値（画面表示用） */
	private static final String DEFAULT_VALUE = GUIText.PluginParamInfoListTableModel_002.toString();
	/** 必須フラグ（画面表示用） */
	private static final String REQUIRED = GUIText.PluginParamInfoListTableModel_003.toString();

	/**
	 * デフォルトコンストラクタ
	 * @param list データ
	 */
	public PluginParamInfoListTableModel(List<PluginParamInfo> list) {
		this.list = list;
	}

	/** {@inheritDoc} */
	@Override
	public int getRowCount() {
		return list.size();
	}

	/** {@inheritDoc} */
	@Override
	public int getColumnCount() {
		return 3;
	}

	/** {@inheritDoc} */
	@Override
	public Object getValueAt(int row, int col) {
		return switch (col) {
		case 0 -> list.get(row).name();
		case 1 -> list.get(row).defaultValue();
		case 2 -> list.get(row).required();
		default -> throw new IllegalArgumentException("Unexpected value: " + col);
		};
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
		return switch (col) {
		case 0 -> NAME;
		case 1 -> DEFAULT_VALUE;
		case 2 -> REQUIRED;
		default -> throw new IllegalArgumentException("Unexpected value: " + col);
		};
	}

}

package com.sakulabo.application.app.gui.CenterPanelParts.ScriptPanelParts;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.InputMethodEvent;
import java.awt.event.InputMethodListener;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.sakulabo.application.common.mixin.JLabelMixin;
import com.sakulabo.application.common.mixin.JPanelMixin;
import com.sakulabo.application.common.mixin.JSplitPanelMixin;
import com.sakulabo.application.common.mixin.JTextComponentMixin;
import com.sakulabo.application.helper.KagerowScriptHelper;
import com.sakulabo.core.Kagerow.Utilities.KagerowScriptAccessor;
import com.sakulabo.core.Kagerow.Utilities.KagerowScriptAccessor.KagerowPluginAccessor;
import com.sakulabo.core.Kagerow.Utilities.KagerowUtilities;

/**
 * GUIアプリケーションのPlugin個別編集パネルクラスです
 * 
 * @author keeeeeent
 */
@JPanelMixin.Setting(layout = JPanelMixin.Layout.BorderLayout, backgroudColor = 0xffffff)
@JSplitPanelMixin.Setting(orientation = JSplitPane.VERTICAL_SPLIT)
public class NonComponentPluginPopupEditPanel extends JPanel
		implements JTextComponentMixin, JSplitPanelMixin, JLabelMixin, JPanelMixin, Consumer<KagerowScriptAccessor> {

	/** スクリプトヘルパー */
	private final KagerowScriptHelper scriptHelper;
	{
		scriptHelper = KagerowUtilities.getBean(KagerowScriptHelper.class, null).get();
	}

	/** プラグインアクセッサー */
	private KagerowPluginAccessor kagerowPluginAccessor;
	/** スクリプトアクセッサー */
	private volatile KagerowScriptAccessor kagerowScriptAccessor;

	/** 親コンポーネント */
	private final NonComponentScriptEditPanel parent;

	/** 設定値変更テキストエラリア */
	@JTextComponentMixin.Setting
	private final JTextPane edit = new JTextPane();

	// ####################################################################################
	// # 共通内部クラス定義
	// ####################################################################################

	/**
	 * パラメータデータ更新リスナー
	 */
	private class tableModel extends NonComponentMapTableModel implements DocumentListener, InputMethodListener {

		/** 編集区画 */
		private final JTextPane jTextPane;
		/** テーブルキー */
		@SuppressWarnings("javadoc")
		private volatile int row, col;
		/** 入力確定フラグ */
		private final AtomicBoolean commited = new AtomicBoolean();

		/**
		 * デフォルトコンストラクタ
		 * @param map データ
		 * @param jTextPane 編集区画
		 */
		public tableModel(Map<String, String> map, JTextPane jTextPane) {
			super(map);
			this.jTextPane = jTextPane;
			this.jTextPane.getDocument().addDocumentListener(this);
			this.jTextPane.addInputMethodListener(this);
		}

		/** {@inheritDoc} */
		@Override
		public boolean isCellEditable(int row, int col) {
			String key = super.keys.get(row);
			String value = super.map.get(key);
			this.row = row;
			this.col = col;
			jTextPane.setText(value);
			// Undoリセット
			resetUndo();
			return false;
		}

		/** {@inheritDoc} */
		@Override
		protected void refresh() {
			// クローン生成
			KagerowPluginAccessor newPlugin = NonComponentPluginPopupEditPanel.this.kagerowPluginAccessor
					.setParam(this.map);
			List<KagerowPluginAccessor> inputList = scriptHelper.setPluginList(
					NonComponentPluginPopupEditPanel.this.kagerowScriptAccessor.getRawInputPlugins(), newPlugin);
			List<KagerowPluginAccessor> outputList = scriptHelper.setPluginList(
					NonComponentPluginPopupEditPanel.this.kagerowScriptAccessor.getRawOutputPlugins(), newPlugin);
			// 設定変更
			KagerowScriptAccessor newAccessor = NonComponentPluginPopupEditPanel.this.kagerowScriptAccessor
					.setRawInputPlugins(inputList);
			newAccessor = newAccessor.setRawOutputPlugins(outputList);
			// 変更通知
			parent.refresh(newAccessor);
		}

		/** {@inheritDoc} */
		@Override
		public void inputMethodTextChanged(InputMethodEvent event) {
			commited.set(event.getCommittedCharacterCount() == 0);
		}

		/** {@inheritDoc} */
		@Override
		public void caretPositionChanged(InputMethodEvent event) {
			;
		}

		/** {@inheritDoc} */
		@Override
		public void insertUpdate(DocumentEvent e) {
			this.updateData();
		}

		/** {@inheritDoc} */
		@Override
		public void removeUpdate(DocumentEvent e) {
			this.updateData();
		}

		/** {@inheritDoc} */
		@Override
		public void changedUpdate(DocumentEvent e) {
			this.updateData();
		}

		/**
		 * データ更新処理
		 */
		private void updateData() {
			// 入力イベントが確定していない場合処理を一時停止
			if (commited.get()) {
				return;
			}
			super.setValueAt(this.jTextPane.getText(), this.row, this.col);
		}

	}

	// ####################################################################################
	// # 共通内部クラス定義(パラメータ領域)
	// ####################################################################################

	/** 変数格納テーブル */
	protected JTable paramTable = new JTable();

	/**
	 * デフォルトコンストラクタ
	 * @param kagerowScriptAccessor スクリプトアクセッサー
	 * @param kagerowPluginAccessor プラグインアクセッサー
	 * @param parent 親コンポーネント
	 */
	public NonComponentPluginPopupEditPanel(
			KagerowScriptAccessor kagerowScriptAccessor,
			KagerowPluginAccessor kagerowPluginAccessor,
			NonComponentScriptEditPanel parent) {

		// フィールド初期化
		this.kagerowPluginAccessor = kagerowPluginAccessor;
		this.parent = parent;
		this.kagerowScriptAccessor = kagerowScriptAccessor;

		// パネル初期化
		setJPanel(this);

		// ボーダ設定
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		edit.setBorder(
				BorderFactory.createCompoundBorder(
						BorderFactory.createLineBorder(Color.GRAY, 1),
						BorderFactory.createEmptyBorder(8, 8, 8, 8)));

		// 分割区画生成
		JSplitPane jSplitPane = createSplitPane();
		// 初期の位置を指定（50%）
		jSplitPane.setResizeWeight(0.5);
		jSplitPane.setDividerLocation(0.5);

		// レイアウト設定（上書き）
		add(jSplitPane, BorderLayout.CENTER);

		// テーブル初期値を指定
		{
			paramTable.setModel(new tableModel(this.kagerowPluginAccessor.getParam(), edit));
			// それぞれのカラムにおける幅を調整
			paramTable.getColumnModel().getColumn(0).setPreferredWidth(150);
			paramTable.getColumnModel().getColumn(1).setPreferredWidth(300);
			// テーブルヘッダー配置
			add(paramTable.getTableHeader(), BorderLayout.NORTH);
			// テーブル罫線設定
			paramTable.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
			paramTable.getTableHeader().setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
		}

		// Undoリスナー設定
		setUndo();
		// ショートカット設定
		applyMacKeyBindings();

	}

	/** {@inheritDoc} */
	@Override
	public Component getLeftComponent() {
		return paramTable;
	}

	/** {@inheritDoc} */
	@Override
	public Component getRightComponent() {
		return edit;
	}

	/** {@inheritDoc} */
	@Override
	public void accept(KagerowScriptAccessor t) {
		this.kagerowScriptAccessor = t;
		this.kagerowPluginAccessor = this.kagerowPluginAccessor.search(t);
	}

	/** {@inheritDoc} */
	@Override
	public void refreshUndo() throws Exception {
	}

}

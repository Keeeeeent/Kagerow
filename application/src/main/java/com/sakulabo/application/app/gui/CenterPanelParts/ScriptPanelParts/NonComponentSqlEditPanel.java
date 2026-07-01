package com.sakulabo.application.app.gui.CenterPanelParts.ScriptPanelParts;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import com.sakulabo.application.common.code.GUIText;
import com.sakulabo.application.common.mixin.ActionListenerMixin;
import com.sakulabo.application.common.mixin.JButtonMixin;
import com.sakulabo.application.common.mixin.JLabelMixin;
import com.sakulabo.application.common.mixin.JPanelMixin;
import com.sakulabo.application.helper.DialogHelper;
import com.sakulabo.application.helper.KagerowScriptHelper;
import com.sakulabo.core.Kagerow.Utilities.KagerowExecutionPlanAccessor;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;
import com.sakulabo.core.Kagerow.Utilities.KagerowScriptAccessor;
import com.sakulabo.core.Kagerow.Utilities.KagerowScriptAccessor.KagerowSqlAccessor;
import com.sakulabo.core.Kagerow.Utilities.KagerowUtilities;

/**
 * GUIアプリケーションのスクリプト編集パネル(SQL宣言部)クラスです
 * 
 * @author keeeeeent
 */
@JPanelMixin.Setting(layout = JPanelMixin.Layout.BorderLayout, backgroudColor = 0xffffff)
public class NonComponentSqlEditPanel extends JPanel
		implements JLabelMixin, JPanelMixin, JButtonMixin, Consumer<KagerowScriptAccessor> {

	// ####################################################################################
	// # 共通定数
	// ####################################################################################

	/** リフレッシュフラグ */
	private final AtomicBoolean refreshFlag = new AtomicBoolean();
	/** 親コンポーネント */
	private final NonComponentScriptEditPanel parent;
	/** KDBセッション */
	private volatile KagerowExecutionPlanAccessor session;

	/** スクリプトヘルパー */
	private final KagerowScriptHelper scriptHelper;
	{
		scriptHelper = KagerowUtilities.getBean(KagerowScriptHelper.class, null).get();
	}
	/** ダイアログヘルパー */
	private final DialogHelper dialogHelper;
	{
		dialogHelper = KagerowUtilities.getBean(DialogHelper.class, null).get();
	}

	/** スクリプトアクセッサー */
	private volatile KagerowScriptAccessor kagerowScriptAccessor;
	/** ポップアップインスタンス */
	private volatile NonComponentKsqlPopupEditPanel panel;

	/** アクションコマンド（追加） */
	private static final String COMMON_ADD_CMD = "addBtn";
	/** アクションコマンド（削除） */
	private static final String COMMON_DEL_CMD = "delBtn";

	// ####################################################################################
	// # 共通内部クラス定義(KSQL一覧領域)
	// ####################################################################################

	/** 削除ボタン */
	@JButtonMixin.Setting(title = GUIText.ScriptPanel_002, actionCommand = COMMON_DEL_CMD)
	protected JButton delBtn = new JButton();
	/** 追加ボタン */
	@JButtonMixin.Setting(title = GUIText.ScriptPanel_001, actionCommand = COMMON_ADD_CMD)
	protected JButton addBtn = new JButton();

	/** ボタンパネル */
	protected JPanel buttonJpanel = new JPanel();
	{
		// ボタン設定
		try {
			setJButton(this, delBtn);
			setJButton(this, addBtn);
		} catch (IllegalAccessException | NoSuchMethodException e) {
			KagerowLogger.newAppLogger().err(e);
		}
		// レイアウト設定
		buttonJpanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		buttonJpanel.setBackground(Color.WHITE);
		// コンポーネント配置
		buttonJpanel.add(delBtn);
		buttonJpanel.add(Box.createHorizontalStrut(20));
		buttonJpanel.add(addBtn);
	}

	/** KSQL一覧テーブル */
	protected JTable ksqlTable = new JTable();
	{
		// テーブルの横スクロール設定
		ksqlTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
	}

	/**
	 * KSQL一覧パネル
	 */
	protected JPanel ksqlJPanel = new JPanel();
	{
		// レイアウト設定
		setJPanel(ksqlJPanel);
		// コンポーネント配置
		ksqlJPanel.add(ksqlTable, BorderLayout.CENTER);
		ksqlJPanel.add(buttonJpanel, BorderLayout.SOUTH);
	}

	/**
	 * デフォルトコンストラクタ
	 * @param kagerowScriptAccessor スクリプトアクセッサー
	 * @param parent 親コンポーネント
	 * @param session KDBセッション
	 */
	NonComponentSqlEditPanel(KagerowScriptAccessor kagerowScriptAccessor, NonComponentScriptEditPanel parent,
			KagerowExecutionPlanAccessor session) {

		// フィールド初期化
		this.kagerowScriptAccessor = kagerowScriptAccessor;
		this.parent = parent;
		this.session = session;

		// パネル初期化
		setJPanel(this);

		// ボーダ設定
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		ksqlJPanel.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEmptyBorder(),
				GUIText.NonComponentSqlEditPanel_001.toString()));

		// レイアウト設定
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(ksqlJPanel);

		// テーブル初期値を指定
		{
			// モデル生成&設定
			setModel();
			// ボタンクリックイベント管理リスナー設定
			ksqlTable.addMouseListener(new MouseAdapterImpl());
			// テーブルヘッダー配置
			ksqlJPanel.add(ksqlTable.getTableHeader(), BorderLayout.NORTH);
			// テーブル罫線設定
			ksqlTable.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
			ksqlTable.getTableHeader().setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
		}

	}

	/**
	 * テーブルモデルを初期化、生成します
	 */
	private void setModel() {

		try {
			// リフレッシュフラグオン
			refreshFlag.set(true);

			ksqlTable.setModel(new KSQLTableModel());
			// それぞれのカラムにおける幅を調整
			ksqlTable.getColumnModel().getColumn(0).setPreferredWidth(150);
			ksqlTable.getColumnModel().getColumn(1).setPreferredWidth(300);
			// ボタン列を設定（第2列）
			ksqlTable.getColumnModel().getColumn(2).setCellRenderer(new ButtonRenderer());
			ksqlTable.getColumnModel().getColumn(2).setMinWidth(100);
			ksqlTable.getColumnModel().getColumn(2).setMaxWidth(100);
			ksqlTable.getColumnModel().getColumn(2).setPreferredWidth(100);

		} finally {
			// リフレッシュフラグオフ
			refreshFlag.set(false);
		}
	}

	// ####################################################################################
	// # 内部クラス宣言
	// ####################################################################################

	/**
	 * KSQL一覧表示専用モデルクラス
	 */
	private class KSQLTableModel extends AbstractTableModel {

		/** {@inheritDoc} */
		@Override
		public int getRowCount() {
			// KSQLモデル一覧
			List<KagerowSqlAccessor> model = NonComponentSqlEditPanel.this.kagerowScriptAccessor.getRawKsqls();
			return model.size();
		}

		/** {@inheritDoc} */
		@Override
		public int getColumnCount() {
			return 3;
		}

		/** {@inheritDoc} */
		@Override
		public Object getValueAt(int row, int col) {
			// KSQLモデル一覧
			List<KagerowSqlAccessor> model = NonComponentSqlEditPanel.this.kagerowScriptAccessor.getRawKsqls();
			// 変更対象取得
			final KagerowSqlAccessor target = model.get(row);
			return switch (col) {
			case 0 -> target.getId();
			case 1 -> target.getName();
			default -> "";
			};
		}

		/** {@inheritDoc} */
		@Override
		public boolean isCellEditable(int row, int col) {
			return col == 1;
		}

		/** {@inheritDoc} */
		@Override
		public void setValueAt(Object value, int row, int col) {
			// KSQLモデル一覧
			List<KagerowSqlAccessor> model = NonComponentSqlEditPanel.this.kagerowScriptAccessor.getRawKsqls();
			// 変更対象取得
			final KagerowSqlAccessor target = model.get(row);
			switch (col) {
			case 0:
				// IDを設定
				target.setId((String) value);
			case 1:
				// 名称を設定
				target.setName((String) value);
			default:
				;
			}
		}

		/** {@inheritDoc} */
		@Override
		public String getColumnName(int col) {
			return switch (col) {
			case 0 -> "ID";
			case 1 -> "Name";
			case 2 -> "Edit";
			default -> throw new IllegalArgumentException("Unexpected value: " + col);
			};
		}

	}

	/**
	 * JTableボタンレンダラー
	 */
	private class ButtonRenderer extends JButton implements TableCellRenderer {

		/**
		 * デフォルトコンストラクタ
		 */
		public ButtonRenderer() {
			setText(GUIText.NonComponentSqlEditPanel_002.toString());
		}

		public Component getTableCellRendererComponent(JTable table, Object value,
				boolean isSelected, boolean hasFocus, int row, int column) {
			return this;
		}
	}

	/**
	 * ボタンクリック監視アダプター
	 */
	private class MouseAdapterImpl extends MouseAdapter {

		/** ダイアログヘルパー */
		private final DialogHelper helper;
		{
			helper = KagerowUtilities.getBean(DialogHelper.class, null).get();
		}

		/** {@inheritDoc} */
		@Override
		public void mouseClicked(MouseEvent e) {
			// マウスポインター取得
			int row = ksqlTable.rowAtPoint(e.getPoint());
			int col = ksqlTable.columnAtPoint(e.getPoint());
			// ボタン列を指定
			if (col == 2) {
				// モデルリスト取得
				List<KagerowSqlAccessor> modelList = getModel();
				// 処理対象のスクリプトモデル取得
				KagerowSqlAccessor target = modelList.get(row);
				// 編集パネル生成
				panel = new NonComponentKsqlPopupEditPanel(kagerowScriptAccessor, target, parent, session);
				// スクロール可能に設定
				JScrollPane scrollPane = new JScrollPane(panel);
				scrollPane.getVerticalScrollBar().setUnitIncrement(30);
				scrollPane.getHorizontalScrollBar().setUnitIncrement(10);
				scrollPane.setMinimumSize(new Dimension(0, 0));
				// ダイアログ表示
				helper.showCustomDialog(scrollPane, target.getName(), 960, 640, false);
				// パネルクローズ後の事後処理実行
				panel.close();
			}
		}

		/**
		 * データモデルを取得
		 * @return データモデル
		 */
		private List<KagerowSqlAccessor> getModel() {
			return NonComponentSqlEditPanel.this.kagerowScriptAccessor.getRawKsqls();
		}

	}

	/**
	 * 追加ボタン押下後処理メソッド
	 */
	@ActionListenerMixin.ActionCommand(COMMON_ADD_CMD)
	private void addBtn() {
		// スクリプト名称取得
		String name = dialogHelper.showInputtDialog(
				GUIText.NonComponentSqlEditPanel_003.toString(),
				GUIText.NonComponentSqlEditPanel_004.toString());
		// スクリプト追加
		KagerowScriptAccessor newAccessor = scriptHelper.addKsql(kagerowScriptAccessor, name);
		// 変更通知
		parent.refresh(newAccessor);
	}

	/**
	 * 削除実行ボタン押下後処理メソッド
	 */
	@ActionListenerMixin.ActionCommand(COMMON_DEL_CMD)
	private void delBtn() {
		// テーブルからプラグインIDを取得
		int row = this.ksqlTable.getSelectedRow();
		String targetId = (String) this.ksqlTable.getValueAt(row, 0);
		// プラグインをモデルから削除
		KagerowScriptAccessor newAccessor = scriptHelper.delKsql(kagerowScriptAccessor, targetId);
		// 変更通知
		parent.refresh(newAccessor);
	}

	/** {@inheritDoc} */
	@Override
	public void accept(KagerowScriptAccessor t) {
		// モデル更新
		this.kagerowScriptAccessor = t;
		// 子パネルを開いている場合、子側のモデルも更新
		if (Objects.nonNull(panel)) {
			panel.accept(t);
		}
		// モデル生成&設定
		SwingUtilities.invokeLater(() -> {
			setModel();
		});
	}

	/**
	 * 現在操作中のモデルを同期的に支配下にバインドします
	 * @param session セッション
	 */
	synchronized void setSession(KagerowExecutionPlanAccessor session) {
		// セッション最新化
		this.session = session;
	}

}

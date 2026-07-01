package com.sakulabo.application.app.gui.CenterPanelParts.ScriptPanelParts;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import javax.naming.NamingException;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import com.sakulabo.application.common.code.GUIText;
import com.sakulabo.application.common.code.NonComponentPluginEditPanelText;
import com.sakulabo.application.common.mixin.ActionListenerMixin;
import com.sakulabo.application.common.mixin.JButtonMixin;
import com.sakulabo.application.common.mixin.JLabelMixin;
import com.sakulabo.application.common.mixin.JPanelMixin;
import com.sakulabo.application.controller.Context.PluginContextController;
import com.sakulabo.application.helper.DialogHelper;
import com.sakulabo.application.helper.KagerowScriptHelper;
import com.sakulabo.application.service.Context.PluginContextService.PluginContextInfo;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;
import com.sakulabo.core.Kagerow.Utilities.KagerowScriptAccessor;
import com.sakulabo.core.Kagerow.Utilities.KagerowScriptAccessor.KagerowPluginAccessor;
import com.sakulabo.core.Kagerow.Utilities.KagerowUtilities;
import com.sakulabo.regulation.annotation.KagerowPlugin.PluginType;

/**
 * GUIアプリケーションのスクリプト編集パネル(Plugin宣言部)クラスです
 * 
 * @author keeeeeent
 */
@JPanelMixin.Setting(layout = JPanelMixin.Layout.BorderLayout, backgroudColor = 0xffffff)
public class NonComponentPluginEditPanel extends JPanel
		implements JLabelMixin, JPanelMixin, JButtonMixin, Consumer<KagerowScriptAccessor> {

	// ####################################################################################
	// # 共通定数
	// ####################################################################################

	/** リフレッシュフラグ */
	private final AtomicBoolean refreshFlag = new AtomicBoolean();
	/** 親コンポーネント */
	private final NonComponentScriptEditPanel parent;

	/** スクリプトヘルパー */
	private KagerowScriptHelper scriptHelper;
	{
		scriptHelper = KagerowUtilities.getBean(KagerowScriptHelper.class, null).get();
	}
	/** ダイアログヘルパー */
	private DialogHelper dialogHelper;
	{
		dialogHelper = KagerowUtilities.getBean(DialogHelper.class, null).get();
	}

	/** プラグインコントローラー */
	private PluginContextController contextController;
	{
		contextController = KagerowUtilities.getBean(PluginContextController.class, null).get();
	}

	/** スクリプトアクセッサー */
	private volatile KagerowScriptAccessor kagerowScriptAccessor;

	/** アクションコマンド（入力プラグイン追加） */
	private static final String COMMON_ADD_INPUT_CMD = "addInputBtn";
	/** アクションコマンド（入力プラグイン削除） */
	private static final String COMMON_DEL_INPUT_CMD = "delInputBtn";
	/** アクションコマンド（出力プラグイン追加） */
	private static final String COMMON_ADD_OUTPUT_CMD = "addOutputBtn";
	/** アクションコマンド（出力プラグイン削除） */
	private static final String COMMON_DEL_OUTPUT_CMD = "delOutputBtn";
	/** 未選択時項目名 */
	private static final String NOT_SELECTED;
	static {
		NOT_SELECTED = NonComponentPluginEditPanelText.NOT_SELECTED.toString();
	}

	// ####################################################################################
	// # 共通内部クラス定義(入力プラグイン一覧領域)
	// ####################################################################################

	/** 削除ボタン */
	@JButtonMixin.Setting(title = GUIText.ScriptPanel_002, actionCommand = COMMON_DEL_INPUT_CMD)
	protected JButton delInputBtn = new JButton();
	/** 追加ボタン */
	@JButtonMixin.Setting(title = GUIText.ScriptPanel_001, actionCommand = COMMON_ADD_INPUT_CMD)
	protected JButton addInputBtn = new JButton();

	/** ボタンパネル */
	protected JPanel inputButtonJpanel = new JPanel();
	{
		// ボタン設定
		try {
			setJButton(this, delInputBtn);
			setJButton(this, addInputBtn);
		} catch (IllegalAccessException | NoSuchMethodException e) {
			KagerowLogger.newAppLogger().err(e);
		}
		// レイアウト設定
		inputButtonJpanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		inputButtonJpanel.setBackground(Color.WHITE);
		// コンポーネント配置
		inputButtonJpanel.add(delInputBtn);
		inputButtonJpanel.add(Box.createHorizontalStrut(20));
		inputButtonJpanel.add(addInputBtn);
	}

	/** 入力プラグイン一覧テーブル */
	protected JTable inputPluginTable = new JTable();
	{
		// テーブルの横スクロール設定
		inputPluginTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
	}

	/**
	 * 入力プラグイン一覧パネル
	 */
	protected JPanel inputPluginJPanel = new JPanel();
	{
		// レイアウト設定
		setJPanel(inputPluginJPanel);
		// コンポーネント配置
		inputPluginJPanel.add(inputPluginTable, BorderLayout.CENTER);
		inputPluginJPanel.add(inputButtonJpanel, BorderLayout.SOUTH);
	}

	// ####################################################################################
	// # 共通内部クラス定義(出力プラグイン一覧領域)
	// ####################################################################################

	/** 削除ボタン */
	@JButtonMixin.Setting(title = GUIText.ScriptPanel_002, actionCommand = COMMON_DEL_OUTPUT_CMD)
	protected JButton delOutputBtn = new JButton();
	/** 追加ボタン */
	@JButtonMixin.Setting(title = GUIText.ScriptPanel_001, actionCommand = COMMON_ADD_OUTPUT_CMD)
	protected JButton addOutputBtn = new JButton();

	/** ボタンパネル */
	protected JPanel outputButtonJpanel = new JPanel();
	{
		// ボタン設定
		try {
			setJButton(this, delOutputBtn);
			setJButton(this, addOutputBtn);
		} catch (IllegalAccessException | NoSuchMethodException e) {
			KagerowLogger.newAppLogger().err(e);
		}
		// レイアウト設定
		outputButtonJpanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		outputButtonJpanel.setBackground(Color.WHITE);
		// コンポーネント配置
		outputButtonJpanel.add(delOutputBtn);
		outputButtonJpanel.add(Box.createHorizontalStrut(20));
		outputButtonJpanel.add(addOutputBtn);
	}

	/** 出力プラグイン一覧テーブル */
	protected JTable outputPluginTable = new JTable();
	{
		// テーブルの横スクロール設定
		outputPluginTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
	}

	/**
	 * 出力プラグイン一覧パネル
	 */
	protected JPanel outputPluginJPanel = new JPanel();
	{
		// レイアウト設定
		setJPanel(outputPluginJPanel);
		// コンポーネント配置
		outputPluginJPanel.add(outputPluginTable, BorderLayout.CENTER);
		outputPluginJPanel.add(outputButtonJpanel, BorderLayout.SOUTH);
	}

	/**
	 * デフォルトコンストラクタ
	 * @param kagerowScriptAccessor スクリプトアクセッサー
	 * @param parent 親コンポーネント
	 */
	NonComponentPluginEditPanel(KagerowScriptAccessor kagerowScriptAccessor, NonComponentScriptEditPanel parent) {

		// フィールド初期化
		this.kagerowScriptAccessor = kagerowScriptAccessor;
		this.parent = parent;

		// パネル初期化
		setJPanel(this);

		// ボーダ設定
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		inputPluginJPanel.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEmptyBorder(),
				GUIText.NonComponentPluginEditPanel_001.toString()));
		outputPluginJPanel.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEmptyBorder(),
				GUIText.NonComponentPluginEditPanel_002.toString()));

		// レイアウト設定（上書き）
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(inputPluginJPanel);
		add(Box.createVerticalStrut(20));
		add(outputPluginJPanel);

		// 入力プラグイン一覧テーブル初期値を指定
		{
			// モデル取得
			List<KagerowPluginAccessor> model = this.kagerowScriptAccessor.getRawInputPlugins();
			if (model.isEmpty()) {
				// 新規スクリプトの場合、新規モデルを生成
				inputPluginTable.setModel(new KSQLTableModel(new ArrayList<>()));
			} else {
				// 既存スクリプトの場合、モデルを読み込み
				inputPluginTable.setModel(new KSQLTableModel(model));
			}
			// テーブル設定
			setTableSetting(inputPluginTable, model);
			// ボタンクリックイベント管理リスナー設定
			this.inputMouseAdapter = new MouseAdapterImpl(inputPluginTable, model);
			inputPluginTable.addMouseListener(this.inputMouseAdapter);
			// テーブルヘッダー配置
			inputPluginJPanel.add(inputPluginTable.getTableHeader(), BorderLayout.NORTH);
			// テーブル罫線設定
			inputPluginTable.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
			inputPluginTable.getTableHeader().setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
		}

		// 出力プラグイン一覧テーブル初期値を指定
		{
			// モデル取得
			List<KagerowPluginAccessor> model = this.kagerowScriptAccessor.getRawOutputPlugins();
			if (model.isEmpty()) {
				// 新規スクリプトの場合、新規モデルを生成
				outputPluginTable.setModel(new KSQLTableModel(new ArrayList<>()));
			} else {
				// 既存スクリプトの場合、モデルを読み込み
				outputPluginTable.setModel(new KSQLTableModel(model));
			}
			// テーブル設定
			setTableSetting(outputPluginTable, model);
			// ボタンクリックイベント管理リスナー設定
			this.outputMouseAdapterImpl = new MouseAdapterImpl(outputPluginTable, model);
			outputPluginTable.addMouseListener(this.outputMouseAdapterImpl);
			// テーブルヘッダー配置
			outputPluginJPanel.add(outputPluginTable.getTableHeader(), BorderLayout.NORTH);
			// テーブル罫線設定
			outputPluginTable.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
			outputPluginTable.getTableHeader().setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
		}

	}

	/**
	 * テーブルの表示設定を行います
	 * @param table 設定対象テーブル
	 * @param model データモデルリスト
	 */
	private void setTableSetting(JTable table, List<KagerowPluginAccessor> model) {
		// それぞれのカラムにおける幅を調整
		table.getColumnModel().getColumn(0).setPreferredWidth(150);
		table.getColumnModel().getColumn(1).setPreferredWidth(300);
		// ボタン列を設定（第2列）
		table.getColumnModel().getColumn(2).setCellRenderer(new ButtonRenderer());
		table.getColumnModel().getColumn(2).setMinWidth(100);
		table.getColumnModel().getColumn(2).setMaxWidth(100);
		table.getColumnModel().getColumn(2).setPreferredWidth(100);
	}

	/**
	 * UIリフレッシュ処理
	 */
	private void refreshUI() {

		try {
			// リフレッシュフラグオン
			refreshFlag.set(true);

			// 入力プラグイン一覧テーブル初期値を指定
			{
				// モデル取得
				List<KagerowPluginAccessor> model = this.kagerowScriptAccessor.getRawInputPlugins();
				if (model.isEmpty()) {
					// 新規スクリプトの場合、新規モデルを生成
					inputPluginTable.setModel(new KSQLTableModel(new ArrayList<>()));
				} else {
					// 既存スクリプトの場合、モデルを読み込み
					inputPluginTable.setModel(new KSQLTableModel(model));
				}
				// テーブル設定
				setTableSetting(inputPluginTable, model);
				this.inputMouseAdapter.refresh(model);
			}

			// 出力プラグイン一覧テーブル初期値を指定
			{
				// モデル取得
				List<KagerowPluginAccessor> model = this.kagerowScriptAccessor.getRawOutputPlugins();
				if (model.isEmpty()) {
					// 新規スクリプトの場合、新規モデルを生成
					outputPluginTable.setModel(new KSQLTableModel(new ArrayList<>()));
				} else {
					// 既存スクリプトの場合、モデルを読み込み
					outputPluginTable.setModel(new KSQLTableModel(model));
				}
				// テーブル設定
				setTableSetting(outputPluginTable, model);
				this.outputMouseAdapterImpl.refresh(model);
			}

		} finally {
			// リフレッシュフラグオフ
			refreshFlag.set(false);
		}
	}

	// ####################################################################################
	// # 内部クラス宣言
	// ####################################################################################

	/** 編集カラムマウスリスナー（入力プラグイン） */
	private final MouseAdapterImpl inputMouseAdapter;
	/** 編集カラムマウスリスナー（出力プラグイン） */
	private final MouseAdapterImpl outputMouseAdapterImpl;

	/**
	 * KSQL一覧表示専用モデルクラス
	 */
	private class KSQLTableModel extends AbstractTableModel {

		/** KSQLモデル一覧 */
		private List<KagerowPluginAccessor> model;

		/**
		 * デフォルトコンストラクタ
		 * @param model プラグイン一覧モデル
		 */
		KSQLTableModel(List<KagerowPluginAccessor> model) {
			this.model = model;
		}

		/** {@inheritDoc} */
		@Override
		public int getRowCount() {
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
			// 変更対象取得
			final KagerowPluginAccessor target = model.get(row);
			return switch (col) {
			case 0 -> target.getId();
			case 1 -> String.join("/", target.getPackageName(), target.getName());
			default -> "";
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
			// 変更対象取得
			final KagerowPluginAccessor target = model.get(row);
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
			setText(NonComponentPluginEditPanelText.EDIT.toString());
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

		/** リッスン対象テーブル */
		private final JTable table;
		/** KSQLモデル一覧 */
		private List<KagerowPluginAccessor> model;
		/** ダイアログヘルパー */
		private final DialogHelper helper;
		{
			helper = KagerowUtilities.getBean(DialogHelper.class, null).get();
		}

		/**
		 * デフォルトコンストラクタ
		 * @param table リッスン対象テーブル
		 * @param model プラグイン一覧モデル
		 */
		MouseAdapterImpl(JTable table, List<KagerowPluginAccessor> model) {
			this.table = table;
			this.model = model;
		}

		/** {@inheritDoc} */
		@Override
		public void mouseClicked(MouseEvent e) {
			// マウスポインター取得
			int row = table.rowAtPoint(e.getPoint());
			int col = table.columnAtPoint(e.getPoint());
			// ボタン列を指定
			if (col == 2) {
				// 処理対象のスクリプトモデル取得
				KagerowPluginAccessor target = model.get(row);
				// 編集パネル生成
				NonComponentPluginPopupEditPanel panel = new NonComponentPluginPopupEditPanel(
						kagerowScriptAccessor, target, parent);
				// スクロール可能に設定
				JScrollPane scrollPane = new JScrollPane(panel);
				scrollPane.getVerticalScrollBar().setUnitIncrement(30);
				scrollPane.getHorizontalScrollBar().setUnitIncrement(10);
				scrollPane.setMinimumSize(new Dimension(0, 0));
				// ダイアログ表示
				helper.showCustomDialog(scrollPane, target.getName());
			}
		}

		/**
		 * モデルをリフレッシュします
		 * @param model プラグイン一覧モデル
		 */
		void refresh(List<KagerowPluginAccessor> model) {
			this.model = model;
		}

	}

	/**
	 * 入力プラグイン追加ボタン押下後処理メソッド
	 * @throws NamingException 
	 */
	@ActionListenerMixin.ActionCommand(COMMON_ADD_INPUT_CMD)
	private void addInputBtn() throws NamingException {
		// プラグイン情報を取得
		List<PluginContextInfo> pluginInfoList = contextController.getPluginList(PluginType.INPUT);
		// プラグイン名称を取得
		List<String> pluginList = new ArrayList<>() {
			{
				add(NOT_SELECTED);
				pluginInfoList
						.stream()
						.map(PluginContextInfo::toString)
						.forEach(this::add);
			}
		};
		// 選択ダイアログ表示
		String name = dialogHelper.showSelectDialog(
				NonComponentPluginEditPanelText.ADD_INPUT.toString(),
				pluginList.toArray(String[]::new));
		if (Objects.nonNull(name) && !name.equals(NOT_SELECTED)) {
			// スクリプト情報を取得
			PluginContextInfo info = pluginInfoList.stream()
					.filter(f -> f.toString().equals(name))
					.findFirst()
					.get();
			// スクリプト末尾に追加
			KagerowScriptAccessor kagerowScriptAccessor = scriptHelper.addInputPlugin(
					this.kagerowScriptAccessor,
					info.packageName(),
					info.pluginName());
			// モデル更新通知
			parent.refresh(kagerowScriptAccessor);
		}
	}

	/**
	 * 入力プラグイン削除実行ボタン押下後処理メソッド
	 */
	@ActionListenerMixin.ActionCommand(COMMON_DEL_INPUT_CMD)
	private void delInputBtn() {
		// テーブルからプラグインIDを取得
		int row = this.inputPluginTable.getSelectedRow();
		String targetId = (String) this.inputPluginTable.getValueAt(row, 0);
		// プラグインをモデルから削除
		KagerowScriptAccessor kagerowScriptAccessor = scriptHelper.delInputPlugin(
				this.kagerowScriptAccessor, targetId);
		// モデル更新通知
		parent.refresh(kagerowScriptAccessor);
	}

	/**
	 * 出力プラグイン追加ボタン押下後処理メソッド
	 */
	@ActionListenerMixin.ActionCommand(COMMON_ADD_OUTPUT_CMD)
	private void addOutputBtn() {
		// プラグイン情報を取得
		List<PluginContextInfo> pluginInfoList = contextController.getPluginList(PluginType.OUTPUT);
		// プラグイン名称を取得
		List<String> pluginList = new ArrayList<>() {
			{
				add(NOT_SELECTED);
				pluginInfoList
						.stream()
						.map(PluginContextInfo::toString)
						.forEach(this::add);
			}
		};
		// 選択ダイアログ表示
		String name = dialogHelper.showSelectDialog(
				NonComponentPluginEditPanelText.ADD_OUTPUT.toString(),
				pluginList.toArray(String[]::new));
		if (Objects.nonNull(name) && !name.equals(NOT_SELECTED)) {
			// スクリプト情報を取得
			PluginContextInfo info = pluginInfoList.stream()
					.filter(f -> f.toString().equals(name))
					.findFirst()
					.get();
			// スクリプト末尾に追加
			KagerowScriptAccessor kagerowScriptAccessor = scriptHelper.addOutputPlugin(
					this.kagerowScriptAccessor,
					info.packageName(),
					info.pluginName());
			// モデル更新通知
			parent.refresh(kagerowScriptAccessor);
		}
	}

	/**
	 * 出力プラグイン 削除実行ボタン押下後処理メソッド
	 */
	@ActionListenerMixin.ActionCommand(COMMON_DEL_OUTPUT_CMD)
	private void delOutputBtn() {
		// テーブルからプラグインIDを取得
		int row = this.outputPluginTable.getSelectedRow();
		String targetId = (String) this.outputPluginTable.getValueAt(row, 0);
		// プラグインをモデルから削除
		KagerowScriptAccessor kagerowScriptAccessor = scriptHelper.delOutputPlugin(
				this.kagerowScriptAccessor, targetId);
		// モデル更新通知
		parent.refresh(kagerowScriptAccessor);
	}

	/** {@inheritDoc} */
	@Override
	public void accept(KagerowScriptAccessor t) {
		// モデル更新
		this.kagerowScriptAccessor = t;
		// UIリフレッシュ
		refreshUI();
	}

}

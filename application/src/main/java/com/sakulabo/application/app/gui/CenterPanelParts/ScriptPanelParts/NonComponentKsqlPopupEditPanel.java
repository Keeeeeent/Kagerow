package com.sakulabo.application.app.gui.CenterPanelParts.ScriptPanelParts;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import javax.sql.rowset.CachedRowSet;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableModel;

import com.sakulabo.application.app.gui.CenterPanelParts.ScriptPanelParts.NonComponentResultViewPanel.NumberRenderer;
import com.sakulabo.application.common.code.GUIText;
import com.sakulabo.application.common.code.NonComponentKsqlPopupEditPanelText;
import com.sakulabo.application.common.mixin.ActionListenerMixin;
import com.sakulabo.application.common.mixin.AppMixin;
import com.sakulabo.application.common.mixin.JButtonMixin;
import com.sakulabo.application.common.mixin.JFrameMixin;
import com.sakulabo.application.common.mixin.JLabelMixin;
import com.sakulabo.application.common.mixin.JPanelMixin;
import com.sakulabo.application.common.mixin.JTextComponentMixin;
import com.sakulabo.application.controller.Script.KSQLController;
import com.sakulabo.application.helper.AcceleratorHelper;
import com.sakulabo.application.helper.AcceleratorHelper.ShortcutKey;
import com.sakulabo.application.helper.DialogHelper;
import com.sakulabo.application.helper.KagerowScriptHelper;
import com.sakulabo.core.Kagerow.Exception.KSQLParseException;
import com.sakulabo.core.Kagerow.Utilities.KagerowExecutionPlanAccessor;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;
import com.sakulabo.core.Kagerow.Utilities.KagerowScriptAccessor;
import com.sakulabo.core.Kagerow.Utilities.KagerowScriptAccessor.KagerowSqlAccessor;
import com.sakulabo.core.Kagerow.Utilities.KagerowUtilities;

/**
 * GUIアプリケーションのKSQL個別編集パネルクラスです
 * 
 * @author keeeeeent
 */
@AppMixin.Size(height = 320, width = 960)
@JFrameMixin.Setting(closeOperation = JFrame.DO_NOTHING_ON_CLOSE, title = "KagerowSQL")
@JPanelMixin.Setting(layout = JPanelMixin.Layout.BorderLayout, backgroudColor = 0xffffff)
public class NonComponentKsqlPopupEditPanel extends JPanel
		implements JLabelMixin, JPanelMixin, JButtonMixin, Consumer<KagerowScriptAccessor>, JTextComponentMixin,
		AutoCloseable, JFrameMixin {

	// ####################################################################################
	// # 共通定数
	// ####################################################################################

	/** 同時実行制御フラグメモリ */
	private final AtomicBoolean EXECUTE_FLUG = new AtomicBoolean();

	/** 親コンポーネント */
	private final NonComponentScriptEditPanel parent;

	/** 実行結果格納メモリ */
	private final KagerowExecutionPlanAccessor planAccessor;

	/** ダイアログヘルパー */
	private DialogHelper dialogHelper;
	{
		dialogHelper = KagerowUtilities.getBean(DialogHelper.class, null).get();
	}
	/** スクリプトヘルパー */
	private final KagerowScriptHelper scriptHelper;
	{
		scriptHelper = KagerowUtilities.getBean(KagerowScriptHelper.class, null).get();
	}
	/** ショートカットヘルパー */
	private final AcceleratorHelper acceleratorHelper;
	{
		acceleratorHelper = KagerowUtilities.getBean(AcceleratorHelper.class, null).get();
	}

	/** スクリプトパネル */
	private final ScriptPanel scriptPanel;
	{
		scriptPanel = KagerowUtilities.getBean(ScriptPanel.class, null).get();
	}

	/** KSQL実行コントローラー */
	private final KSQLController ksqlController;
	{
		// コントローラー初期化
		Optional<KSQLController> ksqlController = KagerowUtilities.getBean(KSQLController.class, null);
		this.ksqlController = ksqlController.get();
	}

	/** KSQLスクリプトアクセッサー */
	private volatile KagerowSqlAccessor kagerowSqlAccessor;
	/** スクリプトアクセッサー */
	private volatile KagerowScriptAccessor kagerowScriptAccessor;

	/** アクションコマンド（追加） */
	private static final String COMMON_ADD_CMD = "addBtn";
	/** アクションコマンド（削除） */
	private static final String COMMON_DEL_CMD = "delBtn";
	/** アクションコマンド（個別時刻） */
	private static final String COMMON_EXE_CMD = "exeBtn";

	// ####################################################################################
	// # 共通内部クラス定義(コマンド設定領域)
	// ####################################################################################

	/** スクリプト名称受け取りテキストフィールド */
	@JTextComponentMixin.Setting
	protected JTextArea ksqlNameField = new JTextArea();
	{
		ksqlNameField.setLineWrap(true);
		ksqlNameField.setWrapStyleWord(true);
		ksqlNameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
	}
	/** スクリプト名称操作パネル */
	protected JPanel ksqlJPanel = new JPanel();
	{
		// レイアウト設定
		setJPanel(ksqlJPanel);
		// パネルに追加
		ksqlJPanel.add(ksqlNameField);
	}

	/**
	 * SQLデータ更新リスナー
	 */
	private class ksqlNameFieldListner implements DocumentListener {

		/** {@inheritDoc} */
		@Override
		public void insertUpdate(DocumentEvent e) {
			updateData();
		}

		/** {@inheritDoc} */
		@Override
		public void removeUpdate(DocumentEvent e) {
			updateData();
		}

		/** {@inheritDoc} */
		@Override
		public void changedUpdate(DocumentEvent e) {
			updateData();
		}

		/**
		 * データ更新処理
		 */
		private void updateData() {
			// データ更新
			String sql = NonComponentKsqlPopupEditPanel.this.ksqlNameField.getText();
			// 設定変更
			KagerowSqlAccessor newKsql = NonComponentKsqlPopupEditPanel.this.kagerowSqlAccessor
					.setSql(sql);
			KagerowScriptAccessor newAccessor = scriptHelper.setRawKsqls(kagerowScriptAccessor, newKsql);
			// 変更通知
			parent.refresh(newAccessor);
		}

	}

	/**
	 * 置換変数データ更新リスナー
	 */
	private class tableModel extends NonComponentMapTableModel {

		/**
		 * デフォルトコンストラクタ
		 * @param map データ
		 */
		public tableModel(Map<String, String> map) {
			super(map);
		}

		/** {@inheritDoc} */
		@Override
		protected void refresh() {
			// 設定変更
			KagerowSqlAccessor newKsql = NonComponentKsqlPopupEditPanel.this.kagerowSqlAccessor
					.variable(this.map);
			KagerowScriptAccessor newAccessor = scriptHelper.setRawKsqls(kagerowScriptAccessor, newKsql);
			// 変更通知
			parent.refresh(newAccessor);
		}

	}

	// ####################################################################################
	// # 共通内部クラス定義(環境変数領域)
	// ####################################################################################

	/** 削除ボタン */
	@JButtonMixin.Setting(title = GUIText.ScriptPanel_002, actionCommand = COMMON_DEL_CMD)
	protected JButton delBtn = new JButton();
	/** 追加ボタン */
	@JButtonMixin.Setting(title = GUIText.ScriptPanel_001, actionCommand = COMMON_ADD_CMD)
	protected JButton addBtn = new JButton();
	/** 追加ボタン */
	@JButtonMixin.Setting(title = GUIText.ScriptPanel_003, actionCommand = COMMON_EXE_CMD)
	protected JButton exeBtn = new JButton();

	/** ボタンパネル */
	protected JPanel buttonJpanel = new JPanel();
	{
		// ボタン設定
		try {
			setJButton(this, delBtn);
			setJButton(this, addBtn);
			setJButton(this, exeBtn);
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
		buttonJpanel.add(Box.createHorizontalStrut(20));
		buttonJpanel.add(exeBtn);
	}

	/** 変数格納テーブル */
	protected JTable variableTable = new JTable();
	{
		// テーブルの横スクロール設定
		variableTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
	}

	/**
	 * 変数パネル
	 */
	protected JPanel variableJPanel = new JPanel();
	{
		// レイアウト設定
		setJPanel(variableJPanel);
		// コンポーネント配置
		variableJPanel.add(variableTable, BorderLayout.CENTER);
		variableJPanel.add(buttonJpanel, BorderLayout.SOUTH);
	}

	// ####################################################################################
	// # 結果表示パネル管理
	// ####################################################################################

	/** 結果表示パネル専用フレーム */
	private final JFrame resultFrame;
	{
		// フレーム初期化
		resultFrame = createFrame();
		setSize(resultFrame);
	}

	/**
	 * デフォルトコンストラクタKagerowScriptAccessor kagerowScriptAccessor
	 * @param kagerowScriptAccessor スクリプトアクセッサー
	 * @param kagerowSqlAccessor ksqlスクリプトアクセッサー
	 * @param planAccessor KDBセッション
	 * @param parent 親コンポーネント
	 */
	public NonComponentKsqlPopupEditPanel(KagerowScriptAccessor kagerowScriptAccessor,
			KagerowSqlAccessor kagerowSqlAccessor, NonComponentScriptEditPanel parent,
			KagerowExecutionPlanAccessor planAccessor) {

		// フィールド初期化
		this.kagerowSqlAccessor = kagerowSqlAccessor;
		this.parent = parent;
		this.kagerowScriptAccessor = kagerowScriptAccessor;
		this.planAccessor = planAccessor;

		// パネル初期化
		setJPanel(this);

		// ボーダ設定
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		ksqlJPanel.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEmptyBorder(),
				GUIText.NonComponentKsqlPopupEditPanel_001.toString()));
		variableJPanel.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEmptyBorder(),
				GUIText.NonComponentKsqlPopupEditPanel_002.toString()));

		// レイアウト設定（上書き）
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(ksqlJPanel);
		add(Box.createVerticalStrut(10));
		add(variableJPanel);

		// スクリプト初期値を指定
		{
			ksqlNameField.setText(this.kagerowSqlAccessor.getSql());
		}

		// テーブル初期値を指定
		{
			variableTable.setModel(new tableModel(this.kagerowSqlAccessor.variable()));
			// それぞれのカラムにおける幅を調整
			variableTable.getColumnModel().getColumn(0).setPreferredWidth(150);
			variableTable.getColumnModel().getColumn(1).setPreferredWidth(300);
			// テーブルヘッダー配置
			variableJPanel.add(variableTable.getTableHeader(), BorderLayout.NORTH);
			// テーブル罫線設定
			variableTable.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
			variableTable.getTableHeader().setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
		}

		// 個別実行ボタン初期化
		if (Objects.isNull(planAccessor)) {
			// セッション未生成の場合個別実行ボタンは非活性にする
			exeBtn.setEnabled(false);
			// 排他制御フラグも永続的に解除できないようフラグを立てておく
			EXECUTE_FLUG.set(true);
			// 個別実行ができないことを通知
			dialogHelper.showSystemInfo(NonComponentKsqlPopupEditPanelText.EMPTY_SESSION_TEXT.toString());
		}

		// ショートカットキー登録
		acceleratorHelper.setShortcut(ShortcutKey.PRIVATE_EXECUT_SCRIPT, this);

		// リスナー追加
		ksqlNameField.getDocument().addDocumentListener(new ksqlNameFieldListner());

		// Undoリスナー設定
		setUndo();

		// ショートカット設定
		applyMacKeyBindings();

	}

	/**
	 * 追加ボタン押下後処理メソッド
	 */
	@ActionListenerMixin.ActionCommand(COMMON_ADD_CMD)
	private void addBtn() {
		Map<String, String> variable = kagerowSqlAccessor.variable();
		String envName = dialogHelper.showInputtDialog(
				this,
				GUIText.NonComponentKsqlPopupEditPanel_003.toString(),
				GUIText.NonComponentKsqlPopupEditPanel_004.toString());
		if (Objects.nonNull(envName) && !envName.isEmpty()) {
			// 設定追加
			variable.put(envName, "");
			KagerowSqlAccessor newKsql = kagerowSqlAccessor.variable(variable);
			KagerowScriptAccessor newAccessor = scriptHelper.setRawKsqls(kagerowScriptAccessor, newKsql);
			// 変更通知
			parent.refresh(newAccessor);
			variableTable.setModel(new tableModel(variable));
		}
	}

	/**
	 * 削除実行ボタン押下後処理メソッド
	 */
	@ActionListenerMixin.ActionCommand(COMMON_DEL_CMD)
	private void delBtn() {
		Map<String, String> variable = kagerowSqlAccessor.variable();
		int row = this.variableTable.getSelectedRow();
		String key = (String) this.variableTable.getValueAt(row, 0);
		// 設定削除
		variable.remove(key);
		KagerowSqlAccessor newKsql = kagerowSqlAccessor.variable(variable);
		KagerowScriptAccessor newAccessor = scriptHelper.setRawKsqls(kagerowScriptAccessor, newKsql);
		// 変更通知
		parent.refresh(newAccessor);
		variableTable.setModel(new tableModel(variable));
	}

	/**
	 * 個別実行ボタン押下後処理メソッド
	 * @throws Exception SQL実行失敗
	 */
	@ActionListenerMixin.ActionCommand(COMMON_EXE_CMD)
	public void exeBtn() throws Exception {

		// 同時実行制御
		if (EXECUTE_FLUG.compareAndExchange(false, true)) {
			return;
		}

		// 処理中どのような終了の仕方をしても、排他制御を解除してからメソッドを終了するよう調整
		try {

			SwingUtilities.invokeLater(() -> {
				// ボタン非活性化
				exeBtn.setEnabled(false);
				addBtn.setEnabled(false);
				delBtn.setEnabled(false);
			});

			// スクリプト保存
			scriptPanel.saveScriptEditer(false);

			// キャッシュ機能が有効な場合、無効化されてもいいか確認
			if (planAccessor.isCache()) {
				if (!planAccessor.isIgnoreCashe()) {
					// 表示できる結果がないことを通知
					boolean choiceResult = dialogHelper.showChoiceDialog(
							NonComponentKsqlPopupEditPanelText.IGNORE_CHASH_TITLE.toString(),
							NonComponentKsqlPopupEditPanelText.IGNORE_CHASH_TEXT.toString());
					// 実行を拒否した場合、処理を終了
					if (!choiceResult) {
						return;
					}
				}
			}

			// モデル取得
			// ksqlIDを取得
			String ksqlId = kagerowSqlAccessor.getId();
			// 個別SQL実行
			Optional<CachedRowSet> result = Optional.empty();
			try {
				result = ksqlController.executionScript(ksqlId, kagerowScriptAccessor,
						planAccessor);
			} catch (KSQLParseException e) {
				// パースエラーが発生した場合、エラーダイアログを通知として表示
				// ログ出力
				KagerowLogger.newAppLogger().err(e);
				// ダイアログ表示
				dialogHelper.showCompileError(e.getMessage());
				// パースエラーの場合は即時終了
				return;
			}
			// 実行結果確認
			if (result.isEmpty()) {
				// 表示できる結果がないことを通知
				dialogHelper.showSystemInfo(NonComponentKsqlPopupEditPanelText.EMPTY_INFO_TEXT.toString());
				// 実行結果が空の場合、後続処理は行わない
				return;
			}
			// 表示テーブル生成
			// JTable作成
			TableModel model = new NonComponentRowSetTableModel(result.get());
			JTable table = new JTable(model);
			// レンダラー設定
			for (int j = 0; j < table.getColumnCount(); j++) {
				table.getColumnModel().getColumn(j).setCellRenderer(new NumberRenderer());
			}
			// テーブルの横スクロール設定
			table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			// 格納先のスクロール区画を生成、JTable格納
			JScrollPane scrollPane = new JScrollPane(table);
			// スクロールポリシー設定
			scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
			scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			// UI反映
			SwingUtilities.invokeLater(() -> {
				// フレーム要素のクリア
				refreshJFrame(resultFrame, scrollPane);
				// フレームを最前面にする
				onTopJFrame(resultFrame);
			});
			// フレームが表示されていない場合、バーチャルスレッドで表示
			if (!resultFrame.isVisible()) {
				Thread.ofVirtual().start(() -> {
					// フレーム表示
					resultFrame.setVisible(true);
				});
			}
		} finally {
			SwingUtilities.invokeLater(() -> {
				// ボタン活性化
				exeBtn.setEnabled(true);
				addBtn.setEnabled(true);
				delBtn.setEnabled(true);
				// 排他制御解放
				EXECUTE_FLUG.set(false);
			});
		}
	}

	/** {@inheritDoc} */
	@Override
	public void accept(KagerowScriptAccessor t) {
		this.kagerowScriptAccessor = t;
		for (KagerowSqlAccessor target : t.getRawKsqls()) {
			if (kagerowSqlAccessor.getId().equals(target.getId())) {
				this.kagerowSqlAccessor = target;
				break;
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public void refreshUndo() throws Exception {
		;
	}

	/** {@inheritDoc} */
	@Override
	public void close() {
		// フレームクローズ
		resultFrame.dispose();
	}

}

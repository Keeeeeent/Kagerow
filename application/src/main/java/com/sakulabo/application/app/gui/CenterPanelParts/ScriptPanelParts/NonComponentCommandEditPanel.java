package com.sakulabo.application.app.gui.CenterPanelParts.ScriptPanelParts;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.InputMethodEvent;
import java.awt.event.InputMethodListener;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.sakulabo.application.common.code.GUIText;
import com.sakulabo.application.common.mixin.ActionListenerMixin;
import com.sakulabo.application.common.mixin.AppMixin;
import com.sakulabo.application.common.mixin.JButtonMixin;
import com.sakulabo.application.common.mixin.JLabelMixin;
import com.sakulabo.application.common.mixin.JPanelMixin;
import com.sakulabo.application.common.mixin.JTextComponentMixin;
import com.sakulabo.application.helper.DialogHelper;
import com.sakulabo.application.helper.KagerowScriptHelper;
import com.sakulabo.core.Kagerow.Utilities.KagerowCommandMode;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;
import com.sakulabo.core.Kagerow.Utilities.KagerowScriptAccessor;
import com.sakulabo.core.Kagerow.Utilities.KagerowScriptAccessor.KagerowCmdAccessor;
import com.sakulabo.core.Kagerow.Utilities.KagerowUtilities;

/**
 * GUIアプリケーションのスクリプト編集パネル(コマンド宣言部)クラスです
 * 
 * @author keeeeeent
 */
@AppMixin.PreferredSize(width = 330, height = 20)
@JPanelMixin.Setting(layout = JPanelMixin.Layout.BorderLayout, backgroudColor = 0xffffff)
public class NonComponentCommandEditPanel extends JPanel
		implements JLabelMixin, JPanelMixin, JButtonMixin, Consumer<KagerowScriptAccessor>, JTextComponentMixin {

	// ####################################################################################
	// # 共通定数
	// ####################################################################################

	/** リフレッシュフラグ */
	private final AtomicBoolean refreshFlag = new AtomicBoolean();
	/** 親コンポーネント */
	private final NonComponentScriptEditPanel parent;

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

	/** スクリプトアクセッサー */
	private volatile KagerowScriptAccessor kagerowScriptAccessor;

	/** アクションコマンド（追加） */
	private static final String COMMON_ADD_CMD = "addBtn";
	/** アクションコマンド（削除） */
	private static final String COMMON_DEL_CMD = "delBtn";
	/** アクションコマンド（モード選択） */
	private static final String MODE_SELECT_CMD = "selectMode";

	/** スクリプト名称入力確定フラグ */
	private final AtomicBoolean commitedCommandField = new AtomicBoolean();

	// ####################################################################################
	// # 共通内部クラス定義(コマンド設定領域)
	// ####################################################################################

	/** スクリプト名称受け取りテキストフィールド */
	@JTextComponentMixin.Setting
	protected JTextArea commandField = new JTextArea();
	{
		commandField.setLineWrap(true);
		commandField.setWrapStyleWord(true);
		commandField.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
	}
	/** スクリプト名称操作パネル */
	protected JPanel commandJPanel = new JPanel();
	{
		// レイアウト設定
		setJPanel(commandJPanel);
		// コンポーネント配置
		commandJPanel.add(commandField, BorderLayout.CENTER);
	}

	/** 実行モードボックス */
	protected JComboBox<String> commandModeBox = new JComboBox<>();
	{
		// 推奨サイス設定
		setPreferredSize(commandModeBox);
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

	/** 環境変数格納テーブル */
	protected JTable envTable = new JTable();
	{
		// テーブルの横スクロール設定
		envTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
	}

	/**
	 * 環境変数パネル
	 */
	protected JPanel envJPanel = new JPanel();
	{
		// レイアウト設定
		setJPanel(envJPanel);
		// コンポーネント配置
		envJPanel.add(envTable, BorderLayout.CENTER);
		envJPanel.add(buttonJpanel, BorderLayout.SOUTH);
	}

	/** 実行モードラベル */
	@JLabelMixin.Setting(text = GUIText.NonComponentCommandEditPanel_007, width = 150, heigth = 20)
	protected JLabel commandModeLabel = new JLabel();
	/** 実行モード操作パネル */
	protected JPanel commandModeJPanel = new JPanel();

	{
		// ラベル設定
		setJLabel(commandModeLabel);
		// レイアウト設定
		commandModeJPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		commandModeJPanel.setBackground(Color.WHITE);
		// コンポーネント配置
		commandModeJPanel.add(commandModeLabel);
		commandModeJPanel.add(commandModeBox);
	}

	// ####################################################################################
	// # 共通内部クラス定義(リスナー)
	// ####################################################################################

	/**
	 * スクリプト名称データ更新リスナー
	 */
	private class EditscommandFieldListner implements DocumentListener {

		/** 入力確定フラグ参照メモリ */
		private final AtomicBoolean commited;

		/**
		 * デフォルトコンストラクタ
		 * @param commited 入力確定フラグ
		 */
		EditscommandFieldListner(AtomicBoolean commited) {
			this.commited = commited;
		}

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
			// 入力イベントが確定していない場合処理を一時停止
			if (commited.get()) {
				return;
			}
			// 変更通知
			if (!refreshFlag.get()) {
				// データ更新
				final KagerowScriptAccessor accessor = NonComponentCommandEditPanel.this.kagerowScriptAccessor;
				String strCmd = NonComponentCommandEditPanel.this.commandField.getText();
				// 新規作成の場合、このタイミングではコマンドインスタンスが未初期化のため新たなインスタンスを生成する
				KagerowCmdAccessor cmd = null;
				if (accessor.getRawCommand().isEmpty()) {
					// アクセッサー生成
					cmd = KagerowCmdAccessor.createEmptyInstance(accessor);
					// この時点ではモードが未選択のため、デフォルト値を設定
					String mode = (String) commandModeBox.getSelectedItem();
					cmd = cmd.setMode(KagerowCommandMode.toMode(mode));
				} else {
					cmd = accessor.getRawCommand().getFirst();
				}
				// 入力設定
				cmd = cmd.setCmd(strCmd);
				KagerowScriptAccessor newAccessor = scriptHelper.setRawCommand(kagerowScriptAccessor, cmd);

				parent.refresh(newAccessor);
			}
		}

	}

	/**
	 * 環境変数データ更新リスナー
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
			final KagerowScriptAccessor accessor = NonComponentCommandEditPanel.this.kagerowScriptAccessor;
			// 新規作成の場合、このタイミングではコマンドインスタンスが未初期化のため新たなインスタンスを生成する
			KagerowCmdAccessor cmd = null;
			if (accessor.getRawCommand().isEmpty()) {
				// アクセッサー生成
				cmd = KagerowCmdAccessor.createEmptyInstance(accessor);
				// この時点ではモードが未選択のため、デフォルト値を設定
				String mode = (String) commandModeBox.getSelectedItem();
				cmd = cmd.setMode(KagerowCommandMode.toMode(mode));
			} else {
				cmd = accessor.getRawCommand().getFirst();
			}
			// 設定変更
			cmd = cmd.environmental(this.map);
			KagerowScriptAccessor newAccessor = scriptHelper.setRawCommand(kagerowScriptAccessor, cmd);
			// 変更通知
			if (!refreshFlag.get()) {
				parent.refresh(newAccessor);
			}
		}

	}

	/**
	 * 入力確定検知専用リスナー
	 */
	private class InputMethodListenerImpl implements InputMethodListener {

		/** 入力確定フラグ参照メモリ */
		private final AtomicBoolean commited;

		/**
		 * デフォルトコンストラクタ
		 * @param commited 入力確定フラグ
		 */
		InputMethodListenerImpl(AtomicBoolean commited) {
			this.commited = commited;
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

	}

	/**
	 * デフォルトコンストラクタ
	 * @param kagerowScriptAccessor スクリプトアクセッサー
	 * @param parent 親コンポーネント
	 */
	public NonComponentCommandEditPanel(
			KagerowScriptAccessor kagerowScriptAccessor,
			NonComponentScriptEditPanel parent) {

		// フィールド初期化
		this.kagerowScriptAccessor = kagerowScriptAccessor;
		this.parent = parent;

		// パネル初期化
		setJPanel(this);

		// ボーダ設定
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		commandModeJPanel.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEmptyBorder(),
				GUIText.NonComponentCommandEditPanel_006.toString()));
		commandJPanel.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEmptyBorder(),
				GUIText.NonComponentCommandEditPanel_001.toString()));
		envJPanel.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEmptyBorder(),
				GUIText.NonComponentCommandEditPanel_002.toString()));

		// レイアウト設定（上書き）
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(commandModeJPanel);
		add(Box.createVerticalStrut(10));
		add(commandJPanel);
		add(Box.createVerticalStrut(10));
		add(envJPanel);

		// コマンドモデル取得
		List<KagerowCmdAccessor> kagerowCmdAccessor = this.kagerowScriptAccessor.getRawCommand();

		// 新規作成の場合
		if (kagerowCmdAccessor.isEmpty()) {
			// アクセッサーからコマンドアクセッサーを新規作成
			KagerowCmdAccessor KkgerowCmdAccessor = KagerowCmdAccessor.createEmptyInstance(this.kagerowScriptAccessor);
			// 新規コマンドとして追加
			kagerowCmdAccessor.add(KkgerowCmdAccessor);
		}

		// コマンド設定
		KagerowCmdAccessor cmd = kagerowCmdAccessor.getFirst();

		// 実行モード初期値を設定
		{
			// 初期化フラグ取得&コンボボックス初期化
			boolean isInit = setCommandModeBox(cmd);
			try {
				// リスナーの設定
				addCache(this, commandModeBox, MODE_SELECT_CMD);
				if (!isInit && Objects.isNull(cmd.getMode())) {
					// 初回起動の場合モードの場合は最初の要素を選択
					commandModeBox.setSelectedIndex(0);
				}
			} catch (IllegalAccessException | NoSuchMethodException e) {
				KagerowLogger.newAppLogger().err(e);
			}
		}

		// スクリプト初期値を指定
		{
			commandField.setText(cmd.getCmd());
			commandField.getDocument().addDocumentListener(new EditscommandFieldListner(commitedCommandField));
			commandField.addInputMethodListener(new InputMethodListenerImpl(commitedCommandField));
		}

		// テーブル初期値を指定
		{
			envTable.setModel(new tableModel(cmd.environmental()));
			// それぞれのカラムにおける幅を調整
			envTable.getColumnModel().getColumn(0).setPreferredWidth(150);
			envTable.getColumnModel().getColumn(1).setPreferredWidth(300);
			// テーブルヘッダー配置
			envJPanel.add(envTable.getTableHeader(), BorderLayout.NORTH);
			// テーブル罫線設定
			envTable.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
			envTable.getTableHeader().setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
		}

		// Undoリスナー設定
		setUndo();

		// ショートカット設定
		applyMacKeyBindings();

	}

	/**
	 * コンボボックスの初期値を設定します
	 * @param cmd コマンドインスタンス
	 * @return 初期化フラグ
	 */
	private boolean setCommandModeBox(KagerowCmdAccessor cmd) {
		// 初期化フラグ
		boolean isInit = false;
		// インデックス
		int index = 0;
		// 初期値設定
		for (KagerowCommandMode mode : KagerowCommandMode.values()) {
			if (mode.canExecute()) {
				// 実行可能な場合初期値を設定
				commandModeBox.addItem(mode.toString());
				// スクリプトで指定しているモードの場合選択済みにする
				if (mode.equals(cmd.getMode())) {
					// アイテム選択
					commandModeBox.setSelectedIndex(index);
					// フラグオン
					isInit = true;
				}
				// インデックスカウント
				index++;
			}
		}
		// 既存スクリプトにて初期化されていない場合、別プラットフォームでのスクリプト読み込み、
		// もしくは実行環境の構築ができていない状態のため変更不可能な状態とする
		if (!isInit) {
			// 初回起動の場合モードはnullのため個々の処理は行わない
			if (Objects.nonNull(cmd.getMode())) {
				// スクリプトに設定されているコマンド実行モードを追加
				commandModeBox.addItem(cmd.getMode().name());
				// アイテム選択
				commandModeBox.setSelectedIndex(index);
				// 変更不可能に設定
				commandModeBox.setEnabled(false);
			}
		}
		// 初期化フラグ返却
		return isInit;
	}

	/**
	 * UIリフレッシュ処理
	 */
	private void refreshUI() {
		refresh();
	}

	/**
	 * 追加ボタン押下後処理メソッド
	 */
	@ActionListenerMixin.ActionCommand(COMMON_ADD_CMD)
	private void addBtn() {
		// 変更値取得
		Map<String, String> envMap = this.kagerowScriptAccessor.getRawCommand().getFirst().environmental();
		String envName = dialogHelper.showInputtDialog(
				GUIText.NonComponentCommandEditPanel_004.toString(),
				GUIText.NonComponentCommandEditPanel_005.toString());
		if (Objects.nonNull(envName)) {
			// 環境変数追加
			envMap.put(envName, "");
			// 設定変更
			KagerowCmdAccessor cmd = this.kagerowScriptAccessor.getRawCommand().getFirst()
					.environmental(envMap);
			KagerowScriptAccessor newAccessor = scriptHelper.setRawCommand(kagerowScriptAccessor, cmd);
			// 変更通知
			if (!refreshFlag.get()) {
				parent.refresh(newAccessor);
			}
			// UI更新
			SwingUtilities.invokeLater(() -> {
				envTable.setModel(new tableModel(envMap));
			});
		}
	}

	/**
	 * 削除実行ボタン押下後処理メソッド
	 */
	@ActionListenerMixin.ActionCommand(COMMON_DEL_CMD)
	private void delBtn() {
		// 変更値取得
		Map<String, String> envMap = this.kagerowScriptAccessor.getRawCommand().getFirst().environmental();
		int row = this.envTable.getSelectedRow();
		String key = (String) this.envTable.getValueAt(row, 0);
		// 環境変数削除
		envMap.remove(key);
		// 設定変更
		KagerowCmdAccessor cmd = this.kagerowScriptAccessor.getRawCommand().getFirst().environmental(envMap);
		KagerowScriptAccessor newAccessor = scriptHelper.setRawCommand(kagerowScriptAccessor, cmd);
		// 変更通知
		if (!refreshFlag.get()) {
			parent.refresh(newAccessor);
		}
		// UI更新
		SwingUtilities.invokeLater(() -> {
			envTable.setModel(new tableModel(envMap));
		});
	}

	/**
	 * コンボボックス選択後処理メソッド
	 * @param event イベント
	 */
	@SuppressWarnings("unchecked")
	@ActionListenerMixin.ActionCommand(MODE_SELECT_CMD)
	private void selectMode(ActionEvent event) {
		// 選択されたモードを取得
		JComboBox<String> comboBox = (JComboBox<String>) event.getSource();
		String mode = (String) comboBox.getSelectedItem();
		// コマンドが未定義の場合早期リターン
		List<KagerowCmdAccessor> list = kagerowScriptAccessor.getRawCommand();
		// 新規作成の場合、このタイミングではコマンドインスタンスが未初期化のため新たなインスタンスを生成する
		KagerowCmdAccessor cmd = null;
		if (list.isEmpty()) {
			// アクセッサー生成
			cmd = KagerowCmdAccessor.createEmptyInstance(kagerowScriptAccessor);
		} else {
			cmd = list.getFirst();
		}
		// 選択された設定の保存
		cmd = cmd.setMode(KagerowCommandMode.toMode(mode));
		// 設定変更
		KagerowScriptAccessor newAccessor = scriptHelper.setRawCommand(kagerowScriptAccessor, cmd);
		// 変更通知
		if (!refreshFlag.get()) {
			parent.refresh(newAccessor);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void accept(KagerowScriptAccessor t) {
		// モデル更新
		this.kagerowScriptAccessor = t;
		// UIリフレッシュ
		SwingUtilities.invokeLater(() -> {
			refreshUI();
		});
	}

	/** {@inheritDoc} */
	@Override
	public void refreshUndo() throws Exception {

		try {
			// リフレッシュフラグオン
			refreshFlag.set(true);
			// コマンドモデル取得
			List<KagerowCmdAccessor> kagerowCmdAccessor = this.kagerowScriptAccessor.getRawCommand();
			if (kagerowCmdAccessor.isEmpty()) {
				return;
			}
			// コマンド設定
			KagerowCmdAccessor cmd = kagerowCmdAccessor.getFirst();
			// スクリプト初期値を指定
			int caret = commandField.getCaretPosition();
			commandField.setText(cmd.getCmd());
			caret = Math.min(caret, commandField.getDocument().getLength());
			commandField.setCaretPosition(caret);
			// テーブル初期値を指定
			envTable.setModel(new tableModel(cmd.environmental()));
			// それぞれのカラムにおける幅を調整
			envTable.getColumnModel().getColumn(0).setPreferredWidth(150);
			envTable.getColumnModel().getColumn(1).setPreferredWidth(300);
			// コンボボックス初期化
			for (KagerowCommandMode mode : KagerowCommandMode.values()) {
				if (mode.canExecute()) {
					// スクリプトで指定しているモードの場合選択済みにする
					if (mode.equals(cmd.getMode())) {
						// アイテム選択
						commandModeBox.setSelectedItem(mode.toString());
						break;
					}
				}
			}
		} finally {
			// リフレッシュフラグオフ
			refreshFlag.set(false);
		}

	}

}

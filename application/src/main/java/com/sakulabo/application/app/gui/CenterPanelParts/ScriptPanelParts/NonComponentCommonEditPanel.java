package com.sakulabo.application.app.gui.CenterPanelParts.ScriptPanelParts;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.InputMethodEvent;
import java.awt.event.InputMethodListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import javax.naming.NamingException;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.sakulabo.application.app.gui.CenterPanelParts.ContextPanelParts.ContextPanel;
import com.sakulabo.application.common.code.GUIText;
import com.sakulabo.application.common.code.NonComponentCommonEditPanelText;
import com.sakulabo.application.common.mixin.ActionListenerMixin;
import com.sakulabo.application.common.mixin.AppMixin;
import com.sakulabo.application.common.mixin.JButtonMixin;
import com.sakulabo.application.common.mixin.JLabelMixin;
import com.sakulabo.application.common.mixin.JPanelMixin;
import com.sakulabo.application.common.mixin.JTextComponentMixin;
import com.sakulabo.application.helper.DialogHelper;
import com.sakulabo.core.Kagerow.Context.KagerowCacheContext;
import com.sakulabo.core.Kagerow.Exception.AppLogicException;
import com.sakulabo.core.Kagerow.Utilities.KagerowDBMode;
import com.sakulabo.core.Kagerow.Utilities.KagerowExecutionPlanAccessor;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;
import com.sakulabo.core.Kagerow.Utilities.KagerowScriptAccessor;
import com.sakulabo.core.Kagerow.Utilities.KagerowUtilities;

/**
 * GUIアプリケーションのスクリプト編集パネル(共通宣言部)クラスです
 * @author keeeeeent
 */
@AppMixin.PreferredSize(width = 330, height = 20)
@JPanelMixin.Setting(layout = JPanelMixin.Layout.BorderLayout, backgroudColor = 0xffffff)
public class NonComponentCommonEditPanel extends JPanel
		implements JLabelMixin, JPanelMixin, JButtonMixin, Consumer<KagerowScriptAccessor>, JTextComponentMixin {

	// ####################################################################################
	// # 共通定数
	// ####################################################################################

	/** 初期化フラグ */
	private final AtomicBoolean initFlag = new AtomicBoolean();
	/** リフレッシュフラグ */
	private final AtomicBoolean refreshFlag = new AtomicBoolean();
	/** 親コンポーネント */
	private final NonComponentScriptEditPanel parent;
	/** KDBセッション */
	private volatile KagerowExecutionPlanAccessor session;

	/** ダイアログヘルパー */
	private DialogHelper dialogHelper;
	{
		dialogHelper = KagerowUtilities.getBean(DialogHelper.class, null).get();
	}

	/** コンテキストパネル */
	private ContextPanel contextPanel;
	{
		contextPanel = KagerowUtilities.getBean(ContextPanel.class, null).get();
	}

	/** スクリプトアクセッサー */
	private volatile KagerowScriptAccessor kagerowScriptAccessor;

	/** アクションコマンド（追加） */
	private static final String COMMON_ADD_CMD = "addBtn";
	/** アクションコマンド（削除） */
	private static final String COMMON_DEL_CMD = "delBtn";
	/** アクションコマンド（モード選択） */
	private static final String MODE_SELECT_CMD = "selectMode";
	/** アクションコマンド（キャッシュ作成） */
	private static final String CREATE_CACHE_CMD = "createCacheBtn";

	/** スクリプト名称入力確定フラグ */
	private final AtomicBoolean commitedScriptNameField = new AtomicBoolean();
	/** スクリプト概要入力確定フラグ */
	private final AtomicBoolean commitedSucriptSummaryArea = new AtomicBoolean();
	/** スクリプトスキーマ入力確定フラグ */
	private final AtomicBoolean commitedScriptSchemaField = new AtomicBoolean();

	// ####################################################################################
	// # 共通内部クラス定義(共通設定領域)
	// ####################################################################################

	/** スクリプト名称受け取りテキストフィールド */
	@JTextComponentMixin.Setting
	protected JTextField scriptNameField = new JTextField(40);
	/** スクリプト名称ラベル */
	@JLabelMixin.Setting(text = GUIText.NonComponentCommonEditPanel_004, width = 150, heigth = 20)
	protected JLabel scriptNameLabel = new JLabel();
	/** スクリプト名称操作パネル */
	protected JPanel scriptNameJPanel = new JPanel();

	{
		// ラベル設定
		setJLabel(scriptNameLabel);
		// レイアウト設定
		scriptNameJPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		scriptNameJPanel.setBackground(Color.WHITE);
		// コンポーネント配置
		scriptNameJPanel.add(scriptNameLabel);
		scriptNameJPanel.add(scriptNameField);
	}

	/** スクリプト概要受け取りテキストフィールド */
	@JTextComponentMixin.Setting
	protected JTextArea sucriptSummaryArea = new JTextArea(3, 40);
	/** スクリプト名称ラベル */
	@JLabelMixin.Setting(text = GUIText.NonComponentCommonEditPanel_005, width = 150, heigth = 20)
	protected JLabel sucriptSummaryLabel = new JLabel();
	/** スクリプト名称操作パネル */
	protected JPanel sucriptSummaryJPanel = new JPanel();

	{
		// ラベル設定
		setJLabel(sucriptSummaryLabel);
		// レイアウト設定
		sucriptSummaryJPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		sucriptSummaryJPanel.setBackground(Color.WHITE);
		// コンポーネント配置
		sucriptSummaryJPanel.add(sucriptSummaryLabel);
		sucriptSummaryJPanel.add(sucriptSummaryArea);
	}

	/** 実行モードボックス */
	protected JComboBox<String> scriptModeBox = new JComboBox<>();

	{

		// 推奨サイス設定
		setPreferredSize(scriptModeBox);

		for (KagerowDBMode mode : KagerowDBMode.values()) {
			// 不明なモードを除き、設定可能なモードであれば初期値とする
			if (KagerowDBMode.ILLEGALITY != mode) {
				// 初期値を設定
				scriptModeBox.addItem(mode.toString());
				// 初期値が指定された場合、変更不可能とする
				scriptModeBox.setEnabled(false);
			}
		}

		// リスナーの設定
		try {
			addCache(this, scriptModeBox, MODE_SELECT_CMD);
		} catch (IllegalAccessException | NoSuchMethodException e) {
			KagerowLogger.newAppLogger().err(e);
		}

	}

	/** 実行モードラベル */
	@JLabelMixin.Setting(text = GUIText.NonComponentCommonEditPanel_001, width = 150, heigth = 20)
	protected JLabel scriptModeLabel = new JLabel();
	/** 実行モード操作パネル */
	protected JPanel scriptModeJPanel = new JPanel();

	{
		// ラベル設定
		setJLabel(scriptModeLabel);
		// レイアウト設定
		scriptModeJPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		scriptModeJPanel.setBackground(Color.WHITE);
		// コンポーネント配置
		scriptModeJPanel.add(scriptModeLabel);
		scriptModeJPanel.add(scriptModeBox);
	}

	/** カレントスキーマ受け取りテキストフィールド */
	@JTextComponentMixin.Setting
	protected JTextField scriptSchemaField = new JTextField(40);
	/** カレントスキーマラベル */
	@JLabelMixin.Setting(text = GUIText.NonComponentCommonEditPanel_006, width = 150, heigth = 20)
	protected JLabel scriptSchemaLabel = new JLabel();
	/** カレントスキーマパネル */
	protected JPanel scriptSchemaJPanel = new JPanel();

	{
		// ラベル設定
		setJLabel(scriptSchemaLabel);
		// レイアウト設定
		scriptSchemaJPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		scriptSchemaJPanel.setBackground(Color.WHITE);
		// コンポーネント配置
		scriptSchemaJPanel.add(scriptSchemaLabel);
		scriptSchemaJPanel.add(scriptSchemaField);
	}

	/**
	 * 共通設定パネル
	 */
	protected JPanel optionJPanel = new JPanel();

	{
		// レイアウト設定
		optionJPanel.setLayout(new BoxLayout(optionJPanel, BoxLayout.Y_AXIS));
		optionJPanel.setBackground(Color.WHITE);
		// コンポーネント配置
		optionJPanel.add(scriptNameJPanel);
		optionJPanel.add(sucriptSummaryJPanel);
		optionJPanel.add(scriptModeJPanel);
		optionJPanel.add(scriptSchemaJPanel);
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
		envJPanel.setLayout(new BorderLayout());
		envJPanel.setBackground(Color.WHITE);

		// コンポーネント配置
		envJPanel.add(envTable, BorderLayout.CENTER);
		envJPanel.add(buttonJpanel, BorderLayout.SOUTH);
	}

	// ####################################################################################
	// # 共通内部クラス定義(キャッシュ領域)
	// ####################################################################################

	/** キャッシュ作成ボタン */
	@JButtonMixin.Setting(title = GUIText.ScriptPanel_004, actionCommand = CREATE_CACHE_CMD)
	protected JButton cacheCreateBtn = new JButton();

	/** ボタンパネル */
	protected JPanel cacheButtonJpanel = new JPanel();

	{
		// ボタン設定
		try {
			setJButton(this, cacheCreateBtn);
		} catch (IllegalAccessException | NoSuchMethodException e) {
			KagerowLogger.newAppLogger().err(e);
		}
		// 初回スクリプト実行までボタンは非活性
		cacheCreateBtn.setEnabled(false);
		// レイアウト設定
		cacheButtonJpanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		cacheButtonJpanel.setBackground(Color.WHITE);
		// コンポーネント配置
		cacheButtonJpanel.add(cacheCreateBtn);
	}

	/** キャッシュ設定値格納テーブル */
	protected JTable cacheTable = new JTable();

	{
		// テーブルの横スクロール設定
		cacheTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
	}

	/**
	 * キャッシュパネル
	 */
	protected JPanel cacheJPanel = new JPanel();

	{
		// レイアウト設定
		cacheJPanel.setLayout(new BorderLayout());
		cacheJPanel.setBackground(Color.WHITE);

		// コンポーネント配置
		cacheJPanel.add(cacheTable, BorderLayout.CENTER);
		cacheJPanel.add(cacheButtonJpanel, BorderLayout.SOUTH);
	}

	// ####################################################################################
	// # 共通内部クラス定義(リスナー)
	// ####################################################################################

	/**
	 * スクリプト名称データ更新リスナー
	 */
	private class EditscriptNameFieldListner implements DocumentListener {

		/** 入力確定フラグ参照メモリ */
		private final AtomicBoolean commited;

		/**
		 * デフォルトコンストラクタ
		 * @param commited 入力確定フラグ
		 */
		EditscriptNameFieldListner(AtomicBoolean commited) {
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
			if (!refreshFlag.get()) {
				// データ更新
				final KagerowScriptAccessor accessor = NonComponentCommonEditPanel.this.kagerowScriptAccessor;
				String name = NonComponentCommonEditPanel.this.scriptNameField.getText();
				KagerowScriptAccessor newAccessor = accessor.setRawName(name);
				// 変更通知
				parent.refresh(newAccessor);
			}
		}

	}

	/**
	 * スクリプト概要データ更新リスナー
	 */
	private class EditsucriptSummaryAreaListner implements DocumentListener {

		/** 入力確定フラグ参照メモリ */
		private final AtomicBoolean commited;

		/**
		 * デフォルトコンストラクタ
		 * @param commited 入力確定フラグ
		 */
		EditsucriptSummaryAreaListner(AtomicBoolean commited) {
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
				final KagerowScriptAccessor accessor = NonComponentCommonEditPanel.this.kagerowScriptAccessor;
				String summary = NonComponentCommonEditPanel.this.sucriptSummaryArea.getText();
				KagerowScriptAccessor newAccessor = accessor.setRawSummary(summary);
				parent.refresh(newAccessor);
			}
		}

	}

	/**
	 * カレントスキーマデータ更新リスナー
	 */
	private class EditscriptSchemaFieldListner implements DocumentListener {

		/** 入力確定フラグ参照メモリ */
		private final AtomicBoolean commited;

		/**
		 * デフォルトコンストラクタ
		 * @param commited 入力確定フラグ
		 */
		EditscriptSchemaFieldListner(AtomicBoolean commited) {
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
				final KagerowScriptAccessor accessor = NonComponentCommonEditPanel.this.kagerowScriptAccessor;
				String schema = NonComponentCommonEditPanel.this.scriptSchemaField.getText();
				KagerowScriptAccessor newAccessor = accessor.setRawSchema(schema);
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
	 * スクリプト環境変数データ更新リスナー
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
			KagerowScriptAccessor newAccessor = NonComponentCommonEditPanel.this.kagerowScriptAccessor
					.setEnv(this.map);
			// 変更通知
			if (!refreshFlag.get()) {
				parent.refresh(newAccessor);
			}
		}

	}

	/**
	 * キャッシュデータ更新リスナー
	 */
	private class cacheTableModel extends NonComponentMapTableModel {

		/**
		 * デフォルトコンストラクタ
		 */
		public cacheTableModel() {
			super(new HashMap<>() {
				{
					String cacheId = NonComponentCommonEditPanel.this.kagerowScriptAccessor.getRawCacheId();
					put(GUIText.NonComponentCommonEditPanel_010.toString(), cacheId);
					if (Objects.nonNull(NonComponentCommonEditPanel.this.session)) {
						String lastUpdateAt = NonComponentCommonEditPanel.this.session.getCacheTime();
						put(GUIText.NonComponentCommonEditPanel_013.toString(), lastUpdateAt);
					} else {
						put(GUIText.NonComponentCommonEditPanel_013.toString(), "-");
					}
				}
			});
		}

		/** {@inheritDoc} */
		@Override
		protected void refresh() {
			// 設定変更
			KagerowScriptAccessor newAccessor = NonComponentCommonEditPanel.this.kagerowScriptAccessor
					.setCacheId(super.map.get(GUIText.NonComponentCommonEditPanel_010.toString()));
			// 変更通知
			if (!refreshFlag.get()) {
				parent.refresh(newAccessor);
			}
		}

		/** {@inheritDoc} */
		@Override
		public String getColumnName(int col) {
			return col == 0
					? GUIText.NonComponentCommonEditPanel_011.toString()
					: GUIText.NonComponentCommonEditPanel_012.toString();
		}

		/** {@inheritDoc} */
		@Override
		public boolean isCellEditable(int row, int col) {
			return false;
		}

	}

	// 共通処理
	{
		// Undoリスナー設定
		setUndo();
		// ショートカット設定
		applyMacKeyBindings();
	}

	/**
	 * デフォルトコンストラクタ
	 * @param kagerowScriptAccessor スクリプトアクセッサー
	 * @param parent 親コンポーネント
	 * @param session KDBセッション
	 */
	NonComponentCommonEditPanel(KagerowScriptAccessor kagerowScriptAccessor, NonComponentScriptEditPanel parent,
			KagerowExecutionPlanAccessor session) {

		// フィールド初期化
		this.kagerowScriptAccessor = kagerowScriptAccessor;
		this.parent = parent;
		this.session = session;

		// パネル初期化
		setJPanel(this);

		// ボーダ設定
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		optionJPanel.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(),
				GUIText.NonComponentCommonEditPanel_002.toString()));
		envJPanel.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEmptyBorder(),
				GUIText.NonComponentCommonEditPanel_003.toString()));
		cacheJPanel.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEmptyBorder(),
				GUIText.NonComponentCommonEditPanel_009.toString()));

		// レイアウト設定
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(optionJPanel);
		add(Box.createVerticalStrut(10));
		add(envJPanel);
		add(Box.createVerticalStrut(10));
		add(cacheJPanel);

		// コンボボックス初期値を指定
		scriptModeBox.setSelectedItem(this.kagerowScriptAccessor.getMode().toString());
		// 変更リスナー登録
		scriptNameField.getDocument().addDocumentListener(new EditscriptNameFieldListner(commitedScriptNameField));
		scriptNameField.addInputMethodListener(new InputMethodListenerImpl(commitedScriptNameField));

		sucriptSummaryArea.getDocument()
				.addDocumentListener(new EditsucriptSummaryAreaListner(commitedSucriptSummaryArea));
		sucriptSummaryArea.addInputMethodListener(new InputMethodListenerImpl(commitedSucriptSummaryArea));

		scriptSchemaField.getDocument()
				.addDocumentListener(new EditscriptSchemaFieldListner(commitedScriptSchemaField));
		scriptSchemaField.addInputMethodListener(new InputMethodListenerImpl(commitedScriptSchemaField));

		// テーブル初期値を指定（環境変数）
		{
			// テーブルヘッダー配置
			envJPanel.add(envTable.getTableHeader(), BorderLayout.NORTH);
			// テーブル罫線設定
			envTable.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
			envTable.getTableHeader().setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
		}

		// テーブル初期値を指定（キャッシュ）
		{
			// テーブルヘッダー配置
			cacheJPanel.add(cacheTable.getTableHeader(), BorderLayout.NORTH);
			// テーブル罫線設定
			cacheTable.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
			cacheTable.getTableHeader().setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
		}

		// キャッシュが作成済みの場合キャッシュ作成ボタンを更新へ変更
		if (KagerowScriptAccessor.isCached(this.kagerowScriptAccessor)) {
			cacheCreateBtn.setText(GUIText.ScriptPanel_005.toString());
		}

		// UI初期化
		refreshUI();

		// 初期化フラグをオフに設定
		initFlag.set(true);

	}

	/**
	 * 追加の初期化処理
	 */
	public void initialize() {
		// キャッシュIDの有効性チェック
		if (KagerowScriptAccessor.isCached(kagerowScriptAccessor)) {
			String cacheId = kagerowScriptAccessor.getCacheId();
			try {
				// キャッシュコンテキスト取得
				KagerowCacheContext ctx = KagerowUtilities.getContext(KagerowCacheContext._NAME);
				// 有効性確認
				boolean isValid = ctx.isExist(cacheId);
				if (!isValid) {
					// キャッシュIDが不正な旨通知
					dialogHelper.showSystemWarning(NonComponentCommonEditPanelText.INVALID_CACHE_ID.toString(
							new Object[] {
									cacheId
							}));
					// キャッシュが不正な場合、スクリプトのキャッシュIDをデフォルトへ変換
					KagerowScriptAccessor newAccessor = kagerowScriptAccessor.setRawCacheId("default");
					// 変更通知
					this.parent.refresh(newAccessor);
				}
			} catch (NamingException e) {
				KagerowLogger.newAppLogger().err(e);
			}
		}
	}

	/**
	 * UIリフレッシュ処理
	 */
	private void refreshUI() {
		// Undoマネージャー無効化
		refresh();
	}

	/**
	 * 追加ボタン押下後処理メソッド
	 */
	@ActionListenerMixin.ActionCommand(COMMON_ADD_CMD)
	private void addBtn() {
		Map<String, String> envMap = this.kagerowScriptAccessor.getEnv();
		String envName = dialogHelper.showInputtDialog(
				GUIText.NonComponentCommonEditPanel_007.toString(),
				GUIText.NonComponentCommonEditPanel_008.toString());
		if (Objects.nonNull(envName)) {
			envMap.put(envName, "");
			// 設定変更
			KagerowScriptAccessor newAccessor = this.kagerowScriptAccessor.setEnv(envMap);
			// 変更通知
			if (!refreshFlag.get()) {
				parent.refresh(newAccessor);
			}
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
		Map<String, String> envMap = this.kagerowScriptAccessor.getEnv();
		int row = this.envTable.getSelectedRow();
		String key = (String) this.envTable.getValueAt(row, 0);
		envMap.remove(key);
		// 設定変更
		KagerowScriptAccessor newAccessor = this.kagerowScriptAccessor.setEnv(envMap);
		// 変更通知
		if (!refreshFlag.get()) {
			parent.refresh(newAccessor);
		}
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
		// 設定変更
		KagerowScriptAccessor newAccessor = kagerowScriptAccessor.setMode(KagerowDBMode.toMode(mode));
		// 変更通知
		if (!refreshFlag.get()) {
			parent.refresh(newAccessor);
		}
	}

	/**
	 * キャッシュ作成実行ボタン押下後処理メソッド
	 */
	@ActionListenerMixin.ActionCommand(CREATE_CACHE_CMD)
	private void createCacheBtn() {
		try {
			// キャッシュ生成
			String cacheId = session.toCache();
			// データ変更
			KagerowScriptAccessor newAccessor = kagerowScriptAccessor.setRawCacheId(cacheId);
			// 変更通知
			if (!refreshFlag.get()) {
				parent.refresh(newAccessor);
				// インポート終了後、コンテキストパネルに通知をする
				contextPanel.noticeObserver();
			}
		} catch (AppLogicException | NamingException e) {
			KagerowLogger.newAppLogger().err(e);
		}
	}

	/**
	 * スクリプトが新規作成時であることをモデルに通知します
	 */
	public void isNewCreate() {
		scriptModeBox.setEnabled(true);
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
			// テキストフィールド初期値を指定
			{
				int caret = scriptNameField.getCaretPosition();
				scriptNameField.setText(this.kagerowScriptAccessor.getRawName());
				caret = Math.min(caret, scriptNameField.getDocument().getLength());
				scriptNameField.setCaretPosition(caret);
			}
			{
				int caret = sucriptSummaryArea.getCaretPosition();
				sucriptSummaryArea.setText(this.kagerowScriptAccessor.getRawSummary());
				caret = Math.min(caret, sucriptSummaryArea.getDocument().getLength());
				sucriptSummaryArea.setCaretPosition(caret);
			}
			{
				int caret = scriptSchemaField.getCaretPosition();
				scriptSchemaField.setText(this.kagerowScriptAccessor.getRawSchema());
				caret = Math.min(caret, scriptSchemaField.getDocument().getLength());
				scriptSchemaField.setCaretPosition(caret);
			}
			// テーブル初期値を指定（環境変数）
			envTable.setModel(new tableModel(this.kagerowScriptAccessor.getEnv()));
			// それぞれのカラムにおける幅を調整
			envTable.getColumnModel().getColumn(0).setPreferredWidth(150);
			envTable.getColumnModel().getColumn(1).setPreferredWidth(300);
			// テーブル初期値を指定（キャッシュ）
			cacheTable.setModel(new cacheTableModel());
			// それぞれのカラムにおける幅を調整
			cacheTable.getColumnModel().getColumn(0).setPreferredWidth(150);
			cacheTable.getColumnModel().getColumn(1).setPreferredWidth(300);
			// キャッシュが作成済みの場合キャッシュ作成ボタンを更新へ変更
			if (KagerowScriptAccessor.isCached(kagerowScriptAccessor)) {
				cacheCreateBtn.setText(GUIText.ScriptPanel_005.toString());
			}
		} finally {
			// リフレッシュフラグオフ
			refreshFlag.set(false);
		}

	}

	/**
	 * 現在操作中のモデルを同期的に支配下にバインドします
	 * @param session セッション
	 */
	synchronized void setSession(KagerowExecutionPlanAccessor session) {
		// セッション最新化
		this.session = session;
		if (Objects.nonNull(session)) {
			// キャッシュ作成/更新ボタン有効化
			this.cacheCreateBtn.setEnabled(true);
		} else {
			// セッションがnullの場合
			// キャッシュ作成/更新ボタン無効化
			this.cacheCreateBtn.setEnabled(false);
		}
		// テーブル初期値を指定（キャッシュ）
		cacheTable.setModel(new cacheTableModel());
		// それぞれのカラムにおける幅を調整
		cacheTable.getColumnModel().getColumn(0).setPreferredWidth(150);
		cacheTable.getColumnModel().getColumn(1).setPreferredWidth(300);
	}

}

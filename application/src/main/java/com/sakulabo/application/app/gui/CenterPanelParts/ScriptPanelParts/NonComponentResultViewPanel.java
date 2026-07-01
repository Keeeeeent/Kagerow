package com.sakulabo.application.app.gui.CenterPanelParts.ScriptPanelParts;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.sql.rowset.CachedRowSet;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;

import com.sakulabo.application.app.gui.AppPanel;
import com.sakulabo.application.common.adapter.GUIExecutionPlanAdapter;
import com.sakulabo.application.common.code.ApplicationConstProperty;
import com.sakulabo.application.common.code.GUIText;
import com.sakulabo.application.common.code.NonComponentResultViewPanelText;
import com.sakulabo.application.common.mixin.ActionListenerMixin;
import com.sakulabo.application.common.mixin.AppMixin;
import com.sakulabo.application.common.mixin.JButtonMixin;
import com.sakulabo.application.common.mixin.JDialogMixin;
import com.sakulabo.application.common.mixin.JFileChooserMixin;
import com.sakulabo.application.common.mixin.JLabelMixin;
import com.sakulabo.application.common.mixin.JMenuItemMixin;
import com.sakulabo.application.common.mixin.JPanelMixin;
import com.sakulabo.application.common.mixin.JTabbedPaneMixin;
import com.sakulabo.application.common.mixin.JToolBarMixin;
import com.sakulabo.application.controller.Context.PluginContextController;
import com.sakulabo.application.controller.Script.KSQLController;
import com.sakulabo.application.helper.AcceleratorHelper;
import com.sakulabo.application.helper.AcceleratorHelper.ShortcutKey;
import com.sakulabo.application.helper.DialogHelper;
import com.sakulabo.application.helper.KagerowScriptAccessorHelper;
import com.sakulabo.application.model.Script.KSQLScriptModel;
import com.sakulabo.core.Kagerow.Exception.AppLogicException;
import com.sakulabo.core.Kagerow.Exception.KSQLExecuteException;
import com.sakulabo.core.Kagerow.Exception.KSQLParseException;
import com.sakulabo.core.Kagerow.Exception.KagerowExecuteException;
import com.sakulabo.core.Kagerow.Exception.KagerowExecuteException.CommandException;
import com.sakulabo.core.Kagerow.Exception.KagerowExecuteException.KsqlException;
import com.sakulabo.core.Kagerow.Exception.KagerowValidationError;
import com.sakulabo.core.Kagerow.Utilities.KagerowExecutionPlanAccessor;
import com.sakulabo.core.Kagerow.Utilities.KagerowFileState;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;
import com.sakulabo.core.Kagerow.Utilities.KagerowUtilities;
import com.sakulabo.regulation.spi.PluginAdapter;

/**
 * GUIアプリケーションのスクリプト実行結果表示パネル
 * 
 * @author keeeeeent
 */
@JToolBarMixin.Setting()
@JPanelMixin.Setting(layout = JPanelMixin.Layout.BorderLayout, backgroudColor = 0xffffff)
@JTabbedPaneMixin.Setting(setTabLayoutPolicy = JTabbedPane.SCROLL_TAB_LAYOUT)
public final class NonComponentResultViewPanel extends AppPanel
		implements JPanelMixin, JButtonMixin, JToolBarMixin, JDialogMixin, JTabbedPaneMixin {

	// ####################################################################################
	// # 共通内部クラス定義
	// ####################################################################################

	/**
	 * 各種パネル向けの基底クラスです
	 */
	@AppMixin.PreferredSize(width = 330, height = 20)
	private abstract class commonPanel extends JPanel
			implements JButtonMixin, JLabelMixin, JFileChooserMixin {

		/** 編集用プラグインパラメータ格納メモリ */
		protected Map<String, String> pluginParam = NonComponentResultViewPanel.this.pluginParam;

		/** 出力先パス */
		protected volatile String outputPath;
		/** 出力先パス受け取りテキストフィールド */
		protected JTextField outputPathField = new JTextField(40);

		// ####################################################################################
		// # 共通内部クラス定義(保存ボタン)
		// ####################################################################################

		/** 保存ボタン */
		@JButtonMixin.Setting(title = GUIText.NonComponentResultViewPanel_004, actionCommand = COMMON_SAVE_PATH_CMD)
		private JButton savePathBtn = new JButton();
		{
			try {
				setJButton(this, savePathBtn);
			} catch (IllegalAccessException | NoSuchMethodException e) {
				KagerowLogger.newAppLogger().err(e);
			}
		}

		// ####################################################################################
		// # 共通内部クラス定義(日付フォーマット)
		// ####################################################################################

		/** 日付コンボボックス */
		protected JComboBox<String> dateFormatBox = new JComboBox<>();
		{
			dateFormatBox.addItem("YYYY-MM-dd");
			setPreferredSize(dateFormatBox);
		}

		/** 日付ラベル */
		@JLabelMixin.Setting(text = GUIText.NonComponentResultViewPanel_009, width = 150, heigth = 20)
		protected JLabel dateFormatLabel = new JLabel();
		/** 日付フォーマット操作パネル */
		protected JPanel dateFormatJPanel = new JPanel();
		{
			// ラベル設定
			setJLabel(dateFormatLabel);
			// レイアウト設定
			dateFormatJPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
			// コンポーネント配置
			dateFormatJPanel.add(dateFormatLabel);
			dateFormatJPanel.add(dateFormatBox);
		}

		// ####################################################################################
		// # 共通内部クラス定義(文字コード)
		// ####################################################################################

		/** 文字コードコンボボックス */
		protected JComboBox<String> charsetBox = new JComboBox<>();
		{
			setPreferredSize(charsetBox);
			for (Map.Entry<String, Charset> charsetEntry : Charset.availableCharsets().entrySet()) {
				charsetBox.addItem(charsetEntry.getValue().name());
			}
		}

		/** 文字コードラベル */
		@JLabelMixin.Setting(text = GUIText.NonComponentResultViewPanel_010, width = 150, heigth = 20)
		protected JLabel charsetLabel = new JLabel();
		/** 文字コード操作パネル */
		protected JPanel charsetJPanel = new JPanel();
		{
			// ラベル設定
			setJLabel(charsetLabel);
			// レイアウト設定
			charsetJPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
			// コンボボックス初期値を指定
			charsetBox.setSelectedItem(StandardCharsets.UTF_8.toString());
			// コンポーネント配置
			charsetJPanel.add(charsetLabel);
			charsetJPanel.add(charsetBox);
		}

		// ####################################################################################
		// # 共通内部クラス定義(出力対象)
		// ####################################################################################

		/** 出力対象コンボボックス */
		protected JComboBox<String> outputTargetBox = new JComboBox<>();
		{
			setPreferredSize(outputTargetBox);
			if (result.isEmpty()) {
				outputTargetBox.setEnabled(false);
			} else {
				for (Map.Entry<String, CachedRowSet> entry : result.entrySet()) {
					outputTargetBox.addItem(entry.getKey());
				}
			}
		}

		/** 出力対象ラベル */
		@JLabelMixin.Setting(text = GUIText.NonComponentResultViewPanel_011, width = 150, heigth = 20)
		protected JLabel outputTargetLabel = new JLabel();
		/** 出力対象操作パネル */
		protected JPanel outputTargetJPanel = new JPanel();
		{
			// ラベル設定
			setJLabel(outputTargetLabel);
			// レイアウト設定
			outputTargetJPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
			// コンポーネント配置
			outputTargetJPanel.add(outputTargetLabel);
			outputTargetJPanel.add(outputTargetBox);
		}

		// ####################################################################################
		// # 共通内部クラス定義(出力オプション詳細)
		// ####################################################################################

		/** エスケープラベル */
		@JLabelMixin.Setting(text = GUIText.NonComponentResultViewPanel_012, width = 100, heigth = 20)
		protected JLabel escapeLabel = new JLabel();
		/** ヘッダーラベル */
		@JLabelMixin.Setting(text = GUIText.NonComponentResultViewPanel_013, width = 100, heigth = 20)
		protected JLabel headerLabel = new JLabel();
		/** エンプティーラベル */
		@JLabelMixin.Setting(width = 150, heigth = 20)
		protected JLabel emptyLabel = new JLabel();
		/** エスケープチェックボックス */
		protected JCheckBox escapeCheckBox = new JCheckBox();
		/** ヘッダーチェックボックス */
		protected JCheckBox headerCheckBox = new JCheckBox();

		// ####################################################################################
		// # 共通内部クラス定義(操作ボタン)
		// ####################################################################################

		/** 取り消しボタン */
		@JButtonMixin.Setting(title = GUIText.NonComponentResultViewPanel_007, actionCommand = COMMON_CLOSE_PANEL_CMD)
		protected JButton cancelBtn = new JButton();
		/** 出力ボタン */
		@JButtonMixin.Setting(title = GUIText.NonComponentResultViewPanel_008, actionCommand = COMMON_EXE_OUTPUT_CMD)
		protected JButton outputExeBtn = new JButton();

		// ####################################################################################
		// # 共通内部クラス定義(パネル定義)
		// ####################################################################################

		/** オプション選択パネル */
		protected JPanel selectOptionJPanel = new JPanel();
		{
			// ラベル設定
			setJLabel(emptyLabel);
			setJLabel(escapeLabel);
			setJLabel(headerLabel);
			// レイアウト設定
			selectOptionJPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
			// コンポーネント配置
			selectOptionJPanel.add(emptyLabel);
			selectOptionJPanel.add(escapeLabel);
			selectOptionJPanel.add(escapeCheckBox);
			selectOptionJPanel.add(headerLabel);
			selectOptionJPanel.add(headerCheckBox);
		}

		/** オプションパネル */
		protected JPanel optionJPanel = new JPanel();
		{
			// ボーダ作成
			optionJPanel.setBorder(BorderFactory.createTitledBorder(
					BorderFactory.createEtchedBorder(),
					GUIText.NonComponentResultViewPanel_006.toString()));
			// レイアウト設定
			optionJPanel.setLayout(new GridLayout(4, 1));
			// コンポーネント配置
			optionJPanel.add(dateFormatJPanel);
			optionJPanel.add(charsetJPanel);
			optionJPanel.add(outputTargetJPanel);
			optionJPanel.add(selectOptionJPanel);
		}

		/** 出力パネル */
		protected JPanel outputJpanel = new JPanel();
		{
			// ボーダ作成
			outputJpanel.setBorder(BorderFactory.createTitledBorder(
					BorderFactory.createEtchedBorder(),
					GUIText.NonComponentResultViewPanel_005.toString()));
			// レイアウト設定
			outputJpanel.setLayout(new FlowLayout());
			// サイズ指定
			outputJpanel.setSize(this.getWidth(), this.getHeight() / 3);
			// コンポーネント配置
			outputJpanel.add(outputPathField);
			outputJpanel.add(savePathBtn);
		}

		/** ボタンパネル */
		protected JPanel buttonJpanel = new JPanel();
		{
			// ボタン設定
			try {
				setJButton(this, cancelBtn);
				setJButton(this, outputExeBtn);
			} catch (IllegalAccessException | NoSuchMethodException e) {
				KagerowLogger.newAppLogger().err(e);
			}
			// レイアウト設定
			buttonJpanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
			// コンポーネント配置
			buttonJpanel.add(cancelBtn);
			buttonJpanel.add(Box.createHorizontalStrut(20));
			buttonJpanel.add(outputExeBtn);
		}

		// ####################################################################################
		// # 共通内部クラス定義(共通処理)
		// ####################################################################################

		/**
		 * 共通コンストラクタ
		 */
		protected commonPanel() {
			setJPanel(this);
			setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
			setLayout(new BorderLayout());
			add(outputJpanel, BorderLayout.NORTH);
			add(optionJPanel, BorderLayout.CENTER);
			add(buttonJpanel, BorderLayout.SOUTH);
		}

		/**
		 * 出力ボタン処理
		 */
		protected void updateParam() {
			pluginParam.put(DATA_FORMAT_KEY, Objects.toString(dateFormatBox.getSelectedItem()));
			pluginParam.put(OUTPUT_PATH_KEY, outputPathField.getText());
			pluginParam.put(IS_ESCAPE_KEY, Boolean.toString(escapeCheckBox.isSelected()));
			pluginParam.put(IS_HEADER_KEY, Boolean.toString(headerCheckBox.isSelected()));
			pluginParam.put(CAHRSET_KEY, Objects.toString(charsetBox.getSelectedItem()));
			pluginParam.put(KSQL_ID_KEY, Objects.toString(outputTargetBox.getSelectedItem()));
		}

		/**
		 * 取消ボタン処理
		 */
		protected abstract void close();

		/**
		 * 出力先選択処理
		 */
		protected void savePathBtn() {
			// ファイルダイアログ表示
			JFileChooser fileChooser = createJFileChooser();
			// ファイルダイアログ表示
			int opneOption = fileChooser.showSaveDialog(dialogHelper.getParent());
			// 単一ファイル選択
			saveFile: if (JFileChooser.APPROVE_OPTION == opneOption) {
				// 保存先取得
				File file = fileChooser.getSelectedFile();
				outputPath = file.toPath().toString();
				// 保存先が既に存在するファイルの場合、上書き確認ダイアログを出力する
				if (Files.exists(file.toPath())) {
					boolean isChoice = dialogHelper.showChoiceDialog(
							NonComponentResultViewPanelText.CONFIRMATION_OUTPUT.toString(),
							NonComponentResultViewPanelText.ALREADY_EXISTS_OUTPUT.toString(new Object[] {
									outputPath
							}));
					if (!isChoice) {
						// 選択しなかった場合処理をここで中断
						break saveFile;
					}
				}
				// ファイルが選択された場合、インプットボックスにパスをデータとして格納
				SwingUtilities.invokeLater(() -> outputPathField.setText(outputPath));
			}
		}

	}

	// ####################################################################################
	// # 共通定数
	// ####################################################################################

	/** アクションコマンド（スクリプト実行） */
	private static final String COMMON_EXE_CMD = "exeBtn";
	/** アクションコマンド（CSV出力画面表示） */
	private static final String COMMON_CSV_CMD = "csvBtn";
	/** アクションコマンド（TSV出力画面表示） */
	private static final String COMMON_TSV_CMD = "tsvBtn";
	/** アクションコマンド（ファイル保存先ダイアログ表示） */
	private static final String COMMON_SAVE_PATH_CMD = "savePathBtn";
	/** アクションコマンド（出力画面クローズ） */
	private static final String COMMON_CLOSE_PANEL_CMD = "closeBtn";
	/** アクションコマンド（出力実行） */
	private static final String COMMON_EXE_OUTPUT_CMD = "outputExeBtn";
	/** スクリプトモデル */
	private final KSQLScriptModel model;
	/** プラグイン（日付フォーマットキー） */
	private static final String DATA_FORMAT_KEY = "DateFormat";
	/** プラグイン（出力先キー） */
	private static final String OUTPUT_PATH_KEY = "OutputPath";
	/** プラグイン（エスケープフラグキー） */
	private static final String IS_ESCAPE_KEY = "IsEscape";
	/** プラグイン（ヘッダーフラグキー） */
	private static final String IS_HEADER_KEY = "IsHeader";
	/** プラグイン（文字コードキー） */
	private static final String CAHRSET_KEY = "Charset";
	/** プラグイン（出力対象キー） */
	private static final String KSQL_ID_KEY = "KsqlId";

	/** 同時実行制御フラグメモリ */
	private static final AtomicBoolean EXECUTE_FLUG = new AtomicBoolean();
	/** スクリプト編集パネル参照メモリ */
	private final NonComponentScriptEditPanel scriptEditPanel;

	// ####################################################################################
	// # 共通変数
	// ####################################################################################

	/** 実行SQL結果一覧一覧（実行順序順） */
	private volatile Map<String, CachedRowSet> result = new HashMap<>();
	/** 実行結果格納メモリ */
	private KagerowExecutionPlanAccessor planAccessor;
	/** プラグインパラメータ格納メモリ */
	private Map<String, String> pluginParam;

	/** 出力系処理実行可能制御フラグ */
	private final AtomicBoolean outputExeFlug = new AtomicBoolean(false);

	// ####################################################################################
	// # 変数コンポーネント
	// ####################################################################################

	/** JTree格納先 */
	private volatile JTabbedPane tabbedPane = new JTabbedPane();

	// ####################################################################################
	// # 定数コンポーネント（GUI部品）
	// ####################################################################################

	/** ルートパネル */
	private final JPanel rootPanel = new JPanel();
	{
		setJPanel(rootPanel);
	}

	/** 実行結果パネル */
	private final JPanel tablePanel = new JPanel();
	{
		setJPanel(tablePanel);
		rootPanel.add(tablePanel, BorderLayout.CENTER);
	}

	/** ツールバーボタン（スクリプト実行） */
	@JButtonMixin.Setting(title = GUIText.NonComponentResultViewPanel_001, actionCommand = COMMON_EXE_CMD)
	private final JButton exeBtn = new JButton();
	/** ツールバーボタン（CSV出力） */
	@JButtonMixin.Setting(title = GUIText.NonComponentResultViewPanel_002, actionCommand = COMMON_CSV_CMD)
	private final JButton csvBtn = new JButton();
	/** ツールバーボタン（TSV出力） */
	@JButtonMixin.Setting(title = GUIText.NonComponentResultViewPanel_003, actionCommand = COMMON_TSV_CMD)
	private final JButton tsvBtn = new JButton();

	/** 実行時間表示ラベル */
	private final JLabel jLabel = new JLabel(NonComponentResultViewPanelText.MEASURE_LABEL.toString());

	/** ツールバー */
	private final JToolBar toolBar;
	{
		toolBar = createJToolBar();
		rootPanel.add(toolBar, BorderLayout.NORTH);
	}

	// ####################################################################################
	// # 定数コンポーネント（DI部品）
	// ####################################################################################

	/** KSQL実行コントローラー */
	private final KSQLController ksqlController;
	{
		// コントローラー初期化
		Optional<KSQLController> ksqlController = KagerowUtilities.getBean(KSQLController.class, null);
		this.ksqlController = ksqlController.get();
	}

	/** プラグインコントローラー */
	private final PluginContextController pluginContextController;
	{
		// コントローラー初期化
		Optional<PluginContextController> pluginContextController = KagerowUtilities
				.getBean(PluginContextController.class, null);
		this.pluginContextController = pluginContextController.get();
	}

	/** スクリプトヘルパー */
	private final KagerowScriptAccessorHelper accessorHelper;
	{
		// ヘルパー初期化(KagerowScriptAccessorHelper)
		Optional<KagerowScriptAccessorHelper> accessorHelper = KagerowUtilities
				.getBean(KagerowScriptAccessorHelper.class, null);
		this.accessorHelper = accessorHelper.get();
	}

	/** ダイアログヘルパー */
	private final DialogHelper dialogHelper;
	{
		// ヘルパー初期化(DialogHelper)
		Optional<DialogHelper> dialogHelper = KagerowUtilities.getBean(DialogHelper.class, null);
		this.dialogHelper = dialogHelper.get();
	}

	/**
	 * 数値専用JTable向けレンダラー
	 */
	public static class NumberRenderer extends DefaultTableCellRenderer {

		/** 数値向けフォーマッター */
		private static final DecimalFormat df = new DecimalFormat("0.################");
		static {
			df.setGroupingUsed(false);
		}

		/** {@inheritDoc} */
		@Override
		protected void setValue(Object value) {
			if (value instanceof Number num) {
				super.setValue(df.format(num));
			} else {
				super.setValue(value);
			}
		}
	}

	// ####################################################################################
	// # 共通処理
	// ####################################################################################

	/**
	 * 共通初期化処理（オブザーバー登録）
	 */
	{
		// オブザーバー登録
		addObserver(new ButtonObserver());
		// オブザーバー通知
		noticeObserver();
	}

	/**
	 * 共通初期化処理（ショートカットキー登録）
	 */
	{
		KagerowUtilities.getBean(AcceleratorHelper.class, null)
				.ifPresent(h -> h.setShortcut(ShortcutKey.EXECUT_SCRIPT, this));
	}

	/**
	 * デフォルトコンストラクタ
	 * @param model スクリプトモデル
	 * @param scriptEditPanel スクリプト編集パネル
	 */
	NonComponentResultViewPanel(KSQLScriptModel model, NonComponentScriptEditPanel scriptEditPanel) {

		// フィールド初期化
		this.model = model;
		this.scriptEditPanel = scriptEditPanel;

		// ボタンの初期化
		try {
			setJButton(this, exeBtn);
			setJButton(this, csvBtn);
			setJButton(this, tsvBtn);
		} catch (IllegalAccessException | NoSuchMethodException e) {
			KagerowLogger.newAppLogger().err(e);
		}

		// UI初期化
		initialize();
	}

	/** {@inheritDoc} */
	@Override
	public Component getComponent() {
		return rootPanel;
	}

	/**
	 * 実行結果受け取りメソッド
	 * @param result 実行結果
	 */
	public void setData(Map<String, CachedRowSet> result) {
		// 既存状態リフレッシュ
		this.result = result;
		tablePanel.removeAll();
		// テーブルUI初期化
		tabbedPane = createTabbedPane();
		// 実行順序順に表示できるよう一度リストへ変換
		List<Map.Entry<String, CachedRowSet>> resultList = new ArrayList<>(result.entrySet());
		for (int i = resultList.size() - 1; 0 <= i; i--) {
			// 結果を取得
			Map.Entry<String, CachedRowSet> data = resultList.get(i);
			try {
				// JTable作成
				TableModel model = new NonComponentRowSetTableModel(data.getValue());
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
				// SQL名称取得
				String dataName = accessorHelper.getName(this.model.scriptAccessor, data.getKey());
				// タブへ追加
				tabbedPane.add(dataName, scrollPane);
			} catch (SQLException e) {
				// ログ出力
				KagerowLogger.newAppLogger().err(e);
				dialogHelper.showSystemError(NonComponentResultViewPanelText.DISPLAY_FAILURE
						.toString(new Object[] { data.getKey() }));
			}
		}
		// テーブルUI追加
		tablePanel.add(tabbedPane);
	}

	/** {@inheritDoc} */
	@Override
	public Component[] getLeftComponent() {
		return new Component[] {
				exeBtn,
				csvBtn,
				tsvBtn
		};
	}

	/** {@inheritDoc} */
	@Override
	public Component getRightComponent() {
		return jLabel;
	}

	/** {@inheritDoc} */
	@Override
	public Frame getParentFrame() {
		return dialogHelper.getParent();
	}

	// ####################################################################################
	// # 実行ボタン
	// ####################################################################################

	/**
	 * スクリプト実行ボタン押下後処理メソッド
	 */
	@ActionListenerMixin.ActionCommand(COMMON_EXE_CMD)
	public void exeBtn() {

		// スクリプトが実行可能か確認
		if (!KagerowFileState.Ready.equals(model.scriptAccessor.getState())) {
			// ダイアログ表示
			dialogHelper.showSystemWarning(NonComponentResultViewPanelText.NOT_SAVE_SCRIPT.toString());
			return;
		}

		// 同時実行制御
		if (EXECUTE_FLUG.compareAndExchange(false, true)) {
			return;
		}

		SwingUtilities.invokeLater(() -> {
			// ボタン非活性化
			exeBtn.setEnabled(false);
			csvBtn.setEnabled(false);
			tsvBtn.setEnabled(false);
			// 出力可能フラグを折る
			outputExeFlug.set(false);
		});

		// 処理実行
		Thread.startVirtualThread(
				() -> {
					try {

						if (Objects.isNull(planAccessor)) {
							// 初回実行
							planAccessor = ksqlController.executionScript(model);
						} else {
							planAccessor = ksqlController.executionScript(model, planAccessor);
						}

						// 結果セット表示
						setData(planAccessor.getCurrentHistory().currentRowSet());

						SwingUtilities.invokeLater(() -> {
							// 実行時間更新
							jLabel.setText(((GUIExecutionPlanAdapter) model.planAdapter).calcTime());
						});

						// 出力可能フラグを立てる
						outputExeFlug.set(true);

					} catch (KagerowValidationError e) {

						// ログ出力
						KagerowLogger.newAppLogger().err(e);

						// 原因例外を取得
						Throwable error = e.getCause();

						/** プラグインバリデーションエラー */
						if (error instanceof PluginAdapter.PluginValidationException validError) {
							// ダイアログ表示
							dialogHelper.showValidationError("""
									PluginID   : %s
									Name       : %s

									%s""".formatted(
									e.getId(),
									e.getName(),
									validError.getMessage()));
						} else if (error instanceof KSQLParseException parseError) {
							// ダイアログ表示
							dialogHelper.showValidationError("""
									KSQLID   : %s
									Name     : %s

									%s""".formatted(
									e.getId(),
									e.getName(),
									parseError.getMessage()));
						} else {
							// ダイアログ表示
							dialogHelper.showSystemError(NonComponentResultViewPanelText.ERROR_SCRIPT.toString());
						}

						// 実行時間更新
						SwingUtilities.invokeLater(() -> {
							jLabel.setText(NonComponentResultViewPanelText.FAIL_LABEL.toString());
						});
					} catch (KsqlException e) {
						if (e.getCause() instanceof KSQLExecuteException exp) {
							/** SQL実行例外 */
							// ログ出力
							KagerowLogger.newAppLogger().err(e);
							// ダイアログ表示
							dialogHelper.showSQLError("""
									KSQLID   : %s
									Name     : %s

									%s""".formatted(
									exp.getId(),
									exp.getName(),
									e.getMessage()));
							// 実行時間更新
							SwingUtilities.invokeLater(() -> {
								jLabel.setText(NonComponentResultViewPanelText.FAIL_LABEL.toString());
							});
						} else {
							/** 予期しない例外 */
							// ログ出力
							KagerowLogger.newAppLogger().err(e);
							// ダイアログ表示
							dialogHelper.showSystemError(NonComponentResultViewPanelText.ERROR_SCRIPT.toString());
							// 実行時間更新
							SwingUtilities.invokeLater(() -> {
								jLabel.setText(NonComponentResultViewPanelText.FAIL_LABEL.toString());
							});
						}
					} catch (CommandException e) {
						/** SQL実行例外 */
						// ログ出力
						KagerowLogger.newAppLogger().err(e);
						// ダイアログ表示
						dialogHelper.showCMDError(e);
						// 実行時間更新
						SwingUtilities.invokeLater(() -> {
							jLabel.setText(NonComponentResultViewPanelText.FAIL_LABEL.toString());
						});
					} catch (KagerowExecuteException | AppLogicException e) {
						/** 実行計画による例外 */
						// ログ出力
						KagerowLogger.newAppLogger().err(e);
						// ダイアログ表示
						dialogHelper.showSystemError(e.getMessage());
						// 実行時間更新
						SwingUtilities.invokeLater(() -> {
							jLabel.setText(NonComponentResultViewPanelText.FAIL_LABEL.toString());
						});
					} catch (Exception e) {
						/** 予期しない例外 */
						// ログ出力
						KagerowLogger.newAppLogger().err(e);
						// ダイアログ表示
						dialogHelper.showSystemError(NonComponentResultViewPanelText.ERROR_SCRIPT.toString());
						// 実行時間更新
						SwingUtilities.invokeLater(() -> {
							jLabel.setText(NonComponentResultViewPanelText.FAIL_LABEL.toString());
						});
					} finally {
						// セッション最新化
						scriptEditPanel.sqlEditPanel.setSession(planAccessor);
						scriptEditPanel.commonEditPanel.setSession(planAccessor);
						// オブザーバー通知
						noticeObserver();
						// 排他制御解放
						EXECUTE_FLUG.set(false);
					}
				});

	}

	// ####################################################################################
	// # CSV処理
	// ####################################################################################

	/**
	 * CSV出力実行ボタン押下後処理メソッド
	 */
	@ActionListenerMixin.ActionCommand(COMMON_CSV_CMD)
	private void csvBtn() {

		// ボタン非活性化
		SwingUtilities.invokeLater(() -> {
			csvBtn.setEnabled(false);
		});

		// 処理実行
		Thread.startVirtualThread(
				() -> {
					try {
						// バインド元のマップを生成
						pluginParam = pluginContextController.createCSVPluginParam();
						// ダイアログ出力
						JDialog dialog = createJDialog(COMMON_CSV_CMD, GUIText.NonComponentResultViewPanel_002);
						dialog.setVisible(true);
					} catch (Throwable e) {
						// ログ出力
						KagerowLogger.newAppLogger().err(e);
						dialogHelper.showSystemError(NonComponentResultViewPanelText.FAIL_OUTPUT.toString(new Object[] {
								ApplicationConstProperty.CSV_STR
						}));
					} finally {
						// オブザーバー通知
						noticeObserver();
					}
				});

	}

	/**
	 * CSVオプションパネルを生成します
	 * @param dialog ダイアログインスタンス
	 * @return CSVオプションパネル
	 */
	@JDialogMixin.Setting(COMMON_CSV_CMD)
	private JPanel createCSVPanel(JDialog dialog) {
		return new CSVOutputSettingPanel(dialog);
	}

	/**
	 * CSV出力設定パネル
	 */
	private class CSVOutputSettingPanel extends commonPanel {

		/** ダイアログインスタンス */
		JDialog dialog;

		/**
		 * デフォルトコンストラクタ
		 * @param dialog ダイアログインスタンス
		 */
		CSVOutputSettingPanel(JDialog dialog) {
			this.dialog = dialog;
		}

		/** {@inheritDoc} */
		@Override
		@JMenuItemMixin.ActionCommand(COMMON_CLOSE_PANEL_CMD)
		protected void close() {
			dialog.dispose();
		}

		/**
		 * 出力先保管
		 */
		@JMenuItemMixin.ActionCommand(COMMON_SAVE_PATH_CMD)
		private void output() {
			super.savePathBtn();
		}

		/**
		 * 出力処理実行
		 */
		@JMenuItemMixin.ActionCommand(COMMON_EXE_OUTPUT_CMD)
		private void outputExe() {
			super.updateParam();
			try {
				// バインドするマップを生成
				pluginParam = pluginContextController.bindCSVPluginParam(pluginParam);
				// プラグイン実行
				pluginContextController.outputCSV(model, pluginParam, result);
				// 完了メッセージ表示
				dialogHelper.showSystemInfo(NonComponentResultViewPanelText.SUCCESS_OUTPUT.toString());
			} catch (Throwable e) {
				// ログ出力
				KagerowLogger.newAppLogger().err(e);
				dialogHelper.showSystemError(NonComponentResultViewPanelText.FAIL_OUTPUT.toString(new Object[] {
						ApplicationConstProperty.CSV_STR
				}));
			} finally {
				dialog.dispose();
			}
		}

		/** {@inheritDoc} */
		@Override
		public File getPath() {
			return Objects.isNull(super.outputPath) ? null : new File(super.outputPath);
		}

	}

	// ####################################################################################
	// # TSV処理
	// ####################################################################################

	/**
	 * TSV出力実行ボタン押下後処理メソッド
	 */
	@ActionListenerMixin.ActionCommand(COMMON_TSV_CMD)
	private void tsvBtn() {

		// ボタン非活性化
		SwingUtilities.invokeLater(() -> {
			tsvBtn.setEnabled(false);
		});

		// 処理実行
		Thread.startVirtualThread(
				() -> {
					try {
						// バインド元のマップを生成
						pluginParam = pluginContextController.createTSVPluginParam();
						// ダイアログ出力
						JDialog dialog = createJDialog(COMMON_TSV_CMD, GUIText.NonComponentResultViewPanel_003);
						dialog.setVisible(true);
					} catch (Throwable e) {
						// ログ出力
						KagerowLogger.newAppLogger().err(e);
						dialogHelper.showSystemError(NonComponentResultViewPanelText.FAIL_OUTPUT.toString(new Object[] {
								ApplicationConstProperty.TSV_STR
						}));
					} finally {
						// オブザーバー通知
						noticeObserver();
					}
				});
	}

	/**
	 * TSVオプションパネルを生成します
	 * @param dialog ダイアログインスタンス
	 * @return TSVオプションパネル
	 */
	@JDialogMixin.Setting(COMMON_TSV_CMD)
	private JPanel createTSVPanel(JDialog dialog) {
		return new TSVOutputSettingPanel(dialog);
	}

	/**
	 * TSV出力設定パネル
	 */
	private class TSVOutputSettingPanel extends commonPanel {

		/** ダイアログインスタンス */
		JDialog dialog;

		/**
		 * デフォルトコンストラクタ
		 * @param dialog ダイアログインスタンス
		 */
		TSVOutputSettingPanel(JDialog dialog) {
			this.dialog = dialog;
		}

		/** {@inheritDoc} */
		@Override
		@JMenuItemMixin.ActionCommand(COMMON_CLOSE_PANEL_CMD)
		protected void close() {
			dialog.dispose();
		}

		/**
		 * 出力先保管
		 */
		@JMenuItemMixin.ActionCommand(COMMON_SAVE_PATH_CMD)
		private void output() {
			super.savePathBtn();
		}

		/**
		 * 出力処理実行
		 */
		@JMenuItemMixin.ActionCommand(COMMON_EXE_OUTPUT_CMD)
		private void outputExe() {
			super.updateParam();
			try {
				// バインドするマップを生成
				pluginParam = pluginContextController.bindTSVPluginParam(pluginParam);
				// プラグイン実行
				pluginContextController.outputTSV(model, pluginParam, result);
				// 完了メッセージ表示
				dialogHelper.showSystemInfo(NonComponentResultViewPanelText.SUCCESS_OUTPUT.toString());
			} catch (Throwable e) {
				// ログ出力
				KagerowLogger.newAppLogger().err(e);
				dialogHelper.showSystemError(NonComponentResultViewPanelText.FAIL_OUTPUT.toString(new Object[] {
						ApplicationConstProperty.TSV_STR
				}));
			} finally {
				dialog.dispose();
			}
		}

		/** {@inheritDoc} */
		@Override
		public File getPath() {
			return Objects.isNull(super.outputPath) ? null : new File(super.outputPath);
		}

	}

	// ####################################################################################
	// # オブザーバー定義
	// ####################################################################################

	/**
	 * ボタン制御オブザーバー
	 */
	private class ButtonObserver implements Runnable {

		/** {@inheritDoc} */
		@Override
		public void run() {
			// スクリプト実行ボタンはオブザーバー側では常に有効化する
			NonComponentResultViewPanel.this.exeBtn.setEnabled(true);
			// スクリプト実行不可の場合、出力系ボタンを非活性化
			SwingUtilities.invokeLater(() -> {
				boolean flug = outputExeFlug.get();
				NonComponentResultViewPanel.this.csvBtn.setEnabled(flug);
				NonComponentResultViewPanel.this.tsvBtn.setEnabled(flug);
			});
		}

	}

}

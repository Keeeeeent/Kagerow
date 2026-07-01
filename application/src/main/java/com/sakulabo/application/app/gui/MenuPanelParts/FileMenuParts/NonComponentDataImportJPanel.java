package com.sakulabo.application.app.gui.MenuPanelParts.FileMenuParts;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.sakulabo.application.common.code.GUIText;
import com.sakulabo.application.common.code.NonComponentDataImportJPanelText;
import com.sakulabo.application.common.mixin.AppMixin;
import com.sakulabo.application.common.mixin.JButtonMixin;
import com.sakulabo.application.common.mixin.JLabelMixin;
import com.sakulabo.application.common.mixin.JMenuItemMixin;
import com.sakulabo.application.common.mixin.JPanelMixin;
import com.sakulabo.application.controller.Data.DataController;
import com.sakulabo.application.helper.DialogHelper;
import com.sakulabo.application.model.Data.DataImportModel;
import com.sakulabo.core.Kagerow.Utilities.KagerowChunkCreater.ChunkCreateMode;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;
import com.sakulabo.core.Kagerow.Utilities.KagerowUtilities;

/**
 * データインポートボタンダイアログJPanel実装クラスです
 * 
 * @author keeeeeent
 */
@AppMixin.PreferredSize(width = 330, height = 20)
public class NonComponentDataImportJPanel extends JPanel implements JPanelMixin, JButtonMixin, JLabelMixin {

	/** テキストフィールド初期カラム数 */
	private static final int COLUMNS = 30;

	/** データインポートモデル */
	private final DataImportModel model;
	/** ダイアログインスタンス */
	private final JDialog dialog;
	/** データコントローラー */
	private final DataController dataController;
	/** ダイアログヘルパー */
	private final DialogHelper dialogHelper;

	/** 同時実行制御ロックインスタンス */
	private final ReentrantLock lock = new ReentrantLock();

	// ####################################################################################
	// # クラス定義(シノニムパネル)
	// ####################################################################################

	/** シノニムテキストフィールド */
	private JTextField synonymTextField = new JTextField(COLUMNS);

	/** シノニムラベル */
	@JLabelMixin.Setting(text = GUIText.NonComponentDataImportJPanel_003, width = 150, heigth = 20)
	private JLabel synonymLabel = new JLabel();

	/** シノニム操作パネル */
	private JPanel synonymJPanel = new JPanel();
	{
		// ラベル設定
		setJLabel(synonymLabel);
		// レイアウト設定
		synonymJPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		// コンポーネント配置
		synonymJPanel.add(synonymLabel);
		synonymJPanel.add(synonymTextField);
	}

	// ####################################################################################
	// # クラス定義(スキーマパネル)
	// ####################################################################################

	/** スキーマテキストフィールド */
	private JTextField schemaTextField = new JTextField(COLUMNS);

	/** 文字コードラベル */
	@JLabelMixin.Setting(text = GUIText.NonComponentDataImportJPanel_004, width = 150, heigth = 20)
	private JLabel schemaLabel = new JLabel();

	/** スキーマ操作パネル */
	private JPanel schemaJPanel = new JPanel();
	{
		// ラベル設定
		setJLabel(schemaLabel);
		// レイアウト設定
		schemaJPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		// コンポーネント配置
		schemaJPanel.add(schemaLabel);
		schemaJPanel.add(schemaTextField);
	}

	// ####################################################################################
	// # クラス定義(文字コードパネル)
	// ####################################################################################

	/** 文字コードコンボボックス */
	private JComboBox<String> charsetBox = new JComboBox<>();
	{
		setPreferredSize(charsetBox);
		for (Map.Entry<String, Charset> charsetEntry : Charset.availableCharsets().entrySet()) {
			charsetBox.addItem(charsetEntry.getValue().name());
		}
	}

	/** 文字コードラベル */
	@JLabelMixin.Setting(text = GUIText.NonComponentDataImportJPanel_005, width = 150, heigth = 20)
	private JLabel charsetLabel = new JLabel();

	/** 文字コード操作パネル */
	private JPanel charsetJPanel = new JPanel();
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
	// # クラス定義(実行モードパネル)
	// ####################################################################################

	/** 実行モードコンボボックス */
	private JComboBox<String> executeModeBox = new JComboBox<>();
	{
		setPreferredSize(executeModeBox);
		for (ChunkCreateMode entry : ChunkCreateMode.values()) {
			executeModeBox.addItem(entry.name());
		}
	}

	/** 実行モードラベル */
	@JLabelMixin.Setting(text = GUIText.NonComponentDataImportJPanel_006, width = 150, heigth = 20)
	private JLabel executeModeLabel = new JLabel();

	/** 実行モード操作パネル */
	private JPanel executeModeJPanel = new JPanel();
	{
		// ラベル設定
		setJLabel(executeModeLabel);
		// レイアウト設定
		executeModeJPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		// コンボボックス初期値を指定
		executeModeBox.setSelectedItem(StandardCharsets.UTF_8.toString());
		// コンポーネント配置
		executeModeJPanel.add(executeModeLabel);
		executeModeJPanel.add(executeModeBox);
	}

	// ####################################################################################
	// # クラス定義(出力オプション詳細)
	// ####################################################################################

	/** セキュアラベル */
	@JLabelMixin.Setting(text = GUIText.NonComponentDataImportJPanel_007, width = 100, heigth = 20)
	private JLabel secureLabel = new JLabel();
	/** ヘッダーラベル */
	@JLabelMixin.Setting(text = GUIText.NonComponentDataImportJPanel_008, width = 100, heigth = 20)
	private JLabel headerLabel = new JLabel();
	/** エンプティーラベル */
	@JLabelMixin.Setting(width = 150, heigth = 20)
	private JLabel emptyLabel = new JLabel();
	/** 間隔調整ラベル */
	@JLabelMixin.Setting(width = 40, heigth = 20)
	private JLabel bothLabel = new JLabel();
	/** セキュアチェックボックス */
	private JCheckBox secureCheckBox = new JCheckBox();
	/** ヘッダーチェックボックス */
	private JCheckBox headerCheckBox = new JCheckBox();

	/** オプション選択パネル */
	private JPanel selectOptionJPanel = new JPanel();
	{
		// ラベル設定
		setJLabel(emptyLabel);
		setJLabel(secureLabel);
		setJLabel(headerLabel);
		setJLabel(bothLabel);
		// レイアウト設定
		selectOptionJPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		// コンポーネント配置
		selectOptionJPanel.add(emptyLabel);
		selectOptionJPanel.add(secureLabel);
		selectOptionJPanel.add(secureCheckBox);
		selectOptionJPanel.add(bothLabel);
		selectOptionJPanel.add(headerLabel);
		selectOptionJPanel.add(headerCheckBox);
	}

	// ####################################################################################
	// # クラス定義(ルートパネル)
	// ####################################################################################

	/** アクションコマンド（出力画面クローズ） */
	private static final String COMMON_CLOSE_PANEL_CMD = "closeBtn";
	/** アクションコマンド（出力実行） */
	private static final String COMMON_EXE_IMPORT_CMD = "exeBtn";

	/** 取消ボタン */
	@JButtonMixin.Setting(title = GUIText.NonComponentDataImportJPanel_001, actionCommand = COMMON_CLOSE_PANEL_CMD)
	private JButton cancelBtn = new JButton();
	/** 実行ボタン */
	@JButtonMixin.Setting(title = GUIText.NonComponentDataImportJPanel_002, actionCommand = COMMON_EXE_IMPORT_CMD)
	private JButton outputExeBtn = new JButton();

	/** オプションパネル */
	private JPanel optionJPanel = new JPanel();
	{
		// レイアウト設定
		optionJPanel.setLayout(new GridLayout(5, 1));
		// コンポーネント配置
		optionJPanel.add(synonymJPanel);
		optionJPanel.add(schemaJPanel);
		optionJPanel.add(charsetJPanel);
		optionJPanel.add(executeModeJPanel);
		optionJPanel.add(selectOptionJPanel);
	}

	/** ボタンパネル */
	private JPanel buttonJpanel = new JPanel();
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
	// # クラス定義(本体)
	// ####################################################################################

	/**
	 * デフォルトコンストラクタ
	 * @param model データインポートモデル
	 * @param dialog ダイアログインスタンス
	 */
	public NonComponentDataImportJPanel(DataImportModel model, JDialog dialog) {

		// フィールド初期化
		this.model = model;
		this.dialog = dialog;
		this.dataController = KagerowUtilities.getBean(DataController.class, null).get();
		this.dialogHelper = KagerowUtilities.getBean(DialogHelper.class, null).get();

		// レイアウト設定
		setJPanel(this);
		setBorder(BorderFactory.createEmptyBorder(30, 40, 20, 10));
		setLayout(new BorderLayout());
		add(optionJPanel, BorderLayout.CENTER);
		add(buttonJpanel, BorderLayout.SOUTH);
	}

	/**
	 * 取消しボタン押下時の処理
	 */
	@JMenuItemMixin.ActionCommand(COMMON_CLOSE_PANEL_CMD)
	private void close() {
		// ダイアログクローズ
		dialog.dispose();
		// ロック解放
		lock.unlock();
	}

	/**
	 * 実行ボタン押下時の処理
	 */
	@JMenuItemMixin.ActionCommand(COMMON_EXE_IMPORT_CMD)
	private void output() {
		Thread.ofVirtual().start(() -> {
			// ロック取得
			lock.lock();
			// 進捗更新オブザーバー
			Consumer<Double> observer = dialogHelper.showProgressDialog(
					GUIText.NonComponentDataImportJPanel_009.toString());
			try {
				// モデル更新
				update();
				// ダイアログモデル設定
				model.observer = observer;
				// データインポート処理実行
				dataController.importData(model);
				// 完了メッセージ表示
				SwingUtilities.invokeLater(() -> {
					dialogHelper.showSystemInfo(NonComponentDataImportJPanelText.IMPORT_SUCCESS.toString());
				});
			} catch (Throwable exception) {
				// 進捗更新ダイアログクローズ
				observer.accept(DialogHelper.STOP_PROGRESS_DIALOG);
				// ログ出力
				KagerowLogger.newAppLogger().err(exception);
				SwingUtilities.invokeLater(() -> {
					dialogHelper.showSystemError(NonComponentDataImportJPanelText.IMPROT_FAIL.toString());
				});
			} finally {
				// ダイアログクローズ
				close();
			}
		});
	}

	/**
	 * モデルの状態を更新します
	 */
	private void update() {

		// オブジェクト変換
		ChunkCreateMode mode = null;
		{
			String tmp = (String) executeModeBox.getSelectedItem();
			mode = Enum.valueOf(ChunkCreateMode.class, tmp);
		}

		Charset charset = null;
		{
			String tmp = (String) charsetBox.getSelectedItem();
			charset = Charset.forName(tmp);
		}

		// モデル更新
		model.synonym = synonymTextField.getText();
		model.schema = schemaTextField.getText();
		model.mode = mode;
		model.charset = charset;
		model.isHeader = headerCheckBox.isSelected();
		model.isSecure = secureCheckBox.isSelected();
	}

}

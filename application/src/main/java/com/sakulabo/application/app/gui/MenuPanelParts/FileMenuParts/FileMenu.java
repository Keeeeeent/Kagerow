package com.sakulabo.application.app.gui.MenuPanelParts.FileMenuParts;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.sakulabo.application.app.gui.MainFrame;
import com.sakulabo.application.app.gui.CenterPanelParts.ContextPanelParts.ContextPanel;
import com.sakulabo.application.app.gui.CenterPanelParts.ScriptPanelParts.ScriptPanel;
import com.sakulabo.application.app.gui.MenuPanelParts.AppMenu;
import com.sakulabo.application.common.code.FileMenuText;
import com.sakulabo.application.common.code.GUIText;
import com.sakulabo.application.common.code.JFileChooserMixinProperty;
import com.sakulabo.application.common.initializer.GraphicComponent;
import com.sakulabo.application.common.mixin.JDialogMixin;
import com.sakulabo.application.common.mixin.JFileChooserMixin;
import com.sakulabo.application.common.mixin.JMenuItemMixin;
import com.sakulabo.application.controller.Data.DataController;
import com.sakulabo.application.helper.DialogHelper;
import com.sakulabo.application.model.Data.DataImportModel;
import com.sakulabo.core.Kagerow.Exception.AppLogicException;
import com.sakulabo.core.Kagerow.Exception.KFileParseException;
import com.sakulabo.core.Kagerow.Exception.KSQLParseException;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;
import com.sakulabo.regulation.annotation.KagerowComponent;
import com.sakulabo.regulation.annotation.KagerowInject;
import com.sakulabo.regulation.annotation.UseJITCompiler;
import com.sakulabo.regulation.annotation.UseJITCompiler.JITCompilerOption;

/**
 * ファイルメニュー実装クラスです
 * 
 * @author keeeeeent
 */
@KagerowComponent
@GraphicComponent
@JMenuItemMixin.Setting(title = GUIText.FileMenu_002, actionCommand = FileMenu.ADD_SCRIPT)
@JMenuItemMixin.Setting(title = GUIText.FileMenu_006, actionCommand = FileMenu.EDIT_SCRIPT)
@JMenuItemMixin.Setting(title = GUIText.FileMenu_003, actionCommand = FileMenu.SAVE_SCRIPT)
@JMenuItemMixin.Setting(title = GUIText.FileMenu_004, actionCommand = FileMenu.DATA_IMPORT)
//@JMenuItemMixin.Setting(title = GUIText.FileMenu_005, actionCommand = FileMenu.DATA_EXPORT)
public class FileMenu extends AppMenu implements JMenuItemMixin {

	/** スクリプト追加 */
	public static final String ADD_SCRIPT = "addScript";
	/** スクリプト編集 */
	public static final String EDIT_SCRIPT = "editScript";
	/** スクリプト保存 */
	public static final String SAVE_SCRIPT = "saveScript";
	/** データインポート */
	public static final String DATA_IMPORT = "dataImport";
	/** データエクスポート */
	public static final String DATA_EXPORT = "dataExport";

	/** データインポートパネル */
	private static final String DATA_IMPORT_PANEL = "dataImportPanel";
	/** データインポートパネルモデル */
	private DataImportModel model;

	/** オープンパス（addScript） */
	private volatile Path addScriptPath;
	/** オープンパス（dataImport） */
	private volatile Path dataImportPath;

	/** ロガー */
	@KagerowInject
	private KagerowLogger logger;
	/** スクリプト編集パネル */
	@KagerowInject
	private ScriptPanel scriptPanel;
	/** コンテキストパネル */
	@KagerowInject
	private ContextPanel contextPanel;
	/** メインパネル */
	@KagerowInject
	private MainFrame mainFrame;
	/** ダイアログヘルパー */
	@KagerowInject
	private DialogHelper dialogHelper;
	/** データコントローラー */
	@KagerowInject
	private DataController dataController;

	/**
	 * デフォルトコンストラクタ
	 * @throws IllegalAccessException ハンドラーアクセスエラー
	 * @throws NoSuchMethodException メソッド不明
	 */
	public FileMenu() throws IllegalAccessException, NoSuchMethodException {
		super(GUIText.FileMenu_001);
		setJMenuItem(this);
	}

	/**
	 * GUI描画初期化処理実施
	 */
	public void lazyInitialize() {
		SwingUtilities.invokeLater(this::setMenu);
	}

	/**
	 * メニューアイテムの活性制御を行います
	 */
	public void setMenu() {
		// 選択状態を取得
		boolean isSelected = scriptPanel.isSelected();
		for (int i = 0; i < getItemCount(); i++) {
			// メニュー取得
			JMenuItem item = getItem(i);
			// 取得ができなかった場合、次のループに移行
			if (Objects.isNull(item)) {
				continue;
			}
			// コマンド取得
			String cmd = item.getActionCommand();
			if (SAVE_SCRIPT.equals(cmd)) {
				item.setEnabled(isSelected);
			}
		}
	}

	/**
	 * スクリプト編集ボタン押下時の処理を実行します
	 */
	@JMenuItemMixin.ActionCommand(FileMenu.EDIT_SCRIPT)
	public synchronized void editScript() {
		// ファイルダイアログインスタンス生成
		JFileChooserMixin chooserMixin = new EditScriptJFileChooser();
		JFileChooser fileChooser = chooserMixin.createJFileChooser();
		// ファイルダイアログ表示
		int opneOption = fileChooser.showOpenDialog(mainFrame.frame);
		// 単一ファイル選択
		if (JFileChooser.APPROVE_OPTION == opneOption) {
			File file = fileChooser.getSelectedFile();
			addScriptPath = file.toPath().getParent();
			try {
				// スクリプト保存
				scriptPanel.addScriptEditer(file.toPath());
			} catch (KFileParseException | KSQLParseException | AppLogicException exception) {
				// ログ出力
				logger.err(exception);
				// ダイアログ表示
				dialogHelper.showSystemError(FileMenuText.ERROR_001.toString());
			} catch (IOException exception) {
				// ログ出力
				logger.err(exception);
				// ダイアログ表示
				dialogHelper.showSystemError(FileMenuText.ERROR_002.toString());
			} catch (Exception exception) {
				// ログ出力
				logger.err(exception);
				// ダイアログ表示
				dialogHelper.showSystemError(FileMenuText.ERROR_003.toString());
			}
		}
	}

	/**
	 * スクリプト編集ボタンファイルダイアログ
	 */
	@JFileChooserMixin.Setting(value = JFileChooserMixinProperty.KAGEROW_SCRIPT, isPrimary = true)
	private class EditScriptJFileChooser implements JFileChooserMixin {

		/** {@inheritDoc} */
		@Override
		public File getPath() {
			return Objects.isNull(addScriptPath) ? null : addScriptPath.toFile();
		}

		/** {@inheritDoc} */
		@Override
		@UseJITCompiler(JITCompilerOption.DONTINLINE)
		public JFileChooser createJFileChooser() {
			return JFileChooserMixin.super.createJFileChooser();
		}

	}

	/**
	 * スクリプト追加ボタン押下時の処理を実行します
	 * @param e イベント
	 */
	@JMenuItemMixin.ActionCommand(FileMenu.ADD_SCRIPT)
	public synchronized void addScript(ActionEvent e) {
		scriptPanel.addScriptEditer();
	}

	/**
	 * スクリプト保存ボタン押下時の処理を実行します
	 * @param e イベント
	 */
	@JMenuItemMixin.ActionCommand(FileMenu.SAVE_SCRIPT)
	public synchronized void saveScript(ActionEvent e) {
		if (scriptPanel.isSelected()) {
			scriptPanel.saveScriptEditer();
		}
	}

	/**
	 * データインポートを行いアーカイブを作成します
	 */
	@JMenuItemMixin.ActionCommand(FileMenu.DATA_IMPORT)
	public synchronized void dataImport() {

		// データインポート専用内部クラス生成
		DataImportJFileChooser chooserMixin = new DataImportJFileChooser();

		// ファイルダイアログインスタンス生成
		JFileChooser fileChooser = chooserMixin.createJFileChooser();

		// ファイルダイアログ表示
		int opneOption = fileChooser.showOpenDialog(mainFrame.frame);
		// 単一ファイル選択
		if (JFileChooser.APPROVE_OPTION == opneOption) {

			// モデル生成
			model = new DataImportModel();

			// インポート対象取得
			File file = fileChooser.getSelectedFile();
			dataImportPath = file.toPath().getParent();

			// パスの設定
			model.path = file.toPath();

			try {
				// ダイアログ生成
				JDialog dialog = chooserMixin.createJDialog();
				// モデル設定ダイアログ表示&インポート処理実行
				dialog.setVisible(true);
				// インポート終了後、コンテキストパネルに通知をする
				contextPanel.noticeObserver();
			} catch (Throwable exception) {
				// ログ出力
				KagerowLogger.newAppLogger().err(exception);
				// ダイアログ表示
				dialogHelper.showSystemError(FileMenuText.ERROR_004.toString());
			}

		}
	}

	/**
	 * データインポートボタンダイアログ
	 */
	@JFileChooserMixin.Setting(value = JFileChooserMixinProperty.DEFAULT_IMPORT, isPrimary = true)
	private class DataImportJFileChooser implements JFileChooserMixin, JDialogMixin {

		/** {@inheritDoc} */
		@Override
		public File getPath() {
			return Objects.isNull(dataImportPath) ? null : dataImportPath.toFile();
		}

		/** {@inheritDoc} */
		@Override
		@UseJITCompiler(JITCompilerOption.DONTINLINE)
		public JFileChooser createJFileChooser() {
			return JFileChooserMixin.super.createJFileChooser();
		}

		/** {@inheritDoc} */
		@Override
		public Frame getParentFrame() {
			return dialogHelper.getParent();
		}

		/**
		 * JDialogMixin向けパネル生成メソッド
		 * @param dialog 生成されたダイアログ
		 * @return 生成パネル
		 */
		@JDialogMixin.Setting(FileMenu.DATA_IMPORT_PANEL)
		private JPanel createPanel(JDialog dialog) {
			return new NonComponentDataImportJPanel(FileMenu.this.model, dialog);
		}

		/**
		 * JDialogMixinバイパスメソッド
		 * @return ダイアログインスタンス
		 * @throws Throwable ダイアログ生成失敗
		 */
		public JDialog createJDialog() throws Throwable {
			GUIText title = GUIText.FileMenu_004;
			return JDialogMixin.super.createJDialog(FileMenu.DATA_IMPORT_PANEL, title);
		}

	}

	/**
	 * データエクスポートを行います
	 * @param e イベント
	 */
	@JMenuItemMixin.ActionCommand(FileMenu.DATA_EXPORT)
	private synchronized void dataExport(ActionEvent e) {

	}

}

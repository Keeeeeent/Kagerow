package com.sakulabo.application.app.gui.MenuPanelParts.SettingMenuParts;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import javax.naming.NamingException;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.sakulabo.application.app.gui.MainFrame;
import com.sakulabo.application.app.gui.MenuPanelParts.AppMenu;
import com.sakulabo.application.app.gui.MenuPanelParts.HelpMenuParts.HelpMenu;
import com.sakulabo.application.common.code.GUIText;
import com.sakulabo.application.common.code.JFileChooserMixinProperty;
import com.sakulabo.application.common.code.SettingMenuText;
import com.sakulabo.application.common.initializer.GraphicComponent;
import com.sakulabo.application.common.mixin.JDialogMixin;
import com.sakulabo.application.common.mixin.JFileChooserMixin;
import com.sakulabo.application.common.mixin.JMenuItemMixin;
import com.sakulabo.application.helper.DialogHelper;
import com.sakulabo.core.Kagerow.KagerowApplication;
import com.sakulabo.core.Kagerow.Utilities.KagerowDataDump;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;
import com.sakulabo.core.Kagerow.Utilities.KagerowUtilities;
import com.sakulabo.regulation.annotation.KagerowComponent;
import com.sakulabo.regulation.annotation.KagerowInject;
import com.sakulabo.regulation.annotation.UseJITCompiler;
import com.sakulabo.regulation.annotation.UseJITCompiler.JITCompilerOption;

/**
 * 設定メニュー実装クラスです
 * 
 * @author keeeeeent
 */
@KagerowComponent
@GraphicComponent
@JMenuItemMixin.Setting(title = GUIText.SettingMenu_002, actionCommand = SettingMenu.SECURE_BOOT)
@JMenuItemMixin.Setting(title = GUIText.SettingMenu_003, actionCommand = SettingMenu.CHANGE_PASS)
//@JMenuItemMixin.Setting(title = GUIText.SettingMenu_004, actionCommand = SettingMenu.RESET_PASS)
@JMenuItemMixin.Setting(title = GUIText.SettingMenu_005, actionCommand = SettingMenu.EXPORT_BACKUP)
@JMenuItemMixin.Setting(title = GUIText.SettingMenu_006, actionCommand = SettingMenu.IMPORT_BACKUP)
@JMenuItemMixin.Setting(title = GUIText.SettingMenu_007, actionCommand = SettingMenu.MORE_SETTING)
public class SettingMenu extends AppMenu implements JMenuItemMixin {

	/** セキュアブート */
	public static final String SECURE_BOOT = "secureBoot";
	/** パスワード再設定 */
	public static final String CHANGE_PASS = "changePass";
	/** パスワードリセット */
	public static final String RESET_PASS = "resetPass";
	/** バックアップ作成 */
	public static final String EXPORT_BACKUP = "exportBackup";
	/** バックアップ取込 */
	public static final String IMPORT_BACKUP = "importBackup";
	/** 詳細設定 */
	public static final String MORE_SETTING = "moreSetting";

	/** ロガー */
	@KagerowInject
	private KagerowLogger logger;
	/** メインパネル */
	@KagerowInject
	private MainFrame mainFrame;
	/** ダイアログヘルパー */
	@KagerowInject
	private DialogHelper dialogHelper;
	/** 詳細設定パネル */
	@KagerowInject
	private AdvancedSettingsPanel advancedSettingsPanel;

	/**
	 * セキュアブートダイアログ
	 */
	private class SecureBootDialog {

		/**
		 * パスワードを取得します
		 * @throws NamingException セキュアブートに切り替え失敗
		 */
		void setPass() throws NamingException {
			// パスワード入力1回目
			String pass = dialogHelper.showPasswordDialog(
					SettingMenuText.INFO_003.toString(),
					SettingMenuText.INFO_004.toString());
			if (Objects.isNull(pass) || pass.isEmpty()) {
				// パスワードが未入力の場合、通知を行い処理を終了
				dialogHelper.showSystemWarning(SettingMenuText.INFO_010.toString());
				return;
			}
			// パスワード入力2回目
			String nextPass = dialogHelper.showPasswordDialog(
					SettingMenuText.INFO_005.toString(),
					SettingMenuText.INFO_006.toString());
			if (pass.equals(nextPass)) {
				// パスワードが同じ場合セキュアブートに使用するパスワードとして設定
				dialogHelper.showSystemInfo(SettingMenuText.INFO_007.toString());
				// セキュアブートに移行
				KagerowApplication.getInstance().changeToSecureBoot(pass);
				// セキュアブート設定完了通知実施
				dialogHelper.showSystemInfo(SettingMenuText.INFO_008.toString());
			} else {
				// パスワードが異なる場合ユーザ通知実施
				dialogHelper.showSystemWarning(SettingMenuText.INFO_009.toString());
			}
		}

	}

	/**
	 * パスワード再設定ダイアログ
	 */
	private class ChangePassDialog {

		/**
		 * パスワードを取得します
		 * @throws NamingException セキュアブートに切り替え失敗
		 */
		void setPass() throws NamingException {
			// パスワード入力1回目
			String pass = dialogHelper.showPasswordDialog(
					SettingMenuText.INFO_003.toString(),
					SettingMenuText.INFO_004.toString());
			if (Objects.isNull(pass) || pass.isEmpty()) {
				// パスワードが未入力の場合、通知を行い処理を終了
				dialogHelper.showSystemWarning(SettingMenuText.INFO_010.toString());
				return;
			}
			// パスワード入力2回目
			String nextPass = dialogHelper.showPasswordDialog(
					SettingMenuText.INFO_005.toString(),
					SettingMenuText.INFO_006.toString());
			if (pass.equals(nextPass)) {
				// セキュアブートに移行
				KagerowApplication.getInstance().changePassword(pass);
				// セキュアブート設定完了通知実施
				dialogHelper.showSystemInfo(SettingMenuText.INFO_011.toString());
			} else {
				// パスワードが異なる場合ユーザ通知実施
				dialogHelper.showSystemWarning(SettingMenuText.INFO_009.toString());
			}
		}

	}

	/**
	 * パスワードリセットダイアログ
	 */
	private class ResetPassDialog implements JDialogMixin {

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
		@JDialogMixin.Setting(HelpMenu.DISCLAIMER)
		private JPanel createPanel(JDialog dialog) {
			return new JPanel();
		}

		/**
		 * JDialogMixinバイパスメソッド
		 * @return ダイアログインスタンス
		 * @throws Throwable ダイアログ生成失敗
		 */
		public JDialog createJDialog() throws Throwable {
			return JDialogMixin.super.createJDialog(HelpMenu.DISCLAIMER, GUIText.EMPTY);
		}

	}

	/**
	 * バックアップ作成ダイアログ
	 */
	@JFileChooserMixin.Setting(value = JFileChooserMixinProperty.KAGEROW_BUCKUP, isPrimary = true)
	private class ExportBackupDialog implements JFileChooserMixin {

		/** {@inheritDoc} */
		@Override
		public File getPath() {
			String path = System.getProperty("user.home", "");
			return Paths.get(path).toFile();
		}

		/** {@inheritDoc} */
		@Override
		@UseJITCompiler(JITCompilerOption.DONTINLINE)
		public JFileChooser createJFileChooser() {
			return JFileChooserMixin.super.createJFileChooser();
		}

	}

	/**
	 * バックアップ取込ダイアログ
	 */
	@JFileChooserMixin.Setting(value = JFileChooserMixinProperty.KAGEROW_BUCKUP, isPrimary = true)
	private class ImportBackupDialog implements JFileChooserMixin {

		/** {@inheritDoc} */
		@Override
		public File getPath() {
			String path = System.getProperty("user.home", "");
			return Paths.get(path).toFile();
		}

		/** {@inheritDoc} */
		@Override
		@UseJITCompiler(JITCompilerOption.DONTINLINE)
		public JFileChooser createJFileChooser() {
			return JFileChooserMixin.super.createJFileChooser();
		}

	}

	/**
	 * 詳細設定ダイアログ
	 */
	private class MoreSettingDialog implements JDialogMixin {

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
		@JDialogMixin.Setting(HelpMenu.DISCLAIMER)
		private JPanel createPanel(JDialog dialog) {
			return advancedSettingsPanel;
		}

		/**
		 * JDialogMixinバイパスメソッド
		 * @return ダイアログインスタンス
		 * @throws Throwable ダイアログ生成失敗
		 */
		public JDialog createJDialog() throws Throwable {
			return JDialogMixin.super.createJDialog(HelpMenu.DISCLAIMER, GUIText.EMPTY);
		}

	}

	/**
	 * デフォルトコンストラクタ
	 * @throws IllegalAccessException ハンドラーアクセスエラー
	 * @throws NoSuchMethodException メソッド不明
	 */
	public SettingMenu() throws IllegalAccessException, NoSuchMethodException {
		// メニューの設定
		super(GUIText.SettingMenu_001);
		// アイテム設定
		setJMenuItem(this);
		// セキュアフラグ取得
		boolean isSecure = KagerowUtilities.isSecure();
		// 活性制御
		setMenu(isSecure);
	}

	/**
	 * メニューアイテムの活性制御を行います
	 * @param isSecure セキュアフラグ
	 */
	private void setMenu(boolean isSecure) {

		for (int i = 0; i < getItemCount(); i++) {
			// メニュー取得
			JMenuItem item = getItem(i);
			// 取得ができなかった場合、次のループに移行
			if (Objects.isNull(item)) {
				continue;
			}
			// コマンド取得
			String cmd = item.getActionCommand();
			if (isSecure) {
				// セキュアブートが既に行われている場合、セキュアブートボタンを無効化
				if (SECURE_BOOT.equals(cmd)) {
					item.setEnabled(false);
				}
				// セキュアブートが既に行われている場合、パスワード変更有効化
				if (CHANGE_PASS.equals(cmd)) {
					item.setEnabled(true);
				}
			} else {
				// 非セキュアブートの場合、セキュアブートボタンを有効化
				if (SECURE_BOOT.equals(cmd)) {
					item.setEnabled(true);
				}
				// 非セキュアブートの場合、パスワード変更無効化
				if (CHANGE_PASS.equals(cmd)) {
					item.setEnabled(false);
				}
			}
		}

	}

	/**
	 * セキュアブートボタン押下時の処理を実行します
	 * @param e イベント
	 */
	@JMenuItemMixin.ActionCommand(SettingMenu.SECURE_BOOT)
	public synchronized void secureBoot(ActionEvent e) {
		try {
			// ダイアログ生成
			SecureBootDialog dialog = new SecureBootDialog();
			// 設定ダイアログ表示&処理実行
			dialog.setPass();
		} catch (Throwable exception) {
			// ログ出力
			KagerowLogger.newAppLogger().err(exception);
			// ダイアログ表示
			dialogHelper.showSystemError(SettingMenuText.ERROR_001.toString());
		} finally {
			// メニュー設定
			SwingUtilities.invokeLater(() -> {
				setMenu(true);
			});
		}
	}

	/**
	 * パスワード再設定ボタン押下時の処理を実行します
	 * @param e イベント
	 */
	@JMenuItemMixin.ActionCommand(SettingMenu.CHANGE_PASS)
	public synchronized void changePass(ActionEvent e) {
		try {
			// ダイアログ生成
			ChangePassDialog dialog = new ChangePassDialog();
			// 設定ダイアログ表示&処理実行
			dialog.setPass();
		} catch (Throwable exception) {
			// ログ出力
			KagerowLogger.newAppLogger().err(exception);
			// ダイアログ表示
			dialogHelper.showSystemError(SettingMenuText.ERROR_001.toString());
		}
	}

	/**
	 * パスワードリセットボタン押下時の処理を実行します
	 * @param e イベント
	 */
	@JMenuItemMixin.ActionCommand(SettingMenu.RESET_PASS)
	public synchronized void resetPass(ActionEvent e) {
		try {
			// ダイアログ生成
			JDialog dialog = new ResetPassDialog().createJDialog();
			// モデル設定ダイアログ表示&インポート処理実行
			dialog.setVisible(true);
		} catch (Throwable exception) {
			// ログ出力
			KagerowLogger.newAppLogger().err(exception);
			// ダイアログ表示
			dialogHelper.showSystemError(SettingMenuText.ERROR_001.toString());
		}
	}

	/**
	 * バックアップ作成ボタン押下時の処理を実行します
	 * @param e イベント
	 */
	@JMenuItemMixin.ActionCommand(SettingMenu.EXPORT_BACKUP)
	public synchronized void exportBackup(ActionEvent e) {
		// ダイアログ生成
		JFileChooserMixin dialog = new ExportBackupDialog();
		JFileChooser fileChooser = dialog.createJFileChooser();
		// ファイルダイアログ表示
		int opneOption = fileChooser.showOpenDialog(mainFrame.frame);
		// 単一ファイル選択
		if (JFileChooser.APPROVE_OPTION == opneOption) {
			// インポート対象取得
			File file = fileChooser.getSelectedFile();
			// バックアップ作成インスタンス生成
			KagerowDataDump dump = KagerowDataDump.newBasicInstance();
			try {
				// バックアップ先
				Path output = KagerowDataDump.toBackupPath(file.toPath());
				// バックアップ生成
				dump.exportDump(output);
				// 完了メッセージ表示
				dialogHelper.showSystemInfo(SettingMenuText.INFO_001.toString());
			} catch (Throwable exception) {
				// ログ出力
				KagerowLogger.newAppLogger().err(exception);
				// ダイアログ表示
				dialogHelper.showSystemError(SettingMenuText.ERROR_002.toString());
			}
		}
	}

	/**
	 * バックアップ取込ボタン押下時の処理を実行します
	 * @param e イベント
	 */
	@JMenuItemMixin.ActionCommand(SettingMenu.IMPORT_BACKUP)
	public synchronized void importBackup(ActionEvent e) {
		// ダイアログ生成
		JFileChooserMixin dialog = new ImportBackupDialog();
		JFileChooser fileChooser = dialog.createJFileChooser();
		// ファイルダイアログ表示
		int opneOption = fileChooser.showOpenDialog(mainFrame.frame);
		// 単一ファイル選択
		if (JFileChooser.APPROVE_OPTION == opneOption) {
			// インポート対象取得
			File file = fileChooser.getSelectedFile();
			// バックアップ作成インスタンス生成
			KagerowDataDump dump = KagerowDataDump.newBasicInstance();
			try {
				// バックアップ取込
				dump.importDump(file.toPath());
				// 完了メッセージ表示
				dialogHelper.showSystemInfo(SettingMenuText.INFO_002.toString());
			} catch (Throwable exception) {
				// ログ出力
				KagerowLogger.newAppLogger().err(exception);
				// ダイアログ表示
				dialogHelper.showSystemError(SettingMenuText.ERROR_002.toString());
			}
		}
	}

	/**
	 * 詳細設定ボタン押下時の処理を実行します
	 * @param e イベント
	 */
	@JMenuItemMixin.ActionCommand(SettingMenu.MORE_SETTING)
	public synchronized void moreSetting(ActionEvent e) {
		try {
			// ダイアログ生成
			JDialog dialog = new MoreSettingDialog().createJDialog();
			// モデル設定ダイアログ表示&インポート処理実行
			dialog.setVisible(true);
		} catch (Throwable exception) {
			// ログ出力
			KagerowLogger.newAppLogger().err(exception);
			// ダイアログ表示
			dialogHelper.showSystemError(SettingMenuText.ERROR_001.toString());
		}
	}

}

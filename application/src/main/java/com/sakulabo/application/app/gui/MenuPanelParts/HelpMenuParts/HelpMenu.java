package com.sakulabo.application.app.gui.MenuPanelParts.HelpMenuParts;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;

import javax.swing.JDialog;
import javax.swing.JPanel;

import com.sakulabo.application.app.gui.DisclaimerPanel;
import com.sakulabo.application.app.gui.MenuPanelParts.AppMenu;
import com.sakulabo.application.common.code.GUIText;
import com.sakulabo.application.common.code.HelpMenuText;
import com.sakulabo.application.common.initializer.GraphicComponent;
import com.sakulabo.application.common.mixin.JDialogMixin;
import com.sakulabo.application.common.mixin.JMenuItemMixin;
import com.sakulabo.application.helper.DialogHelper;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;
import com.sakulabo.core.Kagerow.Utilities.KagerowUtilities;
import com.sakulabo.regulation.annotation.KagerowComponent;
import com.sakulabo.regulation.annotation.KagerowInject;

/**
 * ヘルプメニュー実装クラスです
 * 
 * @author keeeeeent
 */
@KagerowComponent
@GraphicComponent
@JMenuItemMixin.Setting(title = GUIText.HelpMenu_002, actionCommand = HelpMenu.APP_LICENSE)
@JMenuItemMixin.Setting(title = GUIText.HelpMenu_003, actionCommand = HelpMenu.OTHER_LICENSE)
@JMenuItemMixin.Setting(title = GUIText.HelpMenu_004, actionCommand = HelpMenu.RELEAS_NOTE)
@JMenuItemMixin.Setting(title = GUIText.HelpMenu_005, actionCommand = HelpMenu.DISCLAIMER)
public class HelpMenu extends AppMenu implements JMenuItemMixin {

	/** アプリケーションライセンス情報 */
	public static final String APP_LICENSE = "appLicense";
	/** サードパーティライセンス情報 */
	public static final String OTHER_LICENSE = "otherLicense";
	/** リリースノート */
	public static final String RELEAS_NOTE = "releaseNote";
	/** 免責事項 */
	public static final String DISCLAIMER = "disclaimer";
	/** ライセンスファイル格納ディレクトリ */
	public static final Path LICENCE_DIR = KagerowUtilities.createAppDirPath().resolve("license");

	/** ロガー */
	@KagerowInject
	private KagerowLogger logger;
	/** ダイアログヘルパー */
	@KagerowInject
	private DialogHelper dialogHelper;
	/** ヘルプパネル共通基盤パネル */
	@KagerowInject
	private CommonHelpPanel commonHelpPanel;
	/** ヘルプリストパネル共通基盤パネル */
	@KagerowInject
	private CommonHelpListPanel commonHelpListPanel;
	/** 免責事項パネル */
	@KagerowInject
	private DisclaimerPanel disclaimerPanel;

	/**
	 * デフォルトコンストラクタ
	 * @throws IllegalAccessException ハンドラーアクセスエラー
	 * @throws NoSuchMethodException メソッド不明
	 */
	public HelpMenu() throws IllegalAccessException, NoSuchMethodException {
		super(GUIText.HelpMenu_001);
		setJMenuItem(this);
	}

	/**
	 * アプリケーションライセンス情報ダイアログ
	 */
	private class AppLicenseDialog implements JDialogMixin {

		/** 表示テキスト */
		private StringBuilder text = new StringBuilder();

		/**
		 * デフォルトコンストラクタ
		 * @throws IOException パネル生成失敗
		 */
		AppLicenseDialog() throws IOException {
			Path path = LICENCE_DIR.resolve("LICENSE");
			try (BufferedReader reader = Files.newBufferedReader(path)) {
				String line;
				while ((line = reader.readLine()) != null) {
					text.append(line);
					text.append(System.lineSeparator());
				}
			}
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
		@JDialogMixin.Setting(HelpMenu.APP_LICENSE)
		private JPanel createPanel(JDialog dialog) {
			commonHelpPanel.getComponent(dialog, GUIText.HelpMenu_002.toString(), text);
			return commonHelpPanel;
		}

		/**
		 * JDialogMixinバイパスメソッド
		 * @return ダイアログインスタンス
		 * @throws Throwable ダイアログ生成失敗
		 */
		public JDialog createJDialog() throws Throwable {
			return JDialogMixin.super.createJDialog(HelpMenu.APP_LICENSE, GUIText.EMPTY);
		}

	}

	/**
	 * サードパーティライセンス情報ダイアログ
	 */
	private class OtherLicenseDialog implements JDialogMixin, FileVisitor<Path> {

		/** リストタイトル格納メモリ */
		private String content;

		/** {@inheritDoc} */
		@Override
		public Frame getParentFrame() {
			return dialogHelper.getParent();
		}

		/**
		 * JDialogMixin向けパネル生成メソッド
		 * @param dialog 生成されたダイアログ
		 * @return 生成パネル
		 * @throws IOException パネル生成失敗
		 */
		@JDialogMixin.Setting(HelpMenu.OTHER_LICENSE)
		private JPanel createPanel(JDialog dialog) throws IOException {
			Files.walkFileTree(LICENCE_DIR.resolve("licenses"), this);
			commonHelpListPanel.getComponent(dialog, GUIText.HelpMenu_003.toString());
			return commonHelpListPanel;
		}

		/**
		 * JDialogMixinバイパスメソッド
		 * @return ダイアログインスタンス
		 * @throws Throwable ダイアログ生成失敗
		 */
		public JDialog createJDialog() throws Throwable {
			return JDialogMixin.super.createJDialog(HelpMenu.OTHER_LICENSE, GUIText.EMPTY);
		}

		/** {@inheritDoc} */
		@Override
		public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
			content = Objects.toString(dir.subpath(dir.getNameCount() - 1, dir.getNameCount()));
			return FileVisitResult.CONTINUE;
		}

		/** {@inheritDoc} */
		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
			if (Objects.toString(file.getFileName()).startsWith("LICENSE")) {
				StringBuilder text = new StringBuilder();
				try (BufferedReader reader = Files.newBufferedReader(file)) {
					String line;
					while ((line = reader.readLine()) != null) {
						text.append(line);
						text.append(System.lineSeparator());
					}
				}
				commonHelpListPanel.addList(content, text);
			}
			return FileVisitResult.CONTINUE;
		}

		/** {@inheritDoc} */
		@Override
		public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
			return FileVisitResult.CONTINUE;
		}

		/** {@inheritDoc} */
		@Override
		public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
			return FileVisitResult.CONTINUE;
		}

	}

	/**
	 * リリースノートダイアログ
	 */
	private class ReleaseNoteDialog implements JDialogMixin {

		/** 表示テキスト */
		private StringBuilder text = new StringBuilder();

		/**
		 * デフォルトコンストラクタ
		 * @throws IOException パネル生成失敗
		 */
		ReleaseNoteDialog() throws IOException {
			Path path = LICENCE_DIR.resolve("ReleaseNotes.txt");
			try (BufferedReader reader = Files.newBufferedReader(path)) {
				String line;
				while ((line = reader.readLine()) != null) {
					text.append(line);
					text.append(System.lineSeparator());
				}
			}
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
		@JDialogMixin.Setting(HelpMenu.RELEAS_NOTE)
		private JPanel createPanel(JDialog dialog) {
			commonHelpPanel.getComponent(dialog, GUIText.HelpMenu_004.toString(), text);
			return commonHelpPanel;
		}

		/**
		 * JDialogMixinバイパスメソッド
		 * @return ダイアログインスタンス
		 * @throws Throwable ダイアログ生成失敗
		 */
		public JDialog createJDialog() throws Throwable {
			return JDialogMixin.super.createJDialog(HelpMenu.RELEAS_NOTE, GUIText.EMPTY);
		}

	}

	/**
	 * 免責事項ダイアログ
	 */
	private class DisclaimerDialog implements JDialogMixin {

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
			String text = disclaimerPanel.getDisclaimerText();
			commonHelpPanel.getComponent(dialog, GUIText.HelpMenu_005.toString(), text);
			return commonHelpPanel;
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
	 * アプリケーションライセンス情報ボタン押下時の処理を実行します
	 * @param e イベント
	 */
	@JMenuItemMixin.ActionCommand(HelpMenu.APP_LICENSE)
	public synchronized void appLicense(ActionEvent e) {
		try {
			// ダイアログ生成
			JDialog dialog = new AppLicenseDialog().createJDialog();
			// モデル設定ダイアログ表示&インポート処理実行
			dialog.setVisible(true);
		} catch (Throwable exception) {
			// ログ出力
			KagerowLogger.newAppLogger().err(exception);
			// ダイアログ表示
			dialogHelper.showSystemError(HelpMenuText.ERROR_001.toString());
		} finally {
			commonHelpPanel.close();
		}
	}

	/**
	 * サードパーティライセンス情報ボタン押下時の処理を実行します
	 * @param e イベント
	 */
	@JMenuItemMixin.ActionCommand(HelpMenu.OTHER_LICENSE)
	public synchronized void otherLicense(ActionEvent e) {
		try {
			// ダイアログ生成
			OtherLicenseDialog dialogCreater = new OtherLicenseDialog();
			JDialog dialog = dialogCreater.createJDialog();
			// モデル設定ダイアログ表示&インポート処理実行
			dialog.setVisible(true);
		} catch (Throwable exception) {
			// ログ出力
			KagerowLogger.newAppLogger().err(exception);
			// ダイアログ表示
			dialogHelper.showSystemError(HelpMenuText.ERROR_001.toString());
		} finally {
			commonHelpListPanel.close();
		}
	}

	/**
	 * リリースノートボタン押下時の処理を実行します
	 * @param e イベント
	 */
	@JMenuItemMixin.ActionCommand(HelpMenu.RELEAS_NOTE)
	public synchronized void releaseNote(ActionEvent e) {
		try {
			// ダイアログ生成
			JDialog dialog = new ReleaseNoteDialog().createJDialog();
			// モデル設定ダイアログ表示&インポート処理実行
			dialog.setVisible(true);
		} catch (Throwable exception) {
			// ログ出力
			KagerowLogger.newAppLogger().err(exception);
			// ダイアログ表示
			dialogHelper.showSystemError(HelpMenuText.ERROR_001.toString());
		} finally {
			commonHelpPanel.close();
		}
	}

	/**
	 * 免責事項ボタン押下時の処理を実行します
	 * @param e イベント
	 */
	@JMenuItemMixin.ActionCommand(HelpMenu.DISCLAIMER)
	public synchronized void disclaimer(ActionEvent e) {
		try {
			// ダイアログ生成
			JDialog dialog = new DisclaimerDialog().createJDialog();
			// モデル設定ダイアログ表示&インポート処理実行
			dialog.setVisible(true);
		} catch (Throwable exception) {
			// ログ出力
			KagerowLogger.newAppLogger().err(exception);
			// ダイアログ表示
			dialogHelper.showSystemError(HelpMenuText.ERROR_001.toString());
		} finally {
			commonHelpPanel.close();
		}
	}

}

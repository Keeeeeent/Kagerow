package com.sakulabo.application.helper;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import com.sakulabo.application.app.gui.CenterPanelParts.ScriptPanelParts.NonComponentKsqlPopupEditPanel;
import com.sakulabo.application.app.gui.CenterPanelParts.ScriptPanelParts.NonComponentResultViewPanel;
import com.sakulabo.application.app.gui.MenuPanelParts.FileMenuParts.FileMenu;
import com.sakulabo.application.common.initializer.GraphicComponent;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;
import com.sakulabo.regulation.annotation.KagerowComponent;
import com.sakulabo.regulation.annotation.KagerowInject;

/**
 * ショートカットキーを管理するヘルパーです
 * 
 * @author keeeeeent
 */
@KagerowComponent
@GraphicComponent
public final class AcceleratorHelper {

	/** ファイルメニューコンテンツインスタンス */
	@KagerowInject
	private FileMenu fileMenu;

	/**
	 * ショートカット管理列挙クラス
	 */
	public static enum ShortcutKey {

		/** スクリプト新規作成（ctlr+n） */
		CREATE_SCRIPT(FileMenu.ADD_SCRIPT, KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()) {
			@Override
			public void doShortcut(ActionEvent e, JComponent target) {
				((FileMenu) target).addScript(e);
			}
		},
		/** スクリプト編集（ctlr+o） */
		EDIT_SCRIPT(FileMenu.EDIT_SCRIPT, KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()) {
			@Override
			public void doShortcut(ActionEvent e, JComponent target) {
				((FileMenu) target).editScript();
			}
		},
		/** スクリプト保存（ctlr+s） */
		SAVE_SCRIPT(FileMenu.SAVE_SCRIPT, KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()) {
			@Override
			public void doShortcut(ActionEvent e, JComponent target) {
				((FileMenu) target).saveScript(e);
			}
		},
		/** データインポート（ctlr+i） */
		IMPORT_FILE_DATA(FileMenu.DATA_IMPORT, KeyEvent.VK_I, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()) {
			@Override
			public void doShortcut(ActionEvent e, JComponent target) {
				((FileMenu) target).dataImport();
			}
		},
		/** スクリプト実行（ctlr+enter） */
		EXECUT_SCRIPT("EXECUT_SCRIPT", KeyEvent.VK_ENTER, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()) {
			@Override
			public void doShortcut(ActionEvent e, JComponent target) {
				((NonComponentResultViewPanel) target).exeBtn();
			}
		},
		/** 個別スクリプト実行（ctlr+enter） */
		PRIVATE_EXECUT_SCRIPT("PRIVATE_EXECUT_SCRIPT", KeyEvent.VK_ENTER,
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()) {
			@Override
			public void doShortcut(ActionEvent e, JComponent target) {
				try {
					((NonComponentKsqlPopupEditPanel) target).exeBtn();
				} catch (Exception exception) {
					KagerowLogger.newAppLogger().err(exception);
				}

			}
		},
		/** スクリプト実行（ctlr+space） */
		COMPLEMENT_SCRIPT("COMPLEMENT_SCRIPT", KeyEvent.VK_SPACE,
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()) {
			@Override
			public void doShortcut(ActionEvent e, JComponent target) {
				// TODO スクリプト補完機能が出来たタイミングで登録する
			}
		}

		;

		/** アクションコマンド */
		private String action;
		/** キーイベント */
		private int keyEvent;
		/** キーイベントマスク */
		private int mask;

		/**
		 * デフォルトコンストラクタ
		 * @param action アクションコマンド
		 * @param keyEvent キーイベント
		 * @param mask キーイベントマスク
		 */
		private ShortcutKey(String action, int keyEvent, int mask) {
			this.action = action;
			this.keyEvent = keyEvent;
			this.mask = mask;
		}

		/**
		 * ショートカットキーアクションバインド
		 * @param e イベント
		 * @param target ショートカット起動対象コンポーネント
		 */
		public abstract void doShortcut(ActionEvent e, JComponent target);

	}

	/**
	 * ショートカットキーを生成、登録します
	 * @param shortcutKey ショートカットキー列挙クラス
	 * @param target 登録対象
	 */
	public final void setShortcut(ShortcutKey shortcutKey, JComponent target) {
		// ショートカット作成
		KeyStroke keyStroke = KeyStroke.getKeyStroke(shortcutKey.keyEvent, shortcutKey.mask);
		// ショートカットマップ取得
		InputMap inputMap = target.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		// アクションマップ取得
		ActionMap actionMap = target.getActionMap();
		// アクション登録
		inputMap.put(keyStroke, shortcutKey.action);
		actionMap.put(shortcutKey.action, new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				shortcutKey.doShortcut(e, target);
			}
		});
	}

	/**
	 * ショートカットの初期化処理です
	 */
	public void initialize() {
		// スクリプト新規作成ショートカット登録
		setShortcut(ShortcutKey.CREATE_SCRIPT, fileMenu);
		// スクリプト編集ショートカット登録
		setShortcut(ShortcutKey.EDIT_SCRIPT, fileMenu);
		// スクリプト保存ショートカット登録
		setShortcut(ShortcutKey.SAVE_SCRIPT, fileMenu);
		// データインポートショートカット登録
		setShortcut(ShortcutKey.IMPORT_FILE_DATA, fileMenu);
	}

}

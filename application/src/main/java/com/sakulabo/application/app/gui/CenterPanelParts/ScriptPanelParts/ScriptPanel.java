package com.sakulabo.application.app.gui.CenterPanelParts.ScriptPanelParts;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;

import javax.naming.NamingException;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.TransferHandler;

import com.sakulabo.application.app.gui.AppPanel;
import com.sakulabo.application.app.gui.MenuPanelParts.FileMenuParts.FileMenu;
import com.sakulabo.application.common.code.GUIText;
import com.sakulabo.application.common.code.ScriptPanelText;
import com.sakulabo.application.common.image.ButtonImage;
import com.sakulabo.application.common.initializer.GraphicComponent;
import com.sakulabo.application.common.mixin.ActionListenerMixin;
import com.sakulabo.application.common.mixin.JButtonMixin;
import com.sakulabo.application.common.mixin.JFileChooserMixin;
import com.sakulabo.application.common.mixin.JPanelMixin;
import com.sakulabo.application.common.mixin.JPanelMixin.Layout;
import com.sakulabo.application.common.mixin.JTabbedPaneMixin;
import com.sakulabo.application.controller.Context.SettingContextController;
import com.sakulabo.application.controller.Script.KSQLController;
import com.sakulabo.application.helper.DialogHelper;
import com.sakulabo.application.model.Script.KSQLScriptModel;
import com.sakulabo.core.Kagerow.Contents.KagerowSettingContent;
import com.sakulabo.core.Kagerow.Context.KagerowSettingContext;
import com.sakulabo.core.Kagerow.Exception.AppLogicException;
import com.sakulabo.core.Kagerow.Exception.KFileParseException;
import com.sakulabo.core.Kagerow.Exception.KSQLParseException;
import com.sakulabo.core.Kagerow.Utilities.KagerowFileState;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;
import com.sakulabo.core.Kagerow.Utilities.KagerowScriptAccessor;
import com.sakulabo.core.Kagerow.Utilities.KagerowUtilities;
import com.sakulabo.regulation.annotation.KagerowComponent;
import com.sakulabo.regulation.annotation.KagerowInject;

/**
 * GUIアプリケーションのスクリプト操作パネルクラスです
 * 
 * @author keeeeeent
 */
@KagerowComponent
@GraphicComponent
@JTabbedPaneMixin.Setting(setTabLayoutPolicy = JTabbedPane.SCROLL_TAB_LAYOUT)
public class ScriptPanel extends AppPanel implements JTabbedPaneMixin, JFileChooserMixin {

	/** クイックスタートパネル */
	// TODO Ver1.1.0でマクロ機能と合わせてリリース
	// @Inject
	// private QuickStartPanel quickStartPanel;
	/** 設定操作コントローラー */
	@KagerowInject
	private SettingContextController settingContextController;
	/** スクリプトコントローラー */
	@KagerowInject
	private KSQLController ksqlController;
	/** ダイアログヘルパー */
	@KagerowInject
	private DialogHelper dialogHelper;
	/** ファイルメニュー */
	@KagerowInject
	private FileMenu fileMenu;
	/** タブ区画 */
	private volatile JTabbedPane jTabbedPane;
	/** 設定保存コンテンツ */
	private volatile KagerowSettingContent settingContent;
	/** タブ最大値 */
	private volatile int maxTabCount = 10;
	/** 環境変数（タブ最大値） */
	private static final String MAX_TAB_COUNT = "ScriptPanelMaxTabCount";

	/**
	 * ドラッグ&ドロップを制御するハンドラーです
	 */
	private class FileDroper extends TransferHandler {
		/** {@inheritDoc} */
		@Override
		public boolean canImport(TransferSupport support) {
			return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
		}

		/** {@inheritDoc} */
		@Override
		public boolean importData(TransferSupport support) {
			if (!canImport(support)) {
				return false;
			}
			try {
				// ドロップされたファイル
				@SuppressWarnings("unchecked")
				List<File> files = (List<File>) support.getTransferable()
						.getTransferData(DataFlavor.javaFileListFlavor);
				// スクリプトパネルへ反映
				for (File file : files) {
					Path path = file.toPath();
					// Kagerowファイルの場合のみ受け入れ
					if (KagerowScriptAccessor.isKagerowFile(path)) {
						if (!isExistSetting(path)) {
							addScriptEditer(path);
						} else {
							dialogHelper.showSystemWarning(ScriptPanelText.ScriptPanel_EXITST_FILE.toString(
									new Object[] { path.toRealPath() }));
						}
					} else {
						dialogHelper.showSystemWarning(ScriptPanelText.ScriptPanel_NOT_KAGEROW_FILE.toString(
								new Object[] { path.toRealPath() }));
					}
				}
				return true;
			} catch (Exception e) {
				KagerowLogger.newAppLogger().err(e);
			}
			return false;
		}
	}

	/**
	 * ダイアログにてファイルを保存します
	 */
	private class DialogPanel {

		/** エディタインスタンス */
		private NonComponentScriptEditer editer;

		/**
		 * デフォルトコンストラクタ
		 * 
		 * @param editer エディタインスタンス
		 */
		public DialogPanel(NonComponentScriptEditer editer) {
			this.editer = editer;
		}

		/**
		 * ダイアログを表示します
		 * 
		 * @return 保存結果
		 * @throws Exception 変換失敗
		 */
		public boolean show() throws Exception {
			// ダイアログ表示
			boolean result = dialogHelper.showChoiceDialog(
					ScriptPanelText.ScriptPanel_INFO.toString(),
					ScriptPanelText.ScriptPanel_NOT_SAVE_KAGEROW_FILE.toString(
							new Object[] {
									editer.getModel().scriptAccessor.getName()
							}));
			if (result) {
				try {
					// スクリプト保存実行
					ksqlController.saveScript(editer.getModel());
					// 再度開くことが可能か確認
					KagerowScriptAccessor.getInstance(editer.getModel().path);
				} catch (KFileParseException | KSQLParseException e) {
					// ダイアログ表示
					dialogHelper.showValidationError(e.getMessage());
					throw e;
				}
			}
			return result;
		}

	}

	/**
	 * デフォルマコンストラクタ
	 */
	public ScriptPanel() {
		setTransferHandler(new FileDroper());
	}

	/*
	 * タブレイアウト制御パネル
	 */
	@JPanelMixin.Setting(layout = Layout.FlowLayout)
	private class ClosableTabComponent extends JPanel implements JButtonMixin {

		/** 閉じるボタン押下コマンド */
		static final String CMD = "close";

		/** クローズボタン */
		@JButtonMixin.Setting(actionCommand = CMD, title = GUIText.EMPTY, icon = ButtonImage.CLOSE)
		final JButton closeButton = new JButton();
		/** タブパネル参照 */
		private final JTabbedPane tabbedPane;
		/** 設定操作コントローラー */
		private final SettingContextController settingContextController;
		/** 処理対象コンポーネント */
		private final Component component;

		/**
		 * デフォルトコンストラクタ
		 * 
		 * @param tabbedPane タブパネル
		 * @param component  処理対象コンポーネント
		 */
		ClosableTabComponent(JTabbedPane tabbedPane, Component component) {

			// フィールド初期化
			this.tabbedPane = tabbedPane;
			settingContextController = KagerowUtilities.getBean(SettingContextController.class, null).get();
			this.component = component;

			// レイアウト指定
			setOpaque(false);
			// タイトル指定
			JLabel titleLabel = new JLabel() {
				@Override
				public String getText() {
					int index = tabbedPane.indexOfTabComponent(ClosableTabComponent.this);
					return index != -1 ? tabbedPane.getTitleAt(index) : "";
				}
			};
			// ボタン設定
			try {
				setJButton(this, closeButton);
			} catch (IllegalAccessException | NoSuchMethodException e) {
				KagerowLogger.newAppLogger().err(e);
			}
			// レイアウト追加
			add(titleLabel);
			add(closeButton);
		}

		/**
		 * クローズボタン押下イベント
		 */
		@ActionListenerMixin.ActionCommand(CMD)
		private void close() {
			int index = tabbedPane.indexOfTabComponent(this);
			if (index != -1) {
				NonComponentScriptEditer editer = (NonComponentScriptEditer) component;
				Path path = editer.getModel().path;
				if (!KagerowFileState.Ready.equals(editer.getModel().scriptAccessor.getState())) {
					// ダイアログ表示
					DialogPanel dialog = new DialogPanel(editer);
					try {
						dialog.show();
					} catch (Exception e) {
						// 保存失敗ダイアログを表示する
						dialogHelper.showSystemError(ScriptPanelText.ScriptPanel_SAVE_FAIL.toString());
						// ログ書き出し
						KagerowLogger.newAppLogger().err(e);
						// 強制クローズダイアログ表示
						boolean result = dialogHelper.showChoiceDialog(
								ScriptPanelText.ScriptPanel_INFO.toString(),
								ScriptPanelText.ScriptPanel_CLOSE_TAB.toString());
						if (!result) {
							// 修正が必要なためクローズせずこの処理は終了
							return;
						}
					}
				}
				if (Objects.nonNull(path)) {
					settingContextController.removePath(path.toString());
				}
				tabbedPane.removeTabAt(index);
				KagerowUtilities.getBean(FileMenu.class, null).ifPresent((f) -> {
					f.setMenu();
				});
			}
		}

	}

	/** {@inheritDoc} */
	@Override
	public Component getComponent() {
		if (Objects.isNull(jTabbedPane)) {
			synchronized (this) {
				if (Objects.isNull(jTabbedPane)) {

					// タブ区画初期化
					jTabbedPane = createTabbedPane();
					// TODO Ver1.1.0でマクロ機能と合わせてリリース
					// quickStartPanel.initialize();

					// パネル設定値取得
					try {

						// タブ最大値初期化
						String env = KagerowUtilities.getENV(KagerowSettingContext._NAME, MAX_TAB_COUNT);
						maxTabCount = Integer.parseInt(env);

						// コンテキスト取得
						settingContent = settingContextController.getSettingContent(ScriptPanel.class.getName());

					} catch (NamingException e) {
						logger.err(e);
					}

					// クイックスタート追加
					// TODO Ver1.1.0でマクロ機能と合わせてリリース
					// addTabAndTitle(quickStartPanel.getTitle(), quickStartPanel);

					// 保存済みのパスを取得し初期化
					List<Path> settingList = settingContextController.getSettingList(ScriptPanel.class.getName())
							.stream()
							.map(Paths::get)
							.distinct()
							.toList();

					// タブ追加
					for (Path script : settingList) {
						try {
							addScriptEditer(script);
						} catch (Exception e) {
							settingContextController.removePath(script.toString());
						}
					}

				}
			}
		}
		return jTabbedPane;
	}

	/**
	 * GUI描画初期化処理実施
	 */
	public void lazyInitialize() {
		for (int i = 0; i < jTabbedPane.getTabCount(); i++) {
			NonComponentScriptEditer component = (NonComponentScriptEditer) jTabbedPane.getComponentAt(i);
			component.lazyInitialize();
		}
	}

	/**
	 * アプリケーション終了前処理実施
	 * 
	 * @return クローズしない場合true
	 */
	public boolean befoerClose() {
		for (int i = 0; i < jTabbedPane.getTabCount(); i++) {
			NonComponentScriptEditer component = (NonComponentScriptEditer) jTabbedPane.getComponentAt(i);
			if (!KagerowFileState.Ready.equals(component.getModel().scriptAccessor.getState())) {
				// ダイアログ表示
				DialogPanel dialog = new DialogPanel(component);
				try {
					dialog.show();
				} catch (Exception e) {
					// 保存失敗ダイアログを表示する
					dialogHelper.showSystemError(ScriptPanelText.ScriptPanel_SAVE_FAIL.toString());
					// ログ書き出し
					KagerowLogger.newAppLogger().err(e);
					// 強制クローズダイアログ表示
					boolean tmp = dialogHelper.showChoiceDialog(
							ScriptPanelText.ScriptPanel_INFO.toString(),
							ScriptPanelText.ScriptPanel_CLOSE_TAB.toString());
					if (!tmp) {
						// 修正が必要なためクローズせずこの処理は終了
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * タブを追加します
	 * 
	 * @param title     タイトル
	 * @param component コンポーネント
	 * @return 実行結果
	 */
	private boolean addTabAndTitle(String title, Component component) {
		if (maxTabCount <= jTabbedPane.getTabCount()) {
			return false;
		}
		jTabbedPane.addTab(title, component);
		int index = jTabbedPane.indexOfComponent(component);
		jTabbedPane.setTabComponentAt(index, new ClosableTabComponent(jTabbedPane, component));
		fileMenu.setMenu();
		return true;
	}

	/**
	 * パスを保存します
	 * 
	 * @param path パス
	 * @throws NamingException 既にバインドされている場合
	 */
	private void addSetting(Path path) throws NamingException {
		// パスを正規化
		path = path.toAbsolutePath().normalize();
		// 既に追加済みの場合、重複してしまうため追加しない
		if (!isExistSetting(path)) {
			settingContent.bind(UUID.randomUUID().toString(), path.toString());
		}
	}

	/**
	 * 対象のパスが設定に保存済みであるかどうか判定します
	 * 
	 * @param path 判定パス
	 * @return 設定に追加済みのばあいtrueを返却します
	 */
	private boolean isExistSetting(Path path) {
		// 保存済みのパスを取得し初期化
		List<Path> settingList = settingContextController.getSettingList(ScriptPanel.class.getName())
				.stream().map(Paths::get).toList();
		return settingList.contains(path);
	}

	/**
	 * スクリプトエディターを追加します
	 * 
	 * @param path スクリプトパス
	 * @throws IOException         ファイルIOエラー
	 * @throws KFileParseException KFile解析エラー
	 * @throws KSQLParseException  KSQL解析エラー
	 * @throws AppLogicException   アプリケーションロジック不正
	 */
	public void addScriptEditer(Path path)
			throws KFileParseException, KSQLParseException, AppLogicException, IOException {
		NonComponentScriptEditer scriptEditer = new NonComponentScriptEditer(path);
		if (addTabAndTitle(scriptEditer.getTitle(), scriptEditer)) {
			try {
				addSetting(path);
			} catch (NamingException e) {
				logger.err(e);
			}
		} else {
			// 最大数タブを表示している場合
			maxTabDialog();
		}
	}

	/**
	 * スクリプトエディタの状態をファイルに保存します
	 */
	public void saveScriptEditer() {
		saveScriptEditer(true);
	}

	/**
	 * スクリプトエディタの状態をファイルに保存します
	 * 
	 * @param showSaveInfo 保存完了ダイアログを表示するか指定します
	 */
	public void saveScriptEditer(boolean showSaveInfo) {

		// 選択中の要素取得
		NonComponentScriptEditer selectedComponent = (NonComponentScriptEditer) jTabbedPane.getSelectedComponent();
		// モデル取得
		KSQLScriptModel model = selectedComponent.getModel();
		// モデルの保村先を選択
		Path output = model.path;

		if (Objects.isNull(output)) {

			// 保存先が未指定の場合
			JFileChooser chooser = createJFileChooser();
			// ダイアログ表示
			int result = chooser.showOpenDialog(dialogHelper.getParent());

			if (result == JFileChooser.APPROVE_OPTION) {

				// 選択ファイル
				File selectedFile = chooser.getSelectedFile();
				if (Objects.isNull(selectedFile) || Files.exists(selectedFile.toPath())) {
					// ファイル未選択の処理を終了
					return;
				} else {
					// 選択済みの場合モデルに設定
					model.path = selectedFile.toPath();
				}
			} else {
				// 取り消し/クローズの場合即時リターン
				return;
			}
		}
		// 保村処理実行
		boolean isSuccess = false;
		try {
			// スクリプト保存実行
			isSuccess = ksqlController.saveScript(model);
			// 再度開くことが可能か確認
			KagerowScriptAccessor.getInstance(model.path);
		} catch (KFileParseException | KSQLParseException e) {
			if (showSaveInfo) {
				// ダイアログ表示
				dialogHelper.showValidationError(e.getMessage());
			}
			// ログ書き出し
			logger.err(e);
			return;
		} catch (Exception e) {
			// ログ書き出し
			logger.err(e);
		}

		// 実行結果をダイアログで表示
		if (isSuccess) {
			// 保存成功
			if (showSaveInfo) {
				// 保存完了ダイアログを表示するか指定がされている場合、ダイアログを表示
				dialogHelper.showSystemInfo(ScriptPanelText.ScriptPanel_SAVE_SUCCESS.toString());
			} else {
				// 上記以外の場合はログに出力
				logger.log(Level.INFO, ScriptPanelText.ScriptPanel_SAVE_SUCCESS.toString(), new Object[0]);
			}
			try {
				// 保存処理成功後、再度スクリプトを読み込み状態を再進化
				selectedComponent.refreshModel();
				// 保存処理成功後、次回起動時に備えてパスを保存
				addSetting(model.path);
			} catch (Exception e) {
				logger.err(e);
			}
		} else {
			// 保存失敗
			if (showSaveInfo) {
				// 保存失敗ダイアログを表示するか指定がされている場合、ダイアログを表示
				dialogHelper.showSystemError(ScriptPanelText.ScriptPanel_SAVE_FAIL.toString());
			} else {
				// 上記以外の場合はログに出力
				logger.log(Level.SEVERE, ScriptPanelText.ScriptPanel_SAVE_FAIL.toString(), new Object[0]);
			}
		}

	}

	/**
	 * 新規スクリプトエディターを追加します
	 */
	public void addScriptEditer() {
		NonComponentScriptEditer scriptEditer = new NonComponentScriptEditer();
		if (!addTabAndTitle(scriptEditer.getTitle(), scriptEditer)) {
			// 最大数タブを表示している場合
			maxTabDialog();
		}
	}

	/**
	 * タブ許容数が最大値に達したことを通知するダイアログを表示します
	 */
	private void maxTabDialog() {
		dialogHelper.showSystemWarning(ScriptPanelText.ScriptPanel_MAX_TAB.toString(
				new Object[] {
						maxTabCount
				}));
	}

	/** {@inheritDoc} */
	@Override
	public File getPath() {
		return null;
	}

	/**
	 * タブコンポーネントが選択済みか判定します
	 * 
	 * @return 判定結果
	 */
	public boolean isSelected() {
		return jTabbedPane.getSelectedIndex() != -1;
	}

}

package com.sakulabo.application.app.gui.CenterPanelParts.ContextPanelParts.PluginParts;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

import javax.naming.CannotProceedException;
import javax.naming.NamingException;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import com.sakulabo.application.app.gui.CenterPanelParts.ContextPanelParts.ContextPanel;
import com.sakulabo.application.app.gui.CenterPanelParts.ContextPanelParts.SubContextPanel;
import com.sakulabo.application.common.code.GUIText;
import com.sakulabo.application.common.code.PluginPkgContextPanelText;
import com.sakulabo.application.common.mixin.ActionListenerMixin;
import com.sakulabo.application.common.mixin.JButtonMixin;
import com.sakulabo.application.common.mixin.JLabelMixin;
import com.sakulabo.application.common.mixin.JPanelMixin;
import com.sakulabo.application.common.mixin.JPanelMixin.Layout;
import com.sakulabo.application.helper.DialogHelper;
import com.sakulabo.core.Kagerow.Context.KagerowPluginContext;
import com.sakulabo.core.Kagerow.Context.KagerowPluginPackageContext;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;
import com.sakulabo.core.Kagerow.Utilities.KagerowUtilities;

/**
 * パッケージ詳細パネル実装クラスです
 * 
 * @author keeeeeent
 */
public class PluginPkgContextPanel extends SubContextPanel {

	/** アクションコマンド（操作ボタン） */
	private static final String EDIT_CMD = GUIText.PluginPkgContextPanel_001.toString();

	/** プラグインパッケージ */
	@SuppressWarnings("unused")
	private KagerowPluginContext pluginPkg;
	/** パッケージ名 */
	private final String name;
	/** デフォルトパッケージフラグ */
	private final boolean isDefault;

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

	/** タブコンポーネント */
	private PluginPkgTabPanel pkgTabPanel;
	{
		pkgTabPanel = KagerowUtilities.getBean(PluginPkgTabPanel.class, null).get();
	}

	/**
	 * デフォルトコンストラクタ
	 * @param name パッケージ名称
	 * @param isDefault デフォルトパッケージフラグ
	 */
	public PluginPkgContextPanel(String name, boolean isDefault) {
		// フィールド初期化
		this.name = name;
		this.isDefault = isDefault;
		try {
			// パッケージコンテキスト取得
			KagerowPluginPackageContext pkg = KagerowUtilities.getContext(KagerowPluginPackageContext._NAME);
			// プラグインパッケージ取得
			pluginPkg = pkg.lookup(name);
		} catch (NamingException e) {
			KagerowLogger.newAppLogger().err(e);
		}
	}

	/**
	 * 操作パネル定義
	 */
	@JPanelMixin.Setting(backgroudColor = 0xFFFFFF, layout = Layout.BorderLayout)
	private class EditPanel extends JPanel implements JPanelMixin, JButtonMixin, JLabelMixin {

		// ####################################################################################
		// # 削除化処理向けパーツ
		// ####################################################################################

		/** 削除コマンド */
		private static final String DEL_CMD = "DEL_CMD";

		/** 削除ボタン */
		@JButtonMixin.Setting(title = GUIText.PluginPkgContextPanel_004, actionCommand = DEL_CMD)
		private JButton delBtn = new JButton();
		{
			// ボタン初期化処理
			try {
				setJButton(this, delBtn);
			} catch (IllegalAccessException | NoSuchMethodException e) {
				KagerowLogger.newAppLogger().err(e);
			}
		}
		/** 削除説明文 */
		private JTextArea delText = new JTextArea();
		{
			// 説明文追加
			delText.setText(GUIText.PluginPkgContextPanel_002.toLineString());
			// レイアウト設定
			delText.setEditable(false);
			delText.setLineWrap(true);
			delText.setWrapStyleWord(true);
			delText.setOpaque(false);
			delText.setBorder(null);
		}
		/** 無効パネル */
		private JPanel delBtnPanel = new JPanel();
		{
			// レイアウト設定
			setJPanel(delBtnPanel);
			delBtnPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
			// コンポーネント配置
			delBtnPanel.add(delBtn);
		}
		/** 無効パネル */
		private JPanel delPanel = new JPanel();
		{
			// レイアウト設定
			setJPanel(delPanel);
			delPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
			// ボーダー設定
			delPanel.setBorder(BorderFactory.createTitledBorder(
					BorderFactory.createEtchedBorder(),
					GUIText.PluginPkgContextPanel_003.toString()));
			// コンポーネント配置
			delPanel.add(delText, BorderLayout.CENTER);
			delPanel.add(delBtnPanel, BorderLayout.SOUTH);
		}

		// ####################################################################################
		// # 無効化処理向けパーツ
		// ####################################################################################

		/**  無効化コマンド */
		private static final String DISABLE_CMD = "DISABLE_CMD";

		/**  無効化ボタン */
		@JButtonMixin.Setting(title = GUIText.PluginPkgContextPanel_007, actionCommand = DISABLE_CMD)
		private JButton disBtn = new JButton();
		{
			// ボタン初期化処理
			try {
				setJButton(this, disBtn);
			} catch (IllegalAccessException | NoSuchMethodException e) {
				KagerowLogger.newAppLogger().err(e);
			}
		}
		/** 無効化説明文 */
		private JTextArea disText = new JTextArea();
		{
			// 説明文追加
			disText.setText(GUIText.PluginPkgContextPanel_005.toLineString());
			// レイアウト設定
			disText.setEditable(false);
			disText.setLineWrap(true);
			disText.setWrapStyleWord(true);
			disText.setOpaque(false);
			disText.setBorder(null);
		}
		/** 無効化パネル */
		private JPanel disBtnPanel = new JPanel();
		{
			// レイアウト設定
			setJPanel(disBtnPanel);
			disBtnPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
			// コンポーネント配置
			disBtnPanel.add(disBtn);
		}
		/**  無効化パネル */
		private JPanel disPanel = new JPanel();
		{
			// レイアウト設定
			setJPanel(disPanel);
			disPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
			// ボーダー設定
			disPanel.setBorder(BorderFactory.createTitledBorder(
					BorderFactory.createEtchedBorder(),
					GUIText.PluginPkgContextPanel_006.toString()));
			// コンポーネント配置
			disPanel.add(disText, BorderLayout.CENTER);
			disPanel.add(disBtnPanel, BorderLayout.SOUTH);
		}

		// ####################################################################################
		// # 有効化処理向けパーツ
		// ####################################################################################

		/**  有効化コマンド */
		private static final String ENABLE_CMD = "ENABLE_CMD";

		/**  有効化ボタン */
		@JButtonMixin.Setting(title = GUIText.PluginPkgContextPanel_009, actionCommand = ENABLE_CMD)
		private JButton enaBtn = new JButton();
		{
			// ボタン初期化処理
			try {
				setJButton(this, enaBtn);
			} catch (IllegalAccessException | NoSuchMethodException e) {
				KagerowLogger.newAppLogger().err(e);
			}
		}
		/** 有効化説明文 */
		private JTextArea enaText = new JTextArea();
		{
			// 説明文追加
			enaText.setText(GUIText.PluginPkgContextPanel_010.toLineString());
			// レイアウト設定
			enaText.setEditable(false);
			enaText.setLineWrap(true);
			enaText.setWrapStyleWord(true);
			enaText.setOpaque(false);
			enaText.setBorder(null);
		}
		/** 有効化パネル */
		private JPanel enaBtnPanel = new JPanel();
		{
			// レイアウト設定
			setJPanel(enaBtnPanel);
			enaBtnPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
			// コンポーネント配置
			enaBtnPanel.add(enaBtn);
		}
		/**  有効化パネル */
		private JPanel enaPanel = new JPanel();
		{
			// レイアウト設定
			setJPanel(enaPanel);
			enaPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
			// ボーダー設定
			enaPanel.setBorder(BorderFactory.createTitledBorder(
					BorderFactory.createEtchedBorder(),
					GUIText.PluginPkgContextPanel_008.toString()));
			// コンポーネント配置
			enaPanel.add(enaText, BorderLayout.CENTER);
			enaPanel.add(enaBtnPanel, BorderLayout.SOUTH);
		}

		// ####################################################################################
		// # 内部クラス向けパーツ
		// ####################################################################################

		/** 排他制御ロック */
		private final ReentrantLock lock = new ReentrantLock();

		/**
		 * デフォルトコンストラクタ
		 */
		EditPanel() {
			// 共通初期化処理実行
			setJPanel(this);
			// レイアウト設定
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			// コンポーネント配置
			add(delPanel);
			add(disPanel);
			add(enaPanel);
			// デフォルトパッケージの場合ボタンを無効化
			this.delBtn.setEnabled(!isDefault);
			this.disBtn.setEnabled(!isDefault);
			this.enaBtn.setEnabled(!isDefault);
			try {
				// 表示設定切り替え
				chengeView();
			} catch (NamingException e) {
				KagerowLogger.newAppLogger().err(e);
				// 切り替えに失敗した場合は、有効/無効の切り替えをできなくする
				disPanel.setVisible(false);
				enaPanel.setVisible(false);
			}
		}

		/**
		 * メニューの表示設定を切り替えます
		 * @throws NamingException コンテキストが見つからなかった場合
		 */
		private void chengeView() throws NamingException {
			// パッケージコンテキスト取得
			KagerowPluginPackageContext pkg = KagerowUtilities.getContext(KagerowPluginPackageContext._NAME);
			// プラグインパッケージ有効チェック
			if (pkg.isDisable(name)) {
				disPanel.setVisible(false);
				enaPanel.setVisible(true);
			} else {
				disPanel.setVisible(true);
				enaPanel.setVisible(false);
			}
		}

		/**
		 * 削除ボタン押下イベント
		 */
		@ActionListenerMixin.ActionCommand(DEL_CMD)
		private void del() {
			// 排他制御開始
			lock.lock();
			try {
				// パッケージコンテキスト取得
				KagerowPluginPackageContext pkg = KagerowUtilities.getContext(KagerowPluginPackageContext._NAME);
				// パッケージ削除
				pkg.destroySubcontext(name);
				// 削除成功ダイアログ
				dialogHelper.showSystemInfo(PluginPkgContextPanelText.SUCCESS_DELETED.toString());
				// タブクローズ
				pkgTabPanel.delTab(name);
				// コンテキスト変更通知
				contextPanel.noticeObserver();
			} catch (CannotProceedException e) {
				if (e.getCause() instanceof IOException) {
					// 削除失敗の場合
					dialogHelper.showSystemError(PluginPkgContextPanelText.FILE_DELETED.toString());
				} else {
					// 予期せぬ例外
					dialogHelper.showSystemError(
							PluginPkgContextPanelText.FILE_DELETED_SYSTEM_ERROR.toString());
				}
				// ログ書き出し
				KagerowLogger.newAppLogger().err(e);
			} catch (NamingException e) {
				// 予期せぬ例外
				dialogHelper
						.showSystemError(PluginPkgContextPanelText.FILE_DELETED_SYSTEM_ERROR.toString());
				// ログ書き出し
				KagerowLogger.newAppLogger().err(e);
			} catch (Exception e) {
				// 予期せぬ例外
				dialogHelper
						.showSystemError(PluginPkgContextPanelText.FILE_DELETED_SYSTEM_ERROR.toString());
				// ログ書き出し
				KagerowLogger.newAppLogger().err(e);
			} finally {
				// 排他制御解除
				lock.unlock();
			}
		}

		/**
		 * 無効化ボタン押下イベント
		 */
		@ActionListenerMixin.ActionCommand(DISABLE_CMD)
		private void dis() {
			// 排他制御開始
			lock.lock();
			try {
				// パッケージコンテキスト取得
				KagerowPluginPackageContext pkg = KagerowUtilities.getContext(KagerowPluginPackageContext._NAME);
				// パッケージ無効化
				pkg.setDisable(true, name);
				// 成功ダイアログ
				dialogHelper.showSystemInfo(PluginPkgContextPanelText.SUCCESS_DISABLE.toString());
				// タブクローズ
				pkgTabPanel.delTab(name);
				// コンテキスト変更通知
				contextPanel.noticeObserver();
			} catch (CannotProceedException e) {
				if (e.getCause() instanceof IOException) {
					// 無効化失敗の場合
					dialogHelper.showSystemError(PluginPkgContextPanelText.FILE_DISABLE.toString());
				} else {
					// 予期せぬ例外
					dialogHelper.showSystemError(
							PluginPkgContextPanelText.FILE_DISABLE_SYSTEM_ERROR.toString());
				}
				// ログ書き出し
				KagerowLogger.newAppLogger().err(e);
			} catch (NamingException e) {
				// 予期せぬ例外
				dialogHelper
						.showSystemError(PluginPkgContextPanelText.FILE_DISABLE_SYSTEM_ERROR.toString());
				// ログ書き出し
				KagerowLogger.newAppLogger().err(e);
			} catch (Exception e) {
				// 予期せぬ例外
				dialogHelper
						.showSystemError(PluginPkgContextPanelText.FILE_DISABLE_SYSTEM_ERROR.toString());
				// ログ書き出し
				KagerowLogger.newAppLogger().err(e);
			} finally {
				// 排他制御解除
				lock.unlock();
			}
		}

		/**
		 * 有効化ボタン押下イベント
		 */
		@ActionListenerMixin.ActionCommand(ENABLE_CMD)
		private void ena() {
			// 排他制御開始
			lock.lock();
			try {
				// パッケージコンテキスト取得
				KagerowPluginPackageContext pkg = KagerowUtilities.getContext(KagerowPluginPackageContext._NAME);
				// パッケージ無効化
				pkg.setDisable(false, name);
				// 成功ダイアログ
				dialogHelper.showSystemInfo(PluginPkgContextPanelText.SUCCESS_ENABLE.toString());
				// タブクローズ
				pkgTabPanel.delTab(name);
				// コンテキスト変更通知
				contextPanel.noticeObserver();
			} catch (CannotProceedException e) {
				if (e.getCause() instanceof IOException) {
					// 有効化失敗の場合
					dialogHelper.showSystemError(PluginPkgContextPanelText.FILE_ENABLE.toString());
				} else {
					// 予期せぬ例外
					dialogHelper.showSystemError(
							PluginPkgContextPanelText.FILE_ENABLE_SYSTEM_ERROR.toString());
				}
				// ログ書き出し
				KagerowLogger.newAppLogger().err(e);
			} catch (NamingException e) {
				// 予期せぬ例外
				dialogHelper
						.showSystemError(PluginPkgContextPanelText.FILE_ENABLE_SYSTEM_ERROR.toString());
				// ログ書き出し
				KagerowLogger.newAppLogger().err(e);
			} catch (Exception e) {
				// 予期せぬ例外
				dialogHelper
						.showSystemError(PluginPkgContextPanelText.FILE_ENABLE_SYSTEM_ERROR.toString());
				// ログ書き出し
				KagerowLogger.newAppLogger().err(e);
			} finally {
				// 排他制御解除
				lock.unlock();
			}
		}

	}

	/** {@inheritDoc} */
	@Override
	public Component getComponent() {
		Component component = super.getComponent();
		addAction(EDIT_CMD, new EditPanel());
		return component;
	}

}

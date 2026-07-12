package com.sakulabo.application.app.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Objects;

import javax.naming.NamingException;
import javax.swing.JDialog;
import javax.swing.JFrame;

import com.sakulabo.application.app.gui.CenterPanelParts.ContextPanelParts.CacheParts.CacheTabPanel;
import com.sakulabo.application.app.gui.CenterPanelParts.ContextPanelParts.PluginParts.PluginPkgTabPanel;
import com.sakulabo.application.app.gui.CenterPanelParts.ContextPanelParts.PluginParts.PluginTabPanel;
import com.sakulabo.application.app.gui.CenterPanelParts.ContextPanelParts.SchemaParts.VirtualFileSchemaTabPanel;
import com.sakulabo.application.app.gui.CenterPanelParts.ContextPanelParts.SchemaParts.VirtualFileTableTabPanel;
import com.sakulabo.application.app.gui.CenterPanelParts.ScriptPanelParts.ScriptPanel;
import com.sakulabo.application.app.gui.MenuPanelParts.FileMenuParts.FileMenu;
import com.sakulabo.application.app.rpc.RpcServer;
import com.sakulabo.application.common.code.GUIText;
import com.sakulabo.application.common.initializer.GraphicComponent;
import com.sakulabo.application.common.mixin.AppMixin;
import com.sakulabo.application.common.mixin.JFrameMixin;
import com.sakulabo.application.common.spi.ViewRunner;
import com.sakulabo.application.helper.AcceleratorHelper;
import com.sakulabo.application.helper.DialogHelper;
import com.sakulabo.application.helper.FramePositionHelper;
import com.sakulabo.core.Kagerow.KagerowApplication;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;
import com.sakulabo.regulation.annotation.KagerowComponent;
import com.sakulabo.regulation.annotation.KagerowInject;

/**
 * GUIアプリケーションのメインフレーム実装クラスです
 *
 * @author keeeeeent
 */
@KagerowComponent
@GraphicComponent
@AppMixin.Size(height = 640, width = 960)
@JFrameMixin.Setting(closeOperation = JFrame.DO_NOTHING_ON_CLOSE, title = "KagerowSQL")
public class MainFrame extends WindowAdapter implements ViewRunner, JFrameMixin {

	/** メインフレーム */
	public final JFrame frame;
	/** ロガー */
	@KagerowInject
	private KagerowLogger logger;
	/** RPCサーバ */
	@KagerowInject
	private RpcServer rpcServer;
	/** メインフレーム（上側） */
	@KagerowInject
	private MenuPanel menuPanel;
	/** メインフレーム（下側） */
	@KagerowInject
	private NoticePanel noticePanel;
	/** メインフレーム（右側） */
	@KagerowInject
	private CenterPanel centerPanel;
	/** フレームヘルパー */
	@KagerowInject
	private FramePositionHelper framePositionHelper;
	/** ショートカットヘルパー */
	@KagerowInject
	private AcceleratorHelper acceleratorHelper;
	/** 免責事項パネル */
	@KagerowInject
	private DisclaimerPanel disclaimerPanel;
	/** ダイアログヘルパー */
	@KagerowInject
	private DialogHelper dialogHelper;
	/** スキーマタブパネル（フレーム） */
	@KagerowInject
	private VirtualFileSchemaTabPanel fileSchemaTabPanel;
	/** テーブルタブパネル（フレーム） */
	@KagerowInject
	private VirtualFileTableTabPanel fileTableTabPanel;
	/** プラグインタブパネル（フレーム） */
	@KagerowInject
	private PluginTabPanel pluginTabPanel;
	/** プラグインパッケージタブパネル（フレーム） */
	@KagerowInject
	private PluginPkgTabPanel pkgTabPanel;
	/** キャッシュタブパネル（フレーム） */
	@KagerowInject
	private CacheTabPanel cacheTabPanel;
	/** ファイルメニュー */
	@KagerowInject
	private FileMenu fileMenu;
	/** スクリプトパネル */
	@KagerowInject
	private ScriptPanel scriptPanel;

	/** 免責事項ダイアログ */
	private volatile JDialog dialog;

	/**
	 * デフォルトコンスト楽あ
	 */
	public MainFrame() {

		// フレーム初期化
		frame = createFrame();
		setSize(frame);

		// フレームの設定初期化
		frame.setLayout(new BorderLayout());
		frame.addWindowListener(this);

	}

	/** {@inheritDoc} */
	@Override
	public void windowClosing(WindowEvent event) {
		// 未保存ファイルチェック
		if (scriptPanel.befoerClose()) {
			// 未保存ファイルがある場合、クローズイベントをキャンセル
			return;
		}
		// JMX監視停止
		KagerowApplication.getJMX().stop();
		// フレーム位置保存
		try {
			framePositionHelper.saveFramePosition(frame);
		} catch (NamingException e) {
			logger.err(e);
		}
		// フレームクローズ
		frame.dispose();
		// サブレームクローズ
		for (AppTabPanel tabPanel : new AppTabPanel[] { fileSchemaTabPanel, fileTableTabPanel, pluginTabPanel,
				pkgTabPanel, cacheTabPanel }) {
			if (tabPanel.frame.isDisplayable()) {
				tabPanel.frame.dispose();
			}
		}
		// RPCサーバ停止
		rpcServer.stop();
	}

	/** {@inheritDoc} */
	@Override
	public void start() {

		// 免責事項に同意済みか確認
		if (!disclaimerPanel.checkAgreeAction()) {
			// 同意されていない場合、免責事項表示
			opneDisclaimerPanel();
		}

		// フレーム位置設定
		try {
			framePositionHelper.lordFramePosition(frame);
		} catch (NamingException e) {
			// ロードに失敗した場合画面中央に表示
			frame.setLocationRelativeTo(null);
			logger.err(e);
		}

		// GUI初期化
		AppPanel[] panelList = { menuPanel, noticePanel, centerPanel };
		for (AppPanel panel : panelList) {
			panel.initialize();
		}

		// ショートカット初期化
		acceleratorHelper.initialize();

		// コンテナー取得
		Container container = frame.getContentPane();

		// パネルの設定
		container.add(BorderLayout.NORTH, menuPanel);
		container.add(BorderLayout.SOUTH, noticePanel);
		container.add(BorderLayout.CENTER, centerPanel);

		// フレーム表示
		frame.setVisible(true);

		// Swing初期化処理
		centerPanel.lazyInitialize();
		fileMenu.lazyInitialize();

		// RPCサーバ起動
		rpcServer.start();

	}

	/**
	 * 免責事項ダイアログを表示します
	 */
	private void opneDisclaimerPanel() {
		// パネル初期化（免責事項パネル）
		disclaimerPanel.initialize();
		// ダイアログ生成
		dialog = dialogHelper.createCustomDialog(disclaimerPanel, GUIText.MainFrame_001.toString());
		// ダイアログ設定
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		// ダイアログ表示
		dialog.setVisible(true);
	}

	/**
	 * 免責事項ダイアログをクローズします
	 *
	 * @throws IllegalAccessException ダイアログインスタンス未初期化の場合
	 */
	public void closeDialog() throws IllegalAccessException {
		if (Objects.nonNull(dialog)) {
			dialog.setVisible(false);
		} else {
			throw new IllegalAccessException();
		}
	}

}

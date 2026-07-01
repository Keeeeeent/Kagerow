package com.sakulabo.application.app.gui.CenterPanelParts.ScriptPanelParts;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import com.sakulabo.application.app.gui.AppPanel;
import com.sakulabo.application.common.code.GUIText;
import com.sakulabo.application.common.mixin.ActionListenerMixin;
import com.sakulabo.application.common.mixin.JButtonMixin;
import com.sakulabo.application.common.mixin.JPanelMixin;
import com.sakulabo.application.common.mixin.JToolBarMixin;
import com.sakulabo.core.Kagerow.Utilities.KagerowExecutionPlanAccessor;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;
import com.sakulabo.core.Kagerow.Utilities.KagerowScriptAccessor;

/**
 * GUIアプリケーションのスクリプト編集パネル
 * 
 * @author keeeeeent
 */
@JToolBarMixin.Setting()
@JPanelMixin.Setting(layout = JPanelMixin.Layout.BorderLayout, backgroudColor = 0xffffff)
public final class NonComponentScriptEditPanel extends AppPanel
		implements JPanelMixin, JButtonMixin, JToolBarMixin, Runnable {

	// ####################################################################################
	// # 共通定数
	// ####################################################################################

	/** スクリプトアクセッサー */
	private volatile KagerowScriptAccessor kagerowScriptAccessor;

	/** アクションコマンド（共通画面切替） */
	private static final String COMMON_BTN_CMD = "commmonBtn";
	/** アクションコマンド（KSQL画面切替） */
	private static final String SQL_BTN_CMD = "sqlBtn";
	/** アクションコマンド（プラグイン画面切替） */
	private static final String PLUGIN_BTN_CMD = "pluginBtn";
	/** アクションコマンド（コマンド画面切替） */
	private static final String CMD_BTN_CMD = "cmdBtn";

	// ####################################################################################
	// # 定数コンポーネント（GUI部品）
	// ####################################################################################

	/** ルートパネル */
	private final JPanel rootPanel = new JPanel();
	{
		setJPanel(rootPanel);
	}

	/** レイアウトマネージャー */
	private final CardLayout layout = (CardLayout) JPanelMixin.Layout.CardLayout.getLayout();

	/** 編集パネル */
	private final JPanel editPanel = new JPanel();
	{
		editPanel.setLayout(layout);
		rootPanel.add(editPanel, BorderLayout.CENTER);
	}

	/** ツールバーボタン（共通画面切替） */
	@JButtonMixin.Setting(title = GUIText.NonComponentSscriptEditPanel_001, actionCommand = COMMON_BTN_CMD)
	private final JButton commmonBtn = new JButton();
	/** ツールバーボタン（KSQL画面切替） */
	@JButtonMixin.Setting(title = GUIText.NonComponentSscriptEditPanel_002, actionCommand = SQL_BTN_CMD)
	private final JButton sqlBtn = new JButton();
	/** ツールバーボタン（プラグイン画面切替） */
	@JButtonMixin.Setting(title = GUIText.NonComponentSscriptEditPanel_003, actionCommand = PLUGIN_BTN_CMD)
	private final JButton pluginBtn = new JButton();
	/** ツールバーボタン（コマンド画面切替） */
	@JButtonMixin.Setting(title = GUIText.NonComponentSscriptEditPanel_004, actionCommand = CMD_BTN_CMD)
	private final JButton cmdBtn = new JButton();

	/** 共通編集パネル */
	public final NonComponentCommonEditPanel commonEditPanel;

	/** KSQL編集パネル */
	public final NonComponentSqlEditPanel sqlEditPanel;

	/** プラグイン編集パネル */
	private final NonComponentPluginEditPanel pluginEditPanel;

	/** コマンド編集パネル */
	private final NonComponentCommandEditPanel cmdEditPanel;

	/** 親コンポーネント */
	private final NonComponentScriptEditer nonComponentScriptEditer;

	/** ツールバー */
	private final JToolBar toolBar;
	{
		toolBar = createJToolBar();
		rootPanel.add(toolBar, BorderLayout.NORTH);
	}

	// ####################################################################################
	// # 共通処理
	// ####################################################################################

	/**
	 * デフォルトコンストラクタ
	 * @param kagerowScriptAccessor スクリプトアクセッサー
	 * @param nonComponentScriptEditer 親コンポーネント
	 * @param session KDBセッション
	 */
	NonComponentScriptEditPanel(KagerowScriptAccessor kagerowScriptAccessor,
			NonComponentScriptEditer nonComponentScriptEditer,
			KagerowExecutionPlanAccessor session) {

		// フィールド初期化
		this.kagerowScriptAccessor = kagerowScriptAccessor;
		this.nonComponentScriptEditer = nonComponentScriptEditer;

		// 共通編集パネル設定初期化
		{
			commonEditPanel = new NonComponentCommonEditPanel(this.kagerowScriptAccessor, this, session);
			JScrollPane scrollPane = new JScrollPane(commonEditPanel);
			scrollPane.getVerticalScrollBar().setUnitIncrement(30);
			scrollPane.setMinimumSize(new Dimension(0, 0));
			editPanel.add(scrollPane, COMMON_BTN_CMD);
		}

		// コマンド編集パネル設定初期化
		{
			cmdEditPanel = new NonComponentCommandEditPanel(this.kagerowScriptAccessor, this);
			JScrollPane scrollPane = new JScrollPane(cmdEditPanel);
			scrollPane.getVerticalScrollBar().setUnitIncrement(30);
			scrollPane.getHorizontalScrollBar().setUnitIncrement(10);
			scrollPane.setMinimumSize(new Dimension(0, 0));
			editPanel.add(scrollPane, CMD_BTN_CMD);
		}

		// KSQL編集パネル設定初期化
		{
			sqlEditPanel = new NonComponentSqlEditPanel(this.kagerowScriptAccessor, this, session);
			JScrollPane scrollPane = new JScrollPane(sqlEditPanel);
			scrollPane.getVerticalScrollBar().setUnitIncrement(30);
			scrollPane.getHorizontalScrollBar().setUnitIncrement(10);
			scrollPane.setMinimumSize(new Dimension(0, 0));
			editPanel.add(scrollPane, SQL_BTN_CMD);
		}

		// プラグイン編集パネル設定初期化
		{
			pluginEditPanel = new NonComponentPluginEditPanel(this.kagerowScriptAccessor, this);
			JScrollPane scrollPane = new JScrollPane(pluginEditPanel);
			scrollPane.getVerticalScrollBar().setUnitIncrement(30);
			scrollPane.getHorizontalScrollBar().setUnitIncrement(10);
			scrollPane.setMinimumSize(new Dimension(0, 0));
			editPanel.add(scrollPane, PLUGIN_BTN_CMD);
		}

		// ボタン設定初期化
		try {
			setJButton(this, commmonBtn);
			setJButton(this, sqlBtn);
			setJButton(this, pluginBtn);
			setJButton(this, cmdBtn);
		} catch (IllegalAccessException | NoSuchMethodException e) {
			KagerowLogger.newAppLogger().err(e);
		}

		// UI初期化
		initialize();

		// タブパネル初期位置指定
		layout.show(editPanel, COMMON_BTN_CMD);

		// オブザーバー追加
		addObserver(this);

		// 追加の初期化処理
		commonEditPanel.initialize();

	}

	/** {@inheritDoc} */
	@Override
	public Component getComponent() {
		return rootPanel;
	}

	/**
	 * 共通画面切り替え処理
	 * @param event イベント
	 */
	@ActionListenerMixin.ActionCommand(COMMON_BTN_CMD)
	private void commmonBtn(ActionEvent event) {
		switchPanel(event);
	}

	/**
	 * KSQL画面切り替え処理
	 * @param event イベント
	 */
	@ActionListenerMixin.ActionCommand(SQL_BTN_CMD)
	private void sqlBtn(ActionEvent event) {
		switchPanel(event);
	}

	/**
	 * プラグイン画面切り替え処理
	 * @param event イベント
	 */
	@ActionListenerMixin.ActionCommand(PLUGIN_BTN_CMD)
	private void pluginBtn(ActionEvent event) {
		switchPanel(event);
	}

	/**
	 * コマンド画面切り替え処理
	 * @param event イベント
	 */
	@ActionListenerMixin.ActionCommand(CMD_BTN_CMD)
	private void cmdBtn(ActionEvent event) {
		switchPanel(event);
	}

	/**
	 * アクションによって特定の画面に切り替えます
	 * @param event イベント
	 */
	private void switchPanel(ActionEvent event) {
		layout.show(editPanel, event.getActionCommand());
	}

	/** {@inheritDoc} */
	@Override
	public Component[] getLeftComponent() {
		return new Component[] {
				commmonBtn,
				sqlBtn,
				pluginBtn,
				cmdBtn
		};
	}

	/** {@inheritDoc} */
	@Override
	public Component getRightComponent() {
		return null;
	}

	/**
	 * スクリプトが新規作成時であることをモデルに通知します
	 */
	public void isNewCreate() {
		commonEditPanel.isNewCreate();
	}

	/**
	 * モデルの更新を行います
	 * @param kagerowScriptAccessor スクリプトアクセッサー
	 */
	public void refresh(KagerowScriptAccessor kagerowScriptAccessor) {
		// モデル切り替え
		this.kagerowScriptAccessor = kagerowScriptAccessor;
		// 通知
		this.noticeObserver();
	}

	/** {@inheritDoc} */
	@Override
	@SuppressWarnings("unchecked")
	public void run() {
		try {
			// モデルの変更を子コンポーネントへ通知
			for (Component con : new Component[] {
					commonEditPanel,
					sqlEditPanel,
					pluginEditPanel,
					cmdEditPanel
			}) {
				Consumer<KagerowScriptAccessor> accessor = (Consumer<KagerowScriptAccessor>) con;
				accessor.accept(kagerowScriptAccessor);
			}
			// モデルの変更を親コンポーネントへ通知
			nonComponentScriptEditer.setModel(kagerowScriptAccessor);
		} catch (Exception e) {
			KagerowLogger.newAppLogger().err(e);
		}
	}

}

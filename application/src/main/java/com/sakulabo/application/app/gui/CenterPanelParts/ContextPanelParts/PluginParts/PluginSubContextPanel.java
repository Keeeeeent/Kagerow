package com.sakulabo.application.app.gui.CenterPanelParts.ContextPanelParts.PluginParts;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import javax.naming.CannotProceedException;
import javax.naming.NamingException;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextArea;

import com.sakulabo.application.app.gui.CenterPanelParts.ContextPanelParts.ContextPanel;
import com.sakulabo.application.app.gui.CenterPanelParts.ContextPanelParts.SubContextPanel;
import com.sakulabo.application.common.code.GUIText;
import com.sakulabo.application.common.code.PluginSubContextPanelText;
import com.sakulabo.application.common.mixin.ActionListenerMixin;
import com.sakulabo.application.common.mixin.JButtonMixin;
import com.sakulabo.application.common.mixin.JLabelMixin;
import com.sakulabo.application.common.mixin.JPanelMixin;
import com.sakulabo.application.common.mixin.JPanelMixin.Layout;
import com.sakulabo.application.helper.DialogHelper;
import com.sakulabo.core.Kagerow.Contents.KagerowPluginContent;
import com.sakulabo.core.Kagerow.Contents.KagerowPluginContent.PluginParamInfo;
import com.sakulabo.core.Kagerow.Context.KagerowPluginContext;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;
import com.sakulabo.core.Kagerow.Utilities.KagerowUtilities;
import com.sakulabo.regulation.annotation.KagerowPlugin.PluginType;

/**
 * プラグイン詳細パネル実装クラスです
 * 
 * @author keeeeeent
 */
public class PluginSubContextPanel extends SubContextPanel {

	/** アクションコマンド（操作ボタン） */
	private static final String EDIT_CMD = GUIText.PluginSubContextPanel_001.toString();
	/** アクションコマンド（情報ボタン） */
	private static final String INFO_CMD = GUIText.PluginSubContextPanel_008.toString();
	/** アクションコマンド（入力ボタン） */
	private static final String INPUT_CMD = GUIText.PluginSubContextPanel_009.toString();
	/** アクションコマンド（出力ボタン） */
	private static final String OUTPUT_CMD = GUIText.PluginSubContextPanel_010.toString();

	/** コンテキスト */
	private final KagerowPluginContext context;
	/** プラグイン */
	private final KagerowPluginContent plugin;
	/** デフォルトパッケージフラグ */
	private final boolean isDefault;
	/** プラグイン名 */
	private final String name;

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
	private PluginTabPanel pluginTabPanel;
	{
		pluginTabPanel = KagerowUtilities.getBean(PluginTabPanel.class, null).get();
	}

	/**
	 * デフォルトコンストラクタ
	 * @param name プラグイン名称
	 * @param context コンテキスト
	 * @param plugin プラグイン
	 * @param isDefault デフォルトパッケージフラグ
	 */
	public PluginSubContextPanel(
			String name,
			KagerowPluginContext context,
			KagerowPluginContent plugin,
			boolean isDefault) {
		this.name = name;
		this.plugin = plugin;
		this.context = context;
		this.isDefault = isDefault;
	}

	/**
	 * 操作パネル定義
	 */
	@JPanelMixin.Setting(backgroudColor = 0xFFFFFF, layout = Layout.BorderLayout)
	private class EditPanel extends JPanel implements JPanelMixin, JButtonMixin, JLabelMixin {

		// ####################################################################################
		// # 無効化処理向けパーツ
		// ####################################################################################

		/** 無効コマンド */
		private static final String DISABLE_CMD = "DISABLE_CMD";

		/** 無効ボタン */
		@JButtonMixin.Setting(title = GUIText.PluginSubContextPanel_004, actionCommand = DISABLE_CMD)
		private JButton disBtn = new JButton();
		{
			// ボタン初期化処理
			try {
				setJButton(this, disBtn);
			} catch (IllegalAccessException | NoSuchMethodException e) {
				KagerowLogger.newAppLogger().err(e);
			}
		}
		/** 無効説明文 */
		private JTextArea disText = new JTextArea();
		{
			// 説明文追加
			disText.setText(GUIText.PluginSubContextPanel_002.toLineString());
			// レイアウト設定
			disText.setEditable(false);
			disText.setLineWrap(true);
			disText.setWrapStyleWord(true);
			disText.setOpaque(false);
			disText.setBorder(null);
		}
		/** 無効パネル */
		private JPanel disBtnPanel = new JPanel();
		{
			// レイアウト設定
			setJPanel(disBtnPanel);
			disBtnPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
			// コンポーネント配置
			disBtnPanel.add(disBtn);
		}
		/** 無効パネル */
		private JPanel disPanel = new JPanel();
		{
			// レイアウト設定
			setJPanel(disPanel);
			disPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
			// ボーダー設定
			disPanel.setBorder(BorderFactory.createTitledBorder(
					BorderFactory.createEtchedBorder(),
					GUIText.PluginSubContextPanel_003.toString()));
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
		@JButtonMixin.Setting(title = GUIText.PluginSubContextPanel_006, actionCommand = ENABLE_CMD)
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
			enaText.setText(GUIText.PluginSubContextPanel_007.toLineString());
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
					GUIText.PluginSubContextPanel_005.toString()));
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
			add(disPanel);
			add(enaPanel);
			// デフォルトパッケージの場合ボタンを無効化
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
			// プラグイン有効チェック
			if (context.isDisable(name)) {
				disPanel.setVisible(false);
				enaPanel.setVisible(true);
			} else {
				disPanel.setVisible(true);
				enaPanel.setVisible(false);
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
				// プラグイン無効化
				context.setDisable(true, name);
				// 成功ダイアログ
				dialogHelper.showSystemInfo(PluginSubContextPanelText.SUCCESS_DISABLE.toString());
				// タブクローズ
				pluginTabPanel.delTab(name);
				// コンテキスト変更通知
				contextPanel.noticeObserver();
			} catch (CannotProceedException e) {
				if (e.getCause() instanceof IOException) {
					// 無効化失敗の場合
					dialogHelper.showSystemError(PluginSubContextPanelText.FILE_DISABLE.toString());
				} else {
					// 予期せぬ例外
					dialogHelper.showSystemError(
							PluginSubContextPanelText.FILE_DISABLE_SYSTEM_ERROR.toString());
				}
				// ログ書き出し
				KagerowLogger.newAppLogger().err(e);
			} catch (NamingException e) {
				// 予期せぬ例外
				dialogHelper
						.showSystemError(PluginSubContextPanelText.FILE_DISABLE_SYSTEM_ERROR.toString());
				// ログ書き出し
				KagerowLogger.newAppLogger().err(e);
			} catch (Exception e) {
				// 予期せぬ例外
				dialogHelper
						.showSystemError(PluginSubContextPanelText.FILE_DISABLE_SYSTEM_ERROR.toString());
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
				// プラグイン有効化
				context.setDisable(true, name);
				// 成功ダイアログ
				dialogHelper.showSystemInfo(PluginSubContextPanelText.SUCCESS_ENABLE.toString());
				// タブクローズ
				pluginTabPanel.delTab(name);
				// コンテキスト変更通知
				contextPanel.noticeObserver();
			} catch (CannotProceedException e) {
				if (e.getCause() instanceof IOException) {
					// 有効化失敗の場合
					dialogHelper.showSystemError(PluginSubContextPanelText.FILE_ENABLE.toString());
				} else {
					// 予期せぬ例外
					dialogHelper.showSystemError(
							PluginSubContextPanelText.FILE_ENABLE_SYSTEM_ERROR.toString());
				}
				// ログ書き出し
				KagerowLogger.newAppLogger().err(e);
			} catch (NamingException e) {
				// 予期せぬ例外
				dialogHelper
						.showSystemError(PluginSubContextPanelText.FILE_ENABLE_SYSTEM_ERROR.toString());
				// ログ書き出し
				KagerowLogger.newAppLogger().err(e);
			} catch (Exception e) {
				// 予期せぬ例外
				dialogHelper
						.showSystemError(PluginSubContextPanelText.FILE_ENABLE_SYSTEM_ERROR.toString());
				// ログ書き出し
				KagerowLogger.newAppLogger().err(e);
			} finally {
				// 排他制御解除
				lock.unlock();
			}
		}

	}

	/**
	 * 情報パネル定義
	 */
	@JPanelMixin.Setting(backgroudColor = 0xFFFFFF, layout = Layout.BorderLayout)
	private class InfoPanel extends JPanel implements JPanelMixin, JLabelMixin {

		/** 情報テーブル */
		private PluginInfoMapTableModel model;
		/** 情報マッピング */
		private Map<String, String> data = new HashMap<>();

		/** 情報設定 */
		{
			KagerowPluginContent.PluginInfo info = plugin.toPluginInfo();
			data.put(GUIText.PluginSubContextPanel_011.toString(), info.packageName());
			data.put(GUIText.PluginSubContextPanel_012.toString(), info.pluginName());
			data.put(GUIText.PluginSubContextPanel_013.toString(), info.toVersion());
		}

		/**
		 * デフォルトコンストラクタ
		 */
		InfoPanel() {
			// 共通初期化処理実行
			setJPanel(this);
			// テーブル生成
			model = new PluginInfoMapTableModel(data);
			JTable table = new JTable();
			table.setModel(model);
			// テーブル罫線設定
			table.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
			table.getTableHeader().setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
			// コンポーネント配置
			add(table, BorderLayout.CENTER);
			add(table.getTableHeader(), BorderLayout.NORTH);
		}

	}

	/**
	 * 入力パネル定義
	 */
	@JPanelMixin.Setting(backgroudColor = 0xFFFFFF, layout = Layout.BorderLayout)
	private class InputParamPanel extends JPanel implements JPanelMixin, JLabelMixin {

		/** 情報テーブル */
		private PluginParamInfoListTableModel model;
		/** 情報マッピング */
		private List<PluginParamInfo> data = new ArrayList<>();

		/** 情報設定 */
		{
			KagerowPluginContent.PluginInfo info = plugin.toPluginInfo();
			data = info.param().get(PluginType.INPUT);
		}

		/**
		 * デフォルトコンストラクタ
		 */
		InputParamPanel() {
			// 共通初期化処理実行
			setJPanel(this);
			// テーブル生成
			model = new PluginParamInfoListTableModel(data);
			JTable table = new JTable();
			table.setModel(model);
			// テーブル罫線設定
			table.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
			table.getTableHeader().setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
			// コンポーネント配置
			add(table, BorderLayout.CENTER);
			add(table.getTableHeader(), BorderLayout.NORTH);
		}

	}

	/**
	 * 出力パネル定義
	 */
	@JPanelMixin.Setting(backgroudColor = 0xFFFFFF, layout = Layout.BorderLayout)
	private class OutputParamPanel extends JPanel implements JPanelMixin, JLabelMixin {

		/** 情報テーブル */
		private PluginParamInfoListTableModel model;
		/** 情報マッピング */
		private List<PluginParamInfo> data = new ArrayList<>();

		/** 情報設定 */
		{
			KagerowPluginContent.PluginInfo info = plugin.toPluginInfo();
			data = info.param().get(PluginType.OUTPUT);
		}

		/**
		 * デフォルトコンストラクタ
		 */
		OutputParamPanel() {
			// 共通初期化処理実行
			setJPanel(this);
			// テーブル生成
			model = new PluginParamInfoListTableModel(data);
			JTable table = new JTable();
			table.setModel(model);
			// テーブル罫線設定
			table.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
			table.getTableHeader().setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
			// コンポーネント配置
			add(table, BorderLayout.CENTER);
			add(table.getTableHeader(), BorderLayout.NORTH);
		}

	}

	/** {@inheritDoc} */
	@Override
	public Component getComponent() {
		Component component = super.getComponent();
		addAction(INFO_CMD, new InfoPanel());
		addAction(EDIT_CMD, new EditPanel());
		addAction(INPUT_CMD, new InputParamPanel());
		addAction(OUTPUT_CMD, new OutputParamPanel());
		return component;
	}

}

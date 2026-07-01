package com.sakulabo.application.app.gui.MenuPanelParts.SettingMenuParts;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.naming.NamingException;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextPane;

import com.sakulabo.application.common.code.CommonSettingPanelText;
import com.sakulabo.application.common.code.GUIText;
import com.sakulabo.application.common.mixin.ActionListenerMixin;
import com.sakulabo.application.common.mixin.JButtonMixin;
import com.sakulabo.application.common.mixin.JPanelMixin;
import com.sakulabo.application.common.mixin.JPanelMixin.Layout;
import com.sakulabo.application.common.mixin.JSplitPanelMixin;
import com.sakulabo.application.helper.DialogHelper;
import com.sakulabo.core.Kagerow.Contents.KagerowSettingContent;
import com.sakulabo.core.Kagerow.Context.KagerowSettingContext;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;
import com.sakulabo.core.Kagerow.Utilities.KagerowUtilities;

/**
 * Kagerow設定情報表示共通パネル実装クラスです
 * 
 * @author keeeeeent
 */
@JPanelMixin.Setting(backgroudColor = 0xFFFFFF, layout = Layout.BorderLayout)
@JSplitPanelMixin.Setting(orientation = JSplitPane.VERTICAL_SPLIT)
public class CommonSettingPanel extends JPanel implements JPanelMixin, JSplitPanelMixin, JButtonMixin {

	/** アクションコマンド（保存ボタン） */
	private static final String SAVE_CMD = "saveBtn";

	/** モデル */
	private volatile SettingInfoMapTableModel model;
	/** テーブル */
	private volatile JTable table;

	/** 設定コンテンツ */
	private final KagerowSettingContent cnt;
	/** 設定値変更テキストエラリア */
	private final JTextPane jTextPane = new JTextPane();

	/** 出力ボタン */
	@JButtonMixin.Setting(title = GUIText.CommonSettingPanel_001, actionCommand = SAVE_CMD)
	private final JButton saveBtn = new JButton();

	/** ボタンパネル */
	private JPanel buttonJpanel = new JPanel();
	{
		// レイアウト設定
		buttonJpanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		// コンポーネント配置
		buttonJpanel.add(saveBtn);
	}

	/**
	 * デフォルトコンストラクタ
	 * @param name ネームスペース
	 * @throws NamingException コンテキスト取得失敗
	 */
	CommonSettingPanel(String name) throws NamingException {

		// パネル初期化
		setJPanel(this);
		// ボタン設定
		try {
			setJButton(this, saveBtn);
		} catch (Exception e) {
			KagerowLogger.newAppLogger().err(e);
		}

		// 設定コンテンツ取得
		KagerowSettingContext cxt = KagerowUtilities.getContext(KagerowSettingContext._NAME);
		cnt = cxt.lookup(name);

		// 分割区画生成
		JSplitPane jSplitPane = createSplitPane();
		add(jSplitPane, BorderLayout.CENTER);
		add(buttonJpanel, BorderLayout.SOUTH);
		// 初期の位置を指定（下 30%）
		jSplitPane.setResizeWeight(0.7);
		jSplitPane.setDividerLocation(0.7);

	}

	/** {@inheritDoc} */
	@Override
	public Component getLeftComponent() {
		// 設定値格納マップ
		Map<String, String> setting = getSettings();
		// モデルへ変換
		model = new SettingInfoMapTableModel(setting, jTextPane);
		// テーブル初期化
		table = new JTable(model);
		return table;
	}

	/** {@inheritDoc} */
	@Override
	public Component getRightComponent() {
		return jTextPane;
	}

	/**
	 * 設定情報一覧を取得します
	 * @return 設定情報一覧
	 */
	private Map<String, String> getSettings() {
		// 設定値格納マップ
		Map<String, String> setting = new HashMap<>();
		try {
			// 設定値一覧を生成
			List<String> keys = cnt.settingKeySet();
			for (String key : keys) {
				String value = cnt.lookup(key);
				setting.put(key, value);
			}
		} catch (NamingException e) {
			KagerowLogger.newAppLogger().err(e);
		}
		return setting;
	}

	/**
	 * 保存ボタン
	 */
	@ActionListenerMixin.ActionCommand(SAVE_CMD)
	private void save() {
		if (Objects.nonNull(model)) {
			// ヘルパー初期化(DialogHelper)
			DialogHelper dialogHelper = KagerowUtilities.getBean(DialogHelper.class, null).get();
			// 変更データ取得
			String key = model.getSelectedKey();
			try {
				// 変更データ反映
				cnt.bind(key, jTextPane.getText());
				// 完了ポップアップで通知
				dialogHelper.showSystemInfo(CommonSettingPanelText.INFO_001.toString());
			} catch (NamingException e) {
				// エラーダイアログ表示
				dialogHelper.showSystemError(CommonSettingPanelText.ERROR_001.toString());
				// ログ書き出し
				KagerowLogger.newAppLogger().err(e);
			}
			// 設定値格納マップ
			Map<String, String> setting = getSettings();
			// モデルへ変換
			model = new SettingInfoMapTableModel(setting, jTextPane);
			// テーブルへ反映
			table.setModel(model);
		}
	}

}

package com.sakulabo.application.app.gui.MenuPanelParts.SettingMenuParts;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.naming.NamingException;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.sakulabo.application.app.gui.AppPanel;
import com.sakulabo.application.common.code.GUIText;
import com.sakulabo.application.common.initializer.GraphicComponent;
import com.sakulabo.application.common.mixin.AppMixin;
import com.sakulabo.application.common.mixin.JPanelMixin;
import com.sakulabo.application.common.mixin.JPanelMixin.Layout;
import com.sakulabo.application.common.mixin.JTabbedPaneMixin;
import com.sakulabo.application.helper.KagerowScriptHelper;
import com.sakulabo.core.Kagerow.Utilities.KagerowFileHeaderReader;
import com.sakulabo.regulation.annotation.KagerowComponent;

/**
 * 詳細設定メニュー実装クラスです
 * 
 * @author keeeeeent
 */
@KagerowComponent
@GraphicComponent
@AppMixin.Size(width = 450, height = 260)
@JPanelMixin.Setting(backgroudColor = 0xFFFFFF, layout = Layout.BorderLayout)
@JTabbedPaneMixin.Setting(setTabLayoutPolicy = JTabbedPane.WRAP_TAB_LAYOUT)
public class AdvancedSettingsPanel extends AppPanel implements JPanelMixin, JTabbedPaneMixin {

	/** 詳細設定パネル */
	private final JPanel settingPanel = new JPanel();
	{
		// パネル初期設定
		settingPanel.setLayout(new BorderLayout());
		// 余白設定
		settingPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
	}

	/** 各種設定パネル */
	private final JTabbedPane jTabbedPane = createTabbedPane();
	{
		settingPanel.add(jTabbedPane, BorderLayout.CENTER);
	}

	/**
	 * デフォルトコンストラクタ
	 * @throws NamingException コンテキスト取得失敗
	 */
	public AdvancedSettingsPanel() throws NamingException {

		// KSQL設定パネル追加
		jTabbedPane.addTab(GUIText.AdvancedSettingsPanel_001.toString(),
				new CommonSettingPanel(KagerowScriptHelper.class.getName()));
		// CSV取込設定パネル追加
		jTabbedPane.addTab(GUIText.AdvancedSettingsPanel_002.toString(),
				new CommonSettingPanel(KagerowFileHeaderReader.class.getName()));

		// 詳細設定パネル追加
		add(settingPanel, BorderLayout.CENTER);

	}

	/** {@inheritDoc} */
	@Override
	public Component getComponent() {
		return settingPanel;
	}

}

package com.sakulabo.application.app.gui;

import java.awt.Component;
import java.util.Objects;

import javax.swing.JMenuBar;

import com.sakulabo.application.app.gui.MenuPanelParts.FileMenuParts.FileMenu;
import com.sakulabo.application.app.gui.MenuPanelParts.HelpMenuParts.HelpMenu;
import com.sakulabo.application.app.gui.MenuPanelParts.SettingMenuParts.SettingMenu;
import com.sakulabo.application.common.initializer.GraphicComponent;
import com.sakulabo.regulation.annotation.KagerowComponent;
import com.sakulabo.regulation.annotation.KagerowInject;

/**
 * GUIアプリケーションのメニューパネル実装クラスです
 * 
 * @author keeeeeent
 */
@KagerowComponent
@GraphicComponent
public class MenuPanel extends AppPanel {

	/** メニューバー */
	private volatile JMenuBar menuBar;
	/** メニューバー(ファイル編集) */
	@KagerowInject
	private FileMenu fileMenu;
	/** メニューバー(設定編集) */
	@KagerowInject
	private SettingMenu settingMenu;
	/** メニューバー(ヘルプ画面) */
	@KagerowInject
	private HelpMenu helpMenu;

	/** {@inheritDoc} */
	@Override
	public Component getComponent() {
		if (Objects.isNull(menuBar)) {
			synchronized (this) {
				if (Objects.isNull(menuBar)) {
					menuBar = new JMenuBar();
					menuBar.add(fileMenu);
					menuBar.add(settingMenu);
					menuBar.add(helpMenu);
				}
			}
		}
		return menuBar;
	}

}

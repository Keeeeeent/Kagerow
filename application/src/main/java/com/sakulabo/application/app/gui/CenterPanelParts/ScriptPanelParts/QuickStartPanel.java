package com.sakulabo.application.app.gui.CenterPanelParts.ScriptPanelParts;

import java.awt.Component;

import javax.swing.JDesktopPane;

import com.sakulabo.application.app.gui.AppPanel;
import com.sakulabo.application.common.code.GUIText;
import com.sakulabo.application.common.initializer.GraphicComponent;
import com.sakulabo.application.common.mixin.AppMixin;
import com.sakulabo.regulation.annotation.KagerowComponent;

/**
 * GUIアプリケーションのクイックスターター操作パネルクラスです
 * 
 * @author keeeeeent
 */
@KagerowComponent
@GraphicComponent
public class QuickStartPanel extends AppPanel implements AppMixin {

	/** デスクトップ */
	private JDesktopPane desktopPane = new JDesktopPane();

	/** {@inheritDoc} */
	@Override
	public Component getComponent() {
		return desktopPane;
	}

	/**
	 * クイックスタートタブテキストを返却します
	 * @return タブテキスト
	 */
	public String getTitle() {
		return GUIText.QuickStartPanel_001.toString();
	}

}

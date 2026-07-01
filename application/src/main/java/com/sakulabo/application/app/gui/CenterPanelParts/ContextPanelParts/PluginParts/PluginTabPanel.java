package com.sakulabo.application.app.gui.CenterPanelParts.ContextPanelParts.PluginParts;

import com.sakulabo.application.app.gui.AppTabPanel;
import com.sakulabo.application.common.initializer.GraphicComponent;
import com.sakulabo.regulation.annotation.KagerowComponent;

/**
 * プラグイン詳細パネルクラスです
 * 
 * @author keeeeeent
 */
@KagerowComponent
@GraphicComponent
public class PluginTabPanel extends AppTabPanel {

	/** タブ最大数 */
	private static final int MAX_SIZE = 20;

	/**
	 * デフォルトコンストラクタ
	 */
	public PluginTabPanel() {
		super(MAX_SIZE);
	}

}

package com.sakulabo.application.app.gui.CenterPanelParts.ContextPanelParts.CacheParts;

import com.sakulabo.application.app.gui.AppTabPanel;
import com.sakulabo.application.common.initializer.GraphicComponent;
import com.sakulabo.regulation.annotation.KagerowComponent;

/**
 * キャッシュ詳細パネルクラスです
 * 
 * @author keeeeeent
 */
@KagerowComponent
@GraphicComponent
public class CacheTabPanel extends AppTabPanel {

	/** タブ最大数 */
	private static final int MAX_SIZE = 30;

	/**
	 * デフォルトコンストラクタ
	 */
	public CacheTabPanel() {
		super(MAX_SIZE);
	}

}
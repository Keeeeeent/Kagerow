package com.sakulabo.application.app.gui.CenterPanelParts.ContextPanelParts.SchemaParts;

import com.sakulabo.application.app.gui.AppTabPanel;
import com.sakulabo.application.common.initializer.GraphicComponent;
import com.sakulabo.regulation.annotation.KagerowComponent;

/**
 * 仮想FSテーブル詳細パネルクラスです
 * 
 * @author keeeeeent
 */
@KagerowComponent
@GraphicComponent
public class VirtualFileTableTabPanel extends AppTabPanel {

	/** タブ最大数 */
	private static final int MAX_SIZE = 50;

	/**
	 * デフォルトコンストラクタ
	 */
	public VirtualFileTableTabPanel() {
		super(MAX_SIZE);
	}

}

package com.sakulabo.application.app.gui.CenterPanelParts.ContextPanelParts.SchemaParts;

import com.sakulabo.application.app.gui.AppTabPanel;
import com.sakulabo.application.common.initializer.GraphicComponent;
import com.sakulabo.regulation.annotation.KagerowComponent;

/**
 * 仮想FSスキーマ詳細パネルクラスです
 * 
 * @author keeeeeent
 */
@KagerowComponent
@GraphicComponent
public class VirtualFileSchemaTabPanel extends AppTabPanel {

	/** タブ最大数 */
	private static final int MAX_SIZE = 30;

	/**
	 * デフォルトコンストラクタ
	 */
	public VirtualFileSchemaTabPanel() {
		super(MAX_SIZE);
	}

}

package com.sakulabo.application.app.gui.CenterPanelParts.ContextPanelParts.PluginParts;

import java.awt.event.MouseEvent;
import java.util.List;

import com.sakulabo.application.app.gui.CommonTreeNode;
import com.sakulabo.application.service.Context.PluginContextService.PluginContextInfo;
import com.sakulabo.core.Kagerow.Context.KagerowPluginPackageContext;
import com.sakulabo.core.Kagerow.Utilities.KagerowUtilities;

/**
 * プラグインパッケージノードクラスです
 * 
 * @author keeeeeent
 */
public class PluginPkgTreeNode extends CommonTreeNode {

	/** 表示名称 */
	private final String lookUpName;

	/** 詳細パネル */
	private PluginPkgTabPanel tabpanel;
	{
		tabpanel = KagerowUtilities.getBean(PluginPkgTabPanel.class, null).get();
	}

	/**
	 * デフォルトコンストラクタ
	 * @param pluginPackageName プラグインパッケージ名称
	 * @param pluginInfoList プラグイン情報リスト
	 */
	protected PluginPkgTreeNode(String pluginPackageName, List<PluginContextInfo> pluginInfoList) {
		super(pluginInfoList.getFirst().viewName());
		this.lookUpName = pluginPackageName;
		pluginInfoList.stream()
				.map(PluginSubTreeNode::new)
				.forEach(this::add);
	}

	/** {@inheritDoc} */
	@Override
	protected void doMouseClicked(MouseEvent e) {
		;
	}

	/** {@inheritDoc} */
	@Override
	protected void doMousePressed(MouseEvent e) {
		doAction(e);
	}

	/** {@inheritDoc} */
	@Override
	protected void doMouseReleased(MouseEvent e) {
		doAction(e);
	}

	/**
	 * クリックイベント処理メソッド
	 * @param e イベント
	 */
	private void doAction(MouseEvent e) {
		// 右クリックか判定
		if (e.isPopupTrigger()) {
			// デフォルトパッケージか判定
			boolean isDefault = KagerowPluginPackageContext.DEFAULT_PKG_NAME.equals(lookUpName);
			// 追加するコンポーネント生成
			PluginPkgContextPanel panel = new PluginPkgContextPanel(lookUpName, isDefault);
			// システムスキーマでない場合アクション実行
			tabpanel.addTab(name, panel);
		}
	}

}

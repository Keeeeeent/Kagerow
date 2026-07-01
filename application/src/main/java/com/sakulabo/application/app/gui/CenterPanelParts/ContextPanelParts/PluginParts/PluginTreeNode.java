package com.sakulabo.application.app.gui.CenterPanelParts.ContextPanelParts.PluginParts;

import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.sakulabo.application.app.gui.CommonTreeNode;
import com.sakulabo.application.common.initializer.GraphicComponent;
import com.sakulabo.application.controller.Context.PluginContextController;
import com.sakulabo.application.service.Context.PluginContextService.PluginContextInfo;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;
import com.sakulabo.regulation.annotation.KagerowComponent;
import com.sakulabo.regulation.annotation.KagerowInject;

/**
 * プラグインノードクラスです
 * 
 * @author keeeeeent
 */
@KagerowComponent
@GraphicComponent
public class PluginTreeNode extends CommonTreeNode {

	/** ロガー */
	@KagerowInject
	protected KagerowLogger logger;
	/** プラグインコントローラー	 */
	@KagerowInject
	private PluginContextController pluginContextController;

	/**
	 * デフォルトコンストラクタ
	 */
	public PluginTreeNode() {
		// 名称設定
		super("Plugin");
	}

	/**
	 * コンポーネントの初期化処理を実行します
	 */
	public void initialize() {
		// プラグイン一覧取得
		List<PluginContextInfo> pluginList = pluginContextController.getPluginList();
		// 子ノードを全て削除
		removeAllChildren();
		// コンテキストからノードを作成
		Map<String, List<PluginContextInfo>> pluginMap = pluginList
				.stream()
				.collect(Collectors.groupingBy(PluginContextInfo::packageName));

		for (Map.Entry<String, List<PluginContextInfo>> entry : pluginMap.entrySet()) {
			PluginPkgTreeNode node = new PluginPkgTreeNode(entry.getKey(), entry.getValue());
			add(node);
		}

	}

	/** {@inheritDoc} */
	@Override
	protected void doMouseClicked(MouseEvent e) {
		;
	}

	/** {@inheritDoc} */
	@Override
	protected void doMousePressed(MouseEvent e) {
		;
	}

	/** {@inheritDoc} */
	@Override
	protected void doMouseReleased(MouseEvent e) {

	}

}

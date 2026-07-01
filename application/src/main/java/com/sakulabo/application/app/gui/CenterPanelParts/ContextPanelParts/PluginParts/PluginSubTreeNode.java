package com.sakulabo.application.app.gui.CenterPanelParts.ContextPanelParts.PluginParts;

import java.awt.event.MouseEvent;

import javax.naming.NamingException;

import com.sakulabo.application.app.gui.CommonTreeNode;
import com.sakulabo.application.service.Context.PluginContextService.PluginContextInfo;
import com.sakulabo.core.Kagerow.Contents.KagerowPluginContent;
import com.sakulabo.core.Kagerow.Context.KagerowPluginContext;
import com.sakulabo.core.Kagerow.Context.KagerowPluginPackageContext;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;
import com.sakulabo.core.Kagerow.Utilities.KagerowUtilities;

/**
 * プラグインノードクラスです
 * 
 * @author keeeeeent
 */
public class PluginSubTreeNode extends CommonTreeNode {

	/** 詳細パネル */
	private PluginTabPanel tabpanel;
	{
		tabpanel = KagerowUtilities.getBean(PluginTabPanel.class, null).get();
	}

	/** コンテンツ */
	private KagerowPluginContent content;
	/** コンテキスト */
	private KagerowPluginContext context;
	/** デフォルトパッケージフラグ */
	private final boolean isDefault;

	/**
	 * デフォルトコンストラクタ
	 * @param pluginInfo プラグイン情報
	 */
	protected PluginSubTreeNode(PluginContextInfo pluginInfo) {
		super(pluginInfo.pluginName());
		// デフォルトパッケージか判定
		isDefault = KagerowPluginPackageContext.DEFAULT_PKG_NAME.equals(pluginInfo.packageName());
		try {
			// パッケージコンテキスト取得
			KagerowPluginPackageContext pctx = KagerowUtilities.getContext(KagerowPluginPackageContext._NAME);
			// プラグインコンテキスト取得
			context = pctx.lookup(pluginInfo.packageName());
			// コンテンツ設定
			content = context.lookup(name);
		} catch (NamingException e) {
			KagerowLogger.newAppLogger().err(e);
		}
	}

	/** {@inheritDoc} */
	@Override
	protected void doMouseClicked(MouseEvent e) {
		// ダブルクリックか判定
		if (e.getClickCount() == 2) {
			// 追加するコンポーネント生成
			PluginSubContextPanel panel = new PluginSubContextPanel(name, context, content, isDefault);
			// システムスキーマでない場合アクション実行
			tabpanel.addTab(name, panel);
		}
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

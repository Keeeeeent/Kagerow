package com.sakulabo.application.app.gui.CenterPanelParts.ContextPanelParts.SchemaParts;

import java.awt.event.MouseEvent;

import javax.naming.NamingException;

import com.sakulabo.application.app.gui.CommonTreeNode;
import com.sakulabo.core.Kagerow.Context.KagerowVirtualDirContext;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;
import com.sakulabo.core.Kagerow.Utilities.KagerowUtilities;

/**
 * 仮想FSテーブルノードクラスです
 * 
 * @author keeeeeent
 */
public class VirtualFileTableTreeNode extends CommonTreeNode {

	/** 詳細パネル */
	private VirtualFileTableTabPanel tabpanel;
	{
		tabpanel = KagerowUtilities.getBean(VirtualFileTableTabPanel.class, null).get();
	}

	/** コンテンツ */
	private KagerowVirtualDirContext content;
	/** テーブル名称 */
	private final String tableName;

	/**
	 * デフォルトコンストラクタ
	 * @param name シノニム名称
	 * @param tableName テーブル名称
	 * @param content コンテンツ
	 */
	protected VirtualFileTableTreeNode(String name, String tableName, KagerowVirtualDirContext content) {
		super(name);
		this.tableName = tableName;
		this.content = content;
	}

	/** {@inheritDoc} */
	@Override
	protected void doMouseClicked(MouseEvent e) {
		// ダブルクリックか判定
		if (e.getClickCount() == 2) {
			try {
				// 追加するコンポーネント生成
				VirtualFileTableSubContextPanel panel = new VirtualFileTableSubContextPanel(content, name, tableName);
				// システムスキーマでない場合アクション実行
				tabpanel.addTab(name, panel);
			} catch (NamingException exp) {
				KagerowLogger.newAppLogger().err(exp);
			}
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

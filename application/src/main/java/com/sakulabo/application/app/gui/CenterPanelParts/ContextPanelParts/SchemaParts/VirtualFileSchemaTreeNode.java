package com.sakulabo.application.app.gui.CenterPanelParts.ContextPanelParts.SchemaParts;

import java.awt.event.MouseEvent;

import javax.naming.NamingException;

import com.sakulabo.application.app.gui.CommonTreeNode;
import com.sakulabo.core.Kagerow.Context.KagerowVirtualDirContext;
import com.sakulabo.core.Kagerow.Context.KagerowVirtualFileContext;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;
import com.sakulabo.core.Kagerow.Utilities.KagerowUtilities;

/**
 * 仮想FSスキーマノードクラスです
 * 
 * @author keeeeeent
 */
public class VirtualFileSchemaTreeNode extends CommonTreeNode {

	/** 詳細パネル */
	private VirtualFileSchemaTabPanel tabpanel;
	{
		tabpanel = KagerowUtilities.getBean(VirtualFileSchemaTabPanel.class, null).get();
	}

	/**
	 * デフォルトコンストラクタ
	 * @param name スキーマ名称
	 */
	protected VirtualFileSchemaTreeNode(String name) {
		super(name);
		// システム管理のスキーマか判定
		if (!KagerowVirtualFileContext.SYSTEM_SCHEMA.equals(name)) {
			try {
				// 仮想FSコンテキスト取得
				KagerowVirtualFileContext context = (KagerowVirtualFileContext) KagerowUtilities
						.getContext(KagerowVirtualFileContext._NAME);
				// コンテンツ取得
				final KagerowVirtualDirContext content = context.lookup(name);
				// シノニム一覧を取得
				content.getSynonymMapList()
						.entrySet()
						.stream()
						.map(entry -> new VirtualFileTableTreeNode(entry.getKey(), entry.getValue(), content))
						.forEach(this::add);
			} catch (NamingException e) {
				// ロジック的にコンテンツのルックアップ失敗はありえないが
				// 予期せぬ例外も考慮し処理をしておく
				KagerowLogger.newAppLogger().err(e);
			}
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
			// システムスキーマか判定
			if (!KagerowVirtualFileContext.SYSTEM_SCHEMA.equals(name)) {
				// 追加するコンポーネント生成
				VirtualFileSchemaSubContextPanel panel = new VirtualFileSchemaSubContextPanel(name);
				// システムスキーマでない場合アクション実行
				tabpanel.addTab(name, panel);
			}
		}
	}

}

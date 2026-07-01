package com.sakulabo.application.app.gui.CenterPanelParts.ContextPanelParts.CacheParts;

import java.awt.event.MouseEvent;

import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import com.sakulabo.application.app.gui.CommonTreeNode;
import com.sakulabo.core.Kagerow.Contents.KagerowCacheContent;
import com.sakulabo.core.Kagerow.Context.KagerowCacheContext;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;
import com.sakulabo.core.Kagerow.Utilities.KagerowUtilities;

/**
 * キャッシュサブノードクラスです
 * 
 * @author keeeeeent
 */
public class CacheTreeSubNode extends CommonTreeNode {

	/** 詳細パネル */
	private CacheTabPanel tabpanel;
	{
		tabpanel = KagerowUtilities.getBean(CacheTabPanel.class, null).get();
	}

	/**
	 * デフォルトコンストラクタ
	 * @param name スキーマ名称
	 */
	protected CacheTreeSubNode(String name) {
		super(name);
		try {
			// コンテキスト取得
			KagerowCacheContext context = (KagerowCacheContext) KagerowUtilities
					.getContext(KagerowCacheContext._NAME);
			// コンテンツ取得
			final KagerowCacheContent content = context.lookup(name);
			// キャッシュ一覧を取得
			NamingEnumeration<NameClassPair> list = content.list((String) null);
			while (list.hasMore()) {
				NameClassPair info = list.next();
				CacheTreeFileNode node = new CacheTreeFileNode(info.getName());
				this.add(node);
			}
		} catch (NamingException e) {
			// ロジック的にコンテンツのルックアップ失敗はありえないが
			// 予期せぬ例外も考慮し処理をしておく
			KagerowLogger.newAppLogger().err(e);
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
			// 追加するコンポーネント生成
			CacheSubContextPanel panel = new CacheSubContextPanel(name);
			// システムスキーマでない場合アクション実行
			tabpanel.addTab(name, panel);
		}
	}

}

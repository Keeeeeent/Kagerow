package com.sakulabo.application.app.gui.CenterPanelParts.ContextPanelParts.CacheParts;

import java.awt.event.MouseEvent;

import javax.naming.NamingException;

import com.sakulabo.application.app.gui.CommonTreeNode;
import com.sakulabo.application.common.initializer.GraphicComponent;
import com.sakulabo.core.Kagerow.Context.KagerowCacheContext;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;
import com.sakulabo.core.Kagerow.Utilities.KagerowUtilities;
import com.sakulabo.regulation.annotation.KagerowComponent;
import com.sakulabo.regulation.annotation.KagerowInject;

/**
 * キャッシュノードクラスです
 * 
 * @author keeeeeent
 */
@KagerowComponent
@GraphicComponent
public final class CacheTreeNode extends CommonTreeNode {

	/** ロガー */
	@KagerowInject
	protected KagerowLogger logger;

	/**
	 * デフォルトコンストラクタ
	 */
	public CacheTreeNode() {
		// 名称設定
		super("Cache");
	}

	/**
	 * コンポーネントの初期化処理を実行します
	 */
	public void initialize() {
		try {
			// コンテキスト取得
			KagerowCacheContext context = (KagerowCacheContext) KagerowUtilities
					.getContext(KagerowCacheContext._NAME);
			// 子ノードを全て削除
			removeAllChildren();
			// コンテキストからノードを作成
			context.nameList()
					.stream()
					.map(CacheTreeSubNode::new)
					.forEach(this::add);
		} catch (NamingException e) {
			logger.err(e);
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

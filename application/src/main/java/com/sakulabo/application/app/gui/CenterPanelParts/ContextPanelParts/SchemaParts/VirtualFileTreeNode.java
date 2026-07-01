package com.sakulabo.application.app.gui.CenterPanelParts.ContextPanelParts.SchemaParts;

import java.awt.event.MouseEvent;

import javax.naming.NamingException;

import com.sakulabo.application.app.gui.CommonTreeNode;
import com.sakulabo.application.common.initializer.GraphicComponent;
import com.sakulabo.core.Kagerow.Context.KagerowVirtualFileContext;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;
import com.sakulabo.core.Kagerow.Utilities.KagerowUtilities;
import com.sakulabo.regulation.annotation.KagerowComponent;
import com.sakulabo.regulation.annotation.KagerowInject;

/**
 * 仮想FSノードクラスです
 * 
 * @author keeeeeent
 */
@KagerowComponent
@GraphicComponent
public final class VirtualFileTreeNode extends CommonTreeNode {

	/** ロガー */
	@KagerowInject
	protected KagerowLogger logger;

	/**
	 * デフォルトコンストラクタ
	 */
	public VirtualFileTreeNode() {
		// 名称設定
		super("Schema");
	}

	/**
	 * コンポーネントの初期化処理を実行します
	 */
	@SuppressWarnings("removal")
	public void initialize() {
		try {
			// 仮想FSコンテキスト取得
			KagerowVirtualFileContext context = (KagerowVirtualFileContext) KagerowUtilities
					.getContext(KagerowVirtualFileContext._NAME);
			// 子ノードを全て削除
			removeAllChildren();
			// コンテキストからノードを作成
			context.getContext()
					.keySet()
					.stream()
					.map(VirtualFileSchemaTreeNode::new)
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

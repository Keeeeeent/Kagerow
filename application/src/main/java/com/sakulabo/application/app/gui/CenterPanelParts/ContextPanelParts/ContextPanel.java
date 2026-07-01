package com.sakulabo.application.app.gui.CenterPanelParts.ContextPanelParts;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.Objects;

import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import com.sakulabo.application.app.gui.AppPanel;
import com.sakulabo.application.app.gui.CommonTreeNode;
import com.sakulabo.application.app.gui.CenterPanelParts.ContextPanelParts.CacheParts.CacheTreeNode;
import com.sakulabo.application.app.gui.CenterPanelParts.ContextPanelParts.PluginParts.PluginTreeNode;
import com.sakulabo.application.app.gui.CenterPanelParts.ContextPanelParts.SchemaParts.VirtualFileTreeNode;
import com.sakulabo.application.common.initializer.GraphicComponent;
import com.sakulabo.application.common.mixin.AppMixin;
import com.sakulabo.application.controller.Context.PluginContextController;
import com.sakulabo.regulation.annotation.KagerowComponent;
import com.sakulabo.regulation.annotation.KagerowInject;

/**
 * GUIアプリケーションのコンテキスト操作パネルクラスです
 * 
 * @author keeeeeent
 */
@KagerowComponent
@GraphicComponent
public class ContextPanel extends AppPanel implements AppMixin, Runnable {

	/** プラグインコントローラー	 */
	@KagerowInject
	private PluginContextController pluginContextController;
	/** Kagerowデータノードツリー */
	private volatile JTree jTree;
	/** プラグインノードツリー */
	@KagerowInject
	private PluginTreeNode pluginNode;
	/** 仮想ファイルシステムノードツリー */
	@KagerowInject
	private VirtualFileTreeNode schemaFileNode;
	/** キャッシュノードツリー */
	@KagerowInject
	private CacheTreeNode cacheFileNode;
	/** ルートノード */
	private final DefaultMutableTreeNode root = new DefaultMutableTreeNode("Kagerow");

	// ####################################################################################
	// # 内部クラス宣言
	// ####################################################################################

	/** ダブルクリックイベント監視アダプター */
	private class MouseAdapterImpl extends MouseAdapter {

		/** {@inheritDoc} */
		@Override
		public void mousePressed(MouseEvent e) {
			// クリック位置を取得
			TreePath path = jTree.getPathForLocation(e.getX(), e.getY());
			// パスを取得でき場合、詳細画面を表示する
			if (path != null) {
				Object node = path.getLastPathComponent();
				if (node instanceof CommonTreeNode commonNode) {
					if (!commonNode.isShow()) {
						commonNode.mousePressed(e);
					}
				}
			}
		}

		/** {@inheritDoc} */
		@Override
		public void mouseReleased(MouseEvent e) {
			// クリック位置を取得
			TreePath path = jTree.getPathForLocation(e.getX(), e.getY());
			// パスを取得でき場合、詳細画面を表示する
			if (path != null) {
				Object node = path.getLastPathComponent();
				if (node instanceof CommonTreeNode commonNode) {
					if (!commonNode.isShow()) {
						commonNode.mouseReleased(e);
					}
				}
			}
		}

		/** {@inheritDoc} */
		@Override
		public void mouseClicked(MouseEvent e) {
			if (SwingUtilities.isLeftMouseButton(e)) {
				// クリック位置を取得
				TreePath path = jTree.getPathForLocation(e.getX(), e.getY());
				// パスを取得でき場合、詳細画面を表示する
				if (path != null) {
					Object node = path.getLastPathComponent();
					if (node instanceof CommonTreeNode commonNode) {
						if (!commonNode.isShow()) {
							commonNode.mouseClicked(e);
						}
					}
				}
			}
		}

	}

	/** {@inheritDoc} */
	@Override
	public Component getComponent() {
		if (Objects.isNull(jTree)) {
			synchronized (this) {
				if (Objects.isNull(jTree)) {
					// コンポーネント生成
					TreeModel model = createTreeModel();
					jTree = new JTree(model);
					jTree.addMouseListener(new MouseAdapterImpl());
					// 自分自身をリスナー登録
					addObserver(this);
				}
			}
		}
		return jTree;
	}

	/**
	 * Kagerowデータノードツリーを構築します
	 * @return 構築済みKagerowデータノードツリー
	 */
	private TreeModel createTreeModel() {

		// ノードモデルを生成
		DefaultTreeModel treeModel = new DefaultTreeModel(root);

		// プラグインノード生成&追加
		pluginNode.initialize();
		if (pluginNode.getChildCount() != 0) {
			root.add(pluginNode);
		}

		// 仮想FSノード生成&追加
		schemaFileNode.initialize();
		if (schemaFileNode.getChildCount() != 0) {
			root.add(schemaFileNode);
		}

		// キャッシュノード生成
		cacheFileNode.initialize();
		if (cacheFileNode.getChildCount() != 0) {
			root.add(cacheFileNode);
		}

		return treeModel;
	}

	/** {@inheritDoc} */
	@Override
	public void run() {
		SwingUtilities.invokeLater(() -> {
			Enumeration<TreePath> expanded = jTree.getExpandedDescendants(new TreePath(jTree.getModel().getRoot()));
			TreeModel model = createTreeModel();
			jTree.setModel(model);
			if (expanded != null) {
				while (expanded.hasMoreElements()) {
					jTree.expandPath(expanded.nextElement());
				}
			}
		});
	}

}

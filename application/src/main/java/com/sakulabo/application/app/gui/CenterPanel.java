package com.sakulabo.application.app.gui;

import java.awt.Component;
import java.util.Objects;

import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import com.sakulabo.application.app.gui.CenterPanelParts.ContextPanelParts.ContextPanel;
import com.sakulabo.application.app.gui.CenterPanelParts.ScriptPanelParts.ScriptPanel;
import com.sakulabo.application.common.initializer.GraphicComponent;
import com.sakulabo.application.common.mixin.JSplitPanelMixin;
import com.sakulabo.regulation.annotation.KagerowComponent;
import com.sakulabo.regulation.annotation.KagerowInject;

/**
 * GUIアプリケーションのメインパネル実装クラスです
 * 
 * @author keeeeeent
 */
@KagerowComponent
@GraphicComponent
@JSplitPanelMixin.Setting(orientation = JSplitPane.HORIZONTAL_SPLIT)
public class CenterPanel extends AppPanel implements JSplitPanelMixin {

	/** KSQLスクリプト操作パネル */
	@KagerowInject
	private ScriptPanel scriptPanel;

	/** Kagerowコンテキスト操作パネル */
	@KagerowInject
	private ContextPanel contextPanel;

	/** 分割区画 */
	private volatile JSplitPane basePanel;

	/** {@inheritDoc} */
	@Override
	public Component getComponent() {
		if (Objects.isNull(basePanel)) {
			synchronized (this) {
				if (Objects.isNull(basePanel)) {
					// スプリットパネル生成
					basePanel = createSplitPane();
				}
			}
		}
		return basePanel;
	}

	/**
	 * GUI描画初期化処理実施
	 */
	public void lazyInitialize() {
		SwingUtilities.invokeLater(() -> {
			// 初期の位置を指定（左 30%）
			basePanel.setResizeWeight(0.3);
			basePanel.setDividerLocation(0.3);
			// スクリプトパネルの初期位置を指定
			scriptPanel.lazyInitialize();
		});
	}

	/** {@inheritDoc} */
	@Override
	public Component getLeftComponent() {
		contextPanel.initialize();
		JScrollPane scrollPane = new JScrollPane(contextPanel);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.getVerticalScrollBar().setUnitIncrement(30);
		return scrollPane;
	}

	/** {@inheritDoc} */
	@Override
	public Component getRightComponent() {
		scriptPanel.initialize();
		return scriptPanel;
	}

}

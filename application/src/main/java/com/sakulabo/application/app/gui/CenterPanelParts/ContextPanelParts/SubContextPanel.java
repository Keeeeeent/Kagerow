package com.sakulabo.application.app.gui.CenterPanelParts.ContextPanelParts;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.Objects;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import com.sakulabo.application.app.gui.AppPanel;
import com.sakulabo.application.common.mixin.JPanelMixin;
import com.sakulabo.application.common.mixin.JSplitPanelMixin;

/**
 * GUIアプリケーションのサブコンテキスト操作パネル基底クラスです
 * 
 * @author keeeeeent
 */
@JSplitPanelMixin.Setting(orientation = JSplitPane.HORIZONTAL_SPLIT)
public abstract class SubContextPanel extends AppPanel implements JSplitPanelMixin {

	/** レイアウトマネージャー */
	private final CardLayout layout = (CardLayout) JPanelMixin.Layout.CardLayout.getLayout();

	/** ボタンパネル */
	private final JPanel leftComponent = new JPanel();
	/** ボタンメイン */
	private final JPanel rightComponent = new JPanel();
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
					// 分割区画の位置固定
					basePanel.setEnabled(false);
				}
			}
		}
		return basePanel;
	}

	/** {@inheritDoc} */
	@Override
	public final Component getLeftComponent() {
		leftComponent.setLayout(new BoxLayout(leftComponent, BoxLayout.Y_AXIS));
		return leftComponent;
	}

	/** {@inheritDoc} */
	@Override
	public final Component getRightComponent() {
		rightComponent.setLayout(layout);
		JScrollPane scrollPane = new JScrollPane(rightComponent);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.getVerticalScrollBar().setUnitIncrement(30);
		return scrollPane;
	}

	/**
	 * アクションによって特定の画面に切り替えます
	 * @param event イベント
	 */
	private void switchPanel(ActionEvent event) {
		layout.show(rightComponent, event.getActionCommand());
	}

	/**
	 * アクションとセットになるコンポーネントを追加します
	 * @param btn アクションコマンド
	 * @param component コンポーネント
	 */
	protected final void addAction(String btn, Component component) {
		// ボタン生成&追加
		JButton button = new JButton(btn);
		button.setActionCommand(btn);
		button.addActionListener(this::switchPanel);
		leftComponent.add(button);
		// コンポーネント追加
		rightComponent.add(component, btn);
	}

}

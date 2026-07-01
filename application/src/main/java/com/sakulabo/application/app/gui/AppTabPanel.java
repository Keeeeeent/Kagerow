package com.sakulabo.application.app.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.Objects;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;

import com.sakulabo.application.common.code.GUIText;
import com.sakulabo.application.common.image.ButtonImage;
import com.sakulabo.application.common.mixin.ActionListenerMixin;
import com.sakulabo.application.common.mixin.AppMixin;
import com.sakulabo.application.common.mixin.JButtonMixin;
import com.sakulabo.application.common.mixin.JFrameMixin;
import com.sakulabo.application.common.mixin.JPanelMixin;
import com.sakulabo.application.common.mixin.JPanelMixin.Layout;
import com.sakulabo.application.common.mixin.JTabbedPaneMixin;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;

/**
 * GUIアプリケーションのタブパネル基底クラスです
 * 
 * @author keeeeeent
 */
@JTabbedPaneMixin.Setting(setTabLayoutPolicy = JTabbedPane.WRAP_TAB_LAYOUT)
@JPanelMixin.Setting(layout = Layout.BorderLayout, backgroudColor = 0xFFFFFF)
@AppMixin.Size(height = 400, width = 600)
@JFrameMixin.Setting(closeOperation = JFrame.HIDE_ON_CLOSE, title = "KagerowSQL")
public abstract class AppTabPanel extends AppPanel implements JTabbedPaneMixin, JPanelMixin, JFrameMixin {

	/** タブパネル */
	private JTabbedPane tabbedPane;
	/** タブ最大数 */
	private final int size;
	/** フレーム */
	public final JFrame frame;

	/**
	 * デフォルトコンストラクタ
	 * @param size パネル最大サイズ
	 */
	protected AppTabPanel(int size) {
		setSize(panel);
		// フィールド初期化
		this.size = size;
		// フレーム初期化
		frame = createFrame();
		setSize(frame);
		// フレームの設定初期化
		frame.setLayout(new BorderLayout());
		// リサイズ禁止
		frame.setResizable(false);
	}

	/*
	 * タブレイアウト制御パネル
	 */
	@SuppressWarnings("javadoc")
	@JPanelMixin.Setting(layout = Layout.FlowLayout)
	private class ClosableTabComponent extends JPanel implements JButtonMixin {

		/** 閉じるボタン押下コマンド */
		static final String CMD = "close";

		/** クローズボタン */
		@JButtonMixin.Setting(actionCommand = CMD, title = GUIText.EMPTY, icon = ButtonImage.CLOSE)
		final JButton closeButton = new JButton();

		/**
		 * デフォルトコンストラクタ 
		 */
		ClosableTabComponent() {

			// レイアウト指定
			setOpaque(false);
			// タイトル指定
			JLabel titleLabel = new JLabel() {
				@Override
				public String getText() {
					int index = tabbedPane.indexOfTabComponent(ClosableTabComponent.this);
					return index != -1 ? tabbedPane.getTitleAt(index) : "";
				}
			};
			// ボタン設定
			try {
				setJButton(this, closeButton);
			} catch (IllegalAccessException | NoSuchMethodException e) {
				KagerowLogger.newAppLogger().err(e);
			}
			// レイアウト追加
			add(titleLabel);
			add(closeButton);
		}

		/**
		 * クローズボタン押下イベント
		 */
		@ActionListenerMixin.ActionCommand(CMD)
		private void close() {
			int index = tabbedPane.indexOfTabComponent(this);
			if (index != -1) {
				tabbedPane.removeTabAt(index);
			}
			// 最後の要素の場合、フレームクローズ
			if (tabbedPane.getTabCount() == 0) {
				frame.setVisible(false);
			}
		}

	}

	/** パネル */
	private final JPanel panel = new JPanel();
	{
		setJPanel(panel);
	}

	/** {@inheritDoc} */
	@Override
	public Component getComponent() {
		if (Objects.isNull(tabbedPane)) {
			synchronized (this) {
				if (Objects.isNull(tabbedPane)) {
					// タブパネル生成
					tabbedPane = createTabbedPane();
					// パネルに追加
					panel.add(tabbedPane, BorderLayout.CENTER);
					// フレームにパネルを追加
					frame.getContentPane().add(panel);
					// 背景色をデフォルトへ変更
					panel.setBackground(UIManager.getColor("Panel.background"));
				}
			}
		}
		return panel;
	}

	/**
	 * タブを追加します
	 * @param title タブタイトル
	 * @param panel 追加パネル
	 * @return 追加結果
	 */
	public final boolean addTab(String title, AppPanel panel) {

		// コンポーネント初期化（初回呼び出しのみ有効）
		getComponent();

		// 生合成チェック
		Objects.requireNonNull(title);
		Objects.requireNonNull(panel);

		// 現在のタブ保持数を確認し最大許容数を超えている場合は何もせず終了
		if (size < tabbedPane.getTabCount()) {
			return false;
		}
		// タイトルが既に表示中か確認
		boolean isAdded = false;
		for (int i = 0; i < tabbedPane.getTabCount(); i++) {
			if (title.equals(tabbedPane.getTitleAt(i))) {
				tabbedPane.setSelectedIndex(i);
				isAdded = true;
			}
		}
		// タブパネルの最初の要素の場合、フレームを表示
		if (0 < tabbedPane.getTabCount() || !frame.isVisible()) {
			frame.setVisible(true);
		}
		// 追加されていない場合、タブ追加処理を実行
		if (!isAdded) {
			// パネル初期化
			panel.initialize();
			// パネル追加
			tabbedPane.addTab(title, panel);
			// クローズボタン追加
			int index = tabbedPane.indexOfComponent(panel);
			tabbedPane.setTabComponentAt(index, new ClosableTabComponent());
		}
		// 結果返却
		return true;
	}

	/**
	 * タブを削除します
	 * @param title タブタイトル
	 * @return 削除結果
	 */
	public final boolean delTab(String title) {

		// 生合成チェック
		Objects.requireNonNull(title);

		for (int i = 0; i < tabbedPane.getTabCount(); i++) {
			if (title.equals(tabbedPane.getTitleAt(i))) {
				ClosableTabComponent closableTabComponent = (ClosableTabComponent) tabbedPane.getTabComponentAt(i);
				closableTabComponent.close();
				return true;
			}
		}

		return false;

	}

}

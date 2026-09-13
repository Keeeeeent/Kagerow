package com.sakulabo.application.app.gui.MenuPanelParts.HelpMenuParts;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.sakulabo.application.app.gui.AppPanel;
import com.sakulabo.application.app.gui.MainFrame;
import com.sakulabo.application.common.code.GUIText;
import com.sakulabo.application.common.initializer.GraphicComponent;
import com.sakulabo.application.common.mixin.ActionListenerMixin;
import com.sakulabo.application.common.mixin.AppMixin;
import com.sakulabo.application.common.mixin.JButtonMixin;
import com.sakulabo.application.common.mixin.JLabelMixin;
import com.sakulabo.application.common.mixin.JPanelMixin;
import com.sakulabo.application.common.mixin.JPanelMixin.Layout;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;
import com.sakulabo.regulation.annotation.KagerowComponent;
import com.sakulabo.regulation.annotation.KagerowInject;

/**
 * ヘルプパネル共通実装リストパネルクラスです
 * 
 * @author keeeeeent
 */
@KagerowComponent
@GraphicComponent
@AppMixin.Size(width = 450, height = 260)
@JPanelMixin.Setting(backgroudColor = 0xFFFFFF, layout = Layout.BorderLayout)
public class CommonHelpListPanel extends AppPanel
		implements JButtonMixin, JPanelMixin, JLabelMixin, AutoCloseable {

	/** ロガー */
	@KagerowInject
	private KagerowLogger logger;
	/** メインフレーム */
	@KagerowInject
	private MainFrame frame;

	/** 終了アクションコマンド */
	private static final String CLOSE_ACTION = "CLOSE";
	/** ダイアログ */
	private volatile JDialog dialog;
	/** 排他制御ロック */
	private final ReentrantLock lock = new ReentrantLock();

	/**
	 * JList向け内部データ構造
	 * 
	 * @param title リストタイトル
	 * @param text  表示内容
	 */
	private static record ListData(String title, CharSequence text) {

		/** {@inheritDoc} */
		@Override
		public final String toString() {
			return title;
		}

	}

	/** 表示スクロールパネル */
	private final JPanel textPanel = new JPanel();
	{
		// パネル初期設定
		setJPanel(textPanel);
		// 余白設定
		textPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 30, 30));
	}

	/** 本文 */
	private final JTextArea text = new JTextArea();
	{
		// 文章の編集を禁止
		text.setEditable(false);
		text.setLineWrap(true);
		text.setWrapStyleWord(true);
		text.setCaretPosition(0);
		text.setFocusable(false);
		text.getCaret().setVisible(false);
		text.setHighlighter(null);
		text.setBorder(null);
	}

	/** パネルのタイトル */
	@JLabelMixin.Setting(text = GUIText.DisclaimerPanel_001, width = 150, heigth = 35)
	private JLabel dateFormatLabel = new JLabel();
	{
		// ラベル初期化
		setJLabel(dateFormatLabel);
		// 余白設定
		dateFormatLabel.setBorder(BorderFactory
				.createEmptyBorder(0, 30, 0, 0));
		// フォントを太文字＋サイズ変更
		dateFormatLabel.setFont(dateFormatLabel.getFont().deriveFont(Font.BOLD, 24f));
		// 背景色変更
		dateFormatLabel.setBackground(Color.WHITE);
	}

	/** 終了ボタン */
	@JButtonMixin.Setting(title = GUIText.DisclaimerPanel_003, actionCommand = CLOSE_ACTION)
	private JButton closeBtn = new JButton();

	/** ボタンパネル */
	private final JPanel btnPanel = new JPanel();
	{
		// レイアウト設定
		btnPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
	}

	/** リストモデル */
	private volatile DefaultListModel<ListData> model = new DefaultListModel<>();
	/** リスト */
	private final JList<ListData> jList = new JList<>(model);
	{
		jList.setPreferredSize(new Dimension(100, 200));
		jList.setMaximumSize(new Dimension(100, Integer.MAX_VALUE));
		jList.addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting()) {
				ListData value = jList.getSelectedValue();
				if (Objects.nonNull(value)) {
					text.setText(Objects.toString(value.text()));
					text.setCaretPosition(0);
				}
			}
		});
	}

	/**
	 * デフォルトコンストラクタ
	 * 
	 * @throws Exception
	 */
	public CommonHelpListPanel() throws Exception {

		// 文章を設定
		textPanel.add(text);
		JScrollPane scrollpane = new JScrollPane(textPanel);
		scrollpane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollpane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollpane.setPreferredSize(new Dimension(300, 200));
		scrollpane.setMaximumSize(new Dimension(300, Integer.MAX_VALUE));
		// ボタン設定
		setJButton(this, closeBtn);
		// ボタン追加
		btnPanel.add(closeBtn);
		// サブタイトル追加
		add(dateFormatLabel, BorderLayout.NORTH);
		// リスト追加
		add(jList, BorderLayout.WEST);
		// 本文追加
		add(scrollpane, BorderLayout.CENTER);
		// ボタンパネル追加
		add(btnPanel, BorderLayout.SOUTH);

	}

	/** {@inheritDoc} */
	@Override
	public Component getComponent() {
		return textPanel;
	}

	/**
	 * ダイアログを生成します
	 * 
	 * @param dialog ダイアログインスタンス
	 * @param title  ダイアログタイトル
	 * @return パネル
	 */
	public JPanel getComponent(JDialog dialog, String title) {
		lock.lock();
		this.jList.clearSelection();
		this.jList.setModel(model);
		this.jList.setSelectedIndex(0);
		this.dialog = Objects.requireNonNull(dialog);
		this.dateFormatLabel.setText(title);
		return (JPanel) getComponent();
	}

	/**
	 * リストにデータを追加します
	 * 
	 * @param title リストタイトル
	 * @param text  表示内容
	 */
	public void addList(String title, CharSequence text) {
		ListData data = new ListData(title, text);
		model.addElement(data);
	}

	/**
	 * 終了アクション実行処理
	 */
	@ActionListenerMixin.ActionCommand(CLOSE_ACTION)
	private void closeAction() {
		dialog.dispose();
		this.close();
	}

	/** {@inheritDoc} */
	@Override
	public void close() {
		model = new DefaultListModel<>();
		if (lock.isLocked()) {
			lock.unlock();
		}
	}
}

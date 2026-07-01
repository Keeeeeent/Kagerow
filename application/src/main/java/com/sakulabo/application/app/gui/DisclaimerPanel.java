package com.sakulabo.application.app.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;

import javax.naming.NamingException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;

import com.sakulabo.application.common.code.GUIText;
import com.sakulabo.application.common.initializer.GraphicComponent;
import com.sakulabo.application.common.mixin.ActionListenerMixin;
import com.sakulabo.application.common.mixin.AppMixin;
import com.sakulabo.application.common.mixin.JButtonMixin;
import com.sakulabo.application.common.mixin.JLabelMixin;
import com.sakulabo.application.common.mixin.JPanelMixin;
import com.sakulabo.application.common.mixin.JPanelMixin.Layout;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;
import com.sakulabo.core.Kagerow.Utilities.KagerowUtilities;
import com.sakulabo.regulation.annotation.KagerowComponent;
import com.sakulabo.regulation.annotation.KagerowInject;

/**
 * 免責事項同意パネル実装クラスです
 * 
 * @author keeeeeent
 */
@KagerowComponent
@GraphicComponent
@AppMixin.Size(width = 330, height = 200)
@JPanelMixin.Setting(backgroudColor = 0xFFFFFF, layout = Layout.BorderLayout)
public final class DisclaimerPanel extends AppPanel implements AppMixin, JButtonMixin, JPanelMixin, JLabelMixin {

	/** ロガー */
	@KagerowInject
	private KagerowLogger logger;
	/** メインフレーム */
	@KagerowInject
	private MainFrame frame;

	/** 同意アクションコマンド */
	private static final String AGREE_ACTION = "AGREE";
	/** 終了アクションコマンド */
	private static final String CLOSE_ACTION = "CLOSE";

	/** 日本語免責事項文章（ハードコーディングを行い可能な限り改変不可能とする） */
	private static final String JAPAN_TEXT = """
			免責事項

			本ソフトウェアは MIT ライセンスのもとで提供されています。

			本ソフトウェアは「現状のまま（AS IS）」提供され、
			明示的または黙示的を問わず、商品性、特定目的への適合性、
			および権利非侵害を含むいかなる保証も行いません。

			本ソフトウェアの使用または使用不能により生じたいかなる損害
			（データ損失、業務停止、利益損失等を含むがこれに限定されない）
			についても、作者は一切の責任を負いません。

			本ソフトウェアの利用は利用者自身の責任において行ってください。
			""";
	/** 英語免責事項文章（ハードコーディングを行い可能な限り改変不可能とする） */
	private static final String COMMON_TEXT = """
			Disclaimer

			This software is distributed under the MIT License.

			This software is provided "AS IS", without warranty of any kind,
			express or implied, including but not limited to warranties of
			merchantability, fitness for a particular purpose, and noninfringement.

			In no event shall the authors be liable for any claim, damages,
			or other liability arising from the use of this software.

			Use of this software is entirely at your own risk.
			""";

	/** 免責事項表示スクロールパネル */
	private final JPanel disclaimerText = new JPanel();
	{
		// パネル初期設定
		setJPanel(disclaimerText);
		// 余白設定
		disclaimerText.setBorder(BorderFactory.createEmptyBorder(20, 30, 30, 30));
	}

	/** 免責事項本文 */
	/** 免責事項本文 */
	private final JTextPane text = new JTextPane();
	{
		// 文章の編集を禁止
		text.setEditable(false);
	}

	/** 免責事項表示パネルのタイトル */
	@JLabelMixin.Setting(text = GUIText.DisclaimerPanel_001, width = 150, heigth = 35)
	private JLabel dateFormatLabel = new JLabel();
	{
		// ラベル初期化
		setJLabel(dateFormatLabel);
		// 余白設定
		dateFormatLabel.setBorder(BorderFactory.createEmptyBorder(0, 30, 0, 0));
		// フォントを太文字＋サイズ変更
		dateFormatLabel.setFont(dateFormatLabel.getFont().deriveFont(Font.BOLD, 24f));
		// 背景色変更
		dateFormatLabel.setBackground(Color.WHITE);
	}

	/** 同意ボタン */
	@JButtonMixin.Setting(title = GUIText.DisclaimerPanel_002, actionCommand = AGREE_ACTION)
	private JButton agreeBtn = new JButton();
	/** 終了ボタン */
	@JButtonMixin.Setting(title = GUIText.DisclaimerPanel_003, actionCommand = CLOSE_ACTION)
	private JButton closeBtn = new JButton();

	/** ボタンパネル */
	private final JPanel btnPanel = new JPanel();
	{
		// レイアウト設定
		btnPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
	}

	/**
	 * デフォルトコンストラクタ
	 * @throws Exception 
	 */
	public DisclaimerPanel() throws Exception {

		// 免責事項文章を設定
		text.setText(getDisclaimerText());
		disclaimerText.add(text);
		// ボタン設定
		setJButton(this, agreeBtn);
		setJButton(this, closeBtn);
		// ボタン追加
		btnPanel.add(closeBtn);
		btnPanel.add(agreeBtn);
		// サブタイトル追加
		add(dateFormatLabel, BorderLayout.NORTH);
		// 免責事項本文追加
		add(disclaimerText, BorderLayout.CENTER);
		// ボタンパネル追加
		add(btnPanel, BorderLayout.SOUTH);

	}

	/**
	 * 免責事項文章を生成します
	 * @return 生成された文章
	 */
	public String getDisclaimerText() {
		// ロケールを確認
		Locale locale = Locale.getDefault();
		if (locale.equals(Locale.JAPANESE) || locale.equals(Locale.JAPAN)) {
			// 日本語ロケールの場合、日本語の免責事項を表示
			return JAPAN_TEXT;
		} else {
			// その他の場合、英語の免責事項を表示
			return COMMON_TEXT;
		}
	}

	/** {@inheritDoc} */
	@Override
	public Component getComponent() {
		return disclaimerText;
	}

	/**
	 * 過去に免責事項に同意しているか判定します
	 * @return 同意済みの場合true
	 */
	public boolean checkAgreeAction() {
		// 設定ファイル読み込み
		String agreeTime = KagerowUtilities.getSetting(getClass().getName(), AGREE_ACTION);
		// 結果判定
		return Objects.nonNull(agreeTime);
	}

	/**
	 * 同意アクション事項処理
	 */
	@ActionListenerMixin.ActionCommand(AGREE_ACTION)
	private void agreeAction() {
		try {
			// 設定ファイルに書き込み
			KagerowUtilities.setSetting(getClass().getName(), AGREE_ACTION, Instant.now().toString());
			// ダイアログクローズ
			frame.closeDialog();
		} catch (NamingException | IllegalAccessException e) {
			// ログ書き込み
			logger.err(e);
			// 本来あり得ないがこのタイミングで例外が発生した場合、同意した証跡を保管できない可能性がある
			// そのためアプリケーションを異常終了とする
			System.exit(1);
		}
	}

	/**
	 * 終了アクション実行処理
	 */
	@ActionListenerMixin.ActionCommand(CLOSE_ACTION)
	private void closeAction() {
		// 終了ボタン押下の場合そのまま正常終了
		System.exit(0);
	}

}

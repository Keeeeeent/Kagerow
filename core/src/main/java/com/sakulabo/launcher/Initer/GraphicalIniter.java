package com.sakulabo.launcher.Initer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import com.sakulabo.core.Common.StringUtils;
import com.sakulabo.core.Common.VMOption;
import com.sakulabo.launcher.Initer.InitProcessFailedException.FailType;
import com.sakulabo.launcher.Initer.Impl.InitLockFile;

/**
 * GUIによる初期化処理の機能を提供する基底クラスです
 * @author keeeeeent
 */
public final class GraphicalIniter extends Initer implements Runnable {

	/** ウィンドウインスタンス */
	private final JFrame frame;
	/** プログレスバー */
	private final JProgressBar progressBar;
	/** メッセージ */
	private final JLabel message;
	/** 初期化処理アダプターインスタンス */
	private Initer initProcess = new AllInit();

	// サイズ情報保管レコード
	@SuppressWarnings("javadoc")
	private static record Size(int width, int heigth) {

		private static final Size FRAME = new Size(600, 400);
		private static final Size PROGRESSBAR = new Size(600, 3);
		private static final Size MESSAGE = new Size(400, 20);
		private static final Size VERSION = new Size(100, 20);

		/**
		 * dimensionにレコードを変換します
		 * @return ディメンション
		 */
		@SuppressWarnings("unused")
		Dimension toDimension() {
			return new Dimension(width, heigth);
		}
	}

	/** メッセージキー(重複起動エラーメッセージ) */
	private final static String DUPLICATE_LAUNCH = "duplicate-launch";

	/**
	 * デフォルトのコンストラクタ
	 */
	public GraphicalIniter() {

		/** オブザーバーの登録 */
		InitProcessor.observer.add(this);

		/** ウィンドウの設定 */
		frame = new JFrame();
		// ウィンドウサイズ設定
		frame.setSize(Size.FRAME.width(), Size.FRAME.heigth());
		// リサイズ禁止
		frame.setResizable(false);
		// 中方に表示
		frame.setLocationRelativeTo(null);
		// 閉じるボタン、最小化ボタン非表示
		frame.setUndecorated(true);
		// レイアウトマネージャー設定
		frame.setLayout(new BorderLayout());
		// 背景色透明化
		frame.setBackground(new Color(0, 0, 0, 0));
		// 透明度
		frame.setOpacity(1f);
		// 終了時アクション
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		/** レイヤー区画生成 */
		JLayeredPane layerPanel = new JLayeredPane();
		frame.getContentPane().add(layerPanel, BorderLayout.CENTER);

		/** アプリケーションイメージ画面 */
		JLabel label = new JLabel();
		try {
			// イメージファイルパス生成
			URL url = getClass().getResource("/image/application.png");
			// イメージインスタンス生成
			ImageIcon image = new ImageIcon(ImageIO.read(url));
			label.setIcon(image);
			// レイアウトの配置設定
			label.setBounds(0, 0, Size.FRAME.width(), Size.FRAME.heigth());
		} catch (IOException e) {
			// jarファイルにイメージファイルは組み込み為
			// 基本的に例外は発生しない想定
			e.printStackTrace();
		}
		layerPanel.add(label, JLayeredPane.DEFAULT_LAYER);

		/** プログレスバーの設定 */
		progressBar = new JProgressBar();
		progressBar.setMaximum(InitProcessor.MAX_COUNT.get());
		progressBar.setBounds(0, Size.FRAME.heigth - 5, Size.PROGRESSBAR.width(), Size.PROGRESSBAR.heigth());
		layerPanel.add(progressBar, JLayeredPane.PALETTE_LAYER);

		/** メッセージ */
		{
			int x = Size.FRAME.width / 20, y = Size.FRAME.heigth - 50,
					w = Size.MESSAGE.width(), h = Size.MESSAGE.heigth();
			message = new JLabel();
			message.setForeground(Color.WHITE);
			message.setBounds(x, y, w, h);
			layerPanel.add(message, JLayeredPane.PALETTE_LAYER);
		}

		/** バージョン */
		{
			int x = (Size.FRAME.width / 20) + Size.MESSAGE.width() + 30,
					y = Size.FRAME.heigth - 50,
					w = Size.VERSION.width(), h = Size.VERSION.heigth();
			String versionInfo = VMOption.APP_VERSION.getVMoption();
			String versionText = "Ver " + Objects.toString(versionInfo);
			JLabel version = new JLabel(versionText);
			version.setForeground(Color.WHITE);
			version.setBounds(x, y, w, h);
			layerPanel.add(version, JLayeredPane.PALETTE_LAYER);
		}

		/** ウィンドウ表示 */
		frame.setVisible(true);
	}

	/** {@inheritDoc} */
	@Override
	public void doInitProcessAll() throws InitProcessFailedException {
		try {
			// 初期化処理実施
			initProcess.doInitProcessAll();
			// 初期化処理終了後、画面待機
			TimeUnit.SECONDS.sleep(2);
		} catch (Exception e) {
			ERROR: {
				// 2つ前の例外を解析する
				if (Objects.nonNull(e.getCause()) &&
						e.getCause().getCause() instanceof InitProcessFailedException target) {
					// 例外スロー元のインスタンスが重複起動抑止プロセッサーでなおかつ
					// 個別エラーフラグがオンの場合、重複起動ダイアログを表示する
					if (InitLockFile.class.isAssignableFrom(target.getThrowClass())
							&& Objects.nonNull(target.getFlug())) {
						pringDialog(target.getFlug());
						break ERROR;
					} else {
						// 上記以外の場合は例外をそのままスロー
						pringDialog();
					}
				}
				// 基本的にありえないが想定外のエラーの場合は即時アプリケーションを終了する
				throw new InitProcessFailedException(e, FailType.Reject);
			}
		} finally {
			// 初期化プロセスが全て完了次第フレームをクローズ
			frame.dispose();
		}
	}

	/**
	 * 重複起動時のダイアログを表示します
	 * @param pid PID
	 */
	private void pringDialog(Object pid) {
		String message = createMesssage(Initer.PREFIX, DUPLICATE_LAUNCH, Objects.toString(pid));
		createDialog(message);
	}

	/**
	 * 通常のエラーダイアログを表示します
	 */
	private void pringDialog() {
		String message = createMesssage(Initer.PREFIX, StringUtils.DEFAULT);
		createDialog(message);
	}

	/**
	 * ダイアログインスタンスを生成します<br/>
	 * このダイアログ表示が終了するとJVMを強制終了します
	 * @param message ダイアログメッセージ
	 */
	private void createDialog(String message) {
		// ダイアログ表示
		JOptionPane.showMessageDialog(frame, message, createMesssage("dialog", "title"), JOptionPane.WARNING_MESSAGE);
		// アプリケーション強制終了
		System.exit(FailType.Reject.getExitCode());
	}

	/** {@inheritDoc} */
	@Override
	public void run() {
		// Swingスレッドで実行（UIにリアルタイムで反映する為）
		SwingUtilities.invokeLater(() -> {
			int counter = InitProcessor.COUNT.get();
			if (3 < counter) {
				InitProcessor processer = initProcessList.get(counter - 1);
				String msg = processer.createLogMessage();
				message.setText(msg);
			}
			progressBar.setValue(counter);
		});
	}

}

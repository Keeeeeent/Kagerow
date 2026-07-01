package com.sakulabo.application.app.gui;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

import com.sakulabo.application.common.code.GUIText;
import com.sakulabo.application.common.initializer.GraphicComponent;
import com.sakulabo.application.common.mixin.AppMixin;
import com.sakulabo.application.common.mixin.AppMixin.PreferredSize;
import com.sakulabo.application.common.mixin.JPanelMixin;
import com.sakulabo.core.Kagerow.KagerowApplication;
import com.sakulabo.regulation.annotation.KagerowComponent;

/**
 * GUIアプリケーションの通知パネル実装クラスです
 * 
 * @author keeeeeent
 */
@KagerowComponent
@GraphicComponent
@PreferredSize(height = 30, width = 0)
@JPanelMixin.Setting(layout = JPanelMixin.Layout.FlowLayout)
public class NoticePanel extends AppPanel implements JPanelMixin {

	/** バージョン情報パネル */
	private final JPanel version = new JPanel();
	{
		// バージョン情報取得
		String version = GUIText.NoticePanel_001.toString(
				new Object[] {
						KagerowApplication.getVersion()
				});
		// ラベル生成
		JLabel label = new JLabel(version);
		this.version.add(label);
		// レイアウト設定
		setJPanel(this.version);
	}
	/** アプリケーション情報パネル */
	private final JPanel applicationStatus = new JPanel();
	{
		// レイアウト設定
		this.applicationStatus.setLayout(new GridLayout(1, 3, 10, 0));
		this.applicationStatus.setBorder(new EmptyBorder(0, 0, 0, 0));
	}

	/**
	 * CPU使用率表示設定
	 */
	private final JPanel applicationCpuStatus;
	{
		class tmp extends absInnerNoticePanel {

			tmp() {
				super(GUIText.NoticePanel_002.toString());
			}

			/** {@inheritDoc} */
			@Override
			public void run() {
				// 監視インスタンス取得
				com.sun.management.OperatingSystemMXBean mxBean = (com.sun.management.OperatingSystemMXBean) ManagementFactory
						.getOperatingSystemMXBean();
				// 使用中CPU数取得
				int cpuCount = Runtime.getRuntime().availableProcessors();
				// CPU使用率取得
				double rawLoad = mxBean.getProcessCpuLoad();
				if (rawLoad > 1.0d) {
					// 1.0以上の場合、全コア合計とみなして割る
					rawLoad /= cpuCount;
				}
				// CPU使用率を再計算
				double usagePercent = rawLoad * 100.0d;
				// UI反映
				bar.setValue((int) usagePercent);
				bar.setString(String.format("%d%%", (int) usagePercent));
			}

		}

		// コンポーネント生成
		applicationCpuStatus = new tmp();
	}

	/**
	 * メモリ使用率表示設定
	 */
	private final JPanel applicationMenStatus;
	{
		class tmp extends absInnerNoticePanel {

			tmp() {
				super(GUIText.NoticePanel_003.toString());
			}

			/** {@inheritDoc} */
			@Override
			public void run() {
				// 監視インスタンス取得
				MemoryMXBean mxBean = ManagementFactory.getMemoryMXBean();
				// ヒープメモリ使用量
				MemoryUsage heapUsage = mxBean.getHeapMemoryUsage();
				double used = heapUsage.getUsed();
				double max = heapUsage.getMax();
				double usagePercent = used / max * 100;
				// UI反映
				bar.setValue((int) usagePercent);
				bar.setString(String.format("%d%%", (int) usagePercent));
			}

		}

		// コンポーネント生成
		applicationMenStatus = new tmp();
	}

	/**
	 * ストレージ使用率表示設定
	 */
	private final JPanel applicationIoStatus;
	{
		class tmp extends absInnerNoticePanel {

			tmp() {
				super(GUIText.NoticePanel_004.toString());
			}

			/** {@inheritDoc} */
			@Override
			public void run() {
				try {
					Path path = Paths.get(System.getProperty("user.home"));
					FileStore store = Files.getFileStore(path);
					// ストレージ使用量
					double total = store.getTotalSpace();
					double used = total - store.getUnallocatedSpace();
					double usagePercent = used / total * 100;
					// UI反映
					bar.setValue((int) usagePercent);
					bar.setString(String.format("%d%%", (int) usagePercent));
				} catch (Exception ignore) {
					;
				}

			}

		}

		// コンポーネント生成
		applicationIoStatus = new tmp();
	}

	/** 通知パネル */
	private final JPanel noticePanel = new JPanel();
	{
		// コンポーネントの配置
		noticePanel.add(version);
		noticePanel.addComponentListener(new ComponentAdapterImple());
		noticePanel.add(applicationStatus);

		// パネルの設定
		noticePanel.setLayout(null);
		UIrefresh();
	}

	/**
	 * 画面リサイズ対応リスナー
	 */
	private final class ComponentAdapterImple extends ComponentAdapter {
		/** {@inheritDoc} */
		@Override
		public void componentResized(ComponentEvent e) {
			UIrefresh();
		}
	}

	/**
	 * 各種ステータスバー規定UIクラス
	 */
	@AppMixin.PreferredSize(height = 20, width = 100)
	private abstract class absInnerNoticePanel extends JPanel implements Runnable, AppMixin {

		/** プログレスバーラベル */
		private final JLabel label;
		/** プログレスバー */
		protected final JProgressBar bar = new JProgressBar();
		{
			this.bar.setMaximum(100);
			this.bar.setStringPainted(true);
			this.bar.setBorder(new EmptyBorder(0, 0, 0, 0));
			this.setPreferredSize(this.bar);
			this.add(bar);
		}

		/**
		 * デフォルトコンストラクタ
		 * @param label
		 */
		absInnerNoticePanel(String label) {
			// コンポーネント設定
			this.label = new JLabel(label);
			this.add(this.label);
			this.setBorder(new EmptyBorder(0, 0, 0, 0));
			// コンポーネント配置
			NoticePanel.this.applicationStatus.add(this);
		}

	}

	/** UI更新用スレッド更新対象 */
	private final Runnable[] NOTICE_UI_LIST = {
			(Runnable) applicationCpuStatus,
			(Runnable) applicationMenStatus,
			(Runnable) applicationIoStatus
	};

	/** UI更新用スレッド */
	private final Timer NOTICE_UI_TIMER;
	{
		NOTICE_UI_TIMER = new Timer(2000, _ -> {
			for (Runnable r : NOTICE_UI_LIST)
				r.run();
		});
		NOTICE_UI_TIMER.start();
	}

	/**
	 * デフォルトコンストラクタ
	 */
	public NoticePanel() {
		setPreferredSize(noticePanel);
	}

	/** {@inheritDoc} */
	@Override
	public Component getComponent() {
		return noticePanel;
	}

	/**
	 * UIのレンダリングをリフレッシュします
	 */
	private void UIrefresh() {
		int width = noticePanel.getWidth();
		int height = noticePanel.getHeight();
		int newX = (int) (width * 0.3);
		version.setBounds(10, 0, newX - 10, height);
		applicationStatus.setBounds(newX + 4, 0, (newX * 2) - 2, height);
	}

}

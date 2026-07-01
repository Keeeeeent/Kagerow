package com.sakulabo.application.app.gui.CenterPanelParts.ContextPanelParts.CacheParts;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.IOException;

import javax.naming.CannotProceedException;
import javax.naming.NamingException;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import com.sakulabo.application.app.gui.CenterPanelParts.ContextPanelParts.ContextPanel;
import com.sakulabo.application.app.gui.CenterPanelParts.ContextPanelParts.SubContextPanel;
import com.sakulabo.application.common.code.CacheSubContextPanelText;
import com.sakulabo.application.common.code.GUIText;
import com.sakulabo.application.common.mixin.ActionListenerMixin;
import com.sakulabo.application.common.mixin.JButtonMixin;
import com.sakulabo.application.common.mixin.JLabelMixin;
import com.sakulabo.application.common.mixin.JPanelMixin;
import com.sakulabo.application.common.mixin.JPanelMixin.Layout;
import com.sakulabo.application.helper.DialogHelper;
import com.sakulabo.core.Kagerow.Contents.KagerowCacheContent;
import com.sakulabo.core.Kagerow.Context.KagerowCacheContext;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;
import com.sakulabo.core.Kagerow.Utilities.KagerowUtilities;

/**
 * キャッシュ詳細パネル実装クラスです
 * 
 * @author keeeeeent
 */
public class CacheSubContextPanel extends SubContextPanel {

	/** アクションコマンド（操作ボタン） */
	private static final String EDIT_CMD = GUIText.CacheSubContextPanelText_001.toString();

	/** コンテキスト */
	private KagerowCacheContext context;
	/** コンテンツ */
	private KagerowCacheContent content;

	/** コンテキストパネル */
	private ContextPanel contextPanel;
	{
		contextPanel = KagerowUtilities.getBean(ContextPanel.class, null).get();
	}

	/** 詳細パネル */
	private CacheTabPanel tabpanel;
	{
		tabpanel = KagerowUtilities.getBean(CacheTabPanel.class, null).get();
	}

	/** ダイアログヘルパー */
	private DialogHelper dialogHelper;
	{
		dialogHelper = KagerowUtilities.getBean(DialogHelper.class, null).get();
	}

	// ####################################################################################
	// # 共通内部クラス定義(情報パネル)
	// ####################################################################################

	/**
	 * 操作パネル定義
	 */
	@JPanelMixin.Setting(backgroudColor = 0xFFFFFF, layout = Layout.BorderLayout)
	private class EditPanel extends JPanel implements JPanelMixin, JButtonMixin, JLabelMixin {

		/** 削除コマンド */
		private static final String DEL_CMD = "DEL_CMD";

		/** 削除ボタン */
		@JButtonMixin.Setting(title = GUIText.CacheSubContextPanelText_002, actionCommand = DEL_CMD)
		private JButton delBtn = new JButton();
		{
			// ボタン初期化処理
			try {
				setJButton(this, delBtn);
			} catch (IllegalAccessException | NoSuchMethodException e) {
				KagerowLogger.newAppLogger().err(e);
			}
		}
		/** 削除説明文 */
		private JTextArea delText = new JTextArea();
		{
			// 説明文追加
			delText.setText(GUIText.CacheSubContextPanelText_004.toLineString());
			// レイアウト設定
			delText.setEditable(false);
			delText.setLineWrap(true);
			delText.setWrapStyleWord(true);
			delText.setOpaque(false);
			delText.setBorder(null);
		}
		/** 削除パネル */
		private JPanel delBtnPanel = new JPanel();
		{
			// レイアウト設定
			setJPanel(delBtnPanel);
			delBtnPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
			// コンポーネント配置
			delBtnPanel.add(delBtn);
		}
		/** 削除パネル */
		private JPanel delPanel = new JPanel();
		{
			// レイアウト設定
			setJPanel(delPanel);
			delPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
			// ボーダー設定
			delPanel.setBorder(BorderFactory.createTitledBorder(
					BorderFactory.createEtchedBorder(),
					GUIText.CacheSubContextPanelText_003.toString()));
			// コンポーネント配置
			delPanel.add(delText, BorderLayout.CENTER);
			delPanel.add(delBtnPanel, BorderLayout.SOUTH);
		}

		/**
		 * デフォルトコンストラクタ
		 */
		EditPanel() {
			// 共通初期化処理実行
			setJPanel(this);
			// レイアウト設定
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			// コンポーネント配置
			add(delPanel);
		}

		/**
		 * 削除ボタン押下イベント
		 */
		@ActionListenerMixin.ActionCommand(DEL_CMD)
		private void del() {
			try {
				// スキーマ名称
				String schema = content.getNameInNamespace();
				// コンテキスト削除
				context.destroySubcontext(schema);
				// 削除成功ダイアログ
				dialogHelper.showSystemInfo(CacheSubContextPanelText.SUCCESS_DELETED.toString());
				// タブクローズ
				tabpanel.delTab(schema);
				// コンテキスト変更通知
				contextPanel.noticeObserver();
			} catch (CannotProceedException e) {
				if (e.getCause() instanceof IOException) {
					// 削除失敗の場合
					dialogHelper.showSystemError(CacheSubContextPanelText.FAIL_DELETED.toString());
				} else {
					// 予期せぬ例外
					dialogHelper.showSystemError(
							CacheSubContextPanelText.FAIL_DELETED_SYSTEM_ERROR.toString());
				}
				// ログ書き出し
				KagerowLogger.newAppLogger().err(e);
			} catch (NamingException e) {
				// 予期せぬ例外
				dialogHelper.showSystemError(CacheSubContextPanelText.FAIL_DELETED_SYSTEM_ERROR.toString());
				// ログ書き出し
				KagerowLogger.newAppLogger().err(e);
			}
		}

	}

	/**
	 * デフォルトコンストラクタ
	 * @param name スキーマ名称
	 */
	public CacheSubContextPanel(String name) {
		try {
			// 仮想FSコンテキスト取得
			context = (KagerowCacheContext) KagerowUtilities.getContext(KagerowCacheContext._NAME);
			// コンテンツ取得
			content = context.lookup(name);
		} catch (NamingException e) {
			KagerowLogger.newAppLogger().err(e);
		}
	}

	/** {@inheritDoc} */
	@Override
	public Component getComponent() {
		Component component = super.getComponent();
		addAction(EDIT_CMD, new EditPanel());
		return component;
	}

}

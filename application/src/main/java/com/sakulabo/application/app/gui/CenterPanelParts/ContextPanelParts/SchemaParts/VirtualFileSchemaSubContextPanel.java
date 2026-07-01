package com.sakulabo.application.app.gui.CenterPanelParts.ContextPanelParts.SchemaParts;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import javax.naming.CannotProceedException;
import javax.naming.NamingException;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextArea;

import com.sakulabo.application.app.gui.CenterPanelParts.ContextPanelParts.ContextPanel;
import com.sakulabo.application.app.gui.CenterPanelParts.ContextPanelParts.SubContextPanel;
import com.sakulabo.application.common.code.GUIText;
import com.sakulabo.application.common.code.VirtualFileSchemaSubContextPanelText;
import com.sakulabo.application.common.mixin.ActionListenerMixin;
import com.sakulabo.application.common.mixin.JButtonMixin;
import com.sakulabo.application.common.mixin.JLabelMixin;
import com.sakulabo.application.common.mixin.JPanelMixin;
import com.sakulabo.application.common.mixin.JPanelMixin.Layout;
import com.sakulabo.application.helper.DialogHelper;
import com.sakulabo.core.Kagerow.Context.KagerowVirtualDirContext;
import com.sakulabo.core.Kagerow.Context.KagerowVirtualFileContext;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;
import com.sakulabo.core.Kagerow.Utilities.KagerowTransaction;
import com.sakulabo.core.Kagerow.Utilities.KagerowUtilities;

/**
 * 仮想FSスキーマ詳細パネル実装クラスです
 * 
 * @author keeeeeent
 */
public class VirtualFileSchemaSubContextPanel extends SubContextPanel {

	/** アクションコマンド（操作ボタン） */
	private static final String EDIT_CMD = GUIText.VirtualFileSchemaSubContextPanel_001.toString();
	/** アクションコマンド（情報ボタン） */
	private static final String INFO_CMD = GUIText.VirtualFileSchemaSubContextPanel_002.toString();

	/** コンテキスト */
	private KagerowVirtualFileContext context;
	/** コンテンツ */
	private KagerowVirtualDirContext content;

	/** ダイアログヘルパー */
	private DialogHelper dialogHelper;
	{
		dialogHelper = KagerowUtilities.getBean(DialogHelper.class, null).get();
	}

	/** スキーマタブコンポーネント */
	private VirtualFileSchemaTabPanel fileSchemaTabPanel;
	{
		fileSchemaTabPanel = KagerowUtilities.getBean(VirtualFileSchemaTabPanel.class, null).get();
	}

	/** コンテキストパネル */
	private ContextPanel contextPanel;
	{
		contextPanel = KagerowUtilities.getBean(ContextPanel.class, null).get();
	}

	// ####################################################################################
	// # 共通内部クラス定義(情報パネル)
	// ####################################################################################

	/**
	 * 情報パネル定義
	 */
	@JPanelMixin.Setting(backgroudColor = 0xFFFFFF, layout = Layout.BorderLayout)
	private class InfoPanel extends JPanel implements JPanelMixin, JLabelMixin {

		/** 情報テーブル */
		private VirtualFileSchemaMapTableModel model;
		/** 情報マッピング */
		private Map<String, String> data = new HashMap<>();

		/** スキーマサイズ */
		{
			try {
				// サイズ取得
				long size = content.getSchemaContextSize();
				data.put(GUIText.VirtualFileSchemaSubContextPanel_004.toString(), String.format("%dKB", size));
			} catch (IOException e) {
				// ログ書き込み
				KagerowLogger.newAppLogger().err(e);
				// 失敗テキスト生成
				data.put(GUIText.VirtualFileSchemaSubContextPanel_004.toString(), "- KB");
			}
		}

		/** テーブル数 */
		{
			// サイズ取得
			int size = content.getSynonymMapList().size();
			data.put(GUIText.VirtualFileSchemaSubContextPanel_005.toString(), String.format("%d", size));
		}

		/** 最終更新日付 */
		{
			try {
				// パス取得
				Path path = content.getPath();
				// ファイル情報取得
				BasicFileAttributeView view = Files.getFileAttributeView(path, BasicFileAttributeView.class);
				BasicFileAttributes attr = view.readAttributes();
				FileTime time = attr.lastModifiedTime();
				// 最終更新日付をフォーマット
				Instant instant = time.toInstant();
				DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;
				// マップに追加
				data.put(GUIText.VirtualFileSchemaSubContextPanel_006.toString(),
						formatter.format(instant));
			} catch (IOException e) {
				// ログ書き込み
				KagerowLogger.newAppLogger().err(e);
				// 失敗テキスト生成
				data.put(GUIText.VirtualFileSchemaSubContextPanel_006.toString(), "-");
			}
		}

		/**
		 * デフォルトコンストラクタ
		 */
		InfoPanel() {
			// 共通初期化処理実行
			setJPanel(this);
			// テーブル生成
			model = new VirtualFileSchemaMapTableModel(data);
			JTable table = new JTable();
			table.setModel(model);
			// テーブル罫線設定
			table.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
			table.getTableHeader().setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
			// コンポーネント配置
			add(table, BorderLayout.CENTER);
			add(table.getTableHeader(), BorderLayout.NORTH);
		}

	}

	/**
	 * 操作パネル定義
	 */
	@JPanelMixin.Setting(backgroudColor = 0xFFFFFF, layout = Layout.BorderLayout)
	private class EditPanel extends JPanel implements JPanelMixin, JButtonMixin, JLabelMixin {

		/** 削除コマンド */
		private static final String DEL_CMD = "DEL_CMD";

		/** 削除ボタン */
		@JButtonMixin.Setting(title = GUIText.VirtualFileSchemaSubContextPanel_003, actionCommand = DEL_CMD)
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
			delText.setText(GUIText.VirtualFileSchemaSubContextPanel_008.toLineString());
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
					GUIText.VirtualFileSchemaSubContextPanel_007.toString()));
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
				// トランザクション取得
				KagerowTransaction tran = KagerowTransaction.getTransactionFromSchemaName(schema);
				try (tran) {
					// コンテキスト削除
					context.destroySubcontext(schema);
					// 削除成功ダイアログ
					dialogHelper.showSystemInfo(VirtualFileSchemaSubContextPanelText.SUCCESS_DELETED.toString());
					// タブクローズ
					fileSchemaTabPanel.delTab(schema);
					// コンテキスト変更通知
					contextPanel.noticeObserver();
				} catch (CannotProceedException e) {
					if (e.getCause() instanceof IOException) {
						// 削除失敗の場合
						dialogHelper.showSystemError(VirtualFileSchemaSubContextPanelText.FILE_DELETED.toString());
					} else {
						// 予期せぬ例外
						dialogHelper.showSystemError(
								VirtualFileSchemaSubContextPanelText.FILE_DELETED_SYSTEM_ERROR.toString());
					}
					// ログ書き出し
					KagerowLogger.newAppLogger().err(e);
				}
			} catch (NamingException | IOException e) {
				// 予期せぬ例外
				dialogHelper.showSystemError(VirtualFileSchemaSubContextPanelText.FILE_DELETED_SYSTEM_ERROR.toString());
				// ログ書き出し
				KagerowLogger.newAppLogger().err(e);
			}
		}
	}

	/**
	 * デフォルトコンストラクタ
	 * @param name スキーマ名称
	 */
	public VirtualFileSchemaSubContextPanel(String name) {
		try {

			// 仮想FSコンテキスト取得
			context = (KagerowVirtualFileContext) KagerowUtilities
					.getContext(KagerowVirtualFileContext._NAME);
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
		addAction(INFO_CMD, new InfoPanel());
		addAction(EDIT_CMD, new EditPanel());
		return component;
	}

}

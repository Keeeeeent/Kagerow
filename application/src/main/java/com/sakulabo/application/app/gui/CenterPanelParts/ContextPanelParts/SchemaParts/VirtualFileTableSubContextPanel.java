package com.sakulabo.application.app.gui.CenterPanelParts.ContextPanelParts.SchemaParts;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.naming.CannotProceedException;
import javax.naming.NamingException;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.TableCellRenderer;

import com.sakulabo.application.app.gui.CenterPanelParts.ContextPanelParts.ContextPanel;
import com.sakulabo.application.app.gui.CenterPanelParts.ContextPanelParts.SubContextPanel;
import com.sakulabo.application.common.code.GUIText;
import com.sakulabo.application.common.code.VirtualFileTableSubContextPanelText;
import com.sakulabo.application.common.mixin.ActionListenerMixin;
import com.sakulabo.application.common.mixin.JButtonMixin;
import com.sakulabo.application.common.mixin.JLabelMixin;
import com.sakulabo.application.common.mixin.JPanelMixin;
import com.sakulabo.application.common.mixin.JPanelMixin.Layout;
import com.sakulabo.application.helper.DialogHelper;
import com.sakulabo.core.Kagerow.Contents.KagerowVirtualFileContent;
import com.sakulabo.core.Kagerow.Contents.KagerowVirtualFileContent.KagerowDataType;
import com.sakulabo.core.Kagerow.Contents.KagerowVirtualFileContent.KagerowVirtualFileObject;
import com.sakulabo.core.Kagerow.Context.KagerowVirtualDirContext;
import com.sakulabo.core.Kagerow.Utilities.KagerowDBInfoAccesser;
import com.sakulabo.core.Kagerow.Utilities.KagerowDBMode;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;
import com.sakulabo.core.Kagerow.Utilities.KagerowTransaction;
import com.sakulabo.core.Kagerow.Utilities.KagerowUtilities;

/**
 * 仮想FSテーブル詳細パネル実装クラスです
 * 
 * @author keeeeeent
 */
public class VirtualFileTableSubContextPanel extends SubContextPanel {

	/** アクションコマンド（情報ボタン） */
	private static final String INFO_CMD = GUIText.VirtualFileTableSubContextPanel_001.toString();
	/** アクションコマンド（データタイプボタン） */
	private static final String DATA_INFO_CMD = GUIText.VirtualFileTableSubContextPanel_002.toString();
	/** アクションコマンド（世代ボタン） */
	private static final String GENERATION_CMD = GUIText.VirtualFileTableSubContextPanel_003.toString();
	/** アクションコマンド（DDLボタン） */
	private static final String DDL_CMD = GUIText.VirtualFileTableSubContextPanel_004.toString();
	/** アクションコマンド（操作ボタン） */
	private static final String EDIT_CMD = GUIText.VirtualFileTableSubContextPanel_005.toString();

	/** スキーマ */
	private final KagerowVirtualDirContext content;
	/** テーブル */
	private final KagerowVirtualFileContent table;
	/** テーブル情報 */
	private final KagerowVirtualFileObject file;
	/** テーブル論理名 */
	private final String name;

	/** ダイアログヘルパー */
	private DialogHelper dialogHelper;
	{
		dialogHelper = KagerowUtilities.getBean(DialogHelper.class, null).get();
	}

	/** コンテキストパネル */
	private ContextPanel contextPanel;
	{
		contextPanel = KagerowUtilities.getBean(ContextPanel.class, null).get();
	}

	/** スキーマタブコンポーネント */
	private VirtualFileTableTabPanel fileTableTabPanel;
	{
		fileTableTabPanel = KagerowUtilities.getBean(VirtualFileTableTabPanel.class, null).get();
	}

	/**
	 * デフォルトコンストラクタ
	 * @param content コンテンツ
	 * @param name テーブル論理名
	 * @param rowName テーブル物理名
	 * @throws NamingException 
	 */
	public VirtualFileTableSubContextPanel(KagerowVirtualDirContext content, String name, String rowName)
			throws NamingException {
		this.content = content;
		this.table = content.lookup(rowName);
		this.file = table.get(0);
		this.name = name;
	}

	// ####################################################################################
	// # 共通内部クラス定義(情報パネル)
	// ####################################################################################

	/**
	 * JTableボタンレンダラー
	 */
	private class ButtonRenderer extends JButton implements TableCellRenderer {

		/**
		 * デフォルトコンストラクタ
		 */
		public ButtonRenderer() {
			setText(GUIText.VirtualFileTableSubContextPanel_008.toString());
		}

		public Component getTableCellRendererComponent(JTable table, Object value,
				boolean isSelected, boolean hasFocus, int row, int column) {
			return this;
		}
	}

	/**
	 * ボタンクリック監視アダプター
	 */
	private class MouseAdapterImpl extends MouseAdapter {

		/** テーブル */
		private final JTable table;
		/** クリックアクション */
		private final Consumer<Integer> consumer;
		/** 対象カラム */
		private final int targetCol;

		/**
		 * デフォルトコンストラクタ
		 * @param table テーブルインスタンス
		 * @param consumer クリックアクション
		 * @param targetCol 対象カラム
		 */
		MouseAdapterImpl(JTable table, Consumer<Integer> consumer, int targetCol) {
			this.table = table;
			this.consumer = consumer;
			this.targetCol = targetCol;
		}

		/** {@inheritDoc} */
		@Override
		public void mouseClicked(MouseEvent e) {
			// マウスポインター取得
			int row = table.rowAtPoint(e.getPoint());
			int col = table.columnAtPoint(e.getPoint());
			// ボタン列を指定
			if (col == targetCol) {
				consumer.accept(Integer.valueOf(row));
			}
		}

	}

	/**
	 * 情報パネル定義
	 */
	@JPanelMixin.Setting(backgroudColor = 0xFFFFFF, layout = Layout.BorderLayout)
	private class InfoPanel extends JPanel implements JPanelMixin, JLabelMixin {

		/** 情報テーブル */
		private VirtualFileSchemaMapTableModel model;
		/** 情報マッピング */
		private Map<String, String> data = new HashMap<>();

		/** テーブル物理名 */
		{
			data.put(GUIText.VirtualFileTableSubContextPanel_009.toString(), file.binaryName());
		}

		/** kagerowURI */
		{
			data.put(GUIText.VirtualFileTableSubContextPanel_010.toString(), file.uri().get());
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
	 * データタイプパネル定義
	 */
	@JPanelMixin.Setting(backgroudColor = 0xFFFFFF, layout = Layout.BorderLayout)
	private class DataTypePanel extends JPanel implements JPanelMixin, JLabelMixin {

		/** データタイプテーブル */
		private VirtualFileTableDataTypeMapTableModel model;
		/** 情報マッピング */
		private Map<String, String> data = new HashMap<>();

		/** データタイプ */
		{
			KagerowDataType[] dataType = file.dataType();
			String[] columnName = file.headerData();
			for (int i = 0; i < file.dataType().length; i++) {
				data.put(columnName[i], dataType[i].name());
			}
		}

		/**
		 * デフォルトコンストラクタ
		 */
		DataTypePanel() {
			// 共通初期化処理実行
			setJPanel(this);
			// テーブル生成
			model = new VirtualFileTableDataTypeMapTableModel(data);
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
	 * 世代タイプパネル定義
	 */
	@JPanelMixin.Setting(backgroudColor = 0xFFFFFF, layout = Layout.BorderLayout)
	private class GaenerationPanel extends JPanel implements JPanelMixin, JLabelMixin, Consumer<Integer> {

		/** データタイプテーブル */
		private VirtualFileTableGenerationListTableModel model;
		/** 情報マッピング */
		private List<String[]> data = new ArrayList<>();

		/** 世代 */
		{
			try {
				for (int i = 0; i < table.contentSize(); i++) {
					KagerowVirtualFileObject fileObject = table.get(i);
					String[] data = {
							String.valueOf(i),
							fileObject.createTime().toString(),
							fileObject.datSize().toString(),
							String.format("${%s[%d]}", name, i),
							null
					};
					this.data.add(data);
				}
			} catch (NamingException e) {
				KagerowLogger.newAppLogger().err(e);
			}
		}

		/**
		 * デフォルトコンストラクタ
		 */
		GaenerationPanel() {
			// 共通初期化処理実行
			setJPanel(this);
			// テーブル生成
			model = new VirtualFileTableGenerationListTableModel(data);
			JTable table = new JTable();
			table.setModel(model);
			// ボタン位置を生成
			int colPosition = model.getColumnCount() - 1;
			// ボタンクリックイベント管理リスナー設定
			table.addMouseListener(new MouseAdapterImpl(table, this, colPosition));
			// ボタン列を設定（第5列）
			table.getColumnModel().getColumn(colPosition).setCellRenderer(new ButtonRenderer());
			// テーブル罫線設定
			table.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
			table.getTableHeader().setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
			// コンポーネント配置
			add(table, BorderLayout.CENTER);
			add(table.getTableHeader(), BorderLayout.NORTH);
		}

		/** {@inheritDoc} */
		@Override
		public void accept(Integer t) {

			// 最後の1つの場合、世代を削除しないようにする
			if (table.contentSize() == 1) {
				// 警告通知
				dialogHelper.showSystemWarning(
						VirtualFileTableSubContextPanelText.FILE_DELETED_SYSTEM_WARN_GEN.toString());
				return;
			}

			try {
				KagerowVirtualFileObject fileObject = table.get(t);
				// トランザクション制御開始
				final URI uri = URI.create(fileObject.uri().get());
				try (KagerowTransaction tran = KagerowTransaction.getTransaction(uri)) {
					try {
						// 世代削除
						String gen = KagerowVirtualFileContent.getGeneration(fileObject);
						table.destroySubcontext(gen);
						// 削除成功ダイアログ
						dialogHelper.showSystemInfo(VirtualFileTableSubContextPanelText.SUCCESS_DELETED_GEN.toString());
						// タブクローズ
						fileTableTabPanel.delTab(name);
						// トランザクション確定
						tran.commit();
						// コンテキスト変更通知
						contextPanel.noticeObserver();
					} catch (CannotProceedException | IOException e) {
						if (e.getCause() instanceof IOException) {
							// 削除失敗の場合
							dialogHelper.showSystemError(
									VirtualFileTableSubContextPanelText.FILE_DELETED_GEN.toString());
						} else {
							// 予期せぬ例外
							dialogHelper.showSystemError(
									VirtualFileTableSubContextPanelText.FILE_DELETED_SYSTEM_ERROR_GEN.toString());
						}
						// ログ書き出し
						KagerowLogger.newAppLogger().err(e);
						// ロールバック
						tran.rollback(e);
					} catch (NamingException e) {
						// 予期せぬ例外
						dialogHelper.showSystemError(
								VirtualFileTableSubContextPanelText.FILE_DELETED_SYSTEM_ERROR_GEN.toString());
						// ログ書き出し
						KagerowLogger.newAppLogger().err(e);
						// ロールバック
						tran.rollback(e);
					}
				}
			} catch (Exception e1) {
				// 予期せぬ例外
				dialogHelper.showSystemError(
						VirtualFileTableSubContextPanelText.FILE_DELETED_SYSTEM_ERROR_GEN.toString());
				// ログ書き出し
				KagerowLogger.newAppLogger().err(e1);
			}

		}

	}

	/**
	 * DDLパネル定義
	 */
	@JPanelMixin.Setting(backgroudColor = 0xFFFFFF, layout = Layout.BorderLayout)
	private class DDLPanel extends JPanel implements JPanelMixin, JLabelMixin, ActionListener {

		/** DDLテキストエリア */
		private JTextArea ddl = new JTextArea();
		{
			// レイアウト設定
			ddl.setEditable(false);
			ddl.setLineWrap(true);
			ddl.setWrapStyleWord(true);
			ddl.setOpaque(false);
			ddl.setBorder(null);
			// DDL生成インスタンス生成
			KagerowDBInfoAccesser info = KagerowDBInfoAccesser.newDBInfoAccesser(KagerowDBMode.ORACLE);
			// DDL設定
			ddl.setText(info.toDDL(file));
		}

		/**
		 * デフォルトコンストラクタ
		 */
		DDLPanel() {
			// 共通初期化処理実行
			setJPanel(this);
			// テキスト設定生成
			add(ddl, BorderLayout.CENTER);
			// ボタン追加
			JPanel btnPanel = new JPanel();
			btnPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
			for (KagerowDBMode mode : KagerowDBMode.values()) {
				if (KagerowDBMode.ILLEGALITY == mode) {
					continue;
				}
				JButton btn = new JButton(mode.name());
				btn.setActionCommand(mode.name());
				btn.addActionListener(this);
				btnPanel.add(btn);
				btnPanel.add(Box.createHorizontalStrut(10));
			}
			add(btnPanel, BorderLayout.SOUTH);
		}

		/** {@inheritDoc} */
		@Override
		public void actionPerformed(ActionEvent e) {
			KagerowDBMode mode = KagerowDBMode.valueOf(e.getActionCommand());
			KagerowDBInfoAccesser info = KagerowDBInfoAccesser.newDBInfoAccesser(mode);
			ddl.setText(info.toDDL(file));
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
		@JButtonMixin.Setting(title = GUIText.VirtualFileTableSubContextPanel_008, actionCommand = DEL_CMD)
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
			delText.setText(GUIText.VirtualFileTableSubContextPanel_006.toLineString());
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
					GUIText.VirtualFileTableSubContextPanel_007.toString()));
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
			// トランザクション制御開始
			final URI uri = URI.create(file.uri().get());
			try (KagerowTransaction tran = KagerowTransaction.getTransaction(uri)) {
				try {
					// テーブル削除
					content.destroySubcontext(table.getNameInNamespace());
					// 削除成功ダイアログ
					dialogHelper.showSystemInfo(VirtualFileTableSubContextPanelText.SUCCESS_DELETED.toString());
					// タブクローズ
					fileTableTabPanel.delTab(name);
					// トランザクション確定
					tran.commit();
					// コンテキスト変更通知
					contextPanel.noticeObserver();
				} catch (CannotProceedException | IOException e) {
					if (e.getCause() instanceof IOException) {
						// 削除失敗の場合
						dialogHelper.showSystemError(VirtualFileTableSubContextPanelText.FILE_DELETED.toString());
					} else {
						// 予期せぬ例外
						dialogHelper.showSystemError(
								VirtualFileTableSubContextPanelText.FILE_DELETED_SYSTEM_ERROR.toString());
					}
					// ログ書き出し
					KagerowLogger.newAppLogger().err(e);
					// ロールバック
					tran.rollback(e);
				} catch (NamingException e) {
					// 予期せぬ例外
					dialogHelper
							.showSystemError(VirtualFileTableSubContextPanelText.FILE_DELETED_SYSTEM_ERROR.toString());
					// ログ書き出し
					KagerowLogger.newAppLogger().err(e);
					// ロールバック
					tran.rollback(e);
				}
			} catch (Exception e1) {
				// 予期せぬ例外
				dialogHelper
						.showSystemError(VirtualFileTableSubContextPanelText.FILE_DELETED_SYSTEM_ERROR.toString());
				// ログ書き出し
				KagerowLogger.newAppLogger().err(e1);
			}
		}

	}

	/** {@inheritDoc} */
	@Override
	public Component getComponent() {
		Component component = super.getComponent();
		addAction(INFO_CMD, new InfoPanel());
		addAction(DATA_INFO_CMD, new DataTypePanel());
		addAction(GENERATION_CMD, new GaenerationPanel());
		addAction(DDL_CMD, new DDLPanel());
		addAction(EDIT_CMD, new EditPanel());
		return component;
	}

}

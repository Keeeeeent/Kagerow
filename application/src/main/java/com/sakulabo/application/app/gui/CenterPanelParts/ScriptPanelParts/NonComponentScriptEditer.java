package com.sakulabo.application.app.gui.CenterPanelParts.ScriptPanelParts;

import java.awt.Component;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import com.sakulabo.application.app.gui.AppPanel;
import com.sakulabo.application.common.adapter.GUIExecutionPlanAdapter;
import com.sakulabo.application.common.code.NonComponentScriptEditerText;
import com.sakulabo.application.common.mixin.JSplitPanelMixin;
import com.sakulabo.application.helper.DialogHelper;
import com.sakulabo.application.model.Script.KSQLScriptModel;
import com.sakulabo.core.Kagerow.Exception.AppLogicException;
import com.sakulabo.core.Kagerow.Exception.KFileParseException;
import com.sakulabo.core.Kagerow.Exception.KSQLParseException;
import com.sakulabo.core.Kagerow.Utilities.KagerowDBMode;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;
import com.sakulabo.core.Kagerow.Utilities.KagerowScriptAccessor;
import com.sakulabo.core.Kagerow.Utilities.KagerowUtilities;

/**
 * GUIアプリケーションのスクリプト編集パネルクラスです
 * @author keeeeeent
 */
@JSplitPanelMixin.Setting(orientation = JSplitPane.VERTICAL_SPLIT)
public class NonComponentScriptEditer extends AppPanel implements JSplitPanelMixin, Runnable {

	/** ダイアログヘルパー */
	private DialogHelper dialogHelper;
	{
		dialogHelper = KagerowUtilities.getBean(DialogHelper.class, null).get();
	}

	/** スクリプトアクセッサー */
	private KagerowScriptAccessor kagerowScriptAccessor;
	/** スクリプト編集ルートパネル */
	private final JSplitPane rootPanel;
	/** スクリプトモデル */
	private KSQLScriptModel model = new KSQLScriptModel();
	/** スクリプト編集パネル */
	private final NonComponentScriptEditPanel scriptEditPanel;
	/** 実行結果表示パネル */
	private final NonComponentResultViewPanel resultViewPanel;
	/** 新規スクリプト向け内部カウンター */
	private static final AtomicInteger COUNTER = new AtomicInteger();

	/** インスタンスクローズフラグ */
	private final AtomicBoolean closeFlag = new AtomicBoolean();
	/** バックグランドスレッド */
	private final Thread backgroundThread;

	/**
	 * デフォルトコンストラクタ
	 */
	public NonComponentScriptEditer() {

		// 引数初期化
		String name = "temporary_" + COUNTER.incrementAndGet(), summary = "default", schema = "default";
		KagerowDBMode mode = KagerowDBMode.ORACLE;

		// フィールド初期化
		kagerowScriptAccessor = KagerowScriptAccessor.getInstance(name, summary, mode, schema);

		// モデル初期化
		{
			model.path = null;
			model.scriptAccessor = kagerowScriptAccessor;
			model.planAdapter = new GUIExecutionPlanAdapter();
		}

		// スクリプト編集パネル初期化
		scriptEditPanel = new NonComponentScriptEditPanel(kagerowScriptAccessor, this, null);

		// 実行結果パネル初期化
		resultViewPanel = new NonComponentResultViewPanel(model, scriptEditPanel);

		// 新規作成であることをエディタに通知
		scriptEditPanel.isNewCreate();

		// UI初期化
		rootPanel = createSplitPane();
		rootPanel.setContinuousLayout(true);
		rootPanel.setResizeWeight(0.7);
		initialize();
		SwingUtilities.invokeLater(() -> {
			// 初期の位置を指定（下 30%）
			rootPanel.setDividerLocation(0.7);
		});

		// バックグラウンドスレッドを設定
		backgroundThread = new Thread(this);
		backgroundThread.setDaemon(true);
		backgroundThread.setName(getClass().getName());

	}

	/**
	 * デフォルトコンストラクタ
	 * @param path スクリプトパス
	 * @throws IOException ファイルIOエラー
	 * @throws KFileParseException KFile解析エラー
	 * @throws KSQLParseException KSQL解析エラー
	 * @throws AppLogicException アプリケーションロジック不正
	 */
	public NonComponentScriptEditer(Path path)
			throws KFileParseException, KSQLParseException, AppLogicException, IOException {

		// フィールド初期化
		kagerowScriptAccessor = KagerowScriptAccessor.getInstance(path);

		// モデル初期化
		{
			model.path = path;
			model.scriptAccessor = kagerowScriptAccessor;
			model.planAdapter = new GUIExecutionPlanAdapter();
			if (KagerowScriptAccessor.KAGEROW_SECURE_MINE_TYPE.equals(Files.probeContentType(path))) {
				// セキュアファイルの場合
				model.isSecure = true;
			}
		}

		// スクリプト編集パネル初期化
		scriptEditPanel = new NonComponentScriptEditPanel(kagerowScriptAccessor, this, null);

		// 実行結果パネル初期化
		resultViewPanel = new NonComponentResultViewPanel(model, scriptEditPanel);

		// UI初期化
		rootPanel = createSplitPane();
		rootPanel.setContinuousLayout(true);
		rootPanel.setResizeWeight(0.7);
		initialize();
		SwingUtilities.invokeLater(() -> {
			// 初期の位置を指定（下 30%）
			rootPanel.setDividerLocation(0.7);
		});

		// バックグラウンドスレッドを設定
		backgroundThread = new Thread(this);
		backgroundThread.setDaemon(true);
		backgroundThread.setName(getClass().getName());
		// パスが存在するため監視開始
		backgroundThread.start();

	}

	/**
	 * GUI描画初期化処理実施
	 */
	public void lazyInitialize() {
		// 初回起動のみ設定が反映されないためFrame表示後遅延設定
		SwingUtilities.invokeLater(() -> {
			// 初期の位置を指定（下 30%）
			rootPanel.setDividerLocation(0.7);
		});
	}

	/**
	 * タブタイトルを返却します
	 * @return タブタイトル
	 */
	String getTitle() {
		return kagerowScriptAccessor.getName();
	}

	/** {@inheritDoc} */
	@Override
	public Component getComponent() {
		return rootPanel;
	}

	/** {@inheritDoc} */
	@Override
	public Component getLeftComponent() {
		return scriptEditPanel;
	}

	/** {@inheritDoc} */
	@Override
	public Component getRightComponent() {
		return resultViewPanel;
	}

	/**
	 * 現在操作中のモデルを同期的に取得します
	 * @return モデル情報
	 */
	synchronized KSQLScriptModel getModel() {
		return model;
	}

	/**
	 * 現在操作中のモデルを同期的に支配下にバインドします
	 * @param kagerowScriptAccessor アクセッサー
	 * @throws Exception 最新化失敗
	 */
	synchronized void setModel(KagerowScriptAccessor kagerowScriptAccessor) throws Exception {
		// モデル最新化
		model.scriptAccessor = kagerowScriptAccessor;
		// 保存完了通知をここで呼び出してしまうと再帰ループになるためしてはいけない
	}

	/**
	 * 現在操作中のモデルを同期的に最新化します
	 * @throws Exception 最新化失敗
	 */
	synchronized void refreshModel() throws Exception {
		// スクリプトアクセッサー最新化
		kagerowScriptAccessor = KagerowScriptAccessor.getInstance(model.path);
		// モデル最新化
		model.scriptAccessor = kagerowScriptAccessor;
		// 保存完了通知
		scriptEditPanel.refresh(kagerowScriptAccessor);
		// 監視が開始されていない場合開始する
		if (Thread.State.NEW == backgroundThread.getState()) {
			// スレッド開始
			backgroundThread.start();
		}
		// 監視ログ
		KagerowLogger.newAppLogger().log(
				Level.INFO,
				NonComponentScriptEditerText.EDIT_FILE.toString(
						new Object[] { model.path.toAbsolutePath().normalize().toString() }),
				new Object[0]);
	}

	/** {@inheritDoc} */
	@Override
	public void run() {
		// 開始ログ
		KagerowLogger.newAppLogger().log(
				Level.INFO,
				NonComponentScriptEditerText.START_BACKGROUND_THREAD.toString(
						new Object[] { model.path.toAbsolutePath().normalize().toString() }),
				new Object[0]);
		// 監視サービスを取得
		watch: try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
			// パスを監視対象に指定（変更/削除を監視）
			Path target = model.path;
			if (Objects.isNull(target)) {
				// スレッドの実行はmodel.pathがnull出ない場合に行われる想定のためログに書き出す
				KagerowLogger.newAppLogger().log(Level.WARNING,
						NonComponentScriptEditerText.MODEL_PATH_IS_NULL.toString(), new Object[0]);
				// 万が一nullで起動された場合は即座に処理を終了する
				break watch;
			}
			// パスの正規化
			target = target.toAbsolutePath().normalize();
			// パス監視を開始（パスが存在するディレクトリ）
			// 安全保存の場合削除->新規作成になるため作成も監視
			target.getParent().register(
					watchService,
					StandardWatchEventKinds.ENTRY_DELETE,
					StandardWatchEventKinds.ENTRY_MODIFY);
			while (true) {
				// 変更があるまで待機
				WatchKey event = watchService.take();
				// イベントの解析
				for (WatchEvent<?> watchEvent : event.pollEvents()) {
					// パスを取得
					Path eventTarget = (Path) watchEvent.context();
					eventTarget = target.getParent().resolve(eventTarget.getFileName());
					try {
						if (!Files.isSameFile(eventTarget, target)) {
							// 監視対象のパスではない場合処理はせずループ継続
							continue;
						}
					} catch (IOException ignore) {
						// ファイル比較で例外が出た場合はそのままループ継続
						continue;
					}
					// イベント種別取得
					WatchEvent.Kind<?> kind = watchEvent.kind();
					// 削除イベントの場合
					if (StandardWatchEventKinds.ENTRY_DELETE.equals(kind)) {
						// 監視の必要がなくなるためサービス解除とユーザ通知
						dialogHelper.showSystemWarning(NonComponentScriptEditerText.FILE_DELETED.toString(
								new Object[] { target.toString() }));
						break;
					}
					// 変更イベントの場合（安全保存も含める）
					if (StandardWatchEventKinds.ENTRY_MODIFY.equals(kind)) {
						// モデルをリフレッシュ
						refreshModel();
					}
				}
				// イベントキューリセット
				event.reset();
			}
		} catch (IOException e) {
			// スタックトレースを書き込み
			StringWriter writer = new StringWriter();
			e.printStackTrace(new PrintWriter(writer));
			// サービス開始失敗の場合、GUI側からの操作に影響は無いため警告としてログ書き込み
			KagerowLogger.newAppLogger().log(Level.WARNING, writer.toString(), new Object[0]);
		} catch (InterruptedException e) {
			if (!closeFlag.get()) {
				// 未クロースの場合予期しない割り込みのためログに書き込み
				KagerowLogger.newAppLogger().err(e);
			}
		} catch (Exception e) {
			// 予期せぬエラーの場合ログに書き込み
			KagerowLogger.newAppLogger().err(e);
		}
	}

}

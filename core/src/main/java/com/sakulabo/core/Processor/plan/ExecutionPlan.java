package com.sakulabo.core.Processor.plan;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.stream.Collectors;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.sql.rowset.CachedRowSet;

import com.sakulabo.core.Common.AppPathUtils;
import com.sakulabo.core.Common.ApplicationWordDictionary;
import com.sakulabo.core.Common.ErrorMessage;
import com.sakulabo.core.Common.StringUtils;
import com.sakulabo.core.Common.ThreadUtils;
import com.sakulabo.core.Kagerow.KagerowApplication;
import com.sakulabo.core.Kagerow.KagerowApplication.Mode;
import com.sakulabo.core.Kagerow.Adapter.KagerowExecutionPlanAdapter;
import com.sakulabo.core.Kagerow.Adapter.KagerowExecutionPlanAdapter.ExecutKsqlInfo;
import com.sakulabo.core.Kagerow.Adapter.KagerowExecutionPlanAdapter.ExitCode;
import com.sakulabo.core.Kagerow.Contents.KagerowCacheContent;
import com.sakulabo.core.Kagerow.Contents.KagerowCacheContent.KagerowCacheObject.KagerowExecutionCache;
import com.sakulabo.core.Kagerow.Contents.KagerowPluginContent;
import com.sakulabo.core.Kagerow.Contents.KagerowPluginContent.Data.InputData;
import com.sakulabo.core.Kagerow.Contents.KagerowPluginContent.Data.OutputData;
import com.sakulabo.core.Kagerow.Contents.KagerowVirtualFileContent.KagerowVirtualFileObject;
import com.sakulabo.core.Kagerow.Contents.Impl.KagerowPluginContentImpl;
import com.sakulabo.core.Kagerow.Context.KagerowCacheContext;
import com.sakulabo.core.Kagerow.Context.KagerowVirtualFileContext;
import com.sakulabo.core.Kagerow.Exception.AppLogicException;
import com.sakulabo.core.Kagerow.Exception.KSQLExecuteException;
import com.sakulabo.core.Kagerow.Exception.KSQLParseException;
import com.sakulabo.core.Kagerow.Exception.KagerowExecuteException;
import com.sakulabo.core.Kagerow.Exception.KagerowExecuteException.CommandException;
import com.sakulabo.core.Kagerow.Exception.KagerowExecuteException.CreateKDBException;
import com.sakulabo.core.Kagerow.Exception.KagerowExecuteException.InputPluginException;
import com.sakulabo.core.Kagerow.Exception.KagerowExecuteException.KsqlException;
import com.sakulabo.core.Kagerow.Exception.KagerowExecuteException.OutputPluginException;
import com.sakulabo.core.Kagerow.Exception.KagerowValidationError;
import com.sakulabo.core.Kagerow.Utilities.KagerowChunkLorder;
import com.sakulabo.core.Kagerow.Utilities.KagerowExecutionPlanAccessor;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;
import com.sakulabo.core.Kagerow.Utilities.KagerowProcessBuilder;
import com.sakulabo.core.Kagerow.Utilities.KagerowScriptAccessor;
import com.sakulabo.core.Kagerow.Utilities.KagerowScriptAccessor.KagerowCmdAccessor;
import com.sakulabo.core.Kagerow.Utilities.KagerowScriptAccessor.KagerowPluginAccessor;
import com.sakulabo.core.Kagerow.Utilities.KagerowScriptAccessor.KagerowSqlAccessor;
import com.sakulabo.core.Kagerow.Utilities.KagerowUtilities;
import com.sakulabo.core.Processor.command.AppProcessBuilderImpl;
import com.sakulabo.core.Processor.command.CommandTmpFileCreater;
import com.sakulabo.core.Processor.database.DDLConnectionHandler;
import com.sakulabo.core.Processor.database.KsqlParamParser;
import com.sakulabo.core.Processor.database.QueryConnectionHandler;
import com.sakulabo.core.Processor.jmx.AppJMXInitializer;
import com.sakulabo.core.Processor.jmx.ExecutionPlan.ExecutionPlanMXBean;
import com.sakulabo.core.Processor.log.AppLogMessage;
import com.sakulabo.core.Processor.plugin.PluginParamParser;
import com.sakulabo.core.Processor.script.KFile;
import com.sakulabo.core.Processor.script.KsqlDatabaseLinker;
import com.sakulabo.core.Processor.script.KsqlReplaceWordParser;
import com.sakulabo.regulation.annotation.KagerowPlugin.PluginType;
import com.sakulabo.regulation.spi.PluginAdapter;
import com.sakulabo.regulation.spi.PluginAdapter.KagerowRowSet;

/**
 * Kagerowスクリプトの実行計画を実行する基底クラスです
 * @author keeeeeent
 */
public abstract sealed class ExecutionPlan<T extends KagerowVirtualFileObject>
		implements
		KagerowExecutionPlanAccessor,
		AutoCloseable,
		ExecutionPlanMXBean
		permits BasicExecutionPlan, SecureExecutionPlan {

	// ##########################################################################
	// # 共通定数定義
	// ##########################################################################

	/** 最大タスク容量 */
	protected static final Semaphore KDB_MAX_PLAN_TASK_COUNT;

	static {
		KDB_MAX_PLAN_TASK_COUNT = KagerowApplication.getConfig().KDB_MAX_PLAN_TASK_COUNT();
	}

	/** JMX（セッション監視オブジェクト名称） */
	private static final String JMX_SESSIONT_NAME = "ExecutionPlan:type={0}";
	/** DDLプラグイン名称 */
	protected static final String DDL_PLUGIN_NAME = ApplicationWordDictionary.WCD_0006.getMessage();
	/** DDLプラグインパラメータ名称 */
	protected static final String DDL_PLUGIN_PARAM = ApplicationWordDictionary.WCD_0007.getMessage();

	// ##########################################################################
	// # 定数定義
	// ##########################################################################

	/** セッションID */
	protected final UUID sessionId = UUID.randomUUID();
	/** 実行計画専用エグゼキューター */
	protected final ExecutorService exec;

	{
		ThreadFactory factory = ThreadUtils.newVirtualThreadFactory(ThreadUtils.EXECUTION_PLUN_GROUP);
		exec = Executors.newThreadPerTaskExecutor(factory);
	}

	/** KDB構築先パス */
	protected final Path kdbPath;
	/** ロード済みコンテンツ(KDB) */
	protected final Set<T> fileSet;
	/** ロード済みコンテンツ(KVIEW) */
	protected final Map<String, Set<T>> fileMap;
	/** 排他制御ロックオブジェクト */
	protected final ReentrantLock lock = new ReentrantLock();
	/** 保持履歴サイズ */
	protected final int historySize = 5;
	/** 実行履歴リスト生成 */
	protected final Queue<ExecutionPlanHistory> historyList = new ArrayDeque<>();
	/** 実行履歴リスト生成（ソフト参照） */
	protected final List<SoftReference<ExecutionPlanHistory>> softHistoryList = new ArrayList<>();
	/** GC対象検知リスト生成 */
	protected final ReferenceQueue<ExecutionPlanHistory> refList = new ReferenceQueue<>();
	/** GC対象検知後処理対応をスレッド */
	protected final ExecutorService refExec;

	{
		ThreadFactory factory = ThreadUtils.newDemonThreadFactory(ThreadUtils.JMX_GROUP);
		refExec = Executors.newSingleThreadExecutor(factory);
		refExec.submit(new JMXCleaner());
	}

	// ##########################################################################
	// # 共通変数宣言
	// ##########################################################################

	/** スクリプトアクセッサー */
	protected volatile KagerowScriptAccessor script;
	/** 実行SQL一覧（実行順序順） */
	protected volatile Map<String, String> sqlText;
	/** 実行入力プラグイン一覧（実行順序順） */
	protected volatile Map<Integer, List<KagerowPluginAccessor>> inputPlugin;
	/** 実行出力プラグイン一覧（実行順序順） */
	protected volatile Map<Integer, List<KagerowPluginAccessor>> outputPlugin;
	/** 実行SQL結果一覧一覧（実行順序順） */
	protected volatile Map<String, CachedRowSet> result;
	/** 使用予定のスレッド数 */
	protected volatile int lordStep;
	/** トランザクションID */
	protected volatile UUID transactionId;
	/** 実行開始時刻 */
	protected volatile Instant startTime;
	/** 実行終了時刻 */
	protected volatile Instant endTime;
	/** 実行ステップアダプター */
	protected volatile KagerowExecutionPlanAdapter planAdapter;
	/** キャッシュ無効化フラグ */
	protected volatile boolean ignoreCache;
	/** キャッシュID */
	protected volatile String cacheId;
	/** キャッシュオブジェクト */
	protected volatile KagerowExecutionCache cacheObject;

	// ##########################################################################
	// # 内部クラス宣言
	// ##########################################################################

	/** 実行計画履歴クラス */
	public final class ExecutionPlanHistory implements KagerowExecutionPlanHistoryAccessor {

		/** 実行コマンド */
		private final KagerowCmdAccessor cmd;
		/** 実行SQL一覧（実行順序順） */
		private final Map<String, String> sqlText;
		/** 実行入力プラグイン一覧（実行順序順） */
		private final Map<Integer, List<KagerowPluginAccessor>> inputPlugin;
		/** 実行出力プラグイン一覧（実行順序順） */
		private final Map<Integer, List<KagerowPluginAccessor>> outputPlugin;
		/** 使用予定のスレッド数 */
		private final int lordStep;
		/** トランザクションID */
		private final UUID transactionId;
		/** 実行開始時刻 */
		private final Instant startTime;
		/** 実行終了時刻 */
		private final Instant endTime;
		/** 実行時間 */
		private final Duration executionTime;
		/** セッションID */
		private final UUID sessionId;
		/** KDB構築先パス */
		private final Path kdbPath;
		/** 実行SQL結果一覧一覧（実行順序順） */
		protected final Map<String, CachedRowSet> result;

		/**
		 * デフォルトコンストラクタ
		 */
		private ExecutionPlanHistory() {
			if (!ExecutionPlan.this.script.getCommand().isEmpty()) {
				this.cmd = ExecutionPlan.this.script.getCommand().getFirst();
			} else {
				this.cmd = null;
			}
			this.sqlText = ExecutionPlan.this.sqlText;
			this.inputPlugin = ExecutionPlan.this.inputPlugin;
			this.outputPlugin = ExecutionPlan.this.outputPlugin;
			this.lordStep = ExecutionPlan.this.lordStep;
			this.transactionId = ExecutionPlan.this.transactionId;
			this.startTime = ExecutionPlan.this.startTime;
			this.endTime = ExecutionPlan.this.endTime;
			if (Objects.nonNull(startTime) && Objects.nonNull(endTime)) {
				this.executionTime = Duration.between(startTime, endTime);
			} else {
				this.executionTime = Duration.ZERO;
			}
			this.sessionId = ExecutionPlan.this.sessionId;
			this.kdbPath = ExecutionPlan.this.kdbPath;
			this.result = ExecutionPlan.this.result;
		}

		/** {@inheritDoc} */
		@Override
		public KagerowCmdAccessor getCmd() {
			return this.cmd;
		}

		/** {@inheritDoc} */
		@Override
		public Map<String, String> getSqlText() {
			return this.sqlText;
		}

		/** {@inheritDoc} */
		@Override
		public Map<Integer, List<KagerowPluginAccessor>> getInputPlugin() {
			return this.inputPlugin;
		}

		/** {@inheritDoc} */
		@Override
		public Map<Integer, List<KagerowPluginAccessor>> getOutputPlugin() {
			return this.outputPlugin;
		}

		/** {@inheritDoc} */
		@Override
		public int getLordStep() {
			return this.lordStep;
		}

		/** {@inheritDoc} */
		@Override
		public UUID getTransactionId() {
			return this.transactionId;
		}

		/** {@inheritDoc} */
		@Override
		public Instant getStartTime() {
			return this.startTime;
		}

		/** {@inheritDoc} */
		@Override
		public Instant getEndTime() {
			return this.endTime;
		}

		/** {@inheritDoc} */
		@Override
		public Duration getExecutionTime() {
			return this.executionTime;
		}

		/** {@inheritDoc} */
		@Override
		public UUID getSessionId() {
			return this.sessionId;
		}

		/** {@inheritDoc} */
		@Override
		public Path getKdbPath() {
			return this.kdbPath;
		}

		/** {@inheritDoc} */
		@Override
		public Map<String, CachedRowSet> currentRowSet() {
			return this.result;
		}

	}

	/**
	 * JMXアンローダー
	 */
	private class JMXCleaner implements Runnable {

		/** {@inheritDoc} */
		@Override
		public void run() {

			while (true) {
				try {
					// 参照取得
					Reference<? extends ExecutionPlanHistory> key = refList.remove();
					// 参照削除（ソフト参照解除）
					softHistoryList.remove(key);
				} catch (Exception e) {
					KagerowLogger.newAppLogger().err(e);
				}

			}
		}

	}

	/**
	 * チャンクローダー実行隊
	 */
	protected class submiter implements Callable<Void> {

		/** チャンクローダー */
		final KagerowChunkLorder<T> chunkLorder;
		/** ロードファイル */
		final T file;

		/**
		 * デフォルトコンストラクタ
		 * @param chunkLorder チャンクローダー
		 * @param file        ロードファイル
		 */
		submiter(KagerowChunkLorder<T> chunkLorder, T file) {
			this.chunkLorder = chunkLorder;
			this.file = file;
		}

		@Override
		public Void call() throws Exception {
			// ロード実行
			this.chunkLorder.lord(this.file);
			// ロード済みリストに追加
			fileSet.add(this.file);
			return null;
		}

	}

	// ##########################################################################
	// # クラス定義
	// ##########################################################################

	/**
	 * コンストラクタ共通処理
	 */
	{
		// ロード済みコンテンツ(KDB)
		fileSet = new HashSet<>();
		// ロード済みコンテンツ(KVIEW)
		fileMap = new HashMap<>();
	}

	/**
	 * デフォルトコンストラクタ
	 * @throws IOException 一時KDB物理ファイル生成失敗
	 * @throws AppLogicException 監視設定失敗
	 */
	protected ExecutionPlan() throws AppLogicException, IOException {

		// キャッシュID初期化
		cacheId = StringUtils.DEFAULT;

		// キャッシオブジェクト初期化
		cacheObject = null;

		// KDB出力先初期化
		kdbPath = Files.createTempFile(AppPathUtils.createTemporaryDirPath(),
				AppPathUtils.KDB_FILE_PREFIX,
				AppPathUtils.KDB_FILE_EXT);

		// JMXの登録
		setJMX();

	}

	/**
	 * キャッシュロードを行うコンストラクタ
	 * @param cacheId キャッシュID
	 * @throws AppLogicException 監視設定失敗
	 * @throws IOException 一時KDB物理ファイル生成失敗
	 */
	@SuppressWarnings("unchecked")
	protected ExecutionPlan(String cacheId) throws AppLogicException, IOException {

		// キャッシュID初期化
		this.cacheId = cacheId;

		// KDB出力先初期化
		this.kdbPath = Files.createTempFile(AppPathUtils.createTemporaryDirPath(),
				AppPathUtils.KDB_FILE_PREFIX,
				AppPathUtils.KDB_FILE_EXT);

		try {

			// キャッシュコンテキスト取得
			KagerowCacheContext cacheContext = (KagerowCacheContext) KagerowApplication.getInstance()
					.getContext()
					.lookup(KagerowCacheContext._NAME);
			// コンテンツ取得
			KagerowCacheContent cacheContent = cacheContext.lookup(cacheId);
			// キャッシュ取得
			KagerowExecutionCache cache = (KagerowExecutionCache) cacheContent
					.lookup(KagerowExecutionCache.CACHE_FILE_NAME);
			// キャッシュファイルパス生成
			Path fromPath = KagerowExecutionCache.createCacheKDBPath(
					cacheContent.getNameInNamespace(),
					KagerowExecutionCache.CACHE_DB_FILE_NAME);
			// KDBパス再初期化
			Files.copy(fromPath, kdbPath, StandardCopyOption.REPLACE_EXISTING);
			// キャッシュから読み込みを行いロード済みコンテンツを初期化する
			List<URI> lordTargetList = cache.lordedFileList()
					.stream()
					.map(URI::create)
					.toList();

			// ロード済みコンテンツ(KDB)初期化
			for (URI uri : lordTargetList) {
				Path path = Paths.get(uri);
				if (Files.exists(path)) {
					try (
							InputStream input = Files.newInputStream(path);
							ObjectInputStream oinput = new ObjectInputStream(input)) {
						KagerowVirtualFileObject file = (KagerowVirtualFileObject) oinput.readObject();
						fileSet.add((T) file);
					} catch (Exception e) {
						KagerowLogger.newAppLogger().err(e);
					}
				}
			}

			// ロード済みコンテンツ(KVIEW)初期化
			for (Entry<String, Set<String>> entry : cache.lordedFiledMap().entrySet()) {
				// 一時的なファイルセット
				Set<T> tmpFileSet = new HashSet<>();
				// 一時的なファイルセットの初期化実施
				entry.getValue().stream()
						.forEach(uri -> {
							// ロード済みコンテンツ検索
							T t = fileSet.stream()
									.filter(c -> uri.equals(c.uri().get().toString()))
									.findFirst()
									.orElse(null);
							if (Objects.nonNull(t)) {
								// ロード済みコンテンツが見つかった場合ファイルセットに追加
								tmpFileSet.add(t);
							} else {
								// ロード対象コンテンツが見つからない場合、警告ログを出力
								KagerowLogger.newAppLogger().log(
										Level.WARNING,
										ErrorMessage.CODE_005.getMessage(uri),
										new Object[] {});
							}
						});
				// ファイルマップ追加
				fileMap.merge(entry.getKey(), tmpFileSet, this::mergeMap);
			}

			// キャッシオブジェクト初期化
			cacheObject = cache;

		} catch (NamingException e) {
			// キャッシュが無効、もしくは存在しない場合
			// キャッシュID初期化
			this.cacheId = StringUtils.DEFAULT;
			// キャッシオブジェクト初期化
			this.cacheObject = null;
		}

		// JMXの登録
		setJMX();

	}

	/**
	 * JMXの監視設定を行います
	 * @throws AppLogicException 監視設定失敗
	 */
	private void setJMX() throws AppLogicException {

		// 名称オブジェクト生成
		String name = MessageFormat.format(JMX_SESSIONT_NAME, sessionId);
		ObjectName objectName;
		try {
			objectName = new ObjectName(name);
		} catch (MalformedObjectNameException e) {
			throw new AppLogicException("ObjectName can not initialize", e);
		}

		// JMX登録処理
		AppJMXInitializer.registMXBean(this, objectName);

		// JMXサーバー取得
		MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
		// JMX追加登録
		setJMX(mBeanServer, objectName);

	}

	/**
	 * JMX登録処理を行います
	 * @param mBeanServer JMXサーバー
	 * @param ObjectName  JMX名称
	 * @throws AppLogicException 監視設定失敗
	 */
	protected abstract void setJMX(MBeanServer mBeanServer, ObjectName ObjectName) throws AppLogicException;

	/**
	 * セッションIDを取得します
	 * @return セッションID
	 */
	UUID getSessionID() {
		return sessionId;
	}

	/**
	 * チャンクローダーを生成します
	 * @return 生成されたチャンクローダー
	 * @throws Exception チャンクローダー生成失敗
	 */
	protected abstract KagerowChunkLorder<T> createChunkLorder() throws Exception;

	/**
	 * 実行ステップとアダプターを関連付けし、実行ステップを実行します
	 * @param step         実行ステップ
	 * @param startTrigger 開始トリガー
	 * @param endTrigger   終了トリガー
	 * @throws KagerowExecuteException 実行ステップエラー
	 */
	private void wrapStep(Callable<Void> step, Runnable startTrigger, Runnable endTrigger)
			throws KagerowExecuteException {
		try {
			// 開始トリガー
			startTrigger.run();
			// メイン処理
			step.call();
			// 終了トリガー
			endTrigger.run();
		} catch (KagerowExecuteException e) {
			throw e;
		} catch (Exception e) {
			// 未知の例外である場合ラップする
			throw new KagerowExecuteException(e);
		}
	}

	/**
	 * KDBの構築処理を実行します
	 * @return 仮返却変数
	 * @throws CreateKDBException KDB論理インスタンス生成失敗
	 */
	@SuppressWarnings("unchecked")
	protected Void createKDB() throws CreateKDBException {

		try {

			// ロード対象コンテンツ格納リスト
			Set<T> fileSet = new HashSet<>();
			// KVIEW構築対象コンテンツ格納マップ
			Map<String, Set<T>> fileMap = new HashMap<>();

			// コンテキスト取得
			KagerowVirtualFileContext fileContext = (KagerowVirtualFileContext) KagerowApplication.getInstance()
					.getContext().lookup(KagerowVirtualFileContext._NAME);

			// 実行順序にソート
			KsqlParamParser ksqlParamParser = new KsqlParamParser(script);
			List<KagerowSqlAccessor> sqlList = ksqlParamParser.getSqlPlan();
			// ロード先一覧格納リスト
			List<KsqlDatabaseLinker> linkerList = new ArrayList<>();
			for (KagerowSqlAccessor sql : sqlList) {
				// SQL変数適用
				KsqlReplaceWordParser wordParser = new KsqlReplaceWordParser(sql.variable());
				String processedSql = wordParser.transform(sql.getSql());
				// 仮想KDBマッピング
				KsqlDatabaseLinker linker = new KsqlDatabaseLinker(script.getSchema(), fileContext);
				processedSql = linker.transform(processedSql);
				// 処理結果格納
				sqlText.put(sql.getId(), processedSql);
				// ロード先を後ほど処理するため、一時格納
				linkerList.add(linker);
			}

			// デフォルトプラグイン処理
			loadDefaultDDLPlugin(fileContext, linkerList);

			// ロードファイル情報マージと格納
			for (KsqlDatabaseLinker linker : linkerList) {
				// 未ロードのオブジェクトをロード
				for (KagerowVirtualFileObject tmpFile : linker.getFileSet()) {
					// ロードされてい場合
					if (!this.fileSet.contains(tmpFile)) {
						// ロード対象に追加
						fileSet.add((T) tmpFile);
					}
				}
				// 構築対象の場合対象に追加
				Map<String, Set<KagerowVirtualFileObject>> tmpFileMap = linker.getFileMap();
				if (!tmpFileMap.isEmpty()) {
					for (Map.Entry<String, Set<KagerowVirtualFileObject>> entry : tmpFileMap.entrySet()) {
						fileMap.merge(entry.getKey(), (Set<T>) entry.getValue(), this::mergeMap);
					}
				}
			}

			// チャンクローダー生成
			KagerowChunkLorder<T> chunkLorder = createChunkLorder();

			// ロード対象格納リスト
			Map<Future<Void>, T> waitTarget = new HashMap<>();
			// ロードに必要なステップ数を計算
			for (T file : fileSet) {
				lordStep += chunkLorder.canSeparate(file.datSize());
			}

			// 実行可能か判定
			if (!KDB_MAX_PLAN_TASK_COUNT.tryAcquire(lordStep)) {
				throw new AppLogicException(ErrorMessage.CODE_011.getMessage(lordStep));
			}

			try {

				// KDB初期化処理実行
				DDLConnectionHandler.initDDLConnectionHandler(kdbPath, script.getMode(), fileSet);

				// チャンクローダロード処理実行
				for (T file : fileSet) {
					submiter submiterImpl = new submiter(chunkLorder, file);
					Future<Void> waiter = exec.submit(submiterImpl);
					// ロード対象リストに追加
					waitTarget.put(waiter, file);
				}

				// ロード待機
				AppLogicException exp = new AppLogicException(ErrorMessage.CODE_004.getMessage(
						ApplicationWordDictionary.WCD_0001.getMessage()));
				for (Future<Void> waiter : waitTarget.keySet()) {
					try {
						waiter.get();
					} catch (ExecutionException | InterruptedException e) {
						exp.addCause(e);
					}
				}

				// 制御された例外を検知した場合、例外をスロー
				if (exp.isCause()) {
					throw exp;
				}

				// KVIEW初期化処理実行
				DDLConnectionHandler.finishDDLConnectionHandler(kdbPath, script.getMode(), fileMap, script.getSchema());

				// 構築が完了した時点でビュー定義を更新
				if (!fileMap.isEmpty()) {
					for (Map.Entry<String, Set<T>> entry : fileMap.entrySet()) {
						this.fileMap.merge(entry.getKey(), (Set<T>) entry.getValue(), this::mergeMap);
					}
				}

			} finally {
				// 必ず処理終了後、セマフォを解放
				KDB_MAX_PLAN_TASK_COUNT.release(lordStep);
			}

		} catch (Exception e) {
			throw new KagerowExecuteException.CreateKDBException(e);
		}

		return null;

	}

	/**
	 * DDLプラグインに対するKDBマッピングを実施します
	 * @param fileContext 仮想ファイルコンテキスト
	 * @param linkerList  ロード先一時保管リスト
	 * @throws KSQLParseException マッピング失敗
	 */
	private void loadDefaultDDLPlugin(KagerowVirtualFileContext fileContext, List<KsqlDatabaseLinker> linkerList)
			throws KSQLParseException {

		// 仮想KDBマッピング
		KsqlDatabaseLinker linker = new KsqlDatabaseLinker(script.getSchema(), fileContext);
		for (KagerowPluginAccessor plugin : script.getInputPlugins()) {
			// デフォルトDDLプラグインの場合、DDLをKDBマッピング
			if (DDL_PLUGIN_NAME.equals(plugin.getName())) {
				// DDL取得
				String ddl = plugin.getParam().get(DDL_PLUGIN_PARAM);
				if (Objects.nonNull(ddl)) {
					// KDBリンク
					linker.transform(ddl);
					// ロード先を後ほど処理するため、一時格納
					linkerList.add(linker);
				}
			}
		}

	}

	/**
	 * KagerowVirtualFileObjectセットをマージします
	 * @param oldValue マージ先
	 * @param newValue マージ元
	 * @return マージ後セット
	 */
	private Set<T> mergeMap(Set<T> oldValue, Set<T> newValue) {
		if (Objects.isNull(oldValue)) {
			return newValue;
		} else {
			oldValue.addAll(newValue);
			return oldValue;
		}
	}

	/**
	 * コマンド実行を行います
	 * @return 仮返却変数
	 * @throws CommandException コマンド実行インスタンス生成失敗
	 */
	protected Void doCmd() throws CommandException {

		for (KagerowCmdAccessor command : script.getCommand()) {
			// スクリプトの環境変数取得
			Map<String, String> env = command.environmental();
			// プロセスビルダー生成
			final AppProcessBuilderImpl cmdProcesser = (AppProcessBuilderImpl) KagerowProcessBuilder
					.newProcessBuilderMap(command.getMode(), env);
			try {
				// コマンドファイル生成
				CommandTmpFileCreater commandTmpFileCreater = new CommandTmpFileCreater(command);
				Path cmdFilePath = commandTmpFileCreater.build();
				// コマンド設定
				cmdProcesser.addArg(cmdFilePath.toString());
				// コマンド構築
				ProcessBuilder builder = cmdProcesser.build();
				// コマンド実行
				final Process process = builder.start();
				// バックグラウンドスレッド
				CompletableFuture<Void> backgroundThread = null;
				// GUIモードの場合、標準エラー出力をバッファリング
				if (KagerowApplication.getApplicationMode().equals(Mode.GUI)) {
					backgroundThread = CompletableFuture.runAsync(() -> {
						try (OutputStream output = cmdProcesser.createErrorStream();
								InputStream input = process.getErrorStream()) {
							input.transferTo(output);
						} catch (IOException e) {
							KagerowLogger.newAppLogger().err(e);
						}
					}, exec);
				}
				// コマンド実行待機
				int result = process.waitFor();
				// 結果を確認し正常終了出なければ例外をスロー
				if (result != 0) {
					// バックグラウンドスレッドの終了待機
					if (Objects.nonNull(backgroundThread)) {
						backgroundThread.join();
					}
					// 例外スロー
					throw new CommandException(
							cmdProcesser.getErrorStream(),
							ErrorMessage.CODE_029.getMessage());
				}
			} catch (IOException | InterruptedException e) {
				throw new CommandException(
						cmdProcesser.getErrorStream(),
						ErrorMessage.CODE_029.getMessage(),
						e);
			}
		}

		return null;

	}

	/**
	 * KSQLの実行を行います
	 * @return 仮返却変数
	 * @throws KsqlException SQL実行失敗
	 */
	protected Void doKsql() throws KsqlException {

		try {

			// 通知向け情報格納メモリ
			Instant startTime = Instant.now();
			ExitCode exitCode = ExitCode.SUCCESS;

			// SQL実行・結果取得
			for (Map.Entry<String, String> sql : sqlText.entrySet()) {
				try (QueryConnectionHandler connectionHandler = new QueryConnectionHandler(script.getMode(), kdbPath)) {
					try {
						// 開始時刻記録
						startTime = Instant.now();
						// 開始リスナー実行
						this.planAdapter.startDoKsqlIndividual();
						// SQL実行
						List<CachedRowSet> tmpResult = connectionHandler.transaction(sql.getValue());
						if (!tmpResult.isEmpty()) {
							result.put(sql.getKey(), tmpResult.getFirst());
						}
					} catch (KSQLExecuteException e) {
						// 既に例外翻訳されている場合はそのままスロー
						exitCode = ExitCode.FAIL;
						throw e;
					} catch (SQLException e) {
						// 例外翻訳されていない場合はこのタイミングで翻訳
						exitCode = ExitCode.FAIL;
						throw new KSQLExecuteException(ErrorMessage.CODE_029.getMessage(), e);
					} finally {
						// 通知情報生成
						ExecutKsqlInfo info = new ExecutKsqlInfo(
								sql.getKey(),
								startTime,
								Instant.now(),
								exitCode);
						// 終了リスナー実行
						this.planAdapter.endDoKsqlIndividual(info);
					}
				} catch (KSQLExecuteException e) {
					// 既に例外翻訳されている場合、このタイミングでIDとNAMEを設定
					String sqlId = sql.getKey();
					e.setId(sqlId);
					Optional<String> sqlName = script.getRawKsqls().stream().filter(f -> sqlId.equals(f.getId()))
							.map(KagerowSqlAccessor::getName)
							.findFirst();
					sqlName.ifPresent(e::setName);
					throw e;
				} catch (SQLException exp) {
					throw new KSQLExecuteException(ErrorMessage.CODE_029.getMessage(), exp);
				}
			}

		} catch (Exception e) {
			throw new KsqlException(e);
		}

		return null;

	}

	/**
	 * インプットプラグインを使用し、KDBに追加の処理を施します
	 * @return 仮返却変数
	 * @throws InputPluginException ブラブイン実行要求失敗
	 */
	protected Void doInputPlugin() throws InputPluginException {

		try {

			// 入力プラグインを取得(ソート済み)
			inputPlugin = PluginParamParser.getPlugin(p -> p.getInputPlugins(), script);

			// 実行要求
			for (Entry<Integer, List<KagerowPluginAccessor>> plugins : inputPlugin.entrySet()) {

				// プラグインリスト取得
				List<KagerowPluginAccessor> pluginList = plugins.getValue();
				// 実行カウンター生成
				CountDownLatch lacth = new CountDownLatch(pluginList.size());
				// 実行結果格納リスト
				List<InputData> dataList = new ArrayList<>();
				// コネクションハンドラリスト
				List<QueryConnectionHandler> connectionHandlerList = new ArrayList<>();

				for (KagerowPluginAccessor plugin : pluginList) {

					// バージョン付きのルックアップキーを生成
					Name lookupKey = KagerowUtilities.createVersioningPluginPkgName(
							plugin.getPackageName(),
							plugin.getVersion());
					// プラグインコンテンツ取得
					KagerowPluginContent content = KagerowUtilities.getPlugin(lookupKey, plugin.getName());
					// 入力処理をサポートしているか確認
					if (!content.isSupportType(PluginType.INPUT)) {
						// サポートしていない場合処理を中断
						continue;
					}

					// コネクションを生成
					QueryConnectionHandler connectionHandler = new QueryConnectionHandler(script.getMode(), kdbPath);
					connectionHandlerList.add(connectionHandler);
					// パラメータバインド
					PluginParamParser paramParser = new PluginParamParser(
							((KagerowPluginContentImpl) content).toAdapter());
					Map<String, String> baseParamList = plugin.getParam();
					Map<String, String> paramList = paramParser.getPluginParam(baseParamList, PluginParamParser.INPUT);

					// デフォルトDDLプラグインの場合、カレントスキーマを設定
					Connection connection = connectionHandler.getConnection();
					if (DDL_PLUGIN_NAME.equals(plugin.getName())) {
						connection.setSchema(script.getSchema());
					}

					// データ生成
					InputData data = new InputData(
							Collections.unmodifiableMap(paramList),
							this.script.getMode(),
							connection,
							lacth,
							plugin.getId(),
							Optional.ofNullable(planAdapter));
					// 実行リクエスト
					content.submit(data);
					// 実行結果リストにデータ格納
					dataList.add(data);
				}

				// 全件実行待機
				lacth.await();

				// コネクションクローズ
				Exception exp = new Exception();
				boolean expFlug = false;
				for (QueryConnectionHandler connectionHandler : connectionHandlerList) {
					try {
						connectionHandler.close();
					} catch (Exception e) {
						exp.addSuppressed(e);
						expFlug = true;
					}
				}
				// 制御された例外を検知した場合、ラッパーをスロー
				if (expFlug) {
					throw exp;
				}

			}

		} catch (Exception e) {
			throw new KagerowExecuteException.InputPluginException(e);
		}

		return null;

	}

	/**
	 * アウトプットプラグインを使用し、KDBに追加の処理を施します
	 * @return 仮返却変数
	 * @throws OutputPluginException ブラブイン実行要求失敗
	 */
	protected Void doOutputPlugin() throws OutputPluginException {

		try {

			// 入力プラグインを取得(ソート済み)
			outputPlugin = PluginParamParser.getPlugin(p -> p.getOutputPlugins(), script);

			// 実行要求
			for (Entry<Integer, List<KagerowPluginAccessor>> plugins : outputPlugin.entrySet()) {

				// プラグインリスト取得
				List<KagerowPluginAccessor> pluginList = plugins.getValue();
				// 実行カウンター生成
				CountDownLatch lacth = new CountDownLatch(pluginList.size());
				// 実行結果格納リスト
				List<OutputData> dataList = new ArrayList<>();
				// データセット初期化
				List<KagerowRowSet> dataSetList = new ArrayList<>();

				// SQL実行結果加工
				for (Map.Entry<String, CachedRowSet> resultSet : result.entrySet()) {
					// 名称の初期化
					String dataName = script.getKsqls().stream()
							.filter(f -> f.getId().equals(resultSet.getKey()))
							.map(KagerowSqlAccessor::getName)
							.findFirst()
							.orElse(StringUtils.NULL_STR);
					// データ生成
					KagerowRowSet dataSet = new KagerowRowSet(
							script.getMode().toString(),
							resultSet.getKey(),
							dataName,
							resultSet.getValue());
					// データ追加
					dataSetList.add(dataSet);
				}

				for (KagerowPluginAccessor plugin : pluginList) {

					// バージョン付きのルックアップキーを生成
					Name lookupKey = KagerowUtilities.createVersioningPluginPkgName(
							plugin.getPackageName(),
							plugin.getVersion());
					// プラグインコンテンツ取得
					KagerowPluginContent content = KagerowUtilities.getPlugin(lookupKey, plugin.getName());
					// 入力処理をサポートしているか確認
					if (!content.isSupportType(PluginType.OUTPUT)) {
						// サポートしていない場合処理を中断
						continue;
					}

					// パラメータバインド
					PluginParamParser paramParser = new PluginParamParser(
							((KagerowPluginContentImpl) content).toAdapter());
					Map<String, String> baseParamList = plugin.getParam();
					Map<String, String> paramList = paramParser.getPluginParam(baseParamList, PluginParamParser.OUTPUT);
					// データ位置補正
					for (KagerowRowSet rowSet : dataSetList) {
						CachedRowSet cachedRowSet = rowSet.data();
						cachedRowSet.beforeFirst();
					}
					// データ生成
					OutputData data = new OutputData(
							Collections.unmodifiableList(dataSetList),
							Collections.unmodifiableMap(paramList),
							lacth,
							plugin.getId(),
							Optional.ofNullable(planAdapter));
					// 実行リクエスト
					content.submit(data);
					// 実行結果リストにデータ格納
					dataList.add(data);
				}

				// 全件実行待機
				lacth.await();

			}

		} catch (Exception e) {
			throw new KagerowExecuteException.OutputPluginException(e);
		}

		return null;

	}

	// ##########################################################################
	// # クラス定義（インターフェイスの実現）
	// ##########################################################################

	/** {@inheritDoc} */
	@Override
	public final String toCache() throws AppLogicException, NamingException {

		// キャッシュ無効化フラグがONの場合、キャッシュを行わない
		if (ignoreCache) {
			KagerowLogger.newAppLogger().log(Level.WARNING, AppLogMessage.WARNING_MSG9002.name(),
					new Object[] { sessionId.toString(), cacheId });
		}

		// キャッシュオプジェクト生成
		KagerowExecutionCache cache = createCache();

		// キャッシュID
		String cacheId = registCache(cache);

		// キャッシュID返却
		return cacheId;
	}

	/** {@inheritDoc} */
	@Override
	public final void lord(KagerowScriptAccessor script, KagerowExecutionPlanAdapter planAdapter)
			throws AppLogicException {

		// キャッシュIDの同値性確認
		if (!Objects.equals(script.getCacheId(), cacheId)) {
			String msg = ErrorMessage.CODE_012.getMessage(
					ApplicationWordDictionary.WCD_0003.getMessage());
			throw new AppLogicException(msg);
		}

		// ロックを取得
		lock.lock();

		// 履歴生成
		if (Objects.nonNull(this.script)) {
			// 履歴追加
			historyList.add(new ExecutionPlanHistory());
			// 履歴のローテーション
			while (historySize < historyList.size()) {
				ExecutionPlanHistory his = historyList.poll();
				SoftReference<ExecutionPlanHistory> ref = new SoftReference<>(his, refList);
				softHistoryList.add(ref);
			}
		}

		// インスタンス初期化
		this.planAdapter = planAdapter;
		if (script instanceof KFile kfile) {
			this.script = kfile.build(this);
		} else {
			this.script = script;
		}
		sqlText = new HashMap<>();
		inputPlugin = new HashMap<>();
		outputPlugin = new HashMap<>();
		result = new HashMap<>();
		transactionId = UUID.randomUUID();
		lordStep = 0;
		startTime = null;
		endTime = null;

	}

	/** {@inheritDoc} */
	@Override
	public final KagerowExecutionPlanHistoryAccessor getCurrentHistory() {
		ExecutionPlanHistory result = new ExecutionPlanHistory();
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public Void validation() throws IllegalStateException {

		try {

			// コンテキスト取得
			KagerowVirtualFileContext fileContext = (KagerowVirtualFileContext) KagerowApplication.getInstance()
					.getContext().lookup(KagerowVirtualFileContext._NAME);

			/**
			 * 仮想DB存在チェック
			 */
			for (KagerowSqlAccessor sql : script.getKsqls()) {
				try {
					// SQL変数適用
					KsqlReplaceWordParser wordParser = new KsqlReplaceWordParser(sql.variable());
					String processedSql = wordParser.transform(sql.getSql());
					// 仮想KDBマッピング
					KsqlDatabaseLinker linker = new KsqlDatabaseLinker(script.getSchema(), fileContext);
					linker.transform(processedSql);
				} catch (KSQLParseException e) {
					// 例外翻訳
					KagerowValidationError validError = new KagerowValidationError(e.getMessage(), e);
					// 例外にIDと名称を付与
					validError.setId(sql.getId());
					validError.setName(sql.getName());
					// 例外スロー
					throw validError;
				}
			}

			/**
			 * 入力プラグインチェック
			 */
			for (KagerowPluginAccessor plugin : script.getInputPlugins()) {
				// バージョン付きのルックアップキーを生成
				Name lookupKey = KagerowUtilities.createVersioningPluginPkgName(
						plugin.getPackageName(),
						plugin.getVersion());
				// プラグインコンテンツ取得
				KagerowPluginContent content = KagerowUtilities.getPlugin(lookupKey, plugin.getName());
				// 入力処理をサポートしているか確認
				if (!content.isSupportType(PluginType.INPUT)) {
					// サポートしていない場合処理を中断
					continue;
				}
				try {
					// パラメータバインドチェック
					PluginParamParser paramParser = new PluginParamParser(
							((KagerowPluginContentImpl) content).toAdapter());
					Map<String, String> bindParam = paramParser.getPluginParam(plugin.getParam(),
							PluginParamParser.INPUT);
					// カスタムチェック
					content.validation(bindParam, PluginType.INPUT);
				} catch (PluginAdapter.PluginValidationException e) {
					// 例外翻訳
					KagerowValidationError validError = new KagerowValidationError(e.getMessage(), e);
					// 例外にIDと名称を付与
					validError.setId(plugin.getId());
					validError.setName(plugin.getName());
					// 例外スロー
					throw validError;
				}
			}

			/**
			 * 出力プラグインチェック
			 */
			for (KagerowPluginAccessor plugin : script.getOutputPlugins()) {
				// バージョン付きのルックアップキーを生成
				Name lookupKey = KagerowUtilities.createVersioningPluginPkgName(
						plugin.getPackageName(),
						plugin.getVersion());
				// プラグインコンテンツ取得
				KagerowPluginContent content = KagerowUtilities.getPlugin(lookupKey, plugin.getName());
				// 入力処理をサポートしているか確認
				if (!content.isSupportType(PluginType.OUTPUT)) {
					// サポートしていない場合処理を中断
					continue;
				}
				// パラメータバインドチェック
				try {
					PluginParamParser paramParser = new PluginParamParser(
							((KagerowPluginContentImpl) content).toAdapter());
					Map<String, String> bindParam = paramParser.getPluginParam(plugin.getParam(),
							PluginParamParser.OUTPUT);
					// カスタムチェック
					content.validation(bindParam, PluginType.OUTPUT);
				} catch (PluginAdapter.PluginValidationException e) {
					// 例外翻訳
					KagerowValidationError validError = new KagerowValidationError(e.getMessage(), e);
					// 例外にIDと名称を付与
					validError.setId(plugin.getId());
					validError.setName(plugin.getName());
					// 例外スロー
					throw validError;
				}
			}

		} catch (NamingException e) {
			throw new KagerowValidationError(e.getMessage(), e);
		}

		return null;

	}

	/** {@inheritDoc} */
	@Override
	public final void execute() throws KagerowExecuteException {

		try {

			// 開始処理実行
			this.planAdapter.start();

			// 開始時刻設定
			startTime = Instant.now();

			// バリデーション実行
			wrapStep(this::validation, this.planAdapter::startValidation, this.planAdapter::endValidation);

			// 仮想DB構築
			wrapStep(this::createKDB, this.planAdapter::startCreateKDB, this.planAdapter::endCreateKDB);

			// 入力プラグイン実行
			if (!this.script.getInputPlugins().isEmpty()) {
				wrapStep(this::doInputPlugin, this.planAdapter::startDoInputPlugin, this.planAdapter::endDoInputPlugin);
			}

			// SQL実行
			wrapStep(this::doKsql, this.planAdapter::startDoKsql, this.planAdapter::endDoKsql);

			// 出力プラグイン実行
			if (!this.script.getOutputPlugins().isEmpty()) {
				wrapStep(this::doOutputPlugin, this.planAdapter::startDoOutputPlugin,
						this.planAdapter::endDoOutputPlugin);
			}

			// コマンド実行
			if (!this.script.getCommand().isEmpty()) {
				wrapStep(this::doCmd, this.planAdapter::startDoCmd, this.planAdapter::endDoCmd);
			}

			// 終了時刻設定
			endTime = Instant.now();

		} finally {
			// ロック解除
			lock.unlock();
			// 終了処理実行
			this.planAdapter.end();
		}

	}

	/** {@inheritDoc} */
	@Override
	public void close() throws Exception {

		try {
			// スレッド終了
			exec.shutdownNow();
		} finally {
			// キャッシュ使用の場合、状態の更新を行う
			if (!Objects.equals(cacheId, StringUtils.DEFAULT)) {
				// キャッシュオプジェクト生成
				KagerowExecutionCache cache = createCache();
				// キャッシュ登録
				registCache(cache);
			}
		}

	}

	/**
	 * キャッシュオブジェクトを生成します
	 * @return キャッシュオブジェクト
	 */
	private KagerowExecutionCache createCache() {

		// URI一覧リスト格納
		List<String> uriList = new ArrayList<>();
		fileSet.stream()
				.map(KagerowVirtualFileObject::uri)
				.map(AtomicReference<String>::get)
				.forEach(uriList::add);

		// KVIEW一覧マップ格納
		Map<String, Set<String>> uriMap = new HashMap<>();
		for (Entry<String, Set<T>> entry : fileMap.entrySet()) {
			Set<String> uriSet = entry.getValue()
					.stream()
					.map(KagerowVirtualFileObject::uri)
					.map(AtomicReference<String>::get)
					.collect(Collectors.toSet());
			uriMap.merge(entry.getKey(), uriSet, this::mergeMapStr);
		}

		// キャッシュオプジェクト格納変数を初期化
		KagerowExecutionCache cache = null;
		FileTime time = null;
		try {
			// 最終更新日取得
			time = Files.getLastModifiedTime(kdbPath);
			// キャッシュオプジェクト生成
			cache = new KagerowExecutionCache(kdbPath, time, uriList, uriMap);
		} catch (IOException e) {
			// キャッシュオプジェクト生成
			cache = new KagerowExecutionCache(kdbPath, uriList, uriMap);
		}

		return cache;
	}

	/**
	 * KagerowVirtualFileObjectセット文字列をマージします
	 * @param oldValue マージ先
	 * @param newValue マージ元
	 * @return マージ後セット
	 */
	private Set<String> mergeMapStr(Set<String> oldValue, Set<String> newValue) {
		if (Objects.isNull(oldValue)) {
			return newValue;
		} else {
			oldValue.addAll(newValue);
			return oldValue;
		}
	}

	/**
	 * キャッシュIDをコンテキストに登録します
	 * @param cache 登録対象キャッシュインスタンス
	 * @return キャッシュID
	 * @throws AppLogicException キャッシュID登録失敗
	 * @throws NamingException コンテキスト取得失敗
	 */
	private String registCache(KagerowExecutionCache cache) throws AppLogicException, NamingException {

		// キャッシュコンテキスト取得
		KagerowCacheContext cacheContext = (KagerowCacheContext) KagerowApplication.getInstance()
				.getContext()
				.lookup(KagerowCacheContext._NAME);

		// キャッシュID
		String cacheId = null;

		// コンテンツ取得と登録
		KagerowCacheContent cacheContent = null;
		try {
			cacheContent = cacheContext.lookup(this.cacheId);
			cacheContent.rebind(KagerowExecutionCache.CACHE_FILE_NAME, cache);
		} catch (NamingException e) {
			cacheContent = cacheContext.createSubcontext((String) null);
			cacheContent.bind(KagerowExecutionCache.CACHE_FILE_NAME, cache);
		} catch (Exception e) {
			KagerowLogger.newAppLogger().err(e);
		} finally {
			if (Objects.nonNull(cacheContent)) {
				// キャッシュID設定
				cacheId = cacheContent.getNameInNamespace();
			}
		}

		// キャッシュIDの生成に失敗した場合
		if (Objects.isNull(cacheId)) {
			throw new AppLogicException(ErrorMessage.CODE_016.getMessage());
		}

		// キャッシュ取得
		KagerowExecutionCache cacheObject = (KagerowExecutionCache) cacheContent
				.lookup(KagerowExecutionCache.CACHE_FILE_NAME);

		// 内部キャッシュ更新
		this.cacheObject = cacheObject;
		this.cacheId = cacheId;

		// キャッシュID返却
		return cacheId;

	}

	/** {@inheritDoc} */
	@Override
	@SuppressWarnings("unchecked")
	public synchronized Optional<CachedRowSet> execute(String ksqlId) throws KagerowExecuteException {

		try {

			try {

				// キャッシュ使用有無確認
				if (!StringUtils.DEFAULT.equals(cacheId)) {
					// キャッシュ利用中の場合、現状の状態を保存しキャッシュを解除する
					toCache();
					// キャッシュ無効化フラグをONに設定
					ignoreCache = true;
					// 無効化した旨ログに出力
					KagerowLogger.newAppLogger().log(Level.WARNING, AppLogMessage.WARNING_MSG9003.name(),
							new Object[] { sessionId.toString(), cacheId });
				}

				// ロード対象コンテンツ格納リスト
				Set<T> fileSet = new HashSet<>();
				// KVIEW構築対象コンテンツ格納マップ
				Map<String, Set<T>> fileMap = new HashMap<>();

				// コンテキスト取得
				KagerowVirtualFileContext fileContext = (KagerowVirtualFileContext) KagerowApplication.getInstance()
						.getContext().lookup(KagerowVirtualFileContext._NAME);

				// 実行順序にソート
				KsqlParamParser ksqlParamParser = new KsqlParamParser(script);
				List<KagerowSqlAccessor> sqlList = ksqlParamParser.getSqlPlan();
				// ロード先一覧格納リスト
				List<KsqlDatabaseLinker> linkerList = new ArrayList<>();
				// 実行対象を取得
				Optional<KagerowSqlAccessor> sql = sqlList.stream().filter(f -> f.getId().equals(ksqlId)).findFirst();

				// 実行対象が見つかった場合変数、仮想KDBのマッピングを実施
				if (sql.isPresent()) {

					// SQLアクセッサー取得
					KagerowSqlAccessor rawSql = sql.get();
					// SQL変数適用
					KsqlReplaceWordParser wordParser = new KsqlReplaceWordParser(rawSql.variable());
					String processedSql = wordParser.transform(rawSql.getSql());
					// 仮想KDBマッピング
					KsqlDatabaseLinker linker = new KsqlDatabaseLinker(script.getSchema(), fileContext);
					processedSql = linker.transform(processedSql);
					// 処理結果格納
					sqlText.put(rawSql.getId(), processedSql);
					// ロード先を後ほど処理するため、一時格納
					linkerList.add(linker);

					// チャンクローダー生成
					KagerowChunkLorder<T> chunkLorder = createChunkLorder();

					// ロード対象格納リスト
					Map<Future<Void>, T> waitTarget = new HashMap<>();
					// ロードに必要なステップ数を計算
					for (T file : fileSet) {
						lordStep += chunkLorder.canSeparate(file.datSize());
					}

					// 実行可能か判定
					if (!KDB_MAX_PLAN_TASK_COUNT.tryAcquire(lordStep)) {
						throw new AppLogicException(ErrorMessage.CODE_011.getMessage(lordStep));
					}

					// ロードファイル情報マージと格納
					// 未ロードのオブジェクトをロード
					for (KagerowVirtualFileObject tmpFile : linker.getFileSet()) {
						// ロードされてい場合
						if (!this.fileSet.contains(tmpFile)) {
							// ロード対象に追加
							fileSet.add((T) tmpFile);
						}
					}
					// 構築対象の場合対象に追加
					Map<String, Set<KagerowVirtualFileObject>> tmpFileMap = linker.getFileMap();
					if (!tmpFileMap.isEmpty()) {
						for (Map.Entry<String, Set<KagerowVirtualFileObject>> entry : tmpFileMap.entrySet()) {
							fileMap.merge(entry.getKey(), (Set<T>) entry.getValue(), this::mergeMap);
						}
					}

					try {

						// KDB初期化処理実行
						DDLConnectionHandler.initDDLConnectionHandler(kdbPath, script.getMode(), fileSet);

						// チャンクローダロード処理実行
						for (T file : fileSet) {
							submiter submiterImpl = new submiter(chunkLorder, file);
							Future<Void> waiter = exec.submit(submiterImpl);
							// ロード対象リストに追加
							waitTarget.put(waiter, file);
						}

						// ロード待機
						AppLogicException exp = new AppLogicException(ErrorMessage.CODE_004.getMessage(
								ApplicationWordDictionary.WCD_0001.getMessage()));
						for (Future<Void> waiter : waitTarget.keySet()) {
							try {
								waiter.get();
							} catch (ExecutionException e) {
								exp.addCause(e);
							}
						}

						// 制御された例外を検知した場合、例外をスロー
						if (exp.isCause()) {
							throw exp;
						}

						// KVIEW初期化処理実行
						DDLConnectionHandler.finishDDLConnectionHandler(kdbPath, script.getMode(), fileMap,
								script.getSchema());

						// 構築が完了した時点でビュー定義を更新
						if (!fileMap.isEmpty()) {
							for (Map.Entry<String, Set<T>> entry : fileMap.entrySet()) {
								this.fileMap.merge(entry.getKey(), (Set<T>) entry.getValue(), this::mergeMap);
							}
						}

					} finally {
						// 必ず処理終了後、セマフォを解放
						KDB_MAX_PLAN_TASK_COUNT.release(lordStep);
					}

					// SQL実行
					try (QueryConnectionHandler connectionHandler = new QueryConnectionHandler(script.getMode(),
							kdbPath)) {
						// 実行対象は必ず1つとなるため、はじめに見つかった要素を返却
						List<CachedRowSet> tmpResult = connectionHandler.transaction(processedSql);
						return tmpResult.stream().findFirst();
					}

				}
			} finally {
				// 排他制御を解除
				if (lock.isLocked()) {
					lock.unlock();
				}
			}

		} catch (Exception e) {
			throw new KagerowExecuteException(e);
		}

		// 実行対象が見つからない場合、空のOptionalを返却する
		return Optional.empty();

	}

	/** {@inheritDoc} */
	@Override
	public boolean isCache() {
		return !StringUtils.DEFAULT.equals(cacheId);
	}

	/** {@inheritDoc} */
	@Override
	public boolean isIgnoreCashe() {
		return ignoreCache;
	}

	/** {@inheritDoc} */
	@Override
	public String getCacheTime() {
		if (!isCache()) {
			return null;
		}
		return cacheObject.lastUpdateAt();
	}

	// ##########################################################################
	// # JMX
	// ##########################################################################

	/** {@inheritDoc} */
	@Override
	public String getSessionId() {
		return sessionId.toString();
	}

	/** {@inheritDoc} */
	@Override
	public int getHistorySize() {
		return historyList.size();
	}

	/** {@inheritDoc} */
	@Override
	public int getHistoryCacheSize() {
		return softHistoryList.size();
	}

	/** {@inheritDoc} */
	@Override
	public int getCanExecutionSize() {
		return KDB_MAX_PLAN_TASK_COUNT.availablePermits();
	}

}

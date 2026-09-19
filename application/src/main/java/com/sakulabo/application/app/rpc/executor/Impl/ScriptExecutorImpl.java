package com.sakulabo.application.app.rpc.executor.Impl;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.UnaryOperator;

import com.sakulabo.application.app.rpc.RpcFilter;
import com.sakulabo.application.app.rpc.RpcMethod;
import com.sakulabo.application.app.rpc.RpcMethodParam;
import com.sakulabo.application.app.rpc.RpcSetting;
import com.sakulabo.application.app.rpc.datatype.receive.BooleanReceiveDataType;
import com.sakulabo.application.app.rpc.datatype.receive.PathReceiveDataType;
import com.sakulabo.application.app.rpc.datatype.receive.StringReceiveDataType;
import com.sakulabo.application.app.rpc.datatype.send.StringSendDataType;
import com.sakulabo.application.app.rpc.exception.RpcRuntimeException;
import com.sakulabo.application.app.rpc.executor.ScriptExecutor;
import com.sakulabo.application.app.rpc.filters.CertificationFilter;
import com.sakulabo.application.model.Script.KSQLScriptModel;
import com.sakulabo.application.service.Script.KSQLService;
import com.sakulabo.core.Kagerow.Adapter.KagerowExecutionPlanBaseAdapter;
import com.sakulabo.core.Kagerow.Exception.AppLogicException;
import com.sakulabo.core.Kagerow.Exception.KFileParseException;
import com.sakulabo.core.Kagerow.Exception.KSQLParseException;
import com.sakulabo.core.Kagerow.Utilities.KagerowExecutionPlanAccessor;
import com.sakulabo.core.Kagerow.Utilities.KagerowScriptAccessor;
import com.sakulabo.core.Kagerow.Utilities.KagerowUtilities;

/**
 * スクリプト実行機能のRPCコントローラー実装クラス
 *
 * @author keeeeeent
 */
@RpcSetting("script")
@RpcFilter(filter = CertificationFilter.class, required = true)
public class ScriptExecutorImpl implements ScriptExecutor {

	/** セッション格納メモリ */
	private static final Map<UUID, KagerowExecutionPlanAdapterImpl> sessions = new ConcurrentHashMap<>();

	/**
	 * セッション管理クラス
	 */
	private class KagerowExecutionPlanAdapterImpl extends KagerowExecutionPlanBaseAdapter
			implements UnaryOperator<KagerowScriptAccessor> {

		/** 有効期間 */
		private static final Duration MAX_LIMIT = Duration.ofMinutes(30);
		/** 有効期限 */
		private volatile Instant limit = Instant.now().plus(MAX_LIMIT);
		/** セッション */
		private volatile KagerowExecutionPlanAccessor plan;
		/** スクリプト環境変数 */
		private volatile List<String> env = new ArrayList<>();
		/** 実行記録出力先 */
		private volatile StringWriter store = new StringWriter();
		/** 実行詳細記録インスタンス */
		private volatile PrintWriter output = new PrintWriter(store);
		/** KSQLカウンター */
		private volatile int sqlCount;
		/** 入力プラグインカウンター */
		private volatile int inputPluginCount;
		/** 出力プラグインカウンター */
		private volatile int outputPluginCount;
		/** 開始時刻 */
		private volatile Instant startTime;
		/** KSQLモデル */
		private final KSQLScriptModel model;

		/**
		 * コンストラクタ
		 * 
		 * @param path         実行スクリプトパス
		 * @param isSecureFlag セキュア実行フラグ
		 * @throws KFileParseException KFile解析エラー
		 * @throws KSQLParseException  KSQL解析エラー
		 * @throws AppLogicException   アプリケーションロジック不正
		 */
		private KagerowExecutionPlanAdapterImpl(Path path, boolean isSecureFlag)
				throws KFileParseException, KSQLParseException, AppLogicException {
			KagerowScriptAccessor scriptAccessor = KagerowScriptAccessor.getInstance(path);
			model = new KSQLScriptModel();
			model.scriptAccessor = scriptAccessor;
			model.path = path;
			model.isSecure = isSecureFlag;
			model.planAdapter = this;
		}

		/**
		 * 有効期限内か判定します
		 * 
		 * @return 判定結果
		 */
		public boolean isValid() {
			return Instant.now().isAfter(limit);
		}

		/** {@inheritDoc} */
		@Override
		public KagerowScriptAccessor apply(KagerowScriptAccessor scriptAccessor) {
			Map<String, String> env = scriptAccessor.getEnv();
			for (String value : this.env) {
				String[] pair = value.split("=", 2);
				String key = pair[0];
				String val = pair.length > 1 ? pair[1] : "";
				env.put(key, val);
			}
			KagerowScriptAccessor newScriptAccessor = scriptAccessor.setEnv(env);
			return newScriptAccessor;
		}

		/** {@inheritDoc} */
		@Override
		public void start() {
			store = new StringWriter();
			output = new PrintWriter(store);
			sqlCount = 0;
			inputPluginCount = 0;
			outputPluginCount = 0;
			startTime = Instant.now();
			output.println("KagerowSQL");
			output.println("────────────────────────────────");
			output.println(
					"Started: " + LocalDateTime.now().format(
							DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
		}

		/** {@inheritDoc} */
		@Override
		public void end() {
			Instant endTime = Instant.now();
			long elapsed = Duration.between(startTime, endTime).toMillis();
			output.println();
			output.println("────────────────────────────────");
			output.println(
					"Finished: " + LocalDateTime.now().format(
							DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
			output.printf(
					"Elapsed: %d ms%n",
					elapsed);
		}

		/** {@inheritDoc} */
		@Override
		public void startValidation() {
			output.println();
			output.println("Validation");
		}

		/** {@inheritDoc} */
		@Override
		public void endValidation() {
			output.println("  ✓ Completed");
		}

		/** {@inheritDoc} */
		@Override
		public void startCreateKDB() {
			output.println();
			output.println("Virtual Database");
		}

		/** {@inheritDoc} */
		@Override
		public void endCreateKDB() {
			output.println("  ✓ Created");
		}

		/** {@inheritDoc} */
		@Override
		public void startDoInputPlugin() {
			inputPluginCount = 0;
			output.println();
			output.println("Input Plugins");
		}

		/** {@inheritDoc} */
		@Override
		public void endDoInputPlugin() {
			output.println("  ✓ Input plugins completed");
		}

		/** {@inheritDoc} */
		@Override
		public void startDoInputPluginIndividual() {
			output.printf(
					"  → Executing input plugin #%d...%n",
					++inputPluginCount);
		}

		/** {@inheritDoc} */
		@Override
		public void endDoInputPluginIndividual(ExecutPluginInfo info) {
			long elapsed = Duration.between(
					info.startTime(),
					info.endTime()).toMillis();
			String mark = info.exitCode() == ExitCode.SUCCESS ? "✓" : "✗";
			output.printf(
					"  %s Completed [%s] (%s, %d ms) [%s]%n",
					mark,
					info.pluginName(),
					info.exitCode(),
					elapsed,
					info.id());
		}

		/** {@inheritDoc} */
		@Override
		public void startDoKsql() {
			sqlCount = 0;
			output.println();
			output.println("SQL Execution");
		}

		/** {@inheritDoc} */
		@Override
		public void endDoKsql() {
			output.println("  ✓ SQL execution completed");
		}

		/** {@inheritDoc} */
		@Override
		public void startDoKsqlIndividual(String id) {
			output.printf(
					"  → Executing SQL#%d [%s]...%n",
					++sqlCount,
					id);
		}

		/** {@inheritDoc} */
		@Override
		public void endDoKsqlIndividual(ExecutKsqlInfo info) {
			long elapsed = Duration.between(
					info.startTime(),
					info.endTime()).toMillis();
			String mark = info.exitCode() == ExitCode.SUCCESS ? "✓" : "✗";
			output.printf(
					"  %s Completed (%s, %d ms)%n",
					mark,
					info.exitCode(),
					elapsed);
		}

		/** {@inheritDoc} */
		@Override
		public void startDoOutputPlugin() {
			outputPluginCount = 0;
			output.println();
			output.println("Output Plugins");
		}

		/** {@inheritDoc} */
		@Override
		public void endDoOutputPlugin() {
			output.println("  ✓ Output plugins completed");
		}

		/** {@inheritDoc} */
		@Override
		public void startDoOutputPluginIndividual() {
			output.printf(
					"  → Executing output plugin #%d...%n",
					++outputPluginCount);
		}

		/** {@inheritDoc} */
		@Override
		public void endDoOutputPluginIndividual(ExecutPluginInfo info) {
			long elapsed = Duration.between(
					info.startTime(),
					info.endTime()).toMillis();
			String mark = info.exitCode() == ExitCode.SUCCESS ? "✓" : "✗";
			output.printf(
					"  %s Completed [%s] (%s, %d ms) [%s]%n",
					mark,
					info.pluginName(),
					info.exitCode(),
					elapsed,
					info.id());
		}

		/** {@inheritDoc} */
		@Override
		public void startDoCmd() {
			output.println();
			output.println("Command Execution");
			output.println("  → Running...");
			output.println();
		}

		/** {@inheritDoc} */
		@Override
		public void endDoCmd() {
			output.println();
			output.println("  ✓ Completed");
		}

		/** {@inheritDoc} */
		@Override
		public String toString() {
			return store.toString();
		}

	}

	/** {@inheritDoc} */
	@Override
	@RpcMethod("execute")
	public ExecuteResult executeScript(
			@RpcMethodParam("sessionid") StringReceiveDataType sessionId,
			@RpcMethodParam("secure") BooleanReceiveDataType isSecure,
			@RpcMethodParam("path") PathReceiveDataType path) {

		try {

			// 引数取得
			boolean isSecureFlag = isSecure.getRawType().orElse(false);
			String uuid = sessionId.getRawType().orElse(UUID.randomUUID().toString());
			KagerowExecutionPlanAdapterImpl adapter = null;

			// サービス取得
			KSQLService service = KagerowUtilities.getBean(KSQLService.class, null).get();

			// パスを取得
			Optional<Path> filePath = path.getRawType();
			if (filePath.isEmpty()) {
				throw new RpcRuntimeException("Please specify the path to the script file");
			}
			// アダプター取得
			UUID key = UUID.fromString(uuid);
			adapter = sessions.get(key);
			if (Objects.isNull(adapter)) {
				adapter = new KagerowExecutionPlanAdapterImpl(filePath.get(), isSecureFlag);
				sessions.put(key, adapter);
			}
			// 有効期限の確認
			if (!adapter.isValid()) {
				// 有効期限切れの場合、セッションをクローズ
				sessions.remove(key);
				adapter.plan.close();
				// 例外スロー
				throw new RpcRuntimeException("This session has already ended");
			}
			// 有効期限内の場合スクリプト実行
			// スクリプト実行
			KagerowExecutionPlanAccessor plan = service.executionScriptWithEdit(adapter.model, adapter);
			// セッション保存
			adapter.plan = plan;

			// 実行結果取得
			StringSendDataType sendResult = new StringSendDataType(adapter.toString());
			ExecuteResult result = new ExecuteResult(sendResult);
			return result;

		} catch (Exception e) {
			throw new RpcRuntimeException("Script Execution Failed", e);
		}
	}
}

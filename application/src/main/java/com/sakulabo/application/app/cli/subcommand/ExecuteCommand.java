package com.sakulabo.application.app.cli.subcommand;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.UnaryOperator;

import com.sakulabo.application.app.cli.converter.ExistingFilePathConverter;
import com.sakulabo.application.model.Script.KSQLScriptModel;
import com.sakulabo.application.service.Script.KSQLService;
import com.sakulabo.core.Kagerow.Adapter.KagerowExecutionPlanAdapter;
import com.sakulabo.core.Kagerow.Exception.KagerowExecuteException;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;
import com.sakulabo.core.Kagerow.Utilities.KagerowScriptAccessor;
import com.sakulabo.core.Kagerow.Utilities.KagerowUtilities;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * スクリプト実行機能実装クラスです
 *
 * @author keeeeeent
 */
@Command(name = "run", mixinStandardHelpOptions = true)
public class ExecuteCommand
		implements Callable<Integer>, KagerowExecutionPlanAdapter, UnaryOperator<KagerowScriptAccessor> {

	/** インポートファイルパス */
	@Option(names = { "--path",
			"-p" }, required = true, description = "Run script file path.", converter = ExistingFilePathConverter.class)
	public Path path;
	/** セキュアモード */
	@Option(names = { "--secure", "-s" }, negatable = true, description = "Secure mode. Default: ${DEFAULT-VALUE}")
	public boolean isSecure = false;
	/** スクリプト環境変数 */
	@Option(names = "--env", description = "Environment variable. Example: --env KEY=VALUE")
	public List<String> env;
	/** KSQLカウンター */
	private int sqlCount;
	/** 入力プラグインカウンター */
	private int inputPluginCount;
	/** 出力プラグインカウンター */
	private int outputPluginCount;
	/** 開始時刻 */
	private Instant startTime;

	/** {@inheritDoc} */
	@Override
	public Integer call() throws Exception {

		try {
			// 実行モデル生成
			KagerowScriptAccessor scriptAccessor = KagerowScriptAccessor.getInstance(path);
			KSQLScriptModel model = new KSQLScriptModel();
			model.scriptAccessor = scriptAccessor;
			model.path = path;
			model.isSecure = isSecure;
			model.planAdapter = this;
			// サービス取得
			KSQLService service = KagerowUtilities.getBean(KSQLService.class, null).get();
			// スクリプト実行
			service.executionScriptWithEdit(model, this);
			return Integer.valueOf(0);
		} catch (KagerowExecuteException e) {
			System.err.println(e.getMessage());
			return Integer.valueOf(2);
		} catch (Exception e) {
			KagerowLogger.newAppLogger().err(e);
			return Integer.valueOf(1);
		}

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
		startTime = Instant.now();
		System.out.println("KagerowSQL");
		System.out.println("────────────────────────────────");
		System.out.println(
				"Started: " + LocalDateTime.now().format(
						DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
	}

	/** {@inheritDoc} */
	@Override
	public void end() {
		Instant endTime = Instant.now();
		long elapsed = Duration.between(startTime, endTime).toMillis();
		System.out.println();
		System.out.println("────────────────────────────────");
		System.out.println(
				"Finished: " + LocalDateTime.now().format(
						DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
		System.out.printf(
				"Elapsed: %d ms%n",
				elapsed);
	}

	/** {@inheritDoc} */
	@Override
	public void startValidation() {
		System.out.println();
		System.out.println("Validation");
	}

	/** {@inheritDoc} */
	@Override
	public void endValidation() {
		System.out.println("  ✓ Completed");
	}

	/** {@inheritDoc} */
	@Override
	public void startCreateKDB() {
		System.out.println();
		System.out.println("Virtual Database");
	}

	/** {@inheritDoc} */
	@Override
	public void endCreateKDB() {
		System.out.println("  ✓ Created");
	}

	/** {@inheritDoc} */
	@Override
	public void startDoInputPlugin() {
		inputPluginCount = 0;
		System.out.println();
		System.out.println("Input Plugins");
	}

	/** {@inheritDoc} */
	@Override
	public void endDoInputPlugin() {
		System.out.println("  ✓ Input plugins completed");
	}

	/** {@inheritDoc} */
	@Override
	public void startDoInputPluginIndividual() {
		System.out.printf(
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
		System.out.printf(
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
		System.out.println();
		System.out.println("SQL Execution");
	}

	/** {@inheritDoc} */
	@Override
	public void endDoKsql() {
		System.out.println("  ✓ SQL execution completed");
	}

	/** {@inheritDoc} */
	@Override
	public void startDoKsqlIndividual(String id) {
		System.out.printf(
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
		System.out.printf(
				"  %s Completed (%s, %d ms)%n",
				mark,
				info.exitCode(),
				elapsed);
	}

	/** {@inheritDoc} */
	@Override
	public void startDoOutputPlugin() {
		outputPluginCount = 0;
		System.out.println();
		System.out.println("Output Plugins");
	}

	/** {@inheritDoc} */
	@Override
	public void endDoOutputPlugin() {
		System.out.println("  ✓ Output plugins completed");
	}

	/** {@inheritDoc} */
	@Override
	public void startDoOutputPluginIndividual() {
		System.out.printf(
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
		System.out.printf(
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
		System.out.println();
		System.out.println("Command Execution");
		System.out.println("  → Running...");
		System.out.println();
	}

	/** {@inheritDoc} */
	@Override
	public void endDoCmd() {
		System.out.println();
		System.out.println("  ✓ Completed");
	}

}

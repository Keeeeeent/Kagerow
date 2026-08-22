package com.sakulabo.application.app.cli;

import picocli.CommandLine.Command;
import picocli.CommandLine;

import com.sakulabo.application.common.initializer.CommandComponent;
import com.sakulabo.application.common.spi.ViewRunner;
import com.sakulabo.regulation.annotation.KagerowComponent;

import java.io.Console;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sakulabo.application.Main;
import com.sakulabo.application.app.cli.subcommand.AgentCommand;
import com.sakulabo.application.app.cli.subcommand.BackupCommand;
import com.sakulabo.application.app.cli.subcommand.CacheCommand;
import com.sakulabo.application.app.cli.subcommand.ExecuteCommand;
import com.sakulabo.application.app.cli.subcommand.ImportCommand;
import com.sakulabo.application.app.cli.subcommand.ManageCommand;
import com.sakulabo.application.app.cli.subcommand.PluginCommand;
import com.sakulabo.application.app.cli.subcommand.SchemaCommand;
import com.sakulabo.application.app.cli.subcommand.SettingCommand;
import com.sakulabo.application.app.cli.subcommand.SynonymCommand;
import com.sakulabo.application.app.cli.subcommand.TableCommand;

/**
 * CLIアプリケーションのメインコマンド実装クラスです
 *
 * @author keeeeeent
 */
@KagerowComponent
@CommandComponent
@Command(name = "kagerow", mixinStandardHelpOptions = true, version = "1.0.0", subcommands = {
		AgentCommand.class,
		ImportCommand.class,
		ExecuteCommand.class,
		SchemaCommand.class,
		TableCommand.class,
		SynonymCommand.class,
		SettingCommand.class,
		PluginCommand.class,
		CacheCommand.class,
		BackupCommand.class,
		ManageCommand.class
})
public class MainCommand implements ViewRunner {

	/** {@inheritDoc} */
	@Override
	public void start() {
		// コマンド生成
		CommandLine command = new CommandLine(this);
		// 実行設定
		// 実行結果反映
		if (0 < Main.args.length) {
			int exitCode = command.execute(Main.args);
			System.exit(exitCode);
		} else {
			Console console = System.console();
			while (true) {
				String args = console.readLine("kagerow> ");
				if ("exit".equals(args.strip())) {
					System.exit(0);
				} else {
					String[] arrayArgs = parseArguments(args);
					if (0 < arrayArgs.length) {
						command.execute(arrayArgs);
					}
				}
			}
		}
	}

	/**
	 * コマンドライン引数を生成します
	 * @param input 入力文字列
	 * @return コマンドライン引数
	 */
	private static String[] parseArguments(String input) {
		List<String> args = new ArrayList<>();
		Matcher matcher = Pattern.compile("\"([^\"]*)\"|'([^']*)'|(\\S+)").matcher(input);
		while (matcher.find()) {
			if (matcher.group(1) != null) {
				args.add(matcher.group(1));
			} else if (matcher.group(2) != null) {
				args.add(matcher.group(2));
			} else {
				args.add(matcher.group(3));
			}
		}
		return args.toArray(String[]::new);
	}

}

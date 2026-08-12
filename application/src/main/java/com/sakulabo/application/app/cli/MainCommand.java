package com.sakulabo.application.app.cli;

import picocli.CommandLine.Command;
import picocli.CommandLine;

import com.sakulabo.application.common.initializer.CommandComponent;
import com.sakulabo.application.common.spi.ViewRunner;
import com.sakulabo.regulation.annotation.KagerowComponent;

import com.sakulabo.application.Main;
import com.sakulabo.application.app.cli.subcommand.AgentCommand;
import com.sakulabo.application.app.cli.subcommand.ExecuteCommand;
import com.sakulabo.application.app.cli.subcommand.ImportCommand;
import com.sakulabo.application.app.cli.subcommand.SchemaCommand;

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
		SchemaCommand.class
})
public class MainCommand implements ViewRunner {

	/** {@inheritDoc} */
	@Override
	public void start() {
		// コマンド生成
		CommandLine command = new CommandLine(this);
		// 実行設定
		// 実行結果反映
		int exitCode = command.execute(Main.args);
		System.exit(exitCode);
	}

}

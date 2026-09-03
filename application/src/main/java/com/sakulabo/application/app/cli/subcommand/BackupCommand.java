package com.sakulabo.application.app.cli.subcommand;

import java.nio.file.Path;
import java.util.concurrent.Callable;

import com.sakulabo.application.app.cli.converter.ExistingFilePathConverter;
import com.sakulabo.application.app.cli.converter.ExistingParentDirConverter;
import com.sakulabo.core.Kagerow.Utilities.KagerowDataDump;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * バックアップ管理機能実装クラスです
 *
 * @author keeeeeent
 */
@Command(name = "backup", subcommands = {
		BackupCommand.BackupExportCommand.class,
		BackupCommand.BackupImportCommand.class,
})
public class BackupCommand {

	/**
	 * バックアップファイル出力コマンド
	 */
	@Command(name = "export")
	public static class BackupExportCommand implements Callable<Integer> {

		/** インポートファイルパス */
		@Option(names = { "--path",
				"-p" }, required = true, description = "Export file path.", converter = ExistingParentDirConverter.class)
		public Path path;

		/** {@inheritDoc} */
		@Override
		public Integer call() throws Exception {
			try {
				KagerowDataDump dump = KagerowDataDump.newBasicInstance();
				dump.exportDump(path);
			} catch (Exception e) {
				KagerowLogger.newAppLogger().err(e);
				return Integer.valueOf(1);
			}
			return Integer.valueOf(0);
		}

	}

	/**
	 * バックアップファイル取込コマンド
	 */
	@Command(name = "import")
	public static class BackupImportCommand implements Callable<Integer> {

		/** インポートファイルパス */
		@Option(names = { "--path",
				"-p" }, required = true, description = "Import file path.", converter = ExistingFilePathConverter.class)
		public Path path;

		/** {@inheritDoc} */
		@Override
		public Integer call() throws Exception {
			try {
				KagerowDataDump dump = KagerowDataDump.newBasicInstance();
				dump.importDump(path);
			} catch (Exception e) {
				KagerowLogger.newAppLogger().err(e);
				return Integer.valueOf(1);
			}
			return Integer.valueOf(0);
		}

	}

}

package com.sakulabo.application.app.cli.subcommand;

import java.util.Map;
import java.util.concurrent.Callable;

import com.sakulabo.core.Kagerow.Context.KagerowVirtualDirContext;
import com.sakulabo.core.Kagerow.Context.KagerowVirtualFileContext;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;
import com.sakulabo.core.Kagerow.Utilities.KagerowUtilities;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * シノニム管理機能実装クラスです
 *
 * @author keeeeeent
 */
@Command(name = "synonym", subcommands = {
		SynonymCommand.SynonymListCommand.class,
})
public class SynonymCommand {

	/**
	 * シノニム一覧確認コマンド
	 */
	@Command(name = "list")
	public static class SynonymListCommand implements Callable<Integer> {

		/** スキーマ名称 */
		@Option(names = { "--schema", "-s" }, required = true)
		private String schemaName;

		/** {@inheritDoc} */
		@Override
		public Integer call() throws Exception {
			try {
				KagerowVirtualFileContext ctx = KagerowUtilities.getContext(KagerowVirtualFileContext._NAME);
				KagerowVirtualDirContext dirCtx = ctx.lookup(schemaName);
				Map<String, String> synonyms = dirCtx.getSynonymMapList();
				if (0 < synonyms.size()) {
					System.out.printf("%-17s %s%n", "Logical Name", "Physical Name");
					System.out.println("────────────────────────────────────────────────");
				}
				for (Map.Entry<String, String> entry : synonyms.entrySet()) {
					System.out.printf("%-17s %s%n", entry.getKey(), entry.getValue());
				}
			} catch (Exception e) {
				KagerowLogger.newAppLogger().err(e);
				return Integer.valueOf(1);
			}
			return Integer.valueOf(0);
		}

	}

}

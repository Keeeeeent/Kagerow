package com.sakulabo.application.app.cli.subcommand;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;

import javax.naming.Binding;
import javax.naming.Name;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import com.sakulabo.core.Kagerow.Contents.KagerowVirtualFileContent;
import com.sakulabo.core.Kagerow.Contents.KagerowVirtualFileContent.KagerowVirtualFileObject;
import com.sakulabo.core.Kagerow.Context.KagerowVirtualDirContext;
import com.sakulabo.core.Kagerow.Context.KagerowVirtualFileContext;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;
import com.sakulabo.core.Kagerow.Utilities.KagerowTransaction;
import com.sakulabo.core.Kagerow.Utilities.KagerowUtilities;

import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * テーブル管理機能実装クラスです
 *
 * @author keeeeeent
 */
@Command(name = "table", subcommands = {
		TableCommand.TableListCommand.class,
		TableCommand.TableInfoCommand.class,
		TableCommand.TableDeleteCommand.class
})
public class TableCommand {

	/**
	 * サイズフォーマット
	 *
	 * @param sizeBytes サイズ（バイト）
	 * @return フォーマット済みのサイズ
	 */
	private static String formatSize(long sizeBytes) {
		if (sizeBytes < 1024) {
			return sizeBytes + " B";
		}
		double size = sizeBytes;
		String[] units = { "KB", "MB", "GB", "TB", "PB", "EB" };
		for (String unit : units) {
			size /= 1024;

			if (size < 1024) {
				return String.format("%.1f %s", size, unit);
			}
		}
		return String.format("%.1f ZB", size / 1024);
	}

	/**
	 * テーブル一覧確認コマンド
	 */
	@Command(name = "list")
	public static class TableListCommand implements Callable<Integer> {

		/** スキーマ名称 */
		@Option(names = { "--schema", "-s" }, required = true)
		private String schemaName;
		/** テーブル名称 */
		@Option(names = { "--table", "-t" })
		private String tableName;
		/** テーブル名称 */
		@Option(names = "--synonym")
		private String synonymName;

		/** {@inheritDoc} */
		@Override
		public Integer call() throws Exception {
			try {
				KagerowVirtualFileContext ctx = KagerowUtilities.getContext(KagerowVirtualFileContext._NAME);
				KagerowVirtualDirContext dirCtx = ctx.lookup(schemaName);
				Map<String, String> synonyms = dirCtx.getSynonymMapList();
				if (Objects.isNull(tableName)) {
					for (Map.Entry<String, String> entry : synonyms.entrySet()) {
						if (entry.getKey().equals(synonymName)) {
							tableName = entry.getValue();
							break;
						}
					}
				}
				if (Objects.isNull(tableName)) {
					printTableOnly(dirCtx, synonyms);
				} else {
					printTableGeneration(dirCtx, synonyms);
				}
			} catch (Exception e) {
				KagerowLogger.newAppLogger().err(e);
				return Integer.valueOf(1);
			}
			return Integer.valueOf(0);
		}

		/**
		 * テーブルの世代情報を出力します
		 * @param dirCtx テーブルリスト
		 * @param synonyms シノニム設定一覧
		 * @throws NamingException コンテキスト取得失敗
		 */
		private void printTableGeneration(KagerowVirtualDirContext dirCtx, Map<String, String> synonyms)
				throws NamingException {
			KagerowVirtualFileContent cnt = dirCtx.lookup(tableName);
			String physicaTableName = "?????";
			for (Map.Entry<String, String> entry : synonyms.entrySet()) {
				if (entry.getValue().equals(tableName)) {
					physicaTableName = entry.getKey();
					break;
				}
			}
			int size = cnt.contentSize();
			if (0 < size) {
				System.out.printf("%-17s %-17s %-30s %s%n",
						"Index",
						"Size",
						"Created",
						"Name");
				System.out.println(
						"────────────────────────────────────────────────────────────────────────────────────────────────");
			}
			for (int i = 0; i < size; i++) {
				KagerowVirtualFileObject fileObject = cnt.get(i);
				System.out.printf("%-17d %-17s %-30s %s%n",
						i,
						formatSize(fileObject.datSize().longValue()),
						fileObject.createTime().toString(),
						String.format("${%s[%d]}", physicaTableName, i));
			}
		}

		/**
		 * テーブルの情報を出力します
		 * @param dirCtx テーブルリスト
		 * @param synonyms シノニム設定一覧
		 * @throws NamingException コンテキスト取得失敗
		 */
		private void printTableOnly(KagerowVirtualDirContext dirCtx, Map<String, String> synonyms)
				throws NamingException {
			NamingEnumeration<Binding> list = dirCtx.listBindings((Name) null);
			if (list.hasMore()) {
				System.out.printf("%-17s %s%n", "Logical Name", "Physical Name");
				System.out.println("────────────────────────────────────────────────");
			}
			while (list.hasMore()) {
				String physicalName = list.next().getName();
				String logicalName = "?????";
				for (Map.Entry<String, String> entry : synonyms.entrySet()) {
					if (entry.getValue().equals(physicalName)) {
						logicalName = entry.getKey();
						break;
					}
				}
				System.out.printf("%-17s %s%n", logicalName, physicalName);
			}
		}

	}

	/**
	 * テーブル情報確認コマンド
	 */
	@Command(name = "info")
	public static class TableInfoCommand implements Callable<Integer> {

		/** スキーマ名称 */
		@Option(names = { "--schema", "-s" }, required = true)
		private String schemaName;
		/** テーブル名称 */
		@Option(names = { "--table", "-t" })
		private String tableName;
		/** テーブル名称 */
		@Option(names = "--synonym")
		private String synonymName;
		/** テーブル世代 */
		@Option(names = { "--generation", "-g" })
		private int generation = 0;

		/** {@inheritDoc} */
		@Override
		public Integer call() throws Exception {
			if (Objects.isNull(tableName) && Objects.isNull(synonymName)) {
				System.err.println("synonym or table must be specified");
				return Integer.valueOf(2);
			}
			KagerowVirtualFileContext ctx = KagerowUtilities.getContext(KagerowVirtualFileContext._NAME);
			KagerowVirtualDirContext dirCtx = ctx.lookup(schemaName);
			Map<String, String> synonyms = dirCtx.getSynonymMapList();
			if (Objects.isNull(tableName)) {
				for (Map.Entry<String, String> entry : synonyms.entrySet()) {
					if (entry.getKey().equals(synonymName)) {
						tableName = entry.getValue();
						break;
					}
				}
			}
			KagerowVirtualFileContent cnt = dirCtx.lookup(tableName);
			KagerowVirtualFileObject fileObject = cnt.get(generation);
			System.out.println("Table Information");
			System.out.println("────────────────────────────────");
			System.out.printf("KagerowURI   : %s%n", fileObject.uri().get());
			System.out.printf("PhysicsName  : %s%n", schemaName);
			System.out.printf("LogicName    : %s%n", tableName);
			System.out.printf("Size         : %s%n", formatSize(fileObject.datSize().longValue()));
			System.out.printf("Created      : %s%n", fileObject.createTime().toString());
			System.out.println();
			System.out.println("Column Information");
			System.out.println("────────────────────────────────");
			int maxHeaderCount = 13;
			for (int i = 0; i < fileObject.headerData().length; i++) {
				maxHeaderCount = Math.max(maxHeaderCount, fileObject.headerData()[i].length());
			}
			String format = "%-" + maxHeaderCount + "s: %s%n";
			for (int i = 0; i < fileObject.headerData().length; i++) {
				System.out.printf(format, fileObject.headerData()[i], fileObject.dataType()[i]);
			}
			return Integer.valueOf(0);
		}

	}

	/**
	 * テーブル削除コマンド
	 */
	@Command(name = "delete")
	public static class TableDeleteCommand implements Callable<Integer> {

		/**
		 * オプショングループ
		 */
		private static class Options {
			/** テーブル名称 */
			@Option(names = { "--table", "-t" })
			private String tableName;
			/** テーブル名称 */
			@Option(names = "--synonym")
			private String synonymName;
		}

		/** スキーマ名称 */
		@Option(names = { "--schema", "-s" }, required = true)
		private String schemaName;
		/** オプション */
		@ArgGroup(exclusive = true, multiplicity = "1")
		private Options options;
		/** テーブル世代 */
		@Option(names = { "--generation", "-g" })
		private int generation = 0;

		/** {@inheritDoc} */
		@Override
		public Integer call() throws Exception {
			if (Objects.isNull(options.tableName) && Objects.isNull(options.synonymName)) {
				System.err.println("synonym or table must be specified");
				return Integer.valueOf(2);
			}
			KagerowVirtualFileContext ctx = KagerowUtilities.getContext(KagerowVirtualFileContext._NAME);
			KagerowVirtualDirContext dirCtx = ctx.lookup(schemaName);
			Map<String, String> synonyms = dirCtx.getSynonymMapList();
			if (Objects.isNull(options.tableName)) {
				for (Map.Entry<String, String> entry : synonyms.entrySet()) {
					if (entry.getKey().equals(options.synonymName)) {
						options.tableName = entry.getValue();
						break;
					}
				}
			}
			KagerowTransaction tran = KagerowTransaction.getTransactionFromSchemaName(schemaName);
			try (tran) {
				KagerowVirtualFileContent cnt = dirCtx.lookup(options.tableName);
				if (cnt.contentSize() < generation) {
					System.err.println("The number of specified generations exceeds the maximum limit");
					return Integer.valueOf(3);
				}
				if (cnt.contentSize() >= 1) {
					System.err.println("There must be at least one table");
					return Integer.valueOf(4);
				}
				KagerowVirtualFileObject fileObject = cnt.get(generation);
				String target = KagerowVirtualFileContent.getGeneration(fileObject);
				cnt.destroySubcontext(target);
			} catch (Exception e) {
				KagerowLogger.newAppLogger().err(e);
				return Integer.valueOf(1);
			}
			return Integer.valueOf(0);
		}

	}

}

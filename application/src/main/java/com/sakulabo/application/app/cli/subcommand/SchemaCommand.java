package com.sakulabo.application.app.cli.subcommand;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.concurrent.Callable;

import javax.naming.Binding;
import javax.naming.Name;
import javax.naming.NamingEnumeration;

import com.sakulabo.core.Kagerow.Context.KagerowVirtualDirContext;
import com.sakulabo.core.Kagerow.Context.KagerowVirtualFileContext;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;
import com.sakulabo.core.Kagerow.Utilities.KagerowTransaction;
import com.sakulabo.core.Kagerow.Utilities.KagerowUtilities;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * スキーマ管理機能実装クラスです
 *
 * @author keeeeeent
 */
@Command(name = "schema", subcommands = {
		SchemaCommand.SchemaListCommand.class,
		SchemaCommand.SchemaDeleteCommand.class,
		SchemaCommand.SchemaInfoCommand.class
})
public class SchemaCommand {

	/**
	 * スキーマ一覧確認コマンド
	 */
	@Command(name = "list")
	public static class SchemaListCommand implements Callable<Integer> {

		/** {@inheritDoc} */
		@Override
		public Integer call() throws Exception {
			try {
				KagerowVirtualFileContext ctx = KagerowUtilities.getContext(KagerowVirtualFileContext._NAME);
				NamingEnumeration<Binding> list = ctx.listBindings((Name) null);
				while (list.hasMore()) {
					String schemaName = list.next().getName();
					if (!KagerowVirtualFileContext.SYSTEM_SCHEMA.equals(schemaName)) {
						System.out.println(schemaName);
					}
				}
			} catch (Exception e) {
				KagerowLogger.newAppLogger().err(e);
				return Integer.valueOf(1);
			}
			return Integer.valueOf(0);
		}

	}

	/**
	 * スキーマ削除コマンド
	 */
	@Command(name = "delete")
	public static class SchemaDeleteCommand implements Callable<Integer> {

		/**　スキーマ名称 */
		@Option(names = "--name", required = true)
		private String schemaName;

		/** {@inheritDoc} */
		@Override
		public Integer call() throws Exception {
			if (KagerowVirtualFileContext.SYSTEM_SCHEMA.equals(schemaName)) {
				return Integer.valueOf(2);
			}
			KagerowTransaction tran = KagerowTransaction.getTransactionFromSchemaName(schemaName);
			try (tran) {
				KagerowVirtualFileContext ctx = KagerowUtilities.getContext(KagerowVirtualFileContext._NAME);
				ctx.destroySubcontext(schemaName);
				tran.commit();
			} catch (Exception e) {
				KagerowLogger.newAppLogger().err(e);
				tran.rollback(e);
				return Integer.valueOf(1);
			}
			return Integer.valueOf(0);
		}

	}

	/**
	 * スキーマ詳細確認コマンド
	 */
	@Command(name = "info")
	public static class SchemaInfoCommand implements Callable<Integer> {

		/** スキーマ名称 */
		@Option(names = "--name", required = true)
		private String schemaName;

		/** {@inheritDoc} */
		@Override
		public Integer call() throws Exception {
			if (KagerowVirtualFileContext.SYSTEM_SCHEMA.equals(schemaName)) {
				return Integer.valueOf(2);
			}
			try {
				KagerowVirtualFileContext ctx = KagerowUtilities.getContext(KagerowVirtualFileContext._NAME);
				KagerowVirtualDirContext cnt = ctx.lookup(schemaName);
				// 最終更新日
				Path path = cnt.getPath();
				BasicFileAttributeView view = Files.getFileAttributeView(path, BasicFileAttributeView.class);
				BasicFileAttributes attr = view.readAttributes();
				FileTime time = attr.lastModifiedTime();
				String lastUpdated = time.toInstant().toString();
				// サイズ
				long size = cnt.getSchemaContextSize();
				// テーブル数
				int tables = cnt.getSynonymMapList().size();
				// 表示
				System.out.println("Schema Information");
				System.out.println("────────────────────────────────");
				System.out.printf("Name         : %s%n", schemaName);
				System.out.printf("Last Updated : %s%n", lastUpdated);
				System.out.printf("Size         : %s%n", String.format("%dKB", size));
				System.out.printf("Tables       : %,d%n", tables);
			} catch (Exception e) {
				KagerowLogger.newAppLogger().err(e);
				return Integer.valueOf(1);
			}
			return Integer.valueOf(0);
		}

	}

}

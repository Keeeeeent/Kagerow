package com.sakulabo.application.app.cli.subcommand;

import java.util.concurrent.Callable;

import javax.naming.Binding;
import javax.naming.Name;
import javax.naming.NamingEnumeration;

import com.sakulabo.core.Kagerow.Contents.KagerowCacheContent;
import com.sakulabo.core.Kagerow.Context.KagerowCacheContext;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;
import com.sakulabo.core.Kagerow.Utilities.KagerowUtilities;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * キャッシュ管理機能実装クラスです
 *
 * @author keeeeeent
 */
@Command(name = "cache", subcommands = {
		CacheCommand.CacheListCommand.class,
		CacheCommand.CacheCreateCommand.class,
		CacheCommand.CacheDeleteCommand.class,
		CacheCommand.CacheInfoCommand.class,
})
public class CacheCommand {

	/**
	 * キャッシュ一覧確認コマンド
	 */
	@Command(name = "list")
	public static class CacheListCommand implements Callable<Integer> {

		/** {@inheritDoc} */
		@Override
		public Integer call() throws Exception {
			try {
				KagerowCacheContext ctx = KagerowUtilities.getContext(KagerowCacheContext._NAME);
				NamingEnumeration<Binding> list = ctx.listBindings((Name) null);
				if (list.hasMore()) {
					System.out.println();
					System.out.println("Cache Information");
					System.out.println("──────────────────────────────────────────────────────────────");
				}
				while (list.hasMore()) {
					String info = list.next().getName();
					System.out.printf("%-20s %n", info);
				}
			} catch (Exception e) {
				KagerowLogger.newAppLogger().err(e);
				return Integer.valueOf(1);
			}
			return Integer.valueOf(0);
		}

	}

	/**
	 * キャッシュ情報詳細表示コマンド
	 */
	@Command(name = "info")
	public static class CacheInfoCommand implements Callable<Integer> {

		/** キャッシュID */
		@Option(names = { "--id", "-i" }, required = true)
		private String cacheId;

		/** {@inheritDoc} */
		@Override
		public Integer call() throws Exception {
			try {
				KagerowCacheContext ctx = KagerowUtilities.getContext(KagerowCacheContext._NAME);
				KagerowCacheContent cnt = ctx.lookup(cacheId);
				NamingEnumeration<Binding> list = cnt.listBindings((Name) null);
				if (list.hasMore()) {
					System.out.println();
					System.out.println("Cache Details Information");
					System.out.println("──────────────────────────────────────────────────────────────");
				}
				while (list.hasMore()) {
					String info = list.next().getName();
					System.out.printf("%-20s %n", info);
				}
			} catch (Exception e) {
				KagerowLogger.newAppLogger().err(e);
				return Integer.valueOf(1);
			}
			return Integer.valueOf(0);
		}

	}

	/**
	 * キャッシュ生成コマンド
	 */
	@Command(name = "create")
	public static class CacheCreateCommand implements Callable<Integer> {

		/** {@inheritDoc} */
		@Override
		public Integer call() throws Exception {
			try {
				KagerowCacheContext ctx = KagerowUtilities.getContext(KagerowCacheContext._NAME);
				KagerowCacheContent cnt = ctx.createSubcontext((Name) null);
				System.out.println(cnt.getNameInNamespace());
			} catch (Exception e) {
				KagerowLogger.newAppLogger().err(e);
				return Integer.valueOf(1);
			}
			return Integer.valueOf(0);
		}

	}

	/**
	 * キャッシュ削除コマンド
	 */
	@Command(name = "delete")
	public static class CacheDeleteCommand implements Callable<Integer> {

		/** キャッシュID */
		@Option(names = { "--id", "-i" }, required = true)
		private String cacheId;

		/** {@inheritDoc} */
		@Override
		public Integer call() throws Exception {
			try {
				KagerowCacheContext ctx = KagerowUtilities.getContext(KagerowCacheContext._NAME);
				ctx.destroySubcontext(cacheId);
			} catch (Exception e) {
				KagerowLogger.newAppLogger().err(e);
				return Integer.valueOf(1);
			}
			return Integer.valueOf(0);
		}

	}

}

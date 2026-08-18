package com.sakulabo.application.app.cli.subcommand;

import java.util.Objects;
import java.util.concurrent.Callable;

import javax.naming.Binding;
import javax.naming.Name;
import javax.naming.NamingEnumeration;

import com.sakulabo.core.Kagerow.Contents.KagerowSettingContent;
import com.sakulabo.core.Kagerow.Context.KagerowSettingContext;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;
import com.sakulabo.core.Kagerow.Utilities.KagerowUtilities;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

/**
 * 設定管理機能実装クラスです
 *
 * @author keeeeeent
 */
@Command(name = "setting", mixinStandardHelpOptions = true, subcommands = {
		SettingCommand.SettingListCommand.class,
		SettingCommand.SettingGetCommand.class,
		SettingCommand.SettingSetCommand.class
})
public class SettingCommand {

	/**
	 * 設定一覧確認コマンド
	 */
	@Command(name = "list", mixinStandardHelpOptions = true)
	public static class SettingListCommand implements Callable<Integer> {

		/** 名前空間 */
		@Parameters(index = "0", arity = "0..1")
		private String namespace;

		/** {@inheritDoc} */
		@Override
		public Integer call() throws Exception {
			try {
				KagerowSettingContext ctx = KagerowUtilities.getContext(KagerowSettingContext._NAME);
				if (Objects.isNull(namespace)) {
					NamingEnumeration<Binding> list = ctx.listBindings((Name) null);
					while (list.hasMore()) {
						String namespace = list.next().getName();
						System.out.println();
						System.out.println(namespace);
						System.out.println("────────────────────────────────────────────────");
						KagerowSettingContent ctn = ctx.lookup(namespace);
						for (String key : ctn.settingKeySet()) {
							System.out.printf("%-20s %s%n", key, ctn.lookup(key));
						}
					}
				} else {
					if (ctx.isExist(namespace)) {
						System.out.println();
						System.out.println(namespace);
						System.out.println("────────────────────────────────────────────────");
						KagerowSettingContent ctn = ctx.lookup(namespace);
						for (String key : ctn.settingKeySet()) {
							System.out.printf("%-20s %s%n", key, ctn.lookup(key));
						}
					} else {
						System.err.println("The specified namespace does not exist");
						return Integer.valueOf(2);
					}
				}
				System.out.println();
			} catch (Exception e) {
				KagerowLogger.newAppLogger().err(e);
				return Integer.valueOf(1);
			}
			return Integer.valueOf(0);
		}

	}

	/**
	 * 設定確認コマンド
	 */
	@Command(name = "get", mixinStandardHelpOptions = true)
	public static class SettingGetCommand implements Callable<Integer> {

		/** 名前空間 */
		@Parameters(index = "0")
		private String namespace;
		/** 設定項目 */
		@Parameters(index = "1")
		private String key;

		/** {@inheritDoc} */
		@Override
		public Integer call() throws Exception {
			try {
				KagerowSettingContext ctx = KagerowUtilities.getContext(KagerowSettingContext._NAME);
				if (ctx.isExist(namespace)) {
					KagerowSettingContent ctn = ctx.lookup(namespace);
					String value = ctn.lookup(key);
					if (Objects.nonNull(value)) {
						System.out.printf("%-20s %s%n", key, value);
					} else {
						System.err.println("The specified setting does not exist");
						return Integer.valueOf(3);
					}
				} else {
					System.err.println("The specified namespace does not exist");
					return Integer.valueOf(2);
				}
			} catch (Exception e) {
				KagerowLogger.newAppLogger().err(e);
				return Integer.valueOf(1);
			}
			return Integer.valueOf(0);
		}

	}

	/**
	 * 設定追加・変更コマンド
	 */
	@Command(name = "set", mixinStandardHelpOptions = true)
	public static class SettingSetCommand implements Callable<Integer> {

		/** 名前空間 */
		@Parameters(index = "0")
		private String namespace;
		/** 設定項目 */
		@Parameters(index = "1")
		private String key;
		/** 設定値 */
		@Parameters(index = "2")
		private String value;

		/** {@inheritDoc} */
		@Override
		public Integer call() throws Exception {
			try {
				KagerowSettingContext ctx = KagerowUtilities.getContext(KagerowSettingContext._NAME);
				if (ctx.isExist(namespace)) {
					KagerowSettingContent ctn = ctx.lookup(namespace);
					ctn.bind(key, value);
				} else {
					KagerowSettingContent ctn = ctx.createSubcontext(namespace);
					ctn.bind(key, value);
				}
			} catch (Exception e) {
				KagerowLogger.newAppLogger().err(e);
				return Integer.valueOf(1);
			}
			return Integer.valueOf(0);
		}

	}

}

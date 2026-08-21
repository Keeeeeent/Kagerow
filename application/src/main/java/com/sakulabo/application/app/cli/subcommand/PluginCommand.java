package com.sakulabo.application.app.cli.subcommand;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;

import javax.naming.Binding;
import javax.naming.Name;
import javax.naming.NamingEnumeration;

import com.sakulabo.core.Kagerow.Contents.KagerowPluginContent;
import com.sakulabo.core.Kagerow.Contents.KagerowPluginContent.PluginInfo;
import com.sakulabo.core.Kagerow.Contents.KagerowPluginContent.PluginParamInfo;
import com.sakulabo.core.Kagerow.Context.KagerowPluginContext;
import com.sakulabo.core.Kagerow.Context.KagerowPluginPackageContext;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;
import com.sakulabo.core.Kagerow.Utilities.KagerowUtilities;
import com.sakulabo.regulation.annotation.KagerowPlugin.PluginType;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * プラグイン管理機能実装クラスです
 *
 * @author keeeeeent
 */
@Command(name = "plugin", mixinStandardHelpOptions = true, subcommands = {
		PluginCommand.PluginListCommand.class,
		PluginCommand.PluginInfoCommand.class,
		PluginCommand.PluginDisableCommand.class,
		PluginCommand.PluginEnableCommand.class
})
public class PluginCommand {

	/**
	 * プラグイン一覧確認コマンド
	 */
	@Command(name = "list", mixinStandardHelpOptions = true)
	public static class PluginListCommand implements Callable<Integer> {

		/** {@inheritDoc} */
		@Override
		public Integer call() throws Exception {
			try {
				KagerowPluginPackageContext ctx = KagerowUtilities.getContext(KagerowPluginPackageContext._NAME);
				NamingEnumeration<Binding> pluginPkgs = ctx.listBindings((Name) null);
				while (pluginPkgs.hasMore()) {
					String pluginPkgName = pluginPkgs.next().getName();
					KagerowPluginContext plugins = ctx.lookup(pluginPkgName);
					System.out.println();
					System.out.println(plugins);
					System.out.println("────────────────────────────────────────────────");
					NamingEnumeration<Binding> pluginInfo = plugins.listBindings((Name) null);
					info: while (pluginInfo.hasMore()) {
						String key = pluginInfo.next().getName();
						if (plugins.isDisable(key)) {
							break info;
						}
						System.out.printf("%-20s %n", key);
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
	 * プラグイン確認コマンド
	 */
	@Command(name = "info", mixinStandardHelpOptions = true)
	public static class PluginInfoCommand implements Callable<Integer> {

		/**　パッケージ名称 */
		@Option(names = "--pkg")
		private String packageName = "default";
		/**　パッケージ名称 */
		@Option(names = "--ver")
		private String version;
		/**　プラグイン名称 */
		@Option(names = "--name", required = true)
		private String pluginName;

		/** {@inheritDoc} */
		@Override
		public Integer call() throws Exception {
			try {
				Name pkgName = KagerowUtilities.createVersioningPluginPkgName(packageName, version);
				KagerowPluginContent content = KagerowUtilities.getPlugin(pkgName, pluginName);
				KagerowPluginPackageContext ctx = KagerowUtilities.getContext(KagerowPluginPackageContext._NAME);
				KagerowPluginContext plugins = ctx.lookup(packageName);
				PluginInfo info = content.toPluginInfo();
				System.out.println();
				System.out.println("Plugin Information");
				System.out.println("──────────────────────────────────────────────────────────────");
				System.out.printf("Package      : %s%n", packageName);
				System.out.printf("Name         : %s%n", pluginName);
				System.out.printf("Version      : %s%n",
						Objects.isNull(version) ? plugins.toString().split("@", 2)[1] : version);
				if (plugins.isDisable(pluginName) || ctx.isDisable(packageName)) {
					System.out.println("Status       : Disable");
				} else {
					System.out.println("Status       : Enable");
				}
				Map<PluginType, List<PluginParamInfo>> params = info.param();
				List<PluginParamInfo> inputParam = params.get(PluginType.INPUT);
				List<PluginParamInfo> outputParam = params.get(PluginType.OUTPUT);
				print(inputParam, "Input Parameter");
				print(outputParam, "Output Parameter");
				System.out.println();
			} catch (Exception e) {
				KagerowLogger.newAppLogger().err(e);
				return Integer.valueOf(1);
			}
			return Integer.valueOf(0);
		}

		/**
		 * プラグインパラメータ情報一覧を表示します
		 * @param info パラメータ情報リスト
		 * @param title タイトル
		 */
		private void print(List<PluginParamInfo> info, String title) {
			if (!info.isEmpty()) {
				System.out.println();
				System.out.println(title);
				printHeader();
				for (PluginParamInfo param : info) {
					printBody(param);
				}
			}
		}

		/**
		 * ヘッダーを出力します
		 */
		private void printHeader() {
			System.out.println("──────────────────────────────────────────────────────────────");
			System.out.printf("%-25s %-17s %-17s %n",
					"Name",
					"Default",
					"Required");
			System.out.println("──────────────────────────────────────────────────────────────");
		}

		/**
		 * パラメータ情報を表示します
		 * @param param パラメータ情報
		 */
		private void printBody(PluginParamInfo param) {
			System.out.printf("%-25s %-17s %-17s %n",
					param.name(),
					fromDefault(param.defaultValue()),
					fromRequired(param.required()));
		}

		/**
		 * 必須フラグを表示形式に変換します
		 * @param required 必須フラグ
		 * @return 変換後文字列
		 */
		private String fromRequired(boolean required) {
			return required ? "Yes" : "No";
		}

		/**
		 * デフォルト値を表示形式に変換します
		 * @param defaultValue デフォルト値
		 * @return 変換後文字列
		 */
		private String fromDefault(String defaultValue) {
			return Objects.isNull(defaultValue) || defaultValue.isEmpty() ? "-" : defaultValue;
		}

	}

	/**
	 * プラグイン無効化コマンド
	 */
	@Command(name = "disable", mixinStandardHelpOptions = true)
	public static class PluginDisableCommand implements Callable<Integer> {

		/**　パッケージ名称 */
		@Option(names = "--pkg", required = true)
		private String packageName;
		/**　パッケージ名称 */
		@Option(names = "--ver")
		private String version;
		/**　プラグイン名称 */
		@Option(names = "--name")
		private String pluginName;

		/** {@inheritDoc} */
		@Override
		public Integer call() throws Exception {
			try {
				if ("default".equals(packageName)) {
					System.err.println("The default plugin cannot be disabled");
					return Integer.valueOf(4);
				}
				KagerowPluginPackageContext ctx = KagerowUtilities.getContext(KagerowPluginPackageContext._NAME);
				if (ctx.isDisable(packageName)) {
					System.err.println("The target package has already been disabled");
					return Integer.valueOf(2);
				}
				if (Objects.isNull(pluginName)) {
					ctx.setDisable(true, packageName);
				} else {
					KagerowPluginContext plugins = ctx.lookup(packageName);
					if (plugins.isDisable(pluginName)) {
						System.err.println("The plugin has already been disabled");
						return Integer.valueOf(3);
					}
					plugins.setDisable(true, pluginName);
				}
			} catch (Exception e) {
				KagerowLogger.newAppLogger().err(e);
				return Integer.valueOf(1);
			}
			return Integer.valueOf(0);
		}

	}

	/**
	 * プラグイン有効化コマンド
	 */
	@Command(name = "enable", mixinStandardHelpOptions = true)
	public static class PluginEnableCommand implements Callable<Integer> {

		/**　パッケージ名称 */
		@Option(names = "--pkg", required = true)
		private String packageName;
		/**　パッケージ名称 */
		@Option(names = "--ver")
		private String version;
		/**　プラグイン名称 */
		@Option(names = "--name")
		private String pluginName;

		/** {@inheritDoc} */
		@Override
		public Integer call() throws Exception {
			try {
				if ("default".equals(packageName)) {
					return Integer.valueOf(0);
				}
				KagerowPluginPackageContext ctx = KagerowUtilities.getContext(KagerowPluginPackageContext._NAME);
				if (!ctx.isDisable(packageName)) {
					System.err.println("The target package has already been enable");
					return Integer.valueOf(2);
				}
				if (Objects.isNull(pluginName)) {
					ctx.setDisable(false, packageName);
				} else {
					KagerowPluginContext plugins = ctx.lookup(packageName);
					if (!plugins.isDisable(pluginName)) {
						System.err.println("The plugin has already been enable");
						return Integer.valueOf(3);
					}
					plugins.setDisable(false, pluginName);
				}
			} catch (Exception e) {
				KagerowLogger.newAppLogger().err(e);
				return Integer.valueOf(1);
			}
			return Integer.valueOf(0);
		}

	}

}

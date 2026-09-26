package com.sakulabo.application.app.cli.subcommand;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;

import javax.naming.Binding;
import javax.naming.Name;
import javax.naming.NamingEnumeration;

import com.sakulabo.application.app.cli.subcommand.RemoteCommand.RpcResult.Fail;
import com.sakulabo.application.app.cli.subcommand.RemoteCommand.RpcResult.Success;
import com.sakulabo.application.app.rpc.datatype.receive.ArrayReceiveDataType;
import com.sakulabo.application.app.rpc.datatype.send.StringSendDataType;
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
@Command(name = "plugin", subcommands = {
		PluginCommand.PluginListCommand.class,
		PluginCommand.PluginInfoCommand.class,
		PluginCommand.PluginDisableCommand.class,
		PluginCommand.PluginEnableCommand.class
})
public class PluginCommand {

	/**
	 * プラグイン一覧確認コマンド
	 */
	@Command(name = "list")
	public static class PluginListCommand extends AuthRemoteCommand {

		/** {@inheritDoc} */
		@Override
		protected Integer local() throws Exception {
			try {
				KagerowPluginPackageContext ctx = KagerowUtilities.getContext(KagerowPluginPackageContext._NAME);
				NamingEnumeration<Binding> pluginPkgs = ctx.listBindings((Name) null);
				Map<String, List<String>> pluginList = new HashMap<>();
				while (pluginPkgs.hasMore()) {
					String pluginPkgName = pluginPkgs.next().getName();
					KagerowPluginContext plugins = ctx.lookup(pluginPkgName);
					List<String> pluginNames = new ArrayList<>();
					NamingEnumeration<Binding> pluginInfo = plugins.listBindings((Name) null);
					info: while (pluginInfo.hasMore()) {
						String key = pluginInfo.next().getName();
						if (plugins.isDisable(key)) {
							break info;
						}
						pluginNames.add(key);
					}
					pluginList.put(plugins.toString(), pluginNames);
				}
				print(pluginList);
			} catch (Exception e) {
				KagerowLogger.newAppLogger().err(e);
				return Integer.valueOf(1);
			}
			return Integer.valueOf(0);
		}

		/** {@inheritDoc} */
		@Override
		protected Integer remote() throws Exception {
			// メソッド呼び出し
			RpcResult result = doRpcMethodCall("pkgs", "/rpc/plugin", HashMap::new);
			// 結果処理
			return switch (result) {
				case Success success: {
					ArrayReceiveDataType list = new ArrayReceiveDataType(success.response().get("list"));
					Optional<List<String>> resultList = list.getRawType();
					Map<String, List<String>> pluginList = new HashMap<>();
					if (resultList.isPresent()) {
						for (String key : resultList.get()) {
							RpcResult subResult = doRpcMethodCall("list", "/rpc/plugin", () -> {
								return new HashMap<>() {
									{
										put("pkgName", new StringSendDataType(key));
									}
								};
							});
							if (subResult instanceof Success successResult) {
								ArrayReceiveDataType subList = new ArrayReceiveDataType(
										successResult.response().get("list"));
								List<String> value = subList.getRawType().orElse(Collections.emptyList());
								pluginList.put(key, value);
							}
						}
					}
					print(pluginList);
					yield Integer.valueOf(0);
				}
				case Fail fail: {
					System.err
							.println(String.format("StatusCode : %d ResponseText", fail.statusCode(), fail.response()));
					yield Integer.valueOf(1);
				}
			};
		}

		private void print(Map<String, List<String>> plugins) {
			if (plugins.isEmpty()) {
				return;
			}
			for (Entry<String, List<String>> entry : plugins.entrySet()) {
				System.out.println();
				System.out.println(entry.getKey());
				System.out.println("────────────────────────────────────────────────");
				entry.getValue().forEach(key -> System.out.printf("%-20s %n", key));
			}
			System.out.println();
		}

	}

	/**
	 * プラグイン確認コマンド
	 */
	@Command(name = "info")
	public static class PluginInfoCommand extends AuthRemoteCommand {

		/** パッケージ名称 */
		@Option(names = "--pkg")
		private String packageName = "default";
		/** パッケージ名称 */
		@Option(names = "--ver")
		private String version;
		/** プラグイン名称 */
		@Option(names = "--name", required = true)
		private String pluginName;

		/** {@inheritDoc} */
		@Override
		protected Integer local() throws Exception {
			try {
				Name pkgName = KagerowUtilities.createVersioningPluginPkgName(packageName, version);
				KagerowPluginContent content = KagerowUtilities.getPlugin(pkgName, pluginName);
				KagerowPluginPackageContext ctx = KagerowUtilities.getContext(KagerowPluginPackageContext._NAME);
				KagerowPluginContext plugins = ctx.lookup(packageName);
				PluginInfo info = content.toPluginInfo();
				String version = Objects.isNull(this.version) ? plugins.toString().split("@", 2)[1] : this.version;
				boolean status = false;
				if (plugins.isDisable(pluginName) || ctx.isDisable(packageName)) {
					status = true;
				}
				Map<PluginType, List<PluginParamInfo>> params = info.param();
				List<PluginParamInfo> inputParam = params.get(PluginType.INPUT);
				List<PluginParamInfo> outputParam = params.get(PluginType.OUTPUT);
				printInfo(
						packageName,
						pluginName,
						version,
						status,
						inputParam,
						outputParam);
			} catch (Exception e) {
				KagerowLogger.newAppLogger().err(e);
				return Integer.valueOf(1);
			}
			return Integer.valueOf(0);
		}

		/** {@inheritDoc} */
		@Override
		protected Integer remote() throws Exception {
			// メソッド呼び出し
			RpcResult result = doRpcMethodCall("info", "/rpc/plugin", () -> {
				return new HashMap<>() {
					{
						put("packageName", new StringSendDataType(packageName));
						put("version", new StringSendDataType(version));
						put("pluginName", new StringSendDataType(pluginName));
					}
				};
			});
			// 結果処理
			return switch (result) {
				case Success success: {
					Map<String, String> response = success.response();
					String packageName = response.get("packageName");
					String pluginName = response.get("pluginName");
					String version = response.get("version");
					boolean status = Boolean.valueOf(response.get("status"));
					List<PluginParamInfo> inputParam = new ArrayList<>();
					List<PluginParamInfo> outputParam = new ArrayList<>();
					ArrayReceiveDataType inputParamType = new ArrayReceiveDataType(response.get("inputParam"));
					if (inputParamType.getRawType().isPresent()) {
						List<String> params = inputParamType.getRawType().get();
						PluginParamInfo item = new PluginParamInfo(
								params.get(0),
								params.get(1),
								Boolean.valueOf(params.get(2)));
						inputParam.add(item);
					}
					ArrayReceiveDataType outputParamType = new ArrayReceiveDataType(response.get("outputParam"));
					if (outputParamType.getRawType().isPresent()) {
						List<String> params = outputParamType.getRawType().get();
						PluginParamInfo item = new PluginParamInfo(
								params.get(0),
								params.get(1),
								Boolean.valueOf(params.get(2)));
						inputParam.add(item);
					}
					printInfo(
							packageName,
							pluginName,
							version,
							status,
							inputParam,
							outputParam);
					yield Integer.valueOf(0);
				}
				case Fail fail: {
					System.err
							.println(String.format("StatusCode : %d ResponseText", fail.statusCode(), fail.response()));
					yield Integer.valueOf(1);
				}
			};
		}

		/**
		 * プラグインの情報を出力します
		 * 
		 * @param packageName プラグインパッケージ名称
		 * @param pluginName  プラグイン名称
		 * @param version     プラグインバージョン
		 * @param status      プラグインステータス
		 * @param inputParam  プラグイン入力パラメータ
		 * @param outputParam プラグイン出漁パラメータ
		 */
		private void printInfo(String packageName, String pluginName, String version, boolean status,
				List<PluginParamInfo> inputParam, List<PluginParamInfo> outputParam) {
			System.out.println();
			System.out.println("Plugin Information");
			System.out.println("──────────────────────────────────────────────────────────────");
			System.out.printf("Package      : %s%n", packageName);
			System.out.printf("Name         : %s%n", pluginName);
			System.out.printf("Version      : %s%n", version);
			System.out.printf("Status       : %s%n", status ? "Disable" : "Enable");
			print(inputParam, "Input Parameter");
			print(outputParam, "Output Parameter");
			System.out.println();
		}

		/**
		 * プラグインパラメータ情報一覧を表示します
		 * 
		 * @param info  パラメータ情報リスト
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
		 * 
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
		 * 
		 * @param required 必須フラグ
		 * @return 変換後文字列
		 */
		private String fromRequired(boolean required) {
			return required ? "Yes" : "No";
		}

		/**
		 * デフォルト値を表示形式に変換します
		 * 
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
	@Command(name = "disable")
	public static class PluginDisableCommand extends AuthRemoteCommand {

		/** パッケージ名称 */
		@Option(names = "--pkg", required = true)
		private String packageName;
		/** パッケージ名称 */
		@Option(names = "--ver")
		private String version;
		/** プラグイン名称 */
		@Option(names = "--name")
		private String pluginName;

		/** {@inheritDoc} */
		@Override
		protected Integer local() throws Exception {
			try {
				if ("default".equals(packageName)) {
					System.err.println("The default plugin cannot be disabled");
					return Integer.valueOf(4);
				}
				KagerowPluginPackageContext ctx = KagerowUtilities.getContext(KagerowPluginPackageContext._NAME);
				Name pkgName = KagerowUtilities.createVersioningPluginPkgName(packageName, version);
				if (Objects.isNull(pluginName)) {
					if (ctx.isDisable(pkgName.toString())) {
						System.err.println("The target package has already been disabled");
						return Integer.valueOf(2);
					}
					ctx.setDisable(true, pkgName.toString());
				} else {
					KagerowPluginContext plugins = ctx.lookup(pkgName);
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

		/** {@inheritDoc} */
		@Override
		protected Integer remote() throws Exception {
			// メソッド呼び出し
			RpcResult result = doRpcMethodCall("disable", "/rpc/plugin", () -> {
				return new HashMap<>() {
					{
						put("packageName", new StringSendDataType(packageName));
						if (Objects.nonNull(version)) {
							put("version", new StringSendDataType(version));
						}
						if (Objects.nonNull(pluginName)) {
							put("pluginName", new StringSendDataType(pluginName));
						}
					}
				};
			});
			// 結果処理
			return switch (result) {
				case Success _: {
					yield Integer.valueOf(0);
				}
				case Fail fail: {
					Integer exitCode = Integer.valueOf(fail.response().getOrDefault("exitCode", "1"));
					System.err
							.println(String.format("StatusCode : %d ResponseText", fail.statusCode(), fail.response()));
					yield exitCode;
				}
			};
		}

	}

	/**
	 * プラグイン有効化コマンド
	 */
	@Command(name = "enable")
	public static class PluginEnableCommand extends AuthRemoteCommand {

		/** パッケージ名称 */
		@Option(names = "--pkg", required = true)
		private String packageName;
		/** パッケージ名称 */
		@Option(names = "--ver")
		private String version;
		/** プラグイン名称 */
		@Option(names = "--name")
		private String pluginName;

		/** {@inheritDoc} */
		@Override
		protected Integer local() throws Exception {
			try {
				if ("default".equals(packageName)) {
					return Integer.valueOf(0);
				}
				KagerowPluginPackageContext ctx = KagerowUtilities.getContext(KagerowPluginPackageContext._NAME);
				Name pkgName = KagerowUtilities.createVersioningPluginPkgName(packageName, version);
				if (Objects.isNull(pluginName)) {
					if (!ctx.isDisable(pkgName.toString())) {
						System.err.println("The target package has already been enable");
						return Integer.valueOf(2);
					}
					ctx.setDisable(false, pkgName.toString());
				} else {
					KagerowPluginContext plugins = ctx.lookup(pkgName);
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

		/** {@inheritDoc} */
		@Override
		protected Integer remote() throws Exception {
			// メソッド呼び出し
			RpcResult result = doRpcMethodCall("enable", "/rpc/plugin", () -> {
				return new HashMap<>() {
					{
						put("packageName", new StringSendDataType(packageName));
						if (Objects.nonNull(version)) {
							put("version", new StringSendDataType(version));
						}
						if (Objects.nonNull(pluginName)) {
							put("pluginName", new StringSendDataType(pluginName));
						}
					}
				};
			});
			// 結果処理
			return switch (result) {
				case Success _: {
					yield Integer.valueOf(0);
				}
				case Fail fail: {
					Integer exitCode = Integer.valueOf(1);
					if (fail.statusCode() == HttpURLConnection.HTTP_NOT_MODIFIED) {
						exitCode = Integer.valueOf(fail.response().getOrDefault("exitCode", "1"));
					}
					System.err
							.println(String.format("StatusCode : %d ResponseText", fail.statusCode(), fail.response()));
					yield exitCode;
				}
			};
		}

	}

}

package com.sakulabo.application.app.cli.subcommand;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.naming.Binding;
import javax.naming.Name;
import javax.naming.NamingEnumeration;

import com.sakulabo.application.app.cli.subcommand.RemoteCommand.RpcResult.Fail;
import com.sakulabo.application.app.cli.subcommand.RemoteCommand.RpcResult.Success;
import com.sakulabo.application.app.rpc.datatype.receive.ArrayReceiveDataType;
import com.sakulabo.application.app.rpc.datatype.receive.StringReceiveDataType;
import com.sakulabo.application.app.rpc.datatype.send.StringSendDataType;
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
	public static class CacheListCommand extends AuthRemoteCommand {

		/** {@inheritDoc} */
		@Override
		protected Integer local() throws Exception {
			try {
				KagerowCacheContext ctx = KagerowUtilities.getContext(KagerowCacheContext._NAME);
				NamingEnumeration<Binding> list = ctx.listBindings((Name) null);
				List<String> printList = new ArrayList<>();
				while (list.hasMore()) {
					String info = list.next().getName();
					printList.add(info);
				}
				print(printList);
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
			RpcResult result = doRpcMethodCall("list", "/rpc/cache", HashMap::new);
			// 結果処理
			return switch (result) {
				case Success success: {
					ArrayReceiveDataType list = new ArrayReceiveDataType(success.response().get("list"));
					List<String> printList = list.getRawType().orElse(Collections.emptyList());
					print(printList);
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
		 * キャッシュリストを表示します
		 * 
		 * @param list キャッシュリスト
		 */
		private void print(List<String> list) {
			if (list.isEmpty()) {
				return;
			}
			System.out.println();
			System.out.println("Cache Information");
			System.out.println("──────────────────────────────────────────────────────────────");
			for (String info : list) {
				System.out.printf("%-20s %n", info);
			}
		}

	}

	/**
	 * キャッシュ情報詳細表示コマンド
	 */
	@Command(name = "info")
	public static class CacheInfoCommand extends AuthRemoteCommand {

		/** キャッシュID */
		@Option(names = { "--id", "-i" }, required = true)
		private String cacheId;

		/** {@inheritDoc} */
		@Override
		protected Integer local() throws Exception {
			try {
				KagerowCacheContext ctx = KagerowUtilities.getContext(KagerowCacheContext._NAME);
				KagerowCacheContent cnt = ctx.lookup(cacheId);
				NamingEnumeration<Binding> list = cnt.listBindings((Name) null);
				List<String> printList = new ArrayList<>();
				while (list.hasMore()) {
					String info = list.next().getName();
					printList.add(info);
				}
				print(printList);
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
			RpcResult result = doRpcMethodCall("info", "/rpc/cache", () -> {
				return new HashMap<>() {
					{
						put("cacheId", new StringSendDataType(cacheId));
					}
				};
			});
			// 結果処理
			return switch (result) {
				case Success success: {
					ArrayReceiveDataType list = new ArrayReceiveDataType(success.response().get("list"));
					List<String> printList = list.getRawType().orElse(Collections.emptyList());
					print(printList);
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
		 * キャッシュファイルリストを表示します
		 * 
		 * @param list キャッシュファイルリスト
		 */
		private void print(List<String> list) {
			if (list.isEmpty()) {
				return;
			}
			System.out.println();
			System.out.println("Cache Details Information");
			System.out.println("──────────────────────────────────────────────────────────────");
			for (String info : list) {
				System.out.printf("%-20s %n", info);
			}
		}

	}

	/**
	 * キャッシュ生成コマンド
	 */
	@Command(name = "create")
	public static class CacheCreateCommand extends AuthRemoteCommand {

		/** {@inheritDoc} */
		@Override
		protected Integer local() throws Exception {
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

		/** {@inheritDoc} */
		@Override
		protected Integer remote() throws Exception {
			// メソッド呼び出し
			RpcResult result = doRpcMethodCall("create", "/rpc/cache", HashMap::new);
			// 結果処理
			return switch (result) {
				case Success success: {
					StringReceiveDataType list = new StringReceiveDataType(success.response().get("cacheId"));
					String cacheId = list.getRawType().get();
					System.out.println(cacheId);
					yield Integer.valueOf(0);
				}
				case Fail fail: {
					System.err
							.println(String.format("StatusCode : %d ResponseText", fail.statusCode(), fail.response()));
					yield Integer.valueOf(1);
				}
			};
		}

	}

	/**
	 * キャッシュ削除コマンド
	 */
	@Command(name = "delete")
	public static class CacheDeleteCommand extends AuthRemoteCommand {

		/** キャッシュID */
		@Option(names = { "--id", "-i" }, required = true)
		private String cacheId;

		/** {@inheritDoc} */
		@Override
		protected Integer local() throws Exception {
			try {
				KagerowCacheContext ctx = KagerowUtilities.getContext(KagerowCacheContext._NAME);
				ctx.destroySubcontext(cacheId);
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
			RpcResult result = doRpcMethodCall("delete", "/rpc/cache", () -> {
				return new HashMap<>() {
					{
						put("cacheId", new StringSendDataType(cacheId));
					}
				};
			});
			// 結果処理
			return switch (result) {
				case Success _: {
					yield Integer.valueOf(0);
				}
				case Fail fail: {
					System.err
							.println(String.format("StatusCode : %d ResponseText", fail.statusCode(), fail.response()));
					yield Integer.valueOf(1);
				}
			};
		}

	}

}

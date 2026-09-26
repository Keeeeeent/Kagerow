package com.sakulabo.application.app.cli.subcommand;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.sakulabo.application.app.cli.subcommand.RemoteCommand.RpcResult.Fail;
import com.sakulabo.application.app.cli.subcommand.RemoteCommand.RpcResult.Success;
import com.sakulabo.application.app.rpc.datatype.receive.ArrayReceiveDataType;
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
	public static class SynonymListCommand extends AuthRemoteCommand {

		/** スキーマ名称 */
		@Option(names = { "--schema", "-s" }, required = true)
		private String schemaName;

		/** {@inheritDoc} */
		@Override
		protected Integer local() throws Exception {
			try {
				KagerowVirtualFileContext ctx = KagerowUtilities.getContext(KagerowVirtualFileContext._NAME);
				KagerowVirtualDirContext dirCtx = ctx.lookup(schemaName);
				Map<String, String> synonyms = dirCtx.getSynonymMapList();
				print(synonyms);
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
			RpcResult result = doRpcMethodCall("list", "/rpc/synonym", HashMap::new);
			// 結果処理
			return switch (result) {
				case Success success: {
					ArrayReceiveDataType list = new ArrayReceiveDataType(success.response().get("list"));
					Optional<List<String>> synonymList = list.getRawType();
					Map<String, String> synonyms = new HashMap<>();
					synonymList.ifPresent(li -> {
						li.stream().forEach(target -> {
							String[] data = target.split(",", -1);
							synonyms.put(data[0], data[1]);
						});
					});
					print(synonyms);
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
		 * シノニム一覧リストを表示します
		 * 
		 * @param synonyms シノニム情報
		 */
		private void print(Map<String, String> synonyms) {
			if (0 < synonyms.size()) {
				System.out.printf("%-17s %s%n", "Logical Name", "Physical Name");
				System.out.println("────────────────────────────────────────────────");
			}
			for (Map.Entry<String, String> entry : synonyms.entrySet()) {
				System.out.printf("%-17s %s%n", entry.getKey(), entry.getValue());
			}
		}

	}

}

package com.sakulabo.application.app.cli.subcommand;

import java.util.Optional;
import java.util.concurrent.Callable;

import com.sakulabo.application.service.Rpc.AuthService;
import com.sakulabo.core.Kagerow.Utilities.KagerowUtilities;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * 実装クラスです
 *
 * @author keeeeeent
 */
@Command(name = "agent", subcommands = {
		AgentCommand.CreateAgentCommand.class
})
public class AgentCommand {

	/**
	 * ユーザ作成コマンド
	 */
	@Command(name = "user")
	public static class CreateAgentCommand implements Callable<Integer> {

		/** ユーザ名称 */
		@Option(names = "--name", required = true)
		private String username;

		/** {@inheritDoc} */
		@Override
		public Integer call() throws Exception {
			// サービス取得
			AuthService service = KagerowUtilities.getBean(AuthService.class, null).get();
			// ユーザ作成
			Optional<String> secret = service.regist(username);
			if (secret.isPresent()) {
				// 作成に成功した場合、秘密鍵を出力
				System.out.println(secret.get());
				return Integer.valueOf(0);
			} else {
				// 失敗ログ
				System.err.println("User creation failed");
				return Integer.valueOf(1);
			}
		}

	}
}

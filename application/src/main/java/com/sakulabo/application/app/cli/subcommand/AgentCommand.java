package com.sakulabo.application.app.cli.subcommand;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.sakulabo.application.app.cli.converter.ExistingFilePathConverter;
import com.sakulabo.application.app.cli.converter.ExistingParentDirConverter;
import com.sakulabo.application.app.cli.converter.RpcUriConverter;
import com.sakulabo.application.app.cli.subcommand.RemoteCommand.RpcResult.Fail;
import com.sakulabo.application.app.cli.subcommand.RemoteCommand.RpcResult.Success;
import com.sakulabo.application.app.rpc.RpcServer;
import com.sakulabo.application.app.rpc.datatype.send.Base64SendDataType;
import com.sakulabo.application.app.rpc.datatype.send.StringSendDataType;
import com.sakulabo.application.service.Rpc.AuthService;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;
import com.sakulabo.core.Kagerow.Utilities.KagerowUtilities;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import picocli.CommandLine.Command;
import picocli.CommandLine.ITypeConverter;
import picocli.CommandLine.Option;
import picocli.CommandLine.TypeConversionException;

/**
 * エージェント機能実装クラスです
 *
 * @author keeeeeent
 */
@Command(name = "agent", subcommands = {
		AgentCommand.CreateAgentCommand.class,
		AgentCommand.LoginAgentCommand.class,
		AgentCommand.StartAgentCommand.class,
		AgentCommand.CertificateAgentCommand.class,
})
public class AgentCommand {

	/**
	 * ユーザ作成コマンド
	 */
	@Command(name = "create")
	public static class CreateAgentCommand implements Callable<Integer> {

		/** ユーザ名称 */
		@Option(names = "--name", description = "Create a new authentication user with a user name and secret key", required = true)
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

	/**
	 * ログインコマンド
	 */
	@Command(name = "login")
	public static class LoginAgentCommand extends RemoteCommand {

		/** ユーザ名称 */
		@Option(names = "--name", description = "Please specify the user name for authentication", required = true)
		private String username;
		/** 秘密鍵 */
		@Option(names = "--secret", description = "Please specify the secret key for authentication in Base64 format", required = true)
		private String secretkey;
		/** リモート実行 */
		@SuppressFBWarnings("MF_CLASS_MASKS_FIELD")
		@Option(names = "--remote", description = "Please specify the URI in the format rpc://host:port", converter = RpcUriConverter.class, required = true)
		public URI remote = null;
		/** 証明書 */
		@SuppressFBWarnings("MF_CLASS_MASKS_FIELD")
		@Option(names = "--cacert", description = "Please specify the path to the X.509 certificate in PEM format", converter = ExistingFilePathConverter.class)
		public Path cacert = null;

		/** {@inheritDoc} */
		@Override
		protected Integer local() throws Exception {
			if (Objects.isNull(remote)) {
				return Integer.valueOf(1);
			}
			return remote();
		}

		/** {@inheritDoc} */
		@Override
		protected Integer remote() throws Exception {

			// フィールド上書き
			super.remote = this.remote;
			super.cacert = this.cacert;

			// 変数宣言
			RpcResult result = null;

			// メソッド呼び出し(認証取得)
			RpcResult loginResult = doRpcMethodCall("nonce", "/rpc/auth", () -> {
				return new HashMap<>() {
					{
						put("user", new StringSendDataType(username));
					}
				};
			}, false);

			// 呼び出し結果検証
			if (loginResult instanceof Fail fail) {
				// ログイン失敗の場合後続処理は行わない
				result = fail;
			} else if (loginResult instanceof Success success) {
				// チャレンジ開始
				String nonce = success.response().get("nonce");
				byte[] rawNonce = Base64.getDecoder().decode(nonce.getBytes(StandardCharsets.UTF_8));
				// 秘密鍵を生成
				byte[] keyByte = Base64.getDecoder().decode(secretkey.getBytes(StandardCharsets.UTF_8));
				SecretKeySpec key = new SecretKeySpec(keyByte, "AES");
				Mac mac = Mac.getInstance("HmacSHA256");
				mac.init(key);
				// チャレンジ実施
				byte[] challenge = mac.doFinal(rawNonce);
				// メソッド呼び出し(チャレンジ認証)
				RpcResult challengeResult = doRpcMethodCall("challenge", "/rpc/auth",
						() -> {
							return new HashMap<>() {
								{
									put("challenge", new Base64SendDataType(challenge));
								}
							};
						}, false);
				// 結果設定
				result = challengeResult;
			}

			// 結果処理
			return switch (result) {
				case Success success: {
					String token = success.response().get("token");
					System.out.println(token);
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
	 * RPCサーバ起動コマンド
	 */
	@Command(name = "start")
	public static class StartAgentCommand implements Callable<Integer> {

		/** ホスト名称 */
		@Option(names = { "--host", "-h" })
		private String host;
		/** ポート番号 */
		@Option(names = { "--port", "-p" })
		private Integer port;

		/** {@inheritDoc} */
		@Override
		public Integer call() throws Exception {
			try {
				// サーバインスタンス生成
				RpcServer server = new RpcServer(host, port);
				// サーバ起動
				server.start();
				return Integer.valueOf(-1);
			} catch (Exception e) {
				// 失敗ログ
				KagerowLogger.newAppLogger().err(e);
				if (e instanceof IllegalStateException) {
					System.err.println(e.getMessage());
				} else {
					System.err.println("The server failed to start");
				}
				return Integer.valueOf(1);
			}
		}

	}

	/**
	 * ユーザ作成コマンド
	 */
	@Command(name = "certificate")
	public static class CertificateAgentCommand implements Callable<Integer>, ITypeConverter<String> {

		/** DNS名称リスト */
		@Option(names = { "--dns", "-d" }, description = "DNS names to add to the Subject Alternative Name (SAN)")
		private List<String> dns;
		/** IPリスト */
		@Option(names = { "--ip",
				"-i" }, converter = CertificateAgentCommand.class, description = "IP addresses to add to the Subject Alternative Name (SAN)")
		private List<String> ip;
		/** CN名称 */
		@Option(names = { "--cn", "-c" }, required = true, description = "Common Name (CN) of the certificate")
		private String cn;
		/** 証明書出力先 */
		@Option(names = { "--path",
				"-p" }, converter = ExistingParentDirConverter.class, required = true, description = "Output path for the generated certificate")
		private Path crtPath;

		/** {@inheritDoc} */
		@Override
		public String convert(String value) {
			try {
				InetAddress.getByName(value);
				// 必要ならIPv4/IPv6の制限も可能
				return value;
			} catch (UnknownHostException e) {
				throw new TypeConversionException("Invalid IP address: " + value);
			}
		}

		/** {@inheritDoc} */
		@Override
		public Integer call() throws Exception {
			try {
				RpcServer.createCrtFile(crtPath, cn, dns.toArray(String[]::new), ip.toArray(String[]::new));
				return Integer.valueOf(0);
			} catch (Exception e) {
				// 失敗ログ
				System.err.println("Failed to create the certificate");
				KagerowLogger.newAppLogger().err(e);
				return Integer.valueOf(1);
			}
		}

	}

}

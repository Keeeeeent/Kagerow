package com.sakulabo.application.app.cli.subcommand;

import java.net.URI;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

import com.sakulabo.application.app.cli.converter.ExistingFilePathConverter;
import com.sakulabo.application.app.cli.converter.RpcAuthUriConverter;
import com.sakulabo.application.app.rpc.datatype.BaseDataType;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import picocli.CommandLine.Option;

/**
 * 認証済みリモート実行機能規定クラスです
 *
 * @author keeeeeent
 */
public abstract class AuthRemoteCommand extends RemoteCommand implements Callable<Integer> {

	/** リモート実行 */
	@SuppressFBWarnings("MF_CLASS_MASKS_FIELD")
	@Option(names = "--remote", description = "Please specify the URI in the format rpc://host:port?token=xxxxx", converter = RpcAuthUriConverter.class)
	public URI remote = null;
	/** 証明書 */
	@SuppressFBWarnings("MF_CLASS_MASKS_FIELD")
	@Option(names = "--cacert", description = "Please specify the path to the X.509 certificate in PEM format", converter = ExistingFilePathConverter.class)
	public Path cacert = null;

	/** {@inheritDoc} */
	@Override
	public final Integer call() throws Exception {
		// フィールド上書き
		super.remote = this.remote;
		super.cacert = this.cacert;
		return super.call();
	}

	/**
	 * RPCメソッド呼び出しを行います
	 * このメソッドは常に認証済みリクエストを要求します
	 *
	 * @param methodName RPCメソッドパス
	 * @param rpcPath RPCパス
	 * @param createRequestBody リクエストXML生成関数
	 * @return 呼び出し結果
	 * @throws Exception リクエスト失敗
	 */
	protected final RpcResult doRpcMethodCall(String methodName, String rpcPath,
			Supplier<Map<String, BaseDataType<?>>> createRequestBody)
			throws Exception {
		// メソッド呼び出し
		return doRpcMethodCall(methodName, rpcPath, createRequestBody, true);
	}

}

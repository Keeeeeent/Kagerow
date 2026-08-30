package com.sakulabo.application.app.rpc.executor.Impl;

import java.time.format.DateTimeFormatter;

import com.sakulabo.application.app.rpc.RpcMethod;
import com.sakulabo.application.app.rpc.RpcMethodParam;
import com.sakulabo.application.app.rpc.RpcSetting;
import com.sakulabo.application.app.rpc.datatype.receive.StringReceiveDataType;
import com.sakulabo.application.app.rpc.datatype.send.Base64SendDataType;
import com.sakulabo.application.app.rpc.datatype.send.DateTimeSendDataType;
import com.sakulabo.application.app.rpc.datatype.send.StringSendDataType;
import com.sakulabo.application.app.rpc.exception.IllegalCertificationException;
import com.sakulabo.application.app.rpc.exception.RpcRuntmeException;
import com.sakulabo.application.app.rpc.executor.AuthExecutor;
import com.sakulabo.application.service.Rpc.AuthService;
import com.sakulabo.core.Kagerow.Utilities.KagerowUtilities;

/**
 * チャレンジ認証機能のRPCコントローラー実装クラスです
 *
 * @author keeeeeent
 */
@RpcSetting("auth")
public class AuthExecutorImpl implements AuthExecutor {

	/** 認証サービス取得 */
	private AuthService service = KagerowUtilities.getBean(AuthService.class, null).get();

	/** {@inheritDoc} */
	@Override
	@RpcMethod("nonce")
	public Challenge nonce(@RpcMethodParam(value = "user", required = true) StringReceiveDataType userName) {
		// 引数用意
		String user = userName.getRawType().get();
		// サービス実行
		com.sakulabo.application.service.Rpc.AuthService.Challenge result = service.nonce(user).orElseThrow(() -> {
			return new RpcRuntmeException("Failed to generate a nonce");
		});
		// 返却インスタンス生成
		Base64SendDataType nonce = new Base64SendDataType(result.nonce());
		DateTimeSendDataType expiration = new DateTimeSendDataType(	
				result.expiration().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
		Challenge response = new Challenge(nonce, expiration);
		return response;
	}

	/** {@inheritDoc} */
	@Override
	@RpcMethod("challenge")
	public ChallengeResult challenge(
			@RpcMethodParam(value = "challenge", required = true) Base64SendDataType challenge) {
		// 引数用意
		String challengeData = challenge.getRawType().get();
		// サービス実行
		String result = service.challenge(challengeData).orElseThrow(() -> {
			return new IllegalCertificationException("Challenge authentication failed");
		});
		// 返却インスタンス生成
		StringSendDataType token = new StringSendDataType(result);
		ChallengeResult response = new ChallengeResult(token);
		return response;
	}

}

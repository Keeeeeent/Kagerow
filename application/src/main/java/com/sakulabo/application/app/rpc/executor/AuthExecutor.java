package com.sakulabo.application.app.rpc.executor;

import com.sakulabo.application.app.rpc.RpcSendParam;
import com.sakulabo.application.app.rpc.datatype.receive.Base64ReceiveDataType;
import com.sakulabo.application.app.rpc.datatype.receive.StringReceiveDataType;
import com.sakulabo.application.app.rpc.datatype.send.Base64SendDataType;
import com.sakulabo.application.app.rpc.datatype.send.DateTimeSendDataType;
import com.sakulabo.application.app.rpc.datatype.send.StringSendDataType;
import com.sakulabo.application.common.spi.RpcTarget;

/**
 * チャレンジ認証機能のRPCコントローラー規定インターフェイスです
 *
 * @author keeeeeent
 */
public interface AuthExecutor extends RpcTarget {

	/**
	 * チャレンジデータ構造
	 *
	 * @param nonce      チャレンジ向けノンス
	 * @param expiration 有効期限
	 */
	public static record Challenge(
			@RpcSendParam("nonce") Base64SendDataType nonce,
			@RpcSendParam("expiration") DateTimeSendDataType expiration) {
	}

	/**
	 * チャレンジ結果データ構造
	 *
	 * @param token      アクセストークン
	 */
	public static record ChallengeResult(
			@RpcSendParam("token") StringSendDataType token) {
	}

	/**
	 * チャレンジデータ取得処理を実施します
	 *
	 * @param userName ユーザ名
	 * @return チャレンジデータ
	 */
	Challenge nonce(StringReceiveDataType userName);

	/**
	 * チャレンジデータの検証を行います
	 * @param challenge チャレンジデータ（Base64）
	 * @return 認証トークン
	 */
	ChallengeResult challenge(Base64ReceiveDataType challenge);

}

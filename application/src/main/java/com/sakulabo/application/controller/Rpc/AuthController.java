package com.sakulabo.application.controller.Rpc;

import com.sakulabo.application.app.rpc.RpcSendParam;
import com.sakulabo.application.app.rpc.datatype.receive.StringReceiveDataType;
import com.sakulabo.application.app.rpc.datatype.send.Base64SendDataType;
import com.sakulabo.application.app.rpc.datatype.send.DateTimeSendDataType;
import com.sakulabo.application.common.spi.RpcTarget;

/**
 * チャレンジ認証機能のRPCコントローラー規定インターフェイスです
 *
 * @author keeeeeent
 */
public interface AuthController extends RpcTarget {

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
	 * チャレンジデータ取得処理を実施します
	 *
	 * @param userName ユーザ名
	 * @return チャレンジデータ
	 */
	Challenge nonce(StringReceiveDataType userName);

}

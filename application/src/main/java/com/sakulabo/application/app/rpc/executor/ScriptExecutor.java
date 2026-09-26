package com.sakulabo.application.app.rpc.executor;

import com.sakulabo.application.app.rpc.RpcSendParam;
import com.sakulabo.application.app.rpc.datatype.receive.BooleanReceiveDataType;
import com.sakulabo.application.app.rpc.datatype.receive.PathReceiveDataType;
import com.sakulabo.application.app.rpc.datatype.receive.StringReceiveDataType;
import com.sakulabo.application.app.rpc.datatype.send.StringSendDataType;
import com.sakulabo.application.common.spi.RpcTarget;

/**
 * スクリプト実行機能のRPCコントローラー規定インターフェイスです
 *
 * @author keeeeeent
 */
public interface ScriptExecutor extends RpcTarget {

	/**
	 * スクリプト実行結果連携データ構造
	 * 
	 * @param result 実行結果詳細
	 */
	public record ExecuteResult(@RpcSendParam("result") StringSendDataType result) {
	}

	/**
	 * スクリプトの実行を行います
	 * 
	 * @param sessionId セッションID
	 * @param isSecure  セキュアフラグ
	 * @param path      スクリプトパス
	 */
	public ExecuteResult executeScript(
			StringReceiveDataType sessionId,
			BooleanReceiveDataType isSecure,
			PathReceiveDataType path);

}

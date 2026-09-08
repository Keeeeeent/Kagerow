package com.sakulabo.application.app.rpc.executor;

import com.sakulabo.application.app.rpc.RpcSendParam;
import com.sakulabo.application.app.rpc.datatype.send.ArraySendDataType;
import com.sakulabo.application.common.spi.RpcTarget;

/**
 * スキーマ管理機能のRPCコントローラー規定インターフェイスです
 *
 * @author keeeeeent
 */
public interface SchemaExecutor extends RpcTarget {

	/**
	 * スキーマ解析結果返却用データ構造
	 * @param list スキーマ一覧リスト
	 */
	public record SchemaList(
			@RpcSendParam("list") ArraySendDataType list) {
	};

}

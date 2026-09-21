package com.sakulabo.application.app.rpc.executor;

import com.sakulabo.application.app.rpc.RpcSendParam;
import com.sakulabo.application.app.rpc.datatype.receive.StringReceiveDataType;
import com.sakulabo.application.app.rpc.datatype.send.ArraySendDataType;
import com.sakulabo.application.app.rpc.datatype.send.DateTimeSendDataType;
import com.sakulabo.application.app.rpc.datatype.send.IntegerSendDataType;
import com.sakulabo.application.app.rpc.datatype.send.StringSendDataType;
import com.sakulabo.application.app.rpc.exception.ExitCodeException;
import com.sakulabo.application.app.rpc.exception.RpcRuntimeException;
import com.sakulabo.application.common.spi.RpcTarget;

/**
 * スキーマ管理機能のRPCコントローラー規定インターフェイスです
 *
 * @author keeeeeent
 */
public interface SchemaExecutor extends RpcTarget {

	/**
	 * スキーマ解析結果返却用データ構造
	 *
	 * @param list スキーマ一覧リスト
	 */
	public record SchemaList(
			@RpcSendParam("list") ArraySendDataType list) {
	};

	/**
	 * スキーマ情報解析結果返却用データ構造
	 * 
	 * @param schemaName  スキーマ名称
	 * @param size        サイズ
	 * @param tables      テーブル数
	 * @param lastUpdated 最終更新日
	 */
	public record SchemaInfo(
			@RpcSendParam("schemaName") StringSendDataType schemaName,
			@RpcSendParam("size") IntegerSendDataType size,
			@RpcSendParam("tables") IntegerSendDataType tables,
			@RpcSendParam("lastUpdated") DateTimeSendDataType lastUpdated) {
	};

	/**
	 * スキーマ一覧リストを取得します
	 *
	 * @return 取得結果
	 * @throws RpcRuntimeException メソッド実行失敗
	 */
	public SchemaList getSchemaList() throws RpcRuntimeException;

	/**
	 * スキーマ情報の取得を行います
	 * 
	 * @param schema 対象スキーマ名称
	 * @throws ExitCodeException スキーマ情報取得失敗
	 */
	public SchemaInfo getSchemaInfo(StringReceiveDataType schema) throws ExitCodeException;

	/**
	 * スキーマの削除を行います
	 * 
	 * @param schema 削除対象スキーマ名称
	 * @throws ExitCodeException スキーマ削除失敗
	 */
	public void deleteSchema(StringReceiveDataType schema) throws ExitCodeException;

}

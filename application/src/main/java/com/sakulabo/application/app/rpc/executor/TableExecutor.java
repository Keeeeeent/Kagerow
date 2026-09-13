package com.sakulabo.application.app.rpc.executor;

import com.sakulabo.application.app.rpc.RpcSendParam;
import com.sakulabo.application.app.rpc.datatype.receive.StringReceiveDataType;
import com.sakulabo.application.app.rpc.datatype.send.ArraySendDataType;
import com.sakulabo.application.app.rpc.exception.RpcRuntimeException;
import com.sakulabo.application.common.spi.RpcTarget;

/**
 * テーブル管理機能のRPCコントローラー規定インターフェイスです
 *
 * @author keeeeeent
 */
public interface TableExecutor extends RpcTarget {

    /**
     * テーブル解析結果返却用データ構造
     *
     * @param list テーブル覧リスト
     */
    public record TableList(
            @RpcSendParam("list") ArraySendDataType list) {
    };

    /**
     * テーブル一覧リストを取得します
     *
     * @param schema スキーマ名称
     * @return 取得結果
     * @throws RpcRuntimeException メソッド実行失敗
     */
    public TableList getTableList(StringReceiveDataType schema) throws RpcRuntimeException;

}

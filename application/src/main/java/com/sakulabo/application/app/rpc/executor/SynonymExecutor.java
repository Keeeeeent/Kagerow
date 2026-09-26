package com.sakulabo.application.app.rpc.executor;

import com.sakulabo.application.app.rpc.RpcSendParam;
import com.sakulabo.application.app.rpc.datatype.receive.StringReceiveDataType;
import com.sakulabo.application.app.rpc.datatype.send.ArraySendDataType;
import com.sakulabo.application.app.rpc.exception.RpcRuntimeException;
import com.sakulabo.application.common.spi.RpcTarget;

/**
 * シノニム管理機能のRPCコントローラー規定インターフェイスです
 *
 * @author keeeeeent
 */
public interface SynonymExecutor extends RpcTarget {

    /**
     * シノニム解析結果返却用データ構造
     *
     * @param list パッケージ一覧リスト
     */
    public record SynonymList(
            @RpcSendParam("list") ArraySendDataType list) {
    };

    /**
     * シノニム一覧リストを取得します
     * 
     * @param schema スキーマ名称
     * @return シノニム一覧リスト
     * @throws RpcRuntimeException シノニム取得失敗
     */
    public SynonymList getSynonymList(StringReceiveDataType schema) throws RpcRuntimeException;

}

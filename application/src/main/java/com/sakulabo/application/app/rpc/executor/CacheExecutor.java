package com.sakulabo.application.app.rpc.executor;

import com.sakulabo.application.app.rpc.RpcSendParam;
import com.sakulabo.application.app.rpc.datatype.receive.StringReceiveDataType;
import com.sakulabo.application.app.rpc.datatype.send.ArraySendDataType;
import com.sakulabo.application.app.rpc.datatype.send.StringSendDataType;
import com.sakulabo.application.app.rpc.exception.RpcRuntimeException;
import com.sakulabo.application.common.spi.RpcTarget;

/**
 * キャッシュ管理機能のRPCコントローラー規定インターフェイスです
 *
 * @author keeeeeent
 */
public interface CacheExecutor extends RpcTarget {

    /**
     * キャッシュ解析結果返却用データ構造
     *
     * @param list キャッシュ一覧リスト
     */
    public record CacheList(
            @RpcSendParam("list") ArraySendDataType list) {
    };

    /**
     * キャッシュ情報解析結果返却用データ構造
     *
     * @param list キャッシュファイル一覧リスト
     */
    public record CacheInfo(
            @RpcSendParam("list") ArraySendDataType list) {
    };

    /**
     * キャッシュ作成解析結果返却用データ構造
     *
     * @param list キャッシュ作成情報
     */
    public record CacheCreateInfo(
            @RpcSendParam("cacheId") StringSendDataType cache) {
    };

    /**
     * キャッシュ一覧リストを返却します
     * 
     * @return キャッシュ一覧リスト
     * @throws RpcRuntimeException キャッシュ制御失敗
     */
    public CacheList getCacheList() throws RpcRuntimeException;

    /**
     * キャッシュ管理ファイル一覧リストを取得します
     * 
     * @param cache キャッシュID
     * @return キャッシュ管理ファイル一覧リスト
     * @throws RpcRuntimeException キャッシュ制御失敗
     */
    public CacheInfo getCacheInfo(StringReceiveDataType cache) throws RpcRuntimeException;

    /**
     * キャッシュを生成します
     * 
     * @return キャッシュ生成情報
     * @throws RpcRuntimeException キャッシュ生成失敗
     */
    public CacheCreateInfo createCache() throws RpcRuntimeException;

    /**
     * キャッシュを削除します
     * 
     * @param cache キャッシュID
     * @throws RpcRuntimeException キャッシュ生成失敗
     */
    public void deleteCache(StringReceiveDataType cache) throws RpcRuntimeException;

}

package com.sakulabo.application.app.rpc.executor.Impl;

import java.util.ArrayList;
import java.util.List;

import javax.naming.Binding;
import javax.naming.Name;
import javax.naming.NamingEnumeration;

import com.sakulabo.application.app.rpc.RpcFilter;
import com.sakulabo.application.app.rpc.RpcMethod;
import com.sakulabo.application.app.rpc.RpcMethodParam;
import com.sakulabo.application.app.rpc.RpcSetting;
import com.sakulabo.application.app.rpc.datatype.receive.StringReceiveDataType;
import com.sakulabo.application.app.rpc.datatype.send.ArraySendDataType;
import com.sakulabo.application.app.rpc.datatype.send.StringSendDataType;
import com.sakulabo.application.app.rpc.exception.RpcRuntimeException;
import com.sakulabo.application.app.rpc.executor.CacheExecutor;
import com.sakulabo.application.app.rpc.filters.CertificationFilter;
import com.sakulabo.core.Kagerow.Contents.KagerowCacheContent;
import com.sakulabo.core.Kagerow.Context.KagerowCacheContext;
import com.sakulabo.core.Kagerow.Utilities.KagerowUtilities;

/**
 * キャッシュ管理機能のRPCコントローラー実装クラス
 *
 * @author keeeeeent
 */
@RpcSetting("cache")
@RpcFilter(filter = CertificationFilter.class, required = true)
public class CacheExecutorImpl implements CacheExecutor {

    /** {@inheritDoc} */
    @Override
    @RpcMethod("list")
    public CacheList getCacheList() throws RpcRuntimeException {
        try {
            // コンテキスト取得
            KagerowCacheContext ctx = KagerowUtilities.getContext(KagerowCacheContext._NAME);
            // 一覧取得
            NamingEnumeration<Binding> list = ctx.listBindings((Name) null);
            List<String> resultList = new ArrayList<>();
            while (list.hasMore()) {
                // リストに格納
                String name = list.next().getName();
                resultList.add(name);
            }
            // 返却用インスタンス生成
            ArraySendDataType arraySendDataType = ArraySendDataType.getInstance(resultList);
            CacheList cacheList = new CacheList(arraySendDataType);
            return cacheList;
        } catch (Exception e) {
            throw new RpcRuntimeException("Failed to retrieve cache list", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    @RpcMethod("info")
    public CacheInfo getCacheInfo(
            @RpcMethodParam(value = "cacheId", required = true) StringReceiveDataType cache)
            throws RpcRuntimeException {
        // 引数取得
        String cacheId = cache.getRawType().get();
        try {
            // コンテキスト取得
            KagerowCacheContext ctx = KagerowUtilities.getContext(KagerowCacheContext._NAME);
            // 一覧取得
            KagerowCacheContent cnt = ctx.lookup(cacheId);
            NamingEnumeration<Binding> list = cnt.listBindings((Name) null);
            List<String> resultList = new ArrayList<>();
            while (list.hasMore()) {
                // リストに格納
                String name = list.next().getName();
                resultList.add(name);
            }
            // 返却用インスタンス生成
            ArraySendDataType arraySendDataType = ArraySendDataType.getInstance(resultList);
            CacheInfo cacheInfo = new CacheInfo(arraySendDataType);
            return cacheInfo;
        } catch (Exception e) {
            throw new RpcRuntimeException("Failed to retrieve cache information", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    @RpcMethod("create")
    public CacheCreateInfo createCache() throws RpcRuntimeException {
        try {
            // コンテキスト取得
            KagerowCacheContext ctx = KagerowUtilities.getContext(KagerowCacheContext._NAME);
            KagerowCacheContent cnt = ctx.createSubcontext((Name) null);
            String cacheId = cnt.getNameInNamespace();
            StringSendDataType cache = new StringSendDataType(cacheId);
            CacheCreateInfo cacheCreateInfo = new CacheCreateInfo(cache);
            return cacheCreateInfo;
        } catch (Exception e) {
            throw new RpcRuntimeException("Failed to create the cache", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    @RpcMethod("delete")
    public void deleteCache(StringReceiveDataType cache) throws RpcRuntimeException {
        // 引数取得
        String cacheId = cache.getRawType().get();
        try {
            KagerowCacheContext ctx = KagerowUtilities.getContext(KagerowCacheContext._NAME);
            ctx.destroySubcontext(cacheId);
        } catch (Exception e) {
            throw new RpcRuntimeException("Failed to delete the cache", e);
        }
    }

}

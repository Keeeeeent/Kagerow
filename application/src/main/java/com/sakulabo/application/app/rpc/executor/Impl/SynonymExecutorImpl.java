package com.sakulabo.application.app.rpc.executor.Impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.sakulabo.application.app.rpc.RpcFilter;
import com.sakulabo.application.app.rpc.RpcMethod;
import com.sakulabo.application.app.rpc.RpcMethodParam;
import com.sakulabo.application.app.rpc.RpcSetting;
import com.sakulabo.application.app.rpc.datatype.receive.StringReceiveDataType;
import com.sakulabo.application.app.rpc.datatype.send.ArraySendDataType;
import com.sakulabo.application.app.rpc.exception.RpcRuntimeException;
import com.sakulabo.application.app.rpc.executor.SynonymExecutor;
import com.sakulabo.application.app.rpc.filters.CertificationFilter;
import com.sakulabo.core.Kagerow.Context.KagerowVirtualDirContext;
import com.sakulabo.core.Kagerow.Context.KagerowVirtualFileContext;
import com.sakulabo.core.Kagerow.Utilities.KagerowUtilities;

/**
 * シノニム管理機能のRPCコントローラー実装クラス
 *
 * @author keeeeeent
 */
@RpcSetting("synonym")
@RpcFilter(filter = CertificationFilter.class, required = true)
public class SynonymExecutorImpl implements SynonymExecutor {

    /** {@inheritDoc} */
    @Override
    @RpcMethod("list")
    public SynonymList getSynonymList(
            @RpcMethodParam(value = "schemaName", required = true) StringReceiveDataType schema)
            throws RpcRuntimeException {
        // 引数取得
        String schemaName = schema.getRawType().get();
        // メイン処理呼び出し
        try {
            KagerowVirtualFileContext ctx = KagerowUtilities.getContext(KagerowVirtualFileContext._NAME);
            KagerowVirtualDirContext dirCtx = ctx.lookup(schemaName);
            Map<String, String> synonyms = dirCtx.getSynonymMapList();
            List<String> synonymList = new ArrayList<>();
            for (Map.Entry<String, String> entry : synonyms.entrySet()) {
                String synonym = String.join(",", entry.getKey(), entry.getValue());
                synonymList.add(synonym);
            }
            // 返却用インスタンス生成
            ArraySendDataType arraySendDataType = ArraySendDataType.getInstance(synonymList);
            SynonymList synonymSendList = new SynonymList(arraySendDataType);
            return synonymSendList;
        } catch (Exception e) {
            throw new RpcRuntimeException("Failed to retrieve schema information", e);
        }
    }

}

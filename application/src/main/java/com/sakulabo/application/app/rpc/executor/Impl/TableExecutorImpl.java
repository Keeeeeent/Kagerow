package com.sakulabo.application.app.rpc.executor.Impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.naming.Binding;
import javax.naming.Name;
import javax.naming.NamingEnumeration;

import com.sakulabo.application.app.rpc.RpcFilter;
import com.sakulabo.application.app.rpc.RpcMethod;
import com.sakulabo.application.app.rpc.RpcMethodParam;
import com.sakulabo.application.app.rpc.RpcSetting;
import com.sakulabo.application.app.rpc.datatype.receive.StringReceiveDataType;
import com.sakulabo.application.app.rpc.datatype.send.ArraySendDataType;
import com.sakulabo.application.app.rpc.exception.RpcRuntimeException;
import com.sakulabo.application.app.rpc.executor.TableExecutor;
import com.sakulabo.application.app.rpc.filters.CertificationFilter;
import com.sakulabo.core.Kagerow.Context.KagerowVirtualDirContext;
import com.sakulabo.core.Kagerow.Context.KagerowVirtualFileContext;
import com.sakulabo.core.Kagerow.Utilities.KagerowUtilities;

/**
 * テーブル管理機能のRPCコントローラー実装クラス
 *
 * @author keeeeeent
 */
@RpcSetting("table")
@RpcFilter(filter = CertificationFilter.class, required = true)
public class TableExecutorImpl implements TableExecutor {

    /**
     * テーブル一覧リストを取得します
     *
     * @param schema スキーマ名称
     * @return 取得結果
     * @throws RpcRuntimeException メソッド実行失敗
     */
    /** {@inheritDoc} */
    @Override
    @RpcMethod("list")
    public TableList getTableList(
            @RpcMethodParam(value = "schemaName", required = true) StringReceiveDataType schema)
            throws RpcRuntimeException {
        // 引数取得
        String schemaName = schema.getRawType().get();
        // メイン処理呼び出し
        try {
            // コンテキスト取得
            KagerowVirtualFileContext ctx = KagerowUtilities.getContext(KagerowVirtualFileContext._NAME);
            // 対象テーブルコンテキスト取得
            KagerowVirtualDirContext dirCtx = ctx.lookup(schemaName);
            // シノニムリスト取得
            Map<String, String> synonyms = dirCtx.getSynonymMapList();
            // テーブル一覧取得
            NamingEnumeration<Binding> list = dirCtx.listBindings((Name) null);
            List<String> resultList = new ArrayList<>();
            while (list.hasMore()) {
                String physicalName = list.next().getName();
                String logicalName = "?????";
                for (Map.Entry<String, String> entry : synonyms.entrySet()) {
                    if (entry.getValue().equals(physicalName)) {
                        logicalName = entry.getKey();
                        break;
                    }
                }
                // フォーマット済み文字列生成
                String formattedStr = String.format("%-17s %s%n", logicalName, physicalName);
                // リストに格納
                resultList.add(formattedStr);
            }
            // 返却用インスタンス生成
            ArraySendDataType arraySendDataType = ArraySendDataType.getInstance(resultList);
            TableList tableList = new TableList(arraySendDataType);
            return tableList;
        } catch (Exception e) {
            throw new RpcRuntimeException("Failed to retrieve table information", e);
        }
    }

}

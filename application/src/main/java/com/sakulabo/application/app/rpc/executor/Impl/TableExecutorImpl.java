package com.sakulabo.application.app.rpc.executor.Impl;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.stream.Stream;

import javax.naming.Binding;
import javax.naming.Name;
import javax.naming.NamingEnumeration;

import com.sakulabo.application.app.rpc.RpcFilter;
import com.sakulabo.application.app.rpc.RpcMethod;
import com.sakulabo.application.app.rpc.RpcMethodParam;
import com.sakulabo.application.app.rpc.RpcSetting;
import com.sakulabo.application.app.rpc.datatype.receive.IntegerReceiveDataType;
import com.sakulabo.application.app.rpc.datatype.receive.StringReceiveDataType;
import com.sakulabo.application.app.rpc.datatype.send.ArraySendDataType;
import com.sakulabo.application.app.rpc.datatype.send.DateTimeSendDataType;
import com.sakulabo.application.app.rpc.datatype.send.IntegerSendDataType;
import com.sakulabo.application.app.rpc.datatype.send.StringSendDataType;
import com.sakulabo.application.app.rpc.exception.ExitCodeException;
import com.sakulabo.application.app.rpc.exception.RpcRuntimeException;
import com.sakulabo.application.app.rpc.executor.TableExecutor;
import com.sakulabo.application.app.rpc.filters.CertificationFilter;
import com.sakulabo.core.Kagerow.Contents.KagerowVirtualFileContent;
import com.sakulabo.core.Kagerow.Contents.KagerowVirtualFileContent.KagerowDataType;
import com.sakulabo.core.Kagerow.Contents.KagerowVirtualFileContent.KagerowVirtualFileObject;
import com.sakulabo.core.Kagerow.Context.KagerowVirtualDirContext;
import com.sakulabo.core.Kagerow.Context.KagerowVirtualFileContext;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;
import com.sakulabo.core.Kagerow.Utilities.KagerowTransaction;
import com.sakulabo.core.Kagerow.Utilities.KagerowUtilities;

/**
 * テーブル管理機能のRPCコントローラー実装クラス
 *
 * @author keeeeeent
 */
@RpcSetting("table")
@RpcFilter(filter = CertificationFilter.class, required = true)
public class TableExecutorImpl implements TableExecutor {

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
                StringJoiner joiner = new StringJoiner(",");
                joiner.add(logicalName)
                        .add(physicalName);
                // リストに格納
                resultList.add(joiner.toString());
            }
            // 返却用インスタンス生成
            ArraySendDataType arraySendDataType = ArraySendDataType.getInstance(resultList);
            TableList tableList = new TableList(arraySendDataType);
            return tableList;
        } catch (Exception e) {
            throw new RpcRuntimeException("Failed to retrieve table information", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    @RpcMethod("generation")
    public TableList getTableGeneration(
            @RpcMethodParam(value = "schemaName", required = true) StringReceiveDataType schema,
            @RpcMethodParam(value = "tableName", required = true) StringReceiveDataType table)
            throws RpcRuntimeException {

        // 引数取得
        String schemaName = schema.getRawType().get();
        String tableName = table.getRawType().get();

        // メイン処理呼び出し
        try {
            // コンテキスト取得
            KagerowVirtualFileContext ctx = KagerowUtilities.getContext(KagerowVirtualFileContext._NAME);
            // 対象テーブルコンテキスト取得
            KagerowVirtualDirContext dirCtx = ctx.lookup(schemaName);
            // シノニムリスト取得
            Map<String, String> synonyms = dirCtx.getSynonymMapList();
            // テーブル世代一覧取得
            KagerowVirtualFileContent cnt = dirCtx.lookup(tableName);
            String physicalTableName = "?????";
            for (Map.Entry<String, String> entry : synonyms.entrySet()) {
                if (entry.getValue().equals(tableName)) {
                    physicalTableName = entry.getKey();
                    break;
                }
            }
            List<String> resultList = new ArrayList<>();
            for (int i = 0; i < cnt.contentSize(); i++) {
                StringJoiner joiner = new StringJoiner(",");
                KagerowVirtualFileObject fileObject = cnt.get(i);
                joiner.add(String.valueOf(i))
                        .add(formatSize(fileObject.datSize().longValue()))
                        .add(fileObject.createTime().toString())
                        .add(String.format("${%s[%d]}", physicalTableName, i));
                resultList.add(joiner.toString());
            }
            // 返却用インスタンス生成
            ArraySendDataType arraySendDataType = ArraySendDataType.getInstance(resultList);
            TableList tableList = new TableList(arraySendDataType);
            return tableList;
        } catch (Exception e) {
            throw new RpcRuntimeException("Failed to retrieve table generation", e);
        }

    }

    /** {@inheritDoc} */
    @Override
    @RpcMethod("info")
    public TableInfo getTableInfo(
            @RpcMethodParam(value = "schemaName", required = true) StringReceiveDataType schema,
            @RpcMethodParam(value = "tableName", required = true) StringReceiveDataType table,
            @RpcMethodParam(value = "generation") IntegerReceiveDataType generation)
            throws RpcRuntimeException {

        // 引数取得
        String schemaName = schema.getRawType().get();
        String tableName = table.getRawType().get();
        int generate = generation.getRawType().orElse(BigInteger.ZERO).intValue();

        // メイン処理呼び出し
        try {
            // コンテキスト取得
            KagerowVirtualFileContext ctx = KagerowUtilities.getContext(KagerowVirtualFileContext._NAME);
            // 対象テーブルコンテキスト取得
            KagerowVirtualDirContext dirCtx = ctx.lookup(schemaName);
            // テーブル世代一覧取得
            KagerowVirtualFileContent cnt = dirCtx.lookup(tableName);
            // 返却用インスタンス生成
            KagerowVirtualFileObject fileObject = cnt.get(generate);
            StringSendDataType sendUri = new StringSendDataType(fileObject.uri().get());
            StringSendDataType sendphysicsname = new StringSendDataType(schemaName);
            StringSendDataType sendTablename = new StringSendDataType(tableName);
            IntegerSendDataType sendSize = new IntegerSendDataType(fileObject.datSize());
            DateTimeSendDataType sendCreated = new DateTimeSendDataType(
                    LocalDateTime.ofInstant(fileObject.createTime(), ZoneId.systemDefault()));
            ArraySendDataType arraySendHeader = ArraySendDataType.getInstance(fileObject.headerData());
            ArraySendDataType arraySendDataType = ArraySendDataType
                    .getInstance(Stream.of(fileObject.dataType()).map(KagerowDataType::name).toList());
            TableInfo tableInfo = new TableInfo(
                    sendUri,
                    sendphysicsname,
                    sendTablename,
                    sendSize,
                    sendCreated,
                    arraySendHeader,
                    arraySendDataType);
            return tableInfo;
        } catch (Exception e) {
            throw new RpcRuntimeException("Failed to retrieve table information", e);
        }

    }

    /** {@inheritDoc} */
    @Override
    @RpcMethod("delete")
    public void deleteTable(
            @RpcMethodParam(value = "schemaName", required = true) StringReceiveDataType schema,
            @RpcMethodParam(value = "tableName") StringReceiveDataType table,
            @RpcMethodParam(value = "synonymName") StringReceiveDataType synonym,
            @RpcMethodParam(value = "generation") IntegerReceiveDataType generation)
            throws ExitCodeException, RpcRuntimeException {

        // 引数取得
        String schemaName = schema.getRawType().get();
        String tableName = table.getRawType().orElse(null);
        String synonymName = table.getRawType().orElse(null);
        int generate = generation.getRawType().orElse(BigInteger.ZERO).intValue();

        try {
            if (Objects.isNull(tableName) && Objects.isNull(synonymName)) {
                throw new ExitCodeException("synonym or table must be specified", 2);
            }
            KagerowVirtualFileContext ctx = KagerowUtilities.getContext(KagerowVirtualFileContext._NAME);
            KagerowVirtualDirContext dirCtx = ctx.lookup(schemaName);
            Map<String, String> synonyms = dirCtx.getSynonymMapList();
            if (Objects.isNull(tableName)) {
                for (Map.Entry<String, String> entry : synonyms.entrySet()) {
                    if (entry.getKey().equals(synonymName)) {
                        tableName = entry.getValue();
                        break;
                    }
                }
            }
            KagerowTransaction tran = KagerowTransaction.getTransactionFromSchemaName(schemaName);
            try (tran) {
                KagerowVirtualFileContent cnt = dirCtx.lookup(tableName);
                if (cnt.contentSize() < generate) {
                    throw new ExitCodeException("The number of specified generations exceeds the maximum limit", 3);
                }
                if (cnt.contentSize() >= 1) {
                    throw new ExitCodeException("There must be at least one table", 4);
                }
                KagerowVirtualFileObject fileObject = cnt.get(generate);
                String target = KagerowVirtualFileContent.getGeneration(fileObject);
                cnt.destroySubcontext(target);
            }

        } catch (Exception e) {
            KagerowLogger.newAppLogger().err(e);
            throw new RpcRuntimeException("Failed to delete the table", e);
        }

    }

    /**
     * サイズフォーマット
     *
     * @param sizeBytes サイズ（バイト）
     * @return フォーマット済みのサイズ
     */
    private static String formatSize(long sizeBytes) {
        if (sizeBytes < 1024) {
            return sizeBytes + " B";
        }
        double size = sizeBytes;
        String[] units = { "KB", "MB", "GB", "TB", "PB", "EB" };
        for (String unit : units) {
            size /= 1024;

            if (size < 1024) {
                return String.format("%.1f %s", size, unit);
            }
        }
        return String.format("%.1f ZB", size / 1024);
    }

}

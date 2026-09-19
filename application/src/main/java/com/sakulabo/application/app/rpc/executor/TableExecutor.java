package com.sakulabo.application.app.rpc.executor;

import com.sakulabo.application.app.rpc.RpcMethod;
import com.sakulabo.application.app.rpc.RpcMethodParam;
import com.sakulabo.application.app.rpc.RpcSendParam;
import com.sakulabo.application.app.rpc.datatype.receive.IntegerReceiveDataType;
import com.sakulabo.application.app.rpc.datatype.receive.StringReceiveDataType;
import com.sakulabo.application.app.rpc.datatype.send.ArraySendDataType;
import com.sakulabo.application.app.rpc.datatype.send.DateTimeSendDataType;
import com.sakulabo.application.app.rpc.datatype.send.IntegerSendDataType;
import com.sakulabo.application.app.rpc.datatype.send.StringSendDataType;
import com.sakulabo.application.app.rpc.exception.ExitCodeException;
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
         * テーブル情報解析結果返却用データ構造
         * 
         * @param uri         KagerowURI
         * @param physicsname テーブル物理名
         * @param tablename   テーブル論理名
         * @param size        データサイズ
         * @param created     作成日
         * @param header      ヘッダーリスト
         * @param datatype    データリスト
         */
        public record TableInfo(
                        @RpcSendParam("uri") StringSendDataType uri,
                        @RpcSendParam("physicsname") StringSendDataType physicsname,
                        @RpcSendParam("tablename") StringSendDataType tablename,
                        @RpcSendParam("size") IntegerSendDataType size,
                        @RpcSendParam("created") DateTimeSendDataType created,
                        @RpcSendParam("header") ArraySendDataType header,
                        @RpcSendParam("datatype") ArraySendDataType datatype) {
        };

        /**
         * テーブル一覧リストを取得します
         *
         * @param schema スキーマ名称
         * @return 取得結果
         * @throws RpcRuntimeException メソッド実行失敗
         */
        public TableList getTableList(StringReceiveDataType schema) throws RpcRuntimeException;

        /**
         * テーブル世代一覧リストを取得します
         *
         * @param schema スキーマ名称
         * @param table  テーブル名称
         * @return 世代一覧リスト
         * @throws RpcRuntimeException
         */
        public TableList getTableGeneration(StringReceiveDataType schema, StringReceiveDataType table)
                        throws RpcRuntimeException;

        /**
         * テーブル詳細情報を取得します
         *
         * @param schema     スキーマ名称
         * @param table      テーブル名称
         * @param generation テーブル世代
         * @return テーブル詳細情報
         * @throws RpcRuntimeException テーブル詳細情報取得失敗
         */
        public TableInfo getTableInfo(
                        StringReceiveDataType schema,
                        StringReceiveDataType table,
                        IntegerReceiveDataType generation)
                        throws RpcRuntimeException;

        /**
         * テーブルを削除します
         * 
         * @param schema     スキーマ名称
         * @param table      テーブル物理名称
         * @param synonym    テーブル論理名称
         * @param generation テーブル世代
         * @throws ExitCodeException   テーブル削除失敗
         * @throws RpcRuntimeException 予期せぬエラー
         */
        @RpcMethod("delete")
        public void deleteTable(
                        @RpcMethodParam(value = "schemaName", required = true) StringReceiveDataType schema,
                        @RpcMethodParam(value = "tableName") StringReceiveDataType table,
                        @RpcMethodParam(value = "synonymName") StringReceiveDataType synonym,
                        @RpcMethodParam(value = "generation") IntegerReceiveDataType generation)
                        throws ExitCodeException, RpcRuntimeException;

}

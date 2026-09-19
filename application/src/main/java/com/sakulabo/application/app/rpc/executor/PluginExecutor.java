package com.sakulabo.application.app.rpc.executor;

import com.sakulabo.application.app.rpc.RpcSendParam;
import com.sakulabo.application.app.rpc.datatype.receive.StringReceiveDataType;
import com.sakulabo.application.app.rpc.datatype.send.ArraySendDataType;
import com.sakulabo.application.app.rpc.exception.ExitCodeException;
import com.sakulabo.application.app.rpc.exception.RpcRuntimeException;
import com.sakulabo.application.common.spi.RpcTarget;

/**
 * プラグイン管理機能のRPCコントローラー規定インターフェイスです
 *
 * @author keeeeeent
 */
public interface PluginExecutor extends RpcTarget {

        /**
         * プラグイン解析結果返却用データ構造
         *
         * @param list プラグイン一覧リスト
         */
        public record PluginList(
                        @RpcSendParam("list") ArraySendDataType list) {
        };

        /**
         * プラグインパッケージ解析結果返却用データ構造<br/>
         *
         * @param list パッケージ一覧リスト
         */
        public record PluginPkgList(
                        @RpcSendParam("list") ArraySendDataType list) {
        };

        /**
         * プラグインパッケージ一覧情報を取得します
         *
         * @return プラグインパッケージ一覧情報
         * @throws RpcRuntimeException プラグインパッケージ一覧情報取得失敗
         */
        public PluginPkgList getPkgList() throws RpcRuntimeException;

        /**
         * プラグイン一覧情報を取得します
         *
         * @param pkg パッケージ名称
         * @return プラグイン一覧情報
         * @throws RpcRuntimeException プラグイン一覧情報取得失敗
         */
        public PluginList getPluginList(StringReceiveDataType pkg) throws RpcRuntimeException;

        /**
         * プラグインの状態を無効化に変更します
         *
         * @param pkgNm     パッケージ名称
         * @param pluginVer プラグインバージョン
         * @param pluginNm  プラグイン名称
         * @throws ExitCodeException プラグイン状態変更失敗
         */
        public void setDisable(
                        StringReceiveDataType pkgNm,
                        StringReceiveDataType pluginVer,
                        StringReceiveDataType pluginNm) throws ExitCodeException;

        /**
         * プラグインの状態を有効化に変更します
         *
         * @param pkgNm     パッケージ名称
         * @param pluginVer プラグインバージョン
         * @param pluginNm  プラグイン名称
         * @throws ExitCodeException プラグイン状態変更失敗
         */
        public void setEnable(
                        StringReceiveDataType pkgNm,
                        StringReceiveDataType pluginVer,
                        StringReceiveDataType pluginNm) throws ExitCodeException;

}

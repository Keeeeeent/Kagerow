package com.sakulabo.application.app.rpc.executor.Impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.naming.Binding;
import javax.naming.Name;
import javax.naming.NamingEnumeration;

import com.sakulabo.application.app.rpc.RpcFilter;
import com.sakulabo.application.app.rpc.RpcMethod;
import com.sakulabo.application.app.rpc.RpcMethodParam;
import com.sakulabo.application.app.rpc.RpcSetting;
import com.sakulabo.application.app.rpc.datatype.receive.StringReceiveDataType;
import com.sakulabo.application.app.rpc.datatype.send.ArraySendDataType;
import com.sakulabo.application.app.rpc.exception.ExitCodeException;
import com.sakulabo.application.app.rpc.exception.RpcRuntimeException;
import com.sakulabo.application.app.rpc.executor.PluginExecutor;
import com.sakulabo.application.app.rpc.filters.CertificationFilter;
import com.sakulabo.core.Kagerow.Context.KagerowPluginContext;
import com.sakulabo.core.Kagerow.Context.KagerowPluginPackageContext;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;
import com.sakulabo.core.Kagerow.Utilities.KagerowUtilities;

/**
 * プラグイン管理機能のRPCコントローラー実装クラスです
 *
 * @author keeeeeent
 */
@RpcSetting("plugin")
@RpcFilter(filter = CertificationFilter.class, required = true)
public class PluginExecutorImpl implements PluginExecutor {

    /** {@inheritDoc} */
    @Override
    @RpcMethod("pkgs")
    public PluginPkgList getPkgList() throws RpcRuntimeException {
        try {
            KagerowPluginPackageContext ctx = KagerowUtilities.getContext(KagerowPluginPackageContext._NAME);
            NamingEnumeration<Binding> pluginPkgs = ctx.listBindings((Name) null);
            List<String> pkgList = new ArrayList<>();
            while (pluginPkgs.hasMore()) {
                String pluginPkgName = pluginPkgs.next().getName();
                pkgList.add(pluginPkgName);
            }
            ArraySendDataType sendList = ArraySendDataType.getInstance(pkgList);
            PluginPkgList pluginPkgList = new PluginPkgList(sendList);
            return pluginPkgList;
        } catch (Exception e) {
            throw new RpcRuntimeException("Failed to retrieve package information", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    @RpcMethod("list")
    public PluginList getPluginList(
            @RpcMethodParam(value = "pkgName", required = true) StringReceiveDataType pkg) throws RpcRuntimeException {
        String pkgName = pkg.getRawType().get();
        try {
            KagerowPluginPackageContext ctx = KagerowUtilities.getContext(KagerowPluginPackageContext._NAME);
            KagerowPluginContext plugin = ctx.lookup(pkgName);
            NamingEnumeration<Binding> pluginInfoList = plugin.listBindings((Name) null);
            List<String> pluginList = new ArrayList<>();
            while (pluginInfoList.hasMore()) {
                String pluginName = pluginInfoList.next().getName();
                pluginList.add(pluginName);
            }
            ArraySendDataType sendList = ArraySendDataType.getInstance(pluginList);
            PluginList result = new PluginList(sendList);
            return result;
        } catch (Exception e) {
            throw new RpcRuntimeException("Failed to retrieve plugin information", e);
        }
    }

    /** {@information} */
    @Override
    @RpcMethod("disable")
    public void setDisable(
            @RpcMethodParam(value = "packageName", required = true) StringReceiveDataType pkgNm,
            @RpcMethodParam("version") StringReceiveDataType pluginVer,
            @RpcMethodParam("pluginName") StringReceiveDataType pluginNm) throws ExitCodeException {
        String packageName = pkgNm.getRawType().get();
        String version = pluginVer.getRawType().orElse(null);
        String pluginName = pluginNm.getRawType().orElse(null);
        try {
            if ("default".equals(packageName)) {
                throw new ExitCodeException("The default plugin cannot be disabled", 4);
            }
            KagerowPluginPackageContext ctx = KagerowUtilities.getContext(KagerowPluginPackageContext._NAME);
            Name pkgName = KagerowUtilities.createVersioningPluginPkgName(packageName, version);
            if (Objects.isNull(pluginName)) {
                if (ctx.isDisable(pkgName.toString())) {
                    throw new ExitCodeException("The target package has already been disabled", 2);
                }
                ctx.setDisable(true, pkgName.toString());
            } else {
                KagerowPluginContext plugins = ctx.lookup(pkgName);
                if (plugins.isDisable(pluginName)) {
                    throw new ExitCodeException("The plugin has already been disabled", 3);
                }
                plugins.setDisable(true, pluginName);
            }
        } catch (Exception e) {
            KagerowLogger.newAppLogger().err(e);
            throw new ExitCodeException("An unexpected error has occurred", 1, e);
        }
    }

    /** {@information} */
    @Override
    @RpcMethod("enable")
    public void setEnable(
            @RpcMethodParam(value = "packageName", required = true) StringReceiveDataType pkgNm,
            @RpcMethodParam("version") StringReceiveDataType pluginVer,
            @RpcMethodParam("pluginName") StringReceiveDataType pluginNm) throws ExitCodeException {
        String packageName = pkgNm.getRawType().get();
        String version = pluginVer.getRawType().orElse(null);
        String pluginName = pluginNm.getRawType().orElse(null);
        try {
            if ("default".equals(packageName)) {
                return;
            }
            KagerowPluginPackageContext ctx = KagerowUtilities.getContext(KagerowPluginPackageContext._NAME);
            Name pkgName = KagerowUtilities.createVersioningPluginPkgName(packageName, version);
            if (Objects.isNull(pluginName)) {
                if (!ctx.isDisable(pkgName.toString())) {
                    throw new ExitCodeException("The target package has already been enable", 2);
                }
                ctx.setDisable(false, pkgName.toString());
            } else {
                KagerowPluginContext plugins = ctx.lookup(pkgName);
                if (!plugins.isDisable(pluginName)) {
                    throw new ExitCodeException("The plugin has already been enable", 3);
                }
                plugins.setDisable(false, pluginName);
            }
        } catch (Exception e) {
            KagerowLogger.newAppLogger().err(e);
            throw new ExitCodeException("An unexpected error has occurred", 1, e);
        }
    }

}

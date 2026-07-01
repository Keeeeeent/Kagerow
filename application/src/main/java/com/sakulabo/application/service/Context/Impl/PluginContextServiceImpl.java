package com.sakulabo.application.service.Context.Impl;

import java.util.ArrayList;
import java.util.List;

import javax.naming.Binding;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import com.sakulabo.application.service.BaseService;
import com.sakulabo.application.service.Context.PluginContextService;
import com.sakulabo.core.Kagerow.Context.KagerowPluginContext;
import com.sakulabo.core.Kagerow.Context.KagerowPluginPackageContext;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;
import com.sakulabo.core.Kagerow.Utilities.KagerowUtilities;
import com.sakulabo.regulation.annotation.KagerowComponent;
import com.sakulabo.regulation.annotation.KagerowInject;
import com.sakulabo.regulation.annotation.KagerowPlugin.PluginType;

/**
 * プラグインコンテキストサービスの実装クラスです
 * 
 * @author keeeeeent
 */
@KagerowComponent
public class PluginContextServiceImpl extends BaseService implements PluginContextService {

	/** アプリケーションロガー */
	@KagerowInject
	private KagerowLogger logger;

	/** {@inheritDoc} */
	@Override
	public List<PluginContextInfo> getPluginList() {
		List<PluginContextInfo> pluginInfoList = new ArrayList<>();
		try {
			KagerowPluginPackageContext pluginPkgs = KagerowUtilities.getContext(KagerowPluginPackageContext._NAME);
			NamingEnumeration<Binding> pluginPkgInfo = pluginPkgs.listBindings("");
			while (pluginPkgInfo.hasMore()) {
				Binding pkgInfo = pluginPkgInfo.next();
				KagerowPluginContext plugins = pluginPkgs.lookup(pkgInfo.getName());
				NamingEnumeration<Binding> pluginInfo = plugins.listBindings("");
				while (pluginInfo.hasMore()) {
					PluginContextInfo info = new PluginContextInfo(
							pkgInfo.getName(),
							pluginInfo.next().getName(),
							plugins.toString());
					pluginInfoList.add(info);
				}
			}
		} catch (NamingException e) {
			logger.err(e);
		}
		return pluginInfoList;
	}

	/** {@inheritDoc} */
	@Override
	public List<PluginContextInfo> getPluginList(PluginType type) {
		List<PluginContextInfo> pluginInfoList = getPluginList()
				.stream()
				.filter(plugin -> {
					try {
						return KagerowUtilities.isSupportPluginType(plugin.packageName(), plugin.pluginName(), type);
					} catch (Exception e) {
						e.printStackTrace();
						return false;
					}
				})
				.toList();
		return pluginInfoList;
	}

}

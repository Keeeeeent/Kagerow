package com.sakulabo.core.Processor.config;

import java.lang.System.Logger.Level;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import javax.management.InstanceAlreadyExistsException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.sakulabo.core.Kagerow.Context.KagerowCacheContext;
import com.sakulabo.core.Kagerow.Context.KagerowContexts;
import com.sakulabo.core.Kagerow.Context.KagerowPluginContext;
import com.sakulabo.core.Kagerow.Context.KagerowPluginPackageContext;
import com.sakulabo.core.Kagerow.Context.KagerowSecurityContext;
import com.sakulabo.core.Kagerow.Context.KagerowSettingContext;
import com.sakulabo.core.Kagerow.Context.KagerowVirtualFileContext;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;
import com.sakulabo.core.Processor.jmx.AppJMXInitializer;

/**
 * Kagerowコンテキスト専用コンフィグレーションレコード生成クラスです
 * 
 * @author keeeeeent
 */
public final class ContextConfigurationLorder extends ConfigurationLorder<ContextConfiguration> {

	/** メインコンテキスト環境変数 */
	private Map<String, String> CONTEXT_ENV = new ConcurrentHashMap<>();
	/** プラグインパッケージコンテキスト環境変数 */
	private Map<String, String> PLUGIN_PKG_CONTEXT_ENV = new ConcurrentHashMap<>();
	/** プラグインコンテキスト環境変数 */
	private Map<String, String> PLUGIN_CONTEXT_ENV = new ConcurrentHashMap<>();
	/** 仮想ファイルコンテキスト環境変数 */
	private Map<String, String> VIRTUAL_FILE_CONTEXT_ENV = new ConcurrentHashMap<>();
	/** キャッシュコンテキスト環境変数 */
	private Map<String, String> CACHE_CONTEXT_ENV = new ConcurrentHashMap<>();
	/** キャッシュコンテキスト環境変数 */
	private Map<String, String> SECURITY_CONTEXT_ENV = new ConcurrentHashMap<>();
	/** セッティングコンテキスト環境変数 */
	private Map<String, String> SETTING_CONTEXT_ENV = new ConcurrentHashMap<>();
	/** 設定インスタンス */
	private static volatile ContextConfiguration config;

	/**
	 * 設定インスタンスを取得、生成します
	 * @return 設定インスタンス
	 */
	public static final ContextConfiguration getInstance() {
		if (Objects.isNull(config)) {
			synchronized (ContextConfigurationLorder.class) {
				if (Objects.isNull(config)) {
					ContextConfigurationLorder lorder = new ContextConfigurationLorder();
					config = lorder.load();
				}
			}
		}
		return config;
	}

	/** {@inheritDoc} */
	@Override
	protected ContextConfiguration build() {

		// コンフィグレーション生成
		ContextConfiguration config = new ContextConfiguration(
				CONTEXT_ENV,
				PLUGIN_PKG_CONTEXT_ENV,
				PLUGIN_CONTEXT_ENV,
				VIRTUAL_FILE_CONTEXT_ENV,
				CACHE_CONTEXT_ENV,
				SECURITY_CONTEXT_ENV,
				SETTING_CONTEXT_ENV);

		// JMX登録
		try {
			AppJMXInitializer.registMXBean(config);
		} catch (Exception e) {
			if (!(e instanceof InstanceAlreadyExistsException)) {
				KagerowLogger.newSystemLogger().log(Level.ERROR, e);
			}
		}

		return config;
	}

	/** {@inheritDoc} */
	@Override
	protected void lordConfigFile(Document document, XPath xPath) throws Exception {

		/** コンテキスト */
		{
			String type = getEnvPropStr(KagerowContexts._NAME);
			expr = xPath.compile(type);
			nodeList = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
			setEnv(CONTEXT_ENV, nodeList);
		}

		/** プラグインパッケージコンテキスト */
		{
			String type = getEnvPropStr(KagerowPluginPackageContext._NAME);
			expr = xPath.compile(type);
			nodeList = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
			setEnv(PLUGIN_PKG_CONTEXT_ENV, nodeList);
		}

		/** プラグインコンテキスト */
		{
			String type = getEnvPropStr(KagerowPluginContext._NAME);
			expr = xPath.compile(type);
			nodeList = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
			setEnv(PLUGIN_CONTEXT_ENV, nodeList);
		}

		/** バーチャルファイルコンテキスト */
		{
			String type = getEnvPropStr(KagerowVirtualFileContext._NAME);
			expr = xPath.compile(type);
			nodeList = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
			setEnv(VIRTUAL_FILE_CONTEXT_ENV, nodeList);
		}

		/** キャッシュコンテキスト */
		{
			String type = getEnvPropStr(KagerowCacheContext._NAME);
			expr = xPath.compile(type);
			nodeList = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
			setEnv(CACHE_CONTEXT_ENV, nodeList);
		}

		/** セキュリティコンテキスト */
		{
			String type = getEnvPropStr(KagerowSecurityContext._NAME);
			expr = xPath.compile(type);
			nodeList = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
			setEnv(SECURITY_CONTEXT_ENV, nodeList);
		}

		/** セッティングコンテキスト */
		{
			String type = getEnvPropStr(KagerowSettingContext._NAME);
			expr = xPath.compile(type);
			nodeList = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
			setEnv(SETTING_CONTEXT_ENV, nodeList);
		}

	}

	/**
	 * 環境変数をノードリストから生成します
	 * @param env 環境変数格納先
	 * @param nodeList ノードリスト
	 */
	private void setEnv(Map<String, String> env, NodeList nodeList) {
		for (int i = 0; i < nodeList.getLength(); i++) {
			if (nodeList.item(i) instanceof Element element) {
				String key = element.getAttribute("key");
				String value = element.getAttribute("value");
				env.put(key, value);
			}
		}
	}

	/**
	 * xPath検索用文字列を生成します
	 * @param type 対応するタイプ属性
	 * @return 生成された文字列
	 */
	private String getEnvPropStr(String type) {
		return "/KagerowApplication/Contexts/Context[@type=\"%s\"]/Environment/Property"
				.formatted(type);
	}

}

package com.sakulabo.core.Processor.config;

import java.util.Map;

import com.sakulabo.core.Processor.jmx.AppJMX;
import com.sakulabo.core.Processor.jmx.Configuration.ContextConfigurationMXBean;

/**
 * コンテキスト専用のコンフィグレーションレコードクラスです
 * 
 * @param CONTEXT_ENV メインコンテキスト環境変数
 * @param PLUGIN_PKG_CONTEXT_ENV プラグインパッケージコンテキスト環境変数
 * @param PLUGIN_CONTEXT_ENV プラグインコンテキスト環境変数
 * @param VIRTUAL_FILE_CONTEXT_ENV 仮想ファイルコンテキスト環境変数
 * @param CACHE_CONTEXT_ENV キャッシュコンテキスト環境変数
 * @param SECURITY_CONTEXT_ENV セキュリティコンテキスト環境変数
 * @param SETTING_CONTEXT_ENV セッティングコンテキスト環境変数
 */
@AppJMX(name = "Configuration", options = { "type=ContextConfiguration" })
public record ContextConfiguration(
		Map<String, String> CONTEXT_ENV,
		Map<String, String> PLUGIN_PKG_CONTEXT_ENV,
		Map<String, String> PLUGIN_CONTEXT_ENV,
		Map<String, String> VIRTUAL_FILE_CONTEXT_ENV,
		Map<String, String> CACHE_CONTEXT_ENV,
		Map<String, String> SECURITY_CONTEXT_ENV,
		Map<String, String> SETTING_CONTEXT_ENV)
		implements ContextConfigurationMXBean {

	/** {@inheritDoc} */
	@Override
	public Map<String, String> getCONTEXT_ENV() {
		return CONTEXT_ENV;
	}

	/** {@inheritDoc} */
	@Override
	public Map<String, String> getPLUGIN_PKG_CONTEXT_ENV() {
		return PLUGIN_PKG_CONTEXT_ENV;
	}

	/** {@inheritDoc} */
	@Override
	public Map<String, String> getPLUGIN_CONTEXT_ENV() {
		return PLUGIN_CONTEXT_ENV;
	}

	/** {@inheritDoc} */
	@Override
	public Map<String, String> getVIRTUAL_FILE_CONTEXT_ENV() {
		return VIRTUAL_FILE_CONTEXT_ENV;
	}

	/** {@inheritDoc} */
	@Override
	public Map<String, String> getCACHE_CONTEXT_ENV() {
		return CACHE_CONTEXT_ENV;
	}

	/** {@inheritDoc} */
	@Override
	public Map<String, String> getSECURITY_CONTEXT_ENV() {
		return SECURITY_CONTEXT_ENV;
	}

	/** {@inheritDoc} */
	@Override
	public Map<String, String> getSETTING_CONTEXT_ENV() {
		return SETTING_CONTEXT_ENV;
	}

}

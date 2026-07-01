package com.sakulabo.core.Processor.jmx.Configuration;

import java.util.Map;

import com.sakulabo.core.Processor.jmx.BaseKagerowJMX;

/**
 * コンテキスト専用のコンフィグレーションを監視するインターフェイスです
 * 
 * @author keeeeeent
 */
public interface ContextConfigurationMXBean extends BaseKagerowJMX {

	/**
	 * メインコンテキスト環境変数を監視します
	 * @return メインコンテキスト環境変数
	 */
	Map<String, String> getCONTEXT_ENV();

	/**
	 * プラグインパッケージコンテキスト環境変数を監視します
	 * @return プラグインパッケージコンテキスト環境変数
	 */
	Map<String, String> getPLUGIN_PKG_CONTEXT_ENV();

	/**
	 * プラグインコンテキスト環境変数を監視します
	 * @return プラグインコンテキスト環境変数
	 */
	Map<String, String> getPLUGIN_CONTEXT_ENV();

	/**
	 * 仮想ファイルコンテキスト環境変数を監視します
	 * @return 仮想ファイルコンテキスト環境変数
	 */
	Map<String, String> getVIRTUAL_FILE_CONTEXT_ENV();

	/**
	 * キャッシュコンテキスト環境変数を監視します
	 * @return キャッシュコンテキスト環境変数
	 */
	Map<String, String> getCACHE_CONTEXT_ENV();

	/**
	 * セキュリティコンテキスト環境変数を監視します
	 * @return セキュリティコンテキスト環境変数
	 */
	Map<String, String> getSECURITY_CONTEXT_ENV();

	/**
	 * セッティングコンテキスト環境変数を監視します
	 * @return セッティングコンテキスト環境変数
	 */
	Map<String, String> getSETTING_CONTEXT_ENV();

}

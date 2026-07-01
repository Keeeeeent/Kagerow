package com.sakulabo.application.helper;

import com.sakulabo.core.Kagerow.Utilities.KagerowScriptAccessor;
import com.sakulabo.core.Kagerow.Utilities.KagerowScriptAccessor.KagerowSqlAccessor;
import com.sakulabo.regulation.annotation.KagerowComponent;

/**
 * KagerowScriptAccessorを操作するヘルパーです
 * 
 * @author keeeeeent
 */
@KagerowComponent
public final class KagerowScriptAccessorHelper {

	/**
	 * スクリプトアクセッサーからIDを元に名称を検索します
	 * @param script 検索ソース（スクリプトアクセッサー）
	 * @param sqlId 検索対象ID
	 * @return 検索結果（名称）
	 */
	public final String getName(KagerowScriptAccessor script, String sqlId) {
		return script.getKsqls()
				.stream()
				.filter(f -> f.getId().equals(sqlId))
				.map(KagerowSqlAccessor::getName)
				.findFirst()
				.orElse(sqlId);
	}

}

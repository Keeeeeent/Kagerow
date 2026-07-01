package com.sakulabo.core.Processor.jmx.Context;

import java.util.Map;

import com.sakulabo.core.Processor.jmx.BaseKagerowJMX;

/**
 * コンテキスト監視基底インターフェイス
 * 
 * @author keeeeeent
 */
public interface JMXContext extends BaseKagerowJMX {

	/**
	 * コンテキストの管理対象を文字列表現で取得します
	 * @return 管理対象文字表現リスト
	 */
	Map<String, String> getContext();

}

package com.sakulabo.core.Processor.jmx.Configuration;

import java.util.List;

import com.sakulabo.core.Processor.jmx.BaseKagerowJMX;

/**
 * コンテキストデシリアライズ専用のコンフィグレーションを監視するインターフェイスです
 * 
 * @author keeeeeent
 */
public interface AppObjectInputFilterConfigurationMXBean extends BaseKagerowJMX {

	/**
	 * オブジェクトチェーン最大深度を監視します
	 * @return オブジェクトチェーン最大深度
	 */
	int getMAX_DEPTH();

	/**
	 * 配列最大要素数を監視します
	 * @return 配列最大要素数
	 */
	int getMAX_ARRAY_LENGTH();

	/**
	 * デシリアライズ許可クラスリストを監視します
	 * @return デシリアライズ許可クラスリスト
	 */
	List<String> getALLOWED_CLASS_LIST();
}

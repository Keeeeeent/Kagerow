package com.sakulabo.core.Processor.config;

import java.util.List;

import com.sakulabo.core.Processor.jmx.AppJMX;
import com.sakulabo.core.Processor.jmx.Configuration.AppObjectInputFilterConfigurationMXBean;

/**
 * Kagerowデシリアライズ専用コンフィグレーションレコードクラスです
 * @param MAX_DEPTH オブジェクトチェーン最大深度
 * @param MAX_ARRAY_LENGTH 配列最大要素数
 * @param ALLOWED_CLASS_LIST デシリアライズ許可クラスリスト
 */
@AppJMX(name = "Configuration", options = { "type=AppObjectInputFilterConfiguration" })
public record AppObjectInputFilterConfiguration(
		int MAX_DEPTH,
		int MAX_ARRAY_LENGTH,
		List<String> ALLOWED_CLASS_LIST) implements AppObjectInputFilterConfigurationMXBean {

	/** {@inheritDoc} */
	@Override
	public int getMAX_DEPTH() {
		return MAX_DEPTH;
	}

	/** {@inheritDoc} */
	@Override
	public int getMAX_ARRAY_LENGTH() {
		return MAX_ARRAY_LENGTH;
	}

	/** {@inheritDoc} */
	@Override
	public List<String> getALLOWED_CLASS_LIST() {
		return ALLOWED_CLASS_LIST;
	}

}

package com.sakulabo.core.Processor.plugin;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;
import com.sakulabo.regulation.spi.PluginAdapter;

/**
 * プラグインのパラメータ解析を補助するキャッシュ保持クラスです<br/>
 * キャッシュの保持はJVM依存となっており、クラスのアンロードと共に破棄されます
 * 
 * @author keeeeeent
 */
public class PluginParamCache extends ClassValue<Map<String, PluginAdapter.Param[]>> {

	/** {@inheritDoc} */
	@Override
	protected Map<String, PluginAdapter.Param[]> computeValue(Class<?> type) {

		Map<String, PluginAdapter.Param[]> result = new HashMap<>();

		try {
			{
				// メソッド取得
				Optional<Method> methoz = Stream.of(type.getMethods())
						.filter(f -> filterMethod(f, PluginParamParser.INPUT))
						.findFirst();

				if (methoz.isPresent()) {
					// アノテーション解析
					PluginAdapter.Param[] paramList = methoz.get()
							.getDeclaredAnnotationsByType(PluginAdapter.Param.class);
					// キャッシュ追加
					result.put(PluginParamParser.INPUT, paramList);
				}

			}
			{
				// メソッド取得
				Optional<Method> methoz = Stream.of(type.getMethods())
						.filter(f -> filterMethod(f, PluginParamParser.OUTPUT))
						.findFirst();

				if (methoz.isPresent()) {
					// アノテーション解析
					PluginAdapter.Param[] paramList = methoz.get()
							.getDeclaredAnnotationsByType(PluginAdapter.Param.class);
					// キャッシュ追加
					result.put(PluginParamParser.OUTPUT, paramList);
				}
			}
		} catch (Exception e) {
			KagerowLogger.newAppLogger().err(e);
		}

		return result;
	}

	/**
	 * メソッドを検索します
	 * @param method メソッド
	 * @param name メソッド名称
	 * @return 判定結果
	 */
	private boolean filterMethod(Method method, String name) {
		return method.getName().equals(name);
	}

}

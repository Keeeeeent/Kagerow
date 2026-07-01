package com.sakulabo.core.Kagerow.Adapter;

/**
 * Beanロード判定機能を提供するアダプターインターフェイスです
 * 
 * @author keeeeeent
 */
@FunctionalInterface
public interface KagerowDIContextAdapter {

	/**
	 * 対象クラスがロード対象か判定します
	 * @param target 解析対象クラス
	 * @return 判定結果
	 */
	boolean isLord(Class<?> target);

}

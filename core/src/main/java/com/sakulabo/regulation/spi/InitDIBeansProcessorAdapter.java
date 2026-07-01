package com.sakulabo.regulation.spi;

/**
 * DIのBean初期化実装を規定するためのアダプターインターフェイスです
 * 
 * @author keeeeeent
 */
@FunctionalInterface
public interface InitDIBeansProcessorAdapter {

	/**
	 * Beanインスタンスの初期化プロセスを提供します
	 * @return 読み込みクラス情報
	 */
	Class<?> init();
}

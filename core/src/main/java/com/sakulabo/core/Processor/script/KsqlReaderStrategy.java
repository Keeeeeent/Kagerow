package com.sakulabo.core.Processor.script;

/**
 * Kagerowスクリプトファイル共通で使用される解析ストラテジーインターフェイスです
 * 将来の仕様変更で対応が必要になった場合、広報互換性を担保した上で実装を拡張してください
 * 
 * @author keeeeeent
 */
public sealed interface KsqlReaderStrategy {

	/**
	 * KFileバージョン1向けのストラテジー実装になります
	 */
	public static non-sealed class KsqlReaderStrategyV1 implements KsqlReaderStrategy {
	}

}

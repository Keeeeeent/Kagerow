package com.sakulabo.core.Common;

import java.math.BigInteger;

/**
 * Kagerowアプリケーション共通のデータサイズ計算クラスです
 * 
 * @author keeeeeent
 */
public enum DataSize {

	/** バイト単位 */
	B(1L),
	/** キロバイト単位 */
	KB(1024L),
	/** メガバイト単位 */
	MB(1024L * 1024L),
	/** ギガバイト単位 */
	GB(1024L * 1024L * 1024L),
	/** テラバイト単位 */
	TB(1024L * 1024L * 1024L * 1024L);

	/** サイズ変換定数 */
	private long base;

	/**
	 * デフォルトコンストラクタ
	 * @param base サイズ変換定数
	 */
	private DataSize(long base) {
		this.base = base;
	}

	/**
	 * サイズ取得
	 * @param size サイズ
	 * @return 計算後サイズ
	 */
	public BigInteger toSize(long size) {
		BigInteger base = BigInteger.valueOf(this.base);
		return base.multiply(BigInteger.valueOf(size));
	}

	/**
	 * サイズを単位つきに変換します
	 * @param size サイズ
	 * @return 単位に関連づけられたサイズ
	 */
	public BigInteger toUnitSize(BigInteger size) {
		return size.divide(BigInteger.valueOf(base));
	}
}

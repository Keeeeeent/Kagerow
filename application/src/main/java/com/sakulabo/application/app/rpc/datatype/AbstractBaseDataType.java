package com.sakulabo.application.app.rpc.datatype;

/**
 * RPCパラメータ基底実装クラス
 *
 * @author keeeeeent
 */
public class AbstractBaseDataType {

	/** 変換前データ */
	protected final String baseData;

	/**
	 * デフォルトコンストラクタ
	 *
	 * @param baseData 変換前データ
	 */
	protected AbstractBaseDataType(String baseData) {
		this.baseData = baseData;
	}

}

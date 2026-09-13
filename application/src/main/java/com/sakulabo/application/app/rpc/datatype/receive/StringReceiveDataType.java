package com.sakulabo.application.app.rpc.datatype.receive;

import java.util.Optional;

import com.sakulabo.application.app.rpc.datatype.AbstractBaseDataType;
import com.sakulabo.application.app.rpc.datatype.BaseDataType;
import com.sakulabo.application.app.rpc.datatype.RpcDataTypes;

/**
 * RPCパラメータString受け取り実装クラス
 *
 * @author keeeeeent
 */
public class StringReceiveDataType extends AbstractBaseDataType implements BaseDataType<String> {

	/**
	 * デフォルトコンストラクタ
	 *
	 * @param baseData 変換前データ
	 */
	public StringReceiveDataType(String baseData) {
		super(baseData);
	}

	/** {@inheritDoc} */
	@Override
	public Optional<String> getRawType() throws IllegalStateException {
		return Optional.ofNullable(baseData);
	}

	/** {@inheritDoc} */
	@Override
	public RpcDataTypes toRpcDataType() {
		return RpcDataTypes.STRING;
	}

}
package com.sakulabo.application.app.rpc.datatype.send;

import java.math.BigInteger;
import java.util.Objects;
import java.util.Optional;

import com.sakulabo.application.app.rpc.datatype.AbstractBaseDataType;
import com.sakulabo.application.app.rpc.datatype.BaseDataType;
import com.sakulabo.application.app.rpc.datatype.RpcDataTypes;

/**
 * RPCパラメータInteger受け渡し実装クラス
 *
 * @author keeeeeent
 */
public class IntegerSendDataType extends AbstractBaseDataType implements BaseDataType<BigInteger> {

	/**
	 * デフォルトコンストラクタ
	 *
	 * @param baseData 変換前データ
	 */
	public IntegerSendDataType(String baseData) {
		super(baseData);
	}

	/** {@inheritDoc} */
	@Override
	public Optional<BigInteger> getRawType() throws IllegalStateException {
		if (Objects.nonNull(baseData)) {
			try {
				BigInteger integer = new BigInteger(baseData);
				return Optional.of(integer);
			} catch (NumberFormatException e) {
				String msg = String.format("Can not to Integer from %s", baseData);
				throw new IllegalStateException(msg, e);
			}
		}
		return Optional.empty();
	}

	/** {@inheritDoc} */
	@Override
	public RpcDataTypes toRpcDataType() {
		return RpcDataTypes.INTEGER;
	}

}

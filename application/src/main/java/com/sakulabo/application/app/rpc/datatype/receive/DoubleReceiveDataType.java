package com.sakulabo.application.app.rpc.datatype.receive;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

import com.sakulabo.application.app.rpc.datatype.AbstractBaseDataType;
import com.sakulabo.application.app.rpc.datatype.BaseDataType;
import com.sakulabo.application.app.rpc.datatype.RpcDataTypes;

/**
 * RPCパラメータDouble受け取り実装クラス
 *
 * @author keeeeeent
 */
public class DoubleReceiveDataType extends AbstractBaseDataType implements BaseDataType<BigDecimal> {

	/**
	 * デフォルトコンストラクタ
	 *
	 * @param baseData 変換前データ
	 */
	public DoubleReceiveDataType(String baseData) {
		super(baseData);
	}

	/** {@inheritDoc} */
	@Override
	public Optional<BigDecimal> getRawType() throws IllegalStateException {
		if (Objects.nonNull(baseData)) {
			try {
				BigDecimal decimal = new BigDecimal(baseData);
				return Optional.of(decimal);
			} catch (NumberFormatException e) {
				String msg = String.format("Can not to Double from %s", baseData);
				throw new IllegalStateException(msg, e);
			}
		}
		return Optional.empty();
	}

	/** {@inheritDoc} */
	@Override
	public RpcDataTypes toRpcDataType() {
		return RpcDataTypes.DOUBLE;
	}

}

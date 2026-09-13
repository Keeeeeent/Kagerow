package com.sakulabo.application.app.rpc.datatype.send;

import java.util.Objects;
import java.util.Optional;

import com.sakulabo.application.app.rpc.datatype.AbstractBaseDataType;
import com.sakulabo.application.app.rpc.datatype.BaseDataType;
import com.sakulabo.application.app.rpc.datatype.RpcDataTypes;

/**
 * RPCパラメータBoolean受け渡し実装クラス
 *
 * @author keeeeeent
 */
public class BooleanSendDataType extends AbstractBaseDataType implements BaseDataType<Boolean> {

	/**
	 * デフォルトコンストラクタ
	 *
	 * @param baseData 変換前データ
	 */
	public BooleanSendDataType(String baseData) {
		super(baseData);
	}

	/** {@inheritDoc} */
	@Override
	public Optional<Boolean> getRawType() {
		if (Objects.nonNull(baseData)) {
			if ("1".equals(baseData)) {
				return Optional.of(Boolean.TRUE);
			}
			return Optional.of(Boolean.FALSE);
		}
		return Optional.empty();
	}

	/** {@inheritDoc} */
	@Override
	public RpcDataTypes toRpcDataType() {
		return RpcDataTypes.BOOLEAN;
	}

}

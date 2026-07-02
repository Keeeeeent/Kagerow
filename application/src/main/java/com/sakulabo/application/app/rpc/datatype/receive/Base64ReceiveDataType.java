package com.sakulabo.application.app.rpc.datatype.receive;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;

import com.sakulabo.application.app.rpc.datatype.AbstractBaseDataType;
import com.sakulabo.application.app.rpc.datatype.BaseDataType;
import com.sakulabo.application.app.rpc.datatype.RpcDataTypes;

/**
 * RPCパラメータBase64受け取り実装クラス
 *
 * @author keeeeeent
 */
public class Base64ReceiveDataType extends AbstractBaseDataType implements BaseDataType<String> {

	/**
	 * デフォルトコンストラクタ
	 *
	 * @param baseData 変換前データ
	 */
	public Base64ReceiveDataType(String baseData) {
		super(baseData);
	}

	/** {@inheritDoc} */
	@Override
	public Optional<String> getRawType() {
		if (Objects.nonNull(baseData)) {
			byte[] data = Base64.getDecoder().decode(baseData.getBytes(StandardCharsets.UTF_8));
			return Optional.of(new String(data));
		}
		return Optional.empty();
	}

	/** {@inheritDoc} */
	@Override
	public RpcDataTypes toRpcDataType() {
		return RpcDataTypes.BASE64;
	}

}

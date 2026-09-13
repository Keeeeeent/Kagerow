package com.sakulabo.application.app.rpc.datatype.receive;

import java.nio.charset.Charset;
import java.util.Objects;
import java.util.Optional;

import com.sakulabo.application.app.rpc.datatype.AbstractBaseDataType;
import com.sakulabo.application.app.rpc.datatype.BaseDataType;
import com.sakulabo.application.app.rpc.datatype.RpcDataTypes;

/**
 * RPCパラメータCharset受け取り実装クラス
 *
 * @author keeeeeent
 */
public class CharsetReceiveDataType extends AbstractBaseDataType implements BaseDataType<Charset> {

	/**
	 * デフォルトコンストラクタ
	 *
	 * @param baseData 変換前データ
	 */
	public CharsetReceiveDataType(String baseData) {
		super(baseData);
	}

	/** {@inheritDoc} */
	@Override
	public Optional<Charset> getRawType() throws IllegalStateException {
		if (Objects.nonNull(baseData)) {
			try {
				Charset charset = Charset.forName(baseData);
				return Optional.of(charset);
			} catch (Exception e) {
				String msg = String.format("Can not to Charset from %s", baseData);
				throw new IllegalStateException(msg, e);
			}
		}
		return Optional.empty();
	}

	/** {@inheritDoc} */
	@Override
	public RpcDataTypes toRpcDataType() {
		return RpcDataTypes.STRING;
	}

}
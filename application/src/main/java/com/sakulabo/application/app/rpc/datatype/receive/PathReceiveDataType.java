package com.sakulabo.application.app.rpc.datatype.receive;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;

import com.sakulabo.application.app.rpc.datatype.AbstractBaseDataType;
import com.sakulabo.application.app.rpc.datatype.BaseDataType;
import com.sakulabo.application.app.rpc.datatype.RpcDataTypes;

/**
 * RPCパラメータPath受け取り実装クラス
 *
 * @author keeeeeent
 */
public class PathReceiveDataType extends AbstractBaseDataType implements BaseDataType<Path> {

	/**
	 * デフォルトコンストラクタ
	 *
	 * @param baseData 変換前データ
	 */
	public PathReceiveDataType(String baseData) {
		super(baseData);
	}

	/** {@inheritDoc} */
	@Override
	public Optional<Path> getRawType() throws IllegalStateException {
		if (Objects.nonNull(baseData)) {
			try {
				Path path = Paths.get(baseData);
				return Optional.of(path);
			} catch (Exception e) {
				String msg = String.format("Can not to Path from %s", baseData);
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

package com.sakulabo.application.app.rpc.datatype.send;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import java.util.Optional;

import com.sakulabo.application.app.rpc.datatype.AbstractBaseDataType;
import com.sakulabo.application.app.rpc.datatype.BaseDataType;
import com.sakulabo.application.app.rpc.datatype.RpcDataTypes;

/**
 * RPCパラメータDateTime受け渡し実装クラス
 *
 * @author keeeeeent
 */
public class DateTimeSendDataType extends AbstractBaseDataType implements BaseDataType<LocalDateTime> {

	/**
	 * デフォルトコンストラクタ
	 *
	 * @param baseData 変換前データ
	 */
	public DateTimeSendDataType(String baseData) {
		super(baseData);
	}

	/** {@inheritDoc} */
	@Override
	public Optional<LocalDateTime> getRawType() throws IllegalStateException {
		if (Objects.nonNull(baseData)) {
			try {
				LocalDateTime dateTime = LocalDateTime.parse(baseData, DateTimeFormatter.ISO_DATE_TIME);
				return Optional.of(dateTime);
			} catch (DateTimeParseException e) {
				String msg = String.format("Can not to LocalDateTime from %s", baseData);
				throw new IllegalStateException(msg, e);
			}
		}
		return Optional.empty();
	}

	/** {@inheritDoc} */
	@Override
	public RpcDataTypes toRpcDataType() {
		return RpcDataTypes.DATETIME;
	}

}

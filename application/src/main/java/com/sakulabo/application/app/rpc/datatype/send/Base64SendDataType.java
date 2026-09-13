package com.sakulabo.application.app.rpc.datatype.send;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;

import com.sakulabo.application.app.rpc.datatype.AbstractBaseDataType;
import com.sakulabo.application.app.rpc.datatype.BaseDataType;
import com.sakulabo.application.app.rpc.datatype.RpcDataTypes;

/**
 * RPCパラメータBase64受け渡し実装クラス
 *
 * @author keeeeeent
 */
public class Base64SendDataType extends AbstractBaseDataType implements BaseDataType<ByteBuffer> {

	/**
	 * デフォルトコンストラクタ
	 *
	 * @param baseData 変換前データ
	 */
	public Base64SendDataType(ByteBuffer baseData) {
		String base64 = null;
		if (Objects.nonNull(baseData)) {
			baseData.flip();
			base64 = Base64.getEncoder().encodeToString(baseData.array());
		}
		super(base64);
	}

	/**
	 * デフォルトコンストラクタ
	 *
	 * @param baseData 変換前データ
	 */
	public Base64SendDataType(byte[] baseData) {
		String base64 = null;
		if (Objects.nonNull(baseData)) {
			base64 = Base64.getEncoder().encodeToString(baseData);
		}
		super(base64);
	}

	/**
	 * デフォルトコンストラクタ
	 *
	 * @param baseData 変換前データ
	 * @param transform Base64変換フラグ
	 */
	public Base64SendDataType(String baseData, boolean transform) {
		String base64 = null;
		if (Objects.nonNull(baseData)) {
			if (transform) {
				base64 = Base64.getEncoder()
						.encodeToString(baseData.getBytes(StandardCharsets.UTF_8));
			} else {
				base64 = baseData;
			}
		}
		super(base64);
	}

	/**
	 * デフォルトコンストラクタ<br/>
	 * このコンストラクタBase64変換を行いません
	 *
	 * @param baseData 変換前データ
	 */
	public Base64SendDataType(String baseData) {
		this(baseData, false);
	}

	/** {@inheritDoc} */
	@Override
	public Optional<ByteBuffer> getRawType() {
		if (Objects.isNull(baseData)) {
			return Optional.empty();
		}
		byte[] rawData = Base64.getDecoder().decode(baseData);
		ByteBuffer buffer = ByteBuffer.wrap(rawData);
		return Optional.of(buffer);
	}

	/**
	 * Base64形式の文字列を取得します
	 * @return Base64形式の文字列
	 */
	public Optional<String> getBase64String() {
		return Optional.ofNullable(baseData);
	}

	/** {@inheritDoc} */
	@Override
	public RpcDataTypes toRpcDataType() {
		return RpcDataTypes.BASE64;
	}

}

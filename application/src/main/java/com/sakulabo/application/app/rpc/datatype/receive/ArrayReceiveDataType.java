package com.sakulabo.application.app.rpc.datatype.receive;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;

import com.sakulabo.application.app.rpc.datatype.AbstractBaseDataType;
import com.sakulabo.application.app.rpc.datatype.BaseDataType;
import com.sakulabo.application.app.rpc.datatype.RpcDataTypes;

/**
 * RPCパラメータ配列受け取り実装クラス
 *
 * @author keeeeeent
 */
public class ArrayReceiveDataType extends AbstractBaseDataType implements BaseDataType<List<String>> {

	/**
	 * デフォルトコンストラクタ
	 *
	 * @param baseData 変換前データ
	 */
	public ArrayReceiveDataType(String baseData) {
		super(baseData);
	}

	/**
	 * リストからインスタンスを生成します
	 * @param baseData ベースリスト
	 * @return 生成されたインスタンス
	 */
	public static ArrayReceiveDataType getInstance(List<String> baseData) {
		return getInstance(baseData.toArray(String[]::new));
	}

	/**
	 * 配列からインスタンスを生成します
	 * @param baseData ベースリスト
	 * @return 生成されたインスタンス
	 */
	public static ArrayReceiveDataType getInstance(String[] baseData) {
		StringJoiner joiner = new StringJoiner(",");
		for (String dat : baseData) {
			joiner.add(URLEncoder.encode(dat, StandardCharsets.UTF_8));
		}
		if (joiner.toString().isEmpty()) {
			return new ArrayReceiveDataType(null);
		} else {
			return new ArrayReceiveDataType(joiner.toString());
		}
	}

	/** {@inheritDoc} */
	@Override
	public Optional<List<String>> getRawType() {
		if (Objects.isNull(baseData)) {
			return Optional.empty();
		}
		List<String> rawData = new ArrayList<>();
		for (String dat : baseData.split(",", -1)) {
			rawData.add(URLDecoder.decode(dat, StandardCharsets.UTF_8));
		}
		return Optional.of(Collections.unmodifiableList(rawData));
	}

	/** {@inheritDoc} */
	@Override
	public RpcDataTypes toRpcDataType() {
		return RpcDataTypes.ARRAY;
	}

}

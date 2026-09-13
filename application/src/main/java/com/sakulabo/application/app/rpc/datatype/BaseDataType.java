package com.sakulabo.application.app.rpc.datatype;

import java.util.Optional;

/**
 * RPCパラメータ実装規定インターフェイス
 *
 * @author keeeeeent
 * @param <T> 内包型
 */
public interface BaseDataType<T> {

	/**
	 * 内包型を取り出します
	 *
	 * @return 内包型インスタンス
	 * @throws IllegalStateException 対象データへの変換が不可能な場合
	 */
	Optional<T> getRawType() throws IllegalStateException;

	/**
	 * RPC仕様向けのデータ型を取得します
	 *
	 * @return RPC仕様向けデータ型
	 */
	RpcDataTypes toRpcDataType();

	/**
	 * 変換前のデータを取得します
	 * @return 変換前データ
	 */
	String getData();

}

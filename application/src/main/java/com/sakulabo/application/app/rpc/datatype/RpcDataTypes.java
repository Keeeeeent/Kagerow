package com.sakulabo.application.app.rpc.datatype;

import java.util.List;

/**
 * RPCで使用可能なデータ型を列挙したクラスです
 *
 * @author keeeeeent
 */
public enum RpcDataTypes {

	/** Base64でエンコードされたバイナリデータ */
	BASE64("base64"),
	/** ブーリアン型の論理値（0または1） */
	BOOLEAN("boolean"),
	/** 日付と時刻 */
	DATETIME("dateTime.iso8601"),
	/** 倍精度浮動小数点数 */
	DOUBLE("double"),
	/** 整数 */
	INTEGER("int", "i4"),
	/** 文字列 */
	STRING("string"),
	/** マップ */
	STRUCT("struct"),
	/** NULL */
	NIL("nil"),
	/** 値配列 */
	ARRAY("array");

	/** 型の文字列表現 */
	private final List<String> typeStr;

	/**
	 * デフォルトコンストラクタ
	 *
	 * @param typeStr 型
	 */
	private RpcDataTypes(String... typeStr) {
		this.typeStr = List.of(typeStr);
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return typeStr.getFirst();
	}

}

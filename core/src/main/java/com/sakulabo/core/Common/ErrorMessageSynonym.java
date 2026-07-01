package com.sakulabo.core.Common;

/**
 * Kagerowアプリケーション専用エラーメッセージ生成シノニムクラスです
 * 
 * @author keeeeeent
 */
public enum ErrorMessageSynonym {

	/** ヘッダーの型情報が不正です */
	DONT_HEADER_NAME,
	/** ヘッダーの名称情報が不正です */
	DONT_TYPE_NAME,
	/** アルゴリズムの指定は必須です */
	DONT_SET_ALGORITHM,
	/** {0}に失敗しました */
	FAIL_LOGIC,
	/** ファイルが見つかりません【パス:{0}】 */
	NOT_FOUND_FILE,
	/** {0}は不正な名称です */
	INVALID_NAME,
	/** 要素数が不正です */
	ILLEGAL_ELEMENT_COUNT,
	/** 引数が指定されていますが使用されません【対象】: {0} */
	DONT_USING_PARAMS,
	/** コネクションが見つかりません */
	NOT_FOUND_CONNECTION;

}

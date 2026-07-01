package com.sakulabo.core.Kagerow.Utilities;

import javax.naming.NamingException;

import com.sakulabo.core.Kagerow.Context.KagerowVirtualFileContext;
import com.sakulabo.core.Kagerow.Exception.KSQLParseException;
import com.sakulabo.core.Processor.script.BaseKsqlTransformer;
import com.sakulabo.core.Processor.script.KsqlDatabaseLinker;

/**
 * KSQLに対するするスクリプト実装を規定するインターフェイスです
 * @author keeeeeent
 */
public sealed interface KagerowKsqlTransformer permits BaseKsqlTransformer {

	/**
	 * 対象文字列を実装されたアルゴリズムに従って変換します
	 * @param target 変換対象
	 * @return 変換後文字列
	 * @throws KSQLParseException 解析失敗
	 */
	String transform(String target) throws KSQLParseException;

	/**
	 * 解析エラーが発生した場合の原因を出力します
	 * @return エラー発生原因
	 */
	String parseLogicError();

	/**
	 * 解析エラーが発生した開始位置を取得します
	 * @return 開始位置
	 */
	int getStartIndex();

	/**
	 * 解析エラーが発生した終了位置を取得します
	 * @return 終了位置
	 */
	int getEndIndex();

	/**
	 * データベースリンク処理を実施するインスタンスを生成します
	 * @param schema  デフォルトスキーマ
	 * @param context コンテキスト
	 * @return データベースリンク実行インスタンス
	 * @throws NamingException インスタンス生成失敗
	 */
	public static KagerowKsqlTransformer createDataBaseLinker(String schema, KagerowVirtualFileContext context)
			throws NamingException {
		return new KsqlDatabaseLinker(schema, context);
	}

}

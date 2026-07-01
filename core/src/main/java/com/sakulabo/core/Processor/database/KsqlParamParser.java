package com.sakulabo.core.Processor.database;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import com.sakulabo.core.Common.ApplicationWordDictionary;
import com.sakulabo.core.Common.ErrorMessage;
import com.sakulabo.core.Kagerow.Exception.AppLogicException;
import com.sakulabo.core.Kagerow.Utilities.KagerowScriptAccessor;
import com.sakulabo.core.Kagerow.Utilities.KagerowScriptAccessor.KagerowSqlAccessor;

/**
 * Ksqlのパラメータ解析を行うパーサクラスです
 * 
 * @author keeeeeent
 */
public class KsqlParamParser {

	/** スクリプトアクセッサー */
	private final KagerowScriptAccessor script;

	/**
	 * デフォルトコンストラクタ
	 * @param script スクリプトアクセッサー 
	 */
	public KsqlParamParser(KagerowScriptAccessor script) {
		this.script = script;
	}

	/**
	 * KSQLをチェーンリンク状にソートします
	 * @return ソート後リスト
	 * @throws AppLogicException 終端KSQLが存在しない場合
	 */
	public final List<KagerowSqlAccessor> getSqlPlan() throws AppLogicException {

		// 結果格納リスト
		List<KagerowSqlAccessor> result = new ArrayList<>();

		if (script.getKsqls().size() == 1) {
			result.add(script.getKsqls().getFirst());
			return result;
		} else {

			// 一時格納先
			Deque<KagerowSqlAccessor> tmp = new ArrayDeque<>();

			// 開始位置となるKSQLインスタンスを取得
			KagerowSqlAccessor lastKsql = script.getKsqls().stream()
					.filter(Predicate.not(KagerowSqlAccessor::hasNext))
					.findFirst()
					.orElseThrow(this::createAppLogicException);

			// 終端要素の格納
			tmp.push(lastKsql);

			// チェーンリンクにするため、再帰処理実行
			searchNextKsql(lastKsql, tmp);
			// 結果返却リストセット
			result = tmp.stream().toList();

			return result;
		}

	}

	/**
	 * KSQLリストからnextによって順序づけを行ったリストを構築します
	 * @param lastKsql 現在のインスタンス
	 * @param result 格納先リスト
	 */
	private void searchNextKsql(KagerowSqlAccessor lastKsql, Deque<KagerowSqlAccessor> result) {

		// 次の要素を取得
		String targetId = lastKsql.getId();
		Optional<KagerowSqlAccessor> nextKsql = script.getKsqls().stream()
				.filter(f -> f.next().equals(targetId))
				.findFirst();

		if (nextKsql.isPresent()) {
			// 結果格納
			result.push(nextKsql.get());
			// 再帰呼び出し
			searchNextKsql(nextKsql.get(), result);
		}

	}

	/**
	 * 終端KSQLが存在しない旨を伝播する例外を生成します
	 * @return 生成された例外
	 */
	private AppLogicException createAppLogicException() {
		String msg = ErrorMessage.CODE_013.getMessage(
				ApplicationWordDictionary.WCD_0004.getMessage());
		return new AppLogicException(msg);
	}

}

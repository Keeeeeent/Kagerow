package com.sakulabo.core.Processor.script;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sakulabo.core.Common.ErrorMessage;
import com.sakulabo.core.Kagerow.Exception.KSQLParseException;

/**
 * KSQLに存在する置換文字列を処理するスクリプトクラスです
 * 
 * @author keeeeeent
 */
public final class KsqlReplaceWordParser extends BaseKsqlTransformer {

	/** ワードディクショナリー */
	private final Map<String, String> replaceWordDictionary;
	/** 置換パターン [@{....}] */
	private static final Pattern TARGET_PATTERN = Pattern.compile("(?<=@\\{)[^}]+(?=\\})");

	/**
	 * デフォルトコンストラクタ
	 * @param replaceWordDictionary ワードディクショナリー
	 */
	public KsqlReplaceWordParser(Map<String, String> replaceWordDictionary) {
		this.replaceWordDictionary = replaceWordDictionary;
	}

	/** {@inheritDoc} */
	@Override
	public String transform(String target) throws KSQLParseException {
		Matcher matcher = TARGET_PATTERN.matcher(target);
		// 対象が存在する場合置換処理対象か確認
		while (matcher.find()) {
			// ヒットワードを抽出
			String targetWord = matcher.group();
			// ワードディクショナリーに含まれているか確認
			if (replaceWordDictionary.containsKey(targetWord)) {
				// 文字列置換
				target = target.replace(formatedStr(targetWord), replaceWordDictionary.get(targetWord));
			} else {
				// エラー詳細構築
				errorMsg = ErrorMessage.CODE_901.getMessage(targetWord);
				startIndex = matcher.start();
				endIndex = matcher.end();
				// 含まれていない場合、未定義置換文字列としてパースエラー
				throw new KSQLParseException(this);
			}
		}
		return target;
	}

	/**
	 * 置換文字列を生成します
	 * @param reword 置換ワード
	 * @return 置換文字列
	 */
	private String formatedStr(String reword) {
		return "@{%s}".formatted(reword);
	}

}

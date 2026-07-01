package com.sakulabo.core.Processor.script;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sakulabo.core.Common.ErrorMessage;
import com.sakulabo.core.Kagerow.Exception.KSQLParseException;
import com.sakulabo.core.Kagerow.Utilities.KagerowScriptAccessor;

/**
 * KSQLに存在する環境変数を置換処理するスクリプトクラスです
 * 
 * @author keeeeeent
 */
public class KsqlReplaceEnvParser extends BaseKsqlTransformer {

	/** ワードディクショナリー */
	private final Map<String, String> replaceWordDictionary = new HashMap<>();
	/** 置換パターン [#{....}] */
	private static final Pattern TARGET_PATTERN = Pattern.compile("(?<=#\\{)[^}]+(?=\\})");

	/**
	 * デフォルトコンストラクタ
	 * @param replaceWordDictionary ワードディクショナリー
	 */
	public KsqlReplaceEnvParser(Map<String, String> replaceWordDictionary) {
		// OSの環境変数を取り込み
		{
			// 仕様上　空のMap or 通常のMapのためそのままputする
			Map<String, String> env = System.getenv();
			this.replaceWordDictionary.putAll(env);
		}
		// KSQLの環境変数を取り込み
		{
			if (Objects.nonNull(replaceWordDictionary)) {
				this.replaceWordDictionary.putAll(replaceWordDictionary);
			}
		}
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
				// システム管理下の環境変数でない場合、例外をスロー
				// システム管理下の環境変数は実行計画にて変換
				if (!KagerowScriptAccessor.KAGEROW_ENV_LIST.contains(targetWord)) {
					// エラー詳細構築
					errorMsg = ErrorMessage.CODE_900.getMessage(targetWord);
					startIndex = matcher.start();
					endIndex = matcher.end();
					// 含まれていない場合、未定義置換文字列としてパースエラー
					throw new KSQLParseException(this);
				}
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
		return "#{%s}".formatted(reword);
	}
}

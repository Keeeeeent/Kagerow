package com.sakulabo.core.Processor.log;

import static com.sakulabo.core.Common.StringUtils.*;

import java.lang.System.Logger;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * ライブラリで使用できる共通のロガーです
 * 
 * @author keeeeeent
 */
public final class CommonLogger {

	/** ロガーインスタンス */
	private static final Logger logger = System.getLogger("KagerowAOP");
	/** ログメッセージフォーマット */
	private static final String logFormat = "[%s] %s.%s(%s)%s";

	/**
	 * インスタンス生成禁止
	 */
	private CommonLogger() {
		;
	}

	/**
	 * 共通のロガーを取得します
	 * @return ロガーインスタンス
	 */
	public static final Logger getInstance() {
		return logger;
	}

	/**
	 * 共通メッセージ生成メソッドです
	 * @param prefix ログプレフィックス
	 * @param raw 対象インスタンス
	 * @param result メソッド実行結果
	 * @param method メソッドインスタンス
	 * @param args メソッド実行引数
	 * @return メッセージ
	 */
	public static final String createMsg(String prefix, Object raw, Object result, Method method, Object[] args) {

		if (Objects.isNull(args)) {
			args = new Object[] { NULL_STR };
		}

		StringJoiner joiner = new StringJoiner(BLANK);
		joiner.add(" ->");
		joiner.add(Objects.isNull(result) ? NULL_STR : toClassName(result));

		if (Objects.nonNull(result)) {
			joiner.add(Objects.toString(result));
		}

		Object[] param = new Object[] {
				prefix,
				toClassName(raw),
				method.getName(),
				Arrays.asList(args)
						.stream()
						.map(m -> toClassName(m) + BLANK + m)
						.collect(Collectors.joining(",")),
				joiner.toString() };

		return String.format(logFormat, param);
	}

}

package com.sakulabo.core.Processor.archive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sakulabo.core.Common.ErrorMessage;
import com.sakulabo.core.Common.StringUtils;
import com.sakulabo.core.Kagerow.KagerowApplication;
import com.sakulabo.core.Kagerow.Contents.KagerowVirtualFileContent.KagerowDataType;

/**
 * データ配列からJavaで対応可能なデータフォーマットを推測します
 * 
 * @author keeeeeent
 */
public final class DataTypeHandler {

	/** データタイプ保持メモリ */
	private checker[] dataTypeChecker;

	/**
	 * デフォルトコンストラクタ
	 * @param data 初回データリスト
	 */
	public DataTypeHandler(String[] data) {
		dataTypeChecker = new checker[data.length];
		Arrays.fill(dataTypeChecker, new nullChecker());
		update(data);
	}

	/**
	 * データタイプ更新メソッド
	 * @param data データリスト
	 */
	public void update(String[] data) {

		if (data.length != dataTypeChecker.length) {
			// 配列の要素数が違う場合
			throw new IllegalStateException(ErrorMessage.CODE_007.getMessage());
		}

		// フォーマットチェック処理
		for (int i = 0; i < data.length; i++) {
			checker check = dataTypeChecker[i];
			dataTypeChecker[i] = check.check(data[i]);
		}

	}

	/**
	 * 現時点でのDataTypeを取得します
	 * @return DataType配列
	 */
	public KagerowDataType[] getDataType() {
		KagerowDataType[] result = new KagerowDataType[dataTypeChecker.length];
		for (int i = 0; i < dataTypeChecker.length; i++) {
			result[i] = dataTypeChecker[i].toDataType();
		}
		return result;
	}

	/**
	 * 文字列判定クラスです<br/>
	 * 処理は以下のとおりチェーン方式で行われます
	 * NULL->BOOLEAN->NUMBER(0から始まる文字列の場合)->TIMESTAMP->DATE->VARCHAR<br/>
	 *            ... NUMBER(上記以外)             ->DECIMAL->TIMESTAMP->DATE->VARCHAR
	 * 
	 */
	private interface checker {
		/**
		 * 判定した結果対応するフォーマットではなかった場合、後続のチェッカーへチェーン処理します。
		 * @param target チェック対象
		 * @return チェッカーインスタンス
		 */
		checker check(String target);

		/**
		 * DataTypeを返却します
		 * @return DataType
		 */
		KagerowDataType toDataType();

		/**
		 * nullチェックを行います
		 * @param target 判定対象
		 * @return 判定結果
		 */
		default boolean nullCheck(Object target) {
			return Objects.isNull(target) || Objects.equals(target, StringUtils.EMPTY);
		}
	}

	@SuppressWarnings("javadoc")
	private static class nullChecker implements checker {

		/** {@inheritDoc} */
		@Override
		public checker check(String target) {
			if (nullCheck(target)) {
				return this;
			} else {
				booleanChecker nextCheker = new booleanChecker("true", "false", new integerChecker());
				booleanChecker cheker = new booleanChecker("0", "1", nextCheker);
				return cheker.check(target);
			}
		}

		/** {@inheritDoc} */
		@Override
		public KagerowDataType toDataType() {
			return KagerowDataType.NULL;
		}

	}

	@SuppressWarnings("javadoc")
	private static class booleanChecker implements checker {

		String falsy;
		String truthy;
		checker next;

		booleanChecker(String falsy, String truthy, checker next) {
			booleanChecker.this.falsy = falsy;
			booleanChecker.this.truthy = truthy;
			booleanChecker.this.next = next;
		}

		/** {@inheritDoc} */
		@Override
		public checker check(String target) {
			if (nullCheck(target)) {
				return this;
			} else if (falsy.equalsIgnoreCase(target) || truthy.equalsIgnoreCase(target)) {
				return this;
			} else {
				return next.check(target);
			}
		}

		/** {@inheritDoc} */
		@Override
		public KagerowDataType toDataType() {
			return KagerowDataType.BOOLEAN;
		}

	}

	@SuppressWarnings("javadoc")
	private static class integerChecker implements checker {

		/** アンチパターン */
		static final Pattern pattern = Pattern.compile("^-?0\\d+");
		/** 負の整数パターン */
		static final Pattern negatePattern = Pattern.compile("^-(?<num>\\d*(\\.\\d+)?([eE][-+]?\\d+)?)$");

		/** {@inheritDoc} */
		@Override
		public checker check(String target) {
			if (nullCheck(target)) {
				return this;
			}
			Matcher matcher = pattern.matcher(target);
			if (matcher.find()) {
				dateChecker checker = new dateChecker();
				return checker.check(target);
			} else {
				Matcher negateMatcher = negatePattern.matcher(target);
				try {
					if (negateMatcher.find()) {
						Long.parseLong(negateMatcher.group("num"));
					} else {
						Long.parseLong(target);
					}
					return this;
				} catch (NumberFormatException e) {
					decimalChecker cheker = new decimalChecker();
					return cheker.check(target);
				}
			}
		}

		/** {@inheritDoc} */
		@Override
		public KagerowDataType toDataType() {
			return KagerowDataType.NUMBER;
		}

	}

	@SuppressWarnings("javadoc")
	private static class decimalChecker implements checker {

		/** 負の整数パターン */
		static final Pattern negatePattern = Pattern.compile("^-(?<num>\\d*(\\.\\d+)?([eE][-+]?\\d+)?)$");

		/** {@inheritDoc} */
		@Override
		public checker check(String target) {
			if (nullCheck(target)) {
				return this;
			}
			try {
				Matcher negateMatcher = negatePattern.matcher(target);
				if (negateMatcher.find()) {
					new BigDecimal(negateMatcher.group("num"));
				} else {
					new BigDecimal(target);
				}
				return this;
			} catch (NumberFormatException e) {
				timestampChecker cheker = new timestampChecker();
				return cheker.check(target);
			}
		}

		/** {@inheritDoc} */
		@Override
		public KagerowDataType toDataType() {
			return KagerowDataType.DECIMAL;
		}

	}

	@SuppressWarnings("javadoc")
	private static class timestampChecker implements checker {

		/** {@inheritDoc} */
		@Override
		public checker check(String target) {
			if (nullCheck(target)) {
				return this;
			}
			boolean flug = false;
			for (DateTimeFormatter formater : KagerowApplication.getConfig().KDB_DATE_FORMAT()) {
				try {
					LocalDateTime.parse(target, formater);
					flug = true;
				} catch (DateTimeParseException timeexp) {
				}
			}
			return flug ? this : new dateChecker().check(target);
		}

		/** {@inheritDoc} */
		@Override
		public KagerowDataType toDataType() {
			return KagerowDataType.TIMESTAMP;
		}

	}

	@SuppressWarnings("javadoc")
	private static class dateChecker implements checker {

		/** {@inheritDoc} */
		@Override
		public checker check(String target) {
			if (nullCheck(target)) {
				return this;
			}
			boolean flug = false;
			for (DateTimeFormatter formater : KagerowApplication.getConfig().KDB_DATE_FORMAT()) {
				try {
					LocalDate.parse(target, formater);
					flug = true;
				} catch (DateTimeParseException exp) {
					;
				}
			}
			return flug ? this : new stringChecker().check(target);
		}

		/** {@inheritDoc} */
		@Override
		public KagerowDataType toDataType() {
			return KagerowDataType.DATE;
		}

	}

	@SuppressWarnings("javadoc")
	private static class stringChecker implements checker {

		/** {@inheritDoc} */
		@Override
		public checker check(String target) {
			return this;
		}

		/** {@inheritDoc} */
		@Override
		public KagerowDataType toDataType() {
			return KagerowDataType.VARCHAR;
		}

	}
}

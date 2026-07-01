package com.sakulabo.core.Kagerow.Exception;

import java.util.Objects;

import com.sakulabo.core.Kagerow.Utilities.KagerowKsqlTransformer;

/**
 * KSQLが不正であることを表す検査例外クラスです
 * 
 * @author keeeeeent
 */
public class KSQLParseException extends Exception {

	/** パース失敗位置（開始位置） */
	private final int startIndex;
	/** パース失敗位置（終了位置） */
	private final int endIndex;

	/**
	 * デフォルトコンストラクタ
	 * @param parser パーサーインスタンス
	 */
	public KSQLParseException(KagerowKsqlTransformer parser) {
		super(Objects.requireNonNull(parser).parseLogicError());
		startIndex = parser.getStartIndex();
		endIndex = parser.getEndIndex();
	}

	/**
	 * デフォルトコンストラクタ
	 * @param parser パーサーインスタンス
	 * @param cause 原因例外
	 */
	public KSQLParseException(KagerowKsqlTransformer parser, Exception cause) {
		super(Objects.requireNonNull(parser).parseLogicError(), cause);
		startIndex = parser.getStartIndex();
		endIndex = parser.getEndIndex();
	}

	/**
	 * パース失敗開始位置を取得します
	 * @return パース失敗開始位置
	 */
	public int getStartIndex() {
		return startIndex;
	}

	/**
	 * パース失敗終了位置を取得します
	 * @return パース終了開始位置
	 */
	public int getEndIndex() {
		return endIndex;
	}

}

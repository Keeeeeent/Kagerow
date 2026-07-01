package com.sakulabo.core.Processor.script;

import com.sakulabo.core.Kagerow.Utilities.KagerowKsqlTransformer;
import com.sakulabo.core.Processor.database.AppKSQLCreater;

/**
 * KSQLに対するするスクリプト実装を提供する基底クラスです
 * @author keeeeeent
 */
public non-sealed abstract class BaseKsqlTransformer implements KagerowKsqlTransformer {

	/** エラーの原因となったメッセージを保持するフィールドです */
	protected String errorMsg;

	/** パース失敗位置（開始位置） */
	protected int startIndex;
	/** パース失敗位置（終了位置） */
	protected int endIndex;

	/** {@inheritDoc} */
	@Override
	public String parseLogicError() {
		return errorMsg;
	}

	/** {@inheritDoc} */
	@Override
	public int getStartIndex() {
		return startIndex;
	}

	//** {@inheritDoc} */
	@Override
	public int getEndIndex() {
		return endIndex;
	}

	/**
	 * バイナリー名称をKDBテーブル名に変換します
	 * @param binaryName バイナリ名称
	 * @return　KDBテーブル名
	 */
	public static final String toTabelName(String binaryName) {
		return AppKSQLCreater.toTabelName(binaryName);
	}

}

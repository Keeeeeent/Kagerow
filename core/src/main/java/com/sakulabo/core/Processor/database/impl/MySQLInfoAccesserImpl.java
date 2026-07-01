package com.sakulabo.core.Processor.database.impl;

import com.sakulabo.core.Kagerow.Utilities.KagerowDBMode;
import com.sakulabo.core.Processor.database.AppDBInfoAccesser;

/**
 * MySQLモード向けの情報アクセッサーを生成します
 * 
 * @author keeeeeent
 */
public final class MySQLInfoAccesserImpl extends AppDBInfoAccesser {

	/**
	 * デフォルトコンストラクタ
	 */
	public MySQLInfoAccesserImpl() {
		super(KagerowDBMode.MYSQL);
	}

	/** {@inheritDoc} */
	@Override
	public String isNULL() {
		return "CHAR(1)";
	}

	/** {@inheritDoc} */
	@Override
	public String isBOOLEAN() {
		return "TINYINT";
	}

	/** {@inheritDoc} */
	@Override
	public String isNUMBER(long size) {
		return "BIGINT";
	}

	/** {@inheritDoc} */
	@Override
	public String isDECIMAL(long size) {
		return "DECIMAL";
	}

	/** {@inheritDoc} */
	@Override
	public String isDATE(long size) {
		return "DATE";
	}

	/** {@inheritDoc} */
	@Override
	public String isTIMESTAMP(long size) {
		return "TIMESTAMP";
	}

	/** {@inheritDoc} */
	@Override
	public String isVARCHAR(long size) {
		if (500 < size) {
			return "TEXT";
		} else {
			return "VARCHAR(%d)".formatted(size);
		}
	}

}

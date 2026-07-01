package com.sakulabo.core.Processor.database.impl;

import com.sakulabo.core.Kagerow.Utilities.KagerowDBMode;
import com.sakulabo.core.Processor.database.AppDBInfoAccesser;

/**
 * Oracleモード向けの情報アクセッサーを生成します
 * 
 * @author keeeeeent
 */
public final class OracleInfoAccesserImpl extends AppDBInfoAccesser {

	/**
	 * デフォルトコンストラクタ
	 */
	public OracleInfoAccesserImpl() {
		super(KagerowDBMode.ORACLE);
	}

	/** {@inheritDoc} */
	@Override
	public String isNULL() {
		return "CHAR(1)";
	}

	/** {@inheritDoc} */
	@Override
	public String isBOOLEAN() {
		return "NUMBER(1)";
	}

	/** {@inheritDoc} */
	@Override
	public String isNUMBER(long size) {
		return "NUMBER";
	}

	/** {@inheritDoc} */
	@Override
	public String isDECIMAL(long size) {
		return "NUMBER";
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
			return "CLOB";
		} else {
			return "VARCHAR2(%d)".formatted(size);
		}
	}

}

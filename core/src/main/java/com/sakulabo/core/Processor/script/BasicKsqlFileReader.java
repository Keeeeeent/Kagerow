package com.sakulabo.core.Processor.script;

import java.nio.file.Path;

import com.sakulabo.core.Kagerow.Exception.AppLogicException;
import com.sakulabo.core.Kagerow.Exception.KFileParseException;
import com.sakulabo.core.Kagerow.Exception.KSQLParseException;

/**
 * Kagerowスクリプトファイル解析クラスです
 * 
 * @author keeeeeent
 */
public final class BasicKsqlFileReader extends KsqlFileReader {

	/**
	 * デフォルトコンストラクタ
	 * @param path 解析対象
	 * @param strategy 読み取りストラテジー
	 * @throws KFileParseException KFile解析エラー
	 * @throws KSQLParseException KSQL解析エラー
	 * @throws AppLogicException アプリケーションロジック不正
	 */
	public BasicKsqlFileReader(Path path, KsqlReaderStrategy strategy)
			throws KFileParseException, KSQLParseException, AppLogicException {
		super(path, strategy);
	}

	/** {@inheritDoc} */
	@Override
	protected String parseEnv(String target) throws KSQLParseException {
		return envParser.transform(target);
	}

	/** {@inheritDoc} */
	@Override
	protected String longParseEnv(String target) throws KSQLParseException {
		return envParser.transform(target);
	}

}

package com.sakulabo.core.Processor.script;

import java.nio.file.Path;

import com.sakulabo.core.Kagerow.Exception.KFileParseException;
import com.sakulabo.core.Kagerow.Utilities.KagerowScriptAccessor;

/**
 * Kagerowスクリプトファイル生成クラスです
 * 
 * @author keeeeeent
 */
public final class BasicKsqlFileWriter extends KsqlFileWriter {

	/**
	 * デフォルトコンストラクタ
	 * @param scriptAccessor スクリプトインスタンス
	 * @param path 出力先
	 * @param strategy 書き込みストラテジー
	 */
	public BasicKsqlFileWriter(KagerowScriptAccessor scriptAccessor, Path path, KsqlWriterStrategy strategy) {
		super(scriptAccessor, path, strategy);
	}

	/** {@inheritDoc} */
	@Override
	protected String parse(String target) throws KFileParseException {
		return target;
	}

	/** {@inheritDoc} */
	@Override
	protected String longParse(String target) throws KFileParseException {
		return target;
	}

}

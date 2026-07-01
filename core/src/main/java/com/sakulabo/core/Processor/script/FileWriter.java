package com.sakulabo.core.Processor.script;

import java.nio.file.Path;

import com.sakulabo.core.Kagerow.Exception.AppLogicException;
import com.sakulabo.core.Kagerow.Exception.KFileParseException;

/**
 * スクリプトファイル共通で使用される書込基底クラスです
 * 
 * @author keeeeeent
 */
public abstract sealed class FileWriter extends KFile permits KsqlFileWriter {

	// ###########################################################################
	// # 共通
	// ###########################################################################

	/** 出力先 */
	protected final Path path;

	/**
	 * デフォルトコンストラクタ
	 * @param path 出力先
	 */
	public FileWriter(Path path) {
		this.path = path;
	}

	/**
	 * XMLを出力します
	 * @throws KFileParseException KFile解析エラー
	 * @throws AppLogicException アプリケーションロジック不正
	 */
	public abstract void outputXML() throws KFileParseException, AppLogicException;

}

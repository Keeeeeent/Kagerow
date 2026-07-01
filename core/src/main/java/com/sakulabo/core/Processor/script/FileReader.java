package com.sakulabo.core.Processor.script;

import java.nio.file.Path;

/**
 * スクリプトファイル共通で使用される読込基底クラスです
 * 
 * @author keeeeeent
 */
public abstract sealed class FileReader extends KFile permits KsqlFileReader {

	// ###########################################################################
	// # 共通
	// ###########################################################################

	/** 解析対象 */
	protected final Path path;

	/**
	 * デフォルトコンストラクタ
	 * @param path 解析対象
	 */
	public FileReader(Path path) {
		this.path = path;
	}

	// ###########################################################################
	// # ルート要素
	// ###########################################################################

	/** root要素 */
	protected static final String KAGEROW_SCRIPT = "/kagerow-script";

	// ###########################################################################
	// # ENV要素
	// ###########################################################################

	/** 環境変数要素（環境変数root要素） */
	protected static final String ENV_ROOT_PATH = KAGEROW_SCRIPT.concat("/environment");

	// ###########################################################################
	// # コンフィグレーション要素
	// ###########################################################################

	/** コンフィグレーション要素（スクリプト名称） */
	protected static final String CONFIGURATION_NAME = KAGEROW_SCRIPT.concat("/configuration/name");
	/** コンフィグレーション要素（スクリプト概要） */
	protected static final String CONFIGURATION_SUMMARY = KAGEROW_SCRIPT.concat("/configuration/summary");
	/** コンフィグレーション要素（スクリプト実行モード） */
	protected static final String CONFIGURATION_MODE = KAGEROW_SCRIPT.concat("/configuration/mode");
	/** コンフィグレーション要素（スクリプトカレントスキーマ） */
	protected static final String CONFIGURATION_SCHEMA = KAGEROW_SCRIPT.concat("/configuration/schema");

	/** コンフィグレーション要素（スクリプトキャッシュID） */
	protected static final String CONFIGURATION_CACHE = KAGEROW_SCRIPT.concat("/configuration/cache");

	// ###########################################################################
	// # プラグイン要素
	// ###########################################################################

	/** プラグイン要素（入力プラグインroot要素） */
	protected static final String INPUT_PLUGINS = KAGEROW_SCRIPT.concat("/plugins/input");
	/** プラグイン要素（出力プラグインroot要素） */
	protected static final String OUTPUT_PLUGINS = KAGEROW_SCRIPT.concat("/plugins/output");

	// ###########################################################################
	// # KSQL要素
	// ###########################################################################

	/** KSQL要素（KSQLroot要素） */
	protected static final String KSQLS = KAGEROW_SCRIPT.concat("/ksqls");

	// ###########################################################################
	// # コマンド要素
	// ###########################################################################

	/** コマンド要素（コマンドroot要素） */
	protected static final String CMDS = KAGEROW_SCRIPT.concat("/command");

}

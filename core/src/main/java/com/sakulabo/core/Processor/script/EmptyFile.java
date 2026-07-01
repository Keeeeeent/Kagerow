package com.sakulabo.core.Processor.script;

import java.util.Objects;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import com.sakulabo.core.Common.StringUtils;
import com.sakulabo.core.Kagerow.Utilities.KagerowDBMode;
import com.sakulabo.core.Kagerow.Utilities.KagerowFileVersion;

/**
 * スクリプトで使用される初期クラスです
 * 
 * @author keeeeeent
 */
public final class EmptyFile extends KFile {

	/**
	 * デフォルトコンストラクタ
	 * @param name スクリプト名称
	 * @param summary スクリプト概要
	 * @param mode スクリプト実行モード
	 * @param schema スクリプトカレントスキーマ
	 * @param version スクリプトバージョン
	 */
	public EmptyFile(String name, String summary, KagerowDBMode mode, String schema, KagerowFileVersion version) {
		// 変換後フィールド初期化
		super.name = Objects.requireNonNull(name);
		super.version = Objects.requireNonNull(version);
		super.summary = Objects.requireNonNull(summary);
		super.mode = Objects.requireNonNull(mode);
		super.schema = Objects.requireNonNull(schema);
		super.inputPlugins = new Vector<>();
		super.outputPlugins = new Vector<>();
		super.ksqls = new Vector<>();
		super.command = new Vector<>();
		super.env = new ConcurrentHashMap<>();
		super.cacheId = StringUtils.DEFAULT;
		// 変換前フィールド初期化
		super.rawname = name;
		super.rawversion = version.toString();
		super.rawsummary = summary;
		super.rawmode = mode.toString();
		super.rawschema = schema;
		super.rawinputPlugins = new Vector<>();
		super.rawoutputPlugins = new Vector<>();
		super.rawksqls = new Vector<>();
		super.rawcommand = new Vector<>();
		super.rawcacheId = StringUtils.DEFAULT;
	}

}

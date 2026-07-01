package com.sakulabo.application.model.Script;

import java.nio.file.Path;

import com.sakulabo.application.model.BaseModel;
import com.sakulabo.core.Kagerow.Adapter.KagerowExecutionPlanAdapter;
import com.sakulabo.core.Kagerow.Utilities.KagerowScriptAccessor;

/**
 * KSQL実行モデルクラスです
 * 
 * @author keeeeeent
 */
public final class KSQLScriptModel extends BaseModel {

	/** スクリプト物理パス */
	public volatile Path path;
	/** 実行計画アクセッサー */
	public volatile KagerowExecutionPlanAdapter planAdapter;
	/** セキュア実行フラグ */
	public volatile boolean isSecure;
	/** スクリプトアクセッサー */
	public volatile KagerowScriptAccessor scriptAccessor;

}

package com.sakulabo.core.Processor.database;

import java.nio.file.Path;

import com.sakulabo.core.Kagerow.KagerowApplication;
import com.sakulabo.core.Kagerow.Utilities.KagerowConfiguration;
import com.sakulabo.core.Kagerow.Utilities.KagerowDBMode;

/**
 * クエリ専用接続用URL生成クラスです
 * 
 * @author keeeeeent
 */
public class QueryKDBUriCreater extends DefaultKDBUriCreater {

	/** クエリ専用SQL実行の最大メモリキャッシュサイズ */
	private static final int KDB_QUERY_URL_CACHE_SIZE;
	/** クエリ専用SQL実行の最大メモリページサイズ */
	private static final int KDB_QUERY_URL_PAGE_SIZE;
	static {
		KagerowConfiguration config = KagerowApplication.getConfig();
		KDB_QUERY_URL_CACHE_SIZE = config.KDB_QUERY_URL_CACHE_SIZE();
		KDB_QUERY_URL_PAGE_SIZE = config.KDB_QUERY_URL_PAGE_SIZE();
	}

	/** オプション */
	private static final String OPTION;
	static {
		OPTION = String.format(
				";CACHE_SIZE=%d;PAGE_SIZE=%d;",
				KDB_QUERY_URL_CACHE_SIZE,
				KDB_QUERY_URL_PAGE_SIZE);
	}

	/**
	 * デフォルトコンストラクタ
	 * @param mode KDB起動モード
	 * @param path KDB物理ファイルパス
	 */
	public QueryKDBUriCreater(KagerowDBMode mode, Path path) {
		super(mode, path);
	}

	/** {@inheritDoc} */
	@Override
	String option() {
		return OPTION;
	}

}

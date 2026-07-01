package com.sakulabo.core.Kagerow.Utilities;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.sakulabo.core.Common.StringUtils;
import com.sakulabo.core.Kagerow.Exception.AppLogicException;
import com.sakulabo.core.Kagerow.Exception.KFileParseException;
import com.sakulabo.core.Kagerow.Exception.KSQLParseException;
import com.sakulabo.core.Processor.script.BasicKsqlFileReader;
import com.sakulabo.core.Processor.script.BasicKsqlFileWriter;
import com.sakulabo.core.Processor.script.EmptyFile;
import com.sakulabo.core.Processor.script.KFile;
import com.sakulabo.core.Processor.script.KFile.KagerowCmdAccessorImpl;
import com.sakulabo.core.Processor.script.KFile.KagerowPluginAccessorImpl;
import com.sakulabo.core.Processor.script.KFile.KagerowSqlAccessorImpl;
import com.sakulabo.core.Processor.script.KsqlFileBuilderFactory;

/**
 * Kagerowスクリプトファイル共通で使用される解析結果アクセッサインターフェイスです
 * 
 * @author keeeeeent
 */
public sealed interface KagerowScriptAccessor permits KFile {

	/** KagerowスクリプトMineType */
	public static final String KAGEROW_BASIC_MINE_TYPE = "kagerow/basic-script";
	/** KagerowセキュアスクリプトMineType */
	public static final String KAGEROW_SECURE_MINE_TYPE = "kagerow/secure-script";

	/** KagerowスクリプトMineType一覧 */
	public static final List<String> KAGEROW_MINE_TYPE_LSIT = List.of(
			KagerowScriptAccessor.KAGEROW_BASIC_MINE_TYPE,
			KagerowScriptAccessor.KAGEROW_SECURE_MINE_TYPE);

	/** セッションIDスクリプト変数検索キー */
	public static final String KAGEROW_SESSION_ID_ENV_KYE = "k_session_id";
	/** 一時フォルダスクリプト変数検索キー */
	public static final String KAGEROW_TMP_DIR_ENV_KYE = "k_tmp_dir";
	/** 実行環境フォルダスクリプト変数検索キー */
	public static final String KAGEROW_RANTIME_DIR_ENV_KYE = "k_runtime_dir";
	/** スクリプト名称スクリプト変数検索キー */
	public static final String KAGEROW_SCRIPT_NAME_ENV_KYE = "k_script_name";
	/** スクイプと実行モードスクリプト変数検索キー */
	public static final String KAGEROW_EXE_MODE_ENV_KYE = "k_script_exe_mode";
	/** カレントスキーマスクリプト変数検索キー */
	public static final String KAGEROW_SCHEMA_ENV_KYE = "k_schema";
	/** 実行スクリプトスクリプト変数検索キー */
	public static final String KAGEROW_SCRIPT_FILE_PATH_ENV_KYE = "k_script_file_path";
	/** タイムスタンプ変数検索キー */
	public static final String KAGEROW_TIMESTAMP_ENV_KYE = "k_timestamp";
	/** タイムスタンプフォーマット変数検索キー */
	public static final String KAGEROW_TIMESTAMP_FORMAT_ENV_KYE = "k_timestamp_format";

	/** システム管理環境変数一覧リスト */
	public static final List<String> KAGEROW_ENV_LIST = List.of(
			KAGEROW_SESSION_ID_ENV_KYE,
			KAGEROW_TMP_DIR_ENV_KYE,
			KAGEROW_RANTIME_DIR_ENV_KYE,
			KAGEROW_SCRIPT_NAME_ENV_KYE,
			KAGEROW_EXE_MODE_ENV_KYE,
			KAGEROW_SCHEMA_ENV_KYE,
			KAGEROW_SCRIPT_FILE_PATH_ENV_KYE,
			KAGEROW_TIMESTAMP_ENV_KYE,
			KAGEROW_TIMESTAMP_FORMAT_ENV_KYE);

	/**
	 * 指定されたパスがKagerowMineType対応か判定します
	 * @param target 対象パス
	 * @return 判定結果
	 * @throws IOException 
	 */
	public static boolean isKagerowFile(Path target) throws IOException {
		String contentTypes = Files.probeContentType(target);
		if (Objects.nonNull(contentTypes)) {
			return KagerowScriptAccessor.KAGEROW_MINE_TYPE_LSIT.contains(contentTypes);
		}
		return false;
	}

	/**
	 * スクリプトがキャッシュに依存しているか判定します
	 * @param scriptAccessor アクセッサインスタンス
	 * @return 判定結果
	 */
	public static boolean isCached(KagerowScriptAccessor scriptAccessor) {
		if (Objects.isNull(scriptAccessor)) {
			return false;
		}
		if (StringUtils.DEFAULT.equals(scriptAccessor.getCacheId())) {
			return false;
		}
		return true;
	}

	/**
	 * 通常スクリプトの読み込みを行い、アクセッサを返却します
	 * @param scriptFile スクリプトファイルパス
	 * @return アクセッサインスタンス
	 * @throws KFileParseException KFile解析エラー
	 * @throws KSQLParseException KSQL解析エラー
	 * @throws AppLogicException アプリケーションロジック不正
	 */
	public static KagerowScriptAccessor getInstance(Path scriptFile)
			throws KFileParseException, KSQLParseException, AppLogicException {
		KagerowFileVersion version = KagerowFileVersion.fromFile(scriptFile);
		KsqlFileBuilderFactory factory = KsqlFileBuilderFactory.getInstance(version, scriptFile);
		BasicKsqlFileReader reader = factory.newBasicReader();
		return reader;
	}

	/**
	 * 通常スクリプトのファイルへの書き込みを行います
	 * @param scriptAccessor アクセッサインスタンス
	 * @param outputPath 出力先
	 * @throws KFileParseException 変換失敗
	 * @throws AppLogicException アプリケーションロジック不正
	 */
	public static void toFile(KagerowScriptAccessor scriptAccessor, Path outputPath)
			throws KFileParseException, AppLogicException {
		KsqlFileBuilderFactory factory = KsqlFileBuilderFactory.getInstance(scriptAccessor.getVersion(), outputPath);
		BasicKsqlFileWriter writer = factory.newBasicWriter(scriptAccessor);
		writer.outputXML();
	}

	/**
	 * 空のアクセッサを返却します
	 * @param name スクリプト名称
	 * @param summary スクリプト概要
	 * @param mode スクリプト実行モード
	 * @param schema スクリプトカレントスキーマ
	 * @param version スクリプトバージョン
	 * @return 空のアクセッサ
	 */
	public static KagerowScriptAccessor getInstance(String name, String summary, KagerowDBMode mode, String schema,
			KagerowFileVersion version) {
		return new EmptyFile(name, summary, mode, schema, version);
	}

	/**
	 * 空のアクセッサを返却します
	 * @param name スクリプト名称
	 * @param summary スクリプト概要
	 * @param mode スクリプト実行モード
	 * @param schema スクリプトカレントスキーマ
	 * @return 空のアクセッサ
	 */
	public static KagerowScriptAccessor getInstance(String name, String summary, KagerowDBMode mode, String schema) {
		return getInstance(name, summary, mode, schema, KagerowFileVersion.latestSupported());
	}

	/**
	 * KagerowPluginAccessorの初期インスタンスを生成します
	 * @param pkg プラグインパッケージ名称
	 * @param name プラグイン名称
	 * @param id プラグインID
	 * @param next 後続処理を行うプラグインID
	 * @return 初期インスタンス
	 */
	public KagerowPluginAccessor newKagerowPluginAccessor(String pkg, String name, String id, String next);

	/**
	 * KagerowSqlAccessorの初期インスタンスを生成します
	 * @param name プラグイン名称
	 * @param id プラグインID
	 * @param next 後続処理を行うプラグインID
	 * @param sql KSQLID
	 * @return 初期インスタンス
	 */
	public KagerowSqlAccessor newKagerowSqlAccessor(String name, String id, String next, String sql);

	/**
	 * KagerowSqlAccessorの初期インスタンスを生成します
	 * @param cmd コマンド
	 * @param mode コマンド実行モード
	 * @return 初期インスタンス
	 */
	public KagerowCmdAccessor newKagerowCmdAccessor(String cmd, KagerowCommandMode mode);

	/**
	 * プラグインに対するアクセスを提供するインターフェイスです
	 */
	public sealed interface KagerowPluginAccessor permits KagerowPluginAccessorImpl {

		/** プラグインIDパラメータ検索キー */
		public static final String PLUGIN_ID_PARAM_KEY = "plugin_id";
		/** プラグイン名称パラメータ検索キー */
		public static final String PLUGIN_NAME_PARAM_KEY = "plugin_name";

		/**
		 * プラグインパッケージ名称を取得します
		 * @return プラグインパッケージ名称
		 */
		String getPackageName();

		/**
		 * プラグインバージョンを取得します
		 * @return プラグインバージョン
		 */
		String getVersion();

		/**
		 * プラグイン名称を取得します
		 * @return プラグイン名称
		 */
		String getName();

		/**
		 * スクリプトによりプラグインに関連づけられた固有のIDを取得します
		 * @return ID
		 */
		String getId();

		/**
		 * スクリプトにより関連づけられた後続処理が存在するか判定します
		 * @return 判定結果
		 */
		boolean hasNext();

		/**
		 * スクリプトにより関連づけられた後続処理を行う、プラグインのIDを返却します
		 * @return 後続処理を行うプラグインのID
		 */
		String next();

		/**
		 * プラグインで使用可能なパラメータ一覧を取得します
		 * @return パラメータ一覧
		 */
		Map<String, String> getParam();

		/**
		 * プラグインで使用可能なパラメータ一覧を取得します
		 * @param newParam パラメータ一覧
		 * @return 新しい世代のmementoインスタンス
		 */
		KagerowPluginAccessor setParam(Map<String, String> newParam);

		/**
		 * プラグインパッケージ名称を設定します
		 * @param pkg 新たなプラグインパッケージ名称 
		 * @return 新しい世代のmementoインスタンス
		 */
		KagerowPluginAccessor setPackageName(String pkg);

		/**
		 * プラグインバージョンを設定します
		 * @param version 新たなプラグインバージョン 
		 * @return 新しい世代のmementoインスタンス
		 */
		KagerowPluginAccessor setVersion(String version);

		/**
		 * プラグイン名称を設定します
		 * @param name 新たなプラグイン名称 
		 * @return 新しい世代のmementoインスタンス
		 */
		KagerowPluginAccessor setName(String name);

		/**
		 * スクリプトによりプラグインに関連づけられた固有のIDを設定します
		 * @param id 新たなID
		 * @return 新しい世代のmementoインスタンス
		 */
		KagerowPluginAccessor setId(String id);

		/**
		 * スクリプトにより関連づけられた後続処理を行う、プラグインのIDを設定します
		 * @param next 新たな後続処理を行うプラグインのID
		 * @return 新しい世代のmementoインスタンス
		 */
		KagerowPluginAccessor setNext(String next);

		/**
		 * クエリで指定したインスタンスと同等のインスタンスをクローンされたアクセッサーから取得します
		 * @param accessor アクセッサ
		 * @return 取得結果
		 */
		public default KagerowPluginAccessor search(KagerowScriptAccessor accessor) {
			KFile kfile = (KFile) accessor;
			KagerowPluginAccessorImpl impl = (KagerowPluginAccessorImpl) this;
			try {
				return impl.search(kfile);
			} catch (CloneNotSupportedException e) {
				KagerowLogger.newAppLogger().err(e);
				return this;
			}
		}

		/**
		 * 対象が同じ始祖を保有するか判定します
		 * @param targetPlugin 対象
		 * @return 判定結果
		 */
		boolean isSame(KagerowPluginAccessor targetPlugin);

	}

	/**
	 * KSQLに対するアクセスを提供するインターフェイスです
	 */
	public sealed interface KagerowSqlAccessor permits KagerowSqlAccessorImpl {

		/** KSQLIDパラメータ検索キー */
		public static final String KSQL_ID_PARAM_KEY = "ksql_id";
		/** KSQL名称パラメータ検索キー */
		public static final String KSQL_NAME_PARAM_KEY = "ksql_name";

		/**
		 * スクリプトによりKSQLに関連づけられた名称を取得します
		 * @return 名称
		 */
		String getName();

		/**
		 * スクリプトによりプラグインに関連づけられた固有のIDを取得します
		 * @return ID
		 */
		String getId();

		/**
		 * スクリプトにより関連づけられた後続処理が存在するか判定します
		 * @return 判定結果
		 */
		boolean hasNext();

		/**
		 * スクリプトにより関連づけられた後続処理を行う、KSQLのIDを返却します
		 * @return 後続処理を行うプラグインのID
		 */
		String next();

		/**
		 * KSQLで使用可能な変数宣言一覧を取得します
		 * @return 変数宣言一覧
		 */
		Map<String, String> variable();

		/**
		 * KSQLで使用可能な変数宣言一覧を設定します
		 * @param newvariable 変数宣言一覧
		 * @return 新しい世代のmementoインスタンス
		 */
		KagerowSqlAccessor variable(Map<String, String> newvariable);

		/**
		 * KSQL本体を取得します
		 * @return KSQL
		 */
		String getSql();

		/**
		 * プラグイン名称を設定します
		 * @param name 新たなプラグイン名称 
		 * @return 新しい世代のmementoインスタンス
		 */
		KagerowSqlAccessor setName(String name);

		/**
		 * スクリプトによりプラグインに関連づけられた固有のIDを設定します
		 * @param id 新たなID
		 * @return 新しい世代のmementoインスタンス
		 */
		KagerowSqlAccessor setId(String id);

		/**
		 * スクリプトにより関連づけられた後続処理を行う、プラグインのIDを設定します
		 * @param next 新たな後続処理を行うプラグインのID
		 * @return 新しい世代のmementoインスタンス
		 */
		KagerowSqlAccessor setNext(String next);

		/**
		 * KSQL本体を設定します
		 * @param sql 新たなSQL
		 * @return 新しい世代のmementoインスタンス
		 */
		KagerowSqlAccessor setSql(String sql);

		/**
		 * クエリで指定したインスタンスと同等のインスタンスをクローンされたアクセッサーから取得します
		 * @param accessor アクセッサ
		 * @return 取得結果
		 */
		public default KagerowSqlAccessor search(KagerowScriptAccessor accessor) {
			KFile kfile = (KFile) accessor;
			KagerowSqlAccessorImpl impl = (KagerowSqlAccessorImpl) this;
			return impl.search(kfile);
		}

		/**
		 * 対象が同じ始祖を保有するか判定します
		 * @param targetKsql 対象
		 * @return 判定結果
		 */
		boolean isSame(KagerowSqlAccessor targetKsql);

	}

	/**
	 * コマンドに対するアクセスを提供するインターフェイスです
	 */
	public sealed interface KagerowCmdAccessor permits KagerowCmdAccessorImpl {

		/**
		 * 実行コマンドで使用可能な環境変数宣言一覧を取得します
		 * @return 変数宣言一覧
		 */
		Map<String, String> environmental();

		/**
		 * 実行コマンドで使用可能な環境変数宣言一覧を設定します
		 * @param newEnvironmental 変数宣言一覧
		 * @return 新しい世代のmementoインスタンス
		 */
		KagerowCmdAccessor environmental(Map<String, String> newEnvironmental);

		/**
		 * コマンド実行モードを取得します
		 * @return モード
		 */
		KagerowCommandMode getMode();

		/**
		 * コマンド本体を取得します
		 * @return コマンド
		 */
		String getCmd();

		/**
		 * コマンド実行モードを設定します
		 * @param mode 新たなモード
		 * @return 新しい世代のmementoインスタンス
		 */
		KagerowCmdAccessor setMode(KagerowCommandMode mode);

		/**
		 * コマンド本体を設定します
		 * @param cmd 新たなコマンド
		 * @return 新しい世代のmementoインスタンス
		 */
		KagerowCmdAccessor setCmd(String cmd);

		/**
		 * 空のコマンドアクセスインスタンスを生成します
		 * @param kagerowScriptAccessor 解析結果アクセッサインスタンス
		 * @return コマンドに対するアクセス実装インスタンス
		 */
		public static KagerowCmdAccessor createEmptyInstance(KagerowScriptAccessor kagerowScriptAccessor) {
			return ((KFile) kagerowScriptAccessor).new KagerowCmdAccessorImpl();
		}

		/**
		 * 対象が同じ始祖を保有するか判定します
		 * @param cmd 対象
		 * @return 判定結果
		 */
		boolean isSame(KagerowCmdAccessor cmd);

	}

	/**
	 * スクリプトの名称を取得します
	 * @return スクリプト名称
	 */
	String getName();

	/**
	 * スクリプトのバージョン情報を取得します
	 * @return スクリプトバージョン情報
	 */
	KagerowFileVersion getVersion();

	/**
	 * スクリプトの状態を取得します
	 * @return スクリプト状態
	 */
	KagerowFileState getState();

	/**
	 * スクリプトの概要を取得します
	 * @return 概要
	 */
	String getSummary();

	/**
	 * スクリプトの実行モードを取得します
	 * @return 実行モード
	 */
	KagerowDBMode getMode();

	/**
	 * スクリプトを実行する際のカレントスキーマを取得します
	 * @return カレントスキーマ
	 */
	String getSchema();

	/**
	 * スクリプトで使用されるキャッシュIDを取得します
	 * @return キャッシュID
	 */
	String getCacheId();

	/**
	 * プラグインで使用可能な環境変数一覧を取得します
	 * @return 環境変数一覧
	 */
	Map<String, String> getEnv();

	/**
	 * プラグインで使用可能な環境変数一覧を設定します
	 * @param newEnv 環境変数一覧
	 * @return 新しい世代のmementoインスタンス
	 */
	KagerowScriptAccessor setEnv(Map<String, String> newEnv);

	/**
	 * 入力プラグイン一覧を取得します
	 * @return プラグイン一覧
	 */
	List<KagerowPluginAccessor> getInputPlugins();

	/**
	 * 入力プラグイン一覧を設定します
	 * @param newPlugins 新しいプラグイン一覧リスト
	 * @return クローンされたインスタンス
	 */
	KagerowScriptAccessor setInputPlugins(List<KagerowPluginAccessor> newPlugins);

	/**
	 * 出力プラグイン一覧を取得します
	 * @return プラグイン一覧
	 */
	List<KagerowPluginAccessor> getOutputPlugins();

	/**
	 * 出力プラグイン一覧を設定します
	 * @param newPlugins 新しいプラグイン一覧リスト
	 * @return クローンされたインスタンス
	 */
	KagerowScriptAccessor setOutputPlugins(List<KagerowPluginAccessor> newPlugins);

	/**
	 * スクリプトに対してプラグインが関連付けされているか判定します
	 * @return 判定結果
	 */
	boolean hasPlugin();

	/**
	 * スクリプトに関連づけられたKSQL一覧を取得します
	 * @return KSQL一覧
	 */
	List<KagerowSqlAccessor> getKsqls();

	/**
	 * スクリプトに関連づけられたKSQL一覧を設定します
	 * @param ksqls 新しいKSQL一覧リスト
	 * @return クローンされたインスタンス
	 */
	KagerowScriptAccessor setKsqls(List<KagerowSqlAccessor> ksqls);

	/**
	 * スクリプトに関連づけられたコマンド一覧を取得します
	 * @return コマンド一覧
	 */
	List<KagerowCmdAccessor> getCommand();

	/**
	 * スクリプトに関連づけられたコマンド一覧を設定します
	 * @param cmds 新しいコマンド一覧リスト
	 * @return クローンされたインスタンス
	 */
	KagerowScriptAccessor setCommand(List<KagerowCmdAccessor> cmds);

	/**
	 * スクリプトに対してコマンドが関連付けされているか判定します
	 * @return 判定結果
	 */
	boolean hasCmd();

	/**
	 * スクリプトの名称を設定します
	 * @param name 新たなスクリプト名称
	 * @return 新しい世代のmementoインスタンス
	 */
	KagerowScriptAccessor setName(String name);

	/**
	 * スクリプトのバージョンを設定します
	 * @param version 新たなバージョン
	 * @return 新しい世代のmementoインスタンス
	 */
	KagerowScriptAccessor setVersion(KagerowFileVersion version);

	/**
	 * スクリプトの概要を設定します
	 * @param summary 新たな概要
	 * @return 新しい世代のmementoインスタンス
	 */
	KagerowScriptAccessor setSummary(String summary);

	/**
	 * スクリプトの実行モードを設定します
	 * @param mode 新たな実行モード
	 * @return 新しい世代のmementoインスタンス
	 */
	KagerowScriptAccessor setMode(KagerowDBMode mode);

	/**
	 * スクリプトを実行する際のカレントスキーマを設定します
	 * @param schema 新たなカレントスキーマ
	 * @return 新しい世代のmementoインスタンス
	 */
	KagerowScriptAccessor setSchema(String schema);

	/**
	 * スクリプトで使用されるキャッシュIDを設定します
	 * @param cacheId 新たなキャッシュID
	 * @return 新しい世代のmementoインスタンス
	 */
	KagerowScriptAccessor setCacheId(String cacheId);

	/**
	 * 変換前スクリプトの名称を取得します
	 * @return スクリプト名称
	 */
	String getRawName();

	/**
	 * 変換前スクリプトのバージョンを取得します
	 * @return バージョン名称
	 */
	String getRawVersion();

	/**
	 * 変換前スクリプトの概要を取得します
	 * @return 概要
	 */
	String getRawSummary();

	/**
	 * 変換前スクリプトを実行する際のカレントスキーマを取得します
	 * @return カレントスキーマ
	 */
	String getRawSchema();

	/**
	 * 変換前スクリプトで使用されるキャッシュIDを取得します
	 * @return キャッシュID
	 */
	String getRawCacheId();

	/**
	 * 変換前スクリプトの実行モードを取得します
	 * @return 実行モード
	 */
	String getRawMode();

	/**
	 * 変換前入力プラグイン一覧を取得します
	 * @return プラグイン一覧
	 */
	List<KagerowPluginAccessor> getRawInputPlugins();

	/**
	 * 変換前入力プラグイン一覧を設定します
	 * @param newPlugins 新しいプラグイン一覧リスト
	 * @return クローンされたインスタンス
	 */
	KagerowScriptAccessor setRawInputPlugins(List<KagerowPluginAccessor> newPlugins);

	/**
	 * 変換前出力プラグイン一覧を取得します
	 * @return プラグイン一覧
	 */
	List<KagerowPluginAccessor> getRawOutputPlugins();

	/**
	 * 変換前出力プラグイン一覧を設定します
	 * @param newPlugins 新しいプラグイン一覧リスト
	 * @return クローンされたインスタンス
	 */
	KagerowScriptAccessor setRawOutputPlugins(List<KagerowPluginAccessor> newPlugins);

	/**
	 * 変換前スクリプトに関連づけられたKSQL一覧を取得します
	 * @return KSQL一覧
	 */
	List<KagerowSqlAccessor> getRawKsqls();

	/**
	 * 変換前スクリプトに関連づけられたKSQL一覧を設定します
	 * @param ksqls 新しいKSQL一覧リスト
	 * @return クローンされたインスタンス
	 */
	KagerowScriptAccessor setRawKsqls(List<KagerowSqlAccessor> ksqls);

	/**
	 * 変換前スクリプトに関連づけられたコマンド一覧を取得します
	 * @return コマンド一覧
	 */
	List<KagerowCmdAccessor> getRawCommand();

	/**
	 * 変換前スクリプトに関連づけられたコマンド一覧を設定します
	 * @param cmds 新しいコマンド一覧リスト
	 * @return クローンされたインスタンス
	 */
	KagerowScriptAccessor setRawCommand(List<KagerowCmdAccessor> cmds);

	/**
	 * 変換前スクリプトの名称を取得します
	 * @param name 新たなスクリプト名称
	 * @return 新しい世代のmementoインスタンス
	 */
	KagerowScriptAccessor setRawName(String name);

	/**
	 * 変換前スクリプトのバージョンを取得します
	 * @param version 新たなスクリプトバージョン
	 * @return 新しい世代のmementoインスタンス
	 */
	KagerowScriptAccessor setRawVersion(String version);

	/**
	 * 変換前スクリプトの概要を取得します
	 * @param summary 新たな概要
	 * @return 新しい世代のmementoインスタンス
	 */
	KagerowScriptAccessor setRawSummary(String summary);

	/**
	 * 変換前スクリプトを実行する際のカレントスキーマを取得します
	 * @param schema 新たなカレントスキーマ
	 * @return 新しい世代のmementoインスタンス
	 */
	KagerowScriptAccessor setRawSchema(String schema);

	/**
	 * 変換前スクリプトで使用されるキャッシュIDを取得します
	 * @param cacheId 新たなキャッシュID
	 * @return 新しい世代のmementoインスタンス
	 */
	KagerowScriptAccessor setRawCacheId(String cacheId);

	/**
	 * 変換前スクリプトの実行モードを取得します
	 * @param mode 新たな実行モード
	 * @return 新しい世代のmementoインスタンス
	 */
	KagerowScriptAccessor setRawMode(String mode);

}

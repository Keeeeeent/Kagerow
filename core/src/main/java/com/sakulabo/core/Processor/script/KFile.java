package com.sakulabo.core.Processor.script;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xml.sax.SAXParseException;

import com.sakulabo.core.Common.AppPathUtils;
import com.sakulabo.core.Common.ErrorMessage;
import com.sakulabo.core.Common.StringUtils;
import com.sakulabo.core.Kagerow.Exception.KFileParseException;
import com.sakulabo.core.Kagerow.Exception.KSQLParseException;
import com.sakulabo.core.Kagerow.Utilities.KagerowCommandMode;
import com.sakulabo.core.Kagerow.Utilities.KagerowDBMode;
import com.sakulabo.core.Kagerow.Utilities.KagerowFileState;
import com.sakulabo.core.Kagerow.Utilities.KagerowFileVersion;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;
import com.sakulabo.core.Kagerow.Utilities.KagerowScriptAccessor;
import com.sakulabo.core.Processor.log.AppLogMessage;
import com.sakulabo.core.Processor.manager.state.StateManager;
import com.sakulabo.core.Processor.plan.ExecutionPlan;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Kagerowスクリプトファイル規定クラスです
 * 
 * @author keeeeeent
 */
public abstract sealed class KFile
		implements KagerowScriptAccessor, Cloneable
		permits FileReader, FileWriter, EmptyFile {

	// ###########################################################################
	// # 共通
	// ###########################################################################

	/** タイムスタンプデフォルトフォーマット */
	protected static final String TIMESTAMP_DEFAULT_FORMAT = "yyyyMMddHHmmSSS";
	/** XSD規定クラスパス */
	protected static final String XSD_DIR_NAME = "/config/xsd/";

	/** ルート要素 */
	protected static final String ROOT = "kagerow-script";

	/** スクリプト名称保持フィールド */
	protected String name;
	/** スクリプトバージョン保持フィールド */
	protected KagerowFileVersion version;
	/** スクリプト概要保持フィールド */
	protected String summary;
	/** スクリプト実行モード保持フィールド */
	protected KagerowDBMode mode;
	/** スクリプトカレントスキーマ保持フィールド */
	protected String schema;
	/** スクリプトキャッシュID保持フィールド */
	protected String cacheId;
	/** スクリプト入力プラグインリスト保持フィールド */
	protected List<KagerowPluginAccessor> inputPlugins;
	/** スクリプト出力プラグインリスト保持フィールド */
	protected List<KagerowPluginAccessor> outputPlugins;
	/** スクリプトKSQL一覧リスト保持フィールド */
	protected List<KagerowSqlAccessor> ksqls;
	/** スクリプト実行コマンド一覧リスト保持フィールド */
	protected List<KagerowCmdAccessor> command;
	/** 環境変数一覧マップ保持フィールド */
	protected Map<String, String> env;

	/** 未変換スクリプト名称保持フィールド */
	protected String rawname;
	/** 未変換スクリプトバージョン保持フィールド */
	protected String rawversion;
	/** 未変換スクリプト概要保持フィールド */
	protected String rawsummary;
	/** 未変換スクリプト実行モード保持フィールド */
	protected String rawmode;
	/** 未変換スクリプトカレントスキーマ保持フィールド */
	protected String rawschema;
	/** 未変換スクリプトキャッシュID保持フィールド */
	protected String rawcacheId;
	/** 未変換スクリプト入力プラグインリスト保持フィールド */
	protected List<KagerowPluginAccessor> rawinputPlugins;
	/** 未変換スクリプト出力プラグインリスト保持フィールド */
	protected List<KagerowPluginAccessor> rawoutputPlugins;
	/** 未変換スクリプトKSQL一覧リスト保持フィールド */
	protected List<KagerowSqlAccessor> rawksqls;
	/** 未変換スクリプト実行コマンド一覧リスト保持フィールド */
	protected List<KagerowCmdAccessor> rawcommand;

	/** スクリプト状態管理フィールド */
	protected StateManager stateManager = new StateManager();

	// ###########################################################################
	// # コンフィグレーション要素
	// ###########################################################################

	/** 設定要素（ルート要素） */
	protected static final String CONF_ROOT = "configuration";

	/** 設定要素（名称要素） */
	protected static final String CONF_NAME = "name";
	/** 設定要素（概要要素） */
	protected static final String CONF_SUMMARY = "summary";
	/** 設定要素（実行モード要素） */
	protected static final String CONF_MODE = "mode";
	/** 設定要素（カレントスキーマ要素） */
	protected static final String CONF_SCHEMA = "schema";
	/** 設定要素（キャッシュID要素） */
	protected static final String CONF_CACHE = "cache";

	// ###########################################################################
	// # 環境変数要素
	// ###########################################################################

	/** 環境変数要素（環境変数ルート要素） */
	protected static final String ENV_ROOT = "environment";

	/** 環境変数要素（環境変数子要素） */
	protected static final String ENV = "env";
	/** 環境変数属性（環境変数名） */
	protected static final String ENV_NAME = "name";
	/** 環境変数属性（環境変数設定値） */
	protected static final String ENV_VALUE = "value";

	// ###########################################################################
	// # プラグイン要素
	// ###########################################################################

	/** プラグイン要素（ルート要素） */
	protected static final String PLUGIN_ROOT = "plugins";
	/** プラグイン要素（インプット要素） */
	protected static final String PLUGIN_INPUT = "input";
	/** プラグイン要素（アウトプット要素） */
	protected static final String PLUGIN_OUTPUT = "output";

	/** プラグイン要素（プラグイン共通要素名） */
	protected static final String PLUGIN = "plugin";

	/** プラグイン属性（ID） */
	protected static final String PLUGIN_ID = "id";
	/** プラグインパッケージ属性（名称） */
	protected static final String PLUGIN_PKG_NM = "package";
	/** プラグイン属性（名称） */
	protected static final String PLUGIN_NM = "name";
	/** プラグイン属性（後続ID） */
	protected static final String PLUGIN_NID = "next";

	/** プラグイン要素（パラメータ要素名） */
	protected static final String PLUGIN_PARAM = "param";
	/** プラグイン要素（パラメータ名） */
	protected static final String PLUGIN_PARAM_NM = "name";

	// ###########################################################################
	// # KSQL要素
	// ###########################################################################

	/** KSQLルート要素 */
	protected static final String KSQL_ROOT = "ksqls";
	/** KSQL要素（KSQL共通要素名） */
	protected static final String KSQL = "ksql";

	/** KSQL属性（ID） */
	protected static final String KSQL_ID = "id";
	/** KSQL属性（名称） */
	protected static final String KSQL_NM = "name";
	/** KSQL属性（後続ID） */
	protected static final String KSQL_NID = "next";

	/** KSQL要素（変数宣言要素名root） */
	protected static final String KSQL_VAR_DEC = "variable-declaration";
	/** KSQL要素（変数宣言要素名） */
	protected static final String KSQL_VAR = "variable";
	/** KSQL要素（SQL宣言要素名） */
	protected static final String KSQL_SQL = "sql";
	/** KSQL変数宣言属性（名称） */
	protected static final String KSQL_VAR_NM = "name";
	/** KSQL変数宣言属性（設定値） */
	protected static final String KSQL_VAR_VAL = "value";

	// ###########################################################################
	// # コマンド要素
	// ###########################################################################

	/** コマンドルート要素 */
	protected static final String COMMAND_ROOT = "command";

	/** コマンド要素（環境変数要素名） */
	protected static final String COMMAND_ENV = "environmental-variables";
	/** コマンド要素（環境変数宣言要素名） */
	protected static final String COMMAND_ENV_VAR = "variable";
	/** コマンド要素（コマンド宣言要素名） */
	protected static final String COMMAND_CMD = "cmd";

	/** コマンド環境変数宣言属性（名称） */
	protected static final String COMMAND_ENV_VAR_NM = "name";
	/** コマンド環境変数宣言属性（設定値） */
	protected static final String COMMAND_ENV_VAR_VAL = "value";
	/** コマンドモード宣言属性 */
	protected static final String COMMAND_MODE = "mode";

	// ###########################################################################
	// # 内部インターフェイス定義
	// ###########################################################################

	/**
	 * KagerowScriptAccessorのビルダーインターフェイスです
	 * @param <T> ビルド成果物
	 */
	public interface AcccessorBuilder<T> {

		/**
		 * ビルド成果物を生成します 
		 * @return ビルド成果物
		 */
		T build();

	}

	// ###########################################################################
	// # ネストインターフェイス実装
	// ###########################################################################

	/**
	 * KagerowPluginAccessor実装クラスです
	 */
	public final class KagerowPluginAccessorImpl
			implements KagerowPluginAccessor, Cloneable, AcccessorBuilder<KagerowPluginAccessor> {

		/** プラグインパッケージ名称保持フィールド */
		protected String pkg;
		/** プラグインバージョン保持フィールド */
		protected String version;
		/** プラグイン名称保持フィールド */
		protected String name;
		/** プラグインID保持フィールド */
		protected String id;
		/** 後続処理プラグインID保持フィールド */
		protected String next;
		/** パラメータ一覧マップ保持フィールド */
		protected Map<String, String> param = new ConcurrentHashMap<>();

		/** インスタンス検索用キーインスタンス */
		private Object KEY;

		/**
		 * デフォルトコンストラクタ
		 */
		public KagerowPluginAccessorImpl() {
			KEY = new Object();
		}

		/** {@inheritDoc} */
		@Override
		public String getPackageName() {
			return pkg;
		}

		/** {@inheritDoc} */
		@Override
		public String getVersion() {
			return version;
		}

		/** {@inheritDoc} */
		@Override
		public String getName() {
			return name;
		}

		/** {@inheritDoc} */
		@Override
		public String getId() {
			return id;
		}

		/** {@inheritDoc} */
		@Override
		@SuppressFBWarnings(value = "NP_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", justification = "ロジック上必ず書き込むため問題なし")
		public boolean hasNext() {
			return !next.isEmpty();
		}

		/** {@inheritDoc} */
		@Override
		public String next() {
			return next;
		}

		/** {@inheritDoc} */
		@Override
		public Map<String, String> getParam() {
			return new ConcurrentHashMap<>(param);
		}

		/** {@inheritDoc} */
		@Override
		public KagerowPluginAccessor setParam(Map<String, String> newParam) {
			Objects.requireNonNull(newParam);
			if (Objects.equals(this.param, newParam)) {
				return this;
			}
			KagerowPluginAccessorImpl newInstance = this;
			stateManager.begin();
			try {
				newInstance = this.clone();
				newInstance.param = newParam;
				stateManager.commit();
			} catch (CloneNotSupportedException e) {
				KagerowLogger.newAppLogger().err(e);
				stateManager.rollback();
			}
			return newInstance;
		}

		/** {@inheritDoc} */
		@Override
		public KagerowPluginAccessor setPackageName(String pkg) {
			Objects.requireNonNull(pkg);
			if (Objects.equals(this.pkg, pkg)) {
				return this;
			}
			KagerowPluginAccessorImpl newInstance = this;
			stateManager.begin();
			try {
				newInstance = this.clone();
				newInstance.pkg = pkg;
				stateManager.commit();
			} catch (CloneNotSupportedException e) {
				KagerowLogger.newAppLogger().err(e);
				stateManager.rollback();
			}
			return newInstance;
		}

		/** {@inheritDoc} */
		@Override
		public KagerowPluginAccessor setVersion(String version) {
			Objects.requireNonNull(version);
			if (Objects.equals(this.version, version)) {
				return this;
			}
			KagerowPluginAccessorImpl newInstance = this;
			stateManager.begin();
			try {
				newInstance = this.clone();
				newInstance.version = version;
				stateManager.commit();
			} catch (CloneNotSupportedException e) {
				KagerowLogger.newAppLogger().err(e);
				stateManager.rollback();
			}
			return newInstance;
		}

		/** {@inheritDoc} */
		@Override
		public KagerowPluginAccessor setName(String name) {
			Objects.requireNonNull(name);
			if (Objects.equals(this.name, name)) {
				return this;
			}
			KagerowPluginAccessorImpl newInstance = this;
			stateManager.begin();
			try {
				newInstance = this.clone();
				newInstance.name = name;
				stateManager.commit();
			} catch (CloneNotSupportedException e) {
				KagerowLogger.newAppLogger().err(e);
				stateManager.rollback();
			}
			return newInstance;
		}

		/** {@inheritDoc} */
		@Override
		public KagerowPluginAccessor setId(String id) {
			Objects.requireNonNull(id);
			if (Objects.equals(this.id, id)) {
				return this;
			}
			KagerowPluginAccessorImpl newInstance = this;
			stateManager.begin();
			try {
				newInstance = this.clone();
				newInstance.id = id;
				stateManager.commit();
			} catch (CloneNotSupportedException e) {
				KagerowLogger.newAppLogger().err(e);
				stateManager.rollback();
			}
			return newInstance;
		}

		/** {@inheritDoc} */
		@Override
		public KagerowPluginAccessor setNext(String next) {
			if (Objects.equals(this.next, next)) {
				return this;
			}
			KagerowPluginAccessorImpl newInstance = this;
			stateManager.begin();
			try {
				newInstance = this.clone();
				newInstance.next = next;
				stateManager.commit();
			} catch (CloneNotSupportedException e) {
				KagerowLogger.newAppLogger().err(e);
				stateManager.rollback();
			}
			return newInstance;
		}

		/** {@inheritDoc} */
		@Override
		protected KagerowPluginAccessorImpl clone() throws CloneNotSupportedException {
			KagerowPluginAccessorImpl cloneInstance = (KagerowPluginAccessorImpl) super.clone();
			cloneInstance.param = new ConcurrentHashMap<>(param);
			return cloneInstance;
		}

		/**
		 * KFileから根源となったインスタンスを検索します
		 * @param kFile KFileインスタンス
		 * @return 根源となるインスタンス
		 * @throws CloneNotSupportedException クローン失敗
		 */
		public KagerowPluginAccessorImpl search(KFile kFile) throws CloneNotSupportedException {
			for (List<?> list : new List<?>[] {
					kFile.getInputPlugins(),
					kFile.getRawInputPlugins(),
					kFile.getOutputPlugins(),
					kFile.getRawOutputPlugins()
			}) {
				for (Object tmp : list) {
					KagerowPluginAccessorImpl instance = (KagerowPluginAccessorImpl) tmp;
					if (instance.KEY == KEY) {
						return instance;
					}
				}
			}
			// クローンしたインスタンスに対して呼び出しを行うため、nullにならないことが保証されている
			return null;
		}

		/** {@inheritDoc} */
		@Override
		public boolean isSame(KagerowPluginAccessor targetPlugin) {
			return ((KagerowPluginAccessorImpl) targetPlugin).KEY == KEY;
		}

		/** {@inheritDoc} */
		@Override
		public KagerowPluginAccessor build() {
			if (param.containsKey(KagerowPluginAccessor.PLUGIN_ID_PARAM_KEY)) {
				KagerowLogger.newAppLogger().log(
						Level.WARNING,
						AppLogMessage.WARNING_MSG9004.name(),
						new Object[] { KagerowPluginAccessor.PLUGIN_ID_PARAM_KEY });
			}
			if (param.containsKey(KagerowPluginAccessor.PLUGIN_NAME_PARAM_KEY)) {
				KagerowLogger.newAppLogger().log(
						Level.WARNING,
						AppLogMessage.WARNING_MSG9004.name(),
						new Object[] { KagerowPluginAccessor.PLUGIN_NAME_PARAM_KEY });
			}
			param.put(KagerowPluginAccessor.PLUGIN_ID_PARAM_KEY, id);
			param.put(KagerowPluginAccessor.PLUGIN_NAME_PARAM_KEY, name);
			return this;
		}

	}

	/**
	 * KagerowSqlAccessor実装クラスです
	 */
	public final class KagerowSqlAccessorImpl
			implements KagerowSqlAccessor, Cloneable, AcccessorBuilder<KagerowSqlAccessor> {

		/** KSQLID保持フィールド */
		protected String id;
		/** 後続処理KSQLID保持フィールド */
		protected String next;
		/** KSQL名称保持フィールド */
		protected String name;
		/** KSQL保持フィールド */
		protected String sql;
		/** KSQL変数宣言一覧マップ保持フィールド */
		protected Map<String, String> variable = new ConcurrentHashMap<>();

		/** インスタンス検索用キーインスタンス */
		private Object KEY = new Object();

		/** {@inheritDoc} */
		@Override
		public String getId() {
			return id;
		}

		/** {@inheritDoc} */
		@Override
		@SuppressFBWarnings(value = "NP_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", justification = "ロジック上必ず書き込むため問題なし")
		public boolean hasNext() {
			return !next.isEmpty();
		}

		/** {@inheritDoc} */
		@Override
		public String next() {
			return next;
		}

		/** {@inheritDoc} */
		@Override
		public Map<String, String> variable() {
			return new ConcurrentHashMap<>(variable);
		}

		/** {@inheritDoc} */
		@Override
		public KagerowSqlAccessor variable(Map<String, String> newvariable) {
			Objects.requireNonNull(newvariable);
			if (Objects.equals(this.variable, newvariable)) {
				return this;
			}
			KagerowSqlAccessorImpl newInstance = this;
			stateManager.begin();
			try {
				newInstance = this.clone();
				newInstance.variable = newvariable;
				stateManager.commit();
			} catch (CloneNotSupportedException e) {
				KagerowLogger.newAppLogger().err(e);
				stateManager.rollback();
			}
			return newInstance;
		}

		/** {@inheritDoc} */
		@Override
		public String getSql() {
			return sql;
		}

		/** {@inheritDoc} */
		@Override
		public String getName() {
			return name;
		}

		/** {@inheritDoc} */
		@Override
		public KagerowSqlAccessor setName(String name) {
			Objects.requireNonNull(name);
			if (Objects.equals(this.name, name)) {
				return this;
			}
			KagerowSqlAccessorImpl newInstance = this;
			stateManager.begin();
			try {
				newInstance = this.clone();
				newInstance.name = name;
				stateManager.commit();
			} catch (CloneNotSupportedException e) {
				KagerowLogger.newAppLogger().err(e);
				stateManager.rollback();
			}
			return newInstance;
		}

		/** {@inheritDoc} */
		@Override
		public KagerowSqlAccessor setId(String id) {
			Objects.requireNonNull(id);
			if (Objects.equals(this.id, id)) {
				return this;
			}
			KagerowSqlAccessorImpl newInstance = this;
			stateManager.begin();
			try {
				newInstance = this.clone();
				newInstance.id = id;
				stateManager.commit();
			} catch (CloneNotSupportedException e) {
				KagerowLogger.newAppLogger().err(e);
				stateManager.rollback();
			}
			return newInstance;
		}

		/** {@inheritDoc} */
		@Override
		public KagerowSqlAccessor setNext(String next) {
			if (Objects.equals(this.next, next)) {
				return this;
			}
			KagerowSqlAccessorImpl newInstance = this;
			stateManager.begin();
			try {
				newInstance = this.clone();
				newInstance.next = next;
				stateManager.commit();
			} catch (CloneNotSupportedException e) {
				KagerowLogger.newAppLogger().err(e);
				stateManager.rollback();
			}
			return newInstance;
		}

		/** {@inheritDoc} */
		@Override
		public KagerowSqlAccessor setSql(String sql) {
			Objects.requireNonNull(sql);
			KagerowSqlAccessorImpl newInstance = null;
			stateManager.begin();
			try {
				newInstance = this.clone();
				newInstance.sql = sql;
				stateManager.commit();
			} catch (CloneNotSupportedException e) {
				KagerowLogger.newAppLogger().err(e);
				stateManager.rollback();
			}
			return newInstance;
		}

		/** {@inheritDoc} */
		@Override
		protected KagerowSqlAccessorImpl clone() throws CloneNotSupportedException {
			KagerowSqlAccessorImpl cloneInstance = (KagerowSqlAccessorImpl) super.clone();
			cloneInstance.variable = new ConcurrentHashMap<>(variable);
			return cloneInstance;
		}

		/**
		 * KFileから根源となったインスタンスを検索します
		 * @param kFile KFileインスタンス
		 * @return 根源となるインスタンス
		 */
		public KagerowSqlAccessorImpl search(KFile kFile) {
			for (List<?> list : new List<?>[] {
					kFile.getRawKsqls(),
					kFile.getKsqls()
			}) {
				for (Object tmp : list) {
					KagerowSqlAccessorImpl instance = (KagerowSqlAccessorImpl) tmp;
					if (instance.KEY == KEY) {
						return instance;
					}
				}
			}
			// クローンしたインスタンスに対して呼び出しを行うため、nullにならないことが保証されている
			return null;
		}

		/** {@inheritDoc} */
		@Override
		public boolean isSame(KagerowSqlAccessor targetKsql) {
			return ((KagerowSqlAccessorImpl) targetKsql).KEY == KEY;
		}

		/** {@inheritDoc} */
		@Override
		public KagerowSqlAccessor build() {
			if (variable.containsKey(KagerowSqlAccessor.KSQL_ID_PARAM_KEY)) {
				KagerowLogger.newAppLogger().log(
						Level.WARNING,
						AppLogMessage.WARNING_MSG9004.name(),
						new Object[] { KagerowSqlAccessor.KSQL_ID_PARAM_KEY });
			}
			if (variable.containsKey(KagerowSqlAccessor.KSQL_NAME_PARAM_KEY)) {
				KagerowLogger.newAppLogger().log(
						Level.WARNING,
						AppLogMessage.WARNING_MSG9004.name(),
						new Object[] { KagerowSqlAccessor.KSQL_NAME_PARAM_KEY });
			}
			variable.put(KagerowSqlAccessor.KSQL_ID_PARAM_KEY, id);
			variable.put(KagerowSqlAccessor.KSQL_NAME_PARAM_KEY, name);
			return this;
		}

	}

	/**
	 * KagerowCmdAccessor実装クラスです
	 */
	public final class KagerowCmdAccessorImpl implements KagerowCmdAccessor, Cloneable {

		/** 環境変数宣言一覧マップ保持フィールド */
		protected Map<String, String> environmental = new ConcurrentHashMap<>();
		/** 実行コマンド保持フィールド */
		protected String cmd;
		/** 実行モード保持フィールド */
		protected KagerowCommandMode mode;

		/** インスタンス検索用キーインスタンス */
		private Object KEY = new Object();

		/** {@inheritDoc} */
		@Override
		public Map<String, String> environmental() {
			return new ConcurrentHashMap<>(environmental);
		}

		/** {@inheritDoc} */
		@Override
		public KagerowCmdAccessor environmental(Map<String, String> newEnvironmental) {
			Objects.requireNonNull(newEnvironmental);
			if (Objects.equals(this.environmental, newEnvironmental)) {
				return this;
			}
			KagerowCmdAccessorImpl newInstance = this;
			stateManager.begin();
			try {
				newInstance = this.clone();
				newInstance.environmental = newEnvironmental;
				stateManager.commit();
			} catch (CloneNotSupportedException e) {
				KagerowLogger.newAppLogger().err(e);
				stateManager.rollback();
			}
			return newInstance;
		}

		/** {@inheritDoc} */
		@Override
		public String getCmd() {
			return cmd;
		}

		/** {@inheritDoc} */
		@Override
		public KagerowCommandMode getMode() {
			return mode;
		}

		/** {@inheritDoc} */
		@Override
		public KagerowCmdAccessor setMode(KagerowCommandMode mode) {
			Objects.requireNonNull(mode);
			if (Objects.equals(this.mode, mode)) {
				return this;
			}
			KagerowCmdAccessorImpl newInstance = this;
			stateManager.begin();
			try {
				newInstance = this.clone();
				newInstance.mode = mode;
				stateManager.commit();
			} catch (CloneNotSupportedException e) {
				KagerowLogger.newAppLogger().err(e);
				stateManager.rollback();
			}
			return newInstance;
		}

		/** {@inheritDoc} */
		@Override
		public KagerowCmdAccessor setCmd(String cmd) {
			Objects.requireNonNull(cmd);
			if (Objects.equals(this.cmd, cmd)) {
				return this;
			}
			KagerowCmdAccessorImpl newInstance = this;
			stateManager.begin();
			try {
				newInstance = this.clone();
				newInstance.cmd = cmd;
				stateManager.commit();
			} catch (CloneNotSupportedException e) {
				KagerowLogger.newAppLogger().err(e);
				stateManager.rollback();
			}
			return newInstance;
		}

		/** {@inheritDoc} */
		@Override
		protected KagerowCmdAccessorImpl clone() throws CloneNotSupportedException {
			KagerowCmdAccessorImpl cloneInstance = (KagerowCmdAccessorImpl) super.clone();
			cloneInstance.environmental = new ConcurrentHashMap<>(environmental);
			return cloneInstance;
		}

		/**
		 * KFileから根源となったインスタンスを検索します
		 * @param kFile KFileインスタンス
		 * @return 根源となるインスタンス
		 */
		public KagerowCmdAccessorImpl search(KFile kFile) {
			for (List<?> list : new List<?>[] {
					kFile.getRawCommand(),
					kFile.getCommand()
			}) {
				for (Object tmp : list) {
					KagerowCmdAccessorImpl instance = (KagerowCmdAccessorImpl) tmp;
					if (instance.KEY == KEY) {
						return instance;
					}
				}
			}
			// クローンしたインスタンスに対して呼び出しを行うため、nullにならないことが保証されている
			return null;
		}

		/** {@inheritDoc} */
		@Override
		public boolean isSame(KagerowCmdAccessor cmd) {
			return ((KagerowCmdAccessorImpl) cmd).KEY == KEY;
		}

	}

	// ###########################################################################
	// # 変換後KFILEデータ
	// ###########################################################################

	/** {@inheritDoc} */
	@Override
	public String getName() {
		return name;
	}

	/** {@inheritDoc} */
	@Override
	public KagerowFileVersion getVersion() {
		return version;
	}

	/** {@inheritDoc} */
	@Override
	public KagerowFileState getState() {
		return stateManager.getState();
	}

	/** {@inheritDoc} */
	@Override
	public String getSummary() {
		return summary;
	}

	/** {@inheritDoc} */
	@Override
	public KagerowDBMode getMode() {
		return mode;
	}

	/** {@inheritDoc} */
	@Override
	public String getSchema() {
		return schema;
	}

	/** {@inheritDoc} */
	@Override
	public String getCacheId() {
		return cacheId;
	}

	/** {@inheritDoc} */
	@Override
	public Map<String, String> getEnv() {
		return new ConcurrentHashMap<>(env);
	}

	/** {@inheritDoc} */
	@Override
	public KagerowScriptAccessor setEnv(Map<String, String> newEnv) {
		Objects.requireNonNull(newEnv);
		if (Objects.equals(this.env, newEnv)) {
			return this;
		}
		KFile newKFile = this;
		newKFile.stateManager.begin();
		try {
			newKFile = (KFile) KFile.this.clone();
			newKFile.env = newEnv;
			newKFile.stateManager.commit();
		} catch (CloneNotSupportedException e) {
			KagerowLogger.newAppLogger().err(e);
			newKFile.stateManager.rollback();
		}
		return newKFile;
	}

	/** {@inheritDoc} */
	@Override
	public List<KagerowPluginAccessor> getInputPlugins() {
		return new Vector<>(inputPlugins);
	}

	/** {@inheritDoc} */
	@Override
	public KagerowScriptAccessor setInputPlugins(List<KagerowPluginAccessor> newPlugins) {
		Objects.requireNonNull(newPlugins);
		if (Objects.equals(this.inputPlugins, newPlugins)) {
			return this;
		}
		KFile newKFile = this;
		newKFile.stateManager.begin();
		try {
			newKFile = (KFile) KFile.this.clone();
			newKFile.inputPlugins = newPlugins;
			newKFile.stateManager.commit();
		} catch (CloneNotSupportedException e) {
			KagerowLogger.newAppLogger().err(e);
			newKFile.stateManager.rollback();
		}
		return newKFile;
	}

	/** {@inheritDoc} */
	@Override
	public List<KagerowPluginAccessor> getOutputPlugins() {
		return new Vector<>(outputPlugins);
	}

	/** {@inheritDoc} */
	@Override
	public KagerowScriptAccessor setOutputPlugins(List<KagerowPluginAccessor> newPlugins) {
		Objects.requireNonNull(newPlugins);
		if (Objects.equals(this.outputPlugins, newPlugins)) {
			return this;
		}
		KFile newKFile = this;
		newKFile.stateManager.begin();
		try {
			newKFile = (KFile) KFile.this.clone();
			newKFile.outputPlugins = newPlugins;
			newKFile.stateManager.commit();
		} catch (CloneNotSupportedException e) {
			KagerowLogger.newAppLogger().err(e);
			newKFile.stateManager.rollback();
		}
		return newKFile;
	}

	/** {@inheritDoc} */
	@Override
	public boolean hasPlugin() {
		return !(inputPlugins.isEmpty() && outputPlugins.isEmpty());
	}

	/** {@inheritDoc} */
	@Override
	public List<KagerowSqlAccessor> getKsqls() {
		return new Vector<>(ksqls);
	}

	/** {@inheritDoc} */
	@Override
	public KagerowScriptAccessor setKsqls(List<KagerowSqlAccessor> ksqls) {
		Objects.requireNonNull(ksqls);
		if (Objects.equals(this.ksqls, ksqls)) {
			return this;
		}
		KFile newKFile = this;
		newKFile.stateManager.begin();
		try {
			newKFile = (KFile) KFile.this.clone();
			newKFile.ksqls = ksqls;
			newKFile.stateManager.commit();
		} catch (CloneNotSupportedException e) {
			KagerowLogger.newAppLogger().err(e);
			newKFile.stateManager.rollback();
		}
		return newKFile;
	}

	/** {@inheritDoc} */
	@Override
	public List<KagerowCmdAccessor> getCommand() {
		return new Vector<>(command);
	}

	/** {@inheritDoc} */
	@Override
	public KagerowScriptAccessor setCommand(List<KagerowCmdAccessor> cmds) {
		Objects.requireNonNull(cmds);
		if (Objects.equals(this.command, cmds)) {
			return this;
		}
		KFile newKFile = this;
		newKFile.stateManager.begin();
		try {
			newKFile = (KFile) KFile.this.clone();
			newKFile.command = cmds;
			newKFile.stateManager.commit();
		} catch (CloneNotSupportedException e) {
			KagerowLogger.newAppLogger().err(e);
			newKFile.stateManager.rollback();
		}
		return newKFile;
	}

	/** {@inheritDoc} */
	@Override
	public boolean hasCmd() {
		return !command.isEmpty();
	}

	/** {@inheritDoc} */
	@Override
	public KagerowScriptAccessor setName(String name) {
		Objects.requireNonNull(name);
		if (Objects.equals(this.name, name)) {
			return this;
		}
		KFile newKFile = this;
		newKFile.stateManager.begin();
		try {
			newKFile = (KFile) KFile.this.clone();
			newKFile.name = name;
			newKFile.stateManager.commit();
		} catch (CloneNotSupportedException e) {
			KagerowLogger.newAppLogger().err(e);
			newKFile.stateManager.rollback();
		}
		return newKFile;
	}

	/** {@inheritDoc} */
	@Override
	public KagerowScriptAccessor setVersion(KagerowFileVersion version) {
		Objects.requireNonNull(version);
		if (Objects.equals(this.version, version)) {
			return this;
		}
		KFile newKFile = this;
		newKFile.stateManager.begin();
		try {
			newKFile = (KFile) KFile.this.clone();
			newKFile.version = version;
			newKFile.stateManager.commit();
		} catch (CloneNotSupportedException e) {
			KagerowLogger.newAppLogger().err(e);
			newKFile.stateManager.rollback();
		}
		return newKFile;
	}

	/** {@inheritDoc} */
	@Override
	public KagerowScriptAccessor setSummary(String summary) {
		Objects.requireNonNull(summary);
		if (Objects.equals(this.summary, summary)) {
			return this;
		}
		KFile newKFile = this;
		newKFile.stateManager.begin();
		try {
			newKFile = (KFile) KFile.this.clone();
			newKFile.summary = summary;
			newKFile.stateManager.commit();
		} catch (CloneNotSupportedException e) {
			KagerowLogger.newAppLogger().err(e);
			newKFile.stateManager.rollback();
		}
		return newKFile;
	}

	/** {@inheritDoc} */
	@Override
	public KagerowScriptAccessor setMode(KagerowDBMode mode) {
		Objects.requireNonNull(mode);
		if (Objects.equals(this.mode, mode)) {
			return this;
		}
		KFile newKFile = this;
		newKFile.stateManager.begin();
		try {
			newKFile = (KFile) KFile.this.clone();
			newKFile.mode = mode;
			newKFile.stateManager.commit();
		} catch (CloneNotSupportedException e) {
			KagerowLogger.newAppLogger().err(e);
			newKFile.stateManager.rollback();
		}
		return newKFile;
	}

	/** {@inheritDoc} */
	@Override
	public KagerowScriptAccessor setSchema(String schema) {
		Objects.requireNonNull(schema);
		if (Objects.equals(this.schema, schema)) {
			return this;
		}
		KFile newKFile = this;
		newKFile.stateManager.begin();
		try {
			newKFile = (KFile) KFile.this.clone();
			newKFile.schema = schema;
			newKFile.stateManager.commit();
		} catch (CloneNotSupportedException e) {
			KagerowLogger.newAppLogger().err(e);
			newKFile.stateManager.rollback();
		}
		return newKFile;
	}

	/** {@inheritDoc} */
	@Override
	public KagerowScriptAccessor setCacheId(String cacheId) {
		Objects.requireNonNull(cacheId);
		if (Objects.equals(this.cacheId, cacheId)) {
			return this;
		}
		KFile newKFile = this;
		newKFile.stateManager.begin();
		try {
			newKFile = (KFile) KFile.this.clone();
			newKFile.cacheId = cacheId;
			newKFile.stateManager.commit();
		} catch (CloneNotSupportedException e) {
			KagerowLogger.newAppLogger().err(e);
			newKFile.stateManager.rollback();
		}
		return newKFile;
	}

	// ###########################################################################
	// # 変換前KFILEデータ
	// ###########################################################################

	/** {@inheritDoc} */
	@Override
	public String getRawName() {
		return rawname;
	}

	/** {@inheritDoc} */
	@Override
	public String getRawVersion() {
		return rawversion;
	}

	/** {@inheritDoc} */
	@Override
	public String getRawSummary() {
		return rawsummary;
	}

	/** {@inheritDoc} */
	@Override
	public String getRawSchema() {
		return rawschema;
	}

	/** {@inheritDoc} */
	@Override
	public String getRawCacheId() {
		return rawcacheId;
	}

	/** {@inheritDoc} */
	@Override
	public String getRawMode() {
		return rawmode;
	}

	/** {@inheritDoc} */
	@Override
	public List<KagerowPluginAccessor> getRawInputPlugins() {
		return new Vector<>(rawinputPlugins);
	}

	/** {@inheritDoc} */
	@Override
	public KagerowScriptAccessor setRawInputPlugins(List<KagerowPluginAccessor> newPlugins) {
		Objects.requireNonNull(newPlugins);
		if (Objects.equals(this.rawinputPlugins, newPlugins)) {
			return this;
		}
		KFile newInstance = this;
		newInstance.stateManager.begin();
		try {
			newInstance = (KFile) KFile.this.clone();
			newInstance.rawinputPlugins = newPlugins;
			newInstance.stateManager.commit();
		} catch (CloneNotSupportedException e) {
			KagerowLogger.newAppLogger().err(e);
			newInstance.stateManager.rollback();
		}
		return newInstance;
	}

	/** {@inheritDoc} */
	@Override
	public List<KagerowPluginAccessor> getRawOutputPlugins() {
		return new Vector<>(rawoutputPlugins);
	}

	/** {@inheritDoc} */
	@Override
	public KagerowScriptAccessor setRawOutputPlugins(List<KagerowPluginAccessor> newPlugins) {
		Objects.requireNonNull(newPlugins);
		if (Objects.equals(this.rawoutputPlugins, newPlugins)) {
			return this;
		}
		KFile newInstance = this;
		newInstance.stateManager.begin();
		try {
			newInstance = (KFile) KFile.this.clone();
			newInstance.rawoutputPlugins = newPlugins;
			newInstance.stateManager.commit();
		} catch (CloneNotSupportedException e) {
			KagerowLogger.newAppLogger().err(e);
			newInstance.stateManager.rollback();
		}
		return newInstance;
	}

	/** {@inheritDoc} */
	@Override
	public List<KagerowSqlAccessor> getRawKsqls() {
		return new Vector<>(rawksqls);
	}

	/** {@inheritDoc} */
	@Override
	public KagerowScriptAccessor setRawKsqls(List<KagerowSqlAccessor> ksqls) {
		Objects.requireNonNull(ksqls);
		if (Objects.equals(this.rawksqls, ksqls)) {
			return this;
		}
		KFile newInstance = this;
		newInstance.stateManager.begin();
		try {
			newInstance = (KFile) KFile.this.clone();
			newInstance.rawksqls = ksqls;
			newInstance.stateManager.commit();
		} catch (CloneNotSupportedException e) {
			KagerowLogger.newAppLogger().err(e);
			newInstance.stateManager.rollback();
		}
		return newInstance;
	}

	/** {@inheritDoc} */
	@Override
	public List<KagerowCmdAccessor> getRawCommand() {
		return new Vector<>(rawcommand);
	}

	/** {@inheritDoc} */
	@Override
	public KagerowScriptAccessor setRawCommand(List<KagerowCmdAccessor> cmds) {
		Objects.requireNonNull(cmds);
		if (Objects.equals(this.rawcommand, cmds)) {
			return this;
		}
		KFile newInstance = this;
		newInstance.stateManager.begin();
		try {
			newInstance = (KFile) KFile.this.clone();
			newInstance.rawcommand = cmds;
			newInstance.stateManager.commit();
		} catch (CloneNotSupportedException e) {
			KagerowLogger.newAppLogger().err(e);
			newInstance.stateManager.rollback();
		}
		return newInstance;
	}

	/** {@inheritDoc} */
	@Override
	public KagerowScriptAccessor setRawName(String name) {
		if (Objects.equals(this.rawname, name)) {
			return this;
		}
		KFile newKFile = this;
		newKFile.stateManager.begin();
		try {
			newKFile = (KFile) KFile.this.clone();
			newKFile.rawname = name;
			newKFile.stateManager.commit();
		} catch (CloneNotSupportedException e) {
			KagerowLogger.newAppLogger().err(e);
			newKFile.stateManager.rollback();
		}
		return newKFile;
	}

	/** {@inheritDoc} */
	@Override
	public KagerowScriptAccessor setRawVersion(String version) {
		if (Objects.equals(this.rawversion, version)) {
			return this;
		}
		KFile newKFile = this;
		newKFile.stateManager.begin();
		try {
			newKFile = (KFile) KFile.this.clone();
			newKFile.rawversion = version;
			newKFile.stateManager.commit();
		} catch (CloneNotSupportedException e) {
			KagerowLogger.newAppLogger().err(e);
			newKFile.stateManager.rollback();
		}
		return newKFile;
	}

	/** {@inheritDoc} */
	@Override
	public KagerowScriptAccessor setRawSummary(String summary) {
		if (Objects.equals(this.rawsummary, summary)) {
			return this;
		}
		KFile newKFile = this;
		newKFile.stateManager.begin();
		try {
			newKFile = (KFile) KFile.this.clone();
			newKFile.rawsummary = summary;
			newKFile.stateManager.commit();
		} catch (CloneNotSupportedException e) {
			KagerowLogger.newAppLogger().err(e);
			newKFile.stateManager.rollback();
		}
		return newKFile;
	}

	/** {@inheritDoc} */
	@Override
	public KagerowScriptAccessor setRawSchema(String schema) {
		if (Objects.equals(this.rawschema, schema)) {
			return this;
		}
		KFile newKFile = this;
		newKFile.stateManager.begin();
		try {
			newKFile = (KFile) KFile.this.clone();
			newKFile.rawschema = schema;
			newKFile.stateManager.commit();
		} catch (CloneNotSupportedException e) {
			KagerowLogger.newAppLogger().err(e);
			newKFile.stateManager.rollback();
		}
		return newKFile;
	}

	/** {@inheritDoc} */
	@Override
	public KagerowScriptAccessor setRawCacheId(String cacheId) {
		if (Objects.equals(this.rawcacheId, cacheId)) {
			return this;
		}
		KFile newKFile = this;
		newKFile.stateManager.begin();
		try {
			newKFile = (KFile) KFile.this.clone();
			newKFile.rawcacheId = cacheId;
			newKFile.stateManager.commit();
		} catch (CloneNotSupportedException e) {
			KagerowLogger.newAppLogger().err(e);
			newKFile.stateManager.rollback();
		}
		return newKFile;
	}

	/** {@inheritDoc} */
	@Override
	public KagerowScriptAccessor setRawMode(String mode) {
		if (Objects.equals(this.rawmode, mode)) {
			return this;
		}
		KFile newKFile = this;
		newKFile.stateManager.begin();
		try {
			newKFile = (KFile) KFile.this.clone();
			newKFile.rawmode = mode;
			newKFile.stateManager.commit();
		} catch (CloneNotSupportedException e) {
			KagerowLogger.newAppLogger().err(e);
			newKFile.stateManager.rollback();
		}
		return newKFile;
	}

	// ###########################################################################
	// # ネストインターフェイスファクトリクラス
	// ###########################################################################

	/** {@inheritDoc} */
	@Override
	public KagerowPluginAccessor newKagerowPluginAccessor(
			String pkg,
			String name,
			String id,
			String next) {
		KagerowPluginAccessorImpl result = new KagerowPluginAccessorImpl();
		result.name = Objects.requireNonNull(name);
		result.id = Objects.requireNonNull(id);
		result.pkg = Objects.requireNonNull(pkg);
		result.next = next;
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public KagerowSqlAccessor newKagerowSqlAccessor(
			String name,
			String id,
			String next,
			String sql) {
		KagerowSqlAccessorImpl result = new KagerowSqlAccessorImpl();
		result.name = Objects.requireNonNull(name);
		result.id = Objects.requireNonNull(id);
		result.next = next;
		result.sql = Objects.requireNonNull(sql);
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public KagerowCmdAccessor newKagerowCmdAccessor(
			String cmd,
			KagerowCommandMode mode) {
		KagerowCmdAccessorImpl result = new KagerowCmdAccessorImpl();
		result.cmd = Objects.requireNonNull(cmd);
		result.mode = Objects.requireNonNull(mode);
		return result;
	}

	// ###########################################################################
	// # クラスクローン処理
	// ###########################################################################

	/** {@inheritDoc} */
	@Override
	protected Object clone() throws CloneNotSupportedException {
		KFile kFile = (KFile) super.clone();
		kFile.env = new ConcurrentHashMap<>(env);
		kFile.stateManager = stateManager.clone();
		cloneCommand(kFile);
		cloneRawCommand(kFile);
		cloneKsql(kFile);
		cloneRawKsql(kFile);
		cloneInputPlugin(kFile);
		cloneRawInputPlugin(kFile);
		cloneOutputPlugin(kFile);
		cloneRawOutputPlugin(kFile);
		return kFile;
	}

	/**
	 * コマンドインスタンスをディープコピーします
	 * @param kFile クローンインスタンス
	 * @throws CloneNotSupportedException クローン失敗
	 */
	private void cloneCommand(KFile kFile) throws CloneNotSupportedException {
		List<KagerowCmdAccessor> result = new Vector<>();
		for (KagerowCmdAccessor base : command) {
			KagerowCmdAccessorImpl baseImpl = (KagerowCmdAccessorImpl) base;
			result.add((KagerowCmdAccessor) baseImpl.clone());
		}
		kFile.command = result;
	}

	/**
	 * Rawコマンドインスタンスをディープコピーします
	 * @param kFile クローンインスタンス
	 * @throws CloneNotSupportedException クローン失敗
	 */
	private void cloneRawCommand(KFile kFile) throws CloneNotSupportedException {
		List<KagerowCmdAccessor> result = new Vector<>();
		for (KagerowCmdAccessor base : rawcommand) {
			KagerowCmdAccessorImpl baseImpl = (KagerowCmdAccessorImpl) base;
			result.add((KagerowCmdAccessor) baseImpl.clone());
		}
		kFile.rawcommand = result;
	}

	/**
	 * KSQLインスタンスをディープコピーします
	 * @param kFile クローンインスタンス
	 * @throws CloneNotSupportedException クローン失敗
	 */
	private void cloneKsql(KFile kFile) throws CloneNotSupportedException {
		List<KagerowSqlAccessor> result = new Vector<>();
		for (KagerowSqlAccessor base : ksqls) {
			KagerowSqlAccessorImpl baseImpl = (KagerowSqlAccessorImpl) base;
			result.add((KagerowSqlAccessor) baseImpl.clone());
		}
		kFile.ksqls = result;
	}

	/**
	 * RawKSQLインスタンスをディープコピーします
	 * @param kFile クローンインスタンス
	 * @throws CloneNotSupportedException クローン失敗
	 */
	private void cloneRawKsql(KFile kFile) throws CloneNotSupportedException {
		List<KagerowSqlAccessor> result = new Vector<>();
		for (KagerowSqlAccessor base : rawksqls) {
			KagerowSqlAccessorImpl baseImpl = (KagerowSqlAccessorImpl) base;
			result.add((KagerowSqlAccessor) baseImpl.clone());
		}
		kFile.rawksqls = result;
	}

	/**
	 * 入力プラグインインスタンスをディープコピーします
	 * @param kFile クローンインスタンス
	 * @throws CloneNotSupportedException クローン失敗
	 */
	private void cloneInputPlugin(KFile kFile) throws CloneNotSupportedException {
		List<KagerowPluginAccessor> result = new Vector<>();
		for (KagerowPluginAccessor base : inputPlugins) {
			KagerowPluginAccessorImpl baseImpl = (KagerowPluginAccessorImpl) base;
			result.add((KagerowPluginAccessor) baseImpl.clone());
		}
		kFile.inputPlugins = result;
	}

	/**
	 * Raw入力プラグインインスタンスをディープコピーします
	 * @param kFile クローンインスタンス
	 * @throws CloneNotSupportedException クローン失敗
	 */
	private void cloneRawInputPlugin(KFile kFile) throws CloneNotSupportedException {
		List<KagerowPluginAccessor> result = new Vector<>();
		for (KagerowPluginAccessor base : rawinputPlugins) {
			KagerowPluginAccessorImpl baseImpl = (KagerowPluginAccessorImpl) base;
			result.add((KagerowPluginAccessor) baseImpl.clone());
		}
		kFile.rawinputPlugins = result;
	}

	/**
	 * 出力プラグインインスタンスをディープコピーします
	 * @param kFile クローンインスタンス
	 * @throws CloneNotSupportedException クローン失敗
	 */
	private void cloneOutputPlugin(KFile kFile) throws CloneNotSupportedException {
		List<KagerowPluginAccessor> result = new Vector<>();
		for (KagerowPluginAccessor base : outputPlugins) {
			KagerowPluginAccessorImpl baseImpl = (KagerowPluginAccessorImpl) base;
			result.add((KagerowPluginAccessor) baseImpl.clone());
		}
		kFile.outputPlugins = result;
	}

	/**
	 * Raw出力プラグインインスタンスをディープコピーします
	 * @param kFile クローンインスタンス
	 * @throws CloneNotSupportedException クローン失敗
	 */
	private void cloneRawOutputPlugin(KFile kFile) throws CloneNotSupportedException {
		List<KagerowPluginAccessor> result = new Vector<>();
		for (KagerowPluginAccessor base : rawoutputPlugins) {
			KagerowPluginAccessorImpl baseImpl = (KagerowPluginAccessorImpl) base;
			result.add((KagerowPluginAccessor) baseImpl.clone());
		}
		kFile.rawoutputPlugins = result;
	}

	/**
	 * 実行に必要なシステム設定を行なったインスタンスを返却します
	 * @param plan 実行計画インスタンス
	 * @return 調整済みの実行インスタンス
	 */
	public KagerowScriptAccessor build(ExecutionPlan<?> plan) {
		try {

			// KFILEインスタンスクローン
			KFile accessor = (KFile) clone();

			// 入力プラグイン処理
			for (KagerowPluginAccessor plugin : accessor.rawinputPlugins) {
				if (plugin instanceof KagerowPluginAccessorImpl impl) {
					impl.build();
				}
			}

			// 出力ブラグイン処理
			for (KagerowPluginAccessor plugin : accessor.rawoutputPlugins) {
				if (plugin instanceof KagerowPluginAccessorImpl impl) {
					impl.build();
				}
			}

			// KSQK処理
			for (KagerowSqlAccessor ksql : accessor.rawksqls) {
				if (ksql instanceof KagerowSqlAccessorImpl impl) {
					impl.build();
				}
			}

			// スクリプト環境変数処理
			Map<String, String> env = accessor.env;
			if (env.containsKey(KagerowScriptAccessor.KAGEROW_SESSION_ID_ENV_KYE)) {
				KagerowLogger.newAppLogger().log(
						Level.WARNING,
						AppLogMessage.WARNING_MSG9005.name(),
						new Object[] { KagerowScriptAccessor.KAGEROW_SESSION_ID_ENV_KYE });
			}
			if (Objects.nonNull(plan)) {
				env.put(KagerowScriptAccessor.KAGEROW_SESSION_ID_ENV_KYE, plan.getSessionId());
			}

			if (env.containsKey(KagerowScriptAccessor.KAGEROW_TMP_DIR_ENV_KYE)) {
				KagerowLogger.newAppLogger().log(
						Level.WARNING,
						AppLogMessage.WARNING_MSG9005.name(),
						new Object[] { KagerowScriptAccessor.KAGEROW_TMP_DIR_ENV_KYE });
			}
			env.put(KagerowScriptAccessor.KAGEROW_TMP_DIR_ENV_KYE,
					AppPathUtils.createTemporaryDirPath().toAbsolutePath().normalize().toString());

			if (env.containsKey(KagerowScriptAccessor.KAGEROW_RANTIME_DIR_ENV_KYE)) {
				KagerowLogger.newAppLogger().log(
						Level.WARNING,
						AppLogMessage.WARNING_MSG9005.name(),
						new Object[] { KagerowScriptAccessor.KAGEROW_RANTIME_DIR_ENV_KYE });
			}
			env.put(KagerowScriptAccessor.KAGEROW_RANTIME_DIR_ENV_KYE,
					AppPathUtils.createRuntimeDirPath().toAbsolutePath().normalize().toString());

			if (env.containsKey(KagerowScriptAccessor.KAGEROW_SCRIPT_NAME_ENV_KYE)) {
				KagerowLogger.newAppLogger().log(
						Level.WARNING,
						AppLogMessage.WARNING_MSG9005.name(),
						new Object[] { KagerowScriptAccessor.KAGEROW_SCRIPT_NAME_ENV_KYE });
			}
			env.put(KagerowScriptAccessor.KAGEROW_SCRIPT_NAME_ENV_KYE, accessor.name);

			if (env.containsKey(KagerowScriptAccessor.KAGEROW_EXE_MODE_ENV_KYE)) {
				KagerowLogger.newAppLogger().log(
						Level.WARNING,
						AppLogMessage.WARNING_MSG9005.name(),
						new Object[] { KagerowScriptAccessor.KAGEROW_EXE_MODE_ENV_KYE });
			}
			env.put(KagerowScriptAccessor.KAGEROW_EXE_MODE_ENV_KYE, accessor.rawmode);

			if (env.containsKey(KagerowScriptAccessor.KAGEROW_SCHEMA_ENV_KYE)) {
				KagerowLogger.newAppLogger().log(
						Level.WARNING,
						AppLogMessage.WARNING_MSG9005.name(),
						new Object[] { KagerowScriptAccessor.KAGEROW_SCHEMA_ENV_KYE });
			}
			env.put(KagerowScriptAccessor.KAGEROW_SCHEMA_ENV_KYE, accessor.schema);

			if (env.containsKey(KagerowScriptAccessor.KAGEROW_SCRIPT_FILE_PATH_ENV_KYE)) {
				KagerowLogger.newAppLogger().log(
						Level.WARNING,
						AppLogMessage.WARNING_MSG9005.name(),
						new Object[] { KagerowScriptAccessor.KAGEROW_SCRIPT_FILE_PATH_ENV_KYE });
			}

			// 日付関連スクリプト変数対応
			{
				String timestampFormat = env.getOrDefault(
						KagerowScriptAccessor.KAGEROW_TIMESTAMP_FORMAT_ENV_KYE,
						TIMESTAMP_DEFAULT_FORMAT);
				String timestamp = StringUtils.EMPTY;
				try {
					// フォーマットチャレンジ
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern(timestampFormat);
					timestamp = formatter.format(LocalDateTime.now());
				} catch (DateTimeException | IllegalArgumentException e) {
					// フォーマットに失敗した場合はデフォルトフォーマットを適用
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern(TIMESTAMP_DEFAULT_FORMAT);
					timestamp = formatter.format(LocalDateTime.now());
					// ログに通知
					KagerowLogger.newAppLogger().log(
							Level.WARNING,
							AppLogMessage.WARNING_MSG9006.name(),
							new Object[] { timestampFormat });
				} finally {
					env.put(KagerowScriptAccessor.KAGEROW_TIMESTAMP_ENV_KYE, timestamp);
				}
			}

			// スクリプトファイルパスはFileReaderの場合のみパスで上書き
			if (accessor instanceof FileReader fileFeader) {
				env.put(KagerowScriptAccessor.KAGEROW_SCRIPT_FILE_PATH_ENV_KYE,
						fileFeader.path.toAbsolutePath().normalize().toString());
			} else {
				env.put(KagerowScriptAccessor.KAGEROW_SCRIPT_FILE_PATH_ENV_KYE, StringUtils.EMPTY);
			}

			// 実行時スクリプト環境変数適用
			KsqlReplaceEnvParser envParser = new KsqlReplaceEnvParser(env);
			accessor.name = parseEnv(envParser, accessor.name);
			accessor.summary = parseEnv(envParser, accessor.summary);
			accessor.schema = parseEnv(envParser, accessor.schema);
			accessor.cacheId = parseEnv(envParser, accessor.cacheId);
			@SuppressWarnings("unchecked")
			List<KagerowPluginAccessor>[] tmpList = (List<KagerowPluginAccessor>[]) new List[] {
					accessor.inputPlugins,
					accessor.outputPlugins
			};
			for (List<KagerowPluginAccessor> pluginAccessors : tmpList) {
				for (KagerowPluginAccessor pluginAccessor : pluginAccessors) {
					if (pluginAccessor instanceof KagerowPluginAccessorImpl impl) {
						impl.pkg = parseEnv(envParser, impl.pkg);
						impl.name = parseEnv(envParser, impl.name);
						impl.id = parseEnv(envParser, impl.id);
						impl.next = parseEnv(envParser, impl.next);
						Map<String, String> param = new ConcurrentHashMap<>();
						for (Map.Entry<String, String> entry : impl.param.entrySet()) {
							String paramName = parseEnv(envParser, entry.getKey());
							String paramValue = parseEnv(envParser, entry.getValue());
							param.put(paramName, paramValue);
						}
						impl.param = param;
					}
				}
			}
			for (KagerowSqlAccessor ksqlAccessor : accessor.ksqls) {
				if (ksqlAccessor instanceof KagerowSqlAccessorImpl impl) {
					impl.name = parseEnv(envParser, impl.name);
					impl.id = parseEnv(envParser, impl.id);
					impl.next = parseEnv(envParser, impl.next);
					impl.sql = parseEnv(envParser, impl.sql);
					Map<String, String> variable = new ConcurrentHashMap<>();
					for (Map.Entry<String, String> entry : impl.variable.entrySet()) {
						String paramName = parseEnv(envParser, entry.getKey());
						String paramValue = parseEnv(envParser, entry.getValue());
						variable.put(paramName, paramValue);
					}
					impl.variable = variable;
				}
			}
			for (KagerowCmdAccessor cmdAccessor : accessor.command) {
				if (cmdAccessor instanceof KagerowCmdAccessorImpl impl) {
					impl.cmd = parseEnv(envParser, impl.cmd);
					Map<String, String> environmental = new ConcurrentHashMap<>();
					for (Map.Entry<String, String> entry : impl.environmental.entrySet()) {
						String paramName = parseEnv(envParser, entry.getKey());
						String paramValue = parseEnv(envParser, entry.getValue());
						environmental.put(paramName, paramValue);
					}
					impl.environmental = environmental;
				}
			}

			return accessor;
		} catch (CloneNotSupportedException _) {
			return this;
		}
	}

	/**
	 * 対象を環境変数込みに表現に変換する処理を提供します
	 * @param envParser パーサー
	 * @param target 処理対象
	 * @return 処理結果
	 */
	private String parseEnv(KsqlReplaceEnvParser envParser, String target) {
		String result = target;
		try {
			result = envParser.transform(target);
		} catch (KSQLParseException e) {
			// パーサーのロジック的にシステム管理下の環境変数ではパースエラーが発生しない
			// 将来拡張した際、ログで気付けるよう考慮
			KagerowLogger.newAppLogger().log(
					Level.WARNING,
					AppLogMessage.WARNING_MSG9007.name(),
					new Object[] { target.substring(e.getStartIndex(), e.getEndIndex()) });
		}
		return result;
	}

	/**
	 * XMLバリデーション結果例外の翻訳例外を構築します
	 * @param target XMLバリデーション結果例外
	 * @return 翻訳例外
	 */
	protected static KFileParseException toKFileParseException(SAXParseException target) {

		// 解析対象メッセージ取得
		String msg = target.getMessage();

		// 要素と期待値を抜く
		Pattern p = Pattern.compile("(要素|element)'(.+?)'.*?\\{(.+?)\\}");
		Matcher m = p.matcher(msg);

		// 例外メッセージ向けに要素名を取得
		String actual = null;
		String expected = null;
		if (m.find()) {
			actual = m.group(2);
			expected = m.group(3);
		}

		// 例外メッセージ向けに行番号・列番号を取得
		int line = target.getLineNumber();
		int col = target.getColumnNumber();

		// 例外メッセージ向けに構築
		if (actual != null && expected != null) {
			// 要素名が取得できた場合
			return new KFileParseException(ErrorMessage.CODE_025.getMessage(line, col, actual, expected), target);
		} else {
			// 要素名が取得できなかった場合
			return new KFileParseException(ErrorMessage.CODE_026.getMessage(line, col, msg), target);
		}
	}

}

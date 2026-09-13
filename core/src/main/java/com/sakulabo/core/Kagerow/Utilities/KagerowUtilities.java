package com.sakulabo.core.Kagerow.Utilities;

import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.SecretKey;
import javax.naming.Binding;
import javax.naming.CompositeName;
import javax.naming.CompoundName;
import javax.naming.Context;
import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.NamingException;

import com.sakulabo.core.Common.AppPathUtils;
import com.sakulabo.core.Common.StringUtils;
import com.sakulabo.core.Kagerow.KagerowApplication;
import com.sakulabo.core.Kagerow.Contents.KagerowPluginContent;
import com.sakulabo.core.Kagerow.Contents.KagerowSecurityContent;
import com.sakulabo.core.Kagerow.Contents.KagerowSettingContent;
import com.sakulabo.core.Kagerow.Contents.Impl.KagerowPluginContentImpl;
import com.sakulabo.core.Kagerow.Contents.KagerowSecurityContent.SecureObject;
import com.sakulabo.core.Kagerow.Context.KagerowContexts;
import com.sakulabo.core.Kagerow.Context.KagerowPluginContext;
import com.sakulabo.core.Kagerow.Context.KagerowPluginPackageContext;
import com.sakulabo.core.Kagerow.Context.KagerowSecurityContext;
import com.sakulabo.core.Kagerow.Context.KagerowSettingContext;
import com.sakulabo.core.Kagerow.Context.Impl.KagerowPluginPackageContextImpl;
import com.sakulabo.core.Kagerow.Context.Impl.KagerowSecurityContextImpl;
import com.sakulabo.core.Kagerow.Context.Impl.KagerowSettingContextImpl;
import com.sakulabo.core.Processor.aop.CommonAOPInvocationHandlProcessor;
import com.sakulabo.core.Processor.plugin.PluginParamParser;
import com.sakulabo.core.Provides.LoardDIBeansProvider.Key;
import com.sakulabo.regulation.annotation.KagerowPlugin.PluginType;
import com.sakulabo.regulation.spi.LoardDIBeansAdapter;
import com.sakulabo.regulation.spi.PluginAdapter.PluginValidationException;

/**
 * Kagerowアプリケーションの汎用クラスです
 *
 * @author keeeeeent
 */
public final class KagerowUtilities {

	/** バージョン管理番号正規表現 */
	private static final Pattern VERSION_REG = Pattern
			.compile("^(?<major>0|[1-9]\\d*)(?:\\.(?<minor>0|[1-9]\\d*))?(?:\\.(?<patch>0|[1-9]\\d*))?$");

	/**
	 * インスタンス生成禁止
	 */
	private KagerowUtilities() {
		;
	}

	/**
	 * Beanコンテキストから取得します
	 *
	 * @param <T>
	 * @param target 取得対象の型情報
	 * @param name   Beanの名称、名称はnullの場合defaultとして解釈されます
	 * @return 取得結果
	 */
	public static <T> Optional<T> getBean(Class<T> target, String name) {
		// 名称の検証
		name = Objects.isNull(name) ? StringUtils.DEFAULT : name;
		// Typeの取得
		String type = target.getName();
		// Keyの生成
		Key key = new Key(name, type, null);
		// コンテキストの取得
		LoardDIBeansAdapter<Binding> provider = KagerowApplication.getInstance().getDIContext();
		// Beanの取得
		@SuppressWarnings("unchecked")
		Optional<T> result = (Optional<T>) provider.lookUp(provider.createContext(), key);
		return result;
	}

	/**
	 * プロキシーインスタンスから元となったインスタンスを取得します<br/>
	 * 対象のインスタンスがプロキシではなかった場合、インスタンス自体をそのまま返却します
	 *
	 * @param <T>
	 * @param proxy 対象インスタンス
	 * @return 元になったインスタンス
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getRawInstance(Object proxy) {
		// 返却用インスタンス初期化
		T result = (T) proxy;
		// プロキシーインスタンスの場合生のインスタンスを取得
		if (Proxy.isProxyClass(proxy.getClass())) {
			CommonAOPInvocationHandlProcessor handler = (CommonAOPInvocationHandlProcessor) Proxy
					.getInvocationHandler(proxy);
			result = (T) handler.getRawInstance();
		}
		return result;
	}

	/**
	 * 指定された名称に関連するデフォルトプラグインを取得します
	 *
	 * @param name デフォルトプラグイン名称
	 * @return プラグインインスタンス
	 * @throws NamingException プラグイン取得失敗
	 */
	public static KagerowPluginContent getPlugin(String name) throws NamingException {
		// 名称生成
		Name packageName = createVersioningPluginPkgName(null, null);
		// プラグインを取得
		return (KagerowPluginContent) getPlugin(packageName, name);
	}

	/**
	 * 指定された名称に関連するプラグインを取得します
	 *
	 * @param packageName パッケージ名称
	 * @param name        プラグイン名称
	 * @return プラグインインスタンス
	 * @throws NamingException プラグイン取得失敗
	 */
	public static KagerowPluginContent getPlugin(Name packageName, String name) throws NamingException {
		// プラグインパッケージコンテキストを取得
		KagerowPluginPackageContext ctx = (KagerowPluginPackageContext) KagerowApplication.getInstance().getContext()
				.lookup(KagerowPluginPackageContext._NAME);
		// 名称インスタンスの正規化
		packageName = new CompoundName(packageName.toString(), KagerowPluginPackageContextImpl.PROPS);
		// プラグインコンテキストを取得
		KagerowPluginContext cont = ctx.lookup(packageName);
		// プラグインを取得
		return (KagerowPluginContent) cont.lookup(name);
	}

	/**
	 * バージョニング管理されたプラグインパッケージ名称を生成します
	 *
	 * @param packageName パッケージ名称
	 * @param version     バージョン番号
	 * @return 名称インスタンス
	 * @throws NamingException プラグイン取得失敗
	 */
	public static Name createVersioningPluginPkgName(String packageName, String version) throws NamingException {
		// パラメータ正規化
		if (Objects.nonNull(version)) {
			version = version.strip();
		}
		if (Objects.nonNull(packageName)) {
			packageName = packageName.strip();
		}
		// パッケージ未指定、もしくは空文字の場合はデフォルトを設定
		if (Objects.isNull(packageName) || StringUtils.EMPTY.equals(packageName)) {
			packageName = StringUtils.DEFAULT;
		}
		// バージョン未指定、もしくは空文字の場合はlatestで返却
		if (Objects.isNull(version) || StringUtils.EMPTY.equals(version)) {
			return new CompoundName(packageName, KagerowPluginPackageContextImpl.PROPS);
		}
		// バージョン初期化
		int major, minor, patch;
		Matcher matcher = VERSION_REG.matcher(version);
		if (matcher.matches()) {
			MatchResult matchResult = matcher.toMatchResult();
			major = Integer.parseInt(matchResult.group("major"));
			minor = matchResult.group("minor") == null ? 0 : Integer.parseInt(matchResult.group("minor"));
			patch = matchResult.group("patch") == null ? 0 : Integer.parseInt(matchResult.group("patch"));
		} else {
			// フォーマット不正なバージョン番号の場合、latestで返却
			return new CompoundName(packageName, KagerowPluginPackageContextImpl.PROPS);
		}
		// バージョン指定が自然数か判定
		if (0 <= major && 0 <= minor && 0 <= patch) {
			// バージョン生成
			return createVersioningPluginPkgName(packageName, major, minor, patch);
		}
		// バージョン指定が不正な場合、例外をスロー
		throw new IllegalArgumentException();
	}

	/**
	 * バージョニング管理されたプラグインパッケージ名称を生成します
	 *
	 * @param packageName パッケージ名称
	 * @param major       メジャーバージョン番号
	 * @param minor       マイナーバージョン番号
	 * @param patch       パッチバージョン番号
	 * @return 名称インスタンス
	 * @throws NamingException プラグイン取得失敗
	 */
	public static Name createVersioningPluginPkgName(String packageName, int major, int minor, int patch)
			throws NamingException {
		// ルックアップキー生成
		Name lookupKey = new CompoundName(MessageFormat.format("{0}/{1}/{2}/{3}", packageName, major, minor, patch),
				KagerowPluginPackageContextImpl.PROPS);
		return lookupKey;
	}

	/**
	 * 指定されたネームスペースに関連づけられた設定値を取得します
	 *
	 * @param namespace ネームスペース
	 * @param name      設定値名称
	 * @return 設定値
	 */
	public static String getSetting(String namespace, String name) {
		// セッテイングを取得
		String setting = null;
		try {
			// セッテイングコンテキストを取得
			KagerowSettingContext ctx = (KagerowSettingContext) KagerowApplication.getInstance().getContext()
					.lookup(KagerowSettingContext._NAME);
			// セッテイングコンテンツ取得
			KagerowSettingContent content = ctx.lookup(namespace);
			// 設定値取得
			setting = content.lookup(name);
		} catch (NamingException e) {
			;
		}
		return setting;
	}

	/**
	 * 指定されたネームスペースに関連づけられた設定値を取得します 値が関連付けされていない場合、デフォルト値を返却します
	 *
	 * @param namespace    ネームスペース
	 * @param name         設定値名称
	 * @param defaultValue デフォルト値
	 * @return 設定値
	 */
	public static String getSetting(String namespace, String name, String defaultValue) {
		Objects.requireNonNull(defaultValue);
		// セッテイングを取得
		String setting = getSetting(namespace, name);
		// 取得失敗の場合、デフォルト値を適用
		if (Objects.isNull(setting)) {
			setting = defaultValue;
		}
		return setting;
	}

	/**
	 * 指定されたネームスペースに関連づけられた設定値を取得します 値が関連付けされていない場合、デフォルト値を登録し返却します
	 *
	 * @param namespace    ネームスペース
	 * @param name         設定値名称
	 * @param defaultValue デフォルト値
	 * @return 設定値
	 * @throws NamingException セッテイングコンテキストが見つからない場合
	 */
	public static String getSettingIfAbsent(String namespace, String name, String defaultValue) throws NamingException {
		// セッテイングを取得
		String setting = getSetting(namespace, name, defaultValue);
		if (defaultValue.equals(setting)) {
			// 未登録設定の場合データを設定
			setSetting(namespace, name, setting);
		}
		return setting;
	}

	/**
	 * 指定されたネームスペースに設定値を関連づけます
	 *
	 * @param namespace ネームスペース
	 * @param name      設定値名称
	 * @param value     設定値
	 * @throws NamingException セッテイングコンテキストが見つからない場合
	 */
	public static void setSetting(String namespace, String name, String value) throws NamingException {
		// セッテイングコンテキストを取得
		KagerowSettingContext ctx = (KagerowSettingContext) KagerowApplication.getInstance().getContext()
				.lookup(KagerowSettingContext._NAME);
		// セッテイングコンテキスト初期化
		KagerowSettingContent content;
		try {
			// セッテイングコンテンツ取得
			content = ctx.lookup(namespace);

		} catch (NamingException e) {
			// 存在しない場合新規作成
			content = ctx.createSubcontext(namespace);
		}
		// 設定値バインド
		content.bind(name, value);
	}

	/**
	 * 指定されたコンテキストに関連付けされた環境変数を取得します
	 *
	 * @param contextName コンテキスト名称
	 * @param envName     環境変数名
	 * @return 環境変数
	 * @throws NamingException コンテキスト取得失敗
	 */
	public static String getENV(String contextName, String envName) throws NamingException {
		// コンテキストを取得
		Context ctx = (Context) KagerowApplication.getInstance().getContext().lookup(contextName);
		// 環境変数返却
		String env = (String) ctx.getEnvironment().get(envName);
		return env;
	}

	/**
	 * プラグインコンテキストに関連付けされた環境変数を取得します
	 *
	 * @param envName 環境変数名
	 * @return 環境変数
	 * @throws NamingException コンテキスト取得失敗
	 */
	public static String getPluginENV(String envName) throws NamingException {
		// コンテキストを取得
		Context ctx = (Context) KagerowApplication.getInstance().getContext().lookup(KagerowPluginPackageContext._NAME);
		// サブコンテキストを取得
		Context subCtx = (Context) ctx.lookup(StringUtils.DEFAULT);
		// 環境変数返却
		String env = (String) subCtx.getEnvironment().get(envName);
		return env;
	}

	/**
	 * 指定されたコンテキストを返却します
	 *
	 * @param <T>
	 * @param target 対象コンテキスト名称
	 * @return 取得コンテキスト
	 * @throws NamingException コンテキスト取得失敗
	 */
	@SuppressWarnings("unchecked")
	public static <T extends KagerowContexts<?>> T getContext(String target) throws NamingException {
		return (T) KagerowApplication.getInstance().getContext().lookup(target);
	}

	/**
	 * 指定されたデフォルトプラグインのパラメーターをバインドします<br/>
	 * 返却されるインスタンスはシャローコピーにて新規作成されます
	 *
	 * @param pluginName    バインドするデフォルトプラグインの名称
	 * @param type          入出力モード
	 * @param baseParamList バインド元のマップ
	 * @return バインド済みマップ
	 * @throws PluginValidationException 必須チェックエラー
	 * @throws NamingException           プラグインが見つからなかった場合
	 * @throws InvalidNameException      名称オブジェクト生成失敗
	 */
	public static Map<String, String> bindPluginParam(String pluginName, PluginType type,
			Map<String, String> baseParamList) throws PluginValidationException, NamingException, InvalidNameException {
		return bindPluginParam(StringUtils.DEFAULT, pluginName, type, baseParamList);

	}

	/**
	 * 指定されたプラグインのパラメーターをバインドします<br/>
	 * 返却されるインスタンスはシャローコピーにて新規作成されます
	 *
	 * @param packageName   パッケージ名称
	 * @param pluginName    バインドするプラグインの名称
	 * @param type          入出力モード
	 * @param baseParamList バインド元のマップ
	 * @return バインド済みマップ
	 * @throws PluginValidationException 必須チェックエラー
	 * @throws NamingException           プラグインが見つからなかった場合
	 * @throws InvalidNameException      名称オブジェクト生成失敗
	 */
	public static Map<String, String> bindPluginParam(String packageName, String pluginName, PluginType type,
			Map<String, String> baseParamList) throws PluginValidationException, NamingException, InvalidNameException {

		// 名称生成
		Name pkgName = new CompoundName(packageName, KagerowPluginPackageContextImpl.PROPS);
		// プラグインコンテンツ取得
		KagerowPluginContentImpl content = (KagerowPluginContentImpl) getPlugin(pkgName, pluginName);
		// 処理をサポートしているか確認
		if (!content.isSupportType(type)) {
			// サポートしていない場合処理を中断
			return Collections.emptyMap();
		}

		// パラメータバインド
		PluginParamParser paramParser = new PluginParamParser(content.toAdapter());
		String paramType = PluginType.INPUT.equals(type) ? PluginParamParser.INPUT : PluginParamParser.OUTPUT;
		Map<String, String> paramList = paramParser.getPluginParam(baseParamList, paramType);

		return paramList;

	}

	/**
	 * 指定したデフォルトプラグインが指定した入出力モードをサポートしているか判定します
	 *
	 * @param pluginName プラグイン名称
	 * @param type       入出力モード
	 * @return 判定結果
	 * @throws NamingException プラグインが見つからなかった場合
	 */
	public static boolean isSupportPluginType(String pluginName, PluginType type) throws NamingException {
		return isSupportPluginType(StringUtils.DEFAULT, pluginName, type);
	}

	/**
	 * 指定したプラグインが指定した入出力モードをサポートしているか判定します
	 *
	 * @param packageName パッケージ名称
	 * @param pluginName  プラグイン名称
	 * @param type        入出力モード
	 * @return 判定結果
	 * @throws NamingException プラグインが見つからなかった場合
	 */
	public static boolean isSupportPluginType(String packageName, String pluginName, PluginType type)
			throws NamingException {
		// 名称生成
		Name pkgName = new CompoundName(packageName, KagerowPluginPackageContextImpl.PROPS);
		// プラグインコンテンツ取得
		KagerowPluginContentImpl content = (KagerowPluginContentImpl) getPlugin(pkgName, pluginName);
		// 処理をサポートしているか確認
		return content.isSupportType(type);
	}

	/**
	 * 指定されたデフォルトプラグインのパラメーターを生成します<br/>
	 * 返却されるインスタンスはシャローコピーにて新規作成されます
	 *
	 * @param pluginName バインドするデフォルトプラグインの名称
	 * @param type       入出力モード
	 * @return 初期化パラメータ
	 * @throws InvalidNameException 名称オブジェクト生成失敗
	 * @throws NamingException      プラグイン取得失敗
	 */
	public static Map<String, String> createPluginParam(String pluginName, PluginType type)
			throws InvalidNameException, NamingException {
		return createPluginParam(StringUtils.DEFAULT, pluginName, type);
	}

	/**
	 * 指定されたプラグインのパラメーターを生成します<br/>
	 * 返却されるインスタンスはシャローコピーにて新規作成されます
	 *
	 * @param packageName パッケージ名称
	 * @param pluginName  バインドするプラグインの名称
	 * @param type        入出力モード
	 * @return 初期化パラメータ
	 * @throws InvalidNameException 名称オブジェクト生成失敗
	 * @throws NamingException      プラグイン取得失敗
	 */
	public static Map<String, String> createPluginParam(String packageName, String pluginName, PluginType type)
			throws InvalidNameException, NamingException {

		// 名称生成
		Name pkgName = new CompoundName(packageName, KagerowPluginPackageContextImpl.PROPS);
		// プラグインコンテンツ取得
		KagerowPluginContentImpl content = (KagerowPluginContentImpl) getPlugin(pkgName, pluginName);
		// 処理をサポートしているか確認
		if (!content.isSupportType(type)) {
			// サポートしていない場合処理を中断
			return Collections.emptyMap();
		}

		// パラメータバインド
		PluginParamParser paramParser = new PluginParamParser(content.toAdapter());
		String paramType = PluginType.INPUT.equals(type) ? PluginParamParser.INPUT : PluginParamParser.OUTPUT;
		Map<String, String> paramList = paramParser.createParam(paramType);

		return paramList;

	}

	/**
	 * コンフィグを読み込みセキュアネームスペースがあるかを判定します<br/>
	 * 取得結果はKagerowがセキュア起動有無と同義です
	 *
	 * @return 判定結果
	 */
	public static boolean isSecure() {
		return KagerowSettingContextImpl.isSecure();
	}

	/**
	 * ランタイムパスに対応するパスをファイル名から生成します
	 *
	 * @param fileName ファイル名
	 * @param delete   削除フラグ
	 * @return ランタイムパス
	 * @throws IllegalStateException 既にファイルが存在する場合
	 */
	public static Path createRuntimePath(String fileName, boolean delete) {
		Objects.requireNonNull(fileName);
		Path runtimePath = AppPathUtils.createRuntimeDirPath();
		Path result = runtimePath.resolve(fileName);
		if (Files.exists(result)) {
			throw new IllegalStateException();
		}
		if (delete) {
			result.toFile().deleteOnExit();
		}
		return result;
	}

	/**
	 * ランタイムパスに対応するパスをファイル名から生成します 本メソッドから生成されたパスに対応するファイルはシステム終了時削除されません
	 *
	 * @param fileName ファイル名
	 * @return ランタイムパス
	 * @throws IllegalStateException 既にファイルが存在する場合
	 */
	public static Path createRuntimePath(String fileName) {
		return createRuntimePath(fileName, false);
	}

	/**
	 * ランタイムパスに既に同盟ファイルが存在するか判定します
	 *
	 * @param fileName ファイル名称
	 * @return 判定結果
	 */
	public static boolean isExistRuntimePath(String fileName) {
		Path runtimePath = AppPathUtils.createRuntimeDirPath();
		Path result = runtimePath.resolve(fileName);
		return Files.exists(result);
	}

	/**
	 * 一時ファイルパスを生成します
	 *
	 * @param fileName ファイル名
	 * @param delete   削除フラグ
	 * @return ランタイムパス
	 * @throws IllegalStateException 既にファイルが存在する場合
	 */
	public static Path createTemporaryPath(String fileName, boolean delete) {
		Objects.requireNonNull(fileName);
		Path tmpPath = AppPathUtils.createTemporaryDirPath();
		Path result = tmpPath.resolve(fileName);
		if (Files.exists(result)) {
			throw new IllegalStateException();
		}
		if (delete) {
			result.toFile().deleteOnExit();
		}
		return result;
	}

	/**
	 * 一時ファイルパスを生成します 本メソッドから生成されたパスに対応するファイルはシステム終了時削除されません
	 *
	 * @param fileName ファイル名
	 * @return ランタイムパス
	 * @throws IllegalStateException 既にファイルが存在する場合
	 */
	public static Path createTemporaryPath(String fileName) {
		return createTemporaryPath(fileName, false);
	}

	/**
	 * 一時ファイルパスに既に同盟ファイルが存在するか判定します
	 *
	 * @param fileName ファイル名称
	 * @return 判定結果
	 */
	public static boolean isExistemporaryPath(String fileName) {
		Path tmpPath = AppPathUtils.createTemporaryDirPath();
		Path result = tmpPath.resolve(fileName);
		return Files.exists(result);
	}

	/**
	 * アプリケーションがインストールされているホームディレクトリのパスを生成します
	 *
	 * @return 生成されたパス
	 */
	public static Path createAppDirPath() {
		return AppPathUtils.createAppDirPath();
	}

	/**
	 * 一時フォルダパスを生成します
	 *
	 * @return 生成されたパス
	 */
	public static Path createTemporaryDirPath() {
		return AppPathUtils.createTemporaryDirPath();
	}

	/**
	 * アプリケーションが管理しているホームディレクトリのパスを生成します
	 *
	 * @return 生成されたパス
	 */
	public static Path createKagerowHomePath() {
		return AppPathUtils.createKagerowHomePath();
	}

	/**
	 * 秘密鍵をシステム管理下で保管します
	 * @param name 秘密鍵名称
	 * @param secretKey 秘密鍵
	 * @return 保存パスワード
	 * @throws NamingException セキュアブートを行っていない場合
	 */
	public static Optional<String> registSecretKey(String name, SecretKey secretKey) throws NamingException {
		// コンテキスト取得
		KagerowSecurityContextImpl ctx = (KagerowSecurityContextImpl) getContext(KagerowSecurityContext._NAME);
		// コンテンツ取得
		if (ctx.getMasterKey().isEmpty()) {
			return Optional.empty();
		}
		KagerowSecurityContent cnt = ctx.getMasterKey().get();
		// 秘密鍵登録
		String pass = cnt.bind(name, secretKey);
		return Optional.of(pass);
	}

	/**
	 * システム管理の秘密鍵を取得します
	 * @param name 秘密鍵名称
	 * @param pass 保存パスワード
	 * @return 秘密鍵
	 * @throws NamingException セキュアブートを行っていない場合
	 */
	public static Optional<SecretKey> selectSecretKey(String name, String pass) throws NamingException {
		// コンテキスト取得
		KagerowSecurityContextImpl ctx = (KagerowSecurityContextImpl) getContext(KagerowSecurityContext._NAME);
		// コンテンツ取得
		if (ctx.getMasterKey().isEmpty()) {
			return Optional.empty();
		}
		KagerowSecurityContent cnt = ctx.getMasterKey().get();
		// 検索キー生成
		CompositeName key = new CompositeName(String.join(StringUtils.SLASH_DELIMIT, name, pass));
		// 秘密鍵取得
		SecureObject secretKey = cnt.lookup(key);
		// 秘密鍵返却
		SecretKey secKey = (SecretKey) secretKey.resultKey();
		return Optional.of(secKey);
	}

}

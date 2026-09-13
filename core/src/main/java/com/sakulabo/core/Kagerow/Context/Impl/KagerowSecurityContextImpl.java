package com.sakulabo.core.Kagerow.Context.Impl;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.Key;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.naming.CannotProceedException;
import javax.naming.CompositeName;
import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.NotContextException;

import com.sakulabo.core.Common.AppPathUtils;
import com.sakulabo.core.Common.StringUtils;
import com.sakulabo.core.Common.URINameParser;
import com.sakulabo.core.Kagerow.Contents.KagerowSecurityContent;
import com.sakulabo.core.Kagerow.Contents.KagerowSecurityContent.SecureObject;
import com.sakulabo.core.Kagerow.Contents.KagerowSettingContent;
import com.sakulabo.core.Kagerow.Contents.Impl.KagerowSecurityContentImpl;
import com.sakulabo.core.Kagerow.Contents.Impl.KagerowSecurityContentImpl.KagerowMasterSecurityContentImpl;
import com.sakulabo.core.Kagerow.Context.KagerowSecurityContext;
import com.sakulabo.core.Kagerow.Context.KagerowSettingContext;
import com.sakulabo.core.Kagerow.Exception.ApplicationError;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;
import com.sakulabo.core.Kagerow.Utilities.KagerowUtilities;
import com.sakulabo.core.Processor.config.ContextConfigurationLorder;
import com.sakulabo.core.Processor.jmx.AppJMX;
import com.sakulabo.core.Processor.jmx.Context.KagerowSecurityContextMXBean;
import com.sakulabo.core.Processor.security.AESKeyCreater;
import com.sakulabo.core.Processor.security.AppAESPassEncrypter;
import com.sakulabo.core.Processor.security.AppHashEncrypter;
import com.sakulabo.core.Processor.security.PBEKeyEncrypter;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Kagerowが管理するセキュリティアクセスコンテンツです
 *
 * @author keeeeeent
 */
@AppJMX(name = "Context", options = { "type=KagerowSecurityContext" })
public final class KagerowSecurityContextImpl extends BaseKagerowContext<KagerowSecurityContent>
		implements KagerowSecurityContext, KagerowSecurityContextMXBean {

	/** コンテキストメモリ */
	private volatile static Map<Name, KagerowSecurityContent> _CONTEXT = new ConcurrentHashMap<>();
	/** コンテキスト環境変数メモリ */
	private static final Map<String, String> _ENV;
	static {
		_ENV = ContextConfigurationLorder.getInstance().SECURITY_CONTEXT_ENV();
	}
	/** マスターkeystoreファイル名称 */
	public static final String MASTER_KEY_STORE_FILE_NAME = "master-enckey.pfx";
	/** コンテキストkeystoreファイル名称 */
	public static final String CONTEXT_KEY_STORE_FILE_NAME = "context-enckey.pfx";
	/** マスターkeystore属性名称 */
	public static final String KEY_STORE_TYPE = "PKCS12";
	/** マスターkey生成用セッティングコンテキストネームスペース */
	public static final String SETTING_NAME_SPACE = "KagerowSecurityContext";
	/** マスターkey生成用ソルトルックアップキー */
	public static final String SALT_KEY_NAME = "salt";
	/** マスターkey生成用ソルトルックアップキー */
	public static final String IV_KEY_NAME = "iv";
	/** 擬似真正乱数発生インスタンス */
	private static final SecureRandom RAND = new SecureRandom();
	/** セッテイングコンテキスト */
	private static volatile KagerowSettingContextImpl cont;
	/** マスターキー暗号化機構 */
	private static volatile PBEKeyEncrypter encrypter;

	/**
	 * デフォルトコンストラクタ
	 * @param pass キーストアパスワード
	 * @param cont セッテイングコンテキスト
	 */
	protected KagerowSecurityContextImpl(String pass, KagerowSettingContextImpl cont) {

		// スーパークラスコンストラクタ呼び出し
		super(_CONTEXT, _ENV, KagerowSecurityContext._NAME);
		// MXBeanの登録
		registMXBean(this);
		// セッテイングコンテキスト初期化
		KagerowSecurityContextImpl.cont = cont;

		try {

			// セッティングコンテンツ取得
			KagerowSettingContent settingContent;
			try {
				settingContent = cont.lookup(SETTING_NAME_SPACE);
			} catch (NotContextException e) {
				settingContent = cont.createSubcontext(SETTING_NAME_SPACE);
			}

			// ソルト初期化
			byte[] salt = AESKeyCreater.getSalt();
			// ソルト取得
			String baseSalt = (String) settingContent.lookup(SALT_KEY_NAME);
			// ソルトが取得できた場合デコードしてバイト配列にする
			if (Objects.nonNull(baseSalt)) {
				int padding = baseSalt.length() % 4;
				baseSalt = baseSalt.concat(StringUtils.EQUAL.repeat(padding));
				salt = Base64.getDecoder().decode(baseSalt);
			} else {
				// ランダムソルト生成
				RAND.nextBytes(salt);
				String saltStr = Base64.getEncoder().withoutPadding().encodeToString(salt);
				settingContent.bind(SALT_KEY_NAME, saltStr);
			}

			// 初期化ベクトル初期化
			byte[] iv = AESKeyCreater.getIV();
			// 初期化ベクトル取得
			String baseIv = (String) settingContent.lookup(IV_KEY_NAME);
			if (Objects.nonNull(baseIv)) {
				int padding = baseIv.length() % 4;
				baseIv = baseIv.concat(StringUtils.EQUAL.repeat(padding));
				iv = Base64.getDecoder().decode(baseIv);
			} else {
				// ランダム初期化ベクトル生成
				RAND.nextBytes(iv);
				String ivStr = Base64.getEncoder().withoutPadding().encodeToString(iv);
				settingContent.bind(IV_KEY_NAME, ivStr);
			}

			// マスターキーストア初期化
			encrypter = new PBEKeyEncrypter(pass, salt, iv);
			// keystore格納先パスを生成
			Path masterKeyPath = AppPathUtils.createSettingDirPath().resolve(MASTER_KEY_STORE_FILE_NAME);
			// keystoreファイルを初期化
			KeyStore keyStore = KeyStore.getInstance(KEY_STORE_TYPE);
			// keystoreをコンテキストへ登録
			bind(KagerowMasterSecurityContentImpl.MASTER_KEY,
					new KagerowMasterSecurityContentImpl(masterKeyPath.toUri(), keyStore, encrypter));

		} catch (Exception e) {
			throw new ApplicationError(e);
		}

	}

	/**
	 * パスワードを再設定します
	 * @param pass 再設定するパスワード
	 */
	@SuppressFBWarnings("SSD_DO_NOT_USE_INSTANCE_LOCK_ON_SHARED_STATIC_DATA")
	public synchronized void changePass(String pass) {
		try {

			// セッテイングコンテキスト取得
			KagerowSettingContext cont = KagerowUtilities.getContext(KagerowSettingContext._NAME);

			// セッティングコンテンツ取得
			KagerowSettingContent settingContent;
			try {
				settingContent = cont.lookup(KagerowSecurityContextImpl.SETTING_NAME_SPACE);
			} catch (NotContextException e) {
				settingContent = cont.createSubcontext(KagerowSecurityContextImpl.SETTING_NAME_SPACE);
			}

			// ソルト初期化
			byte[] salt = AESKeyCreater.getSalt();
			// ランダムソルト生成
			RAND.nextBytes(salt);
			// 設定上書き
			String saltStr = Base64.getEncoder().withoutPadding().encodeToString(salt);
			settingContent.bind(KagerowSecurityContextImpl.SALT_KEY_NAME, saltStr);

			// 初期化ベクトル初期化
			byte[] iv = AESKeyCreater.getIV();
			// ランダム初期化ベクトル生成
			RAND.nextBytes(iv);
			// 設定上書き
			String ivStr = Base64.getEncoder().withoutPadding().encodeToString(iv);
			settingContent.bind(KagerowSecurityContextImpl.IV_KEY_NAME, ivStr);

			// 古いパスワード生成インスタンスを保管
			PBEKeyEncrypter oldEncrypter = encrypter;
			// パスワード生成インスタンス際初期化
			encrypter = new PBEKeyEncrypter(pass, salt, iv);
			// マスターコンテンツ取得
			KagerowMasterSecurityContentImpl securityContent = (KagerowMasterSecurityContentImpl) _CONTEXT
					.get(new CompositeName(KagerowMasterSecurityContentImpl.MASTER_KEY));
			// マスターキーストア再初期化
			securityContent.changePass(encrypter);

			// 調整済みのパスワード
			String password = oldEncrypter.getPassword();
			// URLセーフに修正
			password = Base64.getUrlEncoder().encodeToString(password.getBytes(StandardCharsets.UTF_8));

			// 調整済みのパスワード
			String newPassword = encrypter.getPassword();
			// URLセーフに修正
			newPassword = Base64.getUrlEncoder().encodeToString(newPassword.getBytes(StandardCharsets.UTF_8));

			// マスターキーに紐づく秘密鍵を再設定
			securityContent.rename(
					KagerowMasterSecurityContentImpl.MASTER_KEY.concat(StringUtils.SLASH_DELIMIT + password),
					KagerowMasterSecurityContentImpl.MASTER_KEY.concat(StringUtils.SLASH_DELIMIT + newPassword));

			// リスナー起動
			callListener(KagerowContextEventKind.RENAME);

		} catch (Exception e) {
			throw new ApplicationError(e);
		}
	}

	/** {@inheritDoc} */
	@Override
	public KagerowSecurityContent lookup(Name name) throws NamingException {
		return (KagerowSecurityContent) super.lookup(name);
	}

	/** {@inheritDoc} */
	@Override
	public KagerowSecurityContent lookup(String name) throws NamingException {
		return (KagerowSecurityContent) super.lookup(name);
	}

	/** {@inheritDoc} */
	@Override
	public KagerowSecurityContent createSubcontext(Name name) throws NamingException {

		try {
			// URIを生成し、仮想ファイルシステム内部に接続するためのパスを生成する
			final URINameParser parser = new URINameParser(name);
			final URI uri = parser.toURI(CONTEXT_KEY_STORE_FILE_NAME);

			// キー初期化
			AppHashEncrypter encrypter = new AppHashEncrypter(name.toString());

			// keystoreファイル新規作成
			KeyStore keyStore = KeyStore.getInstance(KEY_STORE_TYPE);

			// コンテンツ作成
			KagerowSecurityContent content = new KagerowSecurityContentImpl(uri, keyStore, encrypter);

			// keystoreをコンテキストへ登録
			bind(name, content);

			// リスナー起動
			callListener(KagerowContextEventKind.CREATE_SUB_CONTEXT);

			return content;

		} catch (Exception e) {
			CannotProceedException exception = new CannotProceedException(e.getMessage());
			exception.setRootCause(e);
			throw exception;
		}

	}

	/** {@inheritDoc} */
	@Override
	public KagerowSecurityContent createSubcontext(String name) throws NamingException {
		Name named = new CompositeName(name);
		return createSubcontext(named);
	}

	/** {@inheritDoc} */
	@Override
	public Map<String, String> getContext() {
		Map<String, String> jmxMap = new HashMap<>();
		for (Map.Entry<Name, ?> jmxTarget : _CONTEXT.entrySet()) {
			jmxMap.put(jmxTarget.getKey().toString(), jmxTarget.getValue().getClass().getCanonicalName());
		}
		return jmxMap;
	}

	/**
	 * 内部コンテキストをリフレッシュしまうす
	 */
	public static void refresh() {
		_CONTEXT = new ConcurrentHashMap<>();
	}

	/**
	 * Kagerowセキュリティーコンテキストの初期化を行います
	 * @param uri エントリー直結URI(FileSystemの向き先が仮想FS)
	 * @param name コンテキスト登録名称
	 * @throws Exception
	 */
	static void initialize(URI uri, String name)
			throws Exception {

		// マスターコンテンツ取得
		KagerowSecurityContent securityContent = _CONTEXT
				.get(new CompositeName(KagerowMasterSecurityContentImpl.MASTER_KEY));

		// マスターコンテンツが存在しない場合、初期化はせず即終了
		if (Objects.isNull(securityContent)) {
			return;
		}

		// URI変換
		uri = URINameParser.joinURI(uri, CONTEXT_KEY_STORE_FILE_NAME);

		// セッティングコンテンツ取得
		KagerowSettingContent settingContent;
		try {
			settingContent = KagerowSecurityContextImpl.cont.lookup(SETTING_NAME_SPACE);
		} catch (NotContextException e) {
			settingContent = KagerowSecurityContextImpl.cont.createSubcontext(SETTING_NAME_SPACE);
		}

		// 初期化ベクトル初期化
		byte[] iv = AESKeyCreater.getIV();
		String ivKey = IV_KEY_NAME.concat(name);
		// 初期化ベクトル取得
		String baseIv = (String) settingContent.lookup(ivKey);
		if (Objects.nonNull(baseIv)) {
			int padding = baseIv.length() % 4;
			baseIv = baseIv.concat(StringUtils.EQUAL.repeat(padding));
			iv = Base64.getDecoder().decode(baseIv);
		} else {
			// ランダム初期化ベクトル生成
			RAND.nextBytes(iv);
			String ivStr = Base64.getEncoder().withoutPadding().encodeToString(iv);
			settingContent.bind(ivKey, ivStr);
		}

		// 秘密鍵取得
		Key resultKey = null;
		String pass = settingContent.lookup(name);

		if (Objects.nonNull(pass)) {
			// ルックアップキー生成
			Name named = new CompositeName(name);
			// パスワード復号化
			pass = decryption(pass, iv);
			named.add(pass);
			// 既に鍵が登録済みの場合、鍵を取得する
			SecureObject key = securityContent.lookup(named);
			resultKey = key.resultKey();
		} else {
			// 鍵が未登録の場合、鍵を生成し登録する
			resultKey = AESKeyCreater.getKey();
			pass = securityContent.bind(name, (SecretKey) resultKey);
			// パスワード暗号化
			pass = encryption(pass, iv);
			settingContent.bind(name, pass);
		}

		// キー初期化
		AppAESPassEncrypter encrypter = new AppAESPassEncrypter(resultKey, name, iv);

		// keystore初期化
		KeyStore keyStore = KeyStore.getInstance(KEY_STORE_TYPE);

		// keystoreをコンテキストへ登録
		_CONTEXT.put(new CompositeName(name), new KagerowSecurityContentImpl(uri, keyStore, encrypter));

	}

	/**
	 * パスワードを復号化します
	 * @param pass 暗号化済みパスワード
	 * @param iv 初期化ベクトル
	 * @return パスワード
	 * @throws Exception 復号化失敗
	 */
	private static String decryption(String pass, byte[] iv) throws Exception {

		// マスターコンテンツ取得
		KagerowSecurityContent securityContent = _CONTEXT
				.get(new CompositeName(KagerowMasterSecurityContentImpl.MASTER_KEY));
		// 調整済みのパスワード
		String password = encrypter.getPassword();
		// URLセーフに修正
		password = Base64.getUrlEncoder().encodeToString(password.getBytes(StandardCharsets.UTF_8));
		// マスターキーに紐づく秘密鍵を取得
		SecureObject secureObject = securityContent
				.lookup(KagerowMasterSecurityContentImpl.MASTER_KEY.concat(StringUtils.SLASH_DELIMIT + password));
		Key key = secureObject.resultKey();

		// 暗号化インスタンスを生成
		Cipher cipher = Cipher.getInstance(AppAESPassEncrypter.ALGORITHM);
		// 初期化パラメータ生成
		IvParameterSpec ivParam = new IvParameterSpec(iv);
		// 暗号化インスタンス初期化
		cipher.init(Cipher.DECRYPT_MODE, key, ivParam);
		// Base64デコード変換
		byte[] decPass = Base64.getDecoder().decode(pass.getBytes(StandardCharsets.UTF_8));
		// エンコード済みパスワード取得
		decPass = cipher.doFinal(decPass);
		return new String(decPass);
	}

	/**
	 * パスワードを暗号化します
	 * @param pass パスワード
	 * @param iv 初期化ベクトル
	 * @return 暗号化済みパスワード
	 * @throws Exception 暗号化失敗
	 */
	private static String encryption(String pass, byte[] iv) throws Exception {

		// マスターコンテンツ取得
		KagerowSecurityContent securityContent = _CONTEXT
				.get(new CompositeName(KagerowMasterSecurityContentImpl.MASTER_KEY));
		// 調整済みのパスワード
		String password = encrypter.getPassword();
		// URLセーフに修正
		password = Base64.getUrlEncoder().encodeToString(password.getBytes(StandardCharsets.UTF_8));
		// マスターキーに紐づく秘密鍵を取得
		SecureObject secureObject = securityContent
				.lookup(KagerowMasterSecurityContentImpl.MASTER_KEY.concat(StringUtils.SLASH_DELIMIT + password));
		Key key = secureObject.resultKey();

		// 暗号化インスタンスを生成
		Cipher cipher = Cipher.getInstance(AppAESPassEncrypter.ALGORITHM);
		// 初期化パラメータ生成
		IvParameterSpec ivParam = new IvParameterSpec(iv);
		// 暗号化インスタンス初期化
		cipher.init(Cipher.ENCRYPT_MODE, key, ivParam);
		// エンコード済みパスワード取得
		byte[] encPass = cipher.doFinal(pass.getBytes(StandardCharsets.UTF_8));
		// Base64エンコード変換
		encPass = Base64.getEncoder().encode(encPass);
		return new String(encPass);
	}

	/**
	 * マスターキーを取得します
	 * @return マスターキー
	 */
	public Optional<KagerowSecurityContent> getMasterKey() {
		try {
			// マスターコンテンツ取得
			KagerowSecurityContent securityContent = _CONTEXT
					.get(new CompositeName(KagerowMasterSecurityContentImpl.MASTER_KEY));
			return Optional.of(securityContent);
		} catch (InvalidNameException e) {
			KagerowLogger.newAppLogger().err(e);
			return Optional.empty();
		}
	}

}

package com.sakulabo.core.Kagerow.Contents.Impl;

import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.UnrecoverableKeyException;
import java.util.Base64;
import java.util.Objects;

import javax.crypto.SecretKey;
import javax.naming.CannotProceedException;
import javax.naming.Name;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import com.sakulabo.core.Kagerow.Contents.KagerowSecurityContent;
import com.sakulabo.core.Kagerow.Contents.KagerowVirtualFileContent.KagerowVirtualFileObject.SecureFileObject;
import com.sakulabo.core.Kagerow.Context.Impl.KagerowSecurityContextImpl;
import com.sakulabo.core.Kagerow.Exception.ApplicationError;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;
import com.sakulabo.core.Processor.security.AESKeyCreater;
import com.sakulabo.core.Processor.security.AppAESPassEncrypter;
import com.sakulabo.core.Processor.security.AppEncrypter;
import com.sakulabo.core.Processor.security.PBEKeyEncrypter;

/**
 * Kagerowアプリケーションのセキュリティコンテンツ実装クラスです
 * 
 * @author keeeeeent
 */
public sealed class KagerowSecurityContentImpl extends BaseKagerowContent implements KagerowSecurityContent {

	/** パスワード失敗時の例外フラグ */
	public static final Object PASSWORD_MISS = new Object();
	/** 管理対象KeyStore */
	protected volatile KeyStore keyStore;
	/** エンコード済みパスワード */
	protected volatile AppEncrypter encrypter;
	/** キーストア物理パス */
	protected final URI keyURI;

	/**
	 * Kagerowアプリケーションのマスターセキュリティコンテンツ実装クラスです
	 */
	public static final class KagerowMasterSecurityContentImpl extends KagerowSecurityContentImpl {

		/** マスターkeystoreコンテキスト登録名称 */
		public static final String MASTER_KEY = "master";

		/**
		 * デフォルトコンストラクタ
		 * @param keyURI キーストア物理パス
		 * @param keyStore キーストア
		 * @param encrypter エンコード済みパスワード
		 * @throws Exception 初期化エラー
		 */
		public KagerowMasterSecurityContentImpl(URI keyURI, KeyStore keyStore, PBEKeyEncrypter encrypter)
				throws Exception {
			// スーパークラス初期化
			super(keyURI, keyStore, encrypter);
			// 鍵の存在チェック
			if (!keyStore.containsAlias(MASTER_KEY)) {

				// 固有鍵が存在しない場合、新たに登録する
				// マスターキー内部に固有鍵を生成
				SecretKey secretKey = AESKeyCreater.getKey();
				// 調整済みのパスワード
				String pass = encrypter.getPassword();
				// URLセーフに修正
				pass = Base64.getUrlEncoder().encodeToString(pass.getBytes(StandardCharsets.UTF_8));
				// キー登録
				KeyStore.Entry entry = new KeyStore.SecretKeyEntry(secretKey);
				KeyStore.ProtectionParameter param = new KeyStore.PasswordProtection(pass.toCharArray());

				keyStore.setEntry(MASTER_KEY, entry, param);

				// キーストアリロード
				reFlesh();
			}
		}

		/** {@inheritDoc} */
		@Override
		public void close() throws NamingException {
			try {
				// パスワード生成
				char[] pass = encrypter.getPassword().toCharArray();
				// 仮想ファイル登録
				Path keyPath = Paths.get(keyURI);
				try (OutputStream output = Files.newOutputStream(keyPath)) {
					keyStore.store(output, pass);
				}
			} catch (Exception e) {
				KagerowLogger.newAppLogger().err(e);
			}
		}

		/**
		 * 秘密鍵を取得します
		 * @return 秘密鍵
		 */
		public SecretKey getSecretKey() {
			return ((PBEKeyEncrypter) encrypter).getSecretKey();
		}

	}

	/**
	 * デフォルトコンストラクタ
	 * @param keyURI キーストア物理パス
	 * @param keyStore キーストア
	 * @param encrypter エンコード済みパスワード
	 */
	public KagerowSecurityContentImpl(URI keyURI, KeyStore keyStore, AppEncrypter encrypter) {
		// フィールド初期化
		this.keyStore = keyStore;
		this.encrypter = encrypter;
		this.keyURI = keyURI;
		Path keyPath = Paths.get(keyURI);
		// キーストアロード
		try {
			if (Files.exists(keyPath)) {
				this.keyStore.load(Files.newInputStream(keyPath), encrypter.getPassword().toCharArray());
			} else {
				this.keyStore.load(null, encrypter.getPassword().toCharArray());
			}
		} catch (Exception e) {
			// 例外生成
			ApplicationError error = new ApplicationError(e);
			// パスワードミスか判定
			if (e.getCause() instanceof UnrecoverableKeyException) {
				error.setFlug(PASSWORD_MISS);
			}
			throw error;
		}

	}

	/**
	 * パスワードを再設定します
	 * @param encrypter 再設定するパスワード生成インスタンス
	 */
	public synchronized void changePass(AppEncrypter encrypter) {
		// マスターキーストア再初期化
		this.encrypter = Objects.requireNonNull(encrypter);
	}

	/**
	 * キーストアのリフレッシュ処理を実行します
	 */
	protected void reFlesh() {

		try {
			// パスワード生成
			char[] pass = encrypter.getPassword().toCharArray();
			// 仮想ファイル登録
			Path keyPath = Paths.get(keyURI);
			try (OutputStream output = Files.newOutputStream(keyPath)) {
				keyStore.store(output, pass);
			}
			// ロード
			keyStore = KeyStore.getInstance(KagerowSecurityContextImpl.KEY_STORE_TYPE);
			this.keyStore.load(Files.newInputStream(keyPath), pass);

		} catch (Exception e) {
			KagerowLogger.newAppLogger().err(e);
		}
	}

	/** {@inheritDoc} */
	@Override
	public SecureObject lookup(SecureFileObject file) throws NamingException {
		// 既存キーの存在確認
		try {
			if (!keyStore.containsAlias(file.alias())) {
				throw new NameNotFoundException(file.alias());
			}
		} catch (KeyStoreException e) {
			CannotProceedException exception = new CannotProceedException(e.getMessage());
			exception.setRootCause(e);
			throw exception;
		}

		// 返却オブジェクト初期化
		SecureObject secureObject = null;

		try {

			// アプリケーション秘密鍵取得
			Key resultKey = keyStore.getKey(file.alias().toLowerCase(), file.password().toCharArray());
			// 調整済みのパスワード
			AppEncrypter encrypter = new AppAESPassEncrypter(resultKey, file.schema());
			String pass = encrypter.getPassword();

			// 返却オブジェクト設定
			secureObject = new SecureObject(pass.toCharArray(), resultKey);

		} catch (Exception e) {
			CannotProceedException exception = new CannotProceedException(e.getMessage());
			exception.setRootCause(e);
			throw exception;
		}

		return secureObject;
	}

	/** {@inheritDoc} */
	@Override
	public SecureObject lookup(Name name) throws NamingException {

		// パスワードと名称に分解
		String pass, keyName = name.get(0);

		// 既存キーの存在確認
		try {
			if (!keyStore.containsAlias(keyName)) {
				throw new NameNotFoundException(keyName);
			}
		} catch (KeyStoreException e) {
			CannotProceedException exception = new CannotProceedException(e.getMessage());
			exception.setRootCause(e);
			throw exception;
		}

		// 返却オブジェクト初期化
		SecureObject secureObject = null;

		try {

			// パスワード取得
			pass = name.get(1);
			// アプリケーション秘密鍵取得
			Key resultKey = keyStore.getKey(keyName.toString(), pass.toCharArray());
			// 調整済みのパスワード
			AppEncrypter encrypter = new AppAESPassEncrypter(resultKey, keyName);
			pass = encrypter.getPassword();

			// 返却オブジェクト設定
			secureObject = new SecureObject(pass.toCharArray(), resultKey);

			// パスワードをメモリから削除
			pass = null;
			System.gc();

		} catch (Exception e) {
			CannotProceedException exception = new CannotProceedException(e.getMessage());
			exception.setRootCause(e);
			throw exception;
		}

		return secureObject;
	}

	/** {@inheritDoc} */
	@Override
	public String bind(String name, SecretKey secretKey) throws NamingException {

		// 既存キーの存在確認
		try {
			if (keyStore.containsAlias(name)) {
				throw new NameAlreadyBoundException(name);
			}
		} catch (KeyStoreException e) {
			CannotProceedException exception = new CannotProceedException(e.getMessage());
			exception.setRootCause(e);
			throw exception;
		}

		try {

			// 調整済みのパスワード
			AppEncrypter encrypter = new AppAESPassEncrypter(secretKey, name);
			String pass = encrypter.getPassword();

			// キー登録
			KeyStore.Entry entry = new KeyStore.SecretKeyEntry(secretKey);
			KeyStore.ProtectionParameter param = new KeyStore.PasswordProtection(pass.toCharArray());
			keyStore.setEntry(name, entry, param);

			// キーストアリロード
			reFlesh();

			// リスナー起動
			callListener(KagerowContentEventKind.CREATE, null);

			return pass;

		} catch (Exception e) {
			CannotProceedException exception = new CannotProceedException(e.getMessage());
			exception.setRootCause(e);
			throw exception;
		}

	}

	/** {@inheritDoc} */
	@Override
	public void unbind(Name name) throws NamingException {

		// 既存キーの存在確認
		try {
			if (keyStore.containsAlias(name.toString())) {
				throw new NameNotFoundException(name.toString());
			}
		} catch (KeyStoreException e) {
			CannotProceedException exception = new CannotProceedException(e.getMessage());
			exception.setRootCause(e);
			throw exception;
		}

		try {

			// キー削除
			keyStore.deleteEntry(name.toString());

		} catch (Exception e) {
			CannotProceedException exception = new CannotProceedException(e.getMessage());
			exception.setRootCause(e);
			throw exception;
		}

		// リスナー起動
		callListener(KagerowContentEventKind.DELETE, null);

	}

	/** {@inheritDoc} */
	@Override
	public void rename(Name oldName, Name newName) throws NamingException {

		// パスワードと名称に分解
		String pass, keyName = oldName.get(0), newPass = newName.get(1), newKeyName = newName.get(0);

		// 既存キーの存在確認
		try {
			if (!keyStore.containsAlias(keyName)) {
				throw new NameNotFoundException(keyName);
			}
		} catch (KeyStoreException e) {
			CannotProceedException exception = new CannotProceedException(e.getMessage());
			exception.setRootCause(e);
			throw exception;
		}

		try {

			// パスワード取得
			pass = oldName.get(1);
			// アプリケーション秘密鍵取得
			Key secretKey = keyStore.getKey(keyName.toString(), pass.toCharArray());

			// キー登録
			KeyStore.Entry entry = new KeyStore.SecretKeyEntry((SecretKey) secretKey);
			KeyStore.ProtectionParameter param = new KeyStore.PasswordProtection(newPass.toCharArray());
			keyStore.setEntry(newKeyName, entry, param);

			// パスワードをメモリから削除
			pass = null;
			newPass = null;
			System.gc();

		} catch (Exception e) {
			CannotProceedException exception = new CannotProceedException(e.getMessage());
			exception.setRootCause(e);
			throw exception;
		}

		// リスナー起動
		callListener(KagerowContentEventKind.UPDATE, null);

	}

	/** {@inheritDoc} */
	@Override
	public String getNameInNamespace() throws NamingException {
		return encrypter.getPassword();
	}

	/** {@inheritDoc} */
	@Override
	public void addListener(KagerowContentEventKind kind, KagerowContentEventListener listener) {
		switch (kind) {
		case DELETE:
		case CREATE:
			super.addListener(kind, listener);
			break;
		default:
			throw new IllegalArgumentException();
		}
	}

}

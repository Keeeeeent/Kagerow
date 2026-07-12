package com.sakulabo.application.service.Rpc.Impl;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.util.Base64;
import java.util.Objects;
import java.util.concurrent.locks.Lock;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.naming.CompositeName;
import javax.naming.NamingException;

import com.sakulabo.application.service.Rpc.AuthService;
import com.sakulabo.core.Kagerow.Contents.KagerowSecurityContent;
import com.sakulabo.core.Kagerow.Contents.KagerowSecurityContent.SecureObject;
import com.sakulabo.core.Kagerow.Context.KagerowSecurityContext;
import com.sakulabo.core.Kagerow.Utilities.KagerowUtilities;
import com.sakulabo.regulation.annotation.KagerowComponent;

/**
 * 認証サービスの実装クラスです
 *
 * @author keeeeeent
 */
@KagerowComponent
public class AuthServiceImpl implements AuthService {

	/** キーストアパス */
	private static final Path STORE_PATH = KagerowUtilities.createKagerowHomePath().resolve("setting", "auth.p12");
	/** キーストア */
	private final KeyStore store;
	/** SHA256メッセージダイジェスト */
	private final MessageDigest digest;

	/**
	 * デフォルトコンストラクタ
	 *
	 * @throws NoSuchAlgorithmException ハッシュ関数生成失敗
	 * @throws KeyStoreException キーストア生成失敗
	 * @throws IOException キーストアロード失敗
	 * @throws CertificateException パスワード不正
	 * @throws NoSuchPaddingException 暗号化インスタンス生成失敗
	 * @throws InvalidKeyException 暗号化インスタンス初期化失敗
	 * @throws InvalidAlgorithmParameterException 初期化ベクトル不正
	 * @throws NamingException セキュアブートを行っていない場合
	 * @throws BadPaddingException パスワード生成失敗
	 * @throws IllegalBlockSizeException パスワード生成失敗
	 */
	public AuthServiceImpl() throws NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException,
			NamingException, InvalidKeyException, NoSuchPaddingException, InvalidAlgorithmParameterException,
			IllegalBlockSizeException, BadPaddingException {
		// 排他制御開始（書き込み）
		Lock lock = AuthService.LOCK.writeLock();
		lock.lock();
		try {
			// ハッシュ関数生成
			digest = MessageDigest.getInstance("sha256");
			// キーストアの存在確認実施
			if (Files.exists(STORE_PATH)) {
				// パスワード生成
				String iv = KagerowUtilities.getSetting(AuthServiceImpl.class.getName(), "iv");
				byte[] ivData = Base64.getUrlDecoder().decode(iv);
				// 秘密鍵取得
				SecretKey secretKey = createKey();
				// 暗号化パスワード生成
				String savePassStr = createPass((SecretKey) secretKey, ivData);
				// キーストアロード
				store = KeyStore.getInstance(STORE_PATH.toFile(), savePassStr.toCharArray());
			} else {
				// キーストアロード
				store = KeyStore.getInstance("PKCS12");
				store.load(null, null);
			}
			try {
				this.store();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} finally {
			// 排他制御解除
			lock.unlock();
		}
	}

	/**
	 * キーストアを保存します
	 *
	 * @throws NoSuchAlgorithmException 秘密鍵生成失敗
	 * @throws NoSuchPaddingException 暗号化インスタンス生成失敗
	 * @throws InvalidKeyException 暗号化インスタンス初期化失敗
	 * @throws InvalidAlgorithmParameterException 初期化ベクトル不正
	 * @throws NamingException セキュアブートを行っていない場合
	 * @throws IOException 保存失敗
	 * @throws BadPaddingException パスワード生成失敗
	 * @throws IllegalBlockSizeException パスワード生成失敗
	 * @throws CertificateException 保存失敗
	 * @throws KeyStoreException 保存失敗
	 */
	private void store() throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
			InvalidAlgorithmParameterException, NamingException, IOException, IllegalBlockSizeException,
			BadPaddingException, KeyStoreException, CertificateException {
		String iv = KagerowUtilities.getSetting(AuthServiceImpl.class.getName(), "iv");
		if (Objects.nonNull(iv)) {
			// 2回目以降の場合
			byte[] ivData = Base64.getUrlDecoder().decode(iv);
			// 秘密鍵取得
			SecretKey secretKey = createKey();
			// 暗号化パスワード生成
			String savePassStr = createPass((SecretKey) secretKey, ivData);
			// キーストア保存
			try (OutputStream output = Files.newOutputStream(STORE_PATH)) {
				store.store(output, savePassStr.toCharArray());
			}
		} else {
			// 初回保存の場合
			SecureRandom rand = new SecureRandom();
			byte[] ivData = new byte[16];
			rand.nextBytes(ivData);
			// 秘密鍵生成
			KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
			keyGenerator.init(256);
			SecretKey secretKey = keyGenerator.generateKey();
			// 暗号化パスワード生成
			String savePassStr = createPass(secretKey, ivData);
			// 秘密鍵保存
			KagerowSecurityContext ctx = (KagerowSecurityContext) KagerowUtilities
					.getContext(KagerowSecurityContext._NAME);
			// ネームスペース生成
			KagerowSecurityContent cnt = ctx.createSubcontext(AuthServiceImpl.class.getSimpleName());
			// 秘密鍵保存
			String pass = cnt.bind("sec", secretKey);
			// パスワード保存（このパスワードはセキュアブート管理下の内部使用向けのため保存しても問題がない）
			KagerowUtilities.setSetting(AuthServiceImpl.class.getName(), "pass", pass);
			// 初期化ベクトル保存
			KagerowUtilities.setSetting(AuthServiceImpl.class.getName(), "iv",
					Base64.getUrlEncoder().encodeToString(ivData));
			// キーストア保存
			try (OutputStream output = Files.newOutputStream(STORE_PATH)) {
				store.store(output, savePassStr.toString().toCharArray());
			}
		}
	}

	private SecretKey createKey() throws NamingException {
		// 秘密鍵取得
		KagerowSecurityContext ctx = (KagerowSecurityContext) KagerowUtilities
				.getContext(KagerowSecurityContext._NAME);
		KagerowSecurityContent cnt = ctx.lookup(AuthServiceImpl.class.getSimpleName());
		// パスワード取得
		String pass = KagerowUtilities.getSetting(AuthServiceImpl.class.getName(), "pass");
		CompositeName name = new CompositeName("sec/" + pass);
		SecureObject secretKey = cnt.lookup(name);
		return (SecretKey) secretKey.resultKey();
	}

	/**
	 * 暗号化パスワード生成
	 *
	 * @param secretKey 秘密鍵
	 * @param ivData 初期化ベクトル
	 * @return 暗号化パスワード
	 * @throws NoSuchAlgorithmException 秘密鍵生成失敗
	 * @throws NoSuchPaddingException 暗号化インスタンス生成失敗
	 * @throws InvalidKeyException 暗号化インスタンス初期化失敗
	 * @throws InvalidAlgorithmParameterException 初期化ベクトル不正
	 * @throws BadPaddingException パスワード生成失敗
	 * @throws IllegalBlockSizeException パスワード生成失敗
	 */
	private String createPass(SecretKey secretKey, byte[] ivData)
			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
			InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
		// 暗号化パスワード生成
		Cipher encryptor = Cipher.getInstance("AES/CBC/PKCS5Padding");
		IvParameterSpec ivSpec = new IvParameterSpec(ivData);
		encryptor.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
		byte[] savePass = encryptor.doFinal(AuthServiceImpl.class.getName().getBytes(StandardCharsets.UTF_8));
		savePass = digest.digest(savePass);
		StringBuilder savePassStr = new StringBuilder();
		for (int i = 0; i < savePass.length; i++) {
			savePassStr.append(Integer.toHexString(savePass[i]));
		}
		return savePassStr.toString();
	}

	/** {@inheritDoc} */
	@Override
	public boolean verified(String authorization) {
		// 排他制御開始（読み込み）
		Lock lock = AuthService.LOCK.readLock();
		lock.lock();
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public Challenge nonce(String userName) {
		// 排他制御開始（読み込み）
		Lock lock = AuthService.LOCK.readLock();
		lock.lock();
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public String challenge(String challenge) {
		// 排他制御開始（読み込み）
		Lock lock = AuthService.LOCK.readLock();
		lock.lock();
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public String regist(String userName) {
		// 排他制御開始（書き込み）
		Lock lock = AuthService.LOCK.writeLock();
		lock.lock();
		return null;
	}

}

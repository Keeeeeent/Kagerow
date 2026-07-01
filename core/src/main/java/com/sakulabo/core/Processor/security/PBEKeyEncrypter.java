package com.sakulabo.core.Processor.security;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Objects;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import com.sakulabo.core.Kagerow.Exception.AppLogicException;
import com.sakulabo.core.Kagerow.Exception.ApplicationError;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;

/**
 * ユーザ入力から生成されたAES秘密鍵による暗号化機能提供をするためのクラスです
 * 
 * @author keeeeeent
 */
public class PBEKeyEncrypter implements AppEncrypter {

	/** アルゴリズム */
	public static String ALGORITHM = "AES/CBC/PKCS5Padding";

	/** エンコード済みパスワード */
	private final String encPassword;
	/** エンコード用ソルト */
	private byte[] salt;
	/** エンコード用初期化ベクトル */
	private byte[] iv;
	/** 擬似真正乱数発生インスタンス */
	private static final SecureRandom RAND = new SecureRandom();
	/** 秘密鍵 */
	private final SecretKey secretKey;
	/** ソルトサイズ */
	public static final int SALT_SIZE = 1024;

	/**
	 * デフォルトコンストラクタ
	 * @param password 基準となるパスワード
	 * @param salt ソルト
	 * @param iv 初期化ベクトル
	 * @throws AppLogicException パスワード生成失敗
	 */
	public PBEKeyEncrypter(String password, byte[] salt, byte[] iv) throws AppLogicException {

		SecretKeyFactory factory;
		try {
			// PBKDF2の実装を取得
			factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
		} catch (NoSuchAlgorithmException e) {
			// 基本的に固定アルゴリズムのため例外は発生しない（アルゴリズム事態もJDK標準のものを使用）
			KagerowLogger.newAppLogger().err(e);
			// 万が一発生した場合、エラーとしてアプリケーションを即座に終了
			throw new ApplicationError(e);
		}

		// ソルト（1024Bランダム）
		if (Objects.isNull(salt) || salt.length < SALT_SIZE) {
			salt = new byte[SALT_SIZE];
			RAND.nextBytes(salt);
		}
		// ソルトをフィールドにセット
		this.salt = salt;

		// 鍵長（bit単位）反復回数を初期化
		final int iterationCount = 65536, keyLength = 128;

		// パスワード, ソルト, 反復回数, 鍵長 を指定
		KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterationCount, keyLength);

		// PBKDF2で中間鍵を生成
		SecretKey tmp;
		try {
			tmp = factory.generateSecret(spec);
		} catch (InvalidKeySpecException e) {
			throw new AppLogicException("PBEKeyEncrypter can not initialize", e);
		}
		// AES用SecretKeyに変換
		secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");

		// パスワードを暗号化
		AppAESPassEncrypter passEncrypter = new AppAESPassEncrypter(secretKey, password, iv);
		this.iv = passEncrypter.getIv();

		// パスワードを調整しフィールドを初期化
		AppEncrypter hashEncrypter = new AppHashEncrypter(passEncrypter.getPassword());
		encPassword = hashEncrypter.getPassword();

	}

	/** {@inheritDoc} */
	@Override
	public String getPassword() {
		return encPassword;
	}

	/**
	 * パスワード初期化に使用したソルトを返却します
	 * @return ソルト
	 */
	public byte[] getSalt() {
		return salt;
	}

	/**
	 * パスワード初期化に使用した初期化ベクトルを返却します
	 * @return ソルト
	 */
	public byte[] getIv() {
		return iv;
	}

	/**
	 * 秘密鍵を取得します
	 * @return 秘密鍵
	 */
	public SecretKey getSecretKey() {
		return secretKey;
	}

}

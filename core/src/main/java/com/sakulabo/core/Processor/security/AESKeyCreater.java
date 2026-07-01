package com.sakulabo.core.Processor.security;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import com.sakulabo.core.Kagerow.Exception.ApplicationError;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;

/**
 * AES秘密鍵による暗号化機能提供をするためのユーティリティクラスです
 * 
 * @author keeeeeent
 */
public final class AESKeyCreater {

	/** GCMアルゴリズム */
	public static final String GCM = "AES/GCM/NoPadding";

	/**
	 * デフォルトコンストラクタ（インスタンス生成禁止）
	 */
	private AESKeyCreater() {
		;
	}

	/**
	 * 128bit長の秘密鍵を生成します
	 * @return 秘密鍵
	 */
	public static SecretKey getKey() {
		try {
			KeyGenerator keyGen = KeyGenerator.getInstance("AES");
			keyGen.init(128);
			SecretKey secretKey = keyGen.generateKey();
			return secretKey;
		} catch (NoSuchAlgorithmException e) {
			// 基本的に固定アルゴリズムのため例外は発生しない（アルゴリズム事態もJDK標準のものを使用）
			KagerowLogger.newAppLogger().err(e);
			// 万が一発生した場合、エラーとしてアプリケーションを即座に終了
			throw new ApplicationError(e);
		}
	}

	/**
	 * 初期化ベクトルを生成します
	 * @param rand 乱数発生インスタンス
	 * @return 初期がベクトル
	 */
	public static byte[] getIV(SecureRandom rand) {
		byte[] iv = getIV();
		rand.nextBytes(iv);
		return iv;
	}

	/**
	 * 空の初期化ベクトルを生成します
	 * @return 空の初期化ベクトル
	 */
	public static byte[] getIV() {
		byte[] iv = new byte[16];
		return iv;
	}

	/**
	 * 空のソルトを生成します
	 * @return 空のソルト
	 */
	public static byte[] getSalt() {
		byte[] salt = new byte[PBEKeyEncrypter.SALT_SIZE];
		return salt;
	}
}

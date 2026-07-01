package com.sakulabo.core.Processor.security;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;

import com.sakulabo.core.Kagerow.Exception.AppLogicException;

/**
 * 秘密鍵を使用し調整されたパスワードを生成機能をするクラスです
 * 
 * @author keeeeeent
 */
public class AppAESPassEncrypter implements AppEncrypter {

	/** AES標準アルゴリズム */
	public static String ALGORITHM = "AES/CBC/PKCS5Padding";
	/** 擬似真正乱数発生インスタンス */
	public static final SecureRandom RAND = new SecureRandom();
	/** 調整されたパスワード */
	private String pass;
	/** エンコード用初期化ベクトル */
	private byte[] iv;

	/**
	 * デフォルトコンストラクタ
	 * @param key 秘密鍵
	 * @param name キー名称
	 * @throws Exception パスワード生成失敗
	 */
	public AppAESPassEncrypter(Key key, String name) throws Exception {
		this(key, name, AESKeyCreater.getIV());
		RAND.nextBytes(iv);
	}

	/**
	 * デフォルトコンストラクタ
	 * @param key 秘密鍵
	 * @param name キー名称
	 * @param iv 初期化ベクトル
	 * @throws AppLogicException パスワード生成失敗
	 */
	public AppAESPassEncrypter(Key key, String name, byte[] iv) throws AppLogicException {

		try {
			// 暗号化インスタンスを生成
			Cipher cipher = Cipher.getInstance(ALGORITHM);
			// 初期化パラメータ生成
			IvParameterSpec ivParam = new IvParameterSpec(iv);
			// 暗号化インスタンス初期化
			cipher.init(Cipher.ENCRYPT_MODE, key, ivParam);
			// エンコード済みパスワード取得
			byte[] encPass = cipher.doFinal(name.getBytes(StandardCharsets.UTF_8));
			// 初期化ベクトル初期化
			this.iv = cipher.getIV();
			// キーの長さを調整
			AppHashEncrypter encrypter = new AppHashEncrypter(Base64.getEncoder().encodeToString(encPass));
			// 調整済みのパスワード
			pass = encrypter.getPassword();
		} catch (NoSuchAlgorithmException
				| NoSuchPaddingException
				| InvalidKeyException
				| InvalidAlgorithmParameterException
				| IllegalBlockSizeException
				| BadPaddingException e) {
			throw new AppLogicException("AppAESPassEncrypter can not initialize", e);
		}
	}

	/** {@inheritDoc} */
	@Override
	public String getPassword() {
		return pass;
	}

	/**
	 * パスワード初期化に使用した初期化ベクトルを返却します
	 * @return ソルト
	 */
	public byte[] getIv() {
		return iv;
	}

}

package com.sakulabo.core.Processor.security;

import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.KeyStore.ProtectionParameter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import com.sakulabo.core.Common.StringUtils;
import com.sakulabo.core.Kagerow.Exception.ApplicationError;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;

/**
 * ハッシュ化機能提供をするクラスです
 * 
 * @author keeeeeent
 */
public class AppHashEncrypter implements AppEncrypter, KeyStore.LoadStoreParameter {

	/** エンコード済みパスワード */
	private final String encPassword;

	/**
	 * デフォルトコンストラクタ
	 * @param password 基準となるパスワード
	 */
	public AppHashEncrypter(String password) {

		try {
			// ハッシュ関数初期化
			MessageDigest digest = MessageDigest.getInstance(StringUtils.SHA_256);
			// ハッシュ生成
			byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
			// Base64に変換し保持
			encPassword = Base64.getEncoder().encodeToString(hash);
		} catch (NoSuchAlgorithmException e) {
			// 基本的に固定アルゴリズムのため例外は発生しない（アルゴリズム事態もJDK標準のものを使用）
			KagerowLogger.newAppLogger().err(e);
			// 万が一発生した場合、エラーとしてアプリケーションを即座に終了
			throw new ApplicationError(e);
		}

	}

	/** {@inheritDoc} */
	@Override
	public String getPassword() {
		return encPassword;
	}

	/** {@inheritDoc} */
	@Override
	public ProtectionParameter getProtectionParameter() {
		return new KeyStore.PasswordProtection(encPassword.toCharArray());
	}

}

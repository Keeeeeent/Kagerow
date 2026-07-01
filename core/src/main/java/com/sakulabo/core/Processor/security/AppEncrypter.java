package com.sakulabo.core.Processor.security;

/**
 * パスワード暗号化機能提供をするための規定インターフェイス
 * 
 * @author keeeeeent
 */
public interface AppEncrypter {

	/**
	 * パスワードを取得します
	 * @return パスワード
	 */
	String getPassword();

}

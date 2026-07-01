package com.sakulabo.core.Kagerow.Contents;

import java.security.Key;

import javax.crypto.SecretKey;
import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.OperationNotSupportedException;

import com.sakulabo.core.Kagerow.Contents.KagerowVirtualFileContent.KagerowVirtualFileObject.SecureFileObject;
import com.sakulabo.core.Kagerow.Contents.Impl.KagerowSecurityContentImpl;

/**
 * Kagerowアプリケーションのコンテンツ拡張インターフェースです<br/>
 * このインターフェースの実装クラスでは、Kagerowのセキュリティ機能を提供します<br/>
 * このクラスは通常のコンテキストが定義するメソッドをサポートしておりません
 * 
 * @author keeeeeent
 */
public sealed interface KagerowSecurityContent extends KagerowContents
		permits KagerowSecurityContentImpl {

	/**
	 * キー情報データキャリア
	 * @param pass パスワード
	 * @param resultKey 秘密鍵
	 */
	public record SecureObject(
			char[] pass,
			Key resultKey) {
	};

	/**
	 * セキュアファイルオブジェクトに関連付けされたSecureObjectを取得します
	 * @param file セキュアファイルオブジェクト
	 * @return セキュアファイルオブジェクトに関連付けされたSecureObject
	 * @throws NamingException
	 */
	public SecureObject lookup(SecureFileObject file) throws NamingException;

	/**
	 * 秘密鍵をバインドします
	 * @param name エイリアス
	 * @param secretKey 秘密鍵
	 * @return 関連づけられたパスワード
	 * @throws NamingException
	 */
	public String bind(String name, SecretKey secretKey) throws NamingException;

	@Override
	public SecureObject lookup(Name name) throws NamingException;

	/** {@inheritDoc} */
	@Override
	default SecureObject lookup(String name) throws NamingException {
		return (SecureObject) KagerowContents.super.lookup(name);
	};

	@Override
	@Deprecated
	default void bind(Name name, Object obj) throws NamingException {
		throw new OperationNotSupportedException();
	}

	/** {@inheritDoc} */
	@Override
	@Deprecated
	default void unbind(Name name) throws NamingException {
		throw new OperationNotSupportedException();
	}

	/** {@inheritDoc} */
	@Override
	@Deprecated
	default void rebind(Name name, Object obj) throws NamingException {
		throw new OperationNotSupportedException();
	}

	/** {@inheritDoc} */
	@Override
	public void rename(Name oldName, Name newName) throws NamingException;

	/** {@inheritDoc} */
	@Override
	@Deprecated
	default NamingEnumeration<NameClassPair> list(Name name) throws NamingException {
		throw new OperationNotSupportedException();
	}

	/** {@inheritDoc} */
	@Override
	@Deprecated
	default NamingEnumeration<Binding> listBindings(Name name) throws NamingException {
		throw new OperationNotSupportedException();
	}

	/** {@inheritDoc} */
	@Override
	@Deprecated
	default void destroySubcontext(Name name) throws NamingException {
		throw new OperationNotSupportedException();
	}

	/** {@inheritDoc} */
	@Override
	@Deprecated
	default Context createSubcontext(Name name) throws NamingException {
		throw new OperationNotSupportedException();
	}

	/** {@inheritDoc} */
	@Override
	@Deprecated
	public default void close() throws NamingException {
		;
	}

}

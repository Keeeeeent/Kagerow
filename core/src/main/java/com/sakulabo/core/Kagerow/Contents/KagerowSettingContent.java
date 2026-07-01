package com.sakulabo.core.Kagerow.Contents;

import java.util.List;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.OperationNotSupportedException;

import com.sakulabo.core.Kagerow.Contents.Impl.KagerowSettingContentImpl;

/**
 * Kagerowアプリケーションのコンテンツ拡張インターフェースです<br/>
 * このインターフェースの実装クラスでは、Kagerowのセッテイング機能を提供します<br/>
 * このクラスは通常のコンテキストが定義するメソッドをサポートしておりません
 * 
 * @author keeeeeent
 */
public sealed interface KagerowSettingContent extends KagerowContents
		permits KagerowSettingContentImpl {

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
	public String lookup(Name name) throws NamingException;

	/** {@inheritDoc} */
	@Override
	public String lookup(String name) throws NamingException;

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

	/**
	 * 名前空間に関連付けされた設定一覧を取得します
	 * @return 設定一覧
	 */
	public List<String> settingList();

	/**
	 * 名前空間に関連付けされた設定エントリ一覧を取得します
	 * @return 設定エントリ一覧
	 */
	public List<String> settingKeySet();

}

package com.sakulabo.core.Kagerow.Context;

import java.util.List;

import javax.naming.Name;
import javax.naming.NamingException;

import com.sakulabo.core.Kagerow.Contents.KagerowCacheContent;
import com.sakulabo.core.Kagerow.Context.Impl.KagerowCacheContextImpl;

/**
 * Kagerowアプリケーションのキャッシュコンテキスト規定インターフェースです
 * 
 * @author keeeeeent
 */
public sealed interface KagerowCacheContext
		extends KagerowContexts<KagerowCacheContent>
		permits KagerowCacheContextImpl {

	/** コンテキスト名称文字列 */
	public static final String _NAME = "KagerowCache";

	/** {@inheritDoc} */
	@Override
	KagerowCacheContent lookup(String name) throws NamingException;

	/** {@inheritDoc} */
	@Override
	KagerowCacheContent lookup(Name name) throws NamingException;

	/** {@inheritDoc} */
	@Override
	public KagerowCacheContent createSubcontext(Name name) throws NamingException;

	/** {@inheritDoc} */
	@Override
	public KagerowCacheContent createSubcontext(String name) throws NamingException;

	/**
	 * キャッシュコンテキスト内部のキャッシュデータ一覧を返却します
	 * @return キャッシュデータ一覧
	 * @throws NamingException 一覧データ取得失敗
	 */
	public List<String> nameList() throws NamingException;

}

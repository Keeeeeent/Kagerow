package com.sakulabo.core.Common;

import java.util.Collection;
import java.util.Iterator;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

/**
 * Kagerowアプリケーションのコンテキスト一覧リストクラスです
 * 
 * @author keeeeeent
 * @param <T> 取得結果の型情報
 */
public final class NamingEnumerationImpl<T> implements NamingEnumeration<T> {

	/** イテレーション対象 */
	private Iterator<T> items;

	/**
	 * デフォルトコンストラクタ
	 * @param items
	 */
	public NamingEnumerationImpl(Collection<T> items) {
		this.items = items.iterator();
	}

	/** {@inheritDoc} */
	@Override
	public boolean hasMoreElements() {
		return items.hasNext();
	}

	/** {@inheritDoc} */
	@Override
	public T nextElement() {
		return items.next();
	}

	/** {@inheritDoc} */
	@Override
	public T next() throws NamingException {
		return nextElement();
	}

	/** {@inheritDoc} */
	@Override
	public boolean hasMore() throws NamingException {
		return hasMoreElements();
	}

	/** {@inheritDoc} */
	@Override
	public void close() throws NamingException {
		;
	}

}

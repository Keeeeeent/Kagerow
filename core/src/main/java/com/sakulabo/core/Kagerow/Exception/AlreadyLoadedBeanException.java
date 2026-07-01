package com.sakulabo.core.Kagerow.Exception;

import java.util.Objects;

import javax.naming.Binding;

/**
 * 既に対象のBeanがロード済みであることを表す非検査例外クラスです
 * 
 * @author keeeeeent
 */
public class AlreadyLoadedBeanException extends RuntimeException {

	/** Bean検索キーインスタンス */
	private final Binding key;

	/**
	 * デフォルトのコンストラクタです
	 * @param key Bean検索キーインスタンス
	 */
	public AlreadyLoadedBeanException(Binding key) {
		this(key, null);
	}

	/**
	 * 例外を内包するためのコンストラクタです
	 * @param key Bean検索ようキーインスタンス
	 * @param cause 原因となった例外
	 */
	public AlreadyLoadedBeanException(Binding key, Exception cause) {
		super(cause);
		this.key = key;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return Objects.toString(key);
	}

}

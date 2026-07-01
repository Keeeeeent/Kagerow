package com.sakulabo.core.Kagerow.Exception;

import java.util.Objects;

import javax.naming.Binding;

/**
 * Beanが検索不可能でであることを表す検査例外クラスです
 * 
 * @author keeeeeent
 */
public class BeanNotFoundException extends Exception {

	/**
	 * デフォルトのコンストラクタ
	 * @param key 検索に使用したキー
	 */
	public BeanNotFoundException(Binding key) {
		super(Objects.toString(key));
	}
}

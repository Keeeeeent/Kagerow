package com.sakulabo.core.Kagerow.Exception;

/**
 * 既に対象のObserverが登録済みであることを表す検査例外クラスです
 * 
 * @author keeeeeent
 */
public class AlreadyRegistedException extends Exception {

	/**
	 * デフォルトコンストラクタ
	 * @param maker オブザーバー登録キー
	 */
	public AlreadyRegistedException(String maker) {
		super("AlreadyRegisted : " + maker);
	}

}

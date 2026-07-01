package com.sakulabo.library.common;

import java.io.Serial;

import com.sakulabo.regulation.spi.PluginAdapter.PluginValidationException;

/**
 * プラグイン共通のバリデーションエラーを表す例外クラスです
 * 
 * @author keeeeeent
 */
public class DefaultPluginValidationException extends PluginValidationException {

	/** シリアルID */
	@Serial
	private static final long serialVersionUID = 8326476984173390496L;

	/**
	 * デフォルトコンストラクタ
	 * @param message エラー内容
	 * @param target バリデーション対象
	 */
	public DefaultPluginValidationException(DefaultPluginMessage message, String target) {
		super(message.toString(), target);
	}

	/**
	 * デフォルトコンストラクタ
	 * @param message エラー内容
	 * @param params 埋込文字列リスト
	 * @param cause 原因例外   
	 */
	public DefaultPluginValidationException(DefaultPluginMessage message, Object[] params, Throwable cause) {
		super(message.toString(params), "unknown", cause);
	}

	/**
	 * デフォルトコンストラクタ
	 * @param message エラー内容
	 * @param params 埋込文字列リスト
	 * @param target バリデーション対象   
	 */
	public DefaultPluginValidationException(DefaultPluginMessage message, Object[] params, String target) {
		super(message.toString(params), target);
	}

}

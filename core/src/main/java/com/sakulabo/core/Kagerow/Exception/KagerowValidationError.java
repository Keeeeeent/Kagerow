package com.sakulabo.core.Kagerow.Exception;

import java.util.Objects;

/**
 * KagerowScriptが実行不可能であることを表す検査例外クラスです
 * 
 * @author keeeeeent
 */
public class KagerowValidationError extends IllegalStateException {

	/** 名称 */
	private String name;
	/** ID */
	private String id;

	/**
	 * デフォルトのコンストラクタ
	 * @param cause 原因となった例外 
	 */
	public KagerowValidationError(Exception cause) {
		super(cause);
	}

	/**
	 * デフォルトのコンストラクタ
	 * @param message エラーメッセージ
	 * @param cause 原因となった例外 
	 */
	public KagerowValidationError(String message, Exception cause) {
		super(message, cause);
	}

	/**
	 * IDを設定します
	 * @param id プラグインID
	 */
	public void setId(String id) {
		this.id = Objects.requireNonNull(id);
	}

	/**
	 * IDを取得します
	 * @return プラグインID
	 */
	public String getId() {
		return id;
	}

	/**
	 * 名称を設定します
	 * @param name プラグイン名称
	 */
	public void setName(String name) {
		this.name = Objects.requireNonNull(name);
	}

	/**
	 * 名称を取得します
	 * @return プラグイン名称
	 */
	public String getName() {
		return name;
	}

}

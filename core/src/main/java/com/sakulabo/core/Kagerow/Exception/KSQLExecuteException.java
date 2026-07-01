package com.sakulabo.core.Kagerow.Exception;

import java.sql.SQLException;
import java.util.Objects;

/**
 * KSQLが実行に失敗したことを表す検査例外クラスです
 * 
 * @author keeeeeent
 */
public class KSQLExecuteException extends SQLException {

	/** 名称 */
	private String name;
	/** ID */
	private String id;

	/**
	 * デフォルトコンストラクタ
	 * @param msg エラーメッセージ
	 */
	public KSQLExecuteException(String msg) {
		this(msg, null);
	}

	/**
	 * デフォルトコンストラクタ
	 * @param msg エラーメッセージ
	 * @param error 原因となった例外
	 */
	public KSQLExecuteException(String msg, Throwable error) {
		super(msg, error);
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

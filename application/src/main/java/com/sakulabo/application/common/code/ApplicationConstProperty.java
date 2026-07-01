package com.sakulabo.application.common.code;

/**
 * アプリケーション共通で使用する定数値クラスです
 * 
 * @author keeeeeent
 */
public final class ApplicationConstProperty {

	/** インスタンス生成禁止 */
	private ApplicationConstProperty() {
	}

	/** デフォルトニーモック */
	public static final int DEFAULT_MNEMONIC = -1;

	/**
	 * DIコンテキスト初期化アダプターロードのための、システムプロパティーキー<br/>
	 * 任意で指定されるマーカプロパティーのため、設定値に意味はない
	 */
	public static final String CUSTOM_APP_SYSTEM_PROP_KEY = "customApp";

	/**
	 * DIコンテキスト初期化モードを指定するための、システムプロパティーキー<br/>
	 * 必須で指定されるシステムプロパティー
	 */
	public static final String INIT_MODE_SYSTEM_PROP_KEY = "app.init.mode";

	/**
	 * CSVプラグイン名称Key（システム管理）</br>
	 * ※変更した場合kagerow-setting.xmlと合わせること
	 */
	public static final String CSV_PLUGIN_NAMED_KEY = "CsvOutput";

	/** 
	 * TSVプラグイン名称Key（システム管理）
	 * ※変更した場合kagerow-setting.xmlと合わせること
	 */
	public static final String TSV_PLUGIN_NAMED_KEY = "TsvOutput";

	/** CSVを表す文字列です */
	public static final String CSV_STR = "CSV";

	/** TSVを表す文字列です */
	public static final String TSV_STR = "TSV";

	/** モジュール名称 */
	public static final String MODULE_NAME = "com.sakulabo.application";

}

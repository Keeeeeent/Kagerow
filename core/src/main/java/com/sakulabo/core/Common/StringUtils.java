package com.sakulabo.core.Common;

/**
 * Kagerowアプリケーション専用文字列操作ユーティリティクラスです
 * 
 * @author keeeeeent
 */
public final class StringUtils {

	/**
	 * デフォルトコンストラクタ<br/>
	 * ※インスタンス生成禁止
	 */
	private StringUtils() {
		;
	}

	/** ダブルクオーテーション */
	public static final char DOUBLE_QUOTATION = '"';
	/** シングルオーテーション */
	public static final char SINGLE_QUOTATION = '\'';
	/** カンマ */
	public static final char COMMA = ',';
	/** ラインフィールド */
	public static final char CR = '\r';
	/** キャレッジリターン */
	public static final char LF = '\n';
	/** タブ */
	public static final char TAB = '\t';
	/** NULL文字 */
	public static final char NULL = (char) 0;

	/** 空文字 */
	public static final String EMPTY = "";
	/** 半角空白文字 */
	public static final String BLANK = " ";
	/** 全角空白文字 */
	public static final String FULL_BLANK = "　";
	/** NULL文字列 */
	public static final String NULL_STR = "null";
	/** 不明文字 */
	public static final String UNKNOWN = "unknown";
	/** デフォルト文字列 */
	public static final String DEFAULT = "default";
	/** カンマ文字列 */
	public static final String COMMA_STR = ",";
	/** アンダーバー */
	public static final String UNDERSCORE = "_";
	/** ドット（正規表現） */
	public static final String DOT = "\\.";
	/** ドット（文字列表現） */
	public static final String DOT_STR = ".";
	/** 等価記号 */
	public static final String EQUAL = "=";

	/** デリミタ（#） */
	public static final String SHARP_DELIMIT = "#";
	/** デリミタ（/） */
	public static final String SLASH_DELIMIT = "/";

	/** 時刻フォーマット(YYYY-MM-dd HH:mm:SSS) */
	public static final String LONG_TIMESTAMP = "YYYY-MM-dd HH:mm:SSS";

	/** コンソール着色文字（RED） */
	public static final String RED = "\u001b[00;31m";
	/** コンソール着色文字（RED） */
	public static final String GREEN = "\u001b[00;32m";
	/** コンソール着色文字（RED） */
	public static final String YELLOW = "\u001b[00;33m";
	/** コンソール着色文字（RED） */
	public static final String PURPLE = "\u001b[00;34m";
	/** コンソール着色文字（RED） */
	public static final String PINK = "\u001b[00;35m";
	/** コンソール着色文字（RED） */
	public static final String CYAN = "\u001b[00;36m";
	/** コンソール着色文字（RED） */
	public static final String GRAY = "\u001b[00;37m";
	/** コンソール着色文字（終端） */
	public static final String END = "\u001b[00m";

	/** ハッシュ関数デフォルトアルゴリズム(MD5) */
	public static final String MD5 = "md5";
	/** ハッシュ関数デフォルトアルゴリズム(SHA-256) */
	public static final String SHA_256 = "sha-256";

	/** ランダムアクセスモード（読み取り専用） */
	public static final String RAND_READ_ONLY = "r";
	/** ランダムアクセスモード（読み書き） */
	public static final String RAND_READ_WRITE = "rw";

	/** 埋込文字（?） */
	public static final String EMBEDDED_EXCLAMATION = "?";

	/** Yes */
	public static final String YES_STR = "yes";

	/** モジュール名称 */
	public static final String MODULE_NAME = "com.sakulabo.core";

	/** リフレクション（コンテキスト保持フィールド名称） */
	public static final String REF_CONTEXT = "_CONTEXT";

	/**
	 * 対象インスタンスをクラス文字列表現に変換します
	 * @param instance 変換対象
	 * @return 変換後文字列
	 */
	public static final String toClassName(Object instance) {
		return instance.getClass().getCanonicalName();
	}
}

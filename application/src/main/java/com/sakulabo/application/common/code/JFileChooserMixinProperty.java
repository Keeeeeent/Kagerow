package com.sakulabo.application.common.code;

/**
 * JFileChooserMixin向けファイル拡張子を表す定数値クラスです
 * 
 * @author keeeeeent
 */
public enum JFileChooserMixinProperty {

	/** Kagerowスクリプト */
	KAGEROW_SCRIPT(new String[] { "ksql", "sksql" }),
	/** 取り込み対象ファイル */
	DEFAULT_IMPORT(new String[] { "csv", "tsv" }),
	/** Kagerowバックアップ */
	KAGEROW_BUCKUP(new String[] { "kagerow" });

	/** デフォルトフォーマット */
	private static final String FORMAT = " *.%s";
	/** カスタムフォーマット */
	private static final String CUSTOM_FORMAT = "%s (%s )";

	/** 拡張子リスト */
	private final String[] extensions;

	/**
	 * デフォルトコンストラクタ
	 * @param extensions 拡張子リスト
	 */
	private JFileChooserMixinProperty(String[] extensions) {
		this.extensions = extensions;
	}

	/**
	 * フィルター対象の拡張子リストを返却します
	 * @return 拡張子リスト
	 */
	public String[] toExtensions() {
		return extensions;
	}

	/**
	 * フィルター名称を生成します<br/>
	 * オプションは1つの要素からなる配列である必要があります
	 * @param option 名称オプション
	 * @return フィルター名称
	 */
	@SafeVarargs
	public final String toOverview(String... option) {
		// 返却用変数処理帰化
		StringBuffer buffer = new StringBuffer();
		// 拡張子セット生成
		for (String ext : extensions)
			buffer.append(FORMAT.formatted(ext));
		if (option.length != 0) {
			// 拡張子セット保管
			String ext = new String(buffer);
			// バッファ再初期化
			buffer = new StringBuffer();
			buffer.append(CUSTOM_FORMAT.formatted(option[0], ext));
		}
		return new String(buffer);
	}

}

package com.sakulabo.core.Common;

import java.text.MessageFormat;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

import com.sakulabo.core.Kagerow.Exception.ApplicationError;

/**
 * Kagerowアプリケーション専用ワード生成クラスです
 * 
 * @author keeeeeent
 */
public enum ApplicationWordDictionary {

	/** KDBの生成 */
	WCD_0001("KDB"),
	/** 文字コードの解析 */
	WCD_0002("BOM_ANALYSIS"),
	/** 不正なキャッシュID */
	WCD_0003(StringUtils.EMPTY),
	/** KSQL */
	WCD_0004(StringUtils.EMPTY),
	/** プラグイン */
	WCD_0005(StringUtils.EMPTY),
	/** DDLプラグイン名称 */
	WCD_0006(StringUtils.EMPTY),
	/** DDLプラグインパラメータ名称 */
	WCD_0007(StringUtils.EMPTY)
	;

	/** エラーメッセージシノニム */
	private String synonym;

	/**
	 * デフォルトコンストラクタ
	 * @param synonym シノニム
	 */
	private ApplicationWordDictionary(String synonym) {
		this.synonym = synonym;
	}

	/** メッセージファイル */
	private static final String FILE_NAME = "config.message.word-dictionary";
	/** リソースバンドル */
	private static final ResourceBundle message;
	static {

		Optional<Module> module = ErrorMessage.class
				.getModule()
				.getLayer()
				.findModule(StringUtils.MODULE_NAME);

		if (module.isPresent()) {
			message = ResourceBundle.getBundle(FILE_NAME, module.get());
		} else {
			Exception e = new Exception("not find resource file " + FILE_NAME);
			throw new ApplicationError(e);
		}
	}

	/**
	 * メッセージを生成します
	 * @param param パラメータ
	 * @return メッセージ
	 */
	@SafeVarargs
	public final String getMessage(Object... param) {
		String result = message.getString(name());
		return MessageFormat.format(result, param);
	}

	/**
	 * メッセージを生成します
	 * @param synonym シノニム
	 * @param param パラメータ
	 * @return メッセージ
	 */
	@SafeVarargs
	public static final String getMessage(ApplicationWordDictionarySynonym synonym, Object... param) {
		ApplicationWordDictionary target = null;
		for (ApplicationWordDictionary msg : values()) {
			if (msg.synonym.equals(synonym.name())) {
				target = msg;
			}
		}
		if (Objects.isNull(target)) {
			Exception e = new Exception("not find synonym in ApplicationWordDictionary -> " + synonym);
			throw new ApplicationError(e);
		}
		String result = message.getString(target.name());
		return MessageFormat.format(result, param);
	}

	/**
	 * シノニムを返却します
	 * @return シノニム文字列
	 */
	public final String tosynonym() {
		return synonym;
	}
}

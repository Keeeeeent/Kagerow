package com.sakulabo.library.common;

import java.text.MessageFormat;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * プラグイン共通のメッセージ生成クラスです
 * 
 * @author keeeeeent
 */
public enum DefaultPluginMessage {

	/** 指定された文字列 [{0}] から文字コードの生成に失敗しました */
	E0001(),
	/** 指定された文字列 [{0}] からパスの生成に失敗しました */
	E0002(),
	/** 指定された文字列 [{0}] から数値の生成に失敗しました */
	E0003(),
	/** 指定されたパス [{0}] が存在しません。実行するにはパスが存在している必要があります */
	E0004(),
	/** 指定されたパス [{0}] には親ディレクトリが存在しません。実行するには親ディレクトリが存在している必要があります */
	E0005(),
	/** 指定されたパラメータ[{0}]は不正です */
	E0006(),
	/** 予期せぬエラーが発生しました */
	E0007(),
	/** 指定されたパラメータ[{0}] からドライバーの生成に失敗しました */
	E0008(),
	/** 指定されたパラメータ[{0}] からコネクションの生成に失敗しました */
	E0009(),
	;

	/** リソースバンドル */
	private static final ResourceBundle message;
	static {

		Module module = DefaultPluginMessage.class.getModule();

		if (Objects.nonNull(module)) {
			message = ResourceBundle.getBundle(DefaultPluginMessage.class.getSimpleName(), module);
		} else {
			message = null;
			System.err.println("not find resource file " + DefaultPluginMessage.class.getSimpleName());
		}
	}

	/**
	 * デフォルトコンストラクタ
	 */
	private DefaultPluginMessage() {
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		if (Objects.isNull(message)) {
			throw new RuntimeException("not find resource file " + DefaultPluginMessage.class.getSimpleName());
		}
		return message.getString(name());
	}

	/**
	 * 指定されたパラメータを埋込文字として利用した文字列を生成します
	 * @param params 埋込文字リスト
	 * @return 生成文字列
	 */
	public String toString(Object[] params) {
		if (Objects.isNull(message)) {
			throw new RuntimeException("not find resource file " + DefaultPluginMessage.class.getSimpleName());
		}
		String msg = message.getString(name());
		msg = MessageFormat.format(msg, params);
		return msg;
	}

}

package com.sakulabo.core.Kagerow.Utilities;

import java.util.Map;

import com.sakulabo.core.Processor.command.AppProcessBuilderImpl;

/**
 * Kagerowアプリケーションの汎用コマンド実行インターフェイスです
 * 
 * @author keeeeeent
 */
public sealed interface KagerowProcessBuilder permits AppProcessBuilderImpl {

	/**
	 * プロセスビルダーファクトリメソッド
	 * @param mode モード
	 * @param env 環境変数
	 * @return アプリケーションプロセスビルダー
	 */
	public static KagerowProcessBuilder newProcessBuilderMap(KagerowCommandMode mode, Map<String, String> env) {
		return new AppProcessBuilderImpl(mode, env);
	}

	/**
	 * コマンドライン引数を追加します<br/>
	 * @param arg コマンドライン引数
	 * @return AppProcessBuilder
	 */
	KagerowProcessBuilder addArg(String arg);

	/**
	 * コマンド実行環境の環境変数を設定します<br/>
	 * ここで設定した環境変数はプロセス内部でのスコープをもつため、実行環境の汚染しません。
	 * @param key 環境変数名
	 * @param value 環境変数
	 * @return AppProcessBuilder
	 */
	KagerowProcessBuilder addEnv(String key, String value);

	/**
	 * 指定したコマンドでコマンドビルダーを構築します
	 * @return コマンドビルダーインスタンス
	 */
	ProcessBuilder build();

}

package com.sakulabo.application;

import com.sakulabo.core.Kagerow.KagerowApplication;

/**
 * アプリケーションを起動するエントリーポイントです
 *
 * @author keeeeeent
 */
public class Main {

	/** コマンドライン引数 */
	public static volatile String[] args = new String[0];

	/**
	 * アプリケーションエントリー
	 *
	 * @param args コマンドライン引数
	 * @throws Exception 想定外の例外
	 */
	public static void main(String[] args) throws Exception {

		// 引数格納
		Main.args = args;

		// アプリケーション実行に必要な最初期化処理を実行します
		KagerowApplication.automaticInstance();

	}

}

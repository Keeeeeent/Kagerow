package com.sakulabo.application;

import com.sakulabo.core.Kagerow.KagerowApplication;

/**
 * アプリケーションを起動するエントリーポイントです
 *
 * @author keeeeeent
 */
public class Main {

	/**
	 * アプリケーション実行に必要な最初期化処理を実行します
	 */
	static {
		// Kagerowライブラリロード
		KagerowApplication.automaticInstance();
	}

	/**
	 * アプリケーションエントリー
	 *
	 * @param args コマンとライン引数
	 * @throws Exception 想定外の例外（デモンストレーション向け）
	 */
	public static void main(String[] args) throws Exception {
		;
	}

}

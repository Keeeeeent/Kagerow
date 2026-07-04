package com.sakulabo.application;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;

import com.sakulabo.application.app.rpc.RpcServer;
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

	static {
		try {
			RpcServer rpc = new RpcServer(null, 8080);
			rpc.start();
			Thread.sleep(Duration.ofSeconds(10));
			rpc.stop();
		} catch (NoSuchAlgorithmException | IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
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

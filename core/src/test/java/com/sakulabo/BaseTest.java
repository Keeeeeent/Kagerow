package com.sakulabo;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * テスト実装の基底クラスです 
 * @param <T> テスト対象の型情報
 */
public abstract class BaseTest<T> {

	/** テストデータフォルダ */
	protected final Path testDir;
	/** テスト実施クラス */
	private final Class<?> testTarget;
	/** モック管理インスタンス */
	protected AutoCloseable closeable;

	/**
	 * デフォルトコンストラクタ
	 * @param testTarget テスト対象
	 */
	protected BaseTest(Class<?> testTarget) {
		testDir = Paths.get("testData", "UT_".concat(testTarget.getSimpleName())).toAbsolutePath();
		this.testTarget = testTarget;
		setEnv();
	}

	/**
	 * テスト環境の環境設定
	 */
	private void setEnv() {
		System.setProperty("app.io.archivedatadir", ".kagerow/database");
		System.setProperty("app.io.tmpdir", ".kagerow/tmp");
		System.setProperty("app.logs.dir", ".kagerow/logs");
		System.setProperty("app.io.archivedatadir", ".kagerow/database");
		System.setProperty("user.home", "../");
	}

	/**
	 * 出力先専用のパスを生成します
	 * @param fileName 出力ファイル名称
	 * @return 出力専用パス
	 */
	protected Path getOutputPath(String fileName) {
		fileName = String.join("_", testTarget.getCanonicalName(), fileName);
		return Paths.get("testData/output").toAbsolutePath().resolve(fileName);
	}

	/**
	 * 入力専用のパスを生成します
	 * @param fileName 入力ファイル名称
	 * @return 入力専用パス
	 */
	protected Path getInputPath(String fileName) {
		return testDir.resolve(fileName);
	}

}

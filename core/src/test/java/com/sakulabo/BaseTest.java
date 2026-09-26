package com.sakulabo;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.naming.NameAlreadyBoundException;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import com.sakulabo.core.Kagerow.KagerowApplication;
import com.sakulabo.core.Kagerow.Contents.KagerowVirtualFileContent;
import com.sakulabo.core.Kagerow.Context.KagerowVirtualDirContext;
import com.sakulabo.core.Kagerow.Context.KagerowVirtualFileContext;
import com.sakulabo.core.Kagerow.Utilities.KagerowChunkCreater.ChunkCreateMode;
import com.sakulabo.core.Kagerow.Utilities.KagerowUtilities;
import com.sakulabo.core.Kagerow.Utilities.KagerowVirtualFileCreater;

/**
 * テスト実装の基底クラスです
 * 
 * @param <T> テスト対象の型情報
 */
public abstract class BaseTest<T> {

	/** テストデータフォルダ */
	private Path testDir;
	/** テスト実施クラス */
	private Class<?> testTarget;
	/** モック管理インスタンス */
	protected AutoCloseable closeable;

	/**
	 * テスト環境の環境設定
	 */
	private static void setEnv() {
		System.setProperty("app.io.archivedatadir", ".kagerow/database");
		System.setProperty("app.io.tmpdir", ".kagerow/tmp");
		System.setProperty("app.logs.dir", ".kagerow/logs");
		System.setProperty("user.home", "../");
	}

	/**
	 * 出力先専用のパスを生成します
	 * 
	 * @param fileName 出力ファイル名称
	 * @return 出力専用パス
	 */
	protected Path getOutputPath(String fileName) {
		fileName = String.join("_", testTarget.getCanonicalName(), fileName);
		return Paths.get("testData/output").toAbsolutePath().resolve(fileName);
	}

	/**
	 * 入力専用のパスを生成します
	 * 
	 * @param fileName 入力ファイル名称
	 * @return 入力専用パス
	 */
	protected Path getInputPath(String fileName) {
		return testDir.resolve(fileName);
	}

	/**
	 * テストフォルダを生成します
	 * 
	 * @return テストフォルダ
	 */
	protected Path getTestDir() {
		return testDir;
	}

	/**
	 * テスト環境をクリーンアップします
	 */
	protected static void cleanUpEnv() throws IOException {
		Path kagerowHome = Paths.get(".").toAbsolutePath()
				.getParent()
				.getParent()
				.resolve(".kagerow");
		for (Path file : Files.walk(kagerowHome).toList()) {
			if (file.endsWith(".gitkeep")) {
				continue;
			}
			if (!Files.isDirectory(file)) {
				Files.delete(file);
			}
		}
		for (Path path : Files.walk(kagerowHome).toList()) {
			if (path.endsWith(".gitkeep")
					|| path.endsWith(".kagerow")
					|| Files.exists(path.resolve(".gitkeep"))) {
				continue;
			}
			Files.delete(path);
		}
		setEnv();
	}

	/**
	 * Kagerow実行環境ランナー
	 */
	public static class KagerowContainerRunner
			implements BeforeEachCallback, AfterEachCallback, BeforeAllCallback, AfterAllCallback {

		@Override
		public void beforeAll(ExtensionContext context) throws Exception {
			cleanUpEnv();
			KagerowApplication.getInstance();
		}

		@Override
		public void beforeEach(ExtensionContext context) throws Exception {
			Class<?> testTarget = context.getRequiredTestClass();
			BaseTest<?> testInstance = (BaseTest<?>) context.getRequiredTestInstance();
			testInstance.testTarget = testTarget;
			testInstance.testDir = Paths.get("testData", "UT_".concat(testTarget.getSimpleName())).toAbsolutePath();
			setEnv();
		}

		@Override
		public void afterEach(ExtensionContext context) throws Exception {
			;
		}

		@Override
		public void afterAll(ExtensionContext context) throws Exception {
			cleanUpEnv();
		}

		/**
		 * Kagerow実行環境ランナー(セキュアブート)
		 */
		public static class KagerowSecureContainerRunner extends KagerowContainerRunner {

			@Override
			public void beforeAll(ExtensionContext context) throws Exception {
				cleanUpEnv();
				// コンテキストをリセット
				VarHandle handle = MethodHandles.privateLookupIn(KagerowApplication.class, MethodHandles.lookup())
						.findStaticVarHandle(KagerowApplication.class, "application", KagerowApplication.class);
				handle.setVolatile(null);
				// セキュアコンテキスト生成
				KagerowApplication.getInstance("test");
			}

		}

	}

	public static class KagerowDBRunner implements ParameterResolver {

		@Target(ElementType.PARAMETER)
		@Retention(RetentionPolicy.RUNTIME)
		public static @interface KDB {
			ChunkCreateMode mode() default ChunkCreateMode.CSV;

			String schema();

			String path();

			String charset() default "UTF-8";

			boolean isHeader() default false;;

			String synonym() default "";

			boolean isSecure() default false;
		}

		@Override
		public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
				throws ParameterResolutionException {
			return parameterContext.isAnnotated(KDB.class);
		}

		@Override
		public URI resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
				throws ParameterResolutionException {
			KDB annotation = parameterContext
					.findAnnotation(KDB.class)
					.orElseThrow();
			// パラメータ取得
			ChunkCreateMode mode = annotation.mode();
			String schema = annotation.schema();
			String path = annotation.path();
			Charset charset = Charset.forName(annotation.charset());
			boolean isHeader = annotation.isHeader();
			String synonym = annotation.synonym().isEmpty() ? null : annotation.synonym();
			boolean isSecure = annotation.isSecure();
			// データインポート
			BaseTest<?> testInstance = (BaseTest<?>) extensionContext.getRequiredTestInstance();
			Path testData = testInstance.getTestDir().resolve(path);
			try {
				return KagerowVirtualFileCreater.constructionKDB(
						mode,
						schema,
						testData,
						charset,
						isHeader,
						synonym,
						isSecure);
			} catch (NameAlreadyBoundException _) {
				try {
					KagerowVirtualFileContext ctx = KagerowUtilities.getContext(KagerowVirtualFileContext._NAME);
					KagerowVirtualDirContext cnt = ctx.lookup(schema);
					String table = cnt.getSynonymMapList().get(synonym);
					KagerowVirtualFileContent file = cnt.lookup(table);
					return URI.create(file.get(0).uri().get());
				} catch (Exception e) {
					throw new ParameterResolutionException(e.getMessage(), e);
				}
			} catch (Exception e) {
				throw new ParameterResolutionException(e.getMessage(), e);
			}
		}
	}

}

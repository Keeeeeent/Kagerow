package com.sakulabo;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.naming.Context;
import javax.naming.NameAlreadyBoundException;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.params.converter.SimpleArgumentConverter;

import com.sakulabo.core.Kagerow.KagerowApplication;
import com.sakulabo.core.Kagerow.Context.KagerowVirtualFileContext;
import com.sakulabo.core.Kagerow.Utilities.KagerowChunkCreater.ChunkCreateMode;
import com.sakulabo.core.Kagerow.Utilities.KagerowUtilities;
import com.sakulabo.core.Kagerow.Utilities.KagerowVirtualFileCreater;

import sun.misc.Unsafe;

/**
 * テスト実装の基底クラスです
 * 
 * @param <T> テスト対象の型情報
 */
public abstract class BaseTest {

	/** テストデータフォルダ */
	private Path testDir;
	/** テスト実施クラス */
	private Class<?> testTarget;
	/** モック管理インスタンス */
	protected AutoCloseable closeable;

	@SuppressWarnings("unchecked")
	protected static <R extends Enum<?>> R createEnum(Class<?> target) {
		try {
			Field field = Unsafe.class.getDeclaredField("theUnsafe");
			field.setAccessible(true);
			Unsafe unsafe = (Unsafe) field.get(null);
			return (R) unsafe.allocateInstance(target);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

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

	public static class KagerowSystemPropertyRunner implements BeforeEachCallback, AfterEachCallback {

		@Retention(RetentionPolicy.RUNTIME)
		@Target(ElementType.METHOD)
		@Repeatable(SystemProperty.List.class)
		public @interface SystemProperty {

			String key();

			String value() default "";

			@Retention(RetentionPolicy.RUNTIME)
			@Target(ElementType.METHOD)
			public @interface List {
				SystemProperty[] value();
			}

		}

		private record EnvData(String settingValue, String originalValue) {
		};

		@Override
		public void beforeEach(ExtensionContext context) throws Exception {
			List<EnvData> envList = new ArrayList<>();
			SystemProperty[] annotationList = context.getRequiredTestMethod()
					.getDeclaredAnnotationsByType(SystemProperty.class);
			for (SystemProperty annotation : annotationList) {
				String key = annotation.key();
				String originalValue = System.getProperty(key);
				String settingValue = annotation.value();
				if (settingValue.isEmpty()) {
					System.clearProperty(key);
				} else {
					System.setProperty(key, settingValue);
				}
				envList.add(new EnvData(key, originalValue));
			}
			Namespace namespace = ExtensionContext.Namespace.create(context.getRequiredTestClass());
			Store store = context.getStore(namespace);
			store.put(context.getRequiredTestMethod(), envList);
		}

		@Override
		public void afterEach(ExtensionContext context) throws Exception {
			Namespace namespace = ExtensionContext.Namespace.create(context.getRequiredTestClass());
			Store store = context.getStore(namespace);
			@SuppressWarnings("unchecked")
			List<EnvData> envList = (List<EnvData>) store.get(context.getRequiredTestMethod());
			for (EnvData env : envList) {
				String name = env.settingValue();
				String originalValue = env.originalValue();
				if (originalValue == null) {
					System.clearProperty(name);
				} else {
					System.setProperty(name, originalValue);
				}
			}
		}
	}

	/**
	 * Kagerow実行環境ランナー
	 */
	public static class KagerowContainerRunner
			implements BeforeEachCallback, AfterEachCallback, BeforeAllCallback, AfterAllCallback {

		private final VarHandle handle;
		private final VarHandle context;

		public KagerowContainerRunner() throws Exception {
			handle = MethodHandles
					.privateLookupIn(KagerowApplication.class, MethodHandles.lookup())
					.findStaticVarHandle(KagerowApplication.class, "application", KagerowApplication.class);
			context = MethodHandles
					.privateLookupIn(KagerowApplication.class, MethodHandles.lookup())
					.findVarHandle(KagerowApplication.class, "context", Context.class);
		}

		@Override
		public void beforeAll(ExtensionContext context) throws Exception {
			cleanUpEnv();
			handle.setVolatile(null);
			KagerowApplication.getInstance();
		}

		@Override
		public void beforeEach(ExtensionContext context) throws Exception {
			Class<?> testTarget = context.getRequiredTestClass();
			BaseTest testInstance = (BaseTest) context.getRequiredTestInstance();
			testInstance.testTarget = testTarget;
			String className = testTarget.getSimpleName();
			className = className.replace("_", "/");
			testInstance.testDir = Paths.get("testData", "UT_".concat(className)).toAbsolutePath();
			setEnv();
		}

		@Override
		public void afterEach(ExtensionContext context) throws Exception {
		}

		@Override
		public void afterAll(ExtensionContext context) throws Exception {
			Context ctx = (Context) this.context.getVolatile(KagerowApplication.getInstance());
			ctx.close();
			cleanUpEnv();
		}

		/**
		 * Kagerow実行環境ランナー(セキュアブート)
		 */
		public static class KagerowSecureContainerRunner extends KagerowContainerRunner {

			public KagerowSecureContainerRunner() throws Exception {
				super();
			}

			@Override
			public void beforeAll(ExtensionContext context) throws Exception {
				cleanUpEnv();
				super.handle.setVolatile(null);
				KagerowApplication.getInstance("test");
			}

		}

	}

	public static class KagerowDBRunner implements BeforeEachCallback {

		@Target(ElementType.METHOD)
		@Retention(RetentionPolicy.RUNTIME)
		public static @interface KDB {
			ChunkCreateMode mode()

			default ChunkCreateMode.CSV;

			String schema();

			String path();

			String charset()

			default "UTF-8";

			boolean isHeader()

			default false;;

			String synonym()

			default "";

			boolean isSecure() default false;
		}

		@Override
		public void beforeEach(ExtensionContext context) throws Exception {
			KDB[] kdbList = context.getRequiredTestMethod()
					.getDeclaredAnnotationsByType(KDB.class);
			for (KDB kdb : kdbList) {
				// パラメータ取得
				ChunkCreateMode mode = kdb.mode();
				String schema = kdb.schema();
				String path = kdb.path();
				Charset charset = Charset.forName(kdb.charset());
				boolean isHeader = kdb.isHeader();
				String synonym = kdb.synonym().isEmpty() ? null : kdb.synonym();
				boolean isSecure = kdb.isSecure();
				// データインポート
				BaseTest testInstance = (BaseTest) context.getRequiredTestInstance();
				Path testData = testInstance.getTestDir().resolve(path);
				try {
					KagerowVirtualFileCreater.constructionKDB(
							mode,
							schema,
							testData,
							charset,
							isHeader,
							synonym,
							isSecure);
				} catch (NameAlreadyBoundException _) {
					// ignore
				}
			}

		}
	}

	public static class ToNotNullableString extends SimpleArgumentConverter {

		@Override
		protected Object convert(Object source, Class<?> targetType) {
			return Objects.toString(source, "");
		}
	}

	public static class KagerowSchemaCreateRunner implements BeforeEachCallback {

		@Target(ElementType.METHOD)
		@Retention(RetentionPolicy.RUNTIME)
		public @interface NeedsKagerowSchema {
			String value();
		}

		@Override
		public void beforeEach(ExtensionContext context) throws Exception {
			NeedsKagerowSchema[] kagerowSchemas = context.getRequiredTestMethod()
					.getDeclaredAnnotationsByType(NeedsKagerowSchema.class);
			KagerowVirtualFileContext ctx = KagerowUtilities.getContext(KagerowVirtualFileContext._NAME);
			for (NeedsKagerowSchema kagerowSchema : kagerowSchemas) {
				String needsSchema = kagerowSchema.value();
				if (!ctx.isExist(needsSchema)) {
					ctx.createSubcontext(needsSchema);
				}
			}
		}

	}

}

package com.sakulabo.core.Processor.plugin;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.module.Configuration;
import java.lang.module.FindException;
import java.lang.module.ModuleFinder;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.naming.Name;

import com.sakulabo.core.Common.AppPathUtils;
import com.sakulabo.core.Common.StringUtils;
import com.sakulabo.core.Common.ThreadUtils;
import com.sakulabo.core.Kagerow.Exception.ApplicationError;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;
import com.sakulabo.core.Kagerow.Utilities.KagerowUtilities;
import com.sakulabo.regulation.annotation.processor.AppPluginProcessor;
import com.sakulabo.regulation.spi.PluginAdapter;

/**
 * アプリケーション共通で使用されるプラグイン専用クラスローダーです
 * @author keeeeeent
 */
public class KagerowClassLoader extends URLClassLoader {

	/** スレッド名称インデックスマップ */
	private static final Map<String, AtomicInteger> INDEX = new ConcurrentHashMap<>();
	/** スレッド名称フォーマット */
	private static final String NAME_FORMAT = "PluginThread@%s-%d";
	/** 読み込みファイルパス */
	private final Path path;
	/** モジュール */
	private final Optional<ModuleLayer> moduleLayer;
	/** プラグインパッケージ名称 */
	private final Optional<String> pluginPackage;
	/** プラグインバージョン */
	private final Optional<Name> pluginVersion;

	/** プラグイン格納プレフィックス */
	private final static String PLUGIN_LIBRARY = "lib/";

	/**
	 * 内部向けコンストラクタ
	 */
	public KagerowClassLoader() {

		// クラスローダー初期化
		super(new URL[] {}, ClassLoader.getSystemClassLoader());

		// フィールド初期化
		this.path = null;
		this.moduleLayer = Optional.empty();
		this.pluginPackage = Optional.empty();
		this.pluginVersion = Optional.empty();

	}

	/**
	 * デフォルトコンストラクタ
	 * @param path プラグインファイルパス
	 */
	public KagerowClassLoader(Path path) {

		// クラスローダー初期化
		super(new URL[] {}, ClassLoader.getSystemClassLoader());

		// フィールド初期化
		this.path = path;

		try {

			// 一時展開ディレクトリを生成
			final Path tmpDir = Files.createTempDirectory(AppPathUtils.createTemporaryDirPath(), StringUtils.EMPTY);
			// JVM終了時ディレクトリ削除
			tmpDir.toFile().deleteOnExit();
			// モジュールロード用Pathリスト
			List<Path> pathList = new ArrayList<>();
			// モジュール名称
			String moduleName = StringUtils.EMPTY;
			// プラグインパッケージ名称
			String pluginPkgName = StringUtils.EMPTY;
			// プラグインバージョン
			Name pluginVersion = null;

			// jarファイルロード
			try (InputStream input = Files.newInputStream(this.path);
					ZipInputStream zinput = new ZipInputStream(input);) {
				ZipEntry entry = null;
				while ((entry = zinput.getNextEntry()) != null) {
					if (AppPluginProcessor.SETTING_FILE_NAME.equals(entry.getName())) {
						// 設定読み込み
						Properties prop = new Properties();
						ByteArrayInputStream byteInput = new ByteArrayInputStream(zinput.readAllBytes());
						prop.load(byteInput);
						{
							// モジュール名取得
							String name = prop.getProperty(AppPluginProcessor.SETTING_FILE_KEY_MODULE_NAME);
							// モジュール名称が指定されている場合、デフォルトを上書き
							if (Objects.nonNull(name) && !name.isEmpty()) {
								moduleName = name;
							}
						}
						{
							// プラグインパッケージ名取得
							String name = prop.getProperty(AppPluginProcessor.SETTING_FILE_KEY_PLUGIN_PKG_NAME);
							// プラグイン名が指定されている場合、デフォルトを上書き
							if (Objects.nonNull(name) && !name.isEmpty()) {
								pluginPkgName = name;
							}
						}
						{
							// プラグインバージョン取得
							String majorVersion = prop.getProperty(AppPluginProcessor.MAJOR_VERSION);
							String minorVersion = prop.getProperty(AppPluginProcessor.MINOR_VERSION);
							String patchVersion = prop.getProperty(AppPluginProcessor.PATCH_VERSION);
							// バージョン生成
							pluginVersion = KagerowUtilities.createVersioningPluginPkgName(
									pluginPkgName,
									String.join(StringUtils.DOT_STR,
											majorVersion, minorVersion, patchVersion));
						}
						continue;
					}
					if (entry.getName().startsWith(PLUGIN_LIBRARY)) {
						Path tmpJarFile = tmpDir.resolve(entry.getName().replace(PLUGIN_LIBRARY, StringUtils.EMPTY));
						try (OutputStream output = Files.newOutputStream(tmpJarFile)) {
							// ファイル展開
							zinput.transferTo(output);
							// JVM終了時ファイル削除
							tmpJarFile.toFile().deleteOnExit();
						}
						// クラスローダーのURLとして追加
						addURL(tmpJarFile.toUri().toURL());
						// モジュール検索対象パスとして登録
						pathList.add(tmpJarFile);
					}
				}

			}

			// モジュールの初期化
			Optional<ModuleLayer> layer = Optional.empty();
			lord: try {
				// モジュール設定が見つからなかった場合、即時終了
				if (StringUtils.EMPTY.equals(moduleName)) {
					break lord;
				}
				// モジュールの設定
				ModuleFinder moduleFinder = ModuleFinder.of(pathList.toArray(Path[]::new));
				// モジュールレイヤー設定
				ModuleLayer bootLayer = ModuleLayer.boot();
				Configuration configuration = bootLayer.configuration().resolve(
						moduleFinder,
						ModuleFinder.of(),
						Set.of(moduleName));
				// モジュールルックアップ
				layer = Optional.ofNullable(ModuleLayer
						.defineModulesWithOneLoader(configuration, List.of(bootLayer), this)
						.layer());
			} catch (FindException e) {
				KagerowLogger.newAppLogger().log(Level.WARNING, e.getMessage(), new Object[0]);
			}

			// モジュールをメモリに設定
			this.moduleLayer = layer;

			// プラグインパッケージ名をメモリに設定
			if (StringUtils.EMPTY.equals(pluginPkgName)) {
				this.pluginPackage = Optional.empty();
			} else {
				this.pluginPackage = Optional.of(pluginPkgName);
			}

			// プラグインバージョンをメモリに設定
			if (Objects.nonNull(pluginVersion)) {
				this.pluginVersion = Optional.of(pluginVersion);
			} else {
				this.pluginVersion = Optional.empty();
			}

		} catch (Exception e) {
			// ハンドラ取得失敗の場合、アプリケーションを終了
			throw new ApplicationError(e);
		}

	}

	/**
	 * クラスローダーが管理しているバーチャルスレッドを返却します
	 * @param name スレッド名称
	 * @return プラグイン専用スレッド
	 */
	public final Thread currentThread(String name) {
		return currentThread(name, ThreadUtils.EMPTY_RUNNABLE);
	}

	/**
	 * クラスローダーが管理しているバーチャルスレッドを返却します
	 * @param name   スレッド名称
	 * @param runner ランナー実装
	 * @return プラグイン専用スレッド
	 */
	public final Thread currentThread(String name, Runnable runner) {
		Thread currentThread = Thread.ofVirtual().unstarted(runner);
		name = createName(name);
		currentThread.setContextClassLoader(this);
		currentThread.setDaemon(true);
		currentThread.setName(name);
		return currentThread;
	}

	/**
	 * プラグインスレッドの名称を生成します
	 * @param name プラグイン名称
	 * @return 名称
	 */
	private String createName(String name) {
		int index = -1;
		if (INDEX.containsKey(name)) {
			index = INDEX.get(name).incrementAndGet();
		} else {
			AtomicInteger atomicInteger = new AtomicInteger();
			INDEX.put(name, atomicInteger);
			index = atomicInteger.incrementAndGet();
		}
		return String.format(NAME_FORMAT, name, index);
	}

	/**
	 * クラスローダーが管理しているプラットフォームスレッドを返却します
	 * @param name   スレッド名称
	 * @param plugin プラグインアダプター
	 * @return プラグイン専用スレッド
	 */
	public final Thread shutdownThread(String name, PluginAdapter plugin) {
		Thread currentThread = new Thread(() -> {
			// 終了処理実行
			try {
				plugin.close();
			} catch (Exception e) {
				KagerowLogger.newAppLogger().err(e);
			}
		});
		currentThread.setContextClassLoader(this);
		currentThread.setName(name);
		return currentThread;
	}

	/**
	 * プラグインファイルの物理パスを返却します
	 * @return プラグインファイル
	 */
	public final Path getPluginFilePath() {
		return path;
	}

	/**
	 * モジュールを取得します
	 * @return 生成モジュール
	 */
	public final Optional<ModuleLayer> getModule() {
		return moduleLayer;
	}

	/**
	 * プラグインパッケージ名称を取得します
	 * @return プラグインパッケージ名称
	 */
	public final Optional<String> getPluginPkg() {
		return pluginPackage;
	}

	/**
	 * プラグインバージョンを取得します
	 * @return プラグインバージョン
	 */
	public final Optional<Name> getPluginVersion() {
		return pluginVersion;
	}

}

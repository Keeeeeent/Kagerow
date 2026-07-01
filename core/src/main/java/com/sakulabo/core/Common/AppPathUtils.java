package com.sakulabo.core.Common;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * Kagerowアプリケーション専用パス生成ユーティリティクラスです
 * 
 * @author keeeeeent
 */
public final class AppPathUtils {

	/** Kagerowホームディレクトリ */
	private static final String KAGEROW_HOME = ".kagerow";
	/** KDBファイル拡張子 */
	public static final String KDB_FILE_EXT = ".mv.db";
	/** KDBファイルプレフィックス */
	public static final String KDB_FILE_PREFIX = "kdb";
	/** 一時ファイル拡張子 */
	public static final String TMP_FILE_EXT = ".tmp";
	/** キャッシュファイル拡張子 */
	public static final String CACHE_FILE_EXT = ".cache";
	/** jarファイル拡張子 */
	public static final String JAR_EXTENSION = ".jar";
	/** zipファイル拡張子 */
	public static final String ZIP_EXTENSION = ".zip";

	/**
	 * インスタンス生成禁止
	 */
	private AppPathUtils() {
		;
	}

	/**
	 * ベースパスを生成します
	 * @return ベースパス文字列表現
	 */
	public static String getBasePath() {
		String userHome = VMOption.USER_HOME.getVMoption();
		String appHome = VMOption.APP_HOME.getVMoption();
		return Objects.isNull(appHome) ? userHome : appHome;
	}

	/**
	 * アプリケーションインストールディレクトリのフルパスを生成します
	 * @return 生成されたパス
	 */
	public static Path createAppDirPath() {
		Path result = null;
		final int upCount = 2;
		try {
			URI jarPath = AppPathUtils.class.getProtectionDomain()
					.getCodeSource()
					.getLocation()
					.toURI();
			result = Paths.get(jarPath);
			int count = 0;
			do {
				if (Objects.nonNull(result.getParent())) {
					result = result.getParent();
				}
			} while (++count < upCount);
		} catch (URISyntaxException e) {
			;
		}
		return result;
	}

	/**
	 * アプリケーションインストールディレクトリの設定ファイルパスを生成します
	 * @return 生成されたパス
	 */
	public static Path createConfigDirPath() {
		Path result = createAppDirPath();
		if (Objects.nonNull(result)) {
			result = result.resolve("config");
		}
		return result;
	}

	/**
	 * アプリケーションインストールディレクトリのプラグインファイルパスを生成します
	 * @return 生成されたパス
	 */
	public static Path createDefaultPluginDirPath() {
		Path result = createAppDirPath();
		if (Objects.nonNull(result)) {
			result = result.resolve("plugin");
		}
		return result;
	}

	/**
	 * アプリケーションのアーカイブファイルパスを生成します
	 * @return 生成されたパス
	 */
	public static Path createArchiveDirPath() {
		String archivePath = VMOption.APP_IO_ARCHIVEDATADIR.getVMoption();
		String home = getBasePath();
		return Paths.get(home, archivePath);
	}

	/**
	 * アプリケーションのプラグインファイルパスを生成します
	 * @return 生成されたパス
	 */
	public static Path createPluginDirPath() {
		String home = getBasePath();
		return Paths.get(home, KAGEROW_HOME, "plugin")
				.normalize()
				.toAbsolutePath();
	}

	/**
	 * アプリケーションのキャッシュファイルパスを生成します
	 * @return 生成されたパス
	 */
	public static Path createCacheDirPath() {
		String home = getBasePath();
		return Paths.get(home, KAGEROW_HOME, "cache")
				.normalize()
				.toAbsolutePath();
	}

	/**
	 * アプリケーションのログファイルパスを生成します
	 * @return 生成されたパス
	 */
	public static Path createLogDirPath() {
		String dir = VMOption.APP_LOGSDIR.getVMoption("./logs");
		String home = getBasePath();
		return Paths.get(home, dir)
				.normalize()
				.toAbsolutePath();
	}

	/**
	 * アプリケーションのテンポラリーファイルパスを生成します
	 * @return 生成されたパス
	 */
	public static Path createTemporaryDirPath() {
		String tmpPath = VMOption.APP_IO_TMPDIR.getVMoption();
		String home = getBasePath();
		return Paths.get(home, tmpPath);
	}

	/**
	 * アプリケーションのランタイムファイルパスを生成します
	 * @return 生成されたパス
	 */
	public static Path createRuntimeDirPath() {
		String home = getBasePath();
		return Paths.get(home, KAGEROW_HOME, "runtime")
				.normalize()
				.toAbsolutePath();
	}

	/**
	 * アプリケーションに必要な設定ファイル向けのパスを生成します
	 * @return 生成されたパス
	 */
	public static Path createSettingDirPath() {
		String home = getBasePath();
		return Paths.get(home, KAGEROW_HOME, "setting")
				.normalize()
				.toAbsolutePath();
	}

	/**
	 * アプリケーションが管理しているホームディレクトリのパスを生成します
	 * @return 生成されたパス
	 */
	public static Path createKagerowHomePath() {
		String home = getBasePath();
		return Paths.get(home, KAGEROW_HOME)
				.normalize()
				.toAbsolutePath();
	}

}

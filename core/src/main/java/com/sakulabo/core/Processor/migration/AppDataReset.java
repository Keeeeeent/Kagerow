package com.sakulabo.core.Processor.migration;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;

import com.sakulabo.core.Common.AppPathUtils;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;

/**
 * Kagerowアプリケーション専用データ消去クラスです
 *
 * @author keeeeeent
 */
public final class AppDataReset {

	/** Kagerow管理ディレクトリパス */
	protected static final Path KAGEROW_HOME = AppPathUtils.createKagerowHomePath();

	/**
	 * データダンプ実装内部クラス
	 */
	private static class Reseter implements FileVisitor<Path> {

		/** 実行失敗フラグ */
		private boolean isFailed;

		/** {@inheritDoc} */
		@Override
		public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
			if (dir.endsWith("logs")) {
				return FileVisitResult.SKIP_SUBTREE;
			}
			return FileVisitResult.CONTINUE;
		}

		/** {@inheritDoc} */
		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
			if (!Paths.get("app.lock").equals(file.getFileName())) {
				Files.delete(file);
			}
			return FileVisitResult.CONTINUE;
		}

		/** {@inheritDoc} */
		@Override
		public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
			KagerowLogger.newAppLogger().err(exc);
			isFailed = true;
			return FileVisitResult.CONTINUE;
		}

		/** {@inheritDoc} */
		@Override
		public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
			if (!(dir.endsWith("logs") || dir.endsWith(".kagerow"))) {
				Files.delete(dir);
			}
			return FileVisitResult.CONTINUE;
		}

	}

	/**
	 * アプリケーションのデータをリセットします
	 *
	 * @throws IOException データリセット失敗
	 */
	public void resetApplication() throws IOException {
		Reseter reseter = new Reseter();
		Files.walkFileTree(KAGEROW_HOME, reseter);
		if (reseter.isFailed) {
			throw new IOException("Data Reset Failed");
		}
	}

}

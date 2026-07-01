package com.sakulabo.launcher.Initer.Impl;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;

import com.sakulabo.core.Common.AppPathUtils;
import com.sakulabo.core.Common.VMOption;
import com.sakulabo.launcher.Initer.InitProcessFailedException;
import com.sakulabo.launcher.Initer.InitProcessFailedException.FailType;
import com.sakulabo.launcher.Initer.InitProcessor;
import com.sakulabo.launcher.Initer.Initer;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * 一時フォルダをクリーンアップする初期化プロセッサー実装クラスです
 * @author keeeeeent
 */
public final class TmpFolderCleaner implements InitProcessor, FileVisitor<Path> {

	/** 試行回数カウンター */
	private static volatile int count;
	/** クリーンアップバイト数 */
	private long size;

	/** メッセージキー */
	private final static String MAX_COUNT = "max-count";

	/** 一時ファイルパス */
	private String tmpPath;

	/** {@inheritDoc} */
	@Override
	public void init() throws InitProcessFailedException {
		// アプリケーション管理の一時フォルダ取得
		tmpPath = VMOption.APP_IO_TMPDIR.getVMoption();
		String userHome = AppPathUtils.getBasePath();
		// システム管理の一時フォルダ設定上書き
		tmpPath = String.join("/", userHome, tmpPath);
		VMOption.JAVA_IO_TMPDIR.setVMoption(tmpPath);
		// クリーンアップ試行回数カウントアップ
		synchronized (getClass()) {
			// 最大2回まではクリーンアップ試行
			if (2 < count++) {
				throw new InitProcessFailedException(Initer.createMesssage(Initer.PREFIX, MAX_COUNT),
						FailType.Continue);
			}
		}
		try {
			// 一時ファイルクリーンアップ処理実行
			Files.walkFileTree(Paths.get(tmpPath), this);
		} catch (IOException e) {
			// IOExceptionの場合は処理か処理をリトライ
			throw new InitProcessFailedException(e, FailType.Continue);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void endLod(Method method, Object[] args, Object result) {
		System.out.println(String.format("%d Byte CleanUp", size));
	}

	/** {@inheritDoc} */
	@Override
	public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
		return FileVisitResult.CONTINUE;
	}

	/** {@inheritDoc} */
	@Override
	@SuppressFBWarnings("AT_NONATOMIC_OPERATIONS_ON_SHARED_VARIABLE")
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
		long size = Files.size(file);
		if (Files.deleteIfExists(file)) {
			this.size += size;
		}
		return FileVisitResult.CONTINUE;
	}

	/** {@inheritDoc} */
	@Override
	public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
		// 失敗した場合、後続処理を続行
		return FileVisitResult.CONTINUE;
	}

	/** {@inheritDoc} */
	@Override
	public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
		if (!Paths.get(tmpPath).toAbsolutePath().equals(dir.toAbsolutePath())) {
			Files.delete(dir);
		}
		return FileVisitResult.CONTINUE;
	}

}

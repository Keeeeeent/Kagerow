package com.sakulabo.launcher.Initer.Impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.sakulabo.core.Common.AppPathUtils;
import com.sakulabo.core.Common.VMOption;
import com.sakulabo.launcher.Initer.InitProcessFailedException;
import com.sakulabo.launcher.Initer.InitProcessFailedException.FailType;
import com.sakulabo.launcher.Initer.InitProcessor;

/**
 * アーカイブデータのクリーンアップを実行する初期化プロセッサー実装クラスです
 * @author keeeeeent
 */
public final class ArchiveDataCleanner implements InitProcessor, FileVisitor<Path> {

	/** ファイルパス */
	private String archivePath;
	/** クリーンアップ前のデータ */
	private long before;
	/** クリーンアップ後のデータ */
	private long after;

	/** {@inheritDoc} */
	@Override
	public void init() throws InitProcessFailedException {
		// アプリケーション管理の一時フォルダ取得
		archivePath = VMOption.APP_IO_ARCHIVEDATADIR.getVMoption();
		String userHome = AppPathUtils.getBasePath();
		try {
			// 一時ファイルクリーンアップ処理実行
			Files.walkFileTree(Paths.get(userHome, archivePath), this);
		} catch (IOException e) {
			// IOExceptionの場合は処理か処理をリトライ
			throw new InitProcessFailedException(e, FailType.Continue);
		}
	}

	/** {@inheritDoc} */
	@Override
	public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
		// バックアップファイルのみ抽出
		List<Path> files = Files.list(dir)
				.filter(f -> f.getFileName().toString().endsWith(".tmp"))
				.toList();
		for (Path file : files) {
			Files.delete(file);
		}
		return FileVisitResult.CONTINUE;
	}

	/** {@inheritDoc} */
	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
		// zipファイルを対象とする
		if (!Objects.toString(file.getFileName()).endsWith(".zip")) {
			// lockファイルの有効性チェック
			if (Objects.toString(file.getFileName()).endsWith(".lock")) {
				// 元zipファイルパスを生成
				Path filename = file.getFileName();
				if (Objects.nonNull(filename)) {
					String name = filename.toString();
					name = name.substring(0, name.length() - ".lock".length());
					Path dir = file.getParent();
					// 元zipファイルパスが存在しない場合、削除対象のlockファイルとする
					if (Objects.nonNull(dir) && Files.notExists(dir.resolve(name))) {
						Files.delete(file);
					}
				}
			}
			return FileVisitResult.CONTINUE;
		}
		// ファイルのバックアップを取得
		String filename = Objects.toString(file.getFileName(), timeStamp());
		filename = filename.concat(".tmp");
		Path backup = null;
		if ((backup = file.getParent()) != null) {
			backup = backup.resolve(filename);
		} else {
			backup = file.resolve(filename);
		}
		Files.move(file, backup);
		try (
				// アーカイブクリーンアップ対象
				InputStream input = Files.newInputStream(backup);
				ZipInputStream basezip = new ZipInputStream(input);
				// クリーンアップ先
				OutputStream output = Files.newOutputStream(file);
				ZipOutputStream zip = new ZipOutputStream(output)) {
			// 1024バイドずつクリーン
			byte[] buffer = new byte[1024];
			int count = -1;
			// エントリー取得
			ZipEntry entry = null;
			// エントリー分クリーンアップ
			while ((entry = basezip.getNextEntry()) != null) {
				zip.putNextEntry(entry);
				while ((count = basezip.read(buffer)) != -1) {
					zip.write(buffer, 0, count);
				}
				zip.closeEntry();
			}
			before += Files.size(backup);
			after += Files.size(file);
		} catch (IOException e) {
			Files.delete(file);
			Files.move(backup, file);
			throw e;
		}
		return FileVisitResult.CONTINUE;
	}

	/** {@inheritDoc} */
	@Override
	public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
		return FileVisitResult.CONTINUE;
	}

	/** {@inheritDoc} */
	@Override
	public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
		// バックアップファイルのみ抽出
		List<Path> files = Files.list(dir)
				.filter(f -> f.getFileName().toString().endsWith(".tmp"))
				.toList();
		for (Path file : files) {
			Files.delete(file);
		}
		return FileVisitResult.CONTINUE;
	}

	/** {@inheritDoc} */
	@Override
	public void endLod(Method method, Object[] args, Object result) {
		System.out.println(String.format("%d Byte CleanUp", before - after));
	}

	/**
	 * タイムスタンプを生成します
	 * @return タイムスタンプの文字列表現
	 */
	private String timeStamp() {
		return String.valueOf(Instant.now().toEpochMilli());
	}

}

package com.sakulabo.core.Processor.migration;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.sakulabo.core.Common.AppPathUtils;
import com.sakulabo.core.Common.StringUtils;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;
import com.sakulabo.core.Processor.log.AppLogMessage;

/**
 * Kagerowアプリケーション専用データ移行基底クラスです
 * 
 * @author keeeeeent
 */
abstract class AppDataDump {

	/** Kagerow管理ディレクトリパス */
	protected static final Path KAGEROW_HOME = AppPathUtils.createKagerowHomePath();
	/** バックアップ対象ディレクトリ名称リスト */
	protected static final List<String> BACKUP_TARGET_LIST = List.of(
			"cache",
			"database",
			"plugin",
			"runtime",
			"setting");

	/** バックアップ圧縮レベル（50%） */
	protected static final int COMPRESSION_LEVEL = 3;
	/** バッファサイズ */
	protected static final int BUFFER_ZISE = 1024;

	/**
	 * データダンプ実装f内部クラス
	 */
	private static class Dumper implements FileVisitor<Path> {

		/** 出力先 */
		ZipOutputStream zoutput;

		/**
		 * デフォルトコンストラクタ
		 * @param zoutput 出力先
		 */
		Dumper(ZipOutputStream zoutput) {
			this.zoutput = zoutput;
		}

		/** {@inheritDoc} */
		@Override
		public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
			return FileVisitResult.CONTINUE;
		}

		/** {@inheritDoc} */
		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

			// エントリ作成
			String entryName = KAGEROW_HOME.relativize(file).toString();
			ZipEntry entry = new ZipEntry(entryName);

			try (RandomAccessFile accessFile = new RandomAccessFile(file.toFile(), StringUtils.RAND_READ_ONLY)) {

				// ファイルディスクリプター取得
				FileDescriptor fileDescriptor = accessFile.getFD();

				// 圧縮方式の設定
				if (entryName.endsWith(".zip")) {

					// ストリーム生成 ※この後再利用のため明示的クローズ
					@SuppressWarnings("resource")
					InputStream input = new FileInputStream(fileDescriptor);
					try {

						// CRC32初期化
						CRC32 crc32 = new CRC32();
						byte[] buffer = new byte[BUFFER_ZISE];
						int count = -1;
						while ((count = input.read(buffer)) != -1) {
							// バイトデータ書き込み
							crc32.update(buffer, 0, count);
						}
						// CRC32設定
						entry.setCrc(crc32.getValue());

						// サイズ設定
						long size = Files.size(file);
						entry.setSize(size);
						entry.setCompressedSize(size);

						// メソッド設定（非圧縮）
						zoutput.setMethod(ZipEntry.STORED);

					} catch (Exception e) {
						input.close();
						throw e;
					}

				} else {
					zoutput.setMethod(ZipEntry.DEFLATED);
				}

				// エントリ追加
				zoutput.putNextEntry(entry);

				// ディスクリプターを先頭に戻す
				accessFile.seek(0);

				// データ書き込み
				try (InputStream input = new FileInputStream(fileDescriptor)) {
					input.transferTo(zoutput);
				}

			}

			// エントリクローズ
			zoutput.closeEntry();

			return FileVisitResult.CONTINUE;
		}

		/** {@inheritDoc} */
		@Override
		public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
			return FileVisitResult.TERMINATE;
		}

		/** {@inheritDoc} */
		@Override
		public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
			return FileVisitResult.CONTINUE;
		}

	}

	/**
	 * ダンプデータのエクスポートを実行します
	 * @param path エクスポート先ファイルパス
	 * @throws IOException ダンプデータ出力失敗
	 */
	public final void exportDump(Path path) throws IOException {

		// データバックアップ先初期化
		try (
				OutputStream output = createOutputStream(path);
				ZipOutputStream zoutput = new ZipOutputStream(output)) {
			// 圧縮率設定
			zoutput.setLevel(COMPRESSION_LEVEL);
			// バックアップ対象取得
			List<Path> backupTarget = Files.list(KAGEROW_HOME).filter(this::findTarget).toList();
			// バックアップ出力
			Dumper dumper = new Dumper(zoutput);
			for (Path backup : backupTarget) {
				Files.walkFileTree(backup, dumper);
			}
		} catch (Exception e) {
			throw new IOException(e);
		}

	}

	/**
	 * バックアップ対象のディレクトリか判定します
	 * @param path 対象パス
	 * @return 判定結果
	 */
	private boolean findTarget(Path path) {
		if (!Files.isDirectory(path)) {
			return false;
		}
		String targetName = path.getName(path.getNameCount() - 1).toString();
		return BACKUP_TARGET_LIST.contains(targetName);
	}

	/**
	 * データエクスポート時の出力ストリームを生成します
	 * @param path 出力先パス
	 * @return 出力ストリーム
	 * @throws Exception 出力ストリーム生成失敗
	 */
	protected abstract OutputStream createOutputStream(Path path) throws Exception;

	/**
	 * ダンプデータのインポートを実行します
	 * @param dump ダンプデータファイルパス
	 * @throws IOException ダンプデータ取り込み失敗
	 */
	public final void importDump(Path dump) throws IOException {
		// バックアップ展開
		try (InputStream input = createInputStream(dump);
				ZipInputStream zinput = new ZipInputStream(input)) {
			// エントリーを1つづつ取得
			ZipEntry entry;
			while ((entry = zinput.getNextEntry()) != null) {
				// エントリーの名称を取得
				String name = entry.getName();
				if (checkTarget(name)) {
					// 展開先パスに変換
					Path path = KAGEROW_HOME.resolve(name);
					// 前提となるディレクトリが存在するか確認
					Path dir = path.getParent();
					if (Objects.nonNull(dir) && Files.notExists(path)) {
						// 対象が存在しない場合ディレクトリを生成
						Files.createDirectories(dir);
					}
					// データ解凍
					try (OutputStream output = Files.newOutputStream(path)) {
						zinput.transferTo(output);
					}
				} else {
					KagerowLogger.newAppLogger().log(Level.WARNING, AppLogMessage.WARNING_MSG9001.name(),
							new Object[] { name });
				}
			}
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	/**
	 * 復元対象のエントリか判定します
	 * @param entry エントリー
	 * @return 判定結果
	 */
	private boolean checkTarget(String entry) {
		String targetName = entry.split(StringUtils.SLASH_DELIMIT)[0];
		return BACKUP_TARGET_LIST.contains(targetName);
	}

	/**
	 * データエクスポート時の出力ストリームを生成します
	 * @param path 出力先パス
	 * @return 出力ストリーム
	 * @throws Exception 出力ストリーム生成失敗
	 */
	protected abstract InputStream createInputStream(Path path) throws Exception;

}

package com.sakulabo.core.Processor.transaction;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.ref.Cleaner;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import com.sakulabo.core.Common.AppPathUtils;
import com.sakulabo.core.Common.StringUtils;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;
import com.sakulabo.core.Kagerow.Utilities.KagerowTransaction;
import com.sakulabo.core.Provides.ArchiveSystemProvider;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * 仮想ファイルシステムのトランザクション管理をするクラスです 
 */
@SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
public final class FileTransaction implements KagerowTransaction {

	/** ロック共有メモリ */
	private static final Map<URI, WeakReference<ReentrantLock>> LOCK = new ConcurrentHashMap<>();
	/** メモリクリーナー */
	private static final Cleaner CLEANER = Cleaner.create();

	/** 自分自身のロック */
	private final ReentrantLock reentrantLock;
	/** ロールバックのUNDOファイルパス */
	Path backup = null;
	/** 仮想ファイル基底ファイルパス */
	Path zipFilePath;
	/** 仮想ファイルシステム本体 */
	FileSystem target;
	/** 仮想ファイルシステムファイルチャネル */
	FileChannel channel;
	/** 仮想ファイルシステムロックインスタンス */
	FileLock lock;
	/** ロックファイルパス */
	Path lockFile;
	/** トランザクション終了フラグ */
	volatile boolean endFlg;

	/**
	 * デフォルトコンストラクタ
	 * @param zipFilePath 仮想ファイル基底ファイルパス
	 * @param uri 仮想ファイル基底URIパス
	 */
	public FileTransaction(Path zipFilePath, final URI uri) {

		// トランザクション対象のファイルパスを設定
		this.zipFilePath = zipFilePath;
		// ファイルシステム存在確認
		target = ArchiveSystemProvider.cache.get(zipFilePath);
		if (Objects.isNull(target)) {
			// ファイルシステムが存在しない場合、新規で生成し本インスタンスで管理
			target = Paths.get(uri).getFileSystem();
		}

		// 共通ロックを生成
		ReentrantLock prelock = new ReentrantLock();
		// 共有ロックの強参照を取得
		@SuppressWarnings("unused")
		ReentrantLock refLock = null;
		if (Objects.nonNull(LOCK.get(uri))) {
			refLock = LOCK.get(uri).get();
		}
		// 共有ロック追加
		WeakReference<ReentrantLock> ref = LOCK.compute(uri, (_, v) -> {
			ReentrantLock tmpLock = Objects.nonNull(v) ? v.get() : null;
			// ロックが生きていない、もしくは未生成の場合新たなロックを生成
			if (Objects.isNull(tmpLock)) {
				// ロックを生成
				tmpLock = prelock;
				// クリーナー設定
				CLEANER.register(this, () -> LOCK.remove(uri));
			} else {
				// 既に存在する場合そのままリターン
				return v;
			}
			// マップに追加
			return new WeakReference<ReentrantLock>(tmpLock);
		});
		// フィールドにセット
		reentrantLock = ref.get();
		// ロック取得
		reentrantLock.lock();

		try {

			// ロックファイルパス生成
			Path lockFile = zipFilePath.resolveSibling(zipFilePath.getFileName() + ".lock");
			this.lockFile = lockFile;

			// ファイルチャネルロック取得
			channel = FileChannel.open(lockFile,
					StandardOpenOption.READ,
					StandardOpenOption.WRITE,
					StandardOpenOption.CREATE);
			// ロック取得
			lock = channel.lock();

			// バックアップパス生成
			long timestamp = Instant.now().toEpochMilli();
			StringBuilder tmpFileName = new StringBuilder();
			tmpFileName.append(ArchiveSystemProvider.SCHOME)
					.append(StringUtils.UNDERSCORE)
					.append(timestamp)
					.append(AppPathUtils.TMP_FILE_EXT);
			// バックアップファイル生成
			backup = AppPathUtils.createTemporaryDirPath().resolve(tmpFileName.toString());
			// データバックアップ
			Files.copy(zipFilePath, backup);

		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void commit() throws IOException {
		// 既にトランザクションが終了しているか確認
		if (endFlg) {
			throw new IOException("This transaction has already ended");
		}
		try {
			// トランザクション確定
			this.close();
			// バックアップファイル削除
			Files.delete(backup);
		} catch (Exception e) {
			throw new IOException(e);
		} finally {
			// トランザクション終了フラグ設定
			endFlg = true;
		}
	}

	/** {@inheritDoc} */
	@Override
	public void rollback(Throwable e) {
		// 既にトランザクションが終了しているか確認
		if (endFlg) {
			IOException ioe = new IOException("This transaction has already ended", e);
			throw new UncheckedIOException(ioe);
		}
		try {
			// トランザクション確定
			this.close();
			// バックアップファイルへ切り戻し
			Files.move(backup, zipFilePath,
					StandardCopyOption.REPLACE_EXISTING,
					StandardCopyOption.ATOMIC_MOVE);
		} catch (Exception ioe) {
			e.addSuppressed(ioe);
		} finally {
			// トランザクション終了フラグ設定
			endFlg = true;
		}
	}

	/** {@inheritDoc} */
	@Override
	public void close() throws IOException {

		// ファイルシステムキャッシュ削除
		ArchiveSystemProvider.cache.remove(zipFilePath);

		// ロックの存在確認と有効性確認を実施
		if (Objects.nonNull(lock) && lock.isValid()) {
			try {
				// まだロック有効の場合解放する
				lock.release();
			} catch (IOException e) {
				KagerowLogger.newAppLogger().err(e);
			}
		}

		// チャネル存在確認とクローズ済みか確認
		if (Objects.nonNull(channel) && channel.isOpen()) {
			try {
				// まだクローズ処理がされていない場合、クローズする
				channel.close();
			} catch (IOException e) {
				KagerowLogger.newAppLogger().err(e);
			}
		}

		// ファイルシステムの存在確認とクローズ済みか確認
		if (Objects.nonNull(target) && target.isOpen()) {
			try {
				// まだクローズ処理がされていない場合、クローズする
				target.close();
			} catch (IOException e) {
				KagerowLogger.newAppLogger().err(e);
			} finally {
				// 場合によってはネイティブメモリにファイルキャッシュが残ってしまう
				// 防止策として強制的にGCの要求をJVMへ行う
				// ここまでの処理でチェネルやファイルシステムが到達不可能な状態にしておく必要がある
				try {
					// GC要求
					System.gc();
				} catch (Throwable e) {
					KagerowLogger.newAppLogger().err(e);
				}
			}
		}

		// ロックファイル存在確認と削除すみか確認
		if (Objects.nonNull(lockFile)) {
			try {
				// まだ削除されていない場合、削除を行う
				if (Files.exists(lockFile)) {
					Files.delete(lockFile);
				}
			} catch (IOException e) {
				KagerowLogger.newAppLogger().err(e);
			}
		}

		// 共通ロックが解放されているか確認
		if (reentrantLock.isLocked()) {
			reentrantLock.unlock();
		}

	}

}
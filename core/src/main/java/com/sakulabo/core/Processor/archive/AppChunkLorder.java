package com.sakulabo.core.Processor.archive;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;

import com.sakulabo.core.Common.ApplicationWordDictionary;
import com.sakulabo.core.Common.DataSize;
import com.sakulabo.core.Common.ErrorMessage;
import com.sakulabo.core.Common.ThreadUtils;
import com.sakulabo.core.Kagerow.KagerowApplication;
import com.sakulabo.core.Kagerow.Contents.KagerowVirtualFileContent.KagerowVirtualFileObject;
import com.sakulabo.core.Kagerow.Exception.AppLogicException;
import com.sakulabo.core.Kagerow.Utilities.KagerowDBMode;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;
import com.sakulabo.core.Processor.database.DDLConnectionHandler;
import com.sakulabo.core.Processor.database.DMLConnectionHandler;

/**
 * 指定された範囲のデータを、アーカイブファイル群をから読み込みを行う機能を提供する規定クラスです
 * 
 * @author keeeeeent
 * @param <T> 
 */
abstract class AppChunkLorder<T extends KagerowVirtualFileObject> {

	/** KDBファイル出力先 */
	protected final Path path;
	/** KDBスレッドプール */
	protected static final ExecutorService KDB_THREAD_POOL;
	static {
		// インスタンス生成
		KDB_THREAD_POOL = Executors.newFixedThreadPool(
				KagerowApplication.getConfig().KDB_THREAD_POOL_SIZE(),
				ThreadUtils.newDemonThreadFactory(ThreadUtils.CHUNKL_ORDER_GROUP));
		// シャットダウンフック登録
		ThreadUtils.addShutdownHook(KDB_THREAD_POOL);
	}

	/** ロードモード */
	protected KagerowDBMode mode;

	/**
	 * 実行計画を処理する内部クラスです
	 */
	protected class execObject implements Callable<Void> {

		/** チャンク読み取り機構 */
		final ChunkReader reader;
		/** 物理ファイルパス */
		final Path path;
		/** データ構造体 */
		final DataSet dataSet;

		/**
		 * デフォルトコンストラクタ
		 * @param reader リーダインスタンス
		 * @param path 物理ファルパス
		 * @param dataSet データ構造体
		 */
		execObject(ChunkReader reader, Path path, DataSet dataSet) {
			this.reader = reader;
			this.path = path;
			this.dataSet = dataSet;
		}

		/** {@inheritDoc} */
		@Override
		public Void call() throws Exception {

			// DML実行
			try (execObject.this.reader;
					DMLConnectionHandler conn = DMLConnectionHandler.autoSelectionDMLConnectionHandler(
							mode, dataSet, path)) {

				// デシリアライズデータ
				List<String[]> line;

				// デシリアライズデータロード
				while ((line = execObject.this.reader.readData()) != null) {
					for (String[] dat : line) {
						// データ登録
						conn.transaction(dat);
					}
				}
			}

			return null;
		}

	}

	/**
	 * デフォルトコンストラクタ
	 * @param mode ロードモード
	 * @param path KDBファイル出力先
	 */
	protected AppChunkLorder(KagerowDBMode mode, Path path) {
		this.mode = mode;
		this.path = path;
	}

	/**
	 * チャンクリーダーを生成します
	 * @param chunk 読み込みチャンク
	 * @param start 開始位置
	 * @param end 終了位置
	 * @param max ファイル終端
	 * @return チャンクリーダー
	 * @throws IOException チャンクリーダー生成失敗
	 * @throws AppLogicException コンテキスト取得失敗
	 */
	protected abstract ChunkReader createChunkReader(T chunk, long start, long end, long max)
			throws AppLogicException, IOException;

	/**
	 * チャンクからKDBを生成します
	 * @param chunk チャンクファイル
	 * @throws SQLException 初期化SQL実行失敗
	 * @throws IOException チャンクリーダー生成失敗
	 * @throws AppLogicException コンテキスト取得失敗
	 */
	public void lord(T chunk) throws AppLogicException, IOException, SQLException {

		// 処理分割数計算
		final int threadCount = canSeparate(chunk.datSize());

		// 実行計画生成
		final long idxUnitSize = chunk.indexCount();
		// 小数点以下切り捨てでざっくり計算
		final long idxUnitCount = idxUnitSize / threadCount;

		// 位置情報
		final long unit = idxUnitCount * chunk.idxBlockSize().longValue();
		final long max = chunk.datSize().longValue();
		long start = 0;
		long end = unit;

		// データセットに変換
		final DataSet dataSet = new DataSet(
				chunk.schema(),
				chunk.binaryName(),
				chunk.synonym(),
				chunk.headerData(),
				chunk.dataType(),
				chunk.dataSize(),
				Collections.emptyList());

		// DDL実行
		try (DDLConnectionHandler ddlHandler = DDLConnectionHandler.autoSelectionDDLConnectionHandler(
				mode, dataSet, path)) {
			ddlHandler.transaction();
		}

		// KDBデータ投入準備
		final List<execObject> execList = new ArrayList<>();
		for (int i = threadCount; 0 < i; i--) {

			// Reader生成
			ChunkReader reader = createChunkReader(chunk, start, end, max);
			execList.add(new execObject(reader, path, dataSet));
			// 位置情報更新
			start = end;
			end += unit;

			// 最後の実行計画はあまりが発生する可能性があるので、あまりも含め計算できるよう考慮する
			long eof = end + unit;
			if (chunk.idxSize().longValue() < eof) {
				end = chunk.idxSize().longValue();
			}

		}

		// ロード実行
		BlockingQueue<Future<Void>> queue = new LinkedBlockingDeque<>();
		CompletionService<Void> completionService = new ExecutorCompletionService<>(KDB_THREAD_POOL, queue);
		execList.forEach(completionService::submit);

		// ロード実行処理待機
		boolean failFlug = false;
		AppLogicException exception = new AppLogicException(ErrorMessage.CODE_004.getMessage(
				ApplicationWordDictionary.WCD_0001.getMessage()));
		for (int i = 0; i < execList.size(); i++) {
			try {
				Future<Void> future = completionService.take();
				future.get();
			} catch (ExecutionException | InterruptedException e) {
				exception.addSuppressed(e);
				KagerowLogger.newAppLogger().err(e);
				failFlug = true;
			}
		}

		// 結果取得
		if (failFlug) {
			throw exception;
		}

	}

	/**
	 * 処理に必要なスレッド数を計算します
	 * @param size データサイズ
	 * @return スレッド必要数
	 */
	public int canSeparate(BigInteger size) {

		// メガバイト単位に変換
		int unitSize = DataSize.MB.toUnitSize(size).intValue();
		// 使用可能なCPU数を取得
		int cpu = Runtime.getRuntime().availableProcessors();
		// 返却変数初期化
		int result = 1;

		// CPUコア数から最大の2の累乗を計算（ビットを分ける際に安全に計算ができるよう調整）
		// ここでは対数の性質を利用しlog2底のCPU数の計算結果を切り捨てした分だけビッドシフトさせる
		cpu = 1 << (int) (Math.log(cpu) / Math.log(2));
		// 結果が1以下の場合、1とする（シングルコアの対応）
		cpu = 1 < cpu ? cpu : 1;

		if (unitSize < 1) {
			// 100MB以下はシングルコアで処理
			;
		} else if (unitSize < 5) {
			// 1MB以上-5MB以下はダブルコアで処理
			result = Math.min(cpu, 2);
		} else if (unitSize < 20) {
			// 5MB以上-20MB以下はクアッドコアで処理
			result = Math.min(cpu, 4);
		} else if (unitSize < 100) {
			// 20MB以上-100MB以下はオクタコアで処理
			result = Math.min(cpu, 8);
		} else {
			// 上記以上の巨大サイズファイルは最大性能で処理
			result = cpu;
		}

		return result;
	}

}

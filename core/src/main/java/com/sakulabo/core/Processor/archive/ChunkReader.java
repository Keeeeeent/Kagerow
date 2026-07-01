package com.sakulabo.core.Processor.archive;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URI;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.List;

import com.sakulabo.core.Common.StringUtils;
import com.sakulabo.core.Kagerow.Contents.KagerowVirtualFileContent.KagerowVirtualFileObject;

/**
 * チャンクファイルを読み込みます
 * 
 * @author keeeeeent
 */
abstract class ChunkReader implements AutoCloseable {

	/** 読み込み開始位置 */
	protected final long start;
	/** 読み込み終了位置 */
	protected final long end;
	/** チャンクファイル */
	protected final KagerowVirtualFileObject chunk;
	/** データファイル */
	protected final RandomAccessFile datFile;
	/** インデックスファイルチャネル */
	protected final FileChannel idxChannel;
	/** インデックスファイル */
	protected final MappedByteBuffer idxFile;

	/**
	 * デフォルトコンストラクタ
	 * 
	 * @param chunk チャンクインスタンス
	 * @param start 開始位置
	 * @param end 終了位置
	 * @param max ファイル終端
	 * @throws IOException チャンクファイル不正
	 */
	ChunkReader(KagerowVirtualFileObject chunk, long start, long end, long max) throws IOException {
		this.chunk = chunk;
		this.start = start;
		this.end = max < end ? max : end;
		{
			URI uri = URI.create(chunk.datAddr());
			File file = Paths.get(uri).toFile();
			this.datFile = new RandomAccessFile(file, StringUtils.RAND_READ_ONLY);
		}
		{
			URI uri = URI.create(chunk.idxAddr());
			Path path = Paths.get(uri);
			this.idxChannel = FileChannel.open(path, StandardOpenOption.READ);
			this.idxFile = this.idxChannel.map(MapMode.READ_ONLY, start, this.end - start);
		}
	}

	/**
	 * データ読み取り処理を実行します
	 * @param binData 読み取り対象データ
	 * @param datFile データファイルアクセッサー
	 * @return 読み取り結果
	 * @throws Exception 読み取り失敗
	 */
	protected abstract List<String[]> read(byte[] binData, RandomAccessFile datFile) throws Exception;

	/**
	 * データの読み取りを行います<br/>
	 * データ読み取り位置が末端に達している場合、nullを返却します<br/>
	 * データはデータファイル内部のチャンク単位で読み取りされます
	 * 
	 * @return データ
	 * @throws IOException チャンクファイル不正
	 */
	final List<String[]> readData() throws IOException {

		// 読み取り完了の場合
		if (!idxFile.hasRemaining()) {
			return null;
		}

		// 返却用変数初期化
		List<String[]> result = Collections.emptyList();

		// データファイルインデックス取得
		long datIndex = idxFile.getLong();

		// データファイル読み取り位置へシーク
		datFile.seek(datIndex);

		// 読み取りデータ範囲を取得
		int dataSize = datFile.readInt();

		// データ本体を読み取り
		byte[] binData = new byte[dataSize];
		datFile.readFully(binData);

		try {
			result = read(binData, datFile);
		} catch (Exception e) {
			throw new IOException(e);
		}

		return result;
	}

	/** {@inheritDoc} */
	@Override
	public void close() throws IOException {
		datFile.close();
		idxChannel.close();
	}
}

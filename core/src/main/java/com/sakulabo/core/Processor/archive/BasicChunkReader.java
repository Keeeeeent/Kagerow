package com.sakulabo.core.Processor.archive;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.RandomAccessFile;
import java.util.Collections;
import java.util.List;

import com.sakulabo.core.Kagerow.Contents.KagerowVirtualFileContent.KagerowVirtualFileObject.BasicFileObject;

/**
 * チャンクファイルを読み込みます
 * 
 * @author keeeeeent
 */
class BasicChunkReader extends ChunkReader {

	/**
	 * デフォルトコンストラクタ
	 * 
	 * @param chunk チャンクインスタンス
	 * @param start 開始位置
	 * @param end 終了位置
	 * @param max ファイル終端
	 * @throws IOException チャンクファイル不正
	 */
	BasicChunkReader(BasicFileObject chunk, long start, long end, long max) throws IOException {
		super(chunk, start, end, max);
	}

	/** {@inheritDoc} */
	@Override
	@SuppressWarnings("unchecked")
	protected List<String[]> read(byte[] binData, RandomAccessFile datFile) throws IOException {

		List<String[]> result = Collections.emptyList();

		// データデシリアライズ
		try (InputStream input = new ByteArrayInputStream(binData);
				ObjectInputStream oinput = new ObjectInputStream(input);) {
			try {
				result = (List<String[]>) oinput.readObject();
			} catch (ClassNotFoundException e) {
				// このタイミングで読み込みクラスが見つからない場合は、IOExceptionとして翻訳
				// ファイル不正と同等の扱い
				throw new IOException(e);
			}
		}

		return result;
	}

}

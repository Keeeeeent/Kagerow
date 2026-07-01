package com.sakulabo.core.Processor.archive;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.security.MessageDigest;

import com.sakulabo.core.Kagerow.Exception.AppLogicException;

/**
 * データからチャンクファイルを生成します
 * 
 * @author keeeeeent
 */
class BasicChunkWriter extends ChunkWriter {

	/**
	 * デフォルトコンストラクタ
	 * @param digest ハッシュ関数
	 * @param charset 文字コード
	 * @param schema スキーマ
	 * @throws AppLogicException チャンクファイル不正
	 * @throws IOException 出力不可の場合
	 */
	BasicChunkWriter(MessageDigest digest, Charset charset, String schema) throws AppLogicException, IOException {
		super(digest, charset, schema);
	}

	/** {@inheritDoc} */
	@Override
	protected void write(RandomAccessFile dataAccess, int dataSize, byte[] byteData) throws IOException {

		// データサイズ書き込み
		dataAccess.writeInt(dataSize);
		// データ書き込み
		dataAccess.write(byteData);

	}

}

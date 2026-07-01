package com.sakulabo.core.Processor.archive;

import java.io.IOException;
import java.nio.file.Path;

import com.sakulabo.core.Kagerow.Contents.KagerowVirtualFileContent.KagerowVirtualFileObject.BasicFileObject;
import com.sakulabo.core.Kagerow.Utilities.KagerowChunkLorder;
import com.sakulabo.core.Kagerow.Utilities.KagerowDBMode;

/**
 * 指定された範囲のデータを、アーカイブファイル群をから読み込みを行う機能を提供します
 * 
 * @author keeeeeent
 */
public final class BasicChunkLorder extends AppChunkLorder<BasicFileObject>
		implements KagerowChunkLorder<BasicFileObject> {

	/**
	 * デフォルトコンストラクタ
	 * @param mode ロードモード
	 * @param path KDBファイル出力先
	 */
	public BasicChunkLorder(KagerowDBMode mode, Path path) {
		super(mode, path);
	}

	/** {@inheritDoc} */
	@Override
	protected ChunkReader createChunkReader(BasicFileObject chunk, long start, long end, long max) throws IOException {
		return new BasicChunkReader(chunk, start, end, max);
	}

}

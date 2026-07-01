package com.sakulabo.core.Processor.archive;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

import javax.naming.Name;

import com.sakulabo.core.Kagerow.Contents.KagerowVirtualFileContent.KagerowDataType;
import com.sakulabo.core.Kagerow.Contents.KagerowVirtualFileContent.KagerowVirtualFileObject.BasicFileObject;
import com.sakulabo.core.Kagerow.Exception.AppLogicException;
import com.sakulabo.core.Kagerow.Utilities.KagerowChunkCreater;
import com.sakulabo.core.Kagerow.Utilities.KagerowFileBodyReader;
import com.sakulabo.core.Kagerow.Utilities.KagerowFileHeaderReader;
import com.sakulabo.core.Kagerow.Utilities.KagerowFileReaderFactory;

/**
 * ファイルからchunk毎のアーカイブファイル群を生成します<br/>
 * バイナリファイル:物理データファイル（.dat）
 * インデックスファイル:物理データアクセスインデックスファイル（.idx）
 * 
 * @author keeeeeent
 */
public final class BasicChunkCreater extends AppChunkCreater<BasicFileObject>
		implements KagerowChunkCreater<BasicFileObject> {

	/** ファイルreader抽象ファクトリ */
	private KagerowFileReaderFactory factory;

	/**
	 * デフォルトコンストラクタ
	 * @param schema スキーマ名称
	 * @param path 入力ファイル
	 * @param charset 読み込み文字コード
	 * @param isHeader ヘッダー有無
	 * @param factory ファイルreader抽象ファクトリ
	 * @throws AppLogicException 入力ファイルが存在しない場合
	 */
	public BasicChunkCreater(String schema, Path path, Charset charset, boolean isHeader,
			KagerowFileReaderFactory factory)
			throws AppLogicException {
		super(schema, path, charset, isHeader);
		this.factory = factory;
	}

	/** {@inheritDoc} */
	@Override
	protected ChunkWriter createChunkWriter(MessageDigest digest, Charset charset, String schema)
			throws AppLogicException, IOException {
		return new BasicChunkWriter(digest, charset, schema);
	}

	/** {@inheritDoc} */
	@Override
	protected KagerowFileBodyReader createBodyReader(Path path, Charset charset, boolean isHeader) throws IOException {
		return factory.createFileBodyReader(path, charset, isHeader);
	}

	/** {@inheritDoc} */
	@Override
	protected KagerowFileHeaderReader createHeaderReader(Path path, Charset charset, boolean isHeader)
			throws IOException {
		return factory.createFileHeaderReader(path, charset, isHeader);
	}

	/** {@inheritDoc} */
	@Override
	protected BasicFileObject createKagerowVirtualFileObject(String synonym, tmpDataSet bodyData,
			FileReader hederReader, HashPathCreater pathCreater) throws IOException {

		// ヘッダー情報読取
		String[] headerData = hederReader.readLine();
		// データタイプ
		KagerowDataType[] dataType = bodyData.dataType();

		// ハッシュ更新
		Name name = pathCreater.toHashPath(headerData, dataType);
		String binaryName = toTableName(name);

		// データセット精鋭
		BasicFileObject data = new BasicFileObject(
				Instant.now(),
				headerData,
				dataType,
				bodyData.dataSize(),
				bodyData.datAddr(),
				bodyData.datSize(),
				bodyData.idxAddr(),
				bodyData.idxSize(),
				binaryName,
				synonym,
				schema,
				new AtomicReference<>());

		return data;
	}

}

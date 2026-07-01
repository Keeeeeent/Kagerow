package com.sakulabo.core.Processor.archive;

import com.sakulabo.core.Kagerow.Utilities.KagerowFileBodyReader;
import com.sakulabo.core.Kagerow.Utilities.KagerowFileHeaderReader;
import com.sakulabo.core.Kagerow.Utilities.KagerowFileReaderFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;

/**
 * TSVファイルの読み取り実装提供ファクトリクラスです
 * 
 * @author keeeeeent
 */
public class TSVFileReaderFactory extends KagerowFileReaderFactory {

	/** {@inheritDoc} 
	 * @throws IOException */
	@Override
	public KagerowFileBodyReader createFileBodyReader(Path path, Charset charset, boolean isHeader) throws IOException {
		return new TSVFileBodyReader(path, charset, isHeader);
	}

	/** {@inheritDoc} */
	@Override
	public KagerowFileHeaderReader createFileHeaderReader(Path path, Charset charset, boolean isHeader) throws IOException {
		return new TSVFileHeaderReader(path, charset, isHeader);
	}

}

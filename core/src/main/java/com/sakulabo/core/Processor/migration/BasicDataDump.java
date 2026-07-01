package com.sakulabo.core.Processor.migration;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import com.sakulabo.core.Kagerow.Utilities.KagerowDataDump;

/**
 * Kagerowアプリケーション専用データ移行クラスです
 * 
 * @author keeeeeent
 */
public final class BasicDataDump extends AppDataDump implements KagerowDataDump {

	/** {@inheritDoc} */
	@Override
	protected OutputStream createOutputStream(Path path) throws IOException {
		return Files.newOutputStream(path);
	}

	/** {@inheritDoc} */
	@Override
	protected InputStream createInputStream(Path path) throws IOException {
		return Files.newInputStream(path);
	}

}

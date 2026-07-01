package com.sakulabo.core.Provides;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.spi.FileTypeDetector;

import com.sakulabo.core.Kagerow.Utilities.KagerowScriptAccessor;

/**
 * 独自MineTypeを提供するプロバイダクラスです<br>
 * Kagerow専用スクリプトファイルのMintTyped
 * 
 * @author keeeeeent
 */
public class KagerowScriptFileTypeDetector extends FileTypeDetector {

	/** {@inheritDoc} */
	@Override
	public String probeContentType(Path path) throws IOException {
		try {
			// TODO セキュアMineTypeも対応予定
			KagerowScriptAccessor.getInstance(path);
			return KagerowScriptAccessor.KAGEROW_BASIC_MINE_TYPE;
		} catch (Exception e) {
			return null;
		}
	}

}

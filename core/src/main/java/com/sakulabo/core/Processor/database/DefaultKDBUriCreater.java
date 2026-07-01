package com.sakulabo.core.Processor.database;

import java.nio.file.Path;
import java.nio.file.Paths;

import com.sakulabo.core.Common.AppPathUtils;
import com.sakulabo.core.Kagerow.Utilities.KagerowDBMode;

/**
 * KDB接続用URL生成デフォルトクラスです
 * 
 * @author keeeeeent
 */
public class DefaultKDBUriCreater extends AppKDBUriCreater {

	/** KDB物理ファイルパス */
	private Path path;

	/**
	 * デフォルトコンストラクタ
	 * @param mode KDB起動モード
	 * @param path KDB物理ファイルパス
	 */
	public DefaultKDBUriCreater(KagerowDBMode mode, Path path) {
		super(mode);
		String tmpPath = path
				.normalize()
				.toAbsolutePath()
				.toString();
		tmpPath = tmpPath.substring(0, tmpPath.length() - AppPathUtils.KDB_FILE_EXT.length());
		this.path = Paths.get(tmpPath);
	}

	/** {@inheritDoc} */
	@Override
	String getDatabaseName() {
		return path.toString();
	}

	/** {@inheritDoc} */
	@Override
	String createMode() {
		return "file";
	}

}

package com.sakulabo.core.Kagerow.Utilities;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

import com.sakulabo.core.Processor.migration.BasicDataDump;

/**
 * Kagerowアプリケーション専用データ移行インターフェイスです
 * 
 * @author keeeeeent
 */
public sealed interface KagerowDataDump
		permits BasicDataDump {

	/** バックアップ向け拡張子 */
	public static final String BACKUP_EXT = ".kagerow";

	/**
	 * インスタンスを生成します
	 * @return ダンプインスタンス
	 */
	public static KagerowDataDump newBasicInstance() {
		return new BasicDataDump();
	}

	/**
	 * バックアップ向けの拡張子を必要に応じて付与します
	 * @param path バックアップ先
	 * @return 正規化後のパス
	 */
	public static Path toBackupPath(Path path) {
		if (!path.toString().endsWith(KagerowDataDump.BACKUP_EXT)) {
			String fileName = Objects.toString(path.getFileName());
			return path.resolveSibling(fileName.concat(".kagerow"));
		}
		return path;
	}

	/**
	 * ダンプデータのインポートを実行します
	 * @param dump ダンプデータファイルパス
	 * @throws IOException ダンプデータ取り込み失敗
	 */
	void importDump(Path dump) throws IOException;

	/**
	 * ダンプデータのエクスポートを実行します
	 * @param path エクスポート先ファイルパス
	 * @throws IOException ダンプデータ出力失敗
	 */
	void exportDump(Path path) throws IOException;

}

package com.sakulabo.core.Processor.command;

import com.sakulabo.core.Common.AppPathUtils;
import com.sakulabo.core.Common.StringUtils;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;
import com.sakulabo.core.Kagerow.Utilities.KagerowScriptAccessor.KagerowCmdAccessor;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;

/**
 * Kagerowアプリケーション専用コマンドファイル生成クラスです
 * @author keeeeeent
 */
public final class CommandTmpFileCreater {

	/** コマンドアクセッサー */
	private final KagerowCmdAccessor cmd;

	/**
	 * デフォルトコンストラクタ
	 * @param cmd 実行コマンド
	 */
	public CommandTmpFileCreater(KagerowCmdAccessor cmd) {
		this.cmd = cmd;
	}

	/**
	 * コマンドファイルを生成し、生成咲穂パスを返却します
	 * @return 生成パス
	 * @throws IOException コマンドファイル生成失敗
	 */
	public Path build() throws IOException {
		// 書き込みファイル生成
		Path dir = AppPathUtils.createTemporaryDirPath();
		Path file = Files.createTempFile(dir, StringUtils.EMPTY, cmd.getMode().getExtension());
		// ファイル構築
		try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
			// コマンドの書き込み
			writer.write(cmd.getCmd());
			// 実行権限付与
			boolean isExecutable = file.toFile().setExecutable(true);
			if (!isExecutable) {
				KagerowLogger.newAppLogger().log(Level.WARNING, "Failed to grant execution permission", new Object[0]);
			}
		}
		// パスの正規化
		file = file.toAbsolutePath().normalize();
		return file;
	}

}

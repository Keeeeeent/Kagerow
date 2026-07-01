package com.sakulabo.launcher.Initer.Impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.sakulabo.core.Common.AppPathUtils;
import com.sakulabo.core.Common.VMOption;
import com.sakulabo.launcher.Initer.InitProcessFailedException;
import com.sakulabo.launcher.Initer.InitProcessFailedException.FailType;
import com.sakulabo.launcher.Initer.InitProcessor;

/**
 * 実行環境を構築する初期化プロセッサー実装クラスです
 * @author keeeeeent
 */
public final class CreateRumtimeEnv implements InitProcessor {

	/** 構築が必要なディレクトリを参照するVMオプションのキー一覧 */
	private static final String[] ENV_ARG_LIST = new String[] {
			VMOption.APP_IO_TMPDIR.toVMOption(),
			VMOption.APP_IO_ARCHIVEDATADIR.toVMOption()
	};

	/** 構築が必要なディレクトリを表すリスト */
	private static final String[] ENV_DIR_LIST = new String[] {
			".kagerow",
			".kagerow/logs",
			CreatePluginLink.ENV_DIR,
			".kagerow/cache",
			".kagerow/runtime",
			".kagerow/setting"
	};

	/** {@inheritDoc} */
	@Override
	public void init() throws InitProcessFailedException {

		try {
			// ユーザディレクトリを取得
			String homeDir = AppPathUtils.getBasePath();

			// 前提となる、もしくは必須となるディレクトリを生成
			for (String env : ENV_DIR_LIST) {
				// パスを生成
				Path path = Paths.get(homeDir, env);
				// 存在を確認し、ディレクトリが存在しなければ生成
				if (Files.notExists(path)) {
					Files.createDirectory(path);
				}
			}
			// 実行に必要なディレクトリを必要に応じて生成
			for (String arg : ENV_ARG_LIST) {
				// VMオプションを取得
				String env = System.getProperty(arg.substring(2));
				// パスを生成
				Path path = Paths.get(homeDir, env);
				// 存在を確認し、ディレクトリが存在しなければ生成
				if (Files.notExists(path)) {
					Files.createDirectory(path);
				}
			}
		} catch (IOException e) {
			// 生成に失敗した場合は即座に処理を中断
			// 継続したとしても王族で落ちるため
			throw new InitProcessFailedException(e, FailType.Reject);
		}
	}

}

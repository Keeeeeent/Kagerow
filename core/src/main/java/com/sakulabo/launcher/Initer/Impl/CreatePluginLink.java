package com.sakulabo.launcher.Initer.Impl;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import com.sakulabo.core.Common.AppPathUtils;
import com.sakulabo.launcher.Initer.InitProcessFailedException;
import com.sakulabo.launcher.Initer.InitProcessFailedException.FailType;
import com.sakulabo.launcher.Initer.InitProcessor;

/**
 * デフォルトプラグインのシンボリックリンクを生成する初期化プロセッサー実装クラスです
 * @author keeeeeent
 */
public class CreatePluginLink implements InitProcessor {

	/** 構築が必要なディレクトリを表す文字列 */
	static final String ENV_DIR = ".kagerow/plugin";
	/** 生成シンボリック一覧 */
	private List<Path> createList = new ArrayList<>();

	/** {@inheritDoc} */
	@Override
	public void init() throws InitProcessFailedException {

		// ユーザディレクトリを取得
		String homeDir = AppPathUtils.getBasePath();

		try {

			// プラグインファイル一覧を習得
			List<Path> pluginList = Files.walk(AppPathUtils.createDefaultPluginDirPath())
					.map(Path::normalize)
					.map(Path::toAbsolutePath)
					.filter(Predicate.not(Files::isDirectory))
					.filter(this::targetFile)
					.toList();

			// シンボリックリンク生成
			for (Path pluginFile : pluginList) {

				// 生成リンク名称
				String targetName = Objects.toString(pluginFile.getFileName());
				// 生成先基底パス
				Path output = Paths.get(homeDir)
						.normalize()
						.toAbsolutePath()
						.resolve(ENV_DIR)
						.resolve(targetName);

				// リンクの存在確認
				if (Files.exists(output)) {
					/**
					 * アプリケーションがクラッシュした場合、古いリンクが残るため
					 * 古いリンクが存在する場合は削除する。
					 * 削除をしないと、プラグインローダーでリンクが無効となりアプリケーションが起動不可となる
					 */
					Files.delete(output);
				}

				// シンボリックリンク生成
				Files.createSymbolicLink(output, pluginFile);
				// JVM終了時に削除
				output.toFile().deleteOnExit();
				// 生成済みリストへ格納
				createList.add(output);

			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new InitProcessFailedException(e, FailType.Reject);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void endLod(Method method, Object[] args, Object result) {
		System.out.println(String.format("%d LinkFile Created", createList.size()));
	}

	/**
	 * プラグインファイルか判定します
	 * @param target 判定対象
	 * @return 判定結果
	 */
	private boolean targetFile(Path target) {
		return target.toString().endsWith(".plugin");
	}

}

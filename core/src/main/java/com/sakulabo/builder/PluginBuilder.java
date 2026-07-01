package com.sakulabo.builder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.sakulabo.regulation.annotation.processor.AppPluginProcessor;

/**
 * プラグイン構成を構築するクラスです<br/>
 * このクラスはビルダーを拡張していますが、メインクラスとしても機能します
 * @author keeeeeent
 */
public final class PluginBuilder {

	/** zip格納先（ライブラリー） */
	private final static String LIBRARY_PATH = "lib/";

	/**
	 * プラグインビルダーのエントリです
	 * @param args コマンドライン引数
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {

		// パス宣言
		Path dir, output, setting;

		// 引数確認
		if (args.length == 0) {
			throw new IllegalArgumentException("引数の指定は必須です");
		} else if (args.length != 3) {
			throw new IllegalArgumentException("""

					KagerowPluginのビルドには以下パラメータが必須です
					第1引数：バンドル先のディレクトリ
					第2引数：バンドル後ファイル名（拡張子は.plugin）
					第3引数：plugin-setting.propertiesの出力先ディレクトリ
					　　　　※generated-sources/annotations配下に出力されます
					""");
		} else {
			dir = Paths.get(args[0]);
			setting = Paths.get(args[2]);
			if (!Files.isDirectory(dir)) {
				throw new IllegalArgumentException("バンドル先はディレクトリである必要があります");
			} else if (Files.notExists(dir)) {
				throw new IllegalArgumentException("バンドル先のディレクトリが存在しません");
			} else if (Files.notExists(setting)) {
				throw new IllegalArgumentException("設定ファイルディレクトリが存在しません");
			}
			if (!args[1].endsWith(".plugin")) {
				args[1] = args[1].concat(".plugin");
			}
			{
				// 前提となる親ディレクトリの存在確認
				Path tmp = dir.getParent();
				if (Objects.nonNull(tmp)) {
					// 存在しない場合、そのままパスを結合する
					output = tmp.resolve(args[1]);
				} else {
					// 存在する場合、見つかった親ディレクトリにパスを連結
					output = dir.resolve(args[1]);
				}
				// 設定ファイル存在確認
				setting = setting.resolve(AppPluginProcessor.SETTING_FILE_NAME);
				System.out.println(setting.toAbsolutePath());
				if (Files.notExists(setting)) {
					throw new IllegalStateException(AppPluginProcessor.SETTING_FILE_NAME + " が存在しないためビルドできません");
				}
			}

		}

		// 出力先ディレクトリ生成
		{
			Path tmp = output.getParent();
			if (Objects.nonNull(tmp) && Files.notExists(tmp)) {
				Files.createDirectories(tmp);
			}
		}

		// バンドル処理
		try (OutputStream out = Files.newOutputStream(output);
				ZipOutputStream zout = new ZipOutputStream(out);) {
			// エントリ初期化
			ZipEntry entry = null;
			// バンドルファイル一覧取得
			List<Path> bundleList = Files.list(dir)
					.filter(f -> f.toString().endsWith(".jar"))
					.toList();
			// バンドル開始(プログラム資材)
			for (Path path : bundleList) {
				String targetFile = Objects.toString(path.getFileName());
				entry = new ZipEntry(LIBRARY_PATH + targetFile);
				zout.putNextEntry(entry);
				try (InputStream in = Files.newInputStream(path)) {
					in.transferTo(zout);
				}
				zout.closeEntry();
			}
			// 設定ファイル
			entry = new ZipEntry(AppPluginProcessor.SETTING_FILE_NAME);
			zout.putNextEntry(entry);
			try (InputStream in = Files.newInputStream(setting)) {
				in.transferTo(zout);
			}
			zout.closeEntry();
		}

	}

}

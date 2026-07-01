package com.sakulabo.regulation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * アノテーションプロセッサーをコンパイルするエントリクラスです
 * 
 * @author keeeeeent
 */
public class Main {

	/** コマンドプロセス実行インスタンス */
	private final ProcessBuilder cmd;

	/** コマンドライン情報文字色 */
	private final static String CMD_INFO_STR = "\u001b[00;34m";
	/** コマンドライン文字色エンド文字 */
	private final static String CMD_END_STR = "\u001b[00m";

	/**
	 * コンパイル処理に必要は初期化処理を実行します
	 * @param options オプション
	 */
	private Main(List<String> options) {
		// プロセスビルダー生成
		cmd = new ProcessBuilder();
		// プロセスビルダーIO接続
		cmd.inheritIO();
		// JAVA_HOMEの設定
		String javaHome = Paths.get(System.getProperty("java.home"))
				.normalize()
				.toAbsolutePath()
				.toString();
		cmd.environment().put("JAVA_HOME", javaHome);
		cmd.command(options);

		consoleCmd(options);
	}

	/**
	 * コンパイル処理を実行します
	 * @param args コマンド引数
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws IOException, InterruptedException {

		// コマンド作成
		List<String> options = new ArrayList<>();
		// メインコマンド
		options.add("javac");
		// 基本オプションセット
		setInit(options);
		// javacモジュール追加
		setJavac(options);
		// コンパイル対象のセット
		setTarget(options);

		// コマンド生成
		Main that = new Main(options);
		// コマンド実行
		Process process = that.cmd.start();
		// 実行待機
		process.waitFor();

	}

	/**
	 * 基本的なコンパイルに必要なオプションを指定します
	 * @param options オプション格納リスト
	 */
	private static void setInit(List<String> options) {
		options.add("-verbose");
		options.add("-d");
		options.add(Paths.get("../core/target/classes/").normalize().toAbsolutePath().toString());
		options.add("--module-path");
		String[] modulePahts = {
				Paths.get("../core/target/library").normalize().toAbsolutePath().toString().concat("/"),
				Paths.get("../core/target/classes").normalize().toAbsolutePath().toString().concat("/")
		};
		StringJoiner join = new StringJoiner(System.getProperty("path.separator"));
		for (String modulePaht : modulePahts)
			join.add(modulePaht);
		options.add(join.toString());
		options.add("-proc:none");
	}

	/**
	 * コンパイル対象のソースパスを設定します
	 * @param options オプション格納リスト
	 * @throws IOException 
	 */
	private static void setTarget(List<String> options) throws IOException {
		Path base = Paths.get("../core/src/main/javac").normalize().toAbsolutePath();
		List<String> target = Files.find(base, 100, (p, _) -> {
			return p.getFileName().toString().endsWith(".java");
		})
				.map(Path::normalize)
				.map(Path::toAbsolutePath)
				.map(Path::toString)
				.collect(Collectors.toList());
		options.addAll(target);
	}

	/**
	 * コンパイルに必要な依存モジュールを解決します
	 * @param options オプション格納リスト
	 */
	private static void setJavac(List<String> options) {

		String exports = "--add-exports";
		String[] modules = {
				"jdk.compiler/com.sun.tools.javac.api=com.sakulabo.core",
				"jdk.compiler/com.sun.tools.javac.code=com.sakulabo.core",
				"jdk.compiler/com.sun.tools.javac.comp=com.sakulabo.core",
				"jdk.compiler/com.sun.tools.javac.model=com.sakulabo.core",
				"jdk.compiler/com.sun.tools.javac.processing=com.sakulabo.core",
				"jdk.compiler/com.sun.tools.javac.tree=com.sakulabo.core",
				"jdk.compiler/com.sun.tools.javac.util=com.sakulabo.core"
		};

		for (String module : modules) {
			options.add(exports);
			options.add(module);
		}
	}

	/**
	 * Mavenビルドのログと同じ形式でログを出力します<br/>
	 * このログはコマンドプレフィックス専用です
	 * @param cmd 出力内容
	 */
	private static final void consoleCmd(Object cmd) {
		System.out.println(String.format("[%sINFO%s] --- コマンド %s",
				CMD_INFO_STR, CMD_END_STR, Objects.toString(cmd)));
	}

}

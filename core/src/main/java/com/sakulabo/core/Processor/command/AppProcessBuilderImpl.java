package com.sakulabo.core.Processor.command;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.sakulabo.core.Common.AppPathUtils;
import com.sakulabo.core.Common.VMOption;
import com.sakulabo.core.Kagerow.KagerowApplication;
import com.sakulabo.core.Kagerow.KagerowApplication.Mode;
import com.sakulabo.core.Kagerow.Utilities.KagerowCommandMode;
import com.sakulabo.core.Kagerow.Utilities.KagerowProcessBuilder;

/**
 * Kagerowアプリケーション専用プロセスビルダー
 * @author keeeeeent
 */
public final class AppProcessBuilderImpl implements KagerowProcessBuilder {

	/** 環境変数 */
	private final Map<String, String> ENV;
	/** コマンドライン引数 */
	private List<String> ARGS = new ArrayList<>();
	/** コマンド実行モード */
	private final KagerowCommandMode MODE;
	/** 標準エラー格納メモリ */
	private ByteArrayOutputStream errorOutputStream;

	/**
	 * デフォルトコンストラクタ
	 * @param mode モード
	 * @param env  環境変数
	 */
	public AppProcessBuilderImpl(KagerowCommandMode mode, Map<String, String> env) {
		this.ENV = env;
		this.MODE = mode;
	}

	/** {@inheritDoc} */
	@Override
	public KagerowProcessBuilder addArg(String arg) {
		// 引数チェック
		Objects.requireNonNull(arg);
		// フィールド格納
		ARGS.add(arg);
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public KagerowProcessBuilder addEnv(String key, String value) {
		// 引数チェック
		Objects.requireNonNull(key);
		Objects.requireNonNull(value);
		// フィールド格納
		ENV.put(key, value);
		return this;
	}

	/**
	 * 標準エラーストリームを生成します
	 * @return エラーストリーム
	 */
	public InputStream getErrorStream() {
		if (Objects.nonNull(errorOutputStream)) {
			return new ByteArrayInputStream(errorOutputStream.toByteArray());
		} else {
			return InputStream.nullInputStream();
		}
	}

	/**
	 * バッファリング専用のエラーストリーム格納メモリを生成します
	 * @return エラーストリームメモリ
	 */
	public OutputStream createErrorStream() {
		errorOutputStream = new ByteArrayOutputStream();
		return errorOutputStream;
	}

	/** {@inheritDoc} */
	@Override
	public ProcessBuilder build() {

		// コマンド生成
		List<String> commandLine = new ArrayList<>();
		// コマンド本体設定
		commandLine.add(MODE.getScript());
		// コマンドオプション指定
		for (String opt : MODE.getOption())
			commandLine.add(opt);
		// 実行スクリプト設定
		commandLine.addAll(ARGS);

		// プロセスビルダー生成
		ProcessBuilder builder = new ProcessBuilder(commandLine);
		// 環境変数設定
		builder.environment().putAll(ENV);

		// javaコマンドパス取得
		String java = VMOption.JAVA_HOME.getVMoption();
		// 環境変数追加
		builder.environment().put("JAVA_HOME", java);
		String pathEnv = builder.environment().get("PATH");
		String separator = VMOption.PATH_SEPARATOR.getVMoption();
		pathEnv = String.join(separator, pathEnv, java);
		builder.environment().put("PATH", pathEnv);

		// CLIモードの場合、標準出力などを親プロセスに表示
		// GUIモードの場合、コマンド同士のパイプ接続ができなくなるため分離しておく
		if (KagerowApplication.getApplicationMode().equals(Mode.CLI)) {
			builder.inheritIO();
		} else {
			// GUIモードでは標準出力を使用しないため破棄する
			builder.redirectOutput(Redirect.DISCARD);
		}

		// カレントディレクトリ
		builder.directory(AppPathUtils.createRuntimeDirPath().toFile());

		return builder;
	}

}

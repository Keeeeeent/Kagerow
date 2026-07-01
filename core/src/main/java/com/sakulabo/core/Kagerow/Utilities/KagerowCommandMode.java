package com.sakulabo.core.Kagerow.Utilities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Kagerowアプリケーション専用プロセスビルダー向けの実行モード列挙クラスです
 * @author keeeeeent
 */
public enum KagerowCommandMode {

	/** パワーシェル（Windows向け） */
	PowerShell(
			"ps", 
			"powershell", 
			new String[]{"-ExecutionPolicy", "Bypass", "-File"},
			".ps",
			new String[] {"-Command","exit"}),
	/** コマンドプロンプト（Windows向け） */
	Command(
			"cmd", 
			"cmd", 
			new String[]{"/c"}, 
			".cmd",
			new String[] {"/c", "exit"}),
	/** シェルスクリプト(Linux/MacOS向け) */
	Shell(
			"sh",
			"sh",
			new String[]{}, 
			".sh",
			new String[] {"-c" , "exit" , "0"}),
	/** シェルスクリプト[Bash](Linux/MacOS向け) */
	Bash(
			"bash",
			"bash",
			new String[]{}, 
			".sh",
			new String[] {"-c" , "exit" , "0"});

	/** 実行モード文字列表現 */
	private String mode;
	/** 実行スクリプト文字列表現 */
	private String script;
	/** 実行スクリプトオプション文字列表現 */
	private String[] option;
	/** 実行スクリプト拡張子文字列表現 */
	private String extension;
	/** 実行可能判定コマンド */
	private String[] trialRun;

	/**
	 * デフォルトコンストラクタ
	 * @param mode      実行モード文字列表現
	 * @param script    実行スクリプト文字列表現
	 * @param option    実行スクリプトオプション文字列表現
	 * @param extension 実行スクリプト拡張子文字列表現
	 * @param trialRun  実行可能判定コマンド
	 */
	private KagerowCommandMode(String mode, String script, String[] option, String extension,String[] trialRun) {
		this.mode = mode;
		this.script = script;
		this.option = option;
		this.extension = extension;
		this.trialRun = trialRun;
	}

	/**
	 * 文字列表現をモード列挙型に変換します
	 * @param mode モード文字列
	 * @return モード列挙型
	 */
	public static KagerowCommandMode toMode(String mode) {
		KagerowCommandMode result = null;
		for (KagerowCommandMode m : values()) {
			if (m.mode.equalsIgnoreCase(mode)) {
				result = m;
				break;
			}
		}
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return mode;
	}

	/**
	 * コマンドの文字列表現を返却します
	 * @return コマンド文字列表現
	 */
	public String getScript() {
		return script;
	}

	/**
	 * コマンドのオプションを返却します
	 * @return オプション
	 */
	public String[] getOption() {
		return option;
	}

	/**
	 * コマンド実行ファイルの拡張子を返却します
	 * @return コマンド実行ファイル拡張子
	 */
	public String getExtension() {
		return extension;
	}
	
	/**
	 * コマンドが実行可能か判定します
	 * @return コマンド実行可否
	 */
	public boolean canExecute() {
		try {
			// コマンド実行インスタンス生成
			ProcessBuilder builder = new ProcessBuilder();
			builder.redirectOutput(ProcessBuilder.Redirect.DISCARD);
			builder.redirectError(ProcessBuilder.Redirect.DISCARD);
			// コマンド構築
			List<String> cmd = new ArrayList<>();
			cmd.add(script);
			for(String c : trialRun)
				cmd.add(c);
			// コマンド実行
			builder.command(cmd);
			Process prosess = builder.start();
			// コマンド終了待機
			prosess.waitFor();
			// 実行が成功した場合
			return true;
		}catch( IOException | InterruptedException e ) {
			// 実行が失敗した場合
			return false;
		}
	}

}

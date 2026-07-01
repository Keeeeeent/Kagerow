package com.sakulabo.launcher.Initer.Impl;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Properties;

import com.sakulabo.core.Common.AppPathUtils;
import com.sakulabo.core.Common.VMOption;
import com.sakulabo.launcher.Initer.InitProcessFailedException;
import com.sakulabo.launcher.Initer.InitProcessFailedException.FailType;
import com.sakulabo.launcher.Initer.InitProcessor;
import com.sakulabo.launcher.Initer.Initer;
import com.sakulabo.launcher.Initer.Initer.TargetKey;

/**
 * Javaコマンドラインの必須条件を判定する初期化プロセッサー実装クラスです
 * @author keeeeeent
 */
public final class CheckArgument implements InitProcessor {

	/** チェック対象引数リスト */
	private final static List<Argument> checkList = Collections
			.unmodifiableList(new ArrayList<Argument>() {
				{
					/**
					 * ここに登録した順番でチェックをします
					 */

					// マーカー
					add(new Argument(VMOption.INSTANCE.toVMOption()));

					// エージェントファイル
					add(new Argument(VMOption.AGENTJAR.toVMOption()));

					// 一時フォルダ
					add(new Argument(VMOption.APP_IO_TMPDIR.toVMOption()));

					// アーカイブフォルダ
					add(new Argument(VMOption.APP_IO_ARCHIVEDATADIR.toVMOption()));

					// クラスロードプーリングキュー容量
					// add(new ArgumentNumberFormatChecker("-Dapp.class.pool.size"));

					// アプリケーション起動モード
					add(new Argument("-Dapp.init.mode"));

				}
			});

	/** メッセージキー（設置値未設定） */
	private final static String NO_ENV = "no-env";
	/** メッセージキー（IOエラー） */
	private final static String IO_ERROR = "io-error";
	/** メッセージキー（フォーマット不正） */
	private final static String ILLEGAL_FORMAT = "illegal_format";

	/** {@inheritDoc} */
	@Override
	public void init() throws InitProcessFailedException {
		// ランチャー設定ファイル
		String settingFile = VMOption.LAUNCHER_FILE_KEY.getVMoption();
		if (Objects.isNull(settingFile)) {
			// ランチャー設定ファイルが未設定の場合
			throw new InitProcessFailedException(Initer.createMesssage(Initer.PREFIX, CheckArgument.NO_ENV,
					new Object[] { VMOption.LAUNCHER_FILE_KEY.toVMOption() }), FailType.Reject);
		}
		// ランチャー設定ファイルパス生成
		Path configPath = AppPathUtils.createConfigDirPath()
				.resolve(settingFile)
				.normalize();
		// 設定ファイル読み込み
		try (InputStream input = Files.newInputStream(configPath)) {
			Properties prop = new Properties();
			prop.load(input);
			// システムプロパティーを設定
			for (Entry<Object, Object> p : prop.entrySet()) {
				String key = Objects.toString(p.getKey());
				// 設定されたプロパティーが既に設定済みの場合、何もしない
				String sysValue = System.getProperty(key);
				if (Objects.isNull(sysValue)) {
					System.setProperty(key, Objects.toString(p.getValue()));
				}
			}
		} catch (IOException e) {
			// IOエラーの場合、即座に処理終了
			throw new InitProcessFailedException(Initer.createMesssage(Initer.PREFIX, CheckArgument.IO_ERROR),
					e, FailType.Reject);
		}
		// その他のチェック
		for (Argument args : checkList) {
			args.check();
		}
	}

	/** {@inheritDoc} */
	@Override
	public void endLod(Method method, Object[] args, Object result) {
		System.out.println(String.format("%d Options Checked", checkList.size()));
	}

	/**
	 * 必須引数の規定クラスです 
	 */
	@TargetKey("CheckArgument")
	private static class Argument {

		/** ターゲットVM引数 */
		protected final String VM_ARG;

		/**
		 * コンストラクタ
		 * @param envKey VMオプション名称
		 */
		private Argument(String envKey) {
			VM_ARG = envKey;
		}

		/**
		 * 必須引数を指定しているかチェックします
		 * @throws InitProcessFailedException
		 */
		protected void check() throws InitProcessFailedException {
			String target = Argument.this.VM_ARG.substring(2);
			if (Objects.isNull(System.getProperty(target))) {
				throw new InitProcessFailedException(Initer.createMesssage(Initer.PREFIX, CheckArgument.NO_ENV,
						new Object[] { Argument.this.VM_ARG }), FailType.Reject);
			}
		}
	}

	/**
	 * 必須引数の数値フォーマットチェッククラスです 
	 */
	@TargetKey("CheckArgument")
	private static class ArgumentNumberFormatChecker extends Argument {

		/**
		 * コンストラクタ
		 * @param envKey VMオプション名称
		 */
		private ArgumentNumberFormatChecker(String envKey) {
			super(envKey);
		}

		/** {@inheritDoc} */
		@Override
		protected void check() throws InitProcessFailedException {
			// 元のチョックを実行
			super.check();
			// 追加でチェックを実行
			String target = this.VM_ARG.substring(2);
			String value = System.getProperty(target);
			try {
				Integer.valueOf(value);
			} catch (NumberFormatException e) {
				throw new InitProcessFailedException(
						Initer.createMesssage(Initer.PREFIX, CheckArgument.ILLEGAL_FORMAT,
								new Object[] { this.VM_ARG }),
						e, FailType.Reject);
			}
		}

	}

}

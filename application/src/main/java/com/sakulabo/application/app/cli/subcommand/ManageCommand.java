package com.sakulabo.application.app.cli.subcommand;

import java.io.Console;
import java.util.Arrays;
import java.util.concurrent.Callable;

import com.sakulabo.core.Kagerow.KagerowApplication;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;
import com.sakulabo.core.Kagerow.Utilities.KagerowUtilities;

import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * 管理機能実装クラスです
 *
 * @author keeeeeent
 */
@Command(name = "manage")
public class ManageCommand implements Callable<Integer> {

	/**
	 * オプショングループ
	 */
	private static class Options {

		/** パスワード設定コマンド */
		@Option(names = "--change-password", description = "Change the password.")
		private boolean changePassword;
		/** セキュアブート */
		@Option(names = "--secure-boot", description = "Change to SecureBoot.")
		private boolean secureBoot;
		/** アプリケーション初期化 */
		@Option(names = "--reset-application", description = "All data will be deleted and the application will be reset.")
		private boolean resetApplication;

	}

	/** オプション */
	@ArgGroup(exclusive = true, multiplicity = "1")
	private Options options;

	/** {@inheritDoc} */
	@Override
	public Integer call() throws Exception {
		if (options.changePassword) {
			return changePassword();
		} else if (options.secureBoot) {
			return secureBoot();
		} else if (options.resetApplication) {
			return resetApplication();
		}
		return Integer.valueOf(0);
	}

	/**
	 * パスワード変更コマンド
	 * @return リターンコード
	 */
	private Integer changePassword() {
		try {
			Console console = System.console();
			char[] newPassword = console.readPassword("New password: ");
			char[] confirmPassword = console.readPassword("Confirm password: ");
			if (!Arrays.equals(newPassword, confirmPassword)) {
				System.err.println("Passwords do not match.");
				return Integer.valueOf(2);
			}
			KagerowApplication.getInstance().changePassword(new String(newPassword));
			Arrays.fill(newPassword, '\0');
			Arrays.fill(confirmPassword, '\0');
		} catch (Exception e) {
			KagerowLogger.newAppLogger().err(e);
			return Integer.valueOf(1);
		}
		return Integer.valueOf(0);
	}

	/**
	 * セキュアブート切り替えコマンド
	 * @return リターンコード
	 */
	private Integer secureBoot() {
		try {
			if (KagerowUtilities.isSecure()) {
				System.err.println("Secure Boot is already enabled.");
				return Integer.valueOf(3);
			}
			Console console = System.console();
			char[] newPassword = console.readPassword("password: ");
			char[] confirmPassword = console.readPassword("Confirm password: ");
			if (!Arrays.equals(newPassword, confirmPassword)) {
				System.err.println("Passwords do not match.");
				return Integer.valueOf(2);
			}
			KagerowApplication.getInstance().changeToSecureBoot(new String(newPassword));
			Arrays.fill(newPassword, '\0');
			Arrays.fill(confirmPassword, '\0');
		} catch (Exception e) {
			KagerowLogger.newAppLogger().err(e);
			return Integer.valueOf(1);
		}
		return Integer.valueOf(0);
	}

	/**
	 * データリセットコマンド
	 * @return リターンコード
	 */
	private Integer resetApplication() {
		try {
			Console console = System.console();
			System.out.println();
			System.out.println("This will permanently delete all Kagerow application data.");
			System.out.println("Configuration, credentials, and stored data will be removed.");
			System.out.println();
			String answer = console.readLine("Continue? [y/N]: ");
			if (!"y".equalsIgnoreCase(answer)) {
				System.out.println("Reset cancelled.");
				return Integer.valueOf(0);
			} else {
				KagerowApplication.resetApplication();
			}
		} catch (Exception e) {
			KagerowLogger.newAppLogger().err(e);
			return Integer.valueOf(1);
		}
		return Integer.valueOf(0);
	}

}

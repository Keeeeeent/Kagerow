package com.sakulabo.application.common.provider;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JOptionPane;

import com.sakulabo.application.app.gui.ResetPassDialog;
import com.sakulabo.application.common.code.ApplicationConstProperty;
import com.sakulabo.application.common.code.DialogHelperText;
import com.sakulabo.application.common.initializer.DefaultInitializer;
import com.sakulabo.application.common.initializer.EmptyInitializer;
import com.sakulabo.application.helper.DialogHelper;
import com.sakulabo.core.Kagerow.Adapter.KagerowDIContextAdapter;
import com.sakulabo.core.Kagerow.Spi.KagerowAutomaticStarter;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;
import com.sakulabo.core.Kagerow.Utilities.KagerowUtilities;

/**
 * Kagerowアプリケーション起動実装を提供するSPI実装クラスです
 *
 * @author keeeeeent
 */
public class AutomaticStarterProvider implements KagerowAutomaticStarter {

	/** パスワードの初期化キャンセル有無を管理するフラグです */
	private static final AtomicBoolean CANCEL_FLUG = new AtomicBoolean(false);
	/** パスワード失敗ファイルパス */
	private static final Path MISTAKE_PASS_FILE;
	static {
		MISTAKE_PASS_FILE = KagerowUtilities.createKagerowHomePath().resolve("mistake");
	}

	/** {@inheritDoc} */
	@Override
	public KagerowDIContextAdapter getAdapter() {
		// DIコンテキスト初期化アダプター
		KagerowDIContextAdapter adapter;
		// DIコンテキスト初期化アダプターロード
		String prop = System.getProperty(ApplicationConstProperty.CUSTOM_APP_SYSTEM_PROP_KEY);
		if (Objects.isNull(prop)) {
			adapter = new DefaultInitializer();
		} else {
			adapter = new EmptyInitializer();
		}
		return adapter;
	}

	/** {@inheritDoc} */
	@Override
	public String getPassword() {
		// パスワード初期化
		String password = null;
		// セキュア起動か確認
		if (KagerowUtilities.isSecure()) {
			// セキュア起動の場合、パスワード入力を要求
			switch (DefaultInitializer.getMode()) {
			case DefaultInitializer.INIT_MODE_SYSTEM_PROP_GUI_VAL:
				// GUIモードの場合
				password = DialogHelper.showPasswordDialog();
				break;
			case DefaultInitializer.INIT_MODE_SYSTEM_PROP_CLI_VAL:
				// CLIモードの場合
				// TODO 実装を提供する
				break;
			default:
				// 未知のモードの場合、何もしない
				break;
			}
			// パスワードが未入力の場合
			if (Objects.isNull(password) || password.isEmpty()) {
				CANCEL_FLUG.set(true);
			}
		}
		return password;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isCancel() {
		return CANCEL_FLUG.get();
	}

	/** {@inheritDoc} */
	@Override
	public void mistake() {
		DialogHelper.showPasswordMistake();
		try {
			if (Files.notExists(MISTAKE_PASS_FILE)) {
				// パスワード検証失敗ファイルがない場合、初回とみなしファイルを作成し終了
				Files.createFile(MISTAKE_PASS_FILE);
			} else {
				// パスワード検証失敗ファイルがある場合、アプリケーションリセットを行うか確認
				int res = JOptionPane.showConfirmDialog(null, DialogHelperText.DialogHelper_RESET_APP_MSG.toString(),
						DialogHelperText.DialogHelper_RESET_APP_TITLE.toString(), JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE);
				// 初期化を希望の場合、初期化処理へ移行
				if (res == JOptionPane.YES_OPTION) {
					ResetPassDialog dialog = new ResetPassDialog();
					dialog.resetPass();
				}
			}
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void unexpected(Throwable e) {
		KagerowLogger.newAppLogger().err(e);
		DialogHelper.showStaticSystemError(DialogHelperText.DialogHelper_ERROR_UNEXPECTED_MSG.toString());
	}

	/** {@inheritDoc} */
	@Override
	public void success() {
		try {
			Files.deleteIfExists(MISTAKE_PASS_FILE);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

}

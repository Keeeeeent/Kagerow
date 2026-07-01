package com.sakulabo.application.common.provider;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

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
	}

	/** {@inheritDoc} */
	@Override
	public void unexpected(Throwable e) {
		KagerowLogger.newAppLogger().err(e);
		DialogHelper.showStaticSystemError(DialogHelperText.DialogHelper_ERROR_UNEXPECTED_MSG.toString());
	}

}

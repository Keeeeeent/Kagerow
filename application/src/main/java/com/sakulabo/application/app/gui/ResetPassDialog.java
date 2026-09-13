package com.sakulabo.application.app.gui;

import java.io.IOException;
import java.util.Objects;

import javax.swing.JOptionPane;

import com.sakulabo.application.common.code.DialogHelperText;
import com.sakulabo.application.common.code.ResetPassDialogText;
import com.sakulabo.application.common.initializer.GraphicComponent;
import com.sakulabo.application.helper.DialogHelper;
import com.sakulabo.core.Kagerow.KagerowApplication;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;
import com.sakulabo.regulation.annotation.KagerowComponent;
import com.sakulabo.regulation.annotation.KagerowInject;

/**
 * パスワードリセット実装クラスです
 *
 * @author keeeeeent
 */
@KagerowComponent
@GraphicComponent
public class ResetPassDialog {

	/** ダイアログヘルパー */
	@KagerowInject
	private DialogHelper dialogHelper;

	/**
	 * アプリケーションのデータをリセットします
	 */
	public void resetPass() {
		// データ消去前の警告と確認を実施
		boolean result = false;
		if (Objects.nonNull(dialogHelper)) {
			result = dialogHelper.showChoiceDialog(ResetPassDialogText.INFO_003.toString(),
					ResetPassDialogText.INFO_001.toString());
		} else {
			int res = JOptionPane.showConfirmDialog(null, ResetPassDialogText.INFO_001.toString(),
					ResetPassDialogText.INFO_003.toString(), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			result = res == JOptionPane.YES_OPTION;
		}

		if (result) {
			// ユーザがYESを選択したっ場合、データリセット実施
			try {
				// データリセット
				KagerowApplication.resetApplication();
				// データリセット完了後、ユーザ通知
				if (Objects.nonNull(dialogHelper)) {
					dialogHelper.showSystemInfo(ResetPassDialogText.INFO_002.toString());
				} else {
					JOptionPane.showMessageDialog(null, ResetPassDialogText.INFO_002.toString(),
							DialogHelperText.DialogHelper_INFO_MSG.toString(), JOptionPane.INFORMATION_MESSAGE);
				}
			} catch (IOException e) {
				// データリセット失敗の場合ログに記録
				KagerowLogger.newAppLogger().err(e);
				// 失敗した旨ユーザ通知
				if (Objects.nonNull(dialogHelper)) {
					dialogHelper.showSystemWarning(ResetPassDialogText.ERROR_001.toString());
				} else {
					JOptionPane.showMessageDialog(null, ResetPassDialogText.ERROR_001.toString(),
							DialogHelperText.DialogHelper_WARN_MSG.toString(), JOptionPane.WARNING_MESSAGE);
				}
			}

		}
	}

}

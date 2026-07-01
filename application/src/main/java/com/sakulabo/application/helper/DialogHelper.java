package com.sakulabo.application.helper;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import com.sakulabo.application.app.gui.MainFrame;
import com.sakulabo.application.common.code.DialogHelperText;
import com.sakulabo.application.common.initializer.GraphicComponent;
import com.sakulabo.core.Kagerow.Exception.KagerowExecuteException.CommandException;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;
import com.sakulabo.regulation.annotation.KagerowComponent;
import com.sakulabo.regulation.annotation.KagerowInject;

/**
 * ダイアログを表示するヘルパーです
 * 
 * @author keeeeeent
 */
@KagerowComponent
@GraphicComponent
public final class DialogHelper {

	/** プログレスバー強制終了 */
	public static final Double STOP_PROGRESS_DIALOG = Double.valueOf(-1);

	/** メインフレーム */
	@KagerowInject
	private MainFrame mainFrame;

	/** ロガー */
	@KagerowInject
	private KagerowLogger logger;

	/**
	 * コンパイルエラーダイアログを表示します
	 * @param message エラーメッセージ
	 */
	public void showCompileError(String message) {
		JOptionPane.showMessageDialog(mainFrame.frame, message,
				DialogHelperText.DialogHelper_COMPILE_ERROR_MSG.toString(), JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * SQL実行エラーダイアログを表示します
	 * @param message エラーメッセージ
	 */
	public void showSQLError(String message) {
		JOptionPane.showMessageDialog(mainFrame.frame, message,
				DialogHelperText.DialogHelper_SQL_ERROR_MSG.toString(), JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * コマンド実行エラーダイアログを表示します
	 * @param error エラーインスタンス
	 */
	public void showCMDError(CommandException error) {
		// パネル生成
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setBorder(BorderFactory
				.createEmptyBorder(20, 30, 30, 30));
		// エラーメッセージ表示コンポーネント
		JTextArea text = new JTextArea();
		text.setBackground(Color.WHITE);
		text.setLineWrap(true);
		text.setWrapStyleWord(true);
		text.setEditable(false);
		// スクロールパネル設定
		JScrollPane scrollPane = new JScrollPane(text);
		scrollPane.setHorizontalScrollBarPolicy(
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setVerticalScrollBarPolicy(
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		panel.add(BorderLayout.CENTER, scrollPane);
		// エラーメッセージ格納メモリ
		StringJoiner joiner = new StringJoiner(System.lineSeparator());
		// エラーメッセージ読み取り
		try (InputStream input = error.getError();
				InputStreamReader converter = new InputStreamReader(input);
				BufferedReader reader = new BufferedReader(converter)) {
			String line;
			while ((line = reader.readLine()) != null) {
				joiner.add(line);
			}
		} catch (IOException _) {
			// メモリ上のストリームのためエラーは発生しない想定
		}
		// エラーメッセージ設定
		text.setText(joiner.toString());
		// ダイアログ表示
		showCustomDialog(
				panel,
				DialogHelperText.DialogHelper_CMD_ERROR_MSG.toString(),
				320, 150);
	}

	/**
	 * バリデーションエラーダイアログを表示します
	 * @param message エラーメッセージ
	 */
	public void showValidationError(String message) {
		JOptionPane.showMessageDialog(mainFrame.frame, message,
				DialogHelperText.DialogHelper_VALIDATION_ERROR_MSG.toString(), JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * システムエラーダイアログを表示します
	 * @param message エラーメッセージ
	 */
	public void showSystemError(String message) {
		JOptionPane.showMessageDialog(mainFrame.frame, message,
				DialogHelperText.DialogHelper_ERROR_MSG.toString(), JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * 警告ダイアログを表示します
	 * @param message 警告メッセージ
	 */
	public void showSystemWarning(String message) {
		JOptionPane.showMessageDialog(mainFrame.frame, message,
				DialogHelperText.DialogHelper_WARN_MSG.toString(), JOptionPane.WARNING_MESSAGE);
	}

	/**
	 * システム通知ダイアログを表示します<br/>
	 * このメッセージはYes or No によるユーザ選択の結果を返却します<br/>
	 * @param title タイトル
	 * @param message メッセージ
	 * @return 選択結果 Yes -> true : No -> false
	 */
	public boolean showChoiceDialog(String title, String message) {
		int result = JOptionPane.showConfirmDialog(mainFrame.frame, message, title,
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE);
		return result == JOptionPane.YES_OPTION;
	}

	/**
	 * ドロップダウンダイアログを表示します<br/>
	 * 未選択の場合はnullを返却します
	 * @param title タイトル
	 * @param selectList 選択肢リスト
	 * @return 選択した結果
	 */
	public String showSelectDialog(String title, String[] selectList) {
		JComboBox<String> comboBox = new JComboBox<>(selectList);
		String result = null;
		int selecctResult = JOptionPane.showOptionDialog(
				mainFrame.frame,
				comboBox,
				title,
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE,
				null,
				null,
				null);
		if (selecctResult == JOptionPane.YES_OPTION) {
			result = (String) comboBox.getSelectedItem();
		}
		return result;
	}

	/**
	 * パスワード入力要求ダイアログを表示します
	 * @param title タイトル
	 * @param passMessage メッセージ
	 * @return パスワード
	 */
	public String showPasswordDialog(String title, String passMessage) {
		return showPasswordDialog(title, passMessage, mainFrame.frame);
	}

	/**
	 * パスワード入力要求ダイアログを表示します
	 * @param title タイトル
	 * @param passMessage メッセージ
	 * @param parentComponent 親コンポーネント
	 * @return パスワード
	 */
	private static String showPasswordDialog(String title, String passMessage, Component parentComponent) {

		// パスワード入力フィールド生成
		JPasswordField passwordField = new JPasswordField();

		Object[] message = { passMessage, passwordField };

		// JOptionPane生成
		JOptionPane optionPane = new JOptionPane(
				message,
				JOptionPane.PLAIN_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION);

		// ダイアログ生成
		JDialog dialog = optionPane.createDialog(parentComponent, title);

		// フォーカス設定
		dialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowOpened(WindowEvent e) {
				SwingUtilities.invokeLater(() -> {
					passwordField.requestFocusInWindow();
				});
			}

			@Override
			public void windowActivated(WindowEvent e) {
				SwingUtilities.invokeLater(() -> {
					passwordField.requestFocusInWindow();
				});
			}
		});

		// 返却用変数
		String pass = null;

		try {

			// ダイアログ表示
			dialog.setVisible(true);

			// 結果取得
			Object value = optionPane.getValue();

			if (value instanceof Integer input
					&& input.intValue() == JOptionPane.OK_OPTION) {
				char[] password = passwordField.getPassword();
				pass = new String(password);
			}

		} finally {
			// ダイアログ破棄
			dialog.dispose();
		}

		return pass;
	}

	/**
	 * パスワード入力要求ダイアログを表示します
	 * @return パスワード
	 */
	public static String showPasswordDialog() {
		String title = DialogHelperText.DialogHelper_PASS_TITLE.toString();
		String message = DialogHelperText.DialogHelper_PASS_MSG.toString();
		return showPasswordDialog(title, message, null);
	}

	/**
	 * システムエラーダイアログを表示します
	 */
	public static void showPasswordMistake() {
		JOptionPane.showMessageDialog(null, DialogHelperText.DialogHelper_PASS_MISS.toString(),
				DialogHelperText.DialogHelper_ERROR_MSG.toString(), JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * システムエラーダイアログを表示します
	 * @param message エラーメッセージ
	 */
	public static void showStaticSystemError(String message) {
		JOptionPane.showMessageDialog(null, message,
				DialogHelperText.DialogHelper_ERROR_MSG.toString(), JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * システム通知ダイアログを表示します
	 * @param message エラーメッセージ
	 */
	public void showSystemInfo(String message) {
		JOptionPane.showMessageDialog(mainFrame.frame, message,
				DialogHelperText.DialogHelper_INFO_MSG.toString(), JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * 基底フレーム返却します
	 * @return 基底フレーム
	 */
	public Frame getParent() {
		return mainFrame.frame;
	}

	/**
	 * 入力ダイアログを表示します
	 * @param title タイトル
	 * @param message メッセージ
	 * @return ユーザ入力文字列
	 */
	public String showInputtDialog(String title, String message) {
		// 返却用変数
		String result = null;
		// ダイアログ表示
		result = JOptionPane.showInputDialog(mainFrame.frame, message, title, JOptionPane.INFORMATION_MESSAGE);
		return result;
	}

	/**
	 * 入力ダイアログを表示します
	 * @param component 親コンポーネント
	 * @param title タイトル
	 * @param message メッセージ
	 * @return ユーザ入力文字列
	 */
	public String showInputtDialog(Component component, String title, String message) {
		// 返却用変数
		String result = null;
		// ダイアログ表示
		result = JOptionPane.showInputDialog(component, message, title, JOptionPane.INFORMATION_MESSAGE);
		return result;
	}

	/**
	 * カスタマイズ可能なダイアログを表示します<br/>
	 * ダイアログのサイズは600x400の固定帳です
	 * @param panel ダイアログ内部要素
	 * @param title ダイアログタイトル
	 */
	public void showCustomDialog(Component panel, String title) {
		// ダイアログ生成
		JDialog dialog = new JDialog(mainFrame.frame, title, true);
		// レイアウト設定
		dialog.setLayout(new BorderLayout());
		dialog.getContentPane().add(panel, BorderLayout.CENTER);
		dialog.setResizable(false);
		dialog.setSize(600, 400);
		dialog.setLocationRelativeTo(mainFrame.frame);
		dialog.setVisible(true);
	}

	/**
	 * カスタマイズ可能なダイアログを生成します<br/>
	 * ダイアログのサイズは600x400の固定帳です
	 * @param panel ダイアログ内部要素
	 * @param title ダイアログタイトル
	 * @return 生成されたダイアログ
	 */
	public JDialog createCustomDialog(Component panel, String title) {
		// ダイアログ生成
		JDialog dialog = new JDialog(mainFrame.frame, title, true);
		// レイアウト設定
		dialog.setLayout(new BorderLayout());
		dialog.getContentPane().add(panel, BorderLayout.CENTER);
		dialog.setResizable(false);
		dialog.setSize(600, 400);
		dialog.setLocationRelativeTo(mainFrame.frame);
		return dialog;
	}

	/**
	 * カスタマイズ可能なダイアログを表示します
	 * @param panel ダイアログ内部要素
	 * @param title ダイアログタイトル
	 * @param width 幅
	 * @param height 高さ
	 */
	public void showCustomDialog(
			Component panel,
			String title,
			int width,
			int height) {
		showCustomDialog(panel, title, width, height, true);
	}

	/**
	 * カスタマイズ可能なダイアログを表示します
	 * @param panel ダイアログ内部要素
	 * @param title ダイアログタイトル
	 * @param width 幅
	 * @param height 高さ
	 * @param isModal モーダルフラグ
	 */
	public void showCustomDialog(
			Component panel,
			String title,
			int width,
			int height,
			boolean isModal) {
		// ダイアログ生成
		JDialog dialog = new JDialog(mainFrame.frame, title, isModal);
		// レイアウト設定
		dialog.setLayout(new BorderLayout());
		dialog.getContentPane().add(panel, BorderLayout.CENTER);
		dialog.setSize(width, height);
		dialog.setLocationRelativeTo(mainFrame.frame);
		dialog.setModalityType(Dialog.ModalityType.DOCUMENT_MODAL);
		dialog.setVisible(true);
	}

	/**
	 * 進捗更新メッセージを出力します
	 * @param title ダイアログタイトル
	 * @return 進捗更新オブザーバー実装 
	 */
	public Consumer<Double> showProgressDialog(String title) {
		// ダイアログ生成
		JDialog dialog = new JDialog(mainFrame.frame, title, true);
		// ダイアログレイアウト指定
		dialog.setLayout(new BorderLayout());
		dialog.setResizable(false);
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		dialog.setSize(300, 100);
		dialog.setLocationRelativeTo(mainFrame.frame);
		// プログレスバー生成
		JProgressBar progressBar = new JProgressBar();
		progressBar.setMaximum(100);
		progressBar.setStringPainted(true);
		// コンポーネント配置配置
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(Box.createVerticalStrut(30));
		panel.add(progressBar);
		panel.add(Box.createVerticalStrut(30));
		dialog.add(panel, BorderLayout.CENTER);
		// オブザーバー生成
		class observer implements Consumer<Double> {

			// 表示フラグ
			final AtomicBoolean flug = new AtomicBoolean(true);

			@Override
			public void accept(Double t) {
				int progress = t.intValue();
				if (flug.get()) {
					flug.set(false);
					Thread.ofVirtual().start(() -> {
						dialog.setVisible(true);
					});
				}
				SwingUtilities.invokeLater(() -> {
					progressBar.setValue(progress);
					progressBar.setString(String.format("%d%%", progress));
					if (100 <= progress || progress == STOP_PROGRESS_DIALOG.intValue()) {
						dialog.setVisible(false);
					}
				});
			}
		}
		return new observer();
	}

}

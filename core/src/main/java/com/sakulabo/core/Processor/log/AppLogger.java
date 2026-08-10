package com.sakulabo.core.Processor.log;

import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.StringJoiner;
import java.util.logging.FileHandler;
import java.util.logging.Filter;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.sakulabo.core.Common.AppPathUtils;
import com.sakulabo.core.Common.StringUtils;
import com.sakulabo.core.Kagerow.KagerowApplication;
import com.sakulabo.core.Kagerow.KagerowApplication.Mode;
import com.sakulabo.core.Kagerow.Exception.ApplicationError;
import com.sakulabo.core.Kagerow.Utilities.KagerowAOPProcessor;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * アプリケーション共通で使用されるロガークラスです
 *
 * @author keeeeeent
 */
public final class AppLogger implements KagerowAOPProcessor, KagerowLogger {

	/** シングルトンインスタンス保持 */
	private volatile static AppLogger appLogger;

	/** GC回避のため強参照でインスタンスを保持 */
	private volatile static Logger logger;
	/** ログローテーション（10Mバイトごと） */
	private static final int LIMIT = 1024 * 1024 * 10;
	/** ログローテーション（ファイル保持数） */
	private static final int COUNT = 10;
	/** ログローテーション（ファイル名称プレフィックス） */
	private static final String NAME = "APP-LOGGER-%u-%g.log";
	/** ログメッセージ */
	private ResourceBundle message;
	/** ログメッセージリソース名 */
	public static final String RESOURCE_FILE_NAME = "config.message.app-logger";
	/** ログメッセージフォーマット */
	private final String logFormat = "[%s] %s.%s(%s)%s";
	/** ロガー名称 */
	private final String LOGGER_NAME = "AppLogger";

	/**
	 * デフォルトのロガークラスです
	 */
	@SuppressFBWarnings("ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
	private AppLogger() {

		// ロガーの初期化
		logger = Logger.getLogger(LOGGER_NAME);
		// ログレベル初期化
		logger.setLevel(Level.ALL);

		try {

			// メッセージファイル取得
			Optional<Module> module = ModuleLayer.boot().findModule(StringUtils.MODULE_NAME);
			message = ResourceBundle.getBundle(
					RESOURCE_FILE_NAME,
					module.get());

			// ロギングディレクトリパスを取得
			String logDir = AppPathUtils.createLogDirPath()
					.resolve(NAME)
					.toString();

			// ハンドラの追加
			Handler fileHandler = new FileHandler(logDir, LIMIT, COUNT, true);
			fileHandler.setLevel(Level.ALL);
			logger.addHandler(fileHandler);

			// フォーマッターの設定
			Formatter formatter = new AppFileFormatter(message);
			fileHandler.setFormatter(formatter);

			// コンソール設定
			logger.setUseParentHandlers(false);
			if (KagerowApplication.getConfig().LOG_CONSOLE()
					&& KagerowApplication.getApplicationMode() != Mode.CLI) {
				Formatter cFormatter = new AppConsoleFormatter(message);
				Handler consoleHandler = new AppConsoleHandler(cFormatter);
				consoleHandler.setLevel(Level.ALL);
				logger.addHandler(consoleHandler);
			}

			// フィルターの追加
			String level = KagerowApplication.getConfig().LOG_LEVEL();
			Filter filter = new AppLoggerFilter(level);
			logger.setFilter(filter);

		} catch (Exception e) {
			// ハンドラ取得失敗の場合、アプリケーションを終了
			throw new ApplicationError(e);
		}

	}

	/**
	 * ファクトリメソッド
	 * @return ロガーインスタンス
	 */
	public static AppLogger getLogger() {
		if (Objects.isNull(appLogger)) {
			synchronized (AppLogger.class) {
				if (Objects.isNull(appLogger)) {
					appLogger = new AppLogger();
				}
			}
		}
		return appLogger;
	}

	/** {@inheritDoc} */
	@Override
	public void log(Level level, String msg, Object[] param) {
		if (message.containsKey(msg)) {
			param = Objects.isNull(param) ? new Object[] {} : param;
			msg = message.getString(msg);
			msg = MessageFormat.format(msg, param);
		}
		logger.log(level, msg);
	}

	/** {@inheritDoc} */
	@Override
	public void err(Throwable error) {
		logger.log(Level.SEVERE, StringUtils.BLANK, error);
	}

	/** {@inheritDoc} */
	@Override
	public void start(Object raw, Object proxy, Method method, Object[] args) {
		// ログメッセージ生成
		String msg = createMsg("START", raw, null, method, args);
		logger.log(Level.INFO, msg);
	}

	/** {@inheritDoc} */
	@Override
	public void end(Object raw, Object result, Method method, Object[] args) {
		// ログメッセージ生成
		String msg = createMsg("END", raw, result, method, args);
		logger.log(Level.INFO, msg);
	}

	/** {@inheritDoc} */
	@Override
	public void error(Object raw, Throwable error, Method method, Object[] args) {
		// ログメッセージ生成
		String msg = createMsg("ERROR", raw, null, method, args);
		logger.log(Level.SEVERE, msg, error);
	}

	/**
	 * ロガー向けのメッセージを生成します
	 * @param prefix プレフィックス
	 * @param raw 生のインスタンス
	 * @param result メソッド実行結果
	 * @param method メソッド本体
	 * @param args メソッド引数
	 * @return メッセージ
	 */
	private String createMsg(String prefix, Object raw, Object result, Method method, Object[] args) {

		if (Objects.isNull(args)) {
			args = new Object[] { "null" };
		}

		StringJoiner joiner = new StringJoiner(" ");
		joiner.add(" ->");
		joiner.add(Objects.isNull(result) ? "null" : result.getClass().getCanonicalName());

		if (Objects.nonNull(result)) {
			joiner.add(Objects.toString(result));
		}

		String msg = String.format(logFormat,
				new Object[] {
						prefix,
						raw.getClass().getCanonicalName(),
						method.getName(),
						Arrays.asList(args).stream()
								.map(m -> m.getClass().getCanonicalName() + " " + m)
								.collect(Collectors.joining(",")),
						joiner.toString() });
		return msg;
	}

}

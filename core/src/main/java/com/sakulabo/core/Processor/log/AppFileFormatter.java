package com.sakulabo.core.Processor.log;

import static com.sakulabo.core.Common.StringUtils.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * アプリケーションロガー専用フォーマッター(ファイル)
 * 
 * @author keeeeeent
 */
final class AppFileFormatter extends Formatter {

	/** 時刻フォーマット */
	private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern(LONG_TIMESTAMP);
	/** ログメッセージ */
	private final ResourceBundle message;

	/**
	 * デフォルトコンストラクタ
	 * @param message リソースバンドル
	 */
	AppFileFormatter(ResourceBundle message) {
		this.message = message;
	}

	/** {@inheritDoc} */
	@Override
	public String format(LogRecord record) {

		// 通常メッセージ
		String msg = Objects.toString(record.getMessage());
		if (message.containsKey(msg)) {
			msg = message.getString(msg);
		}
		String console = """
				[%s] \
				<%s> --- %s
				""".formatted(
				record.getLevel(),
				formatter.format(LocalDateTime.ofInstant(record.getInstant(), ZoneId.systemDefault())),
				msg);

		// 例外メッセージ
		Throwable error = record.getThrown();
		// 例外ログの場合
		if (Objects.nonNull(error)) {
			// 例外フォーマット
			StringWriter writer = new StringWriter();
			PrintWriter print = new PrintWriter(writer);
			// 通常ログの書き込み
			print.println(console);
			// 例外書き込み
			error.printStackTrace(print);
			// 返却メッセージ上書き
			console = writer.toString();
		}

		return console;
	}

}

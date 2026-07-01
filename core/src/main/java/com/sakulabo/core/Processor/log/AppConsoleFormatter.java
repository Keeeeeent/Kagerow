package com.sakulabo.core.Processor.log;

import static com.sakulabo.core.Common.StringUtils.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * アプリケーションロガー専用フォーマッター(コンソール)
 * 
 * @author keeeeeent
 */
final class AppConsoleFormatter extends Formatter {

	/** 時刻フォーマット */
	private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern(LONG_TIMESTAMP);

	/** 編集対象正規表現 */
	private static final Pattern pattern = Pattern
			.compile("\\[(?<level>SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST)\\]");

	/** ログメッセージ */
	private final ResourceBundle message;

	/**
	 * デフォルトコンストラクタ
	 * @param message リソースバンドル
	 */
	AppConsoleFormatter(ResourceBundle message) {
		this.message = message;
	}

	/** {@inheritDoc} */
	@Override
	public String format(LogRecord record) {

		// 呼び出し元の取得
		String logClass = record.getSourceClassName();
		// スタックトレースから対象のロガー以前のトレースを切り出し
		List<StackTraceElement> stackTrace = Arrays.asList(Thread.currentThread().getStackTrace())
				.stream()
				.dropWhile(trace -> {

					String name = trace.getClassName();
					if (name.equals(logClass)) {
						return false;
					}

					return true;
				}).toList();
		// 呼び出し元のトレース情報を取得
		Optional<StackTraceElement> targetTrace = stackTrace.stream().skip(1).findFirst();
		// 呼び出し元クラス
		String callerClass = targetTrace.isEmpty() ? UNKNOWN : targetTrace.get().getClassName();
		String callerMethod = targetTrace.isEmpty() ? UNKNOWN : targetTrace.get().getMethodName();

		// 通常メッセージ

		// メッセージの取得
		String msg = Objects.toString(record.getMessage());

		// リソースバンドルの確認
		if (message.containsKey(msg)) {
			msg = message.getString(msg);
		}

		// タイムスタンプ生成
		String timestamp = formatter.format(LocalDateTime.ofInstant(record.getInstant(), ZoneId.systemDefault()));

		// スレッド名生成
		String threadName = Thread.currentThread().getName();

		// メッセージ生成
		String console = """
				[%s] \
				[%s] \
				[%s] \
				<%s#%s> \
				--> %s
				""".formatted(
				record.getLevel(),
				timestamp,
				threadName,
				callerClass,
				callerMethod,
				msg);

		// 着色
		console = replacement(console);

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

	/**
	 * コンソール出力に着色を施す加工をします<br/>
	 * 加工対象外の場合はなにもせず、引数で受け取った文字列をそのまま返却します
	 * @param msg 加工対象
	 * @return 加工結果
	 */
	private String replacement(String msg) {

		// レコードの編集
		Matcher matcher = pattern.matcher(msg);
		if (matcher.find()) {

			// 対象グループ取得
			final String target = matcher.group("level");

			// 置換文字列生成
			String reWord = switch (target) {
			case "FINEST" -> String.join(EMPTY, GRAY, target, END);
			case "FINER" -> String.join(EMPTY, PURPLE, target, END);
			case "FINE" -> String.join(EMPTY, PINK, target, END);
			case "CONFIG" -> String.join(EMPTY, GREEN, target, END);
			case "INFO" -> String.join(EMPTY, CYAN, target, END);
			case "WARNING" -> String.join(EMPTY, YELLOW, target, END);
			case "SEVERE" -> String.join(EMPTY, RED, target, END);
			default -> target;
			};

			// 文字列置換
			msg = msg.replace(target, reWord);
		}

		return msg;

	}

}

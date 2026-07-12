package com.sakulabo.application.app.rpc.filters;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.text.MessageFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.logging.Level;

import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;
import com.sakulabo.regulation.annotation.KagerowComponent;
import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;

/**
 * サーバログフィルター実装です
 *
 * @author keeeeeent
 */
@KagerowComponent
public class LoggerFilter extends Filter {

	/** 開始ログフォーマット */
	private final static MessageFormat START_LOG_FORMAT = new MessageFormat("""
			[RPC-Request] \
			session:{0} \
			protocol:{1} \
			ip:{2} \
			method:{3} \
			path:{4} \
			""");

	/** 終了ログフォーマット */
	private final static MessageFormat END_LOG_FORMAT = new MessageFormat("""
			[RPC-Response] \
			session:{0} \
			responseCode:{1} \
			duration:{2}ms \
			""");

	/** {@inheritDoc} */
	@Override
	public void doFilter(HttpExchange exchange, Chain chain) throws IOException {

		// セッションID払い出し
		UUID uuid = UUID.randomUUID();

		// 事前ログ
		InetSocketAddress ip = exchange.getRemoteAddress();
		String method = exchange.getRequestMethod();
		URI uri = exchange.getRequestURI();
		String protocol = exchange.getProtocol();

		// 開始時刻記録
		Instant start = Instant.now();

		// リクエストログ出力
		KagerowLogger.newAppLogger().log(Level.INFO,
				START_LOG_FORMAT.format(new Object[] { uuid, protocol, ip, method, uri }), new Object[0]);

		// 後続のフィルタへ伝搬
		chain.doFilter(exchange);

		// 終了時刻記録
		Instant end = Instant.now();

		// 事後ログ
		Duration duration = Duration.between(start, end);
		int responseCode = exchange.getResponseCode();

		// レスポンスログ出力
		KagerowLogger.newAppLogger().log(Level.INFO,
				END_LOG_FORMAT.format(new Object[] { uuid, responseCode, duration }), new Object[0]);

	}

	/** {@inheritDoc} */
	@Override
	public String description() {
		return getClass().getSimpleName();
	}

}

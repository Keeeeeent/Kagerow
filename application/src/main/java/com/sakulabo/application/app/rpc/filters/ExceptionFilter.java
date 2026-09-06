package com.sakulabo.application.app.rpc.filters;

import java.io.IOException;

import com.sakulabo.application.app.rpc.RpcExceptionHandler;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;
import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;

/**
 * 例外処理を実施するフィルター実装です
 *
 * @author keeeeeent
 */
public class ExceptionFilter extends Filter {

	/** {@inheritDoc} */
	@Override
	public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
		try (exchange) {
			try {
				// 後続のフィルタへ伝搬
				chain.doFilter(exchange);
			} catch (Throwable e) {
				// ロガー書き出し
				KagerowLogger.newAppLogger().err(e);
				// ハンドラー起動
				RpcExceptionHandler.handleException(exchange, e);
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public String description() {
		return getClass().getSimpleName();
	}

}

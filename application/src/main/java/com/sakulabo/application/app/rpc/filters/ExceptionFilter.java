package com.sakulabo.application.app.rpc.filters;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Objects;
import java.util.function.BiConsumer;

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
		try {
			// 後続のフィルタへ伝搬
			chain.doFilter(exchange);
		} catch (Throwable e) {
			// ロガー書き出し
			KagerowLogger.newAppLogger().err(e);
			// ハンドラー検索
			BiConsumer<Throwable, HttpExchange> handler = RpcExceptionHandler.getHandler(e.getClass());
			if (Objects.isNull(handler)) {
				// レスポンスをXMLへ変換（失敗時）
				long size = RpcExceptionHandler.createFalutResponseXML(exchange, e);
				// レスポンスコード設定
				exchange.sendResponseHeaders(HttpURLConnection.HTTP_INTERNAL_ERROR, size);
			} else {
				// ハンドラーが見つかった場合、ハンドラーに処理を委譲
				handler.accept(e, exchange);
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public String description() {
		return getClass().getSimpleName();
	}

}

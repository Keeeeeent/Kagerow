package com.sakulabo.application.app.rpc.filters;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;

import com.sakulabo.regulation.annotation.KagerowComponent;
import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;

/**
 * RPCリクエストを保証するフィルター実装です
 *
 * @author keeeeeent
 */
@KagerowComponent
public class RpcMethodFilter extends Filter {

	/**
	 * デフォルトコンストラクタ
	 */
	public RpcMethodFilter() {
		super();
	}

	/** {@inheritDoc} */
	@Override
	public void doFilter(HttpExchange exchange, Chain chain) throws IOException {

		// リクエストメソッドの確認
		String method = exchange.getRequestMethod();
		if (!"POST".equals(method)) {
			exchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_METHOD, 0);
			return;
		}
		// コンテンツタイプの確認
		List<String> list = exchange.getRequestHeaders().get("Content-Type");
		if (list.isEmpty()) {
			exchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_METHOD, 0);
			return;
		}
		String contentType = list.getFirst();
		if (!"text/xml".equals(contentType)) {
			exchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_METHOD, 0);
			return;
		}

		// 後続のフィルタへ伝搬
		chain.doFilter(exchange);

	}

	/** {@inheritDoc} */
	@Override
	public String description() {
		return getClass().getSimpleName();
	}

}

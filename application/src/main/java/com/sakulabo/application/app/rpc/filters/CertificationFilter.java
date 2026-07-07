package com.sakulabo.application.app.rpc.filters;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import com.sakulabo.application.app.rpc.exception.IllegalCertificationException;
import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

/**
 * サーバ認証フィルター実装です
 *
 * @author keeeeeent
 */
public class CertificationFilter extends Filter {

	/** 認証キー */
	public static final String AUTH_KEY = "Authorization";

	/** {@inheritDoc} */
	@Override
	public void doFilter(HttpExchange exchange, Chain chain) throws IOException {

		// 認証情報確認
		Headers headers = exchange.getRequestHeaders();
		List<String> authValue = headers.get(AUTH_KEY);

		// 認証情報が存在しない場合
		if (Objects.isNull(authValue) || authValue.isEmpty()) {
			throw new IllegalCertificationException("Authentication information not found");
		}

		// 認証情報がある場合検証を実施

		// 後続のフィルタへ伝搬
		chain.doFilter(exchange);

	}

	/** {@inheritDoc} */
	@Override
	public String description() {
		return getClass().getSimpleName();
	}

}

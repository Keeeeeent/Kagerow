package com.sakulabo.application.app.rpc.filters;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import com.sakulabo.application.app.rpc.exception.IllegalCertificationException;
import com.sakulabo.application.service.Rpc.AuthService;
import com.sakulabo.regulation.annotation.KagerowComponent;
import com.sakulabo.regulation.annotation.KagerowInject;
import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

/**
 * サーバ認証フィルター実装です
 *
 * @author keeeeeent
 */
@KagerowComponent
public class CertificationFilter extends Filter {

	/** 認証サービス */
	@KagerowInject
	private AuthService authService;
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
		String auth = authValue.getFirst();
		if (!auth.startsWith("Bearer ")) {
			// Bearer認証ではない場合
			throw new IllegalCertificationException("Invalid authentication method");
		} else {
			// 認証キー取得
			String authKey = auth.substring("Bearer ".length());
			// 認証キー検証
			if (!authService.verified(authKey)) {
				// 認証キーが不正、もしくは期限切れの場合
				throw new IllegalCertificationException("Invalid authentication credentials");
			}
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

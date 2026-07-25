package com.sakulabo.application.service.Rpc;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 認証情報管理サービスの規定インターフェイスです
 *
 * @author keeeeeent
 */
public interface AuthService {

	/**
	 * 排他制御ロック
	 */
	public static ReadWriteLock LOCK = new ReentrantReadWriteLock(true);

	/**
	 * 対象認証キーが認証済みか確認します
	 *
	 * @param authorization 認証トークン（Base64）
	 * @return 検証結果
	 */
	boolean verified(String authorization);

	/**
	 * チャレンジデータ構造
	 *
	 * @param nonce      チャレンジ向けノンス
	 * @param expiration 有効期限
	 */
	public static record Challenge(String nonce, LocalDateTime expiration) {
	}

	/**
	 * チャレンジデータ取得処理を実施します
	 *
	 * @param userName ユーザ名
	 * @return チャレンジデータ
	 */
	Optional<Challenge> nonce(String userName);

	/**
	 * チャレンジデータの検証を行います
	 * @param challenge チャレンジデータ（Base64）
	 * @return 認証トークン（Base64）
	 */
	Optional<String> challenge(String challenge);

	/**
	 * 認証ユーザ登録を行います
	 *
	 * @param userName ユーザ名
	 * @return シークレットキー
	 */
	Optional<String> regist(String userName);

}

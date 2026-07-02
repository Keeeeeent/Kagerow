package com.sakulabo.core.Kagerow.Spi;

import com.sakulabo.core.Kagerow.Adapter.KagerowDIContextAdapter;

/**
 * Kagerowアプリケーション起動実装を提供するSPIインターフェイスです
 *
 * @author keeeeeent
 */
public interface KagerowAutomaticStarter {

	/**
	 * Beanロード判定機能を提供するアダプターを返却します
	 *
	 * @return Beanロード判定機能を提供するアダプター
	 */
	KagerowDIContextAdapter getAdapter();

	/**
	 * セキュア起動に必要なパスワードを返却します
	 *
	 * @return セキュア起動に必要なパスワード
	 */
	String getPassword();

	/**
	 * パスワード入力がキャンセルされたか判定します
	 *
	 * @return 判定結果
	 */
	boolean isCancel();

	/**
	 * パスワードの検証に失敗した場合呼び出されます
	 */
	void mistake();

	/**
	 * パスワードの検証に成功した場合呼び出されます
	 */
	void success();

	/**
	 * エラーが発生した場合に呼び出されます
	 *
	 * @param e エラーインスタンス
	 */
	void unexpected(Throwable e);

}

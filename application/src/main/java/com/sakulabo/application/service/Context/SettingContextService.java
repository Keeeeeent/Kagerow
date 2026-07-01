package com.sakulabo.application.service.Context;

import java.util.List;
import java.util.UUID;

import javax.naming.NamingException;

import com.sakulabo.core.Kagerow.Contents.KagerowSettingContent;

/**
 * 設定コンテキストサービスの規定インターフェイスです
 * 
 * @author keeeeeent
 */
public interface SettingContextService {

	/**
	 * 指定した名前空間に関連づけられた設定一覧を取得します
	 * @param nameSpace 名前空間
	 * @return 設定一覧リスト
	 * @throws NamingException コンテキスト取得失敗
	 */
	List<String> getSettingList(String nameSpace) throws NamingException;

	/**
	 * コンテンツを取得します。<br/>
	 * コンテンツが見つからない場合新規で生成します
	 * @param nameSpace コンテンツ名前空間
	 * @return コンテンツ
	 * @throws NamingException コンテキスト取得失敗
	 */
	KagerowSettingContent getAndCreate(String nameSpace) throws NamingException;

	/**
	 * 指定した名前空間に関連づけられた設定を取得します
	 * @param nameSpace 名前空間
	 * @param propName 設定値キー
	 * @return 設定一覧リスト
	 * @throws NamingException コンテキスト取得失敗
	 */
	String getSetting(String nameSpace, String propName) throws NamingException;

	/**
	 * 指定したパスを設定から削除します
	 * @param path 削除パス
	 * @return 削除した設定のUUID
	 * @throws NamingException コンテキスト取得失敗
	 */
	UUID removePath(String path) throws NamingException;;

}

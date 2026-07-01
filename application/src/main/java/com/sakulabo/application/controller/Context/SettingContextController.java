package com.sakulabo.application.controller.Context;

import java.util.List;
import java.util.UUID;

import com.sakulabo.core.Kagerow.Contents.KagerowSettingContent;

/**
 * 設定コンテキストコントローラーの規定インターフェイスです
 * 
 * @author keeeeeent
 */
public interface SettingContextController {

	/**
	 * 指定した名前空間に関連づけられた設定一覧を取得します
	 * @param nameSpace 名前空間
	 * @return 設定一覧リスト
	 */
	List<String> getSettingList(String nameSpace);

	/**
	 * 指定した名前空間にコンテンツを取得します
	 * @param nameSpace 名前空間
	 * @return 設定一覧リスト
	 */
	KagerowSettingContent getSettingContent(String nameSpace);

	/**
	 * 指定した名前空間に関連づけられた設定を取得します
	 * @param nameSpace 名前空間
	 * @param propName 設定値キー
	 * @return 設定一覧リスト
	 */
	String getSetting(String nameSpace, String propName);

	/**
	 * 指定したパスを設定から削除します
	 * @param path 削除パス
	 * @return 削除した設定のUUID
	 */
	UUID removePath(String path);

}

package com.sakulabo.application.service.Data;

import com.sakulabo.application.model.Data.DataImportModel;

/**
 * データ一サービスの規定インターフェイスです
 * 
 * @author keeeeeent
 */
public interface DataService {

	/**
	 * データインポート処理を実行します
	 * @param model インポートデータモデル
	 * @throws Exception KDB構築失敗
	 */
	public void importData(DataImportModel model) throws Exception;

}

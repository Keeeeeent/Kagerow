package com.sakulabo.application.controller.Data;

import com.sakulabo.application.model.Data.DataImportModel;

/**
 * データ一コントローラーの規定インターフェイスです
 * 
 * @author keeeeeent
 */
public interface DataController {

	/**
	 * データインポート処理を実行します
	 * @param model インポートデータモデル
	 * @throws Exception KDB構築失敗
	 */
	public void importData(DataImportModel model) throws Exception;

}

package com.sakulabo.library.common;

import java.util.List;
import java.util.Optional;

import com.sakulabo.regulation.spi.PluginAdapter.KagerowRowSet;

/**
 * プラグイン処理対象データセット選択機能提供インターフェイス
 * 
 * @author keeeeeent
 */
public interface DataSelecter {

	/**
	 * 対象結果リストからIDを基準にデータを選択します
	 * @param id 選択対象データID
	 * @param data 選択対象データリスト
	 * @return 選択対象データ
	 */
	public default KagerowRowSet select(String id, List<KagerowRowSet> data) {
		Optional<KagerowRowSet> target = data.stream().filter(f -> f.id().equals(id)).findFirst();
		if (target.isEmpty()) {
			return null;
		} else {
			return target.get();
		}
	}

}

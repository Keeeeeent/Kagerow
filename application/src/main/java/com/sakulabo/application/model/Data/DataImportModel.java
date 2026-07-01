package com.sakulabo.application.model.Data;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.function.Consumer;

import com.sakulabo.application.model.BaseModel;
import com.sakulabo.core.Kagerow.Utilities.KagerowChunkCreater.ChunkCreateMode;

/**
 * データインポート処理モデルクラスです
 * 
 * @author keeeeeent
 */
public class DataImportModel extends BaseModel {

	/** セキュア実行フラグ */
	public volatile boolean isSecure;
	/** KagerowChunkCreater実行モード */
	public volatile ChunkCreateMode mode;
	/** 格納先スキーマ */
	public volatile String schema;
	/** インポートファイルパス */
	public volatile Path path;
	/** インポートファイル文字コード */
	public volatile Charset charset;
	/** インポートファイルヘッダーフラグ */
	public volatile boolean isHeader;
	/** インポートデータシノニム */
	public volatile String synonym;
	/** 進捗更新オブザーバー */
	public volatile Consumer<Double> observer = new Consumer<Double>() {
		@Override
		public void accept(Double t) {
			;
		}
	};
	
}

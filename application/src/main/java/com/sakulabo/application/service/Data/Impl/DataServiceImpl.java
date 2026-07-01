package com.sakulabo.application.service.Data.Impl;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.function.Consumer;

import com.sakulabo.application.model.Data.DataImportModel;
import com.sakulabo.application.service.BaseService;
import com.sakulabo.application.service.Data.DataService;
import com.sakulabo.core.Kagerow.Utilities.KagerowChunkCreater.ChunkCreateMode;
import com.sakulabo.core.Kagerow.Utilities.KagerowVirtualFileCreater;
import com.sakulabo.regulation.annotation.KagerowComponent;

/**
 * データ一サービスの実装クラスです
 * 
 * @author keeeeeent
 */
@KagerowComponent
public class DataServiceImpl extends BaseService implements DataService {

	/** {@inheritDoc}  */
	@Override
	public void importData(DataImportModel model) throws Exception {

		// 引数初期化
		ChunkCreateMode mode = model.mode;
		String schema = model.schema;
		Path path = model.path;
		Charset charset = model.charset;
		boolean isHeader = model.isHeader;
		String synonym = model.synonym;
		boolean isSecure = model.isSecure;
		Consumer<Double> observer = model.observer;

		// インポート実行
		KagerowVirtualFileCreater.constructionKDB(mode, schema, path, charset, isHeader, synonym, isSecure, observer);

	}

}

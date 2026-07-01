package com.sakulabo.application.controller.Data.Impl;

import com.sakulabo.application.controller.BaseController;
import com.sakulabo.application.controller.Data.DataController;
import com.sakulabo.application.model.Data.DataImportModel;
import com.sakulabo.application.service.Data.DataService;
import com.sakulabo.regulation.annotation.KagerowComponent;
import com.sakulabo.regulation.annotation.KagerowInject;

/**
 * データ一コントローラの実装クラスです
 * 
 * @author keeeeeent
 */
@KagerowComponent
public class DataControllerImpl extends BaseController implements DataController {

	/** データサービス */
	@KagerowInject
	private DataService dataService;

	/** {@inheritDoc} */
	@Override
	public void importData(DataImportModel model) throws Exception {
		dataService.importData(model);
	}

}

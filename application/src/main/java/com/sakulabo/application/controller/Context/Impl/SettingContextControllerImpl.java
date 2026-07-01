package com.sakulabo.application.controller.Context.Impl;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.naming.NamingException;

import com.sakulabo.application.controller.BaseController;
import com.sakulabo.application.controller.Context.SettingContextController;
import com.sakulabo.application.service.Context.SettingContextService;
import com.sakulabo.core.Kagerow.Contents.KagerowSettingContent;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;
import com.sakulabo.regulation.annotation.KagerowComponent;
import com.sakulabo.regulation.annotation.KagerowInject;

/**
 * 設定コンテキストコントローラーの実装クラスです
 * 
 * @author keeeeeent
 */
@KagerowComponent
public class SettingContextControllerImpl extends BaseController implements SettingContextController {

	/** プラグインコンテキストサービス */
	@KagerowInject
	private SettingContextService settingContextService;
	/** ロガー */
	@KagerowInject
	private KagerowLogger logger;

	/** {@inheritDoc} */
	@Override
	public List<String> getSettingList(String nameSpace) {
		// 返却用リスト初期化
		List<String> resultList = Collections.emptyList();
		try {
			resultList = settingContextService.getSettingList(nameSpace);
		} catch (NamingException e) {
			logger.err(e);
		}
		return resultList;
	}

	/** {@inheritDoc} */
	@Override
	public String getSetting(String nameSpace, String propName) {
		// 返却用変数初期化
		String result = null;
		try {
			result = settingContextService.getSetting(nameSpace, propName);
		} catch (NamingException e) {
			logger.err(e);
		}
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public KagerowSettingContent getSettingContent(String nameSpace) {
		KagerowSettingContent result = null;
		try {
			result = settingContextService.getAndCreate(nameSpace);
		} catch (NamingException e) {
			logger.err(e);
		}
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public UUID removePath(String path) {
		UUID result = null;
		try {
			result = settingContextService.removePath(path);
		} catch (NamingException e) {
			logger.err(e);
		}
		return result;
	}

}

package com.sakulabo.application.service;

import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;
import com.sakulabo.regulation.annotation.KagerowInject;

/**
 * アプリケーションのサービス基底クラスです
 * 
 * @author keeeeeent
 */
public abstract class BaseService {

	/** 共通ロガー */
	@KagerowInject
	protected KagerowLogger logger;

}

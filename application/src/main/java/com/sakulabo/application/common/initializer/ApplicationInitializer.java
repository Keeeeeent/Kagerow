package com.sakulabo.application.common.initializer;

import com.sakulabo.application.common.spi.ViewRunner;
import com.sakulabo.regulation.annotation.KagerowComponent;
import com.sakulabo.regulation.annotation.KagerowComponent.AppInitComponet;
import com.sakulabo.regulation.annotation.KagerowInject;

/**
 * アプリケーションを起動するエントリークラスです
 * 
 * @author keeeeeent
 */
@KagerowComponent
public class ApplicationInitializer implements AppInitComponet {

	/** アプリケーションメインフレーム */
	@KagerowInject
	private ViewRunner mainFrame;

	/** {@inheritDoc} */
	@Override
	public void initialize() {
		mainFrame.start();
	}

}

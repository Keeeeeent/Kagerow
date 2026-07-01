package com.sakulabo.core.Provides;

import com.sakulabo.core.Provides.LoardDIBeansProviderTest.LoardDIBeansProviderMockIF;
import com.sakulabo.regulation.annotation.KagerowComponent;
import com.sakulabo.regulation.annotation.KagerowInject;

@KagerowComponent("mock")
@SuppressWarnings("javadoc")
public class LoardDIBeansProviderInject implements LoardDIBeansProviderMockIF {

	@KagerowInject
	private LoardDIBeansProviderMockIF target1;

	@Override
	public String Test() {
		return target1.Test();
	}
	
	@Override
	public String Error(String args) {
		return target1.Error(args);
	}

}

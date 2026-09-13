package com.sakulabo.core.Provides;

import com.sakulabo.core.Provides.LoardDIBeansProviderTest.LoardDIBeansProviderMockIF;
import com.sakulabo.regulation.annotation.KagerowComponent;
import com.sakulabo.regulation.annotation.KagerowInject;

@KagerowComponent("mock")
public class LoardDIBeansProviderMockFail implements LoardDIBeansProviderMockIF {

	@KagerowInject
	private LoardDIBeansProviderMock target1;

	@Override
	public String Test() {
		return null;
	}

}

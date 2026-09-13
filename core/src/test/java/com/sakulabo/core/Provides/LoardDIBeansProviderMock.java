package com.sakulabo.core.Provides;

import com.sakulabo.core.Provides.LoardDIBeansProviderTest.LoardDIBeansProviderMockIF;
import com.sakulabo.regulation.annotation.KagerowComponent;

@KagerowComponent
public class LoardDIBeansProviderMock implements LoardDIBeansProviderMockIF {

	@Override
	public String Test() {
		return "test";
	}

}

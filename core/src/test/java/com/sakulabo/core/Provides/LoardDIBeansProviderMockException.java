package com.sakulabo.core.Provides;

import com.sakulabo.core.Provides.LoardDIBeansProviderTest.LoardDIBeansProviderMockIF;
import com.sakulabo.regulation.annotation.KagerowComponent;

@KagerowComponent
@SuppressWarnings("javadoc")
public class LoardDIBeansProviderMockException implements LoardDIBeansProviderMockIF {

	public LoardDIBeansProviderMockException() {
		throw new RuntimeException();
	}

	@Override
	public String Test() {
		return null;
	}

}

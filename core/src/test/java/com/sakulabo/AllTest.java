package com.sakulabo;

import org.junit.platform.suite.api.ExcludeClassNamePatterns;
import org.junit.platform.suite.api.ExcludePackages;
import org.junit.platform.suite.api.IncludePackages;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectPackages("com.sakulabo.core")
@IncludePackages({
		"com.sakulabo.core.Provides",
		"com.sakulabo.core.Processor.aop",
		"com.sakulabo.core.Processor.plugin",
		"com.sakulabo.core.Processor.archive",
		"com.sakulabo.core.Processor.database"
})
@ExcludePackages({
})
//@SelectClasses({
//})
@ExcludeClassNamePatterns({
//"com.sakulabo.core.Provides.LoardDIBeansProviderTest"
})
@SuppressWarnings("javadoc")
public class AllTest {

}

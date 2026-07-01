import com.sakulabo.application.common.initializer.ApplicationInitializer;
import com.sakulabo.regulation.annotation.KagerowSpiModule;

/**
 * GUIアプリケーションモジュールです
 * 
 * @provides com.sakulabo.core.Kagerow.Spi.KagerowAutomaticStarter
 * 
 * @author keeeeeent
 */

@KagerowSpiModule(ApplicationInitializer.class) open module com.sakulabo.application {

	// KagerowGUIパッケージ（エントリ）
	exports com.sakulabo.application;
	
	// KagerowGUI更改パッケージ
	exports com.sakulabo.application.common.spi;

	// KagerowGUI依存パッケージ
	requires com.sakulabo.core;
	requires java.management;
	requires jdk.management;

	// KagerowSPI
	provides com.sakulabo.core.Kagerow.Spi.KagerowAutomaticStarter
			with com.sakulabo.application.common.provider.AutomaticStarterProvider;

}
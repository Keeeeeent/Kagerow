import com.sakulabo.application.common.initializer.ApplicationInitializer;
import com.sakulabo.regulation.annotation.KagerowSpiModule;

/**
 * GUIアプリケーションモジュールです
 *
 * @uses com.sakulabo.application.common.spi.RpcTarget;
 * @uses com.sakulabo.application.common.spi.ViewRunner;
 * @provides com.sakulabo.core.Kagerow.Spi.KagerowAutomaticStarter
 * @provides com.sakulabo.application.common.spi.RpcTarget
 * @author keeeeeent
 */

@KagerowSpiModule(ApplicationInitializer.class) open module com.sakulabo.application {

	// KagerowGUIパッケージ（エントリ）
	exports com.sakulabo.application;

	// KagerowGUI公開パッケージ
	exports com.sakulabo.application.common.spi;

	// KagerowGUI依存パッケージ
	requires com.sakulabo.core;
	requires java.management;
	requires jdk.management;

	// KagerowRPC依存パッケージ
	requires jdk.httpserver;
	requires java.xml;
	requires java.compiler;
	requires info.picocli;

	// KagerowRPC
	uses com.sakulabo.application.common.spi.RpcTarget;
	uses com.sakulabo.application.common.spi.ViewRunner;

	provides com.sakulabo.application.common.spi.RpcTarget
			with com.sakulabo.application.controller.Rpc.Impl.DataImportControllerImpl,
			com.sakulabo.application.controller.Rpc.Impl.AuthControllerImpl;

	// KagerowSPI
	provides com.sakulabo.core.Kagerow.Spi.KagerowAutomaticStarter
			with com.sakulabo.application.common.provider.AutomaticStarterProvider;

}
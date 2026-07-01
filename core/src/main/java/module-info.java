/**
 * Kagerowコアモジュールです
 * 
 * @uses com.sakulabo.regulation.spi.InitDIBeansProcessorAdapter
 * @uses com.sakulabo.regulation.spi.PluginAdapter
 * @uses com.sakulabo.regulation.spi.LoardDIBeansAdapter
 * @uses com.sakulabo.core.Kagerow.Spi.KagerowAutomaticStarter
 * 
 * @provides javax.annotation.processing.Processor
 * @provides java.nio.file.spi.FileSystemProvider
 * @provides javax.naming.spi.InitialContextFactory
 * @provides com.sakulabo.regulation.spi.LoardDIBeansAdapter
 * @provides java.nio.file.spi.FileTypeDetector
 * 
 * @author keeeeeent
 */
@SuppressWarnings("rawtypes") module com.sakulabo.core {

	// 公開パッケージ(core)
	exports com.sakulabo.core.Kagerow;
	exports com.sakulabo.core.Kagerow.Spi;
	exports com.sakulabo.core.Kagerow.Adapter;
	exports com.sakulabo.core.Kagerow.Context;
	exports com.sakulabo.core.Kagerow.Contents;
	exports com.sakulabo.core.Kagerow.Utilities;
	exports com.sakulabo.core.Kagerow.Exception;

	// 公開パッケージ(regulation)
	exports com.sakulabo.regulation.spi;
	exports com.sakulabo.regulation.annotation;

	// 公開パッケージ(launcher)
	exports com.sakulabo.launcher to java.instrument;

	// 公開パッケージ(builder)
	exports com.sakulabo.builder;

	// リフレクション
	opens com.sakulabo.core.Provides to java.base;
	opens com.sakulabo.launcher to java.instrument;

	// リフレクション（JMX）
	opens com.sakulabo.core.Processor.jmx;
	opens com.sakulabo.core.Processor.jmx.Configuration;
	opens com.sakulabo.core.Processor.jmx.ExecutionPlan;
	opens com.sakulabo.core.Processor.jmx.Context;

	// 必須パッケージ
	requires transitive java.naming;
	requires transitive java.logging;
	requires transitive java.sql;
	requires transitive java.sql.rowset;
	requires transitive java.desktop;
	requires jdk.zipfs;
	requires java.xml;
	requires jdk.charsets;

	// 必須パッケージ（JMX）
	requires java.management;
	requires jdk.management.agent;
	requires java.management.rmi;
	requires java.rmi;
	requires jdk.attach;

	// 必須パッケージ（APT）
	requires java.compiler;
	requires jdk.compiler;
	requires java.instrument;

	// 必須パッケージ（サードパーティ）※ランタイム不要
	requires static com.github.spotbugs.annotations;

	// 必須パッケージ（サードパーティ）
	requires com.h2database;

	// SPI
	uses com.sakulabo.regulation.spi.InitDIBeansProcessorAdapter;
	uses com.sakulabo.regulation.spi.PluginAdapter;
	uses com.sakulabo.regulation.spi.LoardDIBeansAdapter;
	uses com.sakulabo.core.Kagerow.Spi.KagerowAutomaticStarter;

	// プロバイダー（APT）
	provides javax.annotation.processing.Processor with
			com.sakulabo.regulation.annotation.processor.AppComponentProcessor,
			com.sakulabo.regulation.annotation.processor.AppLoggerProcessor,
			com.sakulabo.regulation.annotation.processor.UseJITCompilerProcessor,
			com.sakulabo.regulation.annotation.processor.AppPluginProcessor;

	// プロバイダー（nio）
	provides java.nio.file.spi.FileSystemProvider
			with com.sakulabo.core.Provides.ArchiveSystemProvider;
	provides java.nio.file.spi.FileTypeDetector
			with com.sakulabo.core.Provides.KagerowScriptFileTypeDetector;

	// プロバイダー（naming）
	provides javax.naming.spi.InitialContextFactory
			with com.sakulabo.core.Provides.InitialContextFactoryProvider;

	// プロバイダー（DIアダプター）
	provides com.sakulabo.regulation.spi.LoardDIBeansAdapter
			with com.sakulabo.core.Provides.LoardDIBeansProvider;

}
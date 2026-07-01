package com.sakulabo.regulation.annotation.processor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import com.sakulabo.regulation.annotation.KagerowPlugin;
import com.sakulabo.regulation.annotation.KagerowPluginSpiModule;
import com.sakulabo.regulation.annotation.utils.ChainProcessor;
import com.sakulabo.regulation.spi.PluginAdapter;

/**
 * KagerowPluginを処理するアノテーションプロセッサーです。
 * @author keeeeeent
 */
@SupportedAnnotationTypes("*")
public class AppPluginProcessor extends AbstractProcessor {

	/** 要素操作ユーティリティ */
	private Elements elementUtils;
	/** 型操作ユーティリティ */
	private Types typeUtils;
	/** 確認対象インターフェイス */
	private TypeMirror targetInterfaceType;
	/** モジュールエレメント */
	private static Element module;

	/** アノテーションプロセッサーチェーン */
	private static final String[] processList = new String[] {
			"com.sakulabo.regulation.annotation.processor.AppPluginChainProcessor"
	};

	/** アノテーションプロセッサーチェーン添字（AppComponentChainProcessor） */
	private static final int APP_PLUGIN_CHAIN_PROCESSOR_INDEX = 0;
	/** 設定ファイル名称 */
	public static final String SETTING_FILE_NAME = "plugin-setting.properties";
	/** 設定ファイル読み取りキー（モジュール名称） */
	public final static String SETTING_FILE_KEY_MODULE_NAME = "MODULE_NAME";
	/** 設定ファイル読み取りキー（プラグインパッケージ名称） */
	public final static String SETTING_FILE_KEY_PLUGIN_PKG_NAME = "PLUGIN_PKG_NAME";
	/** 設定ファイル読み取りキー（メジャーバージョン番号） */
	public final static String MAJOR_VERSION = "MAJOR_VERSION";
	/** 設定ファイル読み取りキー（マイナーバージョン番号） */
	public final static String MINOR_VERSION = "MINOR_VERSION";
	/** 設定ファイル読み取りキー（パッチバージョン番号） */
	public final static String PATCH_VERSION = "PATCH_VERSION";

	/** {@inheritDoc} */
	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latestSupported();
	}

	/** {@inheritDoc} */
	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		this.elementUtils = processingEnv.getElementUtils();
		this.typeUtils = processingEnv.getTypeUtils();
		TypeElement targetInterfaceElement = elementUtils.getTypeElement(PluginAdapter.class.getName());
		this.targetInterfaceType = targetInterfaceElement.asType();
	}

	/** {@inheritDoc} */
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

		try {

			// プラグインリスト生成
			final List<String> pluginList = new ArrayList<>();
			// クラスリスト生成
			final Set<String> classList = new HashSet<>();

			for (Element target : roundEnv.getElementsAnnotatedWith(KagerowPlugin.class)) {

				if (target.getKind() == ElementKind.CLASS) {

					// アノテーション情報取得
					KagerowPlugin pluginInfo = target.getAnnotation(KagerowPlugin.class);
					String pluginName = pluginInfo.name();
					if (pluginList.contains(pluginName)) {
						// プラグイン名称が重複している場合
						processingEnv.getMessager().printMessage(Kind.ERROR, pluginName + " は既に宣言されています", target);
						return false;
					}
					// プラグインリストに追加
					pluginList.add(pluginName);

					// クラス定義にインターフェイスを実装が含まれているか確認
					TypeElement classElement = (TypeElement) target;
					Optional<? extends TypeMirror> inf = classElement.getInterfaces().stream()
							.filter(f -> typeUtils.isSameType(f, targetInterfaceType))
							.findFirst();
					if (inf.isEmpty()) {
						// インターフェイスを実装していない場合、コンパイルエラーとする
						processingEnv.getMessager().printMessage(Kind.ERROR, "PluginAdapterを実装してください", target);
					}
					// クラスリストに追加
					classList.add(target.toString());
				}

			}

			// プラグインが存在する場合のみ処理
			if (!classList.isEmpty()) {
				// リソースファイル生成
				createResource(classList);
			}
			// 全てのクラスの中からmodule-info.javaを取得
			for (Element element : roundEnv.getElementsAnnotatedWith(KagerowPluginSpiModule.class)) {
				// 1番最後に見つかったモジュールを採用する
				module = element;
			}
			// モジュールが見つかった場合
			if (Objects.nonNull(module)) {
				// AppComponentChainProcessorを取得
				Class<?> clazz = Class.forName(processList[APP_PLUGIN_CHAIN_PROCESSOR_INDEX]);
				// チェーンプロセッサーを生成
				ChainProcessor chainProcessor = (ChainProcessor) clazz.getDeclaredConstructor(Element.class)
						.newInstance(module);
				// 連鎖コンパイル実行
				chainProcessor.process(annotations, roundEnv, processingEnv, classList);
				// 設定ファイル生成実行
				if (!roundEnv.processingOver()) {
					createSettingFile();
				}
			}

		} catch (Throwable e) {
			processingEnv.getMessager().printMessage(Kind.ERROR, e.getMessage());
		}

		// プロセッサー処理を継続
		return false;
	}

	/**
	 * リソースファイルを出力します
	 * @param className 生成するクラス完全修飾名称
	 * @throws IOException リソース出力失敗した場合
	 */
	private void createResource(Set<String> className) throws IOException {

		// プロバイダー一覧
		Set<String> providerList = new HashSet<>();
		// 今回コンパイルするプロバイダーを追加
		providerList.addAll(className);

		// 読み込みリソースオブジェクト生成
		FileObject resourceFile = processingEnv.getFiler().getResource(
				StandardLocation.CLASS_OUTPUT,
				"",
				createResourcePath().concat(".tmp"));

		// 既存ファイルの内容取得
		try (InputStream input = resourceFile.openInputStream();
				Reader reader = new InputStreamReader(input, StandardCharsets.UTF_8);
				BufferedReader fileReader = new BufferedReader(reader);) {
			String line;
			while ((line = fileReader.readLine()) != null) {
				providerList.add(line);
			}
		} catch (IOException e) {
			// ファイルがない場合は無視（初回作成）
		}

		// 書き込みリソースオブジェクト生成
		resourceFile = processingEnv.getFiler().createResource(
				StandardLocation.CLASS_OUTPUT,
				"",
				createResourcePath());

		// ファイル生成
		try (Writer writer = resourceFile.openWriter();
				BufferedWriter fileWriter = new BufferedWriter(writer);) {
			for (String provider : providerList) {
				// リソース生成
				fileWriter.append(provider);
				fileWriter.newLine();
				// ログ出力
				resourceLogMsg(provider);
			}
		}

	}

	/**
	 * プラグインロードローダー向けの設定ファイルを生成します
	 * @throws IOException 設定ファイル生成失敗
	 */
	private void createSettingFile() throws IOException {
		// モジュール名称を取得
		ModuleElement moduleElement = (ModuleElement) module;
		String moduleName = moduleElement.getQualifiedName().toString();
		// プラグインパッケージ名を取得
		KagerowPluginSpiModule ano = moduleElement.getAnnotation(KagerowPluginSpiModule.class);
		String pluginPkgName = ano.value();
		String majorVersion = String.valueOf(ano.majorVersion());
		String minorVersion = String.valueOf(ano.minorVersion());
		String patchVersion = String.valueOf(ano.patchVersion());
		// 書き込みリソースオブジェクト生成
		FileObject resourceFile = processingEnv.getFiler().createResource(
				StandardLocation.SOURCE_OUTPUT,
				"",
				SETTING_FILE_NAME);
		try (OutputStream output = resourceFile.openOutputStream()) {
			Properties prop = new Properties();
			prop.put(SETTING_FILE_KEY_MODULE_NAME, moduleName);
			prop.put(SETTING_FILE_KEY_PLUGIN_PKG_NAME, pluginPkgName);
			prop.put(MAJOR_VERSION, majorVersion);
			prop.put(MINOR_VERSION, minorVersion);
			prop.put(PATCH_VERSION, patchVersion);
			prop.store(output, "");
		}
		settingLogMsg(moduleName);
	}

	/**
	 * リソースパスを返却します
	 * @return リソースパス文字列表現
	 */
	private String createResourcePath() {
		String spi = PluginAdapter.class.getName();
		String dir = "META-INF/services/";
		return dir.concat(spi);
	}

	/**
	 * リソースにプロバイダーを追加した時のメッセージを出力します
	 * @param className 生成するクラス完全修飾名称
	 */
	private void resourceLogMsg(String className) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("[[1;34mINFO[m] ");
		buffer.append("Add ProviderClass ");
		buffer.append("[0;33m");
		buffer.append(className);
		buffer.append("[m [1m");
		buffer.append(" In Resource " + createResourcePath());
		System.out.println(new String(buffer));
	}

	/**
	 * 設定ファイルを生成した時のメッセージを出力します
	 * @param settingFile 生成する設定ファイル
	 */
	private void settingLogMsg(String settingFile) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("[[1;36mINFO[m] ");
		buffer.append("Create SettingFile ");
		buffer.append("[0;33m");
		buffer.append(settingFile);
		buffer.append("[m [1m");
		System.out.println(new String(buffer));
	}

}

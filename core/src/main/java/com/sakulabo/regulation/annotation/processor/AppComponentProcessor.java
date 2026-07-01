package com.sakulabo.regulation.annotation.processor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.sakulabo.regulation.annotation.KagerowComponent;
import com.sakulabo.regulation.annotation.KagerowSpiModule;
import com.sakulabo.regulation.annotation.utils.ChainProcessor;
import com.sakulabo.regulation.spi.InitDIBeansProcessorAdapter;

/**
 * KagerowComponentを処理するアノテーションプロセッサーです。
 * 
 * @author keeeeeent
 */
@SupportedAnnotationTypes("*")
public final class AppComponentProcessor extends AbstractProcessor {

	/** クラス名のプレフィックス */
	private final static String CLASS_NAME_PREFIX = "__$Kagerow$__";
	/** 日付フォーマット */
	private final static DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
	/** ドット（文字列表現） */
	private final static String DOT = ".";
	/** ドット（正規表現） */
	private final static String DOT_REG = "\\.";
	/** モジュールエレメント */
	private static Element module;

	/** アノテーションプロセッサーチェーン */
	private static final String[] processList = new String[] {
			"com.sakulabo.regulation.annotation.processor.AppComponentChainProcessor"
	};
	/** アノテーションプロセッサーチェーン添字（AppComponentChainProcessor） */
	private static final int APP_COMPONET_CHAIN_PROCESSOR_INDEX = 0;

	/** {@inheritDoc} */
	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latestSupported();
	}

	/** {@inheritDoc} */
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

		try {

			// クラスの自動生成
			Set<String> className = new HashSet<>();
			for (Element element : roundEnv.getElementsAnnotatedWith(KagerowComponent.class)) {
				// 通常のクラスではない場合コンパイルエラー
				if (element.getEnclosingElement().getKind() == ElementKind.CLASS
						|| element.getEnclosingElement().getKind() == ElementKind.INTERFACE) {
					processingEnv.getMessager().printMessage(Kind.ERROR, "エンクロージングクラスでのみ指定可能です", element);
				}
				// クラスではない場合コンパイルエラー
				if (element.getKind() != ElementKind.CLASS) {
					processingEnv.getMessager().printMessage(Kind.ERROR, "クラスでのみ指定可能です", element);
				}
				// デフォルトコンストラクタの有無確認
				for (Element target : element.getEnclosedElements()) {
					// メソッド、コンストラクタの要素か検証
					if (target instanceof ExecutableElement executableElement) {
						// publicなコンストラクタを対象とする
						if (executableElement.getKind() == ElementKind.CONSTRUCTOR) {
							if (executableElement.getParameters().isEmpty()
									&& executableElement.getModifiers().contains(Modifier.PUBLIC)) {
								// 初期化クラスを自動生成
								String name = create(element);
								className.add(name);
							} else {
								// デフォルトコンストラクタが見つからない場合
								processingEnv.getMessager().printMessage(Kind.ERROR, "デフォルトコンストラクタは必須です", element);
							}
						}
					}
				}
			}
			// クラスの生成がされなかった場合、処理を終了
			if (!className.isEmpty()) {
				// リソースファイル生成
				createResource(className);
			}
			// 全てのクラスの中からmodule-info.javaを取得
			for (Element element : roundEnv.getElementsAnnotatedWith(KagerowSpiModule.class)) {
				// 1番最後に見つかったモジュールを採用する
				module = element;
			}
			// モジュールが見つかった場合
			if (Objects.nonNull(module)) {
				// XML解析
				validXML(module);
				// AppComponentChainProcessorを取得
				Class<?> clazz = Class.forName(processList[APP_COMPONET_CHAIN_PROCESSOR_INDEX]);
				// チェーンプロセッサーを生成
				ChainProcessor chainProcessor = (ChainProcessor) clazz.getDeclaredConstructor(Element.class)
						.newInstance(module);
				// 連鎖コンパイル実行
				chainProcessor.process(annotations, roundEnv, processingEnv, className);
			}
		} catch (Throwable e) {
			processingEnv.getMessager().printMessage(Kind.ERROR, e.getMessage());
		}

		// プロセッサー処理を継続
		return false;
	}

	/**
	 * ソースファイルを自動生成します
	 * @param element 対象要素
	 * @return 生成したクラスの正式名称
	 * @throws IOException ソースファイル生成失敗
	 */
	private String create(Element element) throws IOException {
		// 自動生成のクラス名生成
		String baseName = Objects.requireNonNull(element.asType()).toString();
		String className = element.getSimpleName().toString().concat(CLASS_NAME_PREFIX);
		String pakageName = Arrays.stream(baseName.split(DOT_REG, -1))
				.takeWhile(t -> !t.equals(element.getSimpleName().toString()))
				.collect(Collectors.joining(DOT));
		String fileName = String.join(DOT, pakageName, className);
		// ソースファイル生成
		createSource(baseName, className, pakageName, fileName);
		return element.toString().concat(CLASS_NAME_PREFIX);
	}

	/**
	 * ソースファイルを出力します
	 * @param baseName 生成の雛形となるクラス完全修飾名称
	 * @param className 生成するクラス完全修飾名称
	 * @param pakageName 生成するパッケージ
	 * @param fileName 生成先の完全なパス
	 * @throws IOException ソースファイル生成失敗
	 */
	private void createSource(String baseName, String className, String pakageName, String fileName)
			throws IOException {
		// クラスオブジェクト生成
		JavaFileObject javaFile = processingEnv.getFiler().createSourceFile(fileName);
		try (Writer writer = javaFile.openWriter()) {
			// ソースコード生成
			String classBody = """
					package %s;

					import com.sakulabo.regulation.spi.InitDIBeansProcessorAdapter;

					%s
					@SuppressWarnings("all")
					public final class %s implements InitDIBeansProcessorAdapter {

						/** {@inheritDoc} */
						@Override
						public Class<?> init() {
							try {
								return Class.forName("%s");
							} catch (ClassNotFoundException e) {
								e.printStackTrace();
								return null;
							}
						}

					}
					""".formatted(pakageName, createStamp(), className, baseName);
			// ソースファイルに書き込み
			writer.write(classBody);
			// ログ出力
			sourceLogMsg(className);
		}
	}

	/**
	 * ソースファイルに付与する自動生成のコメントです
	 * @return 自動生成されたコメント
	 */
	private String createStamp() {
		String annotation = """
				/**
				 * Kagerowによって自動生成されました<br>
				 *
				 * @author %s<br>
				 * Generated At %s
				 */"""
				.formatted(getClass().getCanonicalName(), FORMATTER.format(LocalDateTime.now()));
		return annotation;
	}

	/**
	 * リソースにプロバイダーを追加した時のメッセージを出力します
	 * @param className 生成するクラス完全修飾名称
	 */
	private void sourceLogMsg(String className) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("[[1;34mINFO[m] ");
		buffer.append("Generated by APT ");
		buffer.append("[0;35m");
		buffer.append(className);
		buffer.append("[m [1m");
		System.out.println(new String(buffer));
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
	 * リソースパスを返却します
	 * @return リソースパス文字列表現
	 */
	private String createResourcePath() {
		String spi = InitDIBeansProcessorAdapter.class.getName();
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
		buffer.append("[0;35m");
		buffer.append(className);
		buffer.append("[m [1m");
		buffer.append(" In Resource " + createResourcePath());
		System.out.println(new String(buffer));
	}

	/**
	 * kagerow-setting.xmlを解析し、初期化エントリが正しく指定されているか判定します
	 * @param module モジュールエレメント
	 */
	private void validXML(Element module) {

		Elements elements = processingEnv.getElementUtils();
		Map<? extends ExecutableElement, ? extends AnnotationValue> values = elements
				.getElementValuesWithDefaults(module.getAnnotationMirrors().getFirst());
		for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : values.entrySet()) {
			String value = entry.getKey().getSimpleName().toString();
			if (value.equals("value")) {

				// クラスの完全名を取得
				String targetName = entry.getValue().getValue().toString();

				try {

					// 読み込みリソースオブジェクト生成
					FileObject resourceFile = processingEnv.getFiler().getResource(
							StandardLocation.CLASS_OUTPUT,
							"",
							"kagerow-setting.xml");

					// XMLパースインスタンス生成
					DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
					DocumentBuilder builder = factory.newDocumentBuilder();

					// 既存ファイルの内容取得
					try (InputStream input = resourceFile.openInputStream()) {
						// DOM取得
						Document document = builder.parse(input);
						// Xpath取得
						XPathFactory xPathFactory = XPathFactory.newInstance();
						XPath xPath = xPathFactory.newXPath();
						// 要素取得
						XPathExpression expr = xPath.compile("/KagerowApplication/DIContext/Entry-Point");
						Node node = (Node) expr.evaluate(document, XPathConstants.NODE);
						// 内容解析
						String settingClassName = node.getTextContent();
						if (!settingClassName.equals(targetName)) {
							// デフォルト値の取得
							String defaultValue = entry.getKey().getDefaultValue().getValue().toString();
							if (defaultValue.equals(targetName)) {
								// デフォルト値が設定されているにも関わらず、設定ファイルに値が設定されている場合警告を出力
								processingEnv.getMessager().printMessage(Kind.WARNING,
										settingClassName + " Is Found Configuration In kagerow-setting.xml");
							} else {
								// 初期化エントリの設定が謝っている場合
								processingEnv.getMessager().printMessage(Kind.ERROR,
										targetName + " Is Not Found Configuration In kagerow-setting.xml");
							}
						}
					}

				} catch (IOException e) {
					// ファイルがない場合
					processingEnv.getMessager().printMessage(Kind.ERROR, "Not Found kagerow-setting.xml");
				} catch (XPathExpressionException | ParserConfigurationException | SAXException e) {
					;
				}

			}

		}
	}

}

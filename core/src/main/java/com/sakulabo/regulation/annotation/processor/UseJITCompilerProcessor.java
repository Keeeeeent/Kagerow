package com.sakulabo.regulation.annotation.processor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;

import com.sakulabo.regulation.annotation.UseJITCompiler;
import com.sakulabo.regulation.annotation.UseJITCompiler.JITCompilerOption;

/**
 * UseJITCompilerを処理するアノテーションプロセッサーです。
 * 
 * @author keeeeeent
 */
@SupportedAnnotationTypes("com.sakulabo.regulation.annotation.UseJITCompiler")
public class UseJITCompilerProcessor extends AbstractProcessor {

	/** 出力先ファイル名 */
	private static final String FILE_NAME = "JITCompileOptions.properties";
	/** コンパイラオプション文字列表現 */
	private static final String OPTION_FORMAT = "{0} {1} {2}";

	/** {@inheritDoc} */
	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latestSupported();
	}

	/** {@inheritDoc} */
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

		// オプション蓄積用メモリ
		Set<String> outputList = new HashSet<>();

		for (TypeElement type : annotations) {
			for (Element target : roundEnv.getElementsAnnotatedWith(type)) {
				// クラス名取得
				String className = TypeElement.class.cast(target.getEnclosingElement())
						.getQualifiedName()
						.toString()
						.replace(".", "/");

				// シグニチャー取得
				StringJoiner descriptor = new StringJoiner("", "(", ")");
				ExecutableElement method = (ExecutableElement) target;
				Types types = processingEnv.getTypeUtils();
				method.getParameters().stream()
						.map(f -> toDescriptor(f.asType(), types))
						.forEach(descriptor::add);

				// 返却型取得
				String returnType = toDescriptor(method.getReturnType(), types);

				// メソッド名取得
				String methodName = target.getSimpleName().toString();

				// コンパイラオプション取得
				UseJITCompiler optionType = target.getAnnotation(UseJITCompiler.class);
				// コンパイラオプション文字列表現生成
				for (JITCompilerOption opt : optionType.value()) {
					String createOption = MessageFormat.format(OPTION_FORMAT,
							new Object[] { opt, className, methodName + descriptor + returnType });
					outputList.add(createOption);
				}

			}
		}

		// 出力先パス生成
		Path outputPath = Paths.get(FILE_NAME).toAbsolutePath();

		// ファイルが存在する場合、今回のラウンドで取得したオプションとマージ
		if (Files.exists(outputPath)) {
			// 取得結果をファイルに出力
			try (InputStream input = Files.newInputStream(outputPath);
					InputStreamReader converter = new InputStreamReader(
							input, StandardCharsets.UTF_8);
					BufferedReader reader = new BufferedReader(converter)) {
				String line;
				while ((line = reader.readLine()) != null) {
					outputList.add(line);
				}
			} catch (IOException e) {
				processingEnv.getMessager().printMessage(Kind.ERROR, e.getMessage());
			}
		}

		// 取得結果をファイルに出力
		try (OutputStream output = Files.newOutputStream(outputPath);
				OutputStreamWriter converter = new OutputStreamWriter(
						output, StandardCharsets.UTF_8);
				BufferedWriter writer = new BufferedWriter(converter)) {
			for (String opotion : outputList) {
				writer.write(opotion);
				writer.newLine();
			}
			// 出力ログ
			createLogMsg(outputPath);
		} catch (IOException e) {
			processingEnv.getMessager().printMessage(Kind.ERROR, e.getMessage());
		}

		// プロセッサー処理を完了
		return true;
	}

	/**
	 * ファイルを出力した時のメッセージを出力します
	 * @param filePath 出力先パス
	 */
	private void createLogMsg(Path filePath) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("[[1;34mINFO[m] ");
		buffer.append("Create by APT for JitCompiler OptionFile ");
		buffer.append("[0;35m");
		buffer.append(Objects.toString(filePath));
		buffer.append("[m [1m");
		System.out.println(new String(buffer));
	}

	/**
	 * JVMDescriptor文字列の生成を行う<br/>
	 * （型 -> "I", "Ljava/lang/String;"など）
	 * @param type 型情報
	 * @param types 型情報処理ユーティリティ
	 * @return JVMDescriptor文字列表現
	 */
	private String toDescriptor(TypeMirror type, Types types) {
		return switch (type.getKind()) {
		case BOOLEAN -> "Z";
		case BYTE -> "B";
		case SHORT -> "S";
		case INT -> "I";
		case LONG -> "J";
		case CHAR -> "C";
		case FLOAT -> "F";
		case DOUBLE -> "D";
		case VOID -> "V";
		case ARRAY -> {
			ArrayType arrayType = (ArrayType) type;
			yield "[" + toDescriptor(arrayType.getComponentType(), types);
		}
		default -> {
			TypeMirror raw = types.erasure(type);
			DeclaredType declaredType = (DeclaredType) raw;
			TypeElement element = (TypeElement) declaredType.asElement();
			String fqcn = element.getQualifiedName().toString().replace('.', '/');
			yield "L" + fqcn + ";";
		}
		};
	}

}

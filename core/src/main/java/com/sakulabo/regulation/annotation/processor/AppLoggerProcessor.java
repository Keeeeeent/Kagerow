package com.sakulabo.regulation.annotation.processor;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;

import com.sakulabo.regulation.annotation.utils.ChainProcessor;

/**
 * AppLoggerを処理するアノテーションプロセッサーです。
 * 
 * @author keeeeeent
 */
@SupportedAnnotationTypes("com.sakulabo.regulation.annotation.AppLogger")
public class AppLoggerProcessor extends AbstractProcessor {
 
	/** {@inheritDoc} */
	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latestSupported();
	}

	/** {@inheritDoc} */
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

		for (TypeElement type : annotations) {
			for (Element target : roundEnv.getElementsAnnotatedWith(type)) {
				try {
					// 対象が見つかった場合
					Class<?> clazz = Class
							.forName("com.sakulabo.regulation.annotation.processor.AppLoggerChainProcessor");
					// チェーンプロセッサーを生成
					ChainProcessor chainProcessor = (ChainProcessor) clazz.getDeclaredConstructor().newInstance();
					// 連鎖コンパイル実行
					chainProcessor.process(annotations, roundEnv, processingEnv, Set.of(target));
				} catch (Throwable e) {
					e.printStackTrace();
					processingEnv.getMessager().printMessage(Kind.ERROR, e.getMessage());
				}
			}
		}

		// プロセッサー処理を完了
		return true;
	}

}

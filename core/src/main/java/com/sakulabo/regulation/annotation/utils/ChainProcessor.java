package com.sakulabo.regulation.annotation.utils;

import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;

/**
 * JCTree使用中のアノテーションプロセッサー実装を規定するインターフェイスです。
 * 
 * @author keeeeeent
 */
public interface ChainProcessor {

	/**
	 * JCTreeを用いたアノテーションプロセッサーの実装を提供します。
	 * 
	 * @param annotations タイプアノテーション
	 * @param roundEnv ラウンド環境インスタンス
	 * @param processingEnv コンパイル環境インスタンス
	 * @param options コンパイルオプション
	 * @return コンパイル結果
	 */
	public boolean process(Set<? extends TypeElement> annotations,
			RoundEnvironment roundEnv,
			ProcessingEnvironment processingEnv,
			Set<?> options);

	/**
	 * メッセージを出力します
	 * @param prefix プレフィックス
	 * @param logMessage ログメッセージ
	 */
	default void logMsg(String prefix, String logMessage) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("[[1;34mINFO[m] ");
		buffer.append(prefix);
		buffer.append("[0;35m");
		buffer.append(logMessage);
		buffer.append("[m [1m");
		System.out.println(new String(buffer));
	}

}

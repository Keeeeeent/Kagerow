package com.sakulabo.application.common.code;

import java.lang.StackWalker.Option;
import java.util.Optional;
import java.util.ResourceBundle;

import com.sakulabo.regulation.annotation.UseJITCompiler;
import com.sakulabo.regulation.annotation.UseJITCompiler.JITCompilerOption;

/**
 * コード値規定クラスです
 * 
 * @author keeeeeent
 */
abstract class BaseCode {

	/**
	 * リソースバンドルの規定名を生成します
	 * @return 基底名
	 */
	@UseJITCompiler(JITCompilerOption.DONTINLINE)
	static final String createResourceName() {
		// 呼び出し元クラス情報取得
		Class<?> enumClass = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).getCallerClass();
		return "swing." + enumClass.getSimpleName();
	}

	/**
	 * リソースバンドルの規定名を生成します
	 * @param enumClass Enumクラス情報
	 * @return 基底名
	 */
	static final String createResourceName(Class<?> enumClass) {
		return "swing." + enumClass.getSimpleName();
	}

	/**
	 * リソースバンドルを生成します
	 * @return 基底名
	 */
	@UseJITCompiler(JITCompilerOption.DONTINLINE)
	static final ResourceBundle createResourceBundle() {

		// 呼び出し元クラス情報取得
		Class<?> enumClass = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).getCallerClass();

		// 返却用変数
		ResourceBundle message;

		// モジュール取得
		Optional<Module> module = enumClass
				.getModule()
				.getLayer()
				.findModule(ApplicationConstProperty.MODULE_NAME);

		if (module.isPresent()) {
			message = ResourceBundle.getBundle(createResourceName(enumClass), module.get());
		} else {
			message = null;
			System.err.println("not find resource file " + createResourceName(enumClass));
		}

		return message;
	}

	/**
	 * リソース取得失敗時の例外をスローします
	 * @throws RuntimeException リソース取得失敗
	 */
	@UseJITCompiler(JITCompilerOption.DONTINLINE)
	static final void createException() throws RuntimeException {
		// 呼び出し元クラス情報取得
		Class<?> enumClass = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).getCallerClass();
		throw new RuntimeException("not find resource file " + createResourceName(enumClass));
	}

}

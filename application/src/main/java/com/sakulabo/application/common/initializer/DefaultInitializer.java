package com.sakulabo.application.common.initializer;

import java.util.function.Predicate;

import com.sakulabo.application.common.code.ApplicationConstProperty;
import com.sakulabo.core.Kagerow.Adapter.KagerowDIContextAdapter;

/**
 * Beanロード判定機能を提供するクラスです
 * 
 * @author keeeeeent
 */
public class DefaultInitializer implements KagerowDIContextAdapter {

	/** DIコンテキスト初期化モード（GUI） */
	public static final String INIT_MODE_SYSTEM_PROP_GUI_VAL = "GUI";
	/** DIコンテキスト初期化モード（CLI） */
	public static final String INIT_MODE_SYSTEM_PROP_CLI_VAL = "CLI";
	/** DIコンテキスト初期化モードデフォルト値 */
	public static final String INIT_MODE_SYSTEM_PROP_DEF_VAL = "Empty";
	/** DIコンテキスト初期化モード */
	private static final String INIT_MODE_SYSTEM_PROP_VAL;
	static {
		INIT_MODE_SYSTEM_PROP_VAL = System.getProperty(
				ApplicationConstProperty.INIT_MODE_SYSTEM_PROP_KEY,
				INIT_MODE_SYSTEM_PROP_DEF_VAL);
	}
	/** クラス情報判定インスタンス */
	private static final Predicate<Class<?>> IS_LOAD;
	static {

		switch (INIT_MODE_SYSTEM_PROP_VAL) {
		case INIT_MODE_SYSTEM_PROP_GUI_VAL:
			// GUIモードの場合
			IS_LOAD = target -> target.isAnnotationPresent(GraphicComponent.class);
			break;
		case INIT_MODE_SYSTEM_PROP_CLI_VAL:
			// CLIモードの場合
			IS_LOAD = target -> target.isAnnotationPresent(CommandComponent.class);
			break;
		default:
			// 未知のモードの場合
			IS_LOAD = _ -> false;
			break;
		}

	}

	/**
	 * アプリケーション初期化モードを取得します
	 * @return アプリケーション初期化モード表現文字列
	 */
	public static String getMode() {
		return INIT_MODE_SYSTEM_PROP_VAL;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isLord(Class<?> target) {
		// GUIまたはCLI専用コンポーネントの場合、選択的にロード
		if (target.isAnnotationPresent(CommandComponent.class)
				|| target.isAnnotationPresent(GraphicComponent.class)) {
			return IS_LOAD.test(target);
		}
		// 共通実装の場合はロードを行う
		return true;
	}

}

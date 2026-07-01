package com.sakulabo.application.common.initializer;

import com.sakulabo.core.Kagerow.Adapter.KagerowDIContextAdapter;

/**
 * カスタマイズSPIロード向けのイニシャライザです
 * 
 * @author keeeeeent
 */
public class EmptyInitializer implements KagerowDIContextAdapter {

	/** {@inheritDoc} */
	@Override
	public boolean isLord(Class<?> target) {
		// 元実装でGUIまたはCLI専用コンポーネントの場合、ロードはしない
		if (target.isAnnotationPresent(CommandComponent.class)
				|| target.isAnnotationPresent(GraphicComponent.class)) {
			return false;
		}
		// 共通実装の場合はロードを行う
		return true;
	}
}

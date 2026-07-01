package com.sakulabo.application.common.spi;

import com.sakulabo.core.Kagerow.Utilities.KagerowAOP;
import com.sakulabo.core.Kagerow.Utilities.KagerowAOPProcessors;

/**
 * アプリケーションビューを起動するエントリークラスです
 * 
 * @author keeeeeent
 */
public interface ViewRunner {

	/**
	 * アプリケーションを起動します
	 */
	@KagerowAOP(KagerowAOPProcessors.APPLOGGER)
	void start();

}

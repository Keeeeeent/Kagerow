package com.sakulabo.core.Kagerow.Utilities;

import com.sakulabo.core.Processor.jmx.AppJMXInitializer;

/**
 * KagerowのJMX監視コントローラーインターフェイスです
 * 
 * @author keeeeeent
 */
public sealed interface KagerowJMX permits AppJMXInitializer {

	/**
	 * JMXによる監視を停止します
	 */
	void stop();

}

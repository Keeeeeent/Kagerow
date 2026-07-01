package com.sakulabo.core.Processor.config;

import java.lang.System.Logger.Level;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.management.InstanceAlreadyExistsException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;
import com.sakulabo.core.Processor.jmx.AppJMXInitializer;

/**
 * Kagerowデシリアライズ専用コンフィグレーションレコード生成クラスです
 * 
 * @author keeeeeent
 */
public class AppObjectInputFilterConfigurationLorder extends ConfigurationLorder<AppObjectInputFilterConfiguration> {

	/** オブジェクトチェーン最大深度 */
	private int MAX_DEPTH;
	/** 配列最大要素数 */
	private int MAX_ARRAY_LENGTH;
	/** デシリアライズ許可クラスリスト */
	private List<String> ALLOWED_CLASS_LIST = new CopyOnWriteArrayList<>();

	/**
	 * コンフィグレーションレコードを返却します
	 * @return コンフィグレーションレコード
	 */
	public static AppObjectInputFilterConfiguration getConfig() {
		AppObjectInputFilterConfigurationLorder config = new AppObjectInputFilterConfigurationLorder();
		return config.load();
	}

	/** {@inheritDoc} */
	@Override
	protected AppObjectInputFilterConfiguration build() {

		// コンフィグレーション生成
		AppObjectInputFilterConfiguration config = new AppObjectInputFilterConfiguration(
				MAX_DEPTH,
				MAX_ARRAY_LENGTH,
				ALLOWED_CLASS_LIST);

		// JMX登録
		try {
			AppJMXInitializer.registMXBean(config);
		} catch (Exception e) {
			if (!(e instanceof InstanceAlreadyExistsException)) {
				KagerowLogger.newSystemLogger().log(Level.ERROR, e);
			}
		}

		return config;
	}

	/** {@inheritDoc} */
	@Override
	protected void lordConfigFile(Document document, XPath xPath) throws Exception {

		/** オブジェクトチェーン最大深度 */
		{
			expr = xPath.compile("/KagerowApplication/ObjectInputFilter/Max-Depth");
			node = (Node) expr.evaluate(document, XPathConstants.NODE);
			MAX_DEPTH = Integer.valueOf(node.getTextContent()).intValue();
		}

		/** 配列最大要素数 */
		{
			expr = xPath.compile("/KagerowApplication/ObjectInputFilter/Max-Array-Length");
			node = (Node) expr.evaluate(document, XPathConstants.NODE);
			MAX_ARRAY_LENGTH = Integer.valueOf(node.getTextContent()).intValue();
		}

		/** デシリアライズ許可クラスリスト */
		{
			expr = xPath.compile("/KagerowApplication/ObjectInputFilter/Allowed-Class-List");
			nodeList = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
			for (int i = 0; i < nodeList.getLength(); i++) {
				if (nodeList.item(i) instanceof Element element
						&& element.getTagName().equals("Allowed-Class")) {
					ALLOWED_CLASS_LIST.add(element.getTextContent());
				}
			}
		}

	}

}

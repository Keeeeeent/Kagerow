package com.sakulabo.core.Processor.script;

import java.io.InputStream;

import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

/**
 * KSQL専用のXSDファイル解決クラス実装です
 * 
 * @author keeeeeent
 */
public final class KFileXSDClasspathResourceResolver implements LSResourceResolver {

	/**
	 * XSDクラスパス解決向け実装クラス
	 */
	private static class SimpleLSInput implements LSInput {

		/** パブリックID */
		private final String publicId;
		/** システムID */
		private final String systemId;
		/** バイトストリーム */
		private final InputStream inputStream;

		/**
		 * デフォルトコンストラクタ
		 * @param publicId パブリックID
		 * @param systemId システムID
		 * @param inputStream バイトストリーム
		 */
		public SimpleLSInput(
				String publicId,
				String systemId,
				InputStream inputStream) {
			this.publicId = publicId;
			this.systemId = systemId;
			this.inputStream = inputStream;
		}

		/**{@inheritDoc} */
		@Override
		public InputStream getByteStream() {
			return inputStream;
		}

		/**{@inheritDoc} */
		@Override
		public void setByteStream(InputStream byteStream) {
			;
		}

		/**{@inheritDoc} */
		@Override
		public String getSystemId() {
			return systemId;
		}

		/**{@inheritDoc} */
		@Override
		public void setSystemId(String systemId) {
			;
		}

		/**{@inheritDoc} */
		@Override
		public String getPublicId() {
			return publicId;
		}

		/**{@inheritDoc} */
		@Override
		public void setPublicId(String publicId) {
			;
		}

		/**{@inheritDoc} */
		@Override
		public String getBaseURI() {
			return null;
		}

		/**{@inheritDoc} */
		@Override
		public void setBaseURI(String baseURI) {
			;
		}

		/**{@inheritDoc} */
		@Override
		public java.io.Reader getCharacterStream() {
			return null;
		}

		/**{@inheritDoc} */
		@Override
		public void setCharacterStream(java.io.Reader characterStream) {
			;
		}

		/**{@inheritDoc} */
		@Override
		public String getStringData() {
			return null;
		}

		/**{@inheritDoc} */
		@Override
		public void setStringData(String stringData) {
			;
		}

		/**{@inheritDoc} */
		@Override
		public String getEncoding() {
			return "UTF-8";
		}

		/**{@inheritDoc} */
		@Override
		public void setEncoding(String encoding) {
			;
		}

		/**{@inheritDoc} */
		@Override
		public boolean getCertifiedText() {
			return false;
		}

		/**{@inheritDoc} */
		@Override
		public void setCertifiedText(
				boolean certifiedText) {
		}
	}

	/**{@inheritDoc} */
	@Override
	public LSInput resolveResource(
			String type,
			String namespaceURI,
			String publicId,
			String systemId,
			String baseURI) {
		// classpath固定
		String path = "/config/xsd/" + systemId;
		InputStream in = ClassLoader.getSystemClassLoader().getResourceAsStream(path);
		// 未知XSDは拒否
		if (in == null) {
			throw new IllegalArgumentException("Unknown schema: " + systemId);
		}
		return new SimpleLSInput(publicId, systemId, in);
	}

}

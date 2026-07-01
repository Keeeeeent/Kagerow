package com.sakulabo.core.Kagerow.Utilities;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.sakulabo.core.Common.ErrorMessage;
import com.sakulabo.core.Kagerow.Exception.KFileParseException;

/**
 * Kagerowスクリプトファイルのバージョン管理を行う列挙クラスです
 * 
 * @author keeeeeent
 */
public enum KagerowFileVersion {

	/** バージョン1 */
	RELEASE_0(1, "KsqlSchema.xsd");

	/** 内部官向けバージョン情報 */
	private final int version;
	/** バージョン情報 */
	private final String displayVersion;
	/** XSDファイル名称 */
	private final String fileName;

	/** 最新バージョン */
	private static final KagerowFileVersion latest;
	static {
		// ソートインスタンス
		Comparator<KagerowFileVersion> comparator = new Comparator<KagerowFileVersion>() {
			/** {@inheritDoc} */
			@Override
			public int compare(KagerowFileVersion o1, KagerowFileVersion o2) {
				return Integer.valueOf(o1.version).compareTo(Integer.valueOf(o2.version));
			}
		};
		// ソート処理実行
		List<KagerowFileVersion> list = Arrays.asList(values());
		Collections.sort(list, comparator);
		// 最新バージョン設定
		latest = list.getLast();
	}

	/**
	 * デフォルトコンストラクタ
	 * @param version 内部官向けバージョン情報
	 * @param fileName XSDファイル名称
	 */
	private KagerowFileVersion(
			int version,
			String fileName) {
		this.version = version;
		this.fileName = fileName;
		this.displayVersion = String.format("version%d", version);
	}

	/**
	 * 最新バージョンを取得します
	 * @return 最新バージョン
	 */
	public static KagerowFileVersion latestSupported() {
		return latest;
	}

	/**
	 * Kagerowスクリプトファイルのバージョンに変換します
	 * @return Kagerowスクリプトファイルのバージョン
	 */
	public int toVersion() {
		return version;
	}

	/**
	 * XSDファイルに変換します
	 * @return XSDファイル名称
	 */
	public String toFileName() {
		return fileName;
	}

	/**
	 * 文字列をEnumに変換します
	 * @param target ファイル名称
	 * @return Enumインスタンス
	 */
	public static KagerowFileVersion fromString(String target) {
		KagerowFileVersion result = latestSupported();
		for (KagerowFileVersion version : values()) {
			if (version.fileName.equals(target)) {
				result = version;
				break;
			}
		}
		return result;
	}

	/**
	 * ファイルからEnumに変換します
	 * @param path 対象ファイル
	 * @return Enumインスタンス
	 * @throws KFileParseException ファイル読み込み失敗
	 */
	public static KagerowFileVersion fromFile(Path path) throws KFileParseException {
		try {
			// ファイル存在確認
			if (Files.notExists(path)) {
				// ファイルパス不正
				throw new KFileParseException(ErrorMessage.CODE_024.getMessage(), new IOException("Not Exists KFile"));
			}
			// DocumentFactory生成
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			// namespace対応
			factory.setNamespaceAware(true);
			// secure processing
			factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			// DOCTYPE禁止
			factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			// 外部Entity禁止
			factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
			// 外部パラメータEntity禁止
			factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
			// 外部DTD禁止
			factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			// XInclude禁止
			factory.setXIncludeAware(false);
			// Entity展開抑止
			factory.setExpandEntityReferences(false);
			// ビルダー生成
			DocumentBuilder builder = factory.newDocumentBuilder();
			// DOM取得
			try (InputStream input = Files.newInputStream(path)) {
				// DOM生成
				Document document = builder.parse(input);
				// XSD取得
				Element root = document.getDocumentElement();
				final String rawXsdFileName = root.getAttributeNS(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI,
						"noNamespaceSchemaLocation");
				// 結果返却
				return fromString(rawXsdFileName);
			}
		} catch (IOException | SAXException | ParserConfigurationException e) {
			// XMLファイル不正
			throw new KFileParseException(ErrorMessage.CODE_024.getMessage(), e);
		}
	}

	/**
	 * バージョン情報を取得します
	 * @return バージョン情報
	 */
	public String toDisplayVersion() {
		return displayVersion;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return fileName;
	}

}

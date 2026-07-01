package com.sakulabo.core.Processor.config;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sakulabo.core.Kagerow.Exception.ApplicationError;

/**
 * Kagerowアプリケーション専用コンフィグレーションレコード生成基底クラスです
 * 
 * @author keeeeeent
 * @param <T> コンフィグレーションの型情報
 */
abstract class ConfigurationLorder<T> {

	/** Kagerow設定ファイル名称 */
	protected static final String SETTING_FILE_NAME = "kagerow-setting.xml";
	/** 解析用フィールド（xPathアクセッサー） */
	protected XPathExpression expr;
	/** 解析用フィールド（ノード） */
	protected Node node;
	/** 解析用フィールド（ノードリスト） */
	protected NodeList nodeList;

	/**
	 * 設定をロードします
	 * @return 生成した設定インスタンス
	 */
	public final T load() {

		// 返却用変数初期化
		T result = null;

		// クラスローダー取得
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();

		try (InputStream input = classloader.getResourceAsStream(SETTING_FILE_NAME)) {

			// XMLパースインスタンス生成
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();

			// DOM取得
			Document document = builder.parse(input);
			// Xpath取得
			XPathFactory xPathFactory = XPathFactory.newInstance();
			XPath xPath = xPathFactory.newXPath();

			// コンフィグレーション読み込み
			lordConfigFile(document, xPath);

			// インスタンス生成
			result = build();

		} catch (Exception e) {
			// コンテキスト設定ファイル取得失敗
			// アプリケーションを終了
			throw new ApplicationError(e);
		}

		return result;

	}

	/**
	 * コンフィグレーションを構築します
	 * @return コンフィグレーション
	 */
	protected abstract T build();

	/**
	 * コンフィグレーションを読み込みます
	 * @param document XMLインスタンス
	 * @param xPath XML解析用xPathインスタンス
	 * @throws Exception コンフィグレーション解析失敗
	 */
	protected abstract void lordConfigFile(
			Document document,
			XPath xPath) throws Exception;
}

package com.sakulabo.core.Kagerow.Contents.Impl;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import javax.naming.Name;
import javax.naming.NamingException;

import com.sakulabo.core.Kagerow.Contents.KagerowSettingContent;
import com.sakulabo.core.Kagerow.Exception.ApplicationError;

/**
 * Kagerowアプリケーションのセッティングコンテンツ実装クラスです
 * 
 * @author keeeeeent
 */
public final class KagerowSettingContentImpl extends BaseKagerowContent implements KagerowSettingContent {

	/** ネームスペース */
	private final String propName;
	/** プロパティー */
	private final Properties props = new Properties();

	/**
	 * デフォルトコンストラクタ
	 * @param propName ネームスペース
	 * @param props プロパティー
	 */
	public KagerowSettingContentImpl(String propName, StringWriter props) {
		// フィールド初期化
		this.propName = propName;
		// Readerに変換
		StringReader reader = new StringReader(props.toString());
		try {
			// プロパティー読み込み
			this.props.load(reader);
		} catch (IOException e) {
			// 基本的にここでは例外は発生しない
			throw new ApplicationError(e);
		}
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		// Writer生成
		StringBuilder props = new StringBuilder();
		// 名称の追加
		props.append("[" + propName + "]");
		props.append(System.lineSeparator());
		// プロパティー追加
		StringWriter writer = new StringWriter();
		try {
			this.props.store(writer, null);
		} catch (IOException _) {
			// ignore
		}
		writer.toString()
				.lines()
				.skip(1)
				.forEach(target -> {
					props.append(target);
					props.append(System.lineSeparator());
				});
		// 文字列変換
		return props.toString();
	}

	/** {@inheritDoc} */
	@Override
	public String lookup(Name name) throws NamingException {
		return props.getProperty(Objects.toString(name));
	}

	/** {@inheritDoc} */
	@Override
	public String lookup(String name) throws NamingException {
		return props.getProperty(name);
	}

	/** {@inheritDoc} */
	@Override
	public void bind(Name name, Object obj) throws NamingException {
		props.setProperty(Objects.toString(name), Objects.toString(obj));
		callListener(KagerowContentEventKind.CREATE, obj);
	}

	/** {@inheritDoc} */
	@Override
	public void rebind(Name name, Object obj) throws NamingException {
		props.setProperty(Objects.toString(name), Objects.toString(obj));
		callListener(KagerowContentEventKind.UPDATE, obj);
	}

	/** {@inheritDoc} */
	@Override
	public void unbind(Name name) throws NamingException {
		Object obj = props.remove(Objects.toString(name));
		callListener(KagerowContentEventKind.DELETE, obj);
	}

	/** {@inheritDoc} */
	@Override
	public void rename(Name oldName, Name newName) throws NamingException {
		String old = props.getProperty(Objects.toString(oldName));
		props.setProperty(Objects.toString(newName), Objects.toString(old));
		callListener(KagerowContentEventKind.RENAME, old);
	}

	/** {@inheritDoc} */
	@Override
	public String getNameInNamespace() throws NamingException {
		return propName.substring(1, propName.length() - 1);
	}

	/** {@inheritDoc} */
	@Override
	public List<String> settingList() {
		return props.values().stream().map(Objects::toString).toList();
	}

	/** {@inheritDoc} */
	@Override
	public List<String> settingKeySet() {
		return props.keySet().stream().map(Objects::toString).toList();
	}

}

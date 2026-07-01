package com.sakulabo.core.Kagerow.Context.Impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.CompositeName;
import javax.naming.Name;
import javax.naming.NamingException;

import com.sakulabo.core.Common.AppPathUtils;
import com.sakulabo.core.Kagerow.Contents.KagerowSettingContent;
import com.sakulabo.core.Kagerow.Contents.Impl.KagerowSettingContentImpl;
import com.sakulabo.core.Kagerow.Context.KagerowSettingContext;
import com.sakulabo.core.Kagerow.Exception.ApplicationError;
import com.sakulabo.core.Processor.config.ContextConfigurationLorder;
import com.sakulabo.core.Processor.jmx.AppJMX;
import com.sakulabo.core.Processor.jmx.Context.KagerowSettingContextMXBean;

/**
 * Kagerowが管理するセッテイングアクセスコンテンツです
 * 
 * @author keeeeeent
 */
@AppJMX(name = "Context", options = { "type=KagerowSettingContext" })
public final class KagerowSettingContextImpl extends BaseKagerowContext<KagerowSettingContent>
		implements KagerowSettingContext, KagerowSettingContextMXBean {

	/** コンテキスト環境変数メモリ */
	private static final Map<String, String> _ENV;
	static {
		_ENV = ContextConfigurationLorder.getInstance().SETTING_CONTEXT_ENV();
	}
	/** 設定ファイル名称 */
	private static final String SETTING_FILE_NAME = "kagerow.ini";

	/** 設定ファイルパス */
	private final Path settingFilePath;

	/**
	 * デフォルトコンストラクタ
	 */
	protected KagerowSettingContextImpl() {
		this(true);
	}

	/**
	 * 内部実装向けコンストラクタ
	 * @param isRegistMXBean MXBeanの登録有無
	 */
	private KagerowSettingContextImpl(boolean isRegistMXBean) {

		// スーパークラスコンストラクタ呼び出し
		super(new ConcurrentHashMap<>(), _ENV, KagerowSettingContext._NAME);
		// MXBeanの登録
		if (isRegistMXBean) {
			registMXBean(this);
		}
		// 設定ファイルパスを生成
		settingFilePath = AppPathUtils.createSettingDirPath().resolve(SETTING_FILE_NAME);

		Pattern pattern = Pattern.compile("\\[(?<target>.+)\\]");
		StringWriter writer = new StringWriter();
		KagerowSettingContent content = null;

		try {

			// ファイルが存在しない場合新規作成
			if (Files.notExists(settingFilePath)) {
				Files.createFile(settingFilePath);
			}

			// ファイル読み込み
			try (BufferedReader reader = Files.newBufferedReader(settingFilePath, StandardCharsets.UTF_8)) {

				String line, nameSpace = null;
				while ((line = reader.readLine()) != null) {
					Matcher matcher = pattern.matcher(line);
					if (matcher.find()) {
						if (Objects.nonNull(nameSpace)) {
							content = new KagerowSettingContentImpl(nameSpace, writer);
							writer = new StringWriter();
							bind(nameSpace, content);
						}
						nameSpace = matcher.group("target");
					} else {
						writer.append(line);
						writer.append(System.lineSeparator());
					}
				}

				if (Objects.nonNull(nameSpace)) {
					content = new KagerowSettingContentImpl(nameSpace, writer);
					bind(nameSpace, content);
				}

			}

		} catch (Exception e) {
			throw new ApplicationError(e);
		}

	}

	/**
	 * コンフィグを読み込みセキュアネームスペースがあるかを判定します<br/>
	 * 取得結果はKagerowがセキュア起動有無と同義です<br/>
	 * この実装は内部呼び出し以外の想定はしておりません
	 * @return 判定結果
	 */
	public static boolean isSecure() {
		try {
			// セッテイングコンテキストを取得
			KagerowSettingContext ctx = new KagerowSettingContextImpl(false);
			// セッテイングコンテキスト初期化
			KagerowSettingContent content = ctx.lookup(KagerowSecurityContextImpl.SETTING_NAME_SPACE);
			// 所定の設定値を取得
			String iv = content.lookup(KagerowSecurityContextImpl.IV_KEY_NAME);
			String salt = content.lookup(KagerowSecurityContextImpl.SALT_KEY_NAME);
			return Objects.nonNull(iv) && Objects.nonNull(salt);
		} catch (NamingException e) {
			return false;
		}
	}

	/** {@inheritDoc} */
	@Override
	public KagerowSettingContent lookup(Name name) throws NamingException {
		return (KagerowSettingContent) super.lookup(name);
	}

	/** {@inheritDoc} */
	@Override
	public KagerowSettingContent lookup(String name) throws NamingException {
		return (KagerowSettingContent) super.lookup(name);
	}

	/** {@inheritDoc} */
	@Override
	public KagerowSettingContent createSubcontext(Name name) throws NamingException {
		KagerowSettingContent content = new KagerowSettingContentImpl(Objects.toString(name), new StringWriter());
		bind(name, content);
		// リスナー起動
		callListener(KagerowContextEventKind.CREATE_SUB_CONTEXT);
		return content;
	}

	/** {@inheritDoc} */
	@Override
	public KagerowSettingContent createSubcontext(String name) throws NamingException {
		Name named = new CompositeName(name);
		return createSubcontext(named);
	}

	/** {@inheritDoc} */
	@Override
	public Map<String, String> getContext() {
		Map<String, String> jmxMap = new HashMap<>();
		for (Map.Entry<Name, ?> jmxTarget : _CONTEXT.entrySet()) {
			jmxMap.put(jmxTarget.getKey().toString(), jmxTarget.getValue().getClass().getCanonicalName());
		}
		return jmxMap;
	}

	/** {@inheritDoc} */
	@Override
	public void close() throws NamingException {

		try (BufferedWriter writer = Files.newBufferedWriter(settingFilePath, StandardCharsets.UTF_8)) {
			for (KagerowSettingContent content : super._CONTEXT.values()) {
				writer.append(content.toString());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}

package com.sakulabo.core.Kagerow.Context.Impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.StackWalker.Option;
import java.lang.StackWalker.StackFrame;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

import javax.naming.Binding;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.NoPermissionException;

import com.sakulabo.core.Common.AppPathUtils;
import com.sakulabo.core.Common.StringUtils;
import com.sakulabo.core.Kagerow.Contents.KagerowPluginContent;
import com.sakulabo.core.Kagerow.Contents.Impl.KagerowPluginContentImpl;
import com.sakulabo.core.Kagerow.Context.KagerowPluginContext;
import com.sakulabo.core.Kagerow.Exception.ApplicationError;
import com.sakulabo.core.Kagerow.Utilities.KagerowUtilities;
import com.sakulabo.core.Processor.config.ContextConfigurationLorder;
import com.sakulabo.core.Processor.jmx.AppJMX;
import com.sakulabo.core.Processor.jmx.Context.KagerowPluginContextImplMXBean;
import com.sakulabo.core.Processor.plugin.KagerowClassLoader;
import com.sakulabo.library.csv.CSVDefaultPlugin;
import com.sakulabo.library.sql.DDLDefaultPlugin;
import com.sakulabo.library.sql.DTCDefaultPlugin;
import com.sakulabo.library.sql.JDBCDefaultPlugin;
import com.sakulabo.library.text.JSONDefaultPlugin;
import com.sakulabo.library.text.NDJSONDefaultPlugin;
import com.sakulabo.library.text.TEXTDefaultPlugin;
import com.sakulabo.library.tsv.TSVDefaultPlugin;
import com.sakulabo.library.xml.EXCELDefaultPlugin;
import com.sakulabo.library.xml.HTMLDefaultPlugin;
import com.sakulabo.library.xml.XMLDefaultPlugin;
import com.sakulabo.regulation.annotation.UseJITCompiler;
import com.sakulabo.regulation.annotation.UseJITCompiler.JITCompilerOption;
import com.sakulabo.regulation.spi.PluginAdapter;

/**
 * Kagerowが管理するプラグインアクセスコンテンツです
 * @author keeeeeent
 */
@AppJMX(name = "Context", options = { "type=KagerowPluginContext" })
public final class KagerowPluginContextImpl extends BaseKagerowContext<KagerowPluginContent>
		implements KagerowPluginContext, KagerowPluginContextImplMXBean {

	/** コンテキスト環境変数メモリ */
	private static final Map<String, String> _ENV;

	static {
		_ENV = ContextConfigurationLorder.getInstance().PLUGIN_CONTEXT_ENV();
	}

	/** 無効化プラグイン一覧ファイル名称 */
	private static final String CONTEXT_KEY_STORE_FILE_NAME_FORMAT = "%s-disable-plugin.list";

	/** コンテキスト環境変数メモリ */
	private final List<String> disablePluginList;
	/** 無効化プラグイン一覧ファイル名称 */
	private final String contextKeyStoreFileName;
	/** プラグイン物理ファイルパス */
	private final Path pluginFile;
	/** 無効化リストパス */
	private final Path listPath;
	/** デフォルトプラグインリスト */
	private final PluginAdapter[] adapterList = {
			new CSVDefaultPlugin(),
			new TSVDefaultPlugin(),
			new DDLDefaultPlugin(),
			new DTCDefaultPlugin(),
			new JDBCDefaultPlugin(),
			new JSONDefaultPlugin(),
			new NDJSONDefaultPlugin(),
			new TEXTDefaultPlugin(),
			new EXCELDefaultPlugin(),
			new HTMLDefaultPlugin(),
			new XMLDefaultPlugin()
	};

	/**
	 * 内部向けデフォルトコンストラクタ
	 * @throws NamingException コンテキスト生成失敗
	 */
	protected KagerowPluginContextImpl() throws NamingException {

		// スーパークラスコンストラクタ呼び出し
		super(new ConcurrentHashMap<>(), _ENV, StringUtils.DEFAULT);
		// MXBeanの登録
		registMXBean(this);
		// フィールド初期化
		this.pluginFile = null;
		this.listPath = null;
		this.contextKeyStoreFileName = null;
		this.disablePluginList = Collections.emptyList();

		// クラスローダー生成
		KagerowClassLoader classLoader = new KagerowClassLoader();

		for (PluginAdapter adapter : adapterList) {
			// コンテンツ生成
			KagerowPluginContent content = new KagerowPluginContentImpl(adapter, classLoader);
			// コンテンツ追加
			bind(content.getNameInNamespace(), content);
		}

		// 名称の登録
		super._NAME = KagerowUtilities.createVersioningPluginPkgName(StringUtils.DEFAULT, 1, 0, 0);

	}

	/**
	 * デフォルトコンストラクタ
	 * @param pluginFile プラグインファイル
	 * @throws NamingException コンテキスト生成失敗
	 */
	protected KagerowPluginContextImpl(Path pluginFile) throws NamingException {

		// スーパークラスコンストラクタ呼び出し
		super(new ConcurrentHashMap<>(), _ENV, StringUtils.DEFAULT);
		// MXBeanの登録
		registMXBean(this);
		// フィールド初期化
		this.pluginFile = pluginFile;

		// デフォルトプラグインクラス情報リスト生成
		List<?> adapterList = Arrays.asList(this.adapterList)
				.stream()
				.map(PluginAdapter::getClass)
				.toList();

		try {

			/** プラグイン対象ファイル取込 */

			// クラスローダー生成
			KagerowClassLoader classLoader = new KagerowClassLoader(pluginFile);

			// 無効化リストパス初期化
			contextKeyStoreFileName = String.format(
					CONTEXT_KEY_STORE_FILE_NAME_FORMAT,
					classLoader.getPluginPkg().orElse(StringUtils.DEFAULT));

			// 無効中プラグイン一覧リスト初期化
			listPath = AppPathUtils.createSettingDirPath().resolve(contextKeyStoreFileName);
			if (Files.exists(listPath)) {
				try (
						InputStream input = Files.newInputStream(listPath);
						ObjectInputStream oinput = new ObjectInputStream(input)) {
					@SuppressWarnings("unchecked")
					List<String> tmpList = (List<String>) oinput.readObject();
					disablePluginList = Collections.synchronizedList(tmpList);
				}
			} else {
				disablePluginList = Collections.synchronizedList(new ArrayList<>());
			}

			// SPI実装クラス取得
			ServiceLoader<PluginAdapter> loader = ServiceLoader.load(PluginAdapter.class, classLoader);
			for (PluginAdapter adapter : loader) {

				// デフォルトプラグインの場合はスキップ
				if (adapterList.contains(adapter.getClass())) {
					continue;
				}

				// コンテンツ生成
				KagerowPluginContent content = new KagerowPluginContentImpl(adapter, classLoader);
				// 無効化対象か確認
				if (!isDisable(content.getNameInNamespace())) {
					// 無効でなければコンテキストに格納
					bind(content.getNameInNamespace(), content);
				}

			}

			// パッケージ名称を取得
			Optional<Name> pluginVersioningName = classLoader.getPluginVersion();
			if (pluginVersioningName.isPresent()) {
				// 名称の登録
				super._NAME = pluginVersioningName.get();
			}

		} catch (Exception e) {
			throw new ApplicationError(e);
		}

	}

	/**
	 * 対象クラスが許可されたクラスか確認します
	 * @throws NoPermissionException 呼び出しクラス不正
	 */
	@UseJITCompiler(JITCompilerOption.DONTINLINE)
	private void checkClassLoader() throws NoPermissionException {
		// 呼び出し元の取得
		StackWalker walker = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE);
		boolean result = walker.walk(stream -> {
			return stream
					.map(StackFrame::getDeclaringClass)
					.map(Class::getClassLoader)
					.filter(Objects::nonNull)
					.map(ClassLoader::getClass)
					.anyMatch(KagerowClassLoader.class::equals);

		});
		if (result)
			throw new NoPermissionException();
	}

	/** {@inheritDoc} */
	@Override
	@UseJITCompiler(JITCompilerOption.DONTINLINE)
	public KagerowPluginContent lookup(Name name) throws NamingException {
		// クラスローダーチェック
		checkClassLoader();
		return (KagerowPluginContent) super.lookup(name);
	}

	/** {@inheritDoc} */
	@Override
	@UseJITCompiler(JITCompilerOption.DONTINLINE)
	public KagerowPluginContent lookup(String name) throws NamingException {
		// クラスローダーチェック
		checkClassLoader();
		return (KagerowPluginContent) super.lookup(name);
	}

	/** {@inheritDoc} */
	@Override
	@UseJITCompiler(JITCompilerOption.DONTINLINE)
	public void bind(Name name, Object obj) throws NamingException {
		// クラスローダーチェック
		checkClassLoader();
		super.bind(name, obj);
	}

	/** {@inheritDoc} */
	@Override
	@UseJITCompiler(JITCompilerOption.DONTINLINE)
	public void bind(String name, Object obj) throws NamingException {
		// クラスローダーチェック
		checkClassLoader();
		super.bind(name, obj);
	}

	/** {@inheritDoc} */
	@Override
	@UseJITCompiler(JITCompilerOption.DONTINLINE)
	public void rebind(Name name, Object obj) throws NamingException {
		// クラスローダーチェック
		checkClassLoader();
		super.rebind(name, obj);
	}

	/** {@inheritDoc} */
	@Override
	@UseJITCompiler(JITCompilerOption.DONTINLINE)
	public void rebind(String name, Object obj) throws NamingException {
		// クラスローダーチェック
		checkClassLoader();
		super.rebind(name, obj);
	}

	/** {@inheritDoc} */
	@Override
	@UseJITCompiler(JITCompilerOption.DONTINLINE)
	public void unbind(Name name) throws NamingException {
		// クラスローダーチェック
		checkClassLoader();
		super.unbind(name);
	}

	/** {@inheritDoc} */
	@Override
	@UseJITCompiler(JITCompilerOption.DONTINLINE)
	public void unbind(String name) throws NamingException {
		// クラスローダーチェック
		checkClassLoader();
		super.unbind(name);
	}

	/** {@inheritDoc} */
	@Override
	@UseJITCompiler(JITCompilerOption.DONTINLINE)
	public void rename(Name oldName, Name newName) throws NamingException {
		// クラスローダーチェック
		checkClassLoader();
		super.rename(oldName, newName);
	}

	/** {@inheritDoc} */
	@Override
	@UseJITCompiler(JITCompilerOption.DONTINLINE)
	public void rename(String oldName, String newName) throws NamingException {
		// クラスローダーチェック
		checkClassLoader();
		super.rename(oldName, newName);
	}

	/** {@inheritDoc} */
	@Override
	@UseJITCompiler(JITCompilerOption.DONTINLINE)
	public NamingEnumeration<NameClassPair> list(Name name) throws NamingException {
		// クラスローダーチェック
		checkClassLoader();
		return super.list(name);
	}

	/** {@inheritDoc} */
	@Override
	@UseJITCompiler(JITCompilerOption.DONTINLINE)
	public NamingEnumeration<NameClassPair> list(String name) throws NamingException {
		// クラスローダーチェック
		checkClassLoader();
		return super.list(name);
	}

	/** {@inheritDoc} */
	@Override
	@UseJITCompiler(JITCompilerOption.DONTINLINE)
	public NamingEnumeration<Binding> listBindings(Name name) throws NamingException {
		// クラスローダーチェック
		checkClassLoader();
		return super.listBindings(name);
	}

	/** {@inheritDoc} */
	@Override
	@UseJITCompiler(JITCompilerOption.DONTINLINE)
	public NamingEnumeration<Binding> listBindings(String name) throws NamingException {
		// クラスローダーチェック
		checkClassLoader();
		return super.listBindings(name);
	}

	/** {@inheritDoc} */
	@Override
	@UseJITCompiler(JITCompilerOption.DONTINLINE)
	public String getNameInNamespace() throws NamingException {
		// クラスローダーチェック
		checkClassLoader();
		return super.getNameInNamespace();
	}

	/** {@inheritDoc} */
	@Override
	@UseJITCompiler(JITCompilerOption.DONTINLINE)
	public Object addToEnvironment(String propName, Object propVal) throws NamingException {
		// クラスローダーチェック
		checkClassLoader();
		return super.addToEnvironment(propName, propVal);
	}

	/** {@inheritDoc} */
	@Override
	@UseJITCompiler(JITCompilerOption.DONTINLINE)
	public Object removeFromEnvironment(String propName) throws NamingException {
		// クラスローダーチェック
		checkClassLoader();
		return super.removeFromEnvironment(propName);
	}

	/** {@inheritDoc} */
	@Override
	@UseJITCompiler(JITCompilerOption.DONTINLINE)
	public Hashtable<?, ?> getEnvironment() throws NamingException {
		// クラスローダーチェック
		checkClassLoader();
		return super.getEnvironment();
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
	public void setDisable(boolean disable, String name) throws NamingException {
		if (disable) {
			disablePluginList.add(name);
			unbind(name);
		} else {
			boolean target = disablePluginList.remove(name);
			if (!target) {
				// 無効化されていない場合、例外をスロー
				throw new NameNotFoundException(name);
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public boolean isDisable(String name) throws NamingException {
		return disablePluginList.contains(name);
	}

	/** {@inheritDoc} */
	@Override
	public void close() throws NamingException {
		if (Objects.isNull(listPath)) {
			super.close();
			return;
		}
		try (
				OutputStream output = Files.newOutputStream(listPath);
				ObjectOutputStream ooutput = new ObjectOutputStream(output)) {
			ooutput.writeObject(disablePluginList);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			super.close();
		}
	}

	/** {@inheritDoc} */
	@Override
	public Path getPluginFilePath() {
		return this.pluginFile;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return String.format("%s@%s.%s.%s",
				super._NAME.get(0),
				super._NAME.get(1),
				super._NAME.get(2),
				super._NAME.get(3));
	}

}

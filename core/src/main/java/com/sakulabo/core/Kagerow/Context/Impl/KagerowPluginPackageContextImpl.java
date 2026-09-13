package com.sakulabo.core.Kagerow.Context.Impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.StackWalker.Option;
import java.lang.StackWalker.StackFrame;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Properties;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import javax.naming.Binding;
import javax.naming.CannotProceedException;
import javax.naming.CompoundName;
import javax.naming.Context;
import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.NoPermissionException;

import com.sakulabo.core.Common.AppPathUtils;
import com.sakulabo.core.Common.ErrorMessage;
import com.sakulabo.core.Common.StringUtils;
import com.sakulabo.core.Kagerow.Context.KagerowPluginContext;
import com.sakulabo.core.Kagerow.Context.KagerowPluginPackageContext;
import com.sakulabo.core.Kagerow.Exception.ApplicationError;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;
import com.sakulabo.core.Processor.config.ContextConfigurationLorder;
import com.sakulabo.core.Processor.jmx.AppJMX;
import com.sakulabo.core.Processor.jmx.Context.KagerowPluginPackageContextImplMXBean;
import com.sakulabo.core.Processor.plugin.KagerowClassLoader;
import com.sakulabo.regulation.annotation.UseJITCompiler;
import com.sakulabo.regulation.annotation.UseJITCompiler.JITCompilerOption;

/**
 * Kagerowが管理するプラグインパッケージアクセスコンテンツです
 * @author keeeeeent
 */
@AppJMX(name = "Context", options = { "type=KagerowPluginPackageContext" })
public final class KagerowPluginPackageContextImpl
		extends BaseKagerowContext<KagerowPluginContext>
		implements KagerowPluginPackageContext, KagerowPluginPackageContextImplMXBean, Comparator<Name> {

	/** コンテキスト環境変数メモリ */
	private static final Map<String, String> _ENV;

	static {
		_ENV = ContextConfigurationLorder.getInstance().PLUGIN_PKG_CONTEXT_ENV();
	}

	/** コンテキスト環境変数メモリ */
	private static volatile List<String> disablePluginPkgList;
	/** 無効化プラグイン一覧ファイル名称 */
	private static final String PKG_CONTEXT_KEY_STORE_FILE_NAME = "disable-plugin-pkg.list";

	/** 名称設定プロパティー */
	public static final Properties PROPS = new Properties();

	static {
		PROPS.put("jndi.syntax.direction", "left_to_right");
		PROPS.put("jndi.syntax.separator", "/");
		PROPS.put("jndi.syntax.ignorecase", "false");
		PROPS.put("jndi.syntax.trimblanks", "true");
	}

	/**
	 * デフォルトコンストラクタ
	 * @throws NamingException コンテキスト生成失敗
	 */
	protected KagerowPluginPackageContextImpl() throws NamingException {

		// スーパークラスコンストラクタ呼び出し
		super(new ConcurrentHashMap<>(), _ENV, KagerowPluginPackageContext._NAME);
		// MXBeanの登録
		registMXBean(this);

		// デフォルトプラグインロード
		KagerowPluginContext defaultContext = new KagerowPluginContextImpl();
		// デフォルトコンテキストに格納
		bind(defaultContext.getNameInNamespace(), defaultContext);

		try {

			// 無効中プラグイン一覧リスト初期化
			Path listPath = AppPathUtils.createSettingDirPath().resolve(PKG_CONTEXT_KEY_STORE_FILE_NAME);
			if (Files.exists(listPath)) {
				try (
						InputStream input = Files.newInputStream(listPath);
						ObjectInputStream oinput = new ObjectInputStream(input)) {
					@SuppressWarnings("unchecked")
					List<String> tmpList = (List<String>) oinput.readObject();
					disablePluginPkgList = Collections.synchronizedList(tmpList);
				}
			} else {
				disablePluginPkgList = Collections.synchronizedList(new ArrayList<>());
			}

			// プラグイン対象ファイル取込
			Path rootDir = AppPathUtils.createPluginDirPath();
			List<Path> plugins = Files.walk(rootDir, FileVisitOption.FOLLOW_LINKS)
					.filter(KagerowPluginPackageContextImpl::isPluginFile)
					.map(KagerowPluginPackageContextImpl::toRealPath)
					.filter(Objects::nonNull)
					.toList();

			for (Path pluginFile : plugins) {

				// サブコンテキスト生成
				KagerowPluginContext context = new KagerowPluginContextImpl(pluginFile);
				// 無効化対象か確認
				if (!isDisable(context.getNameInNamespace())) {
					// 無効でなければコンテキストに格納
					bind(context.getNameInNamespace(), context);
				}

			}

		} catch (Exception e) {
			throw new ApplicationError(e);
		}

	}

	/**
	 * pluginファイルか拡張子を元に判定します
	 * @param path ファイルパス
	 * @return 判定結果
	 */
	private final static boolean isPluginFile(Path path) {
		boolean fileCheck = path.toString().endsWith(".plugin");
		boolean linkCheck = true;
		if (Files.isSymbolicLink(path)) {
			linkCheck = Files.exists(path, LinkOption.NOFOLLOW_LINKS) && Files.exists(path);
		}
		return fileCheck && linkCheck;
	}

	/**
	 * シンボリックリンクをたどり、物理パスを生成します<br/>
	 * 対象が通常ファイルの場合、そのままのパスを返却します
	 * @param p 対象ファイル
	 * @return 物理パス
	 */
	private final static Path toRealPath(Path p) {
		try {
			return p.toRealPath();
		} catch (IOException e) {
			return null;
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
	public KagerowPluginContext lookup(Name name) throws NamingException {
		// クラスローダーチェック
		checkClassLoader();
		// 名称を正規化
		name = getVersioningMap(name);
		return (KagerowPluginContext) super.lookup(name);
	}

	/** {@inheritDoc} */
	@Override
	@UseJITCompiler(JITCompilerOption.DONTINLINE)
	public KagerowPluginContext lookup(String name) throws NamingException {
		// クラスローダーチェック
		checkClassLoader();
		// 名称を正規化
		Name named = getVersioningMap(new CompoundName(name, PROPS));
		return (KagerowPluginContext) super.lookup(named);
	}

	/**
	 * 正規化された名称を返却します
	 * @param name 名称
	 * @return 正規化後の名称
	 * @throws InvalidNameException 正規化失敗
	 */
	private Name getVersioningMap(Name name) throws InvalidNameException {
		// 返却値初期化
		Name result = null;
		// 名称の解析
		int size = name.size();
		if (size == 1) {
			// マップ初期化
			TreeMap<Name, KagerowPluginContext> nameMap = new TreeMap<>(this);
			String strName = name.get(0);
			for (Entry<Name, KagerowPluginContext> entry : super._CONTEXT.entrySet()) {
				String tmpName = entry.getKey().get(0);
				// パッケージ名が同一の物をマップに追加
				if (tmpName.equals(strName)) {
					nameMap.put(entry.getKey(), entry.getValue());
				}
			}
			// サイズが1の場合、パッケージ名のみの指定であるためlatestを指定
			result = nameMap.lastKey();
		} else if (size == 2) {
			// サイズが2の場合、メジャーバージョンのみの指定のため下位バージョンを0で指定
			name.add("0");
			name.add("0");
		} else if (size == 3) {
			// サイズが3の場合、マイナーバージョンまでの指定のため下位バージョンを0で指定
			name.add("0");
		} else {
			// 上記以外の場合はそのままリターン
			result = name;
		}
		return result;
	}

	/** {@inheritDoc} */
	@Override
	@UseJITCompiler(JITCompilerOption.DONTINLINE)
	public void bind(Name name, Object obj) throws NamingException {
		// クラスローダーチェック
		checkClassLoader();
		// 名称を正規化
		Name named = getVersioningMap(name);
		super.bind(named, obj);
	}

	/** {@inheritDoc} */
	@Override
	@UseJITCompiler(JITCompilerOption.DONTINLINE)
	public void bind(String name, Object obj) throws NamingException {
		// クラスローダーチェック
		checkClassLoader();
		// 名称を正規化
		Name named = getVersioningMap(new CompoundName(name, PROPS));
		super.bind(named, obj);
	}

	/** {@inheritDoc} */
	@Override
	@UseJITCompiler(JITCompilerOption.DONTINLINE)
	public void rebind(Name name, Object obj) throws NamingException {
		// クラスローダーチェック
		checkClassLoader();
		// 名称を正規化
		Name named = getVersioningMap(name);
		super.rebind(named, obj);
	}

	/** {@inheritDoc} */
	@Override
	@UseJITCompiler(JITCompilerOption.DONTINLINE)
	public void rebind(String name, Object obj) throws NamingException {
		// クラスローダーチェック
		checkClassLoader();
		// 名称を正規化
		Name named = getVersioningMap(new CompoundName(name, PROPS));
		super.rebind(named, obj);
	}

	/** {@inheritDoc} */
	@Override
	@UseJITCompiler(JITCompilerOption.DONTINLINE)
	public void unbind(Name name) throws NamingException {
		// クラスローダーチェック
		checkClassLoader();
		// 名称を正規化
		Name named = getVersioningMap(name);
		super.unbind(named);
	}

	/** {@inheritDoc} */
	@Override
	@UseJITCompiler(JITCompilerOption.DONTINLINE)
	public void unbind(String name) throws NamingException {
		// クラスローダーチェック
		checkClassLoader();
		// 名称を正規化
		Name named = getVersioningMap(new CompoundName(name, PROPS));
		super.unbind(named);
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
		super.rename(new CompoundName(oldName, PROPS), new CompoundName(newName, PROPS));
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
		return super.list(new CompoundName(name, PROPS));
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
		return super.listBindings(new CompoundName(name, PROPS));
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
	@UseJITCompiler(JITCompilerOption.DONTINLINE)
	public Context createSubcontext(Name name) throws NamingException {
		// クラスローダーチェック
		checkClassLoader();
		return createSubcontext(name.toString());
	}

	/** {@inheritDoc} */
	@Override
	@UseJITCompiler(JITCompilerOption.DONTINLINE)
	public Context createSubcontext(String name) throws NamingException {

		// クラスローダーチェック
		checkClassLoader();

		// プラグインパス生成
		Path pluginFile = Paths.get(name);
		// ファイルパス検証
		try {
			// リンクの場合、物理ファイルに変換
			pluginFile = pluginFile.toRealPath();
			// プラグインファイルか検証
			if (!isPluginFile(pluginFile)) {
				// プラグインファイルでない場合、例外をスロー
				throw new IOException(ErrorMessage.CODE_036.getMessage(
						pluginFile.toAbsolutePath().normalize().toString()));
			}
			// インストール先パス生成
			Path installPath = AppPathUtils.createPluginDirPath().resolve(pluginFile.getFileName());
			// ファイルの取り込み（インストール作業）
			Files.copy(pluginFile, installPath);
			// インストール処理完了後、取り込み先（プラグインパス）を更新
			pluginFile = installPath;
		} catch (IOException e) {
			CannotProceedException exception = new CannotProceedException();
			exception.setRootCause(e);
			throw exception;
		}

		// サブコンテキスト生成
		KagerowPluginContext context = new KagerowPluginContextImpl(pluginFile);
		// コンテキストに格納
		bind(context.getNameInNamespace(), context);

		// リスナー起動
		callListener(KagerowContextEventKind.CREATE_SUB_CONTEXT);

		return context;
	}

	/** {@inheritDoc} */
	@Override
	@UseJITCompiler(JITCompilerOption.DONTINLINE)
	public void destroySubcontext(Name name) throws NamingException {
		// クラスローダーチェック
		checkClassLoader();
		destroySubcontext(name.toString());
	}

	/** {@inheritDoc} */
	@Override
	@UseJITCompiler(JITCompilerOption.DONTINLINE)
	public void destroySubcontext(String name) throws NamingException {

		// クラスローダーチェック
		checkClassLoader();
		// プラグイン取得
		KagerowPluginContextImpl pctx = (KagerowPluginContextImpl) lookup(name);
		// プラグインファイル物理パス
		Path pluginPath = pctx.getPluginFilePath();
		// プラグインファイル削除
		try {
			if (Files.deleteIfExists(pluginPath)) {
				// ファイルが削除できた場合、バインド解除
				unbind(name);
			}
		} catch (IOException e) {
			CannotProceedException exception = new CannotProceedException();
			exception.setRootCause(e);
			throw exception;
		}
		// リスナー起動
		callListener(KagerowContextEventKind.DELETE_SUB_CONTEXT);

	}

	/** {@inheritDoc} */
	@Override
	public void setDisable(boolean disable, String name) throws NamingException {
		if (disable) {
			disablePluginPkgList.add(name);
			unbind(name);
		} else {
			boolean target = disablePluginPkgList.remove(name);
			if (!target) {
				// 無効化されていない場合、例外をスロー
				throw new NameNotFoundException(name);
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public boolean isDisable(String name) throws NamingException {
		return disablePluginPkgList.contains(name);
	}

	/** {@inheritDoc} */
	@Override
	public void close() throws NamingException {
		Path listPath = AppPathUtils.createSettingDirPath().resolve(PKG_CONTEXT_KEY_STORE_FILE_NAME);
		try (
				OutputStream output = Files.newOutputStream(listPath);
				ObjectOutputStream ooutput = new ObjectOutputStream(output)) {
			ooutput.writeObject(disablePluginPkgList);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			super.close();
		}
	}

	/** {@inheritDoc} */
	@Override
	public Map<String, String> getContext() {
		Map<String, String> jmxMap = new HashMap<>();
		for (Map.Entry<Name, KagerowPluginContext> jmxTarget : _CONTEXT.entrySet()) {
			try {
				jmxMap.put(jmxTarget.getKey().toString(), jmxTarget.getValue().getNameInNamespace());
			} catch (NamingException e) {
				jmxMap.put(jmxTarget.getKey().toString(), StringUtils.DEFAULT);
				KagerowLogger.newAppLogger().err(e);
			}
		}
		return jmxMap;
	}

	/** {@inheritDoc} */
	@Override
	public int compare(Name o1, Name o2) {
		// バージョン初期化
		Integer major1, minor1, patch1;
		Integer major2, minor2, patch2;
		major1 = Integer.valueOf(o1.get(1));
		major2 = Integer.valueOf(o2.get(1));
		minor1 = Integer.valueOf(o1.get(2));
		minor2 = Integer.valueOf(o2.get(2));
		patch1 = Integer.valueOf(o1.get(3));
		patch2 = Integer.valueOf(o2.get(3));
		// 比較処理
		int result = major1.compareTo(major2);
		if (result == 0) {
			result = minor1.compareTo(minor2);
			if (result == 0) {
				result = patch1.compareTo(patch2);
			}
		}
		return result;
	}

}

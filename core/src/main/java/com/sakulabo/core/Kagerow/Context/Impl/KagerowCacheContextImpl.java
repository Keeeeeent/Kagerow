package com.sakulabo.core.Kagerow.Context.Impl;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.naming.CompositeName;
import javax.naming.Name;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.directory.SchemaViolationException;

import com.sakulabo.core.Common.AppPathUtils;
import com.sakulabo.core.Kagerow.Contents.KagerowCacheContent;
import com.sakulabo.core.Kagerow.Contents.Impl.KagerowCacheContentImpl;
import com.sakulabo.core.Kagerow.Context.KagerowCacheContext;
import com.sakulabo.core.Kagerow.Exception.ApplicationError;
import com.sakulabo.core.Processor.config.ContextConfigurationLorder;
import com.sakulabo.core.Processor.jmx.AppJMX;
import com.sakulabo.core.Processor.jmx.Context.KagerowCacheContextImplMXBean;

/**
 * Kagerowが管理するキャッシュアクセスコンテンツです
 * 
 * @author keeeeeent
 */
@AppJMX(name = "Context", options = { "type=KagerowCacheContext" })
public final class KagerowCacheContextImpl extends BaseKagerowContext<KagerowCacheContent>
		implements KagerowCacheContext, FileVisitor<Path>, KagerowCacheContextImplMXBean {

	/** コンテキスト環境変数メモリ */
	private static final Map<String, String> _ENV;
	static {
		_ENV = ContextConfigurationLorder.getInstance().CACHE_CONTEXT_ENV();
	}

	/** キャッシュID再生成最大回数 */
	private static final int MAX_RETRY = 10;

	/**
	 * デフォルトコンストラクタ
	 */
	protected KagerowCacheContextImpl() {
		// 初期化
		super(new ConcurrentHashMap<>(), _ENV, KagerowCacheContext._NAME);
		// MXBeanの登録
		registMXBean(this);
		// コンテンツ読み込み
		try {
			for (Path path : Files.list(AppPathUtils.createCacheDirPath()).toList()) {
				// キャッシュエントリ対象の場合、コンテンツ生成
				if (Files.isDirectory(path)) {
					Path relativizePath = AppPathUtils.createCacheDirPath().relativize(path);
					// コンテンツ生成
					KagerowCacheContent content = new KagerowCacheContentImpl(relativizePath.toString());
					// コンテキストに追加
					bind(relativizePath.toString(), content);
				}
			}
		} catch (Exception e) {
			throw new ApplicationError(e);
		}
	}

	/** {@inheritDoc} */
	@Override
	public KagerowCacheContent lookup(Name name) throws NamingException {
		return (KagerowCacheContent) super.lookup(name);
	}

	/** {@inheritDoc} */
	@Override
	public KagerowCacheContent lookup(String name) throws NamingException {
		return (KagerowCacheContent) super.lookup(name);
	}

	/** {@inheritDoc} */
	@Override
	public void destroySubcontext(Name name) throws NamingException {
		try {
			// キャッシュディレクトリを削除
			Path path = AppPathUtils.createCacheDirPath().resolve(name.toString());
			Files.walkFileTree(path, this);
			// コンテキストから削除
			super.unbind(name);
		} catch (Exception e) {
			NameNotFoundException exp = new NameNotFoundException();
			exp.addSuppressed(e);
			throw exp;
		}

		// リスナー起動
		callListener(KagerowContextEventKind.DELETE_SUB_CONTEXT);

	}

	/** {@inheritDoc} */
	@Override
	public void destroySubcontext(String name) throws NamingException {
		Name named = new CompositeName(name);
		destroySubcontext(named);
	}

	/** {@inheritDoc} */
	@Override
	public KagerowCacheContent createSubcontext(Name name) throws NamingException {
		// コンテンツ初期化
		KagerowCacheContent content = null;
		// 一時変数
		Path path;
		UUID id;
		try {
			// リトライカウンター
			int counter = 0;
			do {
				// キャッシュID生成
				id = UUID.randomUUID();
				// キャッシュディレクトパス生成
				path = AppPathUtils.createCacheDirPath().resolve(id.toString());
				// ディレクトリの存在確認
				if (Files.notExists(path))
					break;
			} while (counter++ < MAX_RETRY);
			// キャッシュディレクトリ生成
			Files.createDirectory(path);
			// コンテンツ生成
			content = new KagerowCacheContentImpl(id.toString());
			// コンテキストに追加
			bind(id.toString(), content);
		} catch (Exception e) {
			throw new SchemaViolationException(e.getMessage());
		}
		// リスナー起動
		callListener(KagerowContextEventKind.CREATE_SUB_CONTEXT);
		return content;
	}

	/** {@inheritDoc} */
	@Override
	public KagerowCacheContent createSubcontext(String name) throws NamingException {
		if (Objects.nonNull(name)) {
			Name named = new CompositeName(name);
			return createSubcontext(named);
		}
		return createSubcontext((Name) null);
	}

	/** {@inheritDoc} */
	@Override
	public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
		return FileVisitResult.CONTINUE;
	}

	/** {@inheritDoc} */
	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
		Files.delete(file);
		return FileVisitResult.CONTINUE;
	}

	/** {@inheritDoc} */
	@Override
	public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
		return FileVisitResult.CONTINUE;
	}

	/** {@inheritDoc} */
	@Override
	public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
		Files.delete(dir);
		return FileVisitResult.CONTINUE;
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
	public List<String> nameList() throws NamingException {
		List<String> resultList = _CONTEXT.keySet()
				.stream()
				.map(Name::toString)
				.toList();
		return resultList;
	}

}

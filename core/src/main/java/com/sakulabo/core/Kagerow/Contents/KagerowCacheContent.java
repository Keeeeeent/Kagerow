package com.sakulabo.core.Kagerow.Contents;

import java.io.Serial;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.OperationNotSupportedException;

import com.sakulabo.core.Common.AppPathUtils;
import com.sakulabo.core.Kagerow.Contents.KagerowCacheContent.KagerowCacheObject.KagerowExecutionCache;
import com.sakulabo.core.Kagerow.Contents.Impl.KagerowCacheContentImpl;

/**
 * Kagerowアプリケーションのキャッシュコンテンツ規定インターフェースです
 * 
 * @author keeeeeent
 */
public sealed interface KagerowCacheContent extends KagerowContents permits KagerowCacheContentImpl {

	/** コンテンツ名称文字列 */
	public static String _NAME = "CacheObject";

	public sealed interface KagerowCacheObject extends Serializable permits KagerowExecutionCache {

		/** シリアライズID */
		@Serial
		public static final long serialVersionUID = 3581811630559318142L;

		/**
		 * キャッシュ対象ファイルパス
		 * @return 物理ファイルパス（絶対パス）
		 */
		String cacheTargetFilePath();

		/**
		 * キャッシュ対象ファイル最終更新日時
		 * @return 物理ファイル最終更新日時
		 */
		String lastUpdateAt();

		/**
		 * 実行計画復元専用のデータ構造体
		 * @param cacheTargetFilePath キャッシュ対象ファイルパス
		 * @param lastUpdateAt キャッシュ対象ファイル最終更新日時
		 * @param lordedFileList KDBロード済みアーカイブファイルリスト
		 * @param lordedFiledMap KVIEWロード済みアーカイブファイルリスト
		 */
		public record KagerowExecutionCache(
				String cacheTargetFilePath,
				String lastUpdateAt,
				List<String> lordedFileList,
				Map<String, Set<String>> lordedFiledMap)
				implements KagerowCacheObject {

			/** キャッシュファイル名称 */
			public static final String CACHE_FILE_NAME = "LordObject.lst.cache";

			/** 物理DBファイル名 */
			public static final String CACHE_DB_FILE_NAME = "cached.kdb";

			/** シリアライズID */
			@Serial
			public static final long serialVersionUID = 3581811630559318143L;

			/**
			 * デフォルトコンストラクタ
			 * @param kdbPath KDB物理ファイルパス
			 * @param lordedFileList ロード済みアーカイブファイルリスト
			 * @param lordedFiledMap KVIEWロード済みアーカイブファイルリスト
			 */
			public KagerowExecutionCache(
					Path kdbPath,
					List<String> lordedFileList,
					Map<String, Set<String>> lordedFiledMap) {
				this(Objects.toString(kdbPath.normalize().toAbsolutePath(), "/dev/null"),
						null,
						lordedFileList,
						lordedFiledMap);
			}

			/**
			 * デフォルトコンストラクタ
			 * @param kdbPath KDB物理ファイルパス
			 * @param lastUpdateAt キャッシュ対象ファイル最終更新日時
			 * @param lordedFileList ロード済みアーカイブファイルリスト
			 * @param lordedFiledMap KVIEWロード済みアーカイブファイルリスト
			 */
			public KagerowExecutionCache(
					Path kdbPath,
					FileTime lastUpdateAt,
					List<String> lordedFileList,
					Map<String, Set<String>> lordedFiledMap) {
				this(Objects.toString(kdbPath.normalize().toAbsolutePath(), "/dev/null"),
						Objects.nonNull(lastUpdateAt) ? lastUpdateAt.toString() : null,
						lordedFileList,
						lordedFiledMap);
			}

			/**
			 * キャッシュされたKDB物理ファイルパスを取得します
			 * @param cacheId キャッシュID
			 * @param fileName ファイル名称
			 * @return キャッシュされたKDB物理ファイルパス
			 */
			public static Path createCacheKDBPath(String cacheId, String fileName) {
				return AppPathUtils.createCacheDirPath()
						.resolve(cacheId)
						.resolve(fileName);
			}

		}

	}

	/** {@inheritDoc} */
	@Override
	public KagerowCacheObject lookup(Name name) throws NamingException;

	/** {@inheritDoc} */
	@Override
	public default KagerowCacheObject lookup(String name) throws NamingException {
		return lookup(toName(name));
	}

	/** {@inheritDoc} */
	@Override
	@Deprecated
	public default void destroySubcontext(Name name) throws NamingException {
		throw new OperationNotSupportedException();
	}

	/** {@inheritDoc} */
	@Override
	@Deprecated
	public default Context createSubcontext(Name name) throws NamingException {
		throw new OperationNotSupportedException();
	}

	/** {@inheritDoc} */
	@Override
	@Deprecated
	public default void close() throws NamingException {
		;
	}

}

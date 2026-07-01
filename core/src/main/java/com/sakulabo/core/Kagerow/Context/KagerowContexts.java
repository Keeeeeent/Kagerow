package com.sakulabo.core.Kagerow.Context;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;

import com.sakulabo.core.Kagerow.Context.Impl.KagerowContextImpl;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;

/**
 * Kagerowアプリケーションのコンテキスト規定インターフェースです
 * 
 * @author keeeeeent
 */
public sealed interface KagerowContexts<T>
		extends Context
		permits KagerowContextImpl,
		KagerowVirtualFileContext,
		KagerowPluginContext,
		KagerowVirtualDirContext,
		KagerowCacheContext,
		KagerowSecurityContext,
		KagerowSettingContext,
		KagerowPluginPackageContext {

	/** コンテキスト名称文字列 */
	public static final String _NAME = "KagerowSystem";

	/**
	 * 新規作成イベント
	 * このイベントはbineメソッドの実行成功時に発行されます
	 */
	public static final String CREATE = "create";

	/**
	 * 名称イベント
	 * このイベントはrenameメソッドの実行成功時に発行されます
	 */
	public static final String RENAME = "rename";

	/**
	 * 更新イベント
	 * このイベントはrenameメソッドの実行成功時に発行されます
	 */
	public static final String UPDATE = "update";

	/**
	 * 削除イベント
	 * このイベントはunbindメソッドの実行成功時に発行されます
	 */
	public static final String DELETE = "delete";

	/**
	 * サブコンテキスト新規作成イベント
	 * このイベントはcreateSubcontextメソッドの実行成功時に発行されます
	 */
	public static final String CREATE_SUB_CONTEXT = "createSubContext";

	/**
	 * サブコンテキスト削除イベント
	 * このイベントはcreateSubcontextメソッドの実行成功時に発行されます
	 */
	public static final String DELETE_SUB_CONTEXT = "deleteSubContext";

	/**
	 * コンテキスト環境プロパティー追加イベント
	 * このイベントはaddToEnvironmentメソッドの実行成功時に発行されます
	 */
	public static final String ADD_ENV = "addEnv";

	/**
	 * コンテキスト環境プロパティー削除イベント
	 * このイベントはremoveFromEnvironmentメソッドの実行成功時に発行されます
	 */
	public static final String REMOVE_ENV = "removeEnv";

	/**
	 * コンテキスト消滅イベント
	 * このイベントはcloseメソッドの実行成功時に発行されます
	 */
	public static final String CLOSE = "close";

	/** {@inheritDoc} */
	@Override
	T lookup(String name) throws NamingException;

	/** {@inheritDoc} */
	@Override
	T lookup(Name name) throws NamingException;

	/**
	 * コンテキストに指定された名称のエントリが存在するか確認します
	 * Nameインスタンスへ変換不可能な文字列が指定された場合、このメソッドはfalseを返却します
	 * nameがnullの場合もfalseを返却します
	 * @param name エントリ名称
	 * @return 存在有無
	 */
	public boolean isExist(String name);

	/**
	 * コンテキストに指定された名称のエントリが存在するか確認します
	 * このメソッドはnameがnullの場合falseを返却します
	 * @param name エントリ名称
	 * @return 存在有無
	 */
	public boolean isExist(Name name);

	/**
	 * リスナーの登録を行います
	 * リスナーがnullの場合このメソッドは例外をスローします
	 * @param kind 登録種別
	 * @param listener 登録リスナー
	 */
	public void addListener(KagerowContextEventKind kind, KagerowContextEventListener listener);

	/**
	 * コンテキストリスナー定義
	 */
	public static interface KagerowContextEventListener
			extends Callable<Void>, BiConsumer<Throwable, KagerowContextEventInfo> {

		/**
		 * 例外発生時デフォルト実装
		 */
		@Override
		public default void accept(Throwable t, KagerowContextEventInfo u) {
			KagerowLogger.newAppLogger().err(t);
		}
	}

	/**
	 * コンテキストのイベント情報を格納するデータ構造です
	 * @param kind イベント種別
	 * @param caller 呼び出し元
	 * @param timeStamp イベントタイムスタンプ
	 */
	public static record KagerowContextEventInfo(
			KagerowContextEventKind kind,
			String caller,
			Instant timeStamp) {

		/**
		 * コンテキストのイベント情報を格納するデータ構造です
		 * タイムスタンプは現在時刻を記録します
		 * @param kind イベント種別
		 * @param caller 呼び出し元
		 */
		public KagerowContextEventInfo(KagerowContextEventKind kind, String caller) {
			this(kind, caller, Instant.now());
		}

	}

	/**
	 * コンテキストのイベント種別を判別する識別子です
	 */
	public static enum KagerowContextEventKind {

		/**
		 * 新規作成イベント
		 * このイベントはbineメソッドの実行成功時に発行されます
		 */
		CREATE(KagerowContexts.CREATE),
		/**
		 * 名称イベント
		 * このイベントはrenameメソッドの実行成功時に発行されます
		 */
		RENAME(KagerowContexts.RENAME),
		/**
		 * 更新イベント
		 * このイベントはrenameメソッドの実行成功時に発行されます
		 */
		UPDATE(KagerowContexts.UPDATE),
		/**
		 * 削除イベント
		 * このイベントはunbindメソッドの実行成功時に発行されます
		 */
		DELETE(KagerowContexts.DELETE),
		/**
		 * サブコンテキスト新規作成イベント
		 * このイベントはcreateSubcontextメソッドの実行成功時に発行されます
		 */
		CREATE_SUB_CONTEXT(KagerowContexts.CREATE_SUB_CONTEXT),
		/**
		 * サブコンテキスト削除イベント
		 * このイベントはcreateSubcontextメソッドの実行成功時に発行されます
		 */
		DELETE_SUB_CONTEXT(KagerowContexts.DELETE_SUB_CONTEXT),
		/**
		 * コンテキスト環境プロパティー追加イベント
		 * このイベントはaddToEnvironmentメソッドの実行成功時に発行されます
		 */
		ADD_ENV(KagerowContexts.ADD_ENV),
		/**
		 * コンテキスト環境プロパティー削除イベント
		 * このイベントはremoveFromEnvironmentメソッドの実行成功時に発行されます
		 */
		REMOVE_ENV(KagerowContexts.REMOVE_ENV),
		/**
		 * コンテキスト消滅イベント
		 * このイベントはcloseメソッドの実行成功時に発行されます
		 */
		CLOSE(KagerowContexts.CLOSE);

		/** イベント物理名称 */
		public final String EventName;

		/**
		 * デフォルトコンストラクタ
		 * @param EventName イベント名称物理名
		 */
		private KagerowContextEventKind(String EventName) {
			this.EventName = EventName;
		}

		/**
		 * 指定された文字列をマッピングができる種別を返却します
		 * @param target 確認文字列
		 * @return マッピング結果
		 */
		public static Optional<KagerowContextEventKind> toKind(String target) {
			for (KagerowContextEventKind kind : values()) {
				if (kind.EventName.equals(target)) {
					return Optional.of(kind);
				}
			}
			return Optional.empty();
		}

	}

}

package com.sakulabo.core.Kagerow.Contents;

import java.time.Instant;
import java.util.Hashtable;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.naming.Binding;
import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.OperationNotSupportedException;

import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;

/**
 * Kagerowアプリケーションのコンテンツ規定インターフェースです
 * 
 * @author keeeeeent
 */
public sealed interface KagerowContents
		extends Context
		permits KagerowVirtualFileContent,
		KagerowPluginContent,
		KagerowCacheContent,
		KagerowSecurityContent,
		KagerowSettingContent {

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
	 * 文字列をNameインスタンスへ変換します
	 * @param name 文字列
	 * @return Nameインスタンス
	 * @throws NamingException 名称解析失敗
	 */
	public default Name toName(String name) throws NamingException {
		if (Objects.isNull(name)) {
			return null;
		}
		return new CompositeName(name);
	}

	/** {@inheritDoc} */
	@Override
	public default Object lookup(String name) throws NamingException {
		return lookup(toName(name));
	}

	/** {@inheritDoc} */
	@Override
	public default void bind(String name, Object obj) throws NamingException {
		bind(toName(name), obj);
	}

	/** {@inheritDoc} */
	@Override
	public default void rebind(String name, Object obj) throws NamingException {
		rebind(toName(name), obj);
	}

	/** {@inheritDoc} */
	@Override
	public default void rename(String oldName, String newName) throws NamingException {
		rename(toName(oldName), toName(newName));
	}

	/** {@inheritDoc} */
	@Override
	public default void unbind(String name) throws NamingException {
		unbind(toName(name));
	}

	/** {@inheritDoc} */
	@Override
	public default NamingEnumeration<NameClassPair> list(String name) throws NamingException {
		return list(toName(name));
	}

	/** {@inheritDoc} */
	@Override
	public default NamingEnumeration<Binding> listBindings(String name) throws NamingException {
		return listBindings(toName(name));
	}

	/** {@inheritDoc} */
	@Override
	public default void destroySubcontext(String name) throws NamingException {
		destroySubcontext(toName(name));
	}

	/** {@inheritDoc} */
	@Override
	public default Context createSubcontext(String name) throws NamingException {
		return createSubcontext(toName(name));
	}

	/** {@inheritDoc} */
	@Override
	@Deprecated
	public default Object lookupLink(Name name) throws NamingException {
		throw new OperationNotSupportedException();
	}

	/** {@inheritDoc} */
	@Override
	@Deprecated
	public default Object lookupLink(String name) throws NamingException {
		throw new OperationNotSupportedException();
	}

	/** {@inheritDoc} */
	@Override
	@Deprecated
	public default NameParser getNameParser(Name name) throws NamingException {
		throw new OperationNotSupportedException();
	}

	/** {@inheritDoc} */
	@Override
	@Deprecated
	public default NameParser getNameParser(String name) throws NamingException {
		throw new OperationNotSupportedException();
	}

	/** {@inheritDoc} */
	@Override
	@Deprecated
	public default Name composeName(Name name, Name prefix) throws NamingException {
		throw new OperationNotSupportedException();
	}

	/** {@inheritDoc} */
	@Override
	@Deprecated
	public default String composeName(String name, String prefix) throws NamingException {
		throw new OperationNotSupportedException();
	}

	/** {@inheritDoc} */
	@Override
	@Deprecated
	public default Object addToEnvironment(String propName, Object propVal) throws NamingException {
		throw new OperationNotSupportedException();
	}

	/** {@inheritDoc} */
	@Override
	@Deprecated
	public default Object removeFromEnvironment(String propName) throws NamingException {
		throw new OperationNotSupportedException();
	}

	/** {@inheritDoc} */
	@Override
	@Deprecated
	public default Hashtable<?, ?> getEnvironment() throws NamingException {
		throw new OperationNotSupportedException();
	}

	/**
	 * リスナーの登録を行います
	 * リスナーがnullの場合このメソッドは例外をスローします
	 * @param kind 登録種別
	 * @param listener 登録リスナー
	 */
	public void addListener(KagerowContentEventKind kind, KagerowContentEventListener listener);

	/**
	 * コンテンツリスナー定義
	 */
	public static interface KagerowContentEventListener
			extends Consumer<KagerowContentEventInfo>, BiConsumer<Throwable, KagerowContentEventInfo> {

		/**
		 * 通常時デフォルト実装
		 */
		@Override
		public default void accept(KagerowContentEventInfo u) {
			;
		}

		/**
		 * 例外発生時デフォルト実装
		 */
		@Override
		public default void accept(Throwable t, KagerowContentEventInfo u) {
			KagerowLogger.newAppLogger().err(t);
		}

	}

	/**
	 * コンテンツのイベント情報を格納するデータ構造です
	 * @param kind イベント種別
	 * @param caller 呼び出し元
	 * @param content 対象となったコンテンツ
	 * @param timeStamp イベントタイムスタンプ
	 */
	public static record KagerowContentEventInfo(
			KagerowContentEventKind kind,
			String caller,
			Object content,
			Instant timeStamp) {

		/**
		 * コンテンツのイベント情報を格納するデータ構造です
		 * タイムスタンプは現在時刻を記録します
		 * @param kind イベント種別
		 * @param content 対象となったコンテンツ
		 * @param caller 呼び出し元
		 */
		public KagerowContentEventInfo(KagerowContentEventKind kind, String caller, Object content) {
			this(kind, caller, content, Instant.now());
		}

	}

	/**
	 * コンテキストのイベント種別を判別する識別子です
	 */
	public static enum KagerowContentEventKind {

		/**
		 * 新規作成イベント
		 * このイベントはbineメソッドの実行成功時に発行されます
		 */
		CREATE(KagerowContents.CREATE),
		/**
		 * 名称イベント
		 * このイベントはrenameメソッドの実行成功時に発行されます
		 */
		RENAME(KagerowContents.RENAME),
		/**
		 * 更新イベント
		 * このイベントはrenameメソッドの実行成功時に発行されます
		 */
		UPDATE(KagerowContents.UPDATE),
		/**
		 * 削除イベント
		 * このイベントはunbindメソッドの実行成功時に発行されます
		 */
		DELETE(KagerowContents.DELETE);

		/** イベント物理名称 */
		public final String EventName;

		/**
		 * デフォルトコンストラクタ
		 * @param EventName イベント名称物理名
		 */
		private KagerowContentEventKind(String EventName) {
			this.EventName = EventName;
		}

		/**
		 * 指定された文字列をマッピングができる種別を返却します
		 * @param target 確認文字列
		 * @return マッピング結果
		 */
		public static Optional<KagerowContentEventKind> toKind(String target) {
			for (KagerowContentEventKind kind : values()) {
				if (kind.EventName.equals(target)) {
					return Optional.of(kind);
				}
			}
			return Optional.empty();
		}

	}

}

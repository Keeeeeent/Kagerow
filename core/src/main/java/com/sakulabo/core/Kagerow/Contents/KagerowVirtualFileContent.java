package com.sakulabo.core.Kagerow.Contents;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigInteger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.OperationNotSupportedException;

import com.sakulabo.core.Kagerow.Contents.Impl.KagerowVirtualFileContentImpl;

/**
 * Kagerowアプリケーションのコンテンツ拡張インターフェースです<br/>
 * このインターフェースの実装クラスでは、Kagerowの仮想ファイルシステムの機能を提供します
 * 
 * @author keeeeeent
 */
public sealed interface KagerowVirtualFileContent
		extends KagerowContents
		permits KagerowVirtualFileContentImpl {

	/**
	 * データ構造体にJavaで対応可能なデータ型であることの意味づけをします
	 * 
	 * @author keeeeeent
	 */
	public enum KagerowDataType {

		/** NULL */
		NULL(0),
		/** 論理型 */
		BOOLEAN(1),
		/** 整数型 */
		NUMBER(2),
		/** 少数型 */
		DECIMAL(3),
		/** 日付型 */
		DATE(4),
		/** タイムスタンプ型 */
		TIMESTAMP(5),
		/** 文字型 */
		VARCHAR(6);

		/** 優先順序 */
		private int order;

		/**
		 * JVM向けコンストラクタ
		 * @param order 優先順序
		 */
		private KagerowDataType(int order) {
			this.order = order;
		}

		/**
		 * 変更が可能なタイプを格納した配列を生成します
		 * @return 変更が可能なタイプを格納した配列
		 */
		public KagerowDataType[] toModifiableList() {
			List<KagerowDataType> base = new ArrayList<>();
			for (KagerowDataType type : values()) {
				if (order <= type.order) {
					base.add(type);
				}
			}
			return base.toArray(KagerowDataType[]::new);
		}

	}

	/**
	 * Kagerowアプリケーションの仮想FS拡張インターフェースです<br/>
	 * このインターフェースの実装クラスでは、Kagerowの仮想ファイルシステムのデータを提供します
	 */
	public sealed interface KagerowVirtualFileObject extends Serializable {

		/** シリアライズID */
		@Serial
		public static final long serialVersionUID = 227405099260436333L;

		/** 物理アドレスセグメントサイズ */
		public static final BigInteger idxBlockSize = BigInteger.valueOf(Long.BYTES);

		/**
		 * データファイルの作成時刻を返却します
		 * @return データファイルの作成時刻
		 */
		public Instant createTime();

		/**
		 * データファイルサイズ(バイト)を返却します
		 * @return データファイルサイズ(バイト)
		 */
		public BigInteger datSize();

		/**
		 * インデックスファイルサイズ(バイト)を返却します
		 * @return インデックスファイルサイズ(バイト)
		 */
		public BigInteger idxSize();

		/**
		 * データファイルの格納先URIを返却します
		 * @return URI文字列参照インスタンス
		 */
		public AtomicReference<String> uri();

		/**
		 * インデックスのサイズを取得します
		 * @return インデックスサイズ
		 */
		public long indexCount();

		/**
		 * 物理アドレスセグメントサイズを取得します
		 * @return 物理アドレスセグメントサイズ
		 */
		public BigInteger idxBlockSize();

		/**
		 * 物理名を取得します
		 * @return 物理名
		 */
		public String binaryName();

		/**
		 * 論理名を取得します
		 * @return 論理名
		 */
		public String synonym();

		/**
		 * スキーマを取得します
		 * @return スキーマ
		 */
		public String schema();

		/**
		 * ヘッダー情報リストを取得します
		 * @return ヘッダー情報リスト
		 */
		public String[] headerData();

		/**
		 * データタイプ情報リストを取得します
		 * @return データタイプ情報リスト
		 */
		public KagerowDataType[] dataType();

		/**
		 * データサイズ情報リストを取得します
		 * @return データサイズ情報リスト
		 */
		public long[] dataSize();

		/**
		 * データファイルリンクを取得します
		 * @return データファイルリンク
		 */
		public String datAddr();

		/**
		 * インデックスファイルリンクを取得します
		 * @return インデックスファイルリンク
		 */
		public String idxAddr();

		/**
		 * 対象の仮想ファイルオブジェクトが共通定義に基づき等しいかどうか判定します
		 * @param target 比較対象
		 * @return 比較結果
		 */
		public default boolean isSame(KagerowVirtualFileObject target) {
			if (Objects.nonNull(target)) {
				return Objects.equals(this.createTime(), target.createTime())
						&& Objects.equals(this.datAddr(), target.datAddr())
						&& Objects.equals(this.idxAddr(), target.idxAddr())
						&& Objects.equals(this.idxSize(), target.idxSize())
						&& Objects.equals(this.binaryName(), target.binaryName())
						&& Objects.equals(this.synonym(), target.synonym())
						&& Objects.equals(this.schema(), target.schema())
						&& Objects.equals(this.uri().get(), target.uri().get())
						&& Objects.deepEquals(this.headerData(), target.headerData())
						&& Objects.deepEquals(this.dataType(), target.dataType())
						&& Objects.deepEquals(this.dataSize(), target.dataSize());
			}
			return false;
		};

		/**
		 * 対象の仮想ファイルオブジェクトの共通定義に基づいたハッシュコードを生成します
		 * @return ハッシュコード
		 */
		public default int toHash() {
			int result = Objects.hash(
					this.createTime(),
					this.datAddr(),
					this.idxAddr(),
					this.idxSize(),
					this.binaryName(),
					this.synonym(),
					this.schema(),
					this.uri().get());
			result = 31 * result + Arrays.hashCode(this.headerData());
			result = 31 * result + Arrays.hashCode(this.dataType());
			result = 31 * result + Arrays.hashCode(this.dataSize());
			return result;
		}

		/**
		 * 基本データ構造
		 * @param createTime インスタンス生成日時
		 * @param headerData ヘッダー情報リスト
		 * @param dataType データタイプ情報リスト
		 * @param dataSize データサイズ情報リスト
		 * @param datAddr データファイルリンク
		 * @param datSize データファイルサイズ(バイト)
		 * @param idxAddr インデックスファイルリンク
		 * @param idxSize インデックスファイルサイズ(バイト)
		 * @param binaryName 物理名
		 * @param synonym 論理名
		 * @param schema スキーマ
		 * @param uri URI
		 */
		public record BasicFileObject(
				Instant createTime,
				String[] headerData,
				KagerowDataType[] dataType,
				long[] dataSize,
				String datAddr,
				BigInteger datSize,
				String idxAddr,
				BigInteger idxSize,
				String binaryName,
				String synonym,
				String schema,
				AtomicReference<String> uri) implements KagerowVirtualFileObject, Comparable<BasicFileObject> {

			/** シリアライズID */
			@Serial
			public static final long serialVersionUID = 227405099260436334L;

			/** {@inheritDoc} */
			@Override
			public final long indexCount() {
				return idxSize.divide(idxBlockSize).longValue();
			}

			/** {@inheritDoc} */
			@Override
			public int compareTo(BasicFileObject o) {
				return createTime.compareTo(o.createTime);
			}

			/** {@inheritDoc} */
			@Override
			public BigInteger idxBlockSize() {
				return idxBlockSize;
			}

			/** {@inheritDoc} */
			@Override
			public final boolean equals(Object arg0) {
				if (arg0 instanceof BasicFileObject target) {
					return isSame(target);
				}
				return false;
			}

			/** {@inheritDoc} */
			@Override
			public final int hashCode() {
				return toHash();
			}

		}

		/**
		 * セキュアデータ構造
		 * @param createTime インスタンス生成日時
		 * @param headerData ヘッダー情報リスト
		 * @param dataType データタイプ情報リスト
		 * @param dataSize データサイズ情報リスト
		 * @param datAddr データファイルリンク
		 * @param datSize データファイルサイズ(バイト)
		 * @param idxAddr インデックスファイルリンク
		 * @param idxSize インデックスファイルサイズ(バイト)
		 * @param binaryName 物理名
		 * @param synonym 論理名
		 * @param schema スキーマ
		 * @param password パスワード
		 * @param alias キーエイリアス
		 * @param uri URI
		 */
		public record SecureFileObject(
				Instant createTime,
				String[] headerData,
				KagerowDataType[] dataType,
				long[] dataSize,
				String datAddr,
				BigInteger datSize,
				String idxAddr,
				BigInteger idxSize,
				String binaryName,
				String synonym,
				String schema,
				String password,
				String alias,
				AtomicReference<String> uri) implements KagerowVirtualFileObject, Comparable<SecureFileObject> {

			/** シリアライズID */
			@Serial
			public static final long serialVersionUID = 227405099260436335L;

			/** 物理アドレスセグメントサイズ */
			public static final BigInteger idxBlockSize = BigInteger.valueOf(Long.BYTES);

			/** {@inheritDoc} */
			@Override
			public final long indexCount() {
				return idxSize.divide(idxBlockSize).longValue();
			}

			/** {@inheritDoc} */
			@Override
			public int compareTo(SecureFileObject o) {
				return createTime.compareTo(o.createTime);
			}

			/** {@inheritDoc} */
			@Override
			public BigInteger idxBlockSize() {
				return idxBlockSize;
			}

			/** {@inheritDoc} */
			@Override
			public final boolean equals(Object arg0) {
				if (arg0 instanceof SecureFileObject target) {
					return isSame(target)
							&& Objects.equals(alias, target.alias)
							&& Objects.equals(password, target.password);
				}
				return false;
			}

			/** {@inheritDoc} */
			@Override
			public final int hashCode() {
				int result = Objects.hash(alias, password);
				result = 31 * result + toHash();
				return result;
			}

		}

	}

	/**
	 * インデックスに対応する相対的なコンテンツを取得します
	 * @param index 相対インデックス
	 * @return 対象コンテンツ
	 * @throws NamingException コンテキスト取得失敗
	 */
	public KagerowVirtualFileObject get(int index) throws NamingException;

	/** {@inheritDoc} */
	@Override
	public KagerowVirtualFileObject lookup(Name name) throws NamingException;

	/**
	 * コンテンツ内部コンテキストのサイズを取得します
	 * @return サイズ
	 */
	public int contentSize();

	/** {@inheritDoc} */
	@Override
	public default KagerowVirtualFileObject lookup(String name) throws NamingException {
		return lookup(toName(name));
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

	/**
	 * 対象のデータファイルから世代情報を取り出します
	 * @param target データファイル
	 * @return 世代情報
	 */
	public static String getGeneration(KagerowVirtualFileObject target) {
		return target.binaryName().split("#", -1)[1];
	}

	/**
	 * 対象のデータファイルからテーブル名称を取り出します
	 * @param target データファイル
	 * @return 世代情報
	 */
	public static String getTableName(KagerowVirtualFileObject target) {
		return target.binaryName().split("#", -1)[0];
	}

}

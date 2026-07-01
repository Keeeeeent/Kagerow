package com.sakulabo.core.Kagerow.Contents;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.OperationNotSupportedException;

import com.sakulabo.core.Common.StringUtils;
import com.sakulabo.core.Kagerow.Adapter.KagerowExecutionPlanAdapter;
import com.sakulabo.core.Kagerow.Contents.Impl.KagerowPluginContentImpl;
import com.sakulabo.core.Kagerow.Utilities.KagerowDBMode;
import com.sakulabo.regulation.annotation.KagerowPlugin.PluginType;
import com.sakulabo.regulation.spi.PluginAdapter;

/**
 * Kagerowアプリケーションのコンテンツ拡張インターフェースです<br/>
 * このインターフェースの実装クラスでは、Kagerowのプラグイン機能を提供します<br/>
 * このクラスは通常のコンテキストが定義するメソッドをサポートしておりません
 * 
 * @author keeeeeent
 */
public sealed interface KagerowPluginContent
		extends KagerowContents, PluginAdapter
		permits KagerowPluginContentImpl {

	/**
	 * プラグイン情報の構造体です
	 * @param packageName パッケージ名称
	 * @param pluginName プラグイン名称
	 * @param majorVersion メジャーバージョン
	 * @param minorVersion マイナーバージョン
	 * @param patchVersion パッチバージョン
	 * @param param パラメータ情報
	 */
	public static record PluginInfo(
			String packageName,
			String pluginName,
			int majorVersion,
			int minorVersion,
			int patchVersion,
			Map<PluginType, List<PluginParamInfo>> param) {

		/**
		 * バージョン文字列（n.n.n形式）を取得します
		 * @return バージョン文字列
		 */
		public String toVersion() {
			return String.join(StringUtils.DOT_STR,
					String.valueOf(majorVersion),
					String.valueOf(minorVersion),
					String.valueOf(patchVersion));
		}

	}

	/**
	 * プラグインパラメータの構造体です
	 * @param name パラメータ名称
	 * @param defaultValue デフォルト値
	 * @param required 必須フラグ
	 */
	public static record PluginParamInfo(
			String name,
			String defaultValue,
			boolean required) {
	}

	/**
	 * プラグイン実行の規定データ構造体です
	 */
	public static sealed interface Data {

		/**
		 * パラメータ受取メソッド
		 * @return パラメータ
		 */
		public Map<String, String> param();

		/**
		 * 実行カウントメソッド
		 * @return 実行カウント
		 */
		public CountDownLatch latch();

		/**
		 * 実行アダプター実行
		 * @return 実行アダプター
		 */
		public Optional<KagerowExecutionPlanAdapter> planAdapter();

		/**
		 * プラグイン実行時IDを取得します
		 * @return プラグイン実行時ID
		 */
		public String id();

		/**
		 * 通知メソッド
		 */
		public default void notice() {
			latch().countDown();
		}

		/**
		 * 入力処理の規定データクラスです
		 * @param param パラメータ
		 * @param mode モード
		 * @param connection コネクション
		 * @param latch カウンター
		 * @param id プラグイン実行時ID
		 * @param planAdapter 実行アダプター
		 */
		public static record InputData(
				Map<String, String> param,
				KagerowDBMode mode,
				Connection connection,
				CountDownLatch latch,
				String id,
				Optional<KagerowExecutionPlanAdapter> planAdapter) implements Data {

			/**
			 * コンパクトコンストラクタ
			 */
			public InputData {

				if ((Objects.isNull(latch))) {
					latch = new CountDownLatch(10);
				}

			}

			/**
			 * コンストラクタのオーバーロード実装です
			 * @param latchCount カウント数
			 * @param mode モード
			 * @param connection コネクション
			 */
			public InputData(int latchCount, KagerowDBMode mode, Connection connection) {
				this(new HashMap<>(), mode, connection,
						new CountDownLatch(latchCount), StringUtils.EMPTY, Optional.empty());
			}

		}

		/**
		 * 出力処理の規定データクラスです
		 * @param data データ
		 * @param param パラメータ
		 * @param latch カウンター
		 * @param id プラグイン実行時ID
		 * @param planAdapter 実行アダプター
		 */
		public static record OutputData(
				List<KagerowRowSet> data,
				Map<String, String> param,
				CountDownLatch latch,
				String id,
				Optional<KagerowExecutionPlanAdapter> planAdapter) implements Data {

			/**
			 * コンパクトコンストラクタ
			 */
			public OutputData {

				if (Objects.isNull(data)) {
					data = new ArrayList<>();
				}

				if ((Objects.isNull(latch))) {
					latch = new CountDownLatch(10);
				}

			}

			/**
			 * コンストラクタのオーバーロード実装です
			 * @param latchCount カウント数
			 */
			public OutputData(int latchCount) {
				this(new ArrayList<>(), new HashMap<>(), new CountDownLatch(latchCount),
						StringUtils.EMPTY, Optional.empty());
			}

		}

	}

	/**
	 * プラグインの実行要求を専用スレッドにて実行します
	 * @param data データ交換オブジェクト
	 */
	public abstract void submit(Data data);

	/**
	 * 指定したプラグインタイプが対応しているか判定します
	 * @param type プラグインタイプ
	 * @return 判定結果
	 */
	public abstract boolean isSupportType(PluginType type);

	/**
	 * プラグイン情報にコンテンツを変換します
	 * @return プラグイン情報
	 */
	public abstract PluginInfo toPluginInfo();

	/** {@inheritDoc} */
	@Override
	@Deprecated
	public default void rebind(Name name, Object obj) throws NamingException {
		throw new OperationNotSupportedException();
	}

	/** {@inheritDoc} */
	@Override
	@Deprecated
	public default void unbind(Name name) throws NamingException {
		throw new OperationNotSupportedException();
	}

	/** {@inheritDoc} */
	@Override
	@Deprecated
	public default void rename(Name oldName, Name newName) throws NamingException {
		throw new OperationNotSupportedException();
	}

	/** {@inheritDoc} */
	@Override
	@Deprecated
	public default NamingEnumeration<NameClassPair> list(Name name) throws NamingException {
		throw new OperationNotSupportedException();
	}

	/** {@inheritDoc} */
	@Override
	@Deprecated
	public default NamingEnumeration<Binding> listBindings(Name name) throws NamingException {
		throw new OperationNotSupportedException();
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
	public default Object lookup(Name name) throws NamingException {
		throw new OperationNotSupportedException();
	}

	/** {@inheritDoc} */
	@Override
	@Deprecated
	public default void bind(Name name, Object obj) throws NamingException {
		throw new OperationNotSupportedException();
	}

	/** {@inheritDoc} */
	@Override
	void close() throws NamingException;

}

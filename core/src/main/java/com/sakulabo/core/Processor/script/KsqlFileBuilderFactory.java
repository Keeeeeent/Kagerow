package com.sakulabo.core.Processor.script;

import java.nio.file.Path;

import com.sakulabo.core.Kagerow.Exception.AppLogicException;
import com.sakulabo.core.Kagerow.Exception.KFileParseException;
import com.sakulabo.core.Kagerow.Exception.KSQLParseException;
import com.sakulabo.core.Kagerow.Utilities.KagerowFileVersion;
import com.sakulabo.core.Kagerow.Utilities.KagerowScriptAccessor;

/**
 * Kagerowスクリプトファイル生成ビルダー規定クラスです
 * @author keeeeeent
 */
public sealed abstract class KsqlFileBuilderFactory {

	/** ファイルパス */
	protected final Path path;

	/**
	 * デフォルトコンストラクタ
	 * @param path ファイルパス
	 */
	protected KsqlFileBuilderFactory(Path path) {
		this.path = path;
	}

	/**
	 * KSQLファイルReaderを生成します
	 * @return インスタンス
	 * @throws KFileParseException KFile解析エラー
	 * @throws KSQLParseException KSQL解析エラー
	 * @throws AppLogicException アプリケーションロジック不正
	 */
	public abstract BasicKsqlFileReader newBasicReader()
			throws KFileParseException, KSQLParseException, AppLogicException;

	/**
	 * KSQLファイルWriterを生成します
	 * @param scriptAccessor スクリプトインスタンス
	 * @return インスタンス
	 */
	public abstract BasicKsqlFileWriter newBasicWriter(KagerowScriptAccessor scriptAccessor);

	/**
	 * 読み込みストラテジーを取得します
	 * @return 読み込みストラテジー
	 */
	public abstract KsqlReaderStrategy getReaderStrategy();

	/**
	 * 書き出しストラテジーを取得します
	 * @return 書き出しストラテジー
	 */
	public abstract KsqlWriterStrategy getWriterStrategy();

	/**
	 * 指定されたバージョンに応じたKSQLファイルファクトリを生成します
	 * @param version KSQLファイルバージョン
	 * @param path KSQLファイルパス
	 * @return ファクトリインスタンス
	 */
	public static final KsqlFileBuilderFactory getInstance(KagerowFileVersion version, Path path) {
		return switch (version) {
		case RELEASE_0 -> new KsqlFileBuilderFactoryV1(path);
		case null -> throw new IllegalArgumentException("Unexpected value: " + version);
		};
	}

	/**
	 * KSQLバージョン1向けのファクトリクラスです
	 */
	private static non-sealed class KsqlFileBuilderFactoryV1 extends KsqlFileBuilderFactory {

		/**
		 * デフォルトコンストラクタ
		 * @param path ファイルパス
		 */
		protected KsqlFileBuilderFactoryV1(Path path) {
			super(path);
		}

		/** {@inheritDoc} */
		@Override
		public BasicKsqlFileReader newBasicReader()
				throws KFileParseException, KSQLParseException, AppLogicException {
			return new BasicKsqlFileReader(path, getReaderStrategy());
		}

		/** {@inheritDoc} */
		@Override
		public BasicKsqlFileWriter newBasicWriter(KagerowScriptAccessor scriptAccessor) {
			return new BasicKsqlFileWriter(scriptAccessor, path, getWriterStrategy());
		}

		/** {@inheritDoc} */
		@Override
		public KsqlReaderStrategy getReaderStrategy() {
			return new KsqlReaderStrategy.KsqlReaderStrategyV1();
		}

		/** {@inheritDoc} */
		@Override
		public KsqlWriterStrategy getWriterStrategy() {
			return new KsqlWriterStrategy.KsqlWriterStrategyV1();
		}

	}
}

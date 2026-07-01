package com.sakulabo.core.Processor.archive;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.function.Consumer;

import javax.naming.Name;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NamingException;
import javax.naming.NotContextException;

import com.sakulabo.core.Common.ApplicationWordDictionary;
import com.sakulabo.core.Common.ErrorMessage;
import com.sakulabo.core.Common.URINameParser;
import com.sakulabo.core.Kagerow.KagerowApplication;
import com.sakulabo.core.Kagerow.Contents.KagerowVirtualFileContent;
import com.sakulabo.core.Kagerow.Contents.KagerowVirtualFileContent.KagerowVirtualFileObject;
import com.sakulabo.core.Kagerow.Context.KagerowVirtualDirContext;
import com.sakulabo.core.Kagerow.Context.KagerowVirtualFileContext;
import com.sakulabo.core.Kagerow.Exception.AppLogicException;
import com.sakulabo.core.Kagerow.Exception.VirtualFileConstructionFailException;
import com.sakulabo.core.Kagerow.Utilities.KagerowChunkCreater.ChunkCreateMode;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;
import com.sakulabo.core.Kagerow.Utilities.KagerowTransaction;
import com.sakulabo.core.Provides.ArchiveSystemProvider;

/**
 * KDB構築インスタンスの規定クラスです
 * 
 * @author keeeeeent
 */
public abstract sealed class AppVirtualFileCreater permits BasicVirtualFileCreater, SecureVirtualFileCreater {

	/** 空のオブザーバー実装 */
	public static final Consumer<Double> EMPTY_CONSUMER = new Consumer<Double>() {
		@Override
		public void accept(Double t) {
			;
		}
	};

	/** URIパーサー */
	protected final URINameParser parser;
	/** 実行モード */
	protected final ChunkCreateMode mode;
	/** スキーマファイル名 */
	protected final String schema;
	/** 入力ファイル */
	protected final Path path;
	/** 入力ファイル文字コード */
	protected final Charset charset;
	/** ヘッダー有無 */
	protected final boolean isHeader;
	/** テーブル名称のシノニム */
	protected final String synonym;
	/** 進捗更新オブザーバー */
	protected final Consumer<Double> observer;

	/**
	 * 共通コンストラクタ
	 * @param mode 実行モード
	 * @param schema スキーマファイル名
	 * @param path 入力ファイル
	 * @param charset 入力ファイル文字コード
	 * @param isHeader ヘッダー有無
	 * @param synonym テーブル名称のシノニム
	 * @param observer 進捗更新オブザーバー
	 * @throws IOException ファイル読み込み失敗、文字コード判定不可
	 */
	protected AppVirtualFileCreater(
			ChunkCreateMode mode,
			String schema,
			Path path,
			Charset charset,
			boolean isHeader,
			String synonym,
			Consumer<Double> observer) throws IOException {

		// フィールド初期化
		this.mode = Objects.requireNonNull(mode);
		this.schema = schema;
		this.path = Objects.requireNonNull(path);
		this.isHeader = isHeader;
		this.synonym = synonym;

		// 文字コード自動判定
		if (Objects.isNull(charset)) {
			this.charset = getCharset(path);
		} else {
			this.charset = charset;
		}

		// オブザーバーの設定
		if (Objects.isNull(observer)) {
			this.observer = EMPTY_CONSUMER;
		} else {
			this.observer = observer;
		}

		// URIパーサー生成
		parser = new URINameParser(schema);
	}

	/**
	 * 仮想ファイルオブジェクトを生成します
	 * @return 仮想ファイルオブジェクト
	 * @throws IOException データセット生成失敗、取得ファイル不正、初期化エラー
	 * @throws AppLogicException コンテキスト取得失敗
	 */
	public abstract KagerowVirtualFileObject createVirtualFileObject() throws AppLogicException, IOException;

	/**
	 * 指定されたファイルパスから文字コードを判定します
	 * @param path 判定対象
	 * @return 判定結果文字コードインスタンス
	 * @throws IOException ファイル読み込み失敗、文字コード判定不可
	 */
	public final Charset getCharset(Path path) throws IOException {
		// BOMを解析し文字コードを生成
		BomHandler bomHandler = new BomHandler(path);
		Charset charset = bomHandler.getCharset();
		if (Objects.isNull(charset)) {
			throw new IOException(
					ErrorMessage.CODE_004.getMessage(ApplicationWordDictionary.WCD_0002.getMessage()));
		}
		return charset;
	}

	/**
	 * Kagerowが管理する仮想ディレクトリコンテキストを取得します
	 * @return 仮想ディレクトリコンテキスト
	 * @throws NamingException コンテキストが見つからない場合
	 */
	public final KagerowVirtualDirContext getVirtualDirContext() throws NamingException {
		// まずはスキーマファイルとバインドされているインスタンスを探す
		KagerowVirtualFileContext context = (KagerowVirtualFileContext) KagerowApplication.getInstance()
				.getContext().lookup(KagerowVirtualFileContext._NAME);
		try {
			return context.lookup(schema);
		} catch (NotContextException e) {
			return context.createSubcontext(schema);
		}
	}

	/**
	 * 仮想DB物理ファイルを生成、管理下に配置します
	 * @return 物理ファイルURI
	 * @throws NameAlreadyBoundException 既に同等の仮想DB物理ファイルが生成されている場合
	 * @throws VirtualFileConstructionFailException 仮想DB物理ファイル生成失敗
	 */
	public final URI construction() throws NameAlreadyBoundException, VirtualFileConstructionFailException {

		try {

			// 仮想ディレクトリコンテキストを取得
			KagerowVirtualDirContext baseContext = getVirtualDirContext();

			// データ生成処理実行
			KagerowVirtualFileObject data = createVirtualFileObject();

			// バインド先URI生成
			Name name = parser.parse(data.binaryName());
			URI result = parser.toURI(name);

			// 名称の分離
			String bodyName = name.get(1);
			String hederName = parser.createHeaderBinaryPath(data.binaryName());

			// トランザクション制御開始
			final KagerowTransaction tran = ArchiveSystemProvider.getTransaction(result);

			try (tran) {

				// シノニムの指定がある場合はコンテキストに追加する
				if (Objects.nonNull(synonym)) {
					if (!baseContext.isExistSynonym(synonym, hederName)) {
						if (!baseContext.getSynonymMapList().containsKey(synonym)) {
							// 別オブジェクトでバインドされていない場合安全に登録可能なためシノニムを追加
							baseContext.addSynonymMapList(hederName, synonym);
						}
					} else {
						// 既に別オブジェクトで登録済みの場合例外をスロー
						throw new NameAlreadyBoundException(ErrorMessage.CODE_017.getMessage(synonym));
					}
				}

				// 次にスキーマオブジェクト内部のヘッダーバインドインスタンスを探す
				KagerowVirtualFileContent headerContext;
				try {
					headerContext = baseContext.lookup(hederName);
				} catch (NotContextException e) {
					headerContext = baseContext.createSubcontext(hederName);
				}

				// 生成したデータをバインドする
				headerContext.bind(bodyName, data);

				// トランザクション制御終了
				tran.commit();

				// URIの返却
				return result;

			} catch (NameAlreadyBoundException exp) {

				try {
					// 既にバインドされている場合、生成済みのデータをロールバック
					{
						// データファイル削除
						String uri = data.datAddr();
						Path paht = Paths.get(URI.create(uri));
						Files.delete(paht);
					}
					{
						// インデックスファイル削除
						String uri = data.idxAddr();
						Path paht = Paths.get(URI.create(uri));
						Files.delete(paht);
					}
				} catch (IOException ioe) {
					// ファイル削除で失敗した場合は制御された例外として追加
					exp.addSuppressed(ioe);
				}

				// トランザクションロールバック
				tran.rollback(exp);

				// ログ出力
				KagerowLogger.newAppLogger().err(exp);

				throw exp;
			}

		} catch (AppLogicException | NamingException | IOException | URISyntaxException e) {
			if (e instanceof NameAlreadyBoundException exp) {
				throw exp;
			}
			throw new VirtualFileConstructionFailException(e);
		}

	}

}

package com.sakulabo.core.Processor.archive;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.naming.Name;

import com.sakulabo.core.Common.ErrorMessage;
import com.sakulabo.core.Common.StringUtils;
import com.sakulabo.core.Kagerow.Contents.KagerowVirtualFileContent.KagerowDataType;
import com.sakulabo.core.Kagerow.Contents.KagerowVirtualFileContent.KagerowVirtualFileObject;
import com.sakulabo.core.Kagerow.Exception.AppLogicException;
import com.sakulabo.core.Kagerow.Utilities.KagerowFileBodyReader;
import com.sakulabo.core.Kagerow.Utilities.KagerowFileHeaderReader;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;

/**
 * ファイルからchunk毎のアーカイブファイル群を生成します<br/>
 * バイナリファイル:物理データファイル（.dat）
 * インデックスファイル:物理データアクセスインデックスファイル（.idx）
 * 
 * @author keeeeeent
 * @param <T> 
 */
abstract class AppChunkCreater<T extends KagerowVirtualFileObject> {

	/** 入力ファイルパス */
	protected final Path path;
	/** ヘッダー有無 */
	protected final boolean isHeader;
	/** 読み込み文字コード */
	protected final Charset charset;
	/** スキーマ名称 */
	protected final String schema;
	/** 進捗更新オブザーバー */
	protected Consumer<Double> observer;

	/**
	 * 内部データ構造
	 * @param datAddr データファイルパス
	 * @param datSize データファイルサイズ
	 * @param idxAddr インデックスファイルパス
	 * @param idxSize インデックスファイルサイズ
	 * @param dataType データタイプ
	 * @param dataSize データサイズ
	 */
	protected static record tmpDataSet(
			String datAddr,
			BigInteger datSize,
			String idxAddr,
			BigInteger idxSize,
			KagerowDataType[] dataType,
			long[] dataSize) {
	};

	/**
	 * データファイル生成クラス定義
	 */
	protected class execObject {

		/** ライターインスタンス */
		ChunkWriter chunkWriter;
		/** リーダーインスタンス */
		KagerowFileBodyReader bodyReader;

		/**
		 * デフォルトコンストラクタ
		 * @param digest ハッシュ関数
		 * @throws AppLogicException チャンクファイル不正
		 * @throws IOException チャンクライター生成失敗
		 */
		execObject(MessageDigest digest) throws AppLogicException, IOException {
			// フィールド初期化
			execObject.this.bodyReader = createBodyReader(path, charset, isHeader);
			execObject.this.chunkWriter = createChunkWriter(digest, charset, schema);
			// オブザーバー設定
			if (Objects.nonNull(observer)) {
				execObject.this.bodyReader.setObserver(observer);
			}
		}

		/**
		 * チャンク構築処理実行
		 * @return 実行結果
		 * @throws IOException ロード失敗
		 */
		public tmpDataSet call() throws IOException {

			// 返却用インスタンス初期化
			tmpDataSet result = null;
			try {

				// ファイル読み込み（CSVデータレコード取得）
				String[] dataRecord;
				while ((dataRecord = execObject.this.bodyReader.readLine()) != null) {
					// データ書き込み
					execObject.this.chunkWriter.write(dataRecord);
				}

				// リソースクローズ
				// 本来であればリソースの自動クローズを行うべきだが、シリアライズ前に早期クローズした上で
				// バイナリデータを書き込みデシリアライズした時の差分をなくす
				execObject.this.bodyReader.close();
				execObject.this.chunkWriter.close();

				// 返却用インスタンス生成
				result = new tmpDataSet(
						execObject.this.chunkWriter.getDatOutputPath(),
						execObject.this.chunkWriter.getDatOutputSize(),
						execObject.this.chunkWriter.getIdxOutputPath(),
						execObject.this.chunkWriter.getIdxOutputSize(),
						execObject.this.bodyReader.getType(),
						execObject.this.bodyReader.getSize());

			} catch (IOException e) {
				// チャンクファイルロールバック
				execObject.this.chunkWriter.rollback();
				throw e;
			}

			return result;
		}

	}

	/**
	 * デフォルトコンストラクタ
	 * @param schema スキーマ名称
	 * @param path 入力ファイル
	 * @param charset 読み込み文字コード
	 * @param isHeader ヘッダー有無
	 * @throws AppLogicException 入力ファイルが存在しない場合
	 */
	AppChunkCreater(String schema, Path path, Charset charset, boolean isHeader) throws AppLogicException {
		if (Files.notExists(path)) {
			throw new AppLogicException(ErrorMessage.CODE_005.getMessage(path));
		}
		this.path = path;
		this.isHeader = isHeader;
		this.charset = charset;
		this.schema = schema;
	}

	/**
	 * チャンクライターを生成します
	 * @param digest ハッシュ関数
	 * @param charset 文字コード
	 * @param schema スキーマ
	 * @return チャンクライター
	 * @throws AppLogicException チャンクファイル不正
	 * @throws IOException チャンクライター生成失敗
	 */
	protected abstract ChunkWriter createChunkWriter(
			MessageDigest digest,
			Charset charset,
			String schema) throws AppLogicException, IOException;

	/**
	 * ボディリーダーを生成します
	 * @param path 入力元パス
	 * @param charset 読み込み文字コード
	 * @param isHeader ヘッダー有無
	 * @return ボディリーダー
	 * @throws IOException 取得ファイル不正、初期化エラー
	 */
	protected abstract KagerowFileBodyReader createBodyReader(
			Path path,
			Charset charset,
			boolean isHeader) throws IOException;

	/**
	 * ヘッダーリーダーを生成します
	 * @param path 入力元パス
	 * @param charset 読み込み文字コード
	 * @param isHeader ヘッダー有無
	 * @return ヘッダーリーダー
	 * @throws IOException 取得ファイル不正、初期化エラー
	 */
	protected abstract KagerowFileHeaderReader createHeaderReader(
			Path path,
			Charset charset,
			boolean isHeader) throws IOException;

	/**
	 * データセットを構築します
	 * @param synonym シノニム
	 * @param bodyData ボディリーダー
	 * @param hederReader ヘッダーリーダー
	 * @param pathCreater パスクリエイター
	 * @return データセット
	 * @throws IOException データセット生成失敗
	 * @throws AppLogicException コンテキスト取得失敗
	 */
	protected abstract T createKagerowVirtualFileObject(
			String synonym,
			tmpDataSet bodyData,
			FileReader hederReader,
			HashPathCreater pathCreater) throws AppLogicException, IOException;

	/**
	 * データセットを構築します
	 * @param synonym シノニム
	 * @return データセット
	 * @throws IOException データセット生成失敗、取得ファイル不正、初期化エラー
	 * @throws AppLogicException コンテキスト取得失敗
	 */
	public T create(String synonym) throws AppLogicException, IOException {

		// 初期化処理実行
		HashPathCreater pathCreater;
		try {
			pathCreater = new HashPathCreater();
		} catch (NoSuchAlgorithmException e) {
			/**
			 * NoSuchAlgorithmException
			 * 初期化の時点で不正名称はバリデーションしているため
			 * ここでは例外は発生しない
			 */
			KagerowLogger.newAppLogger().err(e);
			return null;
		}
		MessageDigest hashData = pathCreater.getMessageDigest();
		execObject exec = new execObject(hashData);

		try (FileReader hederReader = createHeaderReader(path, charset, isHeader);) {

			// ボディー情報取得
			tmpDataSet bodyData = exec.call();

			// データセット精鋭
			T data = createKagerowVirtualFileObject(synonym, bodyData, hederReader, pathCreater);

			return data;
		}
	}

	/**
	 * テーブル名称を生成します
	 * @param name パス
	 * @return テーブル名称
	 */
	protected String toTableName(Name name) {
		Enumeration<String> names = name.getAll();
		List<String> nameList = new ArrayList<>();
		while (names.hasMoreElements()) {
			nameList.add(names.nextElement());
		}
		return nameList.stream()
				.collect(Collectors.joining(StringUtils.SHARP_DELIMIT));
	}

	/**
	 * 進捗更新コンシューマーを設定します
	 * @param observer 進捗更新コンシューマー
	 */
	public final void setObserver(Consumer<Double> observer) {
		this.observer = observer;
	}

}

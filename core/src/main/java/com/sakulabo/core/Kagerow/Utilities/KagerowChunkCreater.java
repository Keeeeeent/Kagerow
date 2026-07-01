package com.sakulabo.core.Kagerow.Utilities;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.function.Consumer;

import com.sakulabo.core.Kagerow.Contents.KagerowVirtualFileContent.KagerowVirtualFileObject;
import com.sakulabo.core.Kagerow.Contents.KagerowVirtualFileContent.KagerowVirtualFileObject.BasicFileObject;
import com.sakulabo.core.Kagerow.Contents.KagerowVirtualFileContent.KagerowVirtualFileObject.SecureFileObject;
import com.sakulabo.core.Kagerow.Exception.AppLogicException;
import com.sakulabo.core.Processor.archive.BasicChunkCreater;
import com.sakulabo.core.Processor.archive.CSVFileReaderFactory;
import com.sakulabo.core.Processor.archive.SecureChunkCreater;
import com.sakulabo.core.Processor.archive.TSVFileReaderFactory;

/**
 * Kagerowアプリケーションのチャンク生成機能規定インターフェイスです
 * 
 * @author keeeeeent
 */
public sealed interface KagerowChunkCreater<T extends KagerowVirtualFileObject>
		permits BasicChunkCreater, SecureChunkCreater {

	/**
	 * KagerowChunkCreater実行モード
	 */
	public enum ChunkCreateMode {

		/** CSVモード */
		CSV,
		/** TSVモード */
		TSV,
		;

	}

	/**
	 * 最も基本的なインスタンスを生成します
	 * 
	 * @param mode 実行モード
	 * @param schema スキーマファイル名
	 * @param path 入力ファイル
	 * @param charset 入力ファイル文字コード
	 * @param isHeader ヘッダー有無
	 * @return KDBチャンク生成インスタンス
	 * @throws AppLogicException 入力ファイルが存在しない場合
	 */
	public static KagerowChunkCreater<BasicFileObject> newBasicInstance(
			ChunkCreateMode mode,
			String schema,
			Path path,
			Charset charset,
			boolean isHeader)
			throws AppLogicException {
		return new BasicChunkCreater(schema, path, charset, isHeader, createFileReaderFactory(mode));
	}

	/**
	 * 最も基本的なインスタンスを生成します
	 * 
	 * @param mode 実行モード
	 * @param schema スキーマファイル名
	 * @param path 入力ファイル
	 * @param charset 入力ファイル文字コード
	 * @param isHeader ヘッダー有無
	 * @return KDBチャンク生成インスタンス
	 * @throws AppLogicException 入力ファイルが存在しない場合
	 */
	public static KagerowChunkCreater<SecureFileObject> newSecureInstance(
			ChunkCreateMode mode,
			String schema,
			Path path,
			Charset charset,
			boolean isHeader)
			throws AppLogicException {
		return new SecureChunkCreater(schema, path, charset, isHeader, createFileReaderFactory(mode));
	}

	/**
	 * 名部向けリーダーファクトリメソッドです
	 * @param mode 実行モード
	 * @return ファイルreader抽象ファクトリ
	 */
	private static KagerowFileReaderFactory createFileReaderFactory(ChunkCreateMode mode) {
		return switch (mode) {
		case CSV -> new CSVFileReaderFactory();
		case TSV -> new TSVFileReaderFactory();
		};
	}

	/**
	 * データセットを生成します
	 * @param synonym シノニム
	 * @return チャンクアクセスオブジェクト
	 * @throws IOException データセット生成失敗、取得ファイル不正、初期化エラー
	 * @throws AppLogicException コンテキスト取得失敗
	 */
	T create(String synonym) throws AppLogicException, IOException;

	/**
	 * 進捗更新コンシューマーを設定します
	 * @param observer 進捗更新コンシューマー
	 */
	public void setObserver(Consumer<Double> observer);

}

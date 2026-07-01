package com.sakulabo.core.Processor.archive;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import com.sakulabo.core.Common.StringUtils;

/**
 * ファイルの読み取り基底実装提供クラスです
 * 
 * @author keeeeeent
 */
public abstract class FileReader implements AutoCloseable {

	/** 入力元パス */
	protected final Path path;
	/** 読み込み文字コード */
	protected final Charset charset;
	/** ヘッダー有無 */
	protected final boolean isHeader;
	/** ランダムアクセスインスタンス */
	protected final RandomAccessFile access;
	/** ストリームインスタンス */
	protected final InputStream input;
	/** エンコーダー */
	protected InputStreamReader converter;
	/** 区切り文字 */
	protected char delimit;
	/** 進捗更新オブザーバー */
	protected Consumer<Double> observer = _ -> {
		;
	};

	/**
	 * デフォルトコンストラクタ
	 * @param delimit 区切り文字
	 * @param path 入力元パス
	 * @param charset 読み込み文字コード
	 * @param isHeader ヘッダー有無
	 * @throws IOException 取得ファイル不正、初期化エラー
	 */
	protected FileReader(char delimit, Path path, Charset charset, boolean isHeader) throws IOException {
		this.delimit = delimit;
		this.path = path;
		this.charset = charset;
		this.isHeader = isHeader;
		this.access = new RandomAccessFile(path.toFile(), StringUtils.RAND_READ_ONLY);
		BomHandler bomHandler = new BomHandler(path);
		int skipByte = bomHandler.skipByte();
		this.access.seek(skipByte);
		this.input = new FileInputStream(this.access.getFD());
		this.converter = new InputStreamReader(this.input, this.charset);
	}

	/**
	 * 1行単位で読み込み
	 * @return CSVデータレコード
	 * @throws IOException データ取得エラー
	 */
	protected String[] readLine() throws IOException {

		// 読み込み開始
		char line = 0x00;
		List<String> recordList = new ArrayList<>();
		StringBuffer buffer = new StringBuffer();
		boolean escape = false;
		char[] charBuffer = new char[2];
		double beforefileReadProgress = 0;

		// ファイルサイズ取得
		double fileSize = Files.size(path);

		while (readChar(charBuffer)) {

			// 1文字読み込み
			line = charBuffer[0];

			// 進捗更新
			double fileReadProgress = (double) access.getFilePointer() / fileSize;
			if (Double.compare(beforefileReadProgress, fileReadProgress) != 0) {
				beforefileReadProgress = fileReadProgress;
				observer.accept(fileReadProgress * 100);
			}

			// エスケープ処理対象か判定
			if (line == StringUtils.DOUBLE_QUOTATION) {
				if (escape) {
					if (!readChar(charBuffer)) {
						break;
					} else {
						// 1文字読み込み
						line = charBuffer[0];
					}
					// エスケープモードの場合
					if (line != StringUtils.DOUBLE_QUOTATION) {
						// 読み飛ばしの結果、ダブルクオーテーションのエスケープではなかった場合
						// エスケープ対象のフラグを反転
						escape ^= true;
					}
				} else {
					// エスケープモードではない場合
					// エスケープ対象のフラグを反転
					escape ^= true;
					// 読み込み対象ではないため読み飛ばし
					continue;
				}
			}

			// カンマの場合一度区切る
			if (line == this.delimit && Boolean.FALSE.compareTo(escape) == 0) {
				// バッファリフレッシュ
				refresh(buffer, recordList);
				// バッファリセット
				buffer.delete(0, buffer.length());
				// ループ処理続行
				continue;
			}

			// 改行コードの場合
			if ((line == StringUtils.CR || line == StringUtils.LF) && Boolean.FALSE.compareTo(escape) == 0) {
				// CRLFの場合初回読み込みがラインフィールドになるため、ループを続ける
				if (charBuffer[1] == StringUtils.NULL && line == StringUtils.LF) {
					continue;
				}
				// バッファリフレッシュ
				refresh(buffer, recordList);
				// バッファリセット
				buffer.delete(0, buffer.length());
				// 処理が終了するためループを抜ける
				break;
			}

			// 上記以外の場合、通常文字のためバッファに追加
			buffer.append(line);

		}

		// 最終業が改行で区切られていない場合、バッファが残っているためここで消化する
		if (buffer.length() != 0) {
			// バッファリフレッシュ
			refresh(buffer, recordList);
		} else {
			// 1つ前の文字が区切り文字だった場合、最終要素が空文字のため追加
			if (charBuffer[1] == this.delimit) {
				// バッファリフレッシュ
				refresh(buffer, recordList);
			}
		}

		// 結果の返却
		String[] result = recordList.isEmpty() ? null : recordList.stream().toArray(String[]::new);
		return result;

	}

	/**
	 * ファイルから1文字読み取ります
	 * @param charBuffer 読み取り文字格納先
	 * @return 読み取り結果
	 * @throws IOException 読み取り失敗
	 */
	private boolean readChar(char[] charBuffer) throws IOException {
		// 読み取り用配列
		char[] buffer = new char[1];
		// ファイル読み取り
		boolean result = converter.read(buffer) != -1;
		// 読み取りを実行する前に前回読み取り結果を保持する
		charBuffer[1] = charBuffer[0];
		// 今回の読み取り結果を格納
		charBuffer[0] = buffer[0];
		// 結果返却
		return result;
	}

	/**
	 * バッファのリフレッシュ処理を行います
	 * @param buffer バッファ
	 * @param recordList 格納先リスト
	 */
	private void refresh(StringBuffer buffer, List<String> recordList) {
		// 文字列化
		String tmp = buffer.toString();
		// 文字正規化(正規分解と正規合成)
		tmp = Normalizer.normalize(tmp, Form.NFC);
		// リストへ追加
		recordList.add(tmp);
	}

	/**
	 * 進捗更新コンシューマーを設定します
	 * @param observer 進捗更新コンシューマー
	 */
	public final void setObserver(Consumer<Double> observer) {
		this.observer = Objects.requireNonNull(observer);
	}

	/** {@inheritDoc} */
	@Override
	public void close() throws IOException {
		converter.close();
		input.close();
		access.close();
	}

}

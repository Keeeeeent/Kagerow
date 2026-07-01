package com.sakulabo.core.Kagerow.Utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.SoftReference;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.naming.NamingException;

import com.sakulabo.core.Kagerow.Exception.ApplicationError;
import com.sakulabo.core.Processor.archive.BomHandler;
import com.sakulabo.core.Processor.archive.FileReader;

/**
 * ファイルのヘッダー読み取り実装提供クラスです
 * 
 * @author keeeeeent
 */
public abstract class KagerowFileHeaderReader extends FileReader {

	static {
		try {
			// 安定動作のためクラスロードのタイミングで初期値を設定するようにする
			KagerowUtilities.getSettingIfAbsent(
					KagerowFileHeaderReader.class.getName(), "DEFAULT_COLUMN_PREFIX", "K_");
		} catch (NamingException _) {
			/** ignore */
		}
	}

	/** ヘッダー名称プレフィックス（予約語向け） */
	private final String DEFAULT_COLUMN_PREFIX;
	/** 予約語一覧セット */
	private static final Set<String> RESERVED_WORDS;
	/** メッセージファイル */
	private static final String FILE_NAME = "/config/dictionary/SQLReservedWordList.txt";
	static {

		Set<String> tmp = new HashSet<>();

		try (
				InputStream input = KagerowFileHeaderReader.class.getResourceAsStream(FILE_NAME);
				InputStreamReader converter = new InputStreamReader(input, StandardCharsets.UTF_8);
				BufferedReader reader = new BufferedReader(converter)) {

			String line;
			while ((line = reader.readLine()) != null) {
				tmp.add(line.strip().toUpperCase());
			}

		} catch (IOException e) {
			Exception exp = new Exception("not find resource file " + FILE_NAME);
			exp.addSuppressed(exp);
			throw new ApplicationError(exp);
		}

		RESERVED_WORDS = Collections.unmodifiableSet(tmp);

	}

	/**
	 * デフォルトコンストラクタ
	 * @param delimit 区切り文字
	 * @param path 入力元パス
	 * @param charset 読み込み文字コード
	 * @param isHeader ヘッダー有無
	 * @throws IOException 取得ファイル不正、初期化エラー
	 */
	protected KagerowFileHeaderReader(char delimit, Path path, Charset charset, boolean isHeader) throws IOException {
		super(delimit, path, charset, isHeader);
		try {
			DEFAULT_COLUMN_PREFIX = KagerowUtilities.getSettingIfAbsent(
					KagerowFileHeaderReader.class.getName(), "DEFAULT_COLUMN_PREFIX", "K_");
		} catch (NamingException e) {
			throw new IOException(e);
		}
	}

	/** デフォルトヘッダー名称プレフィックス */
	private static final String DEFAULT_COLUMN = "COLUMN_";
	/** ヘッダー情報 */
	private volatile SoftReference<String[]> headerData;

	/**
	 * ヘッダー情報取得
	 * @return ヘッダー情報
	 * @throws IOException データ取得エラー
	 */
	@Override
	public String[] readLine() throws IOException {
		// 生成済みか確認
		if (Objects.nonNull(headerData)) {
			String[] tmp = headerData.get();
			if (Objects.nonNull(tmp)) {
				return tmp;
			} else {
				return readHeader();
			}
		}
		// 未生成の場合、ヘッダー情報生成
		synchronized (this) {
			// モニタロック取得後再度存在確認
			if (Objects.nonNull(headerData)) {
				String[] tmp = headerData.get();
				if (Objects.nonNull(tmp)) {
					return tmp;
				} else {
					return readHeader();
				}
			} else {
				return readHeader();
			}
		}

	}

	/**
	 * ヘッダー情報を取得し、ソフト参照を生成します
	 * @return 読み取りヘッダー
	 * @throws IOException データ取得エラー
	 */
	private String[] readHeader() throws IOException {
		BomHandler bomHandler = new BomHandler(path);
		int skipByte = bomHandler.skipByte();
		this.access.seek(skipByte);
		super.converter = new InputStreamReader(super.input, super.charset);
		String[] line = super.readLine();
		if (isHeader) {
			for (int i = 0; i < line.length; i++) {
				String li = line[i];
				if (RESERVED_WORDS.contains(li.toUpperCase())) {
					// 予約語の場合、プレフィックスを付与
					line[i] = DEFAULT_COLUMN_PREFIX.concat(li);
				}
			}
			headerData = new SoftReference<String[]>(line);
			return line;
		} else {
			int colCount = line.length;
			String[] tmp = new String[colCount];
			while (0 < colCount) {
				String colName = DEFAULT_COLUMN + colCount;
				tmp[--colCount] = colName;
			}
			headerData = new SoftReference<String[]>(tmp);
			return tmp;
		}
	}

}

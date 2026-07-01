package com.sakulabo.library.common;

import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.ResultSetMetaData;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.StringJoiner;

import javax.sql.rowset.CachedRowSet;

import com.sakulabo.core.Common.StringUtils;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;
import com.sakulabo.regulation.spi.PluginAdapter.KagerowRowSet;

/**
 * プラグインデフォルトファイル出力機能提供クラス
 * 
 * @author keeeeeent
 */
public final class FileDefaultOutputer {

	/** 出力先 */
	private final Path outputPath;
	/** エスケープ処理フラグ */
	private final Boolean isEscape;
	/** ヘッダーフラグ */
	private final Boolean isHeader;
	/** 文字コード */
	private final Charset charset;
	/** 出力対象データセット */
	private final KagerowRowSet targetData;
	/** 日付フォーマッター */
	private SimpleDateFormat dateFormatter;

	/**
	 * デフォルトコンストラクタ
	 * @param outputPath 出力先
	 * @param isEscape エスケープ処理フラグ
	 * @param isHeader ヘッダーフラグ
	 * @param charset 文字コード
	 * @param targetData 出力対象データセット
	 * @param format 日付出力形式
	 */
	public FileDefaultOutputer(
			Path outputPath,
			Boolean isEscape,
			Boolean isHeader,
			Charset charset,
			KagerowRowSet targetData,
			String format) {
		this.outputPath = outputPath;
		this.isEscape = isEscape;
		this.isHeader = isHeader;
		this.charset = charset;
		this.targetData = targetData;
		try {
			this.dateFormatter = new SimpleDateFormat(format);
		} catch (IllegalArgumentException e) {
			// 日付フォーマット事態が不正な場合、ログを出力しデフォルト値を設定する
			this.dateFormatter = new SimpleDateFormat("YYYY-MM-dd");
			KagerowLogger.newAppLogger().err(e);
		}

	}

	/**
	 * データ書き出しを行います
	 * @param delimiter 区切り文字
	 */
	public void output(char delimiter) {

		// 文字列連結インスタンス
		StringJoiner stringJoiner = new StringJoiner(new String(new char[] { delimiter }));

		// ファイル出力
		try (BufferedWriter writer = Files.newBufferedWriter(outputPath, charset)) {

			// 結果セット取得
			CachedRowSet cachedRowSet = targetData.data();
			// メタデータ取得
			ResultSetMetaData metaData = cachedRowSet.getMetaData();
			// カラム数取得
			int colNum = metaData.getColumnCount();

			// ヘッダーデータ書き出し
			if (isHeader) {
				for (int i = 1; i <= colNum; i++) {
					String colName = metaData.getColumnLabel(i);
					String result = isEscape ? wrapString(colName) : colName;
					stringJoiner.add(result);
				}
				stringJoiner = reset(stringJoiner, writer, delimiter);
			}

			// ボディーデータ書き出し
			while (cachedRowSet.next()) {
				for (int i = 1; i <= colNum; i++) {
					// 値抽出
					Object object = cachedRowSet.getObject(i);
					// 文字列に変換
					String result = parseRowSet(object, dateFormatter, isEscape);
					// 出力形式に整形
					stringJoiner.add(result);
				}
				stringJoiner = reset(stringJoiner, writer, delimiter);
			}

		} catch (Exception e) {
			KagerowLogger.newAppLogger().err(e);
		}

	}

	/**
	 * データセットから抽出したデータを文字列にフォーマットします
	 * @param target データセットから抽出したデータ
	 * @param dateFormatter 日付フォーマッター
	 * @param isEscape エスケープフラグ
	 * @return 変換済み文字列
	 */
	public static String parseRowSet(Object target, SimpleDateFormat dateFormatter, Boolean isEscape) {
		format: {
			if (Objects.isNull(target)) {
				// 対象がnullの場合、フォーマット処理を中断
				break format;
			}
			// フォーマット処理
			target = switch (target) {
			// 日付の場合フォーマットに沿って出力
			case Date date when Objects.nonNull(dateFormatter) -> dateFormatter.format(date);
			// 数値の場合指数表現なしで出力
			case BigDecimal decimal -> decimal.toPlainString();
			// 上記以外の場合変換なし
			default -> target;
			};
		}
		// エスケープ処理判定&実施
		String result = isEscape
				? wrapString(Objects.toString(target, StringUtils.EMPTY))
				: Objects.toString(target, StringUtils.EMPTY);
		return result;
	}

	/**
	 * 書き込みバッファをリセットします
	 * @param stringJoiner 書き込みレコード
	 * @param writer 書き込み先
	 * @param delimiter 区切り文字
	 * @return 新規バッファ
	 * @throws IOException 書き込み失敗
	 */
	private StringJoiner reset(StringJoiner stringJoiner, BufferedWriter writer, char delimiter) throws IOException {
		writer.append(stringJoiner.toString());
		writer.newLine();
		return new StringJoiner(new String(new char[] { delimiter }));
	}

	/**
	 * 対象文字列をダブルクォーテーションでラップします
	 * @param target ラップ対象文字列
	 * @return ラップ後文字列
	 */
	public static String wrapString(String target) {
		return "\"" + target + "\"";
	}

}

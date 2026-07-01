package com.sakulabo.core.Processor.script;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.NamingException;

import com.sakulabo.core.Common.ErrorMessage;
import com.sakulabo.core.Common.StringUtils;
import com.sakulabo.core.Kagerow.Contents.KagerowVirtualFileContent;
import com.sakulabo.core.Kagerow.Contents.KagerowVirtualFileContent.KagerowVirtualFileObject;
import com.sakulabo.core.Kagerow.Contents.KagerowVirtualFileContent.KagerowVirtualFileObject.BasicFileObject;
import com.sakulabo.core.Kagerow.Contents.KagerowVirtualFileContent.KagerowVirtualFileObject.SecureFileObject;
import com.sakulabo.core.Kagerow.Context.KagerowVirtualDirContext;
import com.sakulabo.core.Kagerow.Context.KagerowVirtualFileContext;
import com.sakulabo.core.Kagerow.Exception.KSQLParseException;

/**
 * KSQLに存在するリンク可能文字列を処理するスクリプトクラスです
 * 
 * @author keeeeeent
 */
public final class KsqlDatabaseLinker extends BaseKsqlTransformer {

	/** 検索対象コンテキスト */
	private final KagerowVirtualFileContext context;
	/** 置換パターン [${....}] */
	private static final Pattern TARGET_PATTERN = Pattern.compile("(?<=\\$\\{)[^}]+(?=\\})");
	/** 履歴インデックスパターン */
	private static final Pattern SUB_TARGET_PATTERN = Pattern.compile("\\[(?<num>.+)\\]");
	/** 複合インデックスパターン */
	private static final Pattern COMPSITE_TARGET_PATTERN = Pattern.compile("((L|l)-)?\\d\\.\\.((L|l)-)?\\d");
	/** 仮想表名称フォーマット（KV_ヘッダー_スタートインデックス_エンドインデックス） */
	private static final String VIEW_NAME_FORMAT = "KV_{0}_{1}_{2}";
	/** 全量仮想表名称フォーマット（KV_ヘッダー_ALL） */
	private static final String VIEW_ALL_NAME_FORMAT = "KV_{0}_ALL";
	/** 複合インデックス正規表現 */
	private static final String COMPSITE_REG = "\\.(?![^\\[]*\\])";
	/** コンテンツセット */
	private final Set<KagerowVirtualFileObject> fileSet = new HashSet<>();
	/** 複合コンテンツセット */
	private final Map<String, Set<KagerowVirtualFileObject>> fileMap = new HashMap<>();
	/** デフォルトスキーマ */
	private final String schema;

	/**
	 * 複合マップ向け内部データ構造
	 * @param startIndex 開始インデックス
	 * @param endIndex 終了インデックス
	 * @param targetWord 置換対象
	 */
	private static record CompsiteHistory(
			int startIndex,
			int endIndex,
			String targetWord) {

		/**
		 * 開始インデックスが終了インデックスより後か判定します
		 * @return true -> 終了が先（不正） false -> 終了が後（正常）
		 */
		boolean valid() {
			return endIndex < startIndex;
		}
	}

	/**
	 * デフォルトコンストラクタ
	 * @param schema デフォルトスキーマ
	 * @param context コンテキスト
	 */
	public KsqlDatabaseLinker(String schema, KagerowVirtualFileContext context) {
		this.context = context;
		this.schema = schema;
	}

	/** {@inheritDoc} */
	@Override
	public String transform(String target) throws KSQLParseException {

		// 文字列蓄積メモリ
		StringBuilder buffer = new StringBuilder();
		// 返却文字列メモリ
		StringBuilder result = new StringBuilder();
		// エスケープフラグ
		boolean flug = false;

		// 文字列の解析実施
		for (int i = 0; i < target.length(); i++) {
			// 1文字取得
			char c = target.charAt(i);
			if (c == StringUtils.SINGLE_QUOTATION) {
				// バッファに追加
				buffer.append(c);
				// シングルクウォートの場合、エスケープフラグを確認
				if (flug) {
					// エスケープ中の場合、次の文字がシングルクオート確認
					if (i + 1 < target.length()) {
						char sc = target.charAt(++i);
						// 順番にバッファに追加
						buffer.append(sc);
						if (sc == StringUtils.SINGLE_QUOTATION) {
							// 次の文字もシングルクオートの場合、エスケープ対象のためそのまま処理を続行
							continue;
						} else {
							// 次の文字がシングルクオートではない場合エスケープ状態を解除
							flug = false;
							// エスケープ解除のため無変換
							result.append(buffer);
							// バッファクリア
							buffer.delete(0, buffer.length());
						}
					} else {
						// インデックスの範囲を超えている場合、エスケープ終端のため状態解除
						flug = false;
						// エスケープ解除のため無変換
						result.append(buffer);
						// バッファクリア
						buffer.delete(0, buffer.length());
					}
				} else {
					// フラグがオンではない場合、エスケープ処理に移行
					flug = true;
					// エスケープ解除まで置換対象ではないため、ここで一度パース
					result.append(parse(buffer, i));
					// バッファクリア
					buffer.delete(0, buffer.length());
				}
			} else {
				// バッファに追加
				buffer.append(c);
			}

		}

		// 処理が終了した場合、残りのバッファをコピー
		result.append(parse(buffer, target.length() - buffer.length()));

		return result.toString();
	}

	/**
	 * 文字列に対して置換処理を実行します
	 * @param buffer 対象文字列
	 * @param offset 検索位置オフセット   
	 * @return 置換後文字列
	 * @throws KSQLParseException 解析失敗
	 */
	private String parse(CharSequence buffer, int offset) throws KSQLParseException {

		// 文字列へ変換
		String target = buffer.toString();
		// 正規表現コンパイル
		Matcher matcher = TARGET_PATTERN.matcher(target);

		// 対象が存在する場合置換処理対象か確認
		while (matcher.find()) {

			// ヒットワードを抽出
			String targetWord = matcher.group();

			// スキーマ抽出
			String schema, colName;
			String[] split = targetWord.split(COMPSITE_REG, 2);
			if (split.length == 1) {
				schema = this.schema;
				colName = split[0];
			} else {
				schema = split[0];
				colName = split[1];
			}

			// スキーマディレクトリ取得
			KagerowVirtualDirContext dirContext;
			try {
				dirContext = context.lookup(schema);
			} catch (NamingException e) {
				// エラー詳細構築
				errorMsg = ErrorMessage.CODE_902.getMessage(schema);
				startIndex = matcher.start() + offset;
				endIndex = matcher.end() + offset;
				// スキーマが見つからない場合パースエラー
				throw new KSQLParseException(this, e);
			}
			// ディレクトリコンテンツ習得
			Map<String, String> replaceWordDictionary = dirContext.getSynonymMapList();
			// 相対検索インデックス初期化
			int history = 0;

			// テーブル物理名称
			String realName;
			// コンテンツ
			KagerowVirtualFileContent content;
			// ワードディクショナリーに含まれているか確認
			String rowTargetWord = colName.substring(0, colName.indexOf("["));
			if (replaceWordDictionary.containsKey(rowTargetWord)) {
				try {
					realName = replaceWordDictionary.get(rowTargetWord);
					content = dirContext.lookup(realName);
					history = content.contentSize();
				} catch (NamingException e) {
					// エラー詳細構築
					errorMsg = ErrorMessage.CODE_903.getMessage(replaceWordDictionary.get(rowTargetWord));
					startIndex = matcher.start() + offset;
					endIndex = matcher.end() + offset;
					// テーブルが見つからない場合パースエラー
					throw new KSQLParseException(this, e);
				}
			} else {
				// エラー詳細構築
				errorMsg = ErrorMessage.CODE_903.getMessage(rowTargetWord);
				startIndex = matcher.start() + offset;
				endIndex = matcher.end() + offset;
				// 含まれていない場合、未定義置換文字列としてパースエラー
				throw new KSQLParseException(this);
			}
			// 履歴取得
			Matcher subMathcher = SUB_TARGET_PATTERN.matcher(colName); // 複合マップ生成フラグ
			boolean isCompsite = false;
			/*
			 * 複合マップデータ構造保持ローカル変数定義
			 * この変数は複合マップ処理の場合にのみ初期化されます
			 * 初期化はロジックによって補償する必要あり
			 */
			CompsiteHistory compsiteHistory = null;

			// 構文解析
			if (subMathcher.find()) {
				// プレフックス取得
				String tmpHistory = subMathcher.group("num");
				// 複合プレフックス判定正規表現コンパイル
				Matcher compsiteMathcher = COMPSITE_TARGET_PATTERN.matcher(tmpHistory);
				if (compsiteMathcher.find()) {
					// 複合プレフィックスの場合
					String[] prefixList = tmpHistory.split("\\.\\.");
					// インデックス初期化
					int startIndex = editHistory(prefixList[0], history);
					int endIndex = editHistory(prefixList[1], history);
					// 複合データマップ初期化
					compsiteHistory = new CompsiteHistory(startIndex, endIndex, tmpHistory);
					// フラグを立てる
					isCompsite = true;
				} else {
					// 単一セットの場合、相対インデックスの調整のみを行う
					history = editHistory(tmpHistory, history);
				}
			} else {
				// 指定がない場合は最後尾を取得
				if (0 < history)
					history -= 1;
			}

			// 例外が発生した場合の開始位置、終了位置を保存しておく
			startIndex = matcher.start() + offset;
			endIndex = matcher.end() + offset;

			// パース処理実行
			if (isCompsite) {
				// 複合マップ生成フラグオンの場合
				target = compsiteParse(target, replaceWordDictionary, content, compsiteHistory, targetWord);
			} else {
				// パース処理実行
				target = shingleParse(target, replaceWordDictionary, content, history, targetWord);
			}

		}

		return target;
	}

	/**
	 * 相対インデックスの調整を行います
	 * @param tmpHistory 相対インデックス文字列表現
	 * @param history 調整前相対インデックス
	 * @return 調整済み相対インデックス
	 */
	private int editHistory(String tmpHistory, int history) {
		if (tmpHistory.startsWith("L") || tmpHistory.startsWith("l")) {
			// Lastプレフィックスの場合
			tmpHistory = tmpHistory.substring(1, tmpHistory.length());
			// プレフィックス分引く
			history -= Integer.parseInt(tmpHistory);
		} else {
			// プレフィックスがない場合、そのままの値を使用
			history = Integer.parseInt(tmpHistory);
		}
		return history;
	}

	/**
	 * 単一ファイルセットのパース処理を実行します
	 * @param target パース対象
	 * @param replaceWordDictionary ワードディクショナリー
	 * @param content 仮想FSコンテンツ
	 * @param history 相対インデックス
	 * @param targetWord パースワード
	 * @return 置換済みSQL
	 * @throws KSQLParseException パース失敗
	 */
	private String shingleParse(
			String target,
			Map<String, String> replaceWordDictionary,
			KagerowVirtualFileContent content,
			int history,
			String targetWord)
			throws KSQLParseException {

		// 相対位置からコンテンツを取得
		KagerowVirtualFileObject file;
		try {
			file = content.get(history);
		} catch (NamingException e) {
			// エラー詳細構築
			errorMsg = ErrorMessage.CODE_904.getMessage(targetWord, history);
			// 世代が見つからない場合パースエラー
			throw new KSQLParseException(this, e);
		}
		// 取得したコンテンツからスキーマを取得
		String schema = file.schema();

		// 対象をリプレイス
		target = switch (file) {
		case BasicFileObject f -> {
			String tableName = BaseKsqlTransformer.toTabelName(f.binaryName());
			yield target.replace(formatedStr(targetWord), String.join(StringUtils.DOT_STR, schema, tableName));
		}
		case SecureFileObject f -> {
			String tableName = BaseKsqlTransformer.toTabelName(f.binaryName());
			yield target.replace(formatedStr(targetWord), String.join(StringUtils.DOT_STR, schema, tableName));
		}
		};

		// コンテンツセット追加
		fileSet.add(file);

		// 置換済みSQLをリターン
		return target;

	}

	/**
	 * 複合ファイルセットのパース処理を実行します
	 * @param target パース対象
	 * @param replaceWordDictionary ワードディクショナリー
	 * @param content 仮想FSコンテンツ
	 * @param compsiteHistory 複合プレフィックスデータ構造
	 * @param targetWord パースワード
	 * @return 置換済みSQL
	 * @throws KSQLParseException パース失敗
	 */
	private String compsiteParse(
			String target,
			Map<String, String> replaceWordDictionary,
			KagerowVirtualFileContent content,
			CompsiteHistory compsiteHistory,
			String targetWord)
			throws KSQLParseException {

		// ローカルファイルセット生成
		Set<KagerowVirtualFileObject> fileSet = new HashSet<>();

		if (compsiteHistory.valid()) {
			// エラー詳細構築
			errorMsg = ErrorMessage.CODE_905.getMessage(compsiteHistory.startIndex, compsiteHistory.endIndex);
			// 開始インデックスと終了インデックスが有効範囲内かチェック
			throw new KSQLParseException(this);
		}

		for (int i = compsiteHistory.startIndex(); i <= compsiteHistory.endIndex(); i++) {
			// ファイルインスタンス取得
			KagerowVirtualFileObject file;
			try {
				file = content.get(i);
			} catch (NamingException e) {
				// エラー詳細構築
				errorMsg = ErrorMessage.CODE_904.getMessage(targetWord.substring(targetWord.indexOf("[")), i);
				// 世代が見つからない場合パースエラー
				throw new KSQLParseException(this, e);
			}
			// ローカルコンテンツセット追加
			fileSet.add(file);
			// コンテンツセット追加
			this.fileSet.add(file);
		}

		// 単独シノニム取得
		String header = targetWord.substring(0, targetWord.indexOf("["));
		// KVIEW向け仮想表名称生成（個別）
		String viewName = MessageFormat.format(VIEW_NAME_FORMAT,
				new Object[] { header, compsiteHistory.startIndex, compsiteHistory.endIndex });
		// KVIEW向け仮想表名称生成（全体）
		String viewAllName = MessageFormat.format(VIEW_ALL_NAME_FORMAT, new Object[] { header });

		// 対象をリプレイス
		target = target.replace(formatedStr(targetWord), String.join(StringUtils.DOT_STR, schema, viewName));

		// ファイルマップ追加（個別）
		fileMap.put(viewName, fileSet);
		// ファイルマップ追加（全体）
		fileMap.compute(viewAllName, (k, v) -> addFileSet(k, v, fileSet));

		// 置換済みSQLをリターン
		return target;

	}

	/**
	 * ファイルセットをファイルマップに合成します
	 * @param key キー
	 * @param value ファイルマップに格納済みのファイルセット
	 * @param fileSet 合成するフィあるマップ
	 * @return 合成済みファイルマップ
	 */
	private Set<KagerowVirtualFileObject> addFileSet(
			String key,
			Set<KagerowVirtualFileObject> value,
			Set<KagerowVirtualFileObject> fileSet) {
		if (Objects.nonNull(value)) {
			value.addAll(fileSet);
			return value;
		} else {
			return fileSet;
		}
	}

	/**
	 * 置換文字列を生成します
	 * @param reword 置換ワード
	 * @return 置換文字列
	 */
	private String formatedStr(String reword) {
		return "${%s}".formatted(reword);
	}

	/**
	 * ファイルセットを返却します
	 * @return ファイルセット
	 */
	public Set<KagerowVirtualFileObject> getFileSet() {
		return fileSet;
	}

	/**
	 * 複合ファイルセットを返却します
	 * @return ファイルセット
	 */
	public Map<String, Set<KagerowVirtualFileObject>> getFileMap() {
		return fileMap;
	}

	/**
	 * 複合ファイルセット生成済みか判定します
	 * @return 判定結果
	 */
	public boolean isFileMap() {
		return !fileMap.isEmpty();
	}

}

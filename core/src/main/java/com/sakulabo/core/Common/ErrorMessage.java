package com.sakulabo.core.Common;

import java.text.MessageFormat;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

import com.sakulabo.core.Kagerow.Exception.ApplicationError;

/**
 * Kagerowアプリケーション専用エラーメッセージ生成クラスです
 *
 * @author keeeeeent
 */
public enum ErrorMessage {

	/** ヘッダーの型情報が不正です */
	CODE_001("DONT_HEADER_NAME"),
	/** ヘッダーの名称情報が不正です */
	CODE_002("DONT_TYPE_NAME"),
	/** アルゴリズムの指定は必須です */
	CODE_003("DONT_SET_ALGORITHM"),
	/** {0}に失敗しました */
	CODE_004("FAIL_LOGIC"),
	/** ファイルが見つかりません【パス:{0}】 */
	CODE_005("NOT_FOUND_FILE"),
	/** {0}は不正な名称です */
	CODE_006("INVALID_NAME"),
	/** 要素数が不正です */
	CODE_007("ILLEGAL_ELEMENT_COUNT"),
	/** 引数が指定されていますが使用されません【対象】: {0} */
	CODE_008("DONT_USING_PARAMS"),
	/** コネクションが見つかりません */
	CODE_009("NOT_FOUND_CONNECTION"),
	/** パラメータ [{0}] は指定必須です */
	CODE_010(StringUtils.EMPTY),
	/** エグゼキューターの取得に失敗しました[必要数] : {0} */
	CODE_011(StringUtils.EMPTY),
	/** キャッシュの取込に失敗しました [原因] : {0} */
	CODE_012(StringUtils.EMPTY),
	/** 終端{0}の指定は必須です */
	CODE_013(StringUtils.EMPTY),
	/** セキュアブートが行われていません */
	CODE_014(StringUtils.EMPTY),
	/** キャッシュ非対応インスタンスです */
	CODE_015(StringUtils.EMPTY),
	/** キャッシュIDの生成に失敗しました */
	CODE_016(StringUtils.EMPTY),
	/** 既に存在するシノニムです【対象】: {0} */
	CODE_017(StringUtils.EMPTY),
	/** アプリケーション専用のObjectInputFilter登録に失敗しました */
	CODE_018(StringUtils.EMPTY),
	/** アプリケーションでデシリアライズが許可されてないクラスです【対象】: {0} */
	CODE_019(StringUtils.EMPTY),
	/** アプリケーションでデシリアライズが許可されてないネスト回数です【回数】: {0} */
	CODE_020(StringUtils.EMPTY),
	/** アプリケーションでデシリアライズが許可されてない配列要素数です【回数】: {0} */
	CODE_021(StringUtils.EMPTY),
	/** Kagerowライブラリが未初期化です */
	CODE_022(StringUtils.EMPTY),
	/** KFile向けXSDファイルの読み込みに失敗しました */
	CODE_023(StringUtils.EMPTY),
	/** KSQLファイルの読み込みに失敗しました */
	CODE_024(StringUtils.EMPTY),
	/** XMLエラー（{0}行目, {1}列目）:<{2}> はこの位置では使用できません。<{3}> が必要です */
	CODE_025(StringUtils.EMPTY),
	/** XMLエラー（{0}行目, {1}列目）: {2} */
	CODE_026(StringUtils.EMPTY),
	/** KSQLファイルの書き出しに失敗しました */
	CODE_027(StringUtils.EMPTY),
	/** KSQLファイルへの要素追加に失敗しました */
	CODE_028(StringUtils.EMPTY),
	/** 予期せぬエラーによりKSQL実行に失敗しました */
	CODE_029(StringUtils.EMPTY),
	/** 文字数が長すぎます 【詳細】: {0} */
	CODE_030(StringUtils.EMPTY),
	/** 数値が大きすぎます 【詳細】: {0} */
	CODE_031(StringUtils.EMPTY),
	/** 日付の形式が不正です 【詳細】: {0} */
	CODE_032(StringUtils.EMPTY),
	/** 一意制約違反 【詳細】: {0} */
	CODE_033(StringUtils.EMPTY),
	/** 外部キー制約違反 【詳細】: {0} */
	CODE_034(StringUtils.EMPTY),
	/** NULL制約違反 【詳細】: {0} */
	CODE_035(StringUtils.EMPTY),
	/** プラグインファイルを指定してください 【対象】: {0} */
	CODE_036(StringUtils.EMPTY),
	/** 既にセッションは終了しています 【対象】: {0} */
	CODE_037(StringUtils.EMPTY),

	// スクリプトコンパイル時のエラーメッセージ群

	/** スクリプト環境変数<{0}>が見つかりませんでした */
	CODE_900(StringUtils.EMPTY),
	/** 置換変数<{0}>が見つかりませんでした */
	CODE_901(StringUtils.EMPTY),
	/** スキーマ<{0}>が見つかりませんでした */
	CODE_902(StringUtils.EMPTY),
	/** テーブル<{0}>が見つかりませんでした */
	CODE_903(StringUtils.EMPTY),
	/** テーブル<{0}>の世代[{1}]が見つかりませんでした */
	CODE_904(StringUtils.EMPTY),
	/** 合成テーブルビューの指定された範囲が不正です。【開始】: {0}【終了】: {1} */
	CODE_905(StringUtils.EMPTY);

	/** メッセージファイル */
	private static final String FILE_NAME = "config.message.error-message";
	/** リソースバンドル */
	private static final ResourceBundle message;
	static {

		Optional<Module> module = ErrorMessage.class
				.getModule()
				.getLayer()
				.findModule(StringUtils.MODULE_NAME);

		if (module.isPresent()) {
			message = ResourceBundle.getBundle(FILE_NAME, module.get());
		} else {
			Exception e = new Exception("not find resource file " + FILE_NAME);
			throw new ApplicationError(e);
		}
	}

	/** エラーメッセージシノニム */
	private String synonym;

	/**
	 * デフォルトコンストラクタ
	 * @param synonym シノニム
	 */
	private ErrorMessage(String synonym) {
		this.synonym = synonym;
	}

	/**
	 * メッセージを生成します
	 * @param param パラメータ
	 * @return メッセージ
	 */
	@SafeVarargs
	public final String getMessage(Object... param) {
		String result = message.getString(name());
		return MessageFormat.format(result, param);
	}

	/**
	 * メッセージを生成します
	 * @param synonym シノニム
	 * @param param パラメータ
	 * @return メッセージ
	 */
	@SafeVarargs
	public static final String getMessage(ErrorMessageSynonym synonym, Object... param) {
		ErrorMessage target = null;
		for (ErrorMessage msg : values()) {
			if (msg.synonym.equals(synonym.name())) {
				target = msg;
			}
		}
		if (Objects.isNull(target)) {
			Exception e = new Exception("not find synonym in ErrorMessage -> " + synonym);
			throw new ApplicationError(e);
		}
		String result = message.getString(target.name());
		return MessageFormat.format(result, param);
	}

	/**
	 * シノニムを返却します
	 * @return シノニム文字列
	 */
	public final String tosynonym() {
		return synonym;
	}

}

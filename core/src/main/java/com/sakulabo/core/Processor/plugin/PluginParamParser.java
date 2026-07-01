package com.sakulabo.core.Processor.plugin;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sakulabo.core.Common.ApplicationWordDictionary;
import com.sakulabo.core.Common.ErrorMessage;
import com.sakulabo.core.Kagerow.Exception.AppLogicException;
import com.sakulabo.core.Kagerow.Utilities.KagerowScriptAccessor;
import com.sakulabo.core.Kagerow.Utilities.KagerowScriptAccessor.KagerowPluginAccessor;
import com.sakulabo.regulation.spi.PluginAdapter;

/**
 * プラグインのパラメータ解析を行うパーサクラスです
 * 
 * @author keeeeeent 
 */
public final class PluginParamParser {

	/** 解析モード（入力） */
	public static final String INPUT = "input";
	/** 解析モード（出力） */
	public static final String OUTPUT = "output";
	/** 対象プラグイン */
	private final PluginAdapter plugin;
	/** プラグインパラメータキャッシュ */
	private static final PluginParamCache CACHE = new PluginParamCache();

	/**
	 * デフォルトコンストラクタ
	 * @param plugin 対象プラグイン
	 */
	public PluginParamParser(PluginAdapter plugin) {
		this.plugin = plugin;
	}

	/**
	 * プラグインパラメータのバインド処理を行います
	 * @param bindMap バインド元マップ
	 * @param method メソッド名称
	 * @return バインド済みマップ
	 * @throws PluginAdapter.PluginValidationException 必須チェックエラー
	 */
	public Map<String, String> getPluginParam(Map<String, String> bindMap, String method)
			throws PluginAdapter.PluginValidationException {

		// 返却用マップ初期化
		Map<String, String> result = new HashMap<>();
		// クラス情報取得
		Class<?> clazz = plugin.getClass();
		// アノテーション解析&取得
		PluginAdapter.Param[] paramList = CACHE.get(clazz).get(method);

		// マップへパラメータ適用
		for (PluginAdapter.Param param : paramList) {
			// パラメータ変数初期化
			String key = param.value();
			// マップバインド
			String bindValue = bindMap.get(key);
			if (param.required()) {
				if (Objects.isNull(bindValue) || bindValue.isEmpty()) {
					throw new PluginAdapter.PluginValidationException(
							ErrorMessage.CODE_010.getMessage(key), key);
				}
			}
			// 値のマッピング
			String value;
			if (Objects.isNull(bindValue) || bindValue.isEmpty()) {
				// 値が見つからない場合、デフォルト値を設定
				value = param.defaultValue().isEmpty() ? null : param.defaultValue();
			} else {
				value = bindValue;
			}
			// バインド
			result.put(key, value);
		}

		return result;
	}

	/**
	 * プラグインをチェーンリンク状にソートします
	 * @param selectLogic 対象リスト選択関数
	 * @param script スクリプトアクセッサー
	 * @return ソート後リスト
	 * @throws AppLogicException 終端プラグインが存在しない場合
	 */
	public static Map<Integer, List<KagerowPluginAccessor>> getPlugin(
			Function<KagerowScriptAccessor, List<KagerowPluginAccessor>> selectLogic,
			KagerowScriptAccessor script) throws AppLogicException {

		// 結果格納リスト
		Map<Integer, List<KagerowPluginAccessor>> result = new HashMap<>();

		List<KagerowPluginAccessor> selectedList = selectLogic.apply(script);

		if (selectedList.size() == 0) {
			// プラグインの指定がない場合、そのままリターン
			return result;
		} else if (selectedList.size() == 1) {
			result.put(Integer.valueOf(0), selectedList);
			return result;
		} else {

			// 一時格納先
			Deque<KagerowPluginAccessor> tmp = new ArrayDeque<>();

			// 開始位置となるPLUGINインスタンスを取得
			KagerowPluginAccessor lastPlugin = selectedList.stream()
					.filter(Predicate.not(KagerowPluginAccessor::hasNext))
					.findFirst()
					.orElseThrow(PluginParamParser::createAppLogicException);

			// 終端要素の格納
			tmp.push(lastPlugin);

			// チェーンリンクにするため、再帰処理実行
			searchNextPlugin(lastPlugin, tmp, selectLogic, script);
			// 純粋なIDに変換
			List<String> idList = tmp.stream().map(KagerowPluginAccessor::getId).toList();

			// マッピング
			Map<String, List<KagerowPluginAccessor>> mappingList = selectedList
					.stream()
					.collect(Collectors.groupingBy(KagerowPluginAccessor::getId));

			// ソート
			for (int i = 0; i < idList.size(); i++) {
				String id = idList.get(i);
				List<KagerowPluginAccessor> plugins = mappingList.get(id);
				result.put(Integer.valueOf(i), plugins);
			}

			return result;
		}

	}

	/**
	 * プラグインリストからnextによって順序づけを行ったリストを構築します
	 * @param lastPlugin 現在のインスタンス
	 * @param result 格納先リスト
	 * @param selectLogic 対象リスト選択関数
	 * @param script スクリプトアクセッサー
	 */
	private static void searchNextPlugin(
			KagerowPluginAccessor lastPlugin,
			Deque<KagerowPluginAccessor> result,
			Function<KagerowScriptAccessor, List<KagerowPluginAccessor>> selectLogic,
			KagerowScriptAccessor script) {

		// 次の要素を取得
		String targetId = lastPlugin.getId();
		List<KagerowPluginAccessor> selectedList = selectLogic.apply(script);
		Optional<KagerowPluginAccessor> nextPlugin = selectedList.stream()
				.filter(f -> f.next().equals(targetId))
				.findFirst();

		if (nextPlugin.isPresent()) {
			// 結果格納
			result.push(nextPlugin.get());
			// 再帰呼び出し
			searchNextPlugin(nextPlugin.get(), result, selectLogic, script);
		}

	}

	/**
	 * プラグイン向けの初期化パラメータを生成します
	 * @param method 生成対象メソッド
	 * @return 初期化パラメータ
	 */
	public Map<String, String> createParam(String method) {

		// クラス情報取得
		Class<?> clazz = plugin.getClass();
		// アノテーション解析&取得
		PluginAdapter.Param[] paramList = CACHE.get(clazz).get(method);

		if (Objects.isNull(paramList)) {
			return Collections.emptyMap();
		} else {
			return Stream.of(paramList)
					.collect(Collectors.toMap(
							PluginAdapter.Param::value,
							PluginAdapter.Param::defaultValue));
		}

	}

	/**
	 * 終端プラグインが存在しない旨を伝播する例外を生成します
	 * @return 生成された例外
	 */
	private static AppLogicException createAppLogicException() {
		String msg = ErrorMessage.CODE_013.getMessage(
				ApplicationWordDictionary.WCD_0005.toString());
		return new AppLogicException(msg);
	}

}

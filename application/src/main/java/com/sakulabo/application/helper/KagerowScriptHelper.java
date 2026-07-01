package com.sakulabo.application.helper;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Vector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.naming.NamingException;

import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;
import com.sakulabo.core.Kagerow.Utilities.KagerowScriptAccessor;
import com.sakulabo.core.Kagerow.Utilities.KagerowScriptAccessor.KagerowCmdAccessor;
import com.sakulabo.core.Kagerow.Utilities.KagerowScriptAccessor.KagerowPluginAccessor;
import com.sakulabo.core.Kagerow.Utilities.KagerowScriptAccessor.KagerowSqlAccessor;
import com.sakulabo.core.Kagerow.Utilities.KagerowUtilities;
import com.sakulabo.regulation.annotation.KagerowComponent;
import com.sakulabo.regulation.annotation.KagerowPlugin.PluginType;

/**
 * KagerowScriptを扱うためのヘルパーです
 * 
 * @author keeeeeent
 */
@KagerowComponent
public final class KagerowScriptHelper {

	/** 内部向けワードディクショナリー */
	private static final char[] WORD_DICTIONARY;
	static {
		WORD_DICTIONARY = Stream.concat(
				IntStream.rangeClosed('A', 'Z').mapToObj(Character::toString),
				IntStream.rangeClosed('a', 'z').mapToObj(Character::toString))
				.collect(Collectors.joining())
				.toCharArray();
	}
	/** 乱数発生最大桁数 */
	private static final int MAX_LEN = WORD_DICTIONARY.length;
	/** 内部向け乱数発生装置 */
	private static final Random RUND = new Random();
	/** 再実行カウンター */
	private static final int MAX_COUNT;
	static {
		String count = KagerowUtilities.getSetting(KagerowScriptHelper.class.getName(), "MAX_COUNT");
		if (Objects.isNull(count)) {
			try {
				KagerowUtilities.setSetting(KagerowScriptHelper.class.getName(), "MAX_COUNT", "100");
			} catch (NamingException e) {
				KagerowLogger.newAppLogger().err(e);
			}
		}
		MAX_COUNT = Integer.valueOf(Objects.toString(count, "100")).intValue();
	}
	/** 生成ID長 */
	private static final int ID_LEN;
	static {
		String idLen = KagerowUtilities.getSetting(KagerowScriptHelper.class.getName(), "ID_LEN");
		if (Objects.isNull(idLen)) {
			try {
				KagerowUtilities.setSetting(KagerowScriptHelper.class.getName(), "ID_LEN", "5");
			} catch (NamingException e) {
				KagerowLogger.newAppLogger().err(e);
			}
		}
		ID_LEN = Integer.valueOf(Objects.toString(idLen, "5")).intValue();
	}
	/** デフォルトSQL */
	private static final String DEFAULT_SQL;
	static {
		String sql = """
				SELECT * FROM VALUES(('KAGEROW')) AS K_HLPER(DEF)""";
		String defSql = KagerowUtilities.getSetting(KagerowScriptHelper.class.getName(), "DEFAULT_SQL");
		if (Objects.isNull(defSql)) {
			try {
				KagerowUtilities.setSetting(KagerowScriptHelper.class.getName(), "DEFAULT_SQL", sql);
			} catch (NamingException e) {
				KagerowLogger.newAppLogger().err(e);
			}
		}
		DEFAULT_SQL = Objects.toString(defSql, sql);
	}

	/**
	 * 指定されたプラグインリストから一致するプラグインのインデックス番号を取得します
	 * @param list プラグインリスト
	 * @param target 検索対象
	 * @return 検索結果インデックス
	 */
	private int getPluginIndex(List<KagerowPluginAccessor> list, String target) {
		for (int i = 0; i < list.size(); i++) {
			String id = list.get(i).getId();
			if (id.equals(target)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * 指定されたKSQLリストから一致するKSQLのインデックス番号を取得します
	 * @param list KSQLリスト
	 * @param target 検索対象
	 * @return 検索結果インデックス
	 */
	private int getKsqlIndex(List<KagerowSqlAccessor> list, String target) {
		for (int i = 0; i < list.size(); i++) {
			String id = list.get(i).getId();
			if (id.equals(target)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * 指定した長さのランダム文字列を取得します
	 * @param len 生成文字列長
	 * @return 生成された文字列
	 */
	private String getRandomStr(int len) {
		// 文字列生成用配列初期化
		char[] charArray = new char[len];
		for (int i = 0; i < charArray.length; i++) {
			// 元になる文字を取得
			charArray[i] = WORD_DICTIONARY[RUND.nextInt(MAX_LEN)];
		}
		return new String(charArray);
	}

	/**
	 * 指定されたIDリストに含まれないIDを生成します
	 * @param idList IDリスト
	 * @param len 生成文字列長
	 * @return 生成された文字列
	 */
	private String createID(List<String> idList, int len) {
		// IDを生成
		String id;
		// 再実行カウンター
		int count = 0;
		do {
			// 新規作成IDが生成されるまで継続
			id = getRandomStr(len);
			// カウンターカウンタアップ&	再実行回数オーバーか確認
			if (MAX_COUNT < count++) {
				throw new IllegalCallerException("Retry attempts exceeded");
			}
		} while (idList.contains(id));
		// 結果返却
		return id;
	}

	/**
	 * KsqlIDを変換済みのインスタンスに対して生成を試みます
	 * Rawインスタンスに対する生成は行われません
	 * @param script KSQLアクセッサー
	 * @param len 生成ID長
	 * @return 生成されたID文字列
	 */
	public String getKsqlId(KagerowScriptAccessor script, int len) {
		// Ksql一覧IDリスト取得
		List<String> idList = script.getKsqls().stream()
				.map(KagerowSqlAccessor::getId)
				.toList();
		// 結果返却
		return createID(idList, len);
	}

	/**
	 * InputPluginIDを変換済みのインスタンスに対して生成を試みます
	 * Rawインスタンスに対する生成は行われません
	 * @param script KSQLアクセッサー
	 * @param len 生成ID長
	 * @return 生成されたID文字列
	 */
	public String getInputPluginId(KagerowScriptAccessor script, int len) {
		// Ksql一覧IDリスト取得
		List<String> idList = script.getInputPlugins().stream()
				.map(KagerowPluginAccessor::getId)
				.toList();
		// 結果返却
		return createID(idList, len);
	}

	/**
	 * OutputPluginIDを変換済みのインスタンスに対して生成を試みます
	 * Rawインスタンスに対する生成は行われません
	 * @param script KSQLアクセッサー
	 * @param len 生成ID長
	 * @return 生成されたID文字列
	 */
	public String getOutputPluginId(KagerowScriptAccessor script, int len) {
		// Ksql一覧IDリスト取得
		List<String> idList = script.getOutputPlugins().stream()
				.map(KagerowPluginAccessor::getId)
				.toList();
		// 結果返却
		return createID(idList, len);
	}

	/**
	 * 指定したIDの後ろに指定したプラグインを追加します
	 * @param script スクリプトインスタンス
	 * @param targetId 指定プラグインID
	 * @param pkgName 追加するプラグインパッケージ名称
	 * @param name 追加するプラグイン名称
	 * @return クローンされたアクセッサインスタンス
	 */
	public KagerowScriptAccessor addInputPlugin(
			KagerowScriptAccessor script,
			String targetId,
			String pkgName,
			String name) {

		// 追加先リスト取得
		List<KagerowPluginAccessor> list = script.getRawInputPlugins();
		// 追加先インデックス番号
		int index = getPluginIndex(list, targetId);
		// 追加する際のIDを生成
		String id = getInputPluginId(script, ID_LEN);
		// 追加処理
		KagerowPluginAccessor basePlugin = null;
		String nextId = null;
		if (index != -1) {
			basePlugin = list.get(index);
			nextId = basePlugin.next();
		}

		// プラグインインスタンス生成
		KagerowPluginAccessor pluginInstance = script.newKagerowPluginAccessor(pkgName, name, id, nextId);
		// パラメータの設定
		try {
			// パラメータ取得
			Map<String, String> param = KagerowUtilities.createPluginParam(pkgName, name, PluginType.INPUT);
			// パラメータバインド
			pluginInstance = pluginInstance.setParam(param);
		} catch (Exception e) {
			// ここでの例外はプラグインが見つからない場合に発生するため
			// ロジック的に発生しない想定だが念の為エラーとしては検知できるようにする
			KagerowLogger.newAppLogger().err(e);
		}

		// 生成プラグインしたプラグインをインスタンス管理下に追加
		// この処理を行ってから出ないとsetNextで新たなインスタンスに生成したインスタンスが含まれない
		list.add(pluginInstance);

		// クローンされたインスタンス
		if (Objects.nonNull(basePlugin)) {
			// IDのリンク付け替え
			basePlugin = basePlugin.setNext(id);
			list = setPluginList(list, basePlugin);
			// 連結追加の場合、クローンされたインスタンスを返却
			return script.setRawInputPlugins(list);
		} else {
			// 単純追加の場合、元のインスタンスを返却
			return script.setRawInputPlugins(list);
		}

	}

	/**
	 * 指定したIDの後ろに指定したプラグインを追加します
	 * @param script スクリプトインスタンス
	 * @param targetId 指定プラグインID
	 * @param pkgName 追加するプラグインパッケージ名称
	 * @param name 追加するプラグイン名称
	 * @return クローンされたアクセッサインスタンス
	 */
	public KagerowScriptAccessor addOutputPlugin(
			KagerowScriptAccessor script,
			String targetId,
			String pkgName,
			String name) {

		// 追加先リスト取得
		List<KagerowPluginAccessor> list = script.getRawOutputPlugins();
		// 追加先インデックス番号
		int index = getPluginIndex(list, targetId);
		// 追加する際のIDを生成
		String id = getOutputPluginId(script, ID_LEN);
		// 追加処理
		KagerowPluginAccessor basePlugin = null;
		String nextId = null;
		if (index != -1) {
			basePlugin = list.get(index);
			nextId = basePlugin.next();
		}

		// プラグインインスタンス生成
		KagerowPluginAccessor pluginInstance = script.newKagerowPluginAccessor(pkgName, name, id, nextId);
		// パラメータの設定
		try {
			// パラメータ取得
			Map<String, String> param = KagerowUtilities.createPluginParam(pkgName, name, PluginType.OUTPUT);
			// パラメータバインド
			pluginInstance = pluginInstance.setParam(param);
		} catch (Exception e) {
			// ここでの例外はプラグインが見つからない場合に発生するため
			// ロジック的に発生しない想定だが念の為エラーとしては検知できるようにする
			KagerowLogger.newAppLogger().err(e);
		}

		// 生成プラグインしたプラグインをインスタンス管理下に追加
		// この処理を行ってから出ないとsetNextで新たなインスタンスに生成したインスタンスが含まれない
		list.add(pluginInstance);

		// クローンされたインスタンス
		if (Objects.nonNull(basePlugin)) {
			// IDのリンク付け替え
			basePlugin = basePlugin.setNext(id);
			list = setPluginList(list, basePlugin);
			// 連結追加の場合、クローンされたインスタンスを返却
			return script.setRawOutputPlugins(list);
		} else {
			// 単純追加の場合、元のインスタンスを返却
			return script.setRawOutputPlugins(list);
		}

	}

	/**
	 * 最後尾に指定したプラグインを追加します
	 * @param script スクリプトインスタンス
	 * @param pkgName 追加するプラグインパッケージ名称
	 * @param name 追加するプラグイン名称
	 * @return クローンされたアクセッサインスタンス
	 */
	public KagerowScriptAccessor addInputPlugin(KagerowScriptAccessor script, String pkgName, String name) {
		if (script.getRawInputPlugins().isEmpty()) {
			return addInputPlugin(script, null, pkgName, name);
		} else {
			return addInputPlugin(script, script.getRawInputPlugins().getLast().getId(), pkgName, name);
		}
	}

	/**
	 * 最後尾に指定したプラグインを追加します
	 * @param script スクリプトインスタンス
	 * @param pkgName 追加するプラグインパッケージ名称
	 * @param name 追加するプラグイン名称
	 * @return クローンされたアクセッサインスタンス
	 */
	public KagerowScriptAccessor addOutputPlugin(KagerowScriptAccessor script, String pkgName, String name) {
		if (script.getRawOutputPlugins().isEmpty()) {
			return addOutputPlugin(script, null, pkgName, name);
		} else {
			return addOutputPlugin(script, script.getRawOutputPlugins().getLast().getId(), pkgName, name);
		}
	}

	/**
	 * プラグインの削除処理を実行します
	 * @param list 削除対象リスト
	 * @param script スクリプトインスタンス
	 * @param index 削除対象インデックス
	 * @return 調整済みプラグインリスト
	 */
	private List<KagerowPluginAccessor> delPlugin(
			KagerowScriptAccessor script,
			List<KagerowPluginAccessor> list,
			int index) {
		// 削除処理
		KagerowPluginAccessor delPlugin = null;
		if (index != -1) {
			delPlugin = list.remove(index);
		} else {
			// 見つからなかった場合は即時終了
			throw new IllegalArgumentException("Plugin not found");
		}
		if (0 < index) {
			// 削除対象が先頭の場合差し替え処理は行わない
			// ID差し替えのため差し替え対象取得
			KagerowPluginAccessor plugin = list.get(index - 1);
			// ID差し替え
			plugin = plugin.setNext(delPlugin.next());
			list = setPluginList(list, plugin);
		}
		// 要素が空になった場合、元のインスタンスをそのまま返却する
		return list;
	}

	/**
	 * 指定したIDからプラグインを削除します
	 * @param script スクリプトインスタンス
	 * @param targetId 削除指定プラグインID
	 * @return クローンされたアクセッサインスタンス
	 */
	public KagerowScriptAccessor delOutputPlugin(KagerowScriptAccessor script, String targetId) {
		// リスト取得
		List<KagerowPluginAccessor> list = script.getRawOutputPlugins();
		// 削除インデックス番号
		int index = getPluginIndex(list, targetId);
		// 削除処理
		list = delPlugin(script, list, index);
		KagerowScriptAccessor newInstance = script.setRawOutputPlugins(list);
		return newInstance;
	}

	/**
	 * 指定したIDからプラグインを削除します
	 * @param script スクリプトインスタンス
	 * @param targetId 削除指定プラグインID
	 * @return クローンされたアクセッサインスタンス
	 */
	public KagerowScriptAccessor delInputPlugin(KagerowScriptAccessor script, String targetId) {
		// リスト取得
		List<KagerowPluginAccessor> list = script.getRawInputPlugins();
		// 削除インデックス番号
		int index = getPluginIndex(list, targetId);
		// 削除処理
		list = delPlugin(script, list, index);
		KagerowScriptAccessor newInstance = script.setRawInputPlugins(list);
		return newInstance;
	}

	/**
	 * 指定したIDの後ろに指定したプラグインを追加します
	 * @param script スクリプトインスタンス
	 * @param targetId 指定プラグインID
	 * @param name 追加するプラグイン名称
	 * @return クローンされたアクセッサインスタンス
	 */
	public KagerowScriptAccessor addKsql(KagerowScriptAccessor script, String targetId, String name) {

		// リスト取得
		List<KagerowSqlAccessor> list = script.getRawKsqls();
		// 追加先インデックス番号
		int index = getKsqlIndex(list, targetId);
		// 追加する際のIDを生成
		String id = getKsqlId(script, ID_LEN);
		// 追加処理
		KagerowSqlAccessor baseKsql = null;
		String nextId = null;
		if (index != -1) {
			baseKsql = list.get(index);
			nextId = baseKsql.next();
		}

		// KSQLインスタンス生成
		KagerowSqlAccessor newKSql = script.newKagerowSqlAccessor(name, id, nextId, DEFAULT_SQL);
		// 生成したインスタンスをインスタンス管理下に追加
		// この処理を行ってから出ないとsetNextで新たなインスタンスに生成したインスタンスが含まれない
		list.add(newKSql);

		if (Objects.nonNull(baseKsql)) {
			// 既存スクリプトへの追加の場合
			baseKsql = baseKsql.setNext(id);
			KagerowScriptAccessor newInstance = script.setRawKsqls(list);
			newInstance = setRawKsqls(newInstance, baseKsql);
			return newInstance;
		} else {
			// 完全な新規作成の場合
			return script.setRawKsqls(list);
		}

	}

	/**
	 * 最後尾に指定したKsqlを追加します
	 * @param script スクリプトインスタンス
	 * @param name 追加するKsql名称
	 * @return クローンされたアクセッサインスタンス
	 */
	public KagerowScriptAccessor addKsql(KagerowScriptAccessor script, String name) {
		if (script.getRawKsqls().isEmpty()) {
			return addKsql(script, null, name);
		} else {
			return addKsql(script, script.getRawKsqls().getLast().getId(), name);
		}
	}

	/**
	 * KSQLの削除処理を実行します
	 * @param script スクリプトインスタンス
	 * @param index 削除対象インデックス
	 * @return クローンされたアクセッサインスタンス
	 */
	private KagerowScriptAccessor delKsql(KagerowScriptAccessor script, int index) {
		// リスト取得
		List<KagerowSqlAccessor> list = script.getRawKsqls();
		// 削除処理
		KagerowSqlAccessor delKsql = null;
		if (index != -1) {
			// リストから対象を削除
			delKsql = list.remove(index);
			// オブジェクトのリストをリフレッシュ
			script = script.setRawKsqls(list);
			// ID差し替えのため差し替え対象取得
			KagerowSqlAccessor ksql;
			if (0 < index) {
				// ID差し替え
				ksql = list.get(index - 1);
				ksql = ksql.setNext(delKsql.next());
			} else {
				// 削除対象が先頭の場合差し替え処理は行わない
				return script.setRawKsqls(new Vector<>());
			}
			KagerowScriptAccessor newInstance = setRawKsqls(script, ksql);
			return newInstance;
		} else {
			// 見つからなかった場合は即時終了
			return script;
		}
	}

	/**
	 * 指定したIDからKSQLを削除します
	 * @param script スクリプトインスタンス
	 * @param targetId 削除指定KSQLID
	 * @return クローンされたアクセッサインスタンス
	 */
	public KagerowScriptAccessor delKsql(KagerowScriptAccessor script, String targetId) {
		// リスト取得
		List<KagerowSqlAccessor> list = script.getRawKsqls();
		// 削除インデックス番号
		int index = getKsqlIndex(list, targetId);
		// 削除処理
		KagerowScriptAccessor newInstance = delKsql(script, index);
		return newInstance;
	}

	/**
	 * コマンドインスタンスを差し替えます
	 * @param kagerowScriptAccessor アクセッサ
	 * @param cmd コマンドインスタンス
	 * @return 差し替え済みアクセッサ
	 */
	public KagerowScriptAccessor setRawCommand(KagerowScriptAccessor kagerowScriptAccessor, KagerowCmdAccessor cmd) {
		Vector<KagerowCmdAccessor> list = new Vector<>();
		list.add(cmd);
		return kagerowScriptAccessor.setRawCommand(list);
	}

	/**
	 * KSQLインスタンスを差し替えます
	 * @param kagerowScriptAccessor アクセッサ
	 * @param ksql KSQLインスタンス
	 * @return 差し替え済みアクセッサ
	 */
	public KagerowScriptAccessor setRawKsqls(KagerowScriptAccessor kagerowScriptAccessor, KagerowSqlAccessor ksql) {
		Vector<KagerowSqlAccessor> list = new Vector<>();
		for (KagerowSqlAccessor old : kagerowScriptAccessor.getRawKsqls()) {
			if (ksql.isSame(old)) {
				list.add(ksql);
			} else {
				list.add(old);
			}
		}
		return kagerowScriptAccessor.setRawKsqls(list);
	}

	/**
	 * プラグインインスタンスを差し替えます
	 * @param plugins プラグイン一覧
	 * @param plugin プラグインインスタンス
	 * @return 差し替え済みリスト
	 */
	public List<KagerowPluginAccessor> setPluginList(
			List<KagerowPluginAccessor> plugins,
			KagerowPluginAccessor plugin) {
		Vector<KagerowPluginAccessor> list = new Vector<>();
		for (KagerowPluginAccessor old : plugins) {
			if (plugin.isSame(old)) {
				list.add(plugin);
			} else {
				list.add(old);
			}
		}
		return list;
	}

}

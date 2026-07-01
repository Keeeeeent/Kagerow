package com.sakulabo.application.service.Context;

import java.util.List;
import java.util.Objects;

import com.sakulabo.regulation.annotation.KagerowPlugin.PluginType;

/**
 * プラグインコンテキストサービスの規定インターフェイスです
 * 
 * @author keeeeeent
 */
public interface PluginContextService {

	/**
	 * プラグイン情報格納レコードクラス
	 * @param packageName パッケージ名称
	 * @param pluginName プラグイン名称
	 * @param viewName バージョニング名称
	 */
	public record PluginContextInfo(
			String packageName,
			String pluginName,
			String viewName) {

		/**
		 * コンパクトコンストラクタ
		 * @param packageName パッケージ名称
		 * @param pluginName プラグイン名称
		 */
		public PluginContextInfo {
			Objects.requireNonNull(packageName);
			Objects.requireNonNull(pluginName);
			Objects.requireNonNull(viewName);
		}

		/**
		 * プラグイン名称をパッケージ名を含めた完全名称で返却します<br/>
		 * フォーマット:PluginPackageName/PluginName
		 * @return プラグイン完全名称
		 */
		@Override
		public final String toString() {
			return String.join("/", viewName, pluginName);
		}

	};

	/**
	 * プラグイン一覧を取得します<br />
	 * 要素が取得できない場合は空のリストを返却します
	 * @return プラグイン情報一覧リスト
	 */
	public List<PluginContextInfo> getPluginList();

	/**
	 * 指定したプラグインタイプの一覧を取得します<br />
	 * @param type プラグインタイプ
	 * @return プラグイン情報一覧リスト
	 */
	public List<PluginContextInfo> getPluginList(PluginType type);

}

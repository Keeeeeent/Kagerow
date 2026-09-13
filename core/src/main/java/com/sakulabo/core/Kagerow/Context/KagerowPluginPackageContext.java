package com.sakulabo.core.Kagerow.Context;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.NamingException;

import com.sakulabo.core.Common.StringUtils;
import com.sakulabo.core.Kagerow.Context.Impl.KagerowPluginPackageContextImpl;

/**
 * Kagerowアプリケーションのコンテキスト拡張インターフェースです<br/>
 * このインターフェースの実装クラスでは、Kagerowのプラグインパッケージへの各種アクセスを提供します
 *
 * @author keeeeeent
 */
public sealed interface KagerowPluginPackageContext extends KagerowContexts<KagerowPluginContext>
		permits KagerowPluginPackageContextImpl {

	/** コンテキスト名称 */
	public static final String _NAME = "KagerowPluginPackage";

	/** デフォルトパッケージ名称 */
	public static final String DEFAULT_PKG_NAME = StringUtils.DEFAULT;

	/**
	 * パッケージの無効化を行います
	 * @param disable 無効化フラグ（true:無効化,false:有効化）
	 * @param name 処理対象パッケージ名
	 * @throws NamingException パッケージが見つからなかった場合
	 */
	public void setDisable(boolean disable, String name) throws NamingException;

	/**
	 * パッケージの無効化設定を確認します
	 * @param name 確認対象パッケージ名称
	 * @return 確認結果（true:無効化中,false:有効化中）
	 * @throws NamingException
	 */
	public boolean isDisable(String name) throws NamingException;

	/**
	 * 対象パッケージ名称がデフォルトパッケージか判定します
	 * @param name 判定対象
	 * @return 判定結果
	 */
	public static boolean isDefault(String name) {
		Pattern pattern = Pattern.compile("([^/]+(?=/))|(.+(?=@))");
		Matcher matcher = pattern.matcher(name);
		if (matcher.find()) {
			return DEFAULT_PKG_NAME.equals(matcher.group());
		}
		return false;
	}

}

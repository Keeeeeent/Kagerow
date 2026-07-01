package com.sakulabo.core.Kagerow.Context;

import java.nio.file.Path;

import javax.naming.NamingException;

import com.sakulabo.core.Kagerow.Contents.KagerowPluginContent;
import com.sakulabo.core.Kagerow.Context.Impl.KagerowPluginContextImpl;

/**
 * Kagerowアプリケーションのコンテキスト拡張インターフェースです<br/>
 * このインターフェースの実装クラスでは、Kagerowのプラグインへの各種アクセスを提供します
 * 
 * @author keeeeeent
 */
public sealed interface KagerowPluginContext
		extends KagerowContexts<KagerowPluginContent>
		permits KagerowPluginContextImpl {

	/** コンテキスト名称 */
	public static final String _NAME = "KagerowPlugin";

	/**
	 * プラグインの無効化を行います
	 * @param disable 無効化フラグ（true:無効化,false:有効化）
	 * @param name 処理対象プラグイン名
	 * @throws NamingException プラグインが見つからなかった場合
	 */
	public void setDisable(boolean disable, String name) throws NamingException;

	/**
	 * プラグインの無効化設定を確認します
	 * @param name 確認対象プラグイン名称
	 * @return 確認結果（true:無効化中,false:有効化中）
	 * @throws NamingException
	 */
	public boolean isDisable(String name) throws NamingException;

	/**
	 * プラグイン物理ファイルパスを取得します
	 * @return プラグイン物理ファイルパス
	 */
	public Path getPluginFilePath();

}

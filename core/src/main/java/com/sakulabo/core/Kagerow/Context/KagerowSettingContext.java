package com.sakulabo.core.Kagerow.Context;

import javax.naming.Name;
import javax.naming.NamingException;

import com.sakulabo.core.Kagerow.Contents.KagerowSettingContent;
import com.sakulabo.core.Kagerow.Context.Impl.KagerowSettingContextImpl;

/**
 * Kagerowアプリケーションのコンテキスト拡張インターフェースです<br/>
 * このインターフェースの実装クラスでは、Kagerow設定管理機能への各種アクセスを提供します
 * 
 * @author keeeeeent
 */
public sealed interface KagerowSettingContext extends KagerowContexts<KagerowSettingContent>
		permits KagerowSettingContextImpl {

	/** コンテキスト名称文字列 */
	public static final String _NAME = "KagerowSetting";

	/** {@inheritDoc} */
	@Override
	public KagerowSettingContent createSubcontext(Name name) throws NamingException;

	/** {@inheritDoc} */
	@Override
	public KagerowSettingContent createSubcontext(String name) throws NamingException;

}

package com.sakulabo.core.Kagerow.Context;

import javax.naming.Name;
import javax.naming.NamingException;

import com.sakulabo.core.Kagerow.Contents.KagerowSecurityContent;
import com.sakulabo.core.Kagerow.Context.Impl.KagerowSecurityContextImpl;

/**
 * Kagerowアプリケーションのコンテキスト拡張インターフェースです<br/>
 * このインターフェースの実装クラスでは、Kagerow暗号化機能への各種アクセスを提供します
 * 
 * @author keeeeeent
 */
public sealed interface KagerowSecurityContext extends KagerowContexts<KagerowSecurityContent>
		permits KagerowSecurityContextImpl {

	/** コンテキスト名称 */
	public static final String _NAME = "KagerowSecurity";

	/** {@inheritDoc} */
	@Override
	public KagerowSecurityContent createSubcontext(Name name) throws NamingException;

	/** {@inheritDoc} */
	@Override
	public KagerowSecurityContent createSubcontext(String name) throws NamingException;

}

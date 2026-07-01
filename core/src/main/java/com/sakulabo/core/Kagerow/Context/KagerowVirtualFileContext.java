package com.sakulabo.core.Kagerow.Context;

import java.util.Map;

import javax.naming.Name;
import javax.naming.NamingException;

import com.sakulabo.core.Kagerow.Context.Impl.KagerowVirtualFileContextImpl;
import com.sakulabo.core.Processor.database.AppConnectionHandler;

/**
 * Kagerowアプリケーションのコンテキスト拡張インターフェースです<br/>
 * このインターフェースの実装クラスでは、Kagerowの仮想ファイルシステムへの各種アクセスを提供します
 * 
 * @author keeeeeent
 */
public sealed interface KagerowVirtualFileContext
		extends KagerowContexts<KagerowVirtualDirContext>
		permits KagerowVirtualFileContextImpl {

	/** コンテキスト名称 */
	public static final String _NAME = "KagerowVirtualFile";

	/** システム専用スキーマ名称 */
	public static final String SYSTEM_SCHEMA = AppConnectionHandler.schemaName;

	/** {@inheritDoc} */
	@Override
	public KagerowVirtualDirContext createSubcontext(Name name) throws NamingException;

	/** {@inheritDoc} */
	@Override
	public KagerowVirtualDirContext createSubcontext(String name) throws NamingException;

	/**
	 * コンテキストの実態を取得します
	 * @return コンテキスト実態
	 */
	@Deprecated(forRemoval = true)
	public Map<String, String> getContext();

}
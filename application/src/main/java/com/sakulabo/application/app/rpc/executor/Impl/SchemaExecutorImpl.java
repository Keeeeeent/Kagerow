package com.sakulabo.application.app.rpc.executor.Impl;

import java.util.ArrayList;
import java.util.List;

import javax.naming.Binding;
import javax.naming.Name;
import javax.naming.NamingEnumeration;

import com.sakulabo.application.app.rpc.RpcFilter;
import com.sakulabo.application.app.rpc.RpcMethod;
import com.sakulabo.application.app.rpc.RpcSetting;
import com.sakulabo.application.app.rpc.datatype.send.ArraySendDataType;
import com.sakulabo.application.app.rpc.exception.RpcRuntimeException;
import com.sakulabo.application.app.rpc.executor.SchemaExecutor;
import com.sakulabo.application.app.rpc.filters.CertificationFilter;
import com.sakulabo.core.Kagerow.Context.KagerowVirtualFileContext;
import com.sakulabo.core.Kagerow.Utilities.KagerowUtilities;

/**
 * スクリプト実行機能のRPCコントローラー実装クラス
 *
 * @author keeeeeent
 */
@RpcSetting("schema")
@RpcFilter(filter = CertificationFilter.class, required = true)
public class SchemaExecutorImpl implements SchemaExecutor {

	/**
	 * スキーマ一覧リストを取得します
	 *
	 * @return 取得結果
	 * @throws RpcRuntimeException メソッド実行失敗
	 */
	/** {@inheritDoc} */
	@Override
	@RpcMethod("list")
	public SchemaList getSchemaList() throws RpcRuntimeException {
		try {
			List<String> schemaList = new ArrayList<>();
			KagerowVirtualFileContext ctx = KagerowUtilities.getContext(KagerowVirtualFileContext._NAME);
			NamingEnumeration<Binding> list = ctx.listBindings((Name) null);
			while (list.hasMore()) {
				String schemaName = list.next().getName();
				if (!KagerowVirtualFileContext.SYSTEM_SCHEMA.equals(schemaName)) {
					schemaList.add(schemaName);
				}
			}
			SchemaList result = new SchemaList(ArraySendDataType.getInstance(schemaList));
			return result;
		} catch (Exception e) {
			// 例外翻訳
			throw new RpcRuntimeException(e.getMessage());
		}
	}

}

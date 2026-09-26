package com.sakulabo.application.app.rpc.executor.Impl;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.List;

import javax.naming.Binding;
import javax.naming.Name;
import javax.naming.NamingEnumeration;

import com.sakulabo.application.app.rpc.RpcFilter;
import com.sakulabo.application.app.rpc.RpcMethod;
import com.sakulabo.application.app.rpc.RpcMethodParam;
import com.sakulabo.application.app.rpc.RpcSetting;
import com.sakulabo.application.app.rpc.datatype.receive.StringReceiveDataType;
import com.sakulabo.application.app.rpc.datatype.send.ArraySendDataType;
import com.sakulabo.application.app.rpc.datatype.send.DateTimeSendDataType;
import com.sakulabo.application.app.rpc.datatype.send.IntegerSendDataType;
import com.sakulabo.application.app.rpc.datatype.send.StringSendDataType;
import com.sakulabo.application.app.rpc.exception.ExitCodeException;
import com.sakulabo.application.app.rpc.exception.RpcRuntimeException;
import com.sakulabo.application.app.rpc.executor.SchemaExecutor;
import com.sakulabo.application.app.rpc.filters.CertificationFilter;
import com.sakulabo.core.Kagerow.Context.KagerowVirtualDirContext;
import com.sakulabo.core.Kagerow.Context.KagerowVirtualFileContext;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;
import com.sakulabo.core.Kagerow.Utilities.KagerowTransaction;
import com.sakulabo.core.Kagerow.Utilities.KagerowUtilities;

/**
 * スクリプト実行機能のRPCコントローラー実装クラス
 *
 * @author keeeeeent
 */
@RpcSetting("schema")
@RpcFilter(filter = CertificationFilter.class, required = true)
public class SchemaExecutorImpl implements SchemaExecutor {

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

	/** {@inheritDoc} */
	@Override
	@RpcMethod("delete")
	public void deleteSchema(
			@RpcMethodParam(value = "schemaName", required = true) StringReceiveDataType schema)
			throws ExitCodeException {
		String schemaName = schema.getRawType().get();
		if (KagerowVirtualFileContext.SYSTEM_SCHEMA.equals(schemaName)) {
			throw new ExitCodeException("Cannot delete the schema under system control", 2);
		}
		try {
			KagerowTransaction tran = KagerowTransaction.getTransactionFromSchemaName(schemaName);
			try (tran) {
				KagerowVirtualFileContext ctx = KagerowUtilities.getContext(KagerowVirtualFileContext._NAME);
				ctx.destroySubcontext(schemaName);
				tran.commit();
			} catch (Exception e) {
				KagerowLogger.newAppLogger().err(e);
				tran.rollback(e);
				throw new ExitCodeException("Failed to delete the schema", 1);
			}
		} catch (IOException ioe) {
			KagerowLogger.newAppLogger().err(ioe);
			throw new ExitCodeException("Failed to retrieve the transaction", 1);
		}
	}

	/** {@inheritDoc} */
	@Override
	@RpcMethod("info")
	public SchemaInfo getSchemaInfo(
			@RpcMethodParam(value = "schemaName", required = true) StringReceiveDataType schema)
			throws ExitCodeException {
		String schemaName = schema.getRawType().get();
		if (KagerowVirtualFileContext.SYSTEM_SCHEMA.equals(schemaName)) {
			throw new ExitCodeException("Cannot delete the schema under system control", 2);
		}
		try {
			KagerowVirtualFileContext ctx = KagerowUtilities.getContext(KagerowVirtualFileContext._NAME);
			KagerowVirtualDirContext cnt = ctx.lookup(schemaName);
			// 最終更新日
			Path path = cnt.getPath();
			BasicFileAttributeView view = Files.getFileAttributeView(path, BasicFileAttributeView.class);
			BasicFileAttributes attr = view.readAttributes();
			FileTime time = attr.lastModifiedTime();
			String lastUpdated = time.toInstant().toString();
			// サイズ
			long size = cnt.getSchemaContextSize();
			// テーブル数
			int tables = cnt.getSynonymMapList().size();
			StringSendDataType sendSchemaName = new StringSendDataType(schemaName);
			IntegerSendDataType sendSize = new IntegerSendDataType(BigInteger.valueOf(size));
			IntegerSendDataType sendTables = new IntegerSendDataType(BigInteger.valueOf(tables));
			DateTimeSendDataType sendLastUpdated = new DateTimeSendDataType(lastUpdated);
			SchemaInfo info = new SchemaInfo(
					sendSchemaName,
					sendSize,
					sendTables,
					sendLastUpdated);
			return info;
		} catch (Exception e) {
			KagerowLogger.newAppLogger().err(e);
			throw new ExitCodeException("Failed to retrieve schema information", 1);
		}
	}

}

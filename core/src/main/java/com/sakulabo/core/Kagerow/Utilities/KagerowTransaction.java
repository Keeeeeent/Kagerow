package com.sakulabo.core.Kagerow.Utilities;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.Objects;

import com.sakulabo.core.Processor.transaction.FileTransaction;
import com.sakulabo.core.Processor.transaction.SchemaTransaction;
import com.sakulabo.core.Provides.ArchiveSystemProvider;

/**
 * ファイルシステムのトランザクションを管理します
 * 
 * @author keeeeeent
 */
public sealed interface KagerowTransaction extends AutoCloseable
		permits FileTransaction, SchemaTransaction {

	/**
	 * 現在のトランザクションを確定させます
	 * @throws IOException トランザクション確定失敗
	 */
	void commit() throws IOException;

	/**
	 * 現在のトランザクションをロールバックします
	 * @param e 例外クラス
	 */
	void rollback(Throwable e);

	/**
	 * ファイルシステムのトランザクションを取得します
	 * @param uri 対象URI
	 * @return トランザクション管理インスタンス
	 */
	public static KagerowTransaction getTransaction(URI uri) {
		Objects.requireNonNull(uri);
		return ArchiveSystemProvider.getTransaction(uri);
	}

	/**
	 * スキーマ名称からファイルシステムのトランザクションを取得します
	 * @param schema 対象スキーマ
	 * @return トランザクション管理インスタンス
	 * @throws IOException トランザクション生成失敗
	 * @throws FileNotFoundException ファイルが存在しな場合
	 */
	public static KagerowTransaction getTransactionFromSchemaName(String schema) throws IOException {
		Objects.requireNonNull(schema);
		return new SchemaTransaction(schema);
	}

	@Override
	void close() throws IOException;

}
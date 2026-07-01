package com.sakulabo.core.Processor.transaction;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.spi.FileSystemProvider;

import com.sakulabo.core.Common.AppPathUtils;
import com.sakulabo.core.Common.VMOption;
import com.sakulabo.core.Kagerow.Utilities.KagerowTransaction;
import com.sakulabo.core.Provides.ArchiveSystemProvider;

/**
 * 仮想ファイルシステムのトランザクション管理をするクラスです 
 * このクラスはスキーマ名称に対してトランザクション制御を行います
 */
public final class SchemaTransaction implements KagerowTransaction {

	/** 委譲先のインスタンス */
	private final FileTransaction tran;

	/**
	 * デフォルトコンストラクタ
	 * @param schema スキーマ名称
	 * @throws IOException トランザクション生成失敗
	 */
	public SchemaTransaction(String schema) throws IOException {
		// ファイルパス生成のためのシステム設定を取得
		String archivePath = VMOption.APP_IO_ARCHIVEDATADIR.getVMoption();
		String userHome = AppPathUtils.getBasePath();
		// ファイルパスへ変換
		Path path = Paths.get(userHome, archivePath)
				.normalize()
				.toAbsolutePath()
				.resolve(schema.concat(AppPathUtils.ZIP_EXTENSION));
		// ファイルの存在確認
		if (Files.notExists(path)) {
			throw new FileNotFoundException(path.normalize().toAbsolutePath().toString());
		}
		// ファイルシステムオープン
		FileSystemProvider provider = ArchiveSystemProvider.getFileSystemProvider();
		provider.newFileSystem(path.toUri(), null);
		// トランザクション制御インスタンス生成
		tran = new FileTransaction(path, path.toUri());
	}

	/** {@inheritDoc} */
	@Override
	public void commit() throws IOException {
		tran.commit();
	}

	/** {@inheritDoc} */
	@Override
	public void rollback(Throwable e) {
		tran.rollback(e);
	}

	/** {@inheritDoc} */
	@Override
	public void close() throws IOException {
		tran.close();
	}

}

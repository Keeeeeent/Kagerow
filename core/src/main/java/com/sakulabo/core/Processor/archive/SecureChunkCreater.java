package com.sakulabo.core.Processor.archive;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

import javax.crypto.SecretKey;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.NotContextException;

import com.sakulabo.core.Kagerow.KagerowApplication;
import com.sakulabo.core.Kagerow.Contents.KagerowSecurityContent;
import com.sakulabo.core.Kagerow.Contents.KagerowVirtualFileContent.KagerowDataType;
import com.sakulabo.core.Kagerow.Contents.KagerowVirtualFileContent.KagerowVirtualFileObject.SecureFileObject;
import com.sakulabo.core.Kagerow.Context.KagerowSecurityContext;
import com.sakulabo.core.Kagerow.Exception.AppLogicException;
import com.sakulabo.core.Kagerow.Utilities.KagerowChunkCreater;
import com.sakulabo.core.Kagerow.Utilities.KagerowFileBodyReader;
import com.sakulabo.core.Kagerow.Utilities.KagerowFileHeaderReader;
import com.sakulabo.core.Kagerow.Utilities.KagerowFileReaderFactory;
import com.sakulabo.core.Processor.security.AppHashEncrypter;

/**
 * ファイルからchunk毎のアーカイブファイル群を生成します<br/>
 * バイナリファイル:物理データファイル（.dat）
 * インデックスファイル:物理データアクセスインデックスファイル（.idx）
 * 
 * @author keeeeeent
 */
public final class SecureChunkCreater extends AppChunkCreater<SecureFileObject>
		implements KagerowChunkCreater<SecureFileObject> {

	/** 構築に使用したチャンクライター */
	private SecureChunkWriter chunkWriter;
	/** ファイルreader抽象ファクトリ */
	private KagerowFileReaderFactory factory;

	/**
	 * デフォルトコンストラクタ
	 * @param schema スキーマ名称
	 * @param path 入力ファイル
	 * @param charset 読み込み文字コード
	 * @param isHeader ヘッダー有無
	 * @param factory ファイルreader抽象ファクトリ
	 * @throws AppLogicException 入力ファイルが存在しない場合
	 */
	public SecureChunkCreater(String schema, Path path, Charset charset, boolean isHeader,
			KagerowFileReaderFactory factory)
			throws AppLogicException {
		super(schema, path, charset, isHeader);
		this.factory = factory;
	}

	/** {@inheritDoc} */
	@Override
	protected ChunkWriter createChunkWriter(MessageDigest digest, Charset charset, String schema)
			throws AppLogicException, IOException {
		chunkWriter = new SecureChunkWriter(digest, charset, schema);
		return chunkWriter;
	}

	/** {@inheritDoc} */
	@Override
	protected KagerowFileBodyReader createBodyReader(Path path, Charset charset, boolean isHeader) throws IOException {
		return factory.createFileBodyReader(path, charset, isHeader);
	}

	/** {@inheritDoc} */
	@Override
	protected KagerowFileHeaderReader createHeaderReader(Path path, Charset charset, boolean isHeader)
			throws IOException {
		return factory.createFileHeaderReader(path, charset, isHeader);
	}

	/** {@inheritDoc} */
	@Override
	protected SecureFileObject createKagerowVirtualFileObject(String synonym, tmpDataSet bodyData,
			FileReader hederReader, HashPathCreater pathCreater) throws AppLogicException, IOException {

		// ヘッダー情報読取
		String[] headerData = hederReader.readLine();
		// データタイプ
		KagerowDataType[] dataType = bodyData.dataType();

		// ハッシュ更新
		Name name = pathCreater.toHashPath(headerData, dataType);
		String binaryName = toTableName(name);

		// 秘密鍵取得
		SecretKey key = chunkWriter.getKey();
		// エイリアス生成
		AppHashEncrypter aliasEncrypter = new AppHashEncrypter(binaryName);

		// 変数初期化
		String password, encPassword;
		try {
			// セキュリティコンテキスト取得
			KagerowSecurityContext kagerowSecurityContext = (KagerowSecurityContext) KagerowApplication.getInstance()
					.getContext().lookup(KagerowSecurityContext._NAME);
			// セキュリティコンテンツ取得
			KagerowSecurityContent kagerowSecurityContent;
			try {
				kagerowSecurityContent = kagerowSecurityContext.lookup(schema);
			} catch (NotContextException e) {
				// KeyStoreを初期化
				kagerowSecurityContent = kagerowSecurityContext.createSubcontext(schema);
			}
			// パスワード取得
			password = aliasEncrypter.getPassword();
			// キー登録
			encPassword = kagerowSecurityContent.bind(password, key);
		} catch (NamingException e) {
			throw new AppLogicException("Context or Content not found", e);
		}

		// データセット精鋭
		SecureFileObject data = new SecureFileObject(
				Instant.now(),
				headerData,
				dataType,
				bodyData.dataSize(),
				bodyData.datAddr(),
				bodyData.datSize(),
				bodyData.idxAddr(),
				bodyData.idxSize(),
				binaryName,
				synonym,
				schema,
				encPassword,
				password,
				new AtomicReference<>());

		return data;
	}

}

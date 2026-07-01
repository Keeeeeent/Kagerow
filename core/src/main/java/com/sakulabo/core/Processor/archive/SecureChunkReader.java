package com.sakulabo.core.Processor.archive;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.RandomAccessFile;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

import com.sakulabo.core.Kagerow.Contents.KagerowVirtualFileContent.KagerowVirtualFileObject;
import com.sakulabo.core.Kagerow.Exception.AppLogicException;
import com.sakulabo.core.Processor.security.AESKeyCreater;

/**
 * チャンクファイルを読み込みます
 * 
 * @author keeeeeent
 */
public class SecureChunkReader extends ChunkReader {

	/** 秘密鍵 */
	private final SecretKey key;
	/** 暗号化機能提供インスタンス */
	private final Cipher cipher;

	/**
	 * デフォルトコンストラクタ
	 * 
	 * @param chunk チャンクインスタンス
	 * @param key 秘密鍵 
	 * @param start 開始位置
	 * @param end 終了位置
	 * @param max ファイル終端
	 * @throws AppLogicException チャンクファイル不正
	 * @throws IOException 出力不可の場合
	 */
	SecureChunkReader(KagerowVirtualFileObject chunk, SecretKey key, long start, long end, long max)
			throws AppLogicException, IOException {
		super(chunk, start, end, max);
		this.key = key;
		try {
			this.cipher = Cipher.getInstance(AESKeyCreater.GCM);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			throw new AppLogicException("SecureChunkWriter is not initialized", e);
		}
	}

	/** {@inheritDoc} */
	@Override
	@SuppressWarnings("unchecked")
	protected List<String[]> read(byte[] binData, RandomAccessFile datFile) throws IOException, AppLogicException {

		List<String[]> result = Collections.emptyList();

		// 初期化ベクトル取得
		byte[] iv = AESKeyCreater.getIV();
		datFile.read(iv);

		// 初期化ベクトル生成
		GCMParameterSpec ivParam = new GCMParameterSpec(128, iv);

		try {
			// 暗号化前に初期化を実行（Cipherを再利用）
			cipher.init(Cipher.DECRYPT_MODE, key, ivParam);
			// 復号化
			binData = cipher.doFinal(binData);
		} catch (InvalidKeyException
				| InvalidAlgorithmParameterException
				| IllegalBlockSizeException
				| BadPaddingException e) {
			// 何かしらの状況で復号化に失敗した場合は例外を翻訳
			throw new AppLogicException("SecureChunkReader can not decrypt", e);
		}

		// データデシリアライズ
		try (InputStream input = new ByteArrayInputStream(binData);
				ObjectInputStream oinput = new ObjectInputStream(input);) {
			try {
				result = (List<String[]>) oinput.readObject();
			} catch (ClassNotFoundException e) {
				// このタイミングで読み込みクラスが見つからない場合は、IOExceptionとして翻訳
				// ファイル不正と同等の扱い
				throw new IOException(e);
			}
		}

		return result;
	}

}

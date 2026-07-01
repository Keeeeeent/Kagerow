package com.sakulabo.core.Processor.archive;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

import com.sakulabo.core.Kagerow.Exception.AppLogicException;
import com.sakulabo.core.Processor.security.AESKeyCreater;

/**
 * データからチャンクファイルを生成します
 * 
 * @author keeeeeent
 */
public class SecureChunkWriter extends ChunkWriter {

	/** 秘密鍵 */
	private final SecretKey key;
	/** 擬似真正乱数生成インスタンス */
	private final SecureRandom rand = new SecureRandom();
	/** 暗号化機能提供インスタンス */
	private final Cipher cipher;

	/**
	 * デフォルトコンストラクタ
	 * @param digest ハッシュ関数
	 * @param charset 文字コード
	 * @param schema スキーマ
	 * @throws AppLogicException チャンクファイル不正
	 * @throws IOException 出力不可の場合
	 */
	protected SecureChunkWriter(MessageDigest digest, Charset charset, String schema)
			throws AppLogicException, IOException {
		super(digest, charset, schema);
		try {
			this.key = AESKeyCreater.getKey();
			this.cipher = Cipher.getInstance(AESKeyCreater.GCM);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			throw new AppLogicException("SecureChunkWriter is not initialized", e);
		}
	}

	/** {@inheritDoc} */
	@Override
	protected void write(RandomAccessFile dataAccess, int dataSize, byte[] byteData)
			throws AppLogicException, IOException {

		// 初期化ベクトル生成
		byte[] iv = AESKeyCreater.getIV(rand);
		GCMParameterSpec ivParam = new GCMParameterSpec(128, iv);

		try {
			// 暗号化前に初期化を実行（Cipherを再利用）
			cipher.init(Cipher.ENCRYPT_MODE, key, ivParam);
			// 暗号化
			byteData = cipher.doFinal(byteData);
			dataSize = byteData.length;
		} catch (InvalidKeyException
				| InvalidAlgorithmParameterException
				| IllegalBlockSizeException
				| BadPaddingException e) {
			// 何かしらの状況で暗号化に失敗した場合は例外を翻訳
			throw new AppLogicException("SecureChunkWriter can not encrypt", e);
		}

		// データサイズ書き込み
		dataAccess.writeInt(dataSize);
		// データ書き込み
		dataAccess.write(byteData);
		// 初期化ベクトル書き込み
		dataAccess.write(cipher.getIV());

	}

	/**
	 * 秘密鍵を取得します
	 * @return 秘密鍵
	 */
	final SecretKey getKey() {
		return key;
	}

}

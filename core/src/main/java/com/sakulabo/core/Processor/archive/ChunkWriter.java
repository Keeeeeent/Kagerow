package com.sakulabo.core.Processor.archive;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import com.sakulabo.core.Common.DataSize;
import com.sakulabo.core.Common.StringUtils;
import com.sakulabo.core.Common.URINameParser;
import com.sakulabo.core.Kagerow.KagerowApplication;
import com.sakulabo.core.Kagerow.Exception.AppLogicException;

/**
 * データからチャンクファイルを生成します
 * 
 * @author keeeeeent
 */
abstract class ChunkWriter implements AutoCloseable {

	/** チャンクサイズ */
	protected static final int CHUNK_SIZE;
	static {
		int tmpSize = KagerowApplication.getConfig().KDB_CHUNK_SIZE();
		CHUNK_SIZE = DataSize.KB.toSize(tmpSize).intValue();
	}

	/** ハッシュアルゴリズム */
	protected final MessageDigest digest;
	/** 文字コード */
	protected final Charset charse;
	/** プールデータ容量 */
	protected int size;
	/** メモリプール */
	protected List<String[]> pool = new ArrayList<>();
	/** ファイルアクセッサ(data) */
	protected final RandomAccessFile dataAccess;
	/** 出力ストリーム(data) */
	protected ByteArrayOutputStream dataOutput;
	/** シリアライズストリーム(data) */
	protected ObjectOutputStream dataObjOutput;
	/** 書き込み先パス(data) */
	protected final Path datOutputPath;
	/** 書き込み先URI(data) */
	protected final URI datUri;
	/** 出力ストリーム(index) */
	protected final OutputStream indexOutput;
	/** シリアライズストリームバッファ(index) */
	protected final BufferedOutputStream indexBuffer;
	/** シリアライズストリーム(index) */
	protected final DataOutputStream indexObjOutput;
	/** 書き込み先パス(index) */
	protected final Path idxOutputPath;
	/** 書き込み先URI(index) */
	protected final URI idxUri;

	/** datファイル拡張子 */
	protected static final String DAT_FILE_EXT = ".dat";
	/** idxファイル拡張子 */
	protected static final String IDX_FILE_EXT = ".idx";

	/**
	 * デフォルトコンストラクタ
	 * @param digest ハッシュ関数
	 * @param charset 文字コード
	 * @param schema スキーマ
	 * @throws AppLogicException チャンクファイル不正
	 * @throws IOException 出力不可の場合
	 */
	protected ChunkWriter(MessageDigest digest, Charset charset, String schema) throws AppLogicException, IOException {

		// 共通
		this.digest = digest;
		this.charse = charset;
		URINameParser parser = new URINameParser(schema);

		// データ
		try {
			String datPath = parser.createBinaryPath(StringUtils.MD5, DAT_FILE_EXT);
			datUri = parser.toURI(datPath, URINameParser.BINARY_HOST);
			this.datOutputPath = Paths.get(datUri);
			this.dataAccess = new RandomAccessFile(datOutputPath.toFile(), StringUtils.RAND_READ_WRITE);
			this.dataOutput = new ByteArrayOutputStream();
			this.dataObjOutput = new ObjectOutputStream(this.dataOutput);

			// インデック
			String idxPath = parser.createBinaryPath(StringUtils.MD5, IDX_FILE_EXT);
			idxUri = parser.toURI(idxPath, URINameParser.BINARY_HOST);
			this.idxOutputPath = Paths.get(idxUri);
			this.indexOutput = Files.newOutputStream(this.idxOutputPath);
			this.indexBuffer = new BufferedOutputStream(this.indexOutput);
			this.indexObjOutput = new DataOutputStream(this.indexBuffer);
		} catch (NoSuchAlgorithmException e) {
			// アルゴリズムが不正である可能性は現状ありえないが、将来的に拡張したときデバックしやすいよう翻訳
			throw new AppLogicException("ChunkWriter is not initialized", e);
		} catch (URISyntaxException e) {
			// URIが不正な場合、誤ったコンストラクタ呼び出しのためアプリケーションエラーとして翻訳
			throw new AppLogicException("URI Syntax Error", e);
		}
	}

	/**
	 * バイトデータの書き込み処理を実行します
	 * @param dataAccess データ書き込み先
	 * @param dataSize 書き込みデータサイズ
	 * @param byteData 書き込みデータ
	 * @throws Exception 書き込み失敗
	 */
	protected abstract void write(RandomAccessFile dataAccess, int dataSize, byte[] byteData) throws Exception;

	/**
	 * データ書き込み処理を実行します
	 * @param data 書き込み対象データ
	 * @throws IOException データ更新エラー
	 */
	final void write(String[] data) throws IOException {

		// 内部データ更新
		update(data);
		pool.add(data);

		if (CHUNK_SIZE < size) {
			// 状態保存
			refresh();
		}

	}

	/**
	 * 内部データ更新
	 * @param data 蓄積対象データ
	 */
	private void update(String[] data) {
		for (String dat : data) {
			byte[] rowStr = dat.getBytes(charse);
			size += rowStr.length;
			digest.update(rowStr);
		}
	}

	/**
	 * 外部データ更新
	 * @throws IOException 
	 * @throws IOException データ更新エラー
	 */
	private void refresh() throws IOException {

		// ファイルポジション取得
		long pos = dataAccess.getFilePointer();
		// インデックス追加
		indexObjOutput.writeLong(pos);

		// オブジェクトをシリアライズ
		dataObjOutput.writeObject(pool);
		dataObjOutput.flush();

		// RAWデータ取得
		int dataSize = dataOutput.size();
		byte[] byteData = dataOutput.toByteArray();

		// データ書き込み
		try {
			write(dataAccess, dataSize, byteData);
		} catch (Exception e) {
			// 書き込み中何らかのエラーが発生した場合はIOエラーとして処理
			throw new IOException(e);
		}

		// バッファリセット
		dataOutput = new ByteArrayOutputStream();
		pool = new ArrayList<>();
		dataObjOutput = new ObjectOutputStream(dataOutput);
		size = 0;

	}

	/**
	 * 例外発生時に生成したデータファイル、インデックスファイルを削除します
	 * @throws IOException ロールバック時のファイル削除失敗
	 */
	public final void rollback() throws IOException {
		Files.delete(datOutputPath);
		Files.delete(idxOutputPath);
	}

	/**
	 * データファイル出力先URIを返却します
	 * @return URI 生成されたデータファイルURI文字列表現
	 */
	final String getDatOutputPath() {
		return datUri.toString();
	}

	/**
	 * データファイルサイズを返却します
	 * @return ファイルサイズ
	 * @throws IOException ファイルサイズ取得エラー 
	 */
	final BigInteger getDatOutputSize() throws IOException {
		return BigInteger.valueOf(Files.size(datOutputPath));
	}

	/**
	 * インデックスファイル出力先URIを返却します
	 * @return URI 生成されたインデックスファイルURI文字列表現
	 */
	final String getIdxOutputPath() {
		return idxUri.toString();
	}

	/**
	 * インデックスファイルサイズを返却します
	 * @return ファイルサイズ
	 * @throws IOException ファイルサイズ取得エラー
	 */
	final BigInteger getIdxOutputSize() throws IOException {
		return BigInteger.valueOf(Files.size(idxOutputPath));
	}

	/** {@inheritDoc} */
	@Override
	public void close() throws IOException {

		// 状態保存
		refresh();

		// ストリームクローズ
		dataObjOutput.close();
		dataOutput.close();
		dataAccess.close();
		indexObjOutput.close();
		indexOutput.close();

	}

}

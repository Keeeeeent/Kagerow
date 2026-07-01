package com.sakulabo.core.Processor.archive;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Objects;

import com.sakulabo.core.Common.StringUtils;

/**
 * ファイルから文字コードを推測します
 * 
 * @author keeeeeent
 */
public class BomHandler {

	/** 4バイト */
	private static final int SIZE = 4;
	/** BOM対象バイナリデータ格納先 */
	private byte[] bom = new byte[SIZE];

	/**
	 * デフォルトコンストラクタ
	 * @param path
	 * @throws IOException 読み込みファイル不正
	 */
	public BomHandler(Path path) throws IOException {
		try (RandomAccessFile file = new RandomAccessFile(path.toFile(), StringUtils.RAND_READ_ONLY);) {
			file.seek(0);
			file.readFully(bom);
		}
	}

	/**
	 * 文字コードの判定を行います
	 * @return 文字コードインスタンス
	 */
	public Charset getCharset() {

		if ((bom[0] & 0xFF) == 0x00 && (bom[1] & 0xFF) == 0x00 &&
				(bom[2] & 0xFF) == 0xFE && (bom[3] & 0xFF) == 0xFF) {
			// UTF-32 (BE)
			return Charset.forName("UTF-32BE");
		} else if ((bom[0] & 0xFF) == 0xFF && (bom[1] & 0xFF) == 0xFE &&
				(bom[2] & 0xFF) == 0x00 && (bom[3] & 0xFF) == 0x00) {
			// UTF-32 (LE)
			return Charset.forName("UTF-32LE");
		} else if ((bom[0] & 0xFF) == 0xEF && (bom[1] & 0xFF) == 0xBB && (bom[2] & 0xFF) == 0xBF) {
			// UTF-8
			return StandardCharsets.UTF_8;
		} else if ((bom[0] & 0xFF) == 0xFE && (bom[1] & 0xFF) == 0xFF) {
			// UTF-16(BE)
			return StandardCharsets.UTF_16BE;
		} else if ((bom[0] & 0xFF) == 0xFF && (bom[1] & 0xFF) == 0xFE) {
			// UTF-16(LE)
			return StandardCharsets.UTF_16LE;
		}

		return null;
	}

	/**
	 * 解析したBOMを元に、ストリームでスキップが必要なバイト数を算出します
	 * @return スキップが必要なバイト数
	 */
	public int skipByte() {
		int result = 0;
		Charset charset = getCharset();
		if (Objects.isNull(charset)) {
			return result;
		}
		switch (charset.toString()) {
		case "UTF-32BE":
		case "UTF-32LE":
			result = 4;
			break;
		case "UTF-16BE":
		case "UTF-16LE":
			result = 2;
			break;
		case "UTF-8":
			result = 3;
			break;
		}
		return result;
	}

}

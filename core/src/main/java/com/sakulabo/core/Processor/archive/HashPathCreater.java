package com.sakulabo.core.Processor.archive;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;

import javax.naming.CompositeName;
import javax.naming.InvalidNameException;
import javax.naming.Name;

import com.sakulabo.core.Common.ErrorMessage;
import com.sakulabo.core.Common.StringUtils;
import com.sakulabo.core.Kagerow.Contents.KagerowVirtualFileContent.KagerowDataType;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;

/**
 * ハッシュパスを生成するファクトリクラスです
 * 
 * @author keeeeeent
 */
class HashPathCreater {

	/** ハッシュアルゴリズム */
	private final MessageDigest digest;

	/**
	 * カスタマイズコンストラクタ
	 * @param algorithms アルゴリズム名称
	 * @throws NoSuchAlgorithmException 不正アルゴリズム
	 */
	HashPathCreater(String algorithms) throws NoSuchAlgorithmException {
		Objects.requireNonNull(algorithms, ErrorMessage.CODE_003.getMessage());
		digest = MessageDigest.getInstance(algorithms);
	}

	/**
	 * デフォルトコンストラクタ
	 * @throws NoSuchAlgorithmException 不正アルゴリズム
	 */
	HashPathCreater() throws NoSuchAlgorithmException {
		this(StringUtils.MD5);
	}

	/**
	 * 現在の時点で生成済みのハッシュパスを返却します
	 * @param header ヘッダー論理名格納リスト
	 * @param types  データタイプ格納リスト
	 * @return ハッシュパス
	 */
	public Name toHashPath(String[] header, KagerowDataType[] types) {

		// 名称生成
		final Name name = new CompositeName();

		try {

			// ヘッダー用メッセージダイジェスト生成
			MessageDigest headerDigest = MessageDigest.getInstance(digest.getAlgorithm());

			// ヘッダー情報ハッシュ追加(types)
			for (KagerowDataType type : types) {
				// バリデーション
				Objects.requireNonNull(type, ErrorMessage.CODE_002.getMessage());
				// 文字列に変換
				String clazzName = type.name();
				// ダイジェストに追加
				headerDigest.update(clazzName.getBytes(StandardCharsets.UTF_8));
			}

			// ヘッダー情報ハッシュ追加(header)
			for (String head : header) {
				// バリデーション
				Objects.requireNonNull(head, ErrorMessage.CODE_001.getMessage());
				// ダイジェストに追加
				headerDigest.update(head.getBytes(StandardCharsets.UTF_8));
			}

			// バイナリヘッダハッシュ
			byte[] hashHeaderBin = headerDigest.digest();
			// ハッシュヘッダ文字列化
			String hashHeader = HexFormat.of().formatHex(hashHeaderBin);
			// 名称追加
			name.add(hashHeader);

			// バイナリボディハッシュ
			byte[] hashBodyBin = digest.digest();
			// ハッシュボディ文字列化
			String hashBody = HexFormat.of().formatHex(hashBodyBin);
			// 名称追加
			name.add(hashBody);

		} catch (NoSuchAlgorithmException | InvalidNameException e) {
			/**
			 * NoSuchAlgorithmException
			 * 初期化の時点で不正名称はバリデーションしているため
			 * ここでは例外は発生しない
			 */
			/**
			 * InvalidNameException
			 * ハッシュ値を追加のためここでは例外は発生しない
			 */
			KagerowLogger.newAppLogger().err(e);
		}

		return name;
	}

	/**
	 * MessageDigestインスタンスを返却します
	 * @return MessageDigest ハッシュ関数インスタンス
	 */
	public MessageDigest getMessageDigest() {
		return digest;
	}

}

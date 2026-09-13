package com.sakulabo.core.Processor.archive;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.naming.Name;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sakulabo.BaseTest;
import com.sakulabo.core.Common.ErrorMessage;
import com.sakulabo.core.Common.URINameParser;
import com.sakulabo.core.Kagerow.Contents.KagerowVirtualFileContent.KagerowDataType;

/**
 * ハッシュパスを生成するファクトリクラスのテストクラスです
 */
public class HashPathCreaterTest extends BaseTest<HashPathCreater> {

	/** テスト対象 */
	private HashPathCreater testTarget;

	/**
	 * デフォルトコンストラクタ
	 */
	protected HashPathCreaterTest() {
		super(HashPathCreaterTest.class);
	}

	@BeforeEach
	void initService() throws NoSuchAlgorithmException {
		testTarget = new HashPathCreater();
	}

	@AfterEach
	void closeService() throws Exception {
	}

	/**
	 * [試験観点] : hash生成
	 * [期待される結果] : 正常終了すること、ハッシュ値が生成されること
	 */
	@Test
	public void Test001() throws Throwable {
		// ハッシュ計算実施
		MessageDigest digest = testTarget.getMessageDigest();
		digest.update("test".getBytes());
		Name hash = testTarget.toHashPath(new String[] { "col1" }, new KagerowDataType[] { KagerowDataType.VARCHAR });
		// 検証
		assertThat(hash.toString(), is("5550cb8ecb011aa6d5d08724b9886637/098f6bcd4621d373cade4e832627b4f6"));
	}

	/**
	 * [試験観点] : 空のリスト
	 * [期待される結果] : 正常終了すること、ハッシュ値が生成されること
	 */
	@Test
	public void Test002() throws Throwable {
		// ハッシュ計算実施
		MessageDigest digest = testTarget.getMessageDigest();
		digest.update("test".getBytes());
		Name hash = testTarget.toHashPath(new String[] {}, new KagerowDataType[] {});
		// 検証
		assertThat(hash.toString(), is("d41d8cd98f00b204e9800998ecf8427e/098f6bcd4621d373cade4e832627b4f6"));
	}

	/**
	 * [試験観点] : 型情報にnullを含むのリスト
	 * [期待される結果] : 例外が発生すること、メッセージが期待通りである
	 */
	@Test
	public void Test003() throws Throwable {
		// ハッシュ計算実施
		MessageDigest digest = testTarget.getMessageDigest();
		digest.update("test".getBytes());
		try {
			testTarget.toHashPath(new String[] { "col1", "col2" },
					new KagerowDataType[] { KagerowDataType.VARCHAR, null });
			fail();
		} catch (NullPointerException e) {
			assertThat(e.getMessage(), is(ErrorMessage.CODE_002.getMessage()));
		}
	}

	/**
	 * [試験観点] : ヘッダー情報にnullを含むのリスト
	 * [期待される結果] : 例外が発生すること、メッセージが期待通りである
	 */
	@Test
	public void Test004() throws Throwable {
		// ハッシュ計算実施
		MessageDigest digest = testTarget.getMessageDigest();
		digest.update("test".getBytes());
		try {
			testTarget.toHashPath(new String[] { "col1", null },
					new KagerowDataType[] { KagerowDataType.VARCHAR, KagerowDataType.VARCHAR });
			fail();
		} catch (NullPointerException e) {
			assertThat(e.getMessage(), is(ErrorMessage.CODE_001.getMessage()));
		}

	}

	/**
	 * [試験観点] : nullをコンストラクタで指定
	 * [期待される結果] : 例外が発生すること、メッセージが期待通りである
	 */
	@Test
	public void Test005() throws Throwable {
		try {
			new HashPathCreater(null);
			fail();
		} catch (NullPointerException e) {
			assertThat(e.getMessage(), is(ErrorMessage.CODE_003.getMessage()));
		}

	}

	/**
	 * [試験観点] : 不正なアルゴリズムをコンストラクタで指定
	 * [期待される結果] : 例外が発生すること、メッセージが期待通りである
	 */
	@Test
	public void Test006() throws Throwable {
		try {
			new HashPathCreater("bug");
			fail();
		} catch (NoSuchAlgorithmException e) {
			;
		}

	}

	/**
	 * [試験観点] : URI変換
	 * [期待される結果] : 正常終了すること、URIが生成されること
	 */
	@Test
	public void Test007() throws Throwable {
		// ハッシュ計算実施
		MessageDigest digest = testTarget.getMessageDigest();
		digest.update("test".getBytes());
		Name hash = testTarget.toHashPath(new String[] {}, new KagerowDataType[] {});
		// URI変換
		URINameParser parser = new URINameParser("a");
		URI uri = parser.toURI(hash);
		// 検証
		assertThat(uri.toString(),
				is("kagerow://application/d41d8cd98f00b204e9800998ecf8427e/098f6bcd4621d373cade4e832627b4f6#a.zip"));
	}

}

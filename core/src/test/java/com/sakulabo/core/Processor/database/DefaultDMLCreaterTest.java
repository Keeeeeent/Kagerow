package com.sakulabo.core.Processor.database;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import java.util.Collections;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sakulabo.BaseTest;
import com.sakulabo.core.Kagerow.Contents.KagerowVirtualFileContent.KagerowDataType;
import com.sakulabo.core.Processor.archive.DataSet;
import com.sakulabo.core.Processor.database.impl.DefaultDMLCreater;

/**
 * DML生成テストです
 */
@SuppressWarnings("javadoc")
public class DefaultDMLCreaterTest extends BaseTest<DefaultDMLCreater> {

	/** テスト対象 */
	private DefaultDMLCreater testTarget;

	/**
	 * デフォルトコンストラクタ
	 */
	protected DefaultDMLCreaterTest() {
		super(DefaultDMLCreaterTest.class);
	}

	@BeforeEach
	void initService() {
	}

	@AfterEach
	void closeService() throws Exception {
	}

	/**
	 * [試験観点]      : チャンク読み取り
	 * [期待される結果] : 正常終了すること
	 */
	@Test
	public void Test001() throws Throwable {

		// 期待値
		String exp = """
				INSERT INTO test."KDB_TEST"(\
				col1,\
				col2,\
				col3\
				) VALUES (\
				?,?,?\
				)\
				;""";

		// テストデータ用意
		String schema = "test";
		String table = "test";
		String synonym = null;
		String[] columnList = { "col1", "col2", "col3" };
		KagerowDataType[] dataType = { KagerowDataType.VARCHAR, KagerowDataType.VARCHAR, KagerowDataType.VARCHAR };
		long[] dataSize = { 256L, 256L, 256L };
		DataSet dataSet = new DataSet(
				schema,
				table,
				synonym,
				columnList,
				dataType,
				dataSize,
				Collections.emptyList());

		// テスト対象初期化
		testTarget = new DefaultDMLCreater(dataSet);
		// テスト実行
		String result = testTarget.toSql();
		// 検証
		assertThat(result, is(exp));

	}
}

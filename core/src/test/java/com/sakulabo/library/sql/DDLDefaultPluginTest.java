package com.sakulabo.library.sql;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.mockito.Mockito.*;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sakulabo.BaseTest;
import com.sakulabo.BaseTest.KagerowContainerRunner;
import com.sakulabo.core.Kagerow.Utilities.KagerowChunkCreater.ChunkCreateMode;
import com.sakulabo.core.Kagerow.Utilities.KagerowDBMode;
import com.sakulabo.core.Kagerow.Utilities.KagerowVirtualFileCreater;

/**
 * DDL実行プラグインのテストクラスです
 */
@ExtendWith(KagerowContainerRunner.class)
public class DDLDefaultPluginTest extends BaseTest<DDLDefaultPlugin> {

	/** テスト対象 */
	@InjectMocks
	protected DDLDefaultPlugin testTarget;

	@Mock
	private Connection connection;
	@Mock
	private Statement statement;

	@BeforeEach
	void initService() {
		closeable = MockitoAnnotations.openMocks(this);
	}

	@AfterEach
	void closeService() throws Exception {
		closeable.close();
	}

	/**
	 * [試験観点] : 通常DDL
	 * [期待される結果] : 以下である
	 * ・正常終了すること
	 * ・SQLの内容が正しく読み込みていること
	 * ・DDLが正しく生成されていること
	 */
	@Test
	public void Test001() throws Throwable {

		// 前提準備
		// テーブル作成
		KagerowVirtualFileCreater.constructionKDB(
				ChunkCreateMode.CSV,
				"test",
				getInputPath("test1.csv"),
				StandardCharsets.UTF_8,
				false,
				"Test002",
				false);

		// 引数のキャプチャ
		ArgumentCaptor<String> argCaptor_statement = ArgumentCaptor.forClass(String.class);
		// モック準備
		doReturn(statement).when(connection).createStatement();
		doReturn(true).when(statement).execute(argCaptor_statement.capture());
		doReturn("test").when(connection).getSchema();

		// 引数準備
		Map<String, String> params = new HashMap<>() {
			{
				put("DDL", "create view test as select * from ${Test002[0]}");
			}
		};
		KagerowDBMode mode = KagerowDBMode.ORACLE;

		// テスト実行
		testTarget.input(params, mode, connection);
		// 結果検証
		assertThat(argCaptor_statement.getAllValues().get(0), is("""
				create view test as select * from \
				test."KDB_FF159C696319867925B1963EF74AFDDF#974696A8F5E7602E0B9EC126EC0B0C4D"\
				"""));

	}

}

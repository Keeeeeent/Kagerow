package com.sakulabo.library.xml;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetMetaDataImpl;
import javax.sql.rowset.RowSetProvider;
import javax.sql.rowset.WebRowSet;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sakulabo.BaseTest;
import com.sakulabo.core.Kagerow.Utilities.KagerowDBMode;
import com.sakulabo.regulation.spi.PluginAdapter.KagerowRowSet;

/**
 * XML出力プラグインのテストクラスです
 */
public class XMLDefaultPluginTest extends BaseTest<XMLDefaultPlugin> {

	/** テスト対象 */
	@InjectMocks
	protected XMLDefaultPlugin testTarget;

	@Mock
	private Connection connection;
	@Mock
	private Statement statement;
	@Mock
	private PreparedStatement preparedStatement;
	@Mock
	private CachedRowSet cachedRowSet;
	@Mock
	private ResultSetMetaData metaData;

	/**
	 * デフォルトコンストラクタ
	 */
	protected XMLDefaultPluginTest() {
		super(XMLDefaultPluginTest.class);
	}

	@BeforeEach
	void initService() {
		closeable = MockitoAnnotations.openMocks(this);
	}

	@AfterEach
	void closeService() throws Exception {
		closeable.close();
	}

	/**
	 * [試験観点] : 通常出力
	 * [期待される結果] : 以下である
	 * ・正常終了すること
	 * ・ファイルの内容が正しく読み込みていること
	 * ・XMLが正しく生成されていること
	 */
	@Test
	public void Test001() throws Throwable {

		// 出力先パス
		Path resultFile = getOutputPath("Test001.xml");

		try {

			// 引数準備
			CachedRowSet cachedRowSet = RowSetProvider.newFactory().createCachedRowSet();

			// メタデータ定義
			RowSetMetaDataImpl meta = new RowSetMetaDataImpl();
			meta.setColumnCount(2);
			meta.setColumnName(1, "id");
			meta.setColumnType(1, java.sql.Types.INTEGER);
			meta.setColumnName(2, "name");
			meta.setColumnType(2, java.sql.Types.VARCHAR);

			cachedRowSet.setMetaData(meta);

			// データ投入
			cachedRowSet.moveToInsertRow();
			cachedRowSet.updateInt(1, 1);
			cachedRowSet.updateString(2, "Alice");
			cachedRowSet.insertRow();
			cachedRowSet.moveToCurrentRow();

			KagerowRowSet targetData = new KagerowRowSet(KagerowDBMode.ORACLE.toString(),
					"test",
					"testName",
					cachedRowSet);

			// データ準備
			Map<String, String> params = new HashMap<>();
			params.put("OutputPath", resultFile.toString());
			params.put("KsqlId", "test");
			params.put("Charset", StandardCharsets.UTF_8.name());

			// テスト実行
			testTarget.output(params, List.of(targetData));

			// 結果検証
			WebRowSet webRowSet = RowSetProvider.newFactory().createWebRowSet();
			webRowSet.readXml(Files.newInputStream(resultFile));
			webRowSet.next();
			int id = webRowSet.getInt(1);
			String name = webRowSet.getString(2);
			assertThat(id, is(1));
			assertThat(name, is("Alice"));

		} finally {
			Files.delete(resultFile);
		}

	}

}

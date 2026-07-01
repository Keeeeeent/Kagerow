package com.sakulabo.library.sql;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sakulabo.core.Kagerow.Contents.KagerowVirtualFileContent.KagerowVirtualFileObject.BasicFileObject;
import com.sakulabo.core.Kagerow.Context.KagerowVirtualFileContext;
import com.sakulabo.core.Kagerow.Utilities.KagerowChunkCreater;
import com.sakulabo.core.Kagerow.Utilities.KagerowChunkCreater.ChunkCreateMode;
import com.sakulabo.core.Kagerow.Utilities.KagerowChunkLorder;
import com.sakulabo.core.Kagerow.Utilities.KagerowDBMode;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;
import com.sakulabo.core.Kagerow.Utilities.KagerowUtilities;
import com.sakulabo.library.common.DefaultPlugin;
import com.sakulabo.library.common.DefaultPluginMessage;
import com.sakulabo.library.common.DefaultPluginValidationException;
import com.sakulabo.library.common.FileDefaultOutputer;
import com.sakulabo.regulation.annotation.KagerowPlugin;
import com.sakulabo.regulation.annotation.KagerowPlugin.PluginType;
import com.sakulabo.regulation.spi.PluginAdapter;

/**
 * プラグインデフォルトJDBC接続プラグインクラス
 * @author keeeeeent
 */
@KagerowPlugin(name = "KagerowJDBCPlugin", types = { PluginType.INPUT }, multiSize = 10)
public class JDBCDefaultPlugin extends DefaultPlugin implements PluginAdapter {

	/** パラメータ名称（JDBCパス） */
	private static final String JDBC = "jdbc";
	/** パラメータ名称（分離レベル） */
	private static final String LEVEL = "level";
	/** パラメータ名称（実行SQL） */
	private static final String SQL = "sql";
	/** パラメータ名称（接続先） */
	private static final String URL = "url";
	/** パラメータ名称（接続ユーザ） */
	private static final String USER = "user";
	/** パラメータ名称（接続パスワード） */
	private static final String PASS = "password";
	/** パラメータ名称（生成テーブル） */
	private static final String TABLE = "table";
	/** パラメータ名称（ドライバークラス名） */
	private static final String CLASS = "driver";
	/** 日付フォーマッター */
	private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("YYYY-MM-dd");

	/** KDB物理ファイルパス取得正規表現 */
	private static final Pattern KDB_FILE_PATTERN = Pattern.compile("(?<=file:)(?<file>[^:]+)(?=:?)");

	/** DDLフォーマッター */
	private static final MessageFormat DDL_FORMAT = new MessageFormat("DROP TABLE IF EXISTS {0};");

	/** {@inheritDoc} */
	@Override
	@Param(value = JDBC, required = true)
	@Param(value = LEVEL, required = true)
	@Param(value = SQL, required = true)
	@Param(value = URL, required = true)
	@Param(value = USER)
	@Param(value = PASS)
	@Param(value = CLASS, required = true)
	@Param(value = TABLE)
	public void input(Map<String, String> params, KagerowDBMode mode, Connection connection) {

		// コネクション取得
		try (Connection conn = getConnection(params)) {

			try (Statement statement = conn.createStatement()) {

				// トランザクションレベル設定
				int level = getTransactionLevel(params);
				conn.setTransactionIsolation(level);

				// SQL取得
				String sql = params.get(SQL);

				// SQL実行
				boolean type = statement.execute(sql);
				if (type) {
					// 取得系の場合、テーブル生成
					String tableName = createTabelName(params);
					// 実行結果取得
					ResultSet result = statement.getResultSet();
					// データ書き出し
					Path exportPath = exportData(result);
					// データ取り込み
					importData(exportPath, tableName, connection);
				} else {
					// DMLの場合コミット
					conn.commit();
				}

			} catch (Exception e) {
				// 例外が発生した場合ロールバック
				conn.rollback();
				// ログ書き込み
				KagerowLogger.newAppLogger().err(e);
			}
		} catch (Exception e) {
			KagerowLogger.newAppLogger().err(e);
		}

	}

	/** {@inheritDoc} */
	@Override
	@Deprecated
	public void output(Map<String, String> params, List<KagerowRowSet> data) {
		;
	}

	/** {@inheritDoc} */
	@Override
	public void validation(Map<String, String> params, PluginType type) throws PluginValidationException {
		// JDBC存在確認
		validPath(params, JDBC);
		// Driver生成可否確認
		getDriver(params);
		// コネクション生成可否
		try (Connection _ = getConnection(params)) {
			;
		} catch (SQLException e) {
			KagerowLogger.newAppLogger().err(e);
		}
	}

	/**
	 * データオブジェクトを生成し、現在のセッションにデータを投入します
	 * @param exportPath データセットファイル
	 * @param tableName 生成テーブル名称
	 * @param conn コネクション
	 * @throws Exception データインポート失敗
	 */
	private void importData(Path exportPath, String tableName, Connection conn) throws Exception {

		// 生成データ格納変数
		BasicFileObject data = null;
		try (Statement statement = conn.createStatement()) {

			// 元データが残っている可能性があるため、テーブルをDROPする
			String ddl = DDL_FORMAT.format(new Object[] { tableName });
			statement.execute(ddl);
			conn.commit();

			// データオブジェクトクリエイター生成
			KagerowChunkCreater<BasicFileObject> creater = getCreater(exportPath);
			// データオブジェクト生成処理実行
			data = create(creater, tableName);
			// KDBURL取得
			String kdbPath = conn.getMetaData().getURL();
			// KDB物理ファイルパス取得
			Path file = getKDBFilePath(kdbPath);
			// KDB論理実行モードを取得
			KagerowDBMode mode = getKDBMode(conn);
			// データオブジェクトローダー生成
			KagerowChunkLorder<BasicFileObject> lorder = KagerowChunkLorder.newBasicChunkLorder(mode, file);
			// データロード
			lorder.lord(data);

		} finally {

			// 生成済みのデータをロールバック
			if (Objects.nonNull(data)) {
				// データファイル削除
				deleteTmpFile(data.datAddr());
				// インデックスファイル削除
				deleteTmpFile(data.idxAddr());
			}

		}

	}

	/**
	 * データオブジェクトクリエイターを生成します
	 * @param exportPath 出力先
	 * @return クリエイター
	 * @throws Exception クリエイター生成失敗
	 */
	private KagerowChunkCreater<BasicFileObject> getCreater(Path exportPath) throws Exception {
		// データオブジェクトクリエイター生成
		KagerowChunkCreater<BasicFileObject> creater = KagerowChunkCreater.newBasicInstance(
				ChunkCreateMode.CSV,
				KagerowVirtualFileContext.SYSTEM_SCHEMA,
				exportPath,
				StandardCharsets.UTF_8,
				true);
		return creater;
	}

	/**
	 * URIに対応するファイルを削除します
	 * @param uri 削除対象
	 * @return 削除結果
	 * @throws IOException 削除失敗
	 */
	private boolean deleteTmpFile(String uri) throws IOException {
		Path paht = Paths.get(URI.create(uri));
		return Files.deleteIfExists(paht);
	}

	/**
	 * URIからKDB物理ファイルパスを生成します
	 * @param kdbPath kdbURI
	 * @return 物理ファイルパス
	 */
	private Path getKDBFilePath(String kdbPath) {
		Path file = null;
		Matcher fileMatcher = KDB_FILE_PATTERN.matcher(kdbPath);
		if (fileMatcher.find()) {
			file = Paths.get(fileMatcher.group("file").concat(".mv.db"));
		}
		return file;
	}

	/**
	 * コネクションからKDBモードを生成します
	 * @param conn コネクション
	 * @return KDBモード
	 * @throws SQLException モード取得失敗
	 */
	private KagerowDBMode getKDBMode(Connection conn) throws SQLException {
		KagerowDBMode mode = null;
		try (Statement st = conn.createStatement();
				ResultSet rs = st.executeQuery(
						"SELECT SETTING_VALUE FROM INFORMATION_SCHEMA.SETTINGS WHERE SETTING_NAME='MODE'")) {
			mode = rs.next() ? KagerowDBMode.toMode(rs.getString(1)) : null;
		}
		return mode;
	}

	/**
	 * テーブル名称をカスタマイズしたデータオブジェトクを生成します
	 * @param creater データオブジェクトクリエイター
	 * @param tabelName テーブル名
	 * @return 生成済みのデータオブジェクト
	 * @throws Exception データ生成失敗
	 */
	private BasicFileObject create(KagerowChunkCreater<BasicFileObject> creater, String tabelName) throws Exception {
		// 生成データ格納変数
		BasicFileObject tmp = creater.create(null);
		// データ変換
		BasicFileObject data = new BasicFileObject(
				tmp.createTime(),
				tmp.headerData(),
				tmp.dataType(),
				tmp.dataSize(),
				tmp.datAddr(),
				tmp.datSize(),
				tmp.idxAddr(),
				tmp.idxSize(),
				tabelName,
				tmp.synonym(),
				tmp.schema(),
				tmp.uri());
		return data;
	}

	/**
	 * ResultSetをファイルとして書き出します
	 * @param result SQL実行結果
	 * @return 出力先
	 * @throws IOException ファイル出力失敗
	 * @throws SQLException 結果取得エラー
	 */
	private Path exportData(ResultSet result) throws IOException, SQLException {

		// 結合文字
		final char delimiter = ',';
		// 文字列連結インスタンス
		StringJoiner stringJoiner = new StringJoiner(new String(new char[] { delimiter }));

		// 生成する一時ファイルパスを生成
		UUID id = UUID.randomUUID();
		Path tmpPath = KagerowUtilities.createTemporaryPath(id.toString() + ".tmp");

		try (BufferedWriter writer = Files.newBufferedWriter(tmpPath, StandardCharsets.UTF_8)) {

			// メタデータ取得
			ResultSetMetaData metaData = result.getMetaData();
			// カラム数取得
			int colNum = metaData.getColumnCount();

			// ヘッダーデータ書き出し
			for (int i = 1; i <= colNum; i++) {
				String colName = metaData.getColumnLabel(i);
				String wrapStr = FileDefaultOutputer.wrapString(colName);
				stringJoiner.add(wrapStr);
			}

			// ファイル出力
			writer.append(stringJoiner.toString());
			writer.newLine();
			stringJoiner = new StringJoiner(new String(new char[] { delimiter }));

			// ボディーデータ書き出し
			while (result.next()) {
				for (int i = 1; i <= colNum; i++) {
					// 値抽出
					Object object = result.getObject(i);
					// 文字列に変換
					String wrapStr = FileDefaultOutputer.parseRowSet(object, dateFormatter, true);
					// 出力形式に整形
					stringJoiner.add(wrapStr);
				}
				// ファイル出力
				writer.append(stringJoiner.toString());
				writer.newLine();
				stringJoiner = new StringJoiner(new String(new char[] { delimiter }));
			}

		}

		// 書き込み先を返却
		return tmpPath;
	}

	/**
	 * テーブル名称を生成します
	 * @param params パラメータ
	 * @return 生成されたテーブル名称
	 */
	private String createTabelName(Map<String, String> params) {
		// テーブル名が指定されているか確認
		String name = params.get(TABLE);
		if (Objects.nonNull(name)) {
			return name;
		}
		// 未指定の場合、自動生成のテーブル名を返却
		String id = getId(params);
		return "tid_".concat(id);
	}

	/**
	 * トランザクションレベルを生成します
	 * @param params パラメータ
	 * @return 生成されたトランザクションレベル
	 */
	private int getTransactionLevel(Map<String, String> params) {

		// パラメータ取得
		String level = params.get(LEVEL);

		// 取得出来ない場合、デフォルトを返却
		if (Objects.isNull(level)) {
			return Connection.TRANSACTION_READ_COMMITTED;
		}

		// 取得できた場合、大文字表現にしてからモードの確認
		level = level.toUpperCase();
		return switch (level) {
		case "NONE" -> Connection.TRANSACTION_NONE;
		case "READ_COMMITTED" -> Connection.TRANSACTION_READ_COMMITTED;
		case "READ_UNCOMMITTED" -> Connection.TRANSACTION_READ_UNCOMMITTED;
		case "REPEATABLE_READ" -> Connection.TRANSACTION_REPEATABLE_READ;
		case "SERIALIZABLE" -> Connection.TRANSACTION_SERIALIZABLE;
		default -> Connection.TRANSACTION_READ_COMMITTED;
		};

	}

	/**
	 * パラメータからドライバーを生成します
	 * @param params パラメータ
	 * @return JDBCドライバー
	 * @throws PluginValidationException ドライバー生成失敗
	 */
	private Driver getDriver(Map<String, String> params) throws PluginValidationException {

		// パラメータ取得
		Path jdbc = getPath(params, JDBC);
		String driver = params.get(CLASS);

		// URL格納先リスト生成
		List<java.net.URL> urlList = new ArrayList<>();

		try {

			if (Files.isDirectory(jdbc)) {
				// パスがディレクトリの場合、探索
				Files.newDirectoryStream(jdbc)
						.forEach(p -> {
							try {
								urlList.add(p.toUri().toURL());
							} catch (MalformedURLException e) {
								KagerowLogger.newAppLogger().err(e);
							}
						});
			} else {
				// パスがファイルの場合、そのまま追加
				urlList.add(jdbc.toUri().toURL());
			}

			// 指定されたURLリストでクラスローダー生成
			URLClassLoader loader = new URLClassLoader(
					urlList.toArray(java.net.URL[]::new),
					ClassLoader.getSystemClassLoader());

			// クラス情報取得
			Class<?> clazz = Class.forName(driver, true, loader);

			// ドライバー生成
			Driver result = (Driver) clazz.getDeclaredConstructor().newInstance();
			return result;

		} catch (IOException e) {
			// JDBC読み取りに失敗した場合
			throw new DefaultPluginValidationException(
					DefaultPluginMessage.E0006,
					new Object[] { jdbc.toAbsolutePath().normalize().toString() }, e);
		} catch (ClassNotFoundException e) {
			// ドライバークラス情報が存在しない場合
			throw new DefaultPluginValidationException(
					DefaultPluginMessage.E0006,
					new Object[] { driver }, e);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | NoSuchMethodException e) {
			// ドライバー生成に失敗した場合
			throw new DefaultPluginValidationException(
					DefaultPluginMessage.E0008,
					new Object[] { driver }, e);
		}

	}

	/**
	 * パラメータからコネクションを生成します
	 * @param params パラメータ
	 * @return 生成されたコネクション
	 * @throws PluginValidationException コネクション生成失敗
	 */
	private Connection getConnection(Map<String, String> params) throws PluginValidationException {

		// パラメータ取得
		String url = params.get(URL);
		String user = Objects.toString(params.get(USER), "");
		String pass = Objects.toString(params.get(PASS), "");

		// JDBC向け設定生成
		Properties prop = new Properties();
		prop.setProperty("user", user);
		prop.setProperty("password", pass);

		// ドライバー生成
		Driver driver = getDriver(params);

		// コネクション生成
		Connection conn;
		try {
			// コネクション生成
			conn = driver.connect(url, prop);
			// オートコミットOFF
			conn.setAutoCommit(false);
			return conn;
		} catch (SQLException e) {
			throw new DefaultPluginValidationException(
					DefaultPluginMessage.E0009,
					new Object[] { driver }, e);
		}

	}

}

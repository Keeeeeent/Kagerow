package com.sakulabo.library.common;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.StringJoiner;

import com.sakulabo.core.Kagerow.Utilities.KagerowDBMode;
import com.sakulabo.core.Kagerow.Utilities.KagerowFileBodyReader;
import com.sakulabo.core.Kagerow.Utilities.KagerowFileHeaderReader;
import com.sakulabo.core.Kagerow.Utilities.KagerowFileReaderFactory;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;

/**
 * プラグインデフォルトファイル丹生録機能提供クラス
 * 
 * @author keeeeeent
 */
public final class FileDefaultInputer {

	/** データインプットインスタンス生成 */
	private final KagerowFileReaderFactory factory;
	/** 入力データパス */
	private final Path inputPath;
	/** ヘッダー有無 */
	private final Boolean isHeader;
	/** ユーザ指定初期化DDL */
	private final String initDDL;
	/** 一時テーブル名称 */
	private final String tableName;
	/** KDB構築モード */
	private final KagerowDBMode mode;
	/** 文字コード */
	private final Charset charset;

	/**
	 * デフォルトコンストラクタ
	 * @param factory データインプットインスタンス生成ファクトリクラス
	 * @param inputPath 入力データパス
	 * @param isHeader ヘッダー有無
	 * @param initDDL ユーザ指定初期化DDL
	 * @param tableName 一時テーブル名称
	 * @param mode KDB構築モード
	 * @param charset 文字コード
	 */
	public FileDefaultInputer(
			KagerowFileReaderFactory factory,
			Path inputPath,
			Boolean isHeader,
			String initDDL,
			String tableName,
			KagerowDBMode mode,
			Charset charset) {
		this.factory = factory;
		this.inputPath = inputPath;
		this.isHeader = isHeader;
		this.initDDL = initDDL;
		this.tableName = tableName;
		this.mode = mode;
		this.charset = charset;
	}

	/**
	 * データ読み込みを行います
	 * @param connection コネクション
	 */
	public void input(Connection connection) {

		try {

			// ファイル読み込みインスタンス生成
			KagerowFileBodyReader bodyReader = factory.createFileBodyReader(inputPath, charset, isHeader);
			KagerowFileHeaderReader headerReader = factory.createFileHeaderReader(inputPath, charset, isHeader);

			// ベースDML
			StringJoiner dmlSqlval = new StringJoiner(",");

			// ベースDDL
			StringJoiner ddlSqlval = new StringJoiner(" , ", "", " ");
			StringJoiner baseSql = createDDL();

			// DDL型情報選択
			String ddlformat = createDDLFormat();

			// カラム設定(DDL)
			for (String col : headerReader.readLine()) {
				ddlSqlval.add(String.format(ddlformat, col));
				dmlSqlval.add("?");
			}

			// 既存テーブルが存在する場合、dropする
			String dropSql = dropTableDDL();

			// データベース構築開始
			connection.setAutoCommit(true);
			// ステートメント生成
			Statement statement = connection.createStatement();
			// まずは既存テーブルの存在確認と、削除を実施
			statement.execute(dropSql);
			// 続けてデータ登録先となるテーブルを生成
			baseSql.add(ddlSqlval.toString());
			statement.execute(baseSql.toString());
			// ユーザー指定のDDLを実行
			statement.execute(initDDL);

			// ステートメント生成
			String dmlSql = createDML(dmlSqlval);
			PreparedStatement preparedStatement = connection.prepareStatement(dmlSql);
			String[] line;
			while ((line = bodyReader.readLine()) != null) {
				// パラメータ設定
				for (int i = 1; i <= line.length; i++) {
					preparedStatement.setString(i, line[i - 1]);
				}
				// DML実行
				preparedStatement.executeUpdate();
			}

		} catch (Exception e) {
			// TODO できればプラグイン専用例外にて伝播させたい
			// IOエラーの場合、ログを出力し正常終了
			KagerowLogger.newAppLogger().err(e);
		}
	}

	/**
	 * モードに対応したDDLを生成します
	 * @return テーブル生成DDL
	 */
	private String createDDLFormat() {
		return switch (this.mode) {
		case ORACLE -> "CLOB %s";
		case H2, MYSQL, POSTGRESQL -> "TEXT %s";
		default -> throw new IllegalArgumentException("Unexpected value: " + this.mode);
		};
	}

	/**
	 * テーブルをDROPするDDLを生成します
	 * @return DROP文
	 */
	private String dropTableDDL() {
		return "DROP TABLE IF EXISTS " + tableName + ";";
	}

	/**
	 * ベースとなるDMLを生成します
	 * @return DML生成用StringJoiner
	 */
	private StringJoiner createDDL() {
		StringJoiner baseSql = new StringJoiner(" ", "", ");");
		baseSql.add("CREATE TABLE");
		baseSql.add(tableName);
		baseSql.add("(");
		return baseSql;
	}

	/**
	 * ベースとなるDDLを生成します
	 * @param dmlSqlval VALUES句生成StringJoiner
	 * @return DDL文字列
	 */
	private String createDML(StringJoiner dmlSqlval) {

		// DML作成
		StringJoiner dmlSql = new StringJoiner(" ");
		dmlSql.add("INSERT INTO");
		dmlSql.add(tableName);
		dmlSql.add("VALUES (");

		// VALUE句設定(DML)
		dmlSql.add(dmlSqlval.toString());
		dmlSql.add(");");

		return dmlSql.toString();
	}

}

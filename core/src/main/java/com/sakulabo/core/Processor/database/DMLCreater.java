package com.sakulabo.core.Processor.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sakulabo.core.Common.StringUtils;
import com.sakulabo.core.Kagerow.KagerowApplication;
import com.sakulabo.core.Processor.archive.DataSet;
import com.sakulabo.core.Processor.database.impl.DefaultDMLCreater;

/**
 * KDB専用DML生成クラスです
 * 
 * @author keeeeeent
 */
public sealed abstract class DMLCreater extends AppKSQLCreater
		permits DefaultDMLCreater {

	/** DML文フォーマット */
	private static final String DML_FROMAT = "INSERT INTO {0}.{1}({2}) VALUES ({3});";

	/** 日付フォーマット */
	protected static final List<DateTimeFormatter> DATE_FORMAT = KagerowApplication
			.getConfig()
			.KDB_DATE_FORMAT();

	/**
	 * 日付変換フォーマッター
	 * @param localDate 変換対象
	 * @return 変換結果
	 */
	protected static final LocalDate toLocalDate(String localDate) {
		LocalDate date = null;
		for (DateTimeFormatter format : DATE_FORMAT) {
			try {
				date = LocalDate.parse(localDate, format);
			} catch (Exception e) {
				;
			}
		}
		return date;
	}

	/**
	 * 日時変換フォーマッター
	 * @param localDateTime 変換対象
	 * @return 変換結果
	 */
	protected static final LocalDateTime toLocalDateTime(String localDateTime) {
		LocalDateTime timestamp = null;
		for (DateTimeFormatter format : DATE_FORMAT) {
			try {
				timestamp = LocalDateTime.parse(localDateTime, format);
			} catch (Exception e) {
				;
			}
		}
		return timestamp;
	}

	/**
	 * デフォルトコンストラクター
	 * @param dataSet データ構造体
	 */
	public DMLCreater(DataSet dataSet) {
		super(dataSet);
	}

	/** {@inheritDoc} */
	@Override
	public String toSql() {

		// DML作成用インスタンス初期化(カラム名)
		StringJoiner colNameList = new StringJoiner(StringUtils.COMMA_STR);
		// DML作成用インスタンス初期化(バリュー)
		String colValueList = StringUtils.EMPTY;
		// デーブル名称取得
		String tableName = AppKSQLCreater.toTabelName(dataSet.table());
		// カラム名称取得
		String[] colNames = dataSet.columnList();
		Stream.of(colNames).forEach(colNameList::add);

		// バリュー生成
		colValueList = Stream.generate(StringUtils.EMBEDDED_EXCLAMATION::toString)
				.limit(colNames.length)
				.collect(Collectors.joining(StringUtils.COMMA_STR));

		// DML生成
		String dml = MessageFormat.format(DML_FROMAT,
				dataSet.schema(),
				tableName,
				colNameList.toString(),
				colValueList);
		return dml;
	}

	/** {@inheritDoc} */
	@Override
	public void init(Connection connection) throws SQLException {
		;
	}

	/**
	 * nullチェックを行います
	 * @param target 判定対象
	 * @return 判定結果
	 */
	protected final boolean nullCheck(Object target) {
		return Objects.isNull(target) || Objects.equals(target, StringUtils.EMPTY);
	}

	/**
	 * DataTypeがCLOBか判定します
	 * @param size 判定サイズ
	 * @return 判定結果
	 */
	public abstract boolean isCLOB(long size);

	/**
	 * DataTypeがNULLの場合のDDLを生成します
	 * @param statement SQLバインドインスタンス
	 * @param data バインドデータ
	 * @param index インデックス番号
	 * @throws SQLException KDBクエリエラー
	 */
	public abstract void isNULL(PreparedStatement statement, String data, int index) throws SQLException;

	/**
	 * DataTypeがBOOLEANの場合のDDLを生成します
	 * @param statement SQLバインドインスタンス
	 * @param data バインドデータ
	 * @param index インデックス番号
	 * @throws SQLException KDBクエリエラー
	 */
	public abstract void isBOOLEAN(PreparedStatement statement, String data, int index) throws SQLException;

	/**
	 * DataTypeがNUMBERの場合のDDLを生成します
	 * @param statement SQLバインドインスタンス
	 * @param data バインドデータ
	 * @param index インデックス番号
	 * @throws SQLException KDBクエリエラー
	 */
	public abstract void isNUMBER(PreparedStatement statement, String data, int index) throws SQLException;

	/**
	 * DataTypeがDECIMALの場合のDDLを生成します
	 * @param statement SQLバインドインスタンス
	 * @param data バインドデータ
	 * @param index インデックス番号
	 * @throws SQLException KDBクエリエラー
	 * */
	public abstract void isDECIMAL(PreparedStatement statement, String data, int index) throws SQLException;

	/**
	 * DataTypeがDATEの場合のDDLを生成します
	 * @param statement SQLバインドインスタンス
	 * @param data バインドデータ
	 * @param index インデックス番号
	 * @throws SQLException KDBクエリエラー
	 * */
	public abstract void isDATE(PreparedStatement statement, String data, int index) throws SQLException;

	/**
	 * DataTypeがTIMESTAMPの場合のDDLを生成します
	 * @param statement SQLバインドインスタンス
	 * @param data バインドデータ
	 * @param index インデックス番号
	 * @throws SQLException KDBクエリエラー
	 * */
	public abstract void isTIMESTAMP(PreparedStatement statement, String data, int index) throws SQLException;

	/**
	 * DataTypeがVARCHARの場合のDDLを生成します
	 * @param statement SQLバインドインスタンス
	 * @param data バインドデータ
	 * @param index インデックス番号
	 * @throws SQLException KDBクエリエラー
	 * */
	public abstract void isVARCHAR(PreparedStatement statement, String data, int index) throws SQLException;

}

package com.sakulabo.core.Processor.database;

import java.text.MessageFormat;
import java.util.StringJoiner;

import com.sakulabo.core.Common.StringUtils;
import com.sakulabo.core.Kagerow.Contents.KagerowVirtualFileContent.KagerowDataType;
import com.sakulabo.core.Processor.archive.DataSet;
import com.sakulabo.core.Processor.database.impl.H2DDLCreater;
import com.sakulabo.core.Processor.database.impl.MySQLDDLCreater;
import com.sakulabo.core.Processor.database.impl.OracleDDLCreater;
import com.sakulabo.core.Processor.database.impl.PostgreSQLDDLCreater;

/**
 * KDB専用DDL生成クラスです
 * 
 * @author keeeeeent
 */
public sealed abstract class DDLCreater extends AppKSQLCreater
		permits OracleDDLCreater, H2DDLCreater, PostgreSQLDDLCreater, MySQLDDLCreater {

	/** DDL文フォーマット */
	private static final String DDL_FROMAT = "CREATE TABLE IF NOT EXISTS {0}.{1} ({2});";
	/** 整形済みDDL文フォーマット */
	private static final String FORMATED_DDL_FROMAT = """
			CREATE TABLE IF NOT EXISTS {0}.{1} (
			{2}
			);""";
	/** カーソル */
	protected int index;

	/**
	 * デフォルトコンストラクター
	 * @param dataSet データ構造体
	 */
	public DDLCreater(DataSet dataSet) {
		super(dataSet);
	}

	/** {@inheritDoc} */
	@Override
	public String toSql() {

		// DDL作成用インスタンス初期化
		StringJoiner joiner = new StringJoiner(StringUtils.COMMA_STR);
		// デーブル名称取得
		String tableName = AppKSQLCreater.toTabelName(dataSet.table());

		// カラムリスト取得
		KagerowDataType[] columnList = dataSet.dataType();

		// 主キー設定
		joiner.add(isPK());

		// DDL生成
		for (KagerowDataType type : columnList) {
			String ddl = switch (type) {
			case BOOLEAN -> isBOOLEAN();
			case DATE -> isDATE();
			case DECIMAL -> isDECIMAL();
			case NULL -> isNULL();
			case NUMBER -> isNUMBER();
			case VARCHAR -> isVARCHAR();
			case TIMESTAMP -> isTIMESTAMP();
			};
			joiner.add(ddl);
		}

		// DDL作成
		String ddl = MessageFormat.format(DDL_FROMAT, dataSet.schema(), tableName, joiner.toString());
		return ddl;
	}

	/**
	 * 整形済みのDDLを生成します
	 * @return 生成済みのDDL
	 */
	public final String toFormattedSql() {

		// DDL作成用インスタンス初期化
		StringJoiner joiner = new StringJoiner(StringUtils.COMMA_STR + System.lineSeparator());
		// デーブル名称取得
		String tableName = AppKSQLCreater.toTabelName(dataSet.table());

		// カラムリスト取得
		KagerowDataType[] columnList = dataSet.dataType();

		// 主キー設定
		joiner.add(StringUtils.TAB + isPK());

		// DDL生成
		for (KagerowDataType type : columnList) {
			String ddl = switch (type) {
			case BOOLEAN -> isBOOLEAN();
			case DATE -> isDATE();
			case DECIMAL -> isDECIMAL();
			case NULL -> isNULL();
			case NUMBER -> isNUMBER();
			case VARCHAR -> isVARCHAR();
			case TIMESTAMP -> isTIMESTAMP();
			};
			joiner.add(StringUtils.TAB + ddl);
		}

		// DDL作成
		String ddl = MessageFormat.format(FORMATED_DDL_FROMAT, dataSet.schema(), tableName, joiner.toString());
		return ddl;

	}

	/**
	 * 主キーを生成します
	 * @return DDL
	 */
	public abstract String isPK();

	/**
	 * DataTypeがNULLの場合のDDLを生成します
	 * @return DDL
	 */
	public abstract String isNULL();

	/**
	 * DataTypeがBOOLEANの場合のDDLを生成します
	 * @return DDL
	 */
	public abstract String isBOOLEAN();

	/**
	 * DataTypeがNUMBERの場合のDDLを生成します
	 * @return DDL
	 */
	public abstract String isNUMBER();

	/**
	 * DataTypeがDECIMALの場合のDDLを生成します
	 * @return DDL
	 */
	public abstract String isDECIMAL();

	/**
	 * DataTypeがDATEの場合のDDLを生成します
	 * @return DDL
	 */
	public abstract String isDATE();

	/**
	 * DataTypeがTIMESTAMPの場合のDDLを生成します
	 * @return DDL
	 */
	public abstract String isTIMESTAMP();

	/**
	 * DataTypeがVARCHARの場合のDDLを生成します
	 * @return DDL
	 */
	public abstract String isVARCHAR();

}

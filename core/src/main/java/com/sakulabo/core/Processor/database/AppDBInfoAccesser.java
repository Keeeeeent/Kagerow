package com.sakulabo.core.Processor.database;

import com.sakulabo.core.Kagerow.Contents.KagerowVirtualFileContent.KagerowDataType;
import com.sakulabo.core.Kagerow.Contents.KagerowVirtualFileContent.KagerowVirtualFileObject;
import com.sakulabo.core.Kagerow.Utilities.KagerowDBInfoAccesser;
import com.sakulabo.core.Kagerow.Utilities.KagerowDBMode;
import com.sakulabo.core.Processor.archive.DataSet;
import com.sakulabo.core.Processor.database.impl.H2DDLCreater;
import com.sakulabo.core.Processor.database.impl.H2InfoAccesserImpl;
import com.sakulabo.core.Processor.database.impl.MySQLDDLCreater;
import com.sakulabo.core.Processor.database.impl.MySQLInfoAccesserImpl;
import com.sakulabo.core.Processor.database.impl.OracleDDLCreater;
import com.sakulabo.core.Processor.database.impl.OracleInfoAccesserImpl;
import com.sakulabo.core.Processor.database.impl.PostgreSQLDDLCreater;
import com.sakulabo.core.Processor.database.impl.PostgreSQLInfoAccesserImpl;

/**
 * KDB情報操作インスタンスの基底クラスです
 * 
 * @author keeeeeent
 */
public sealed abstract class AppDBInfoAccesser
		implements KagerowDBInfoAccesser
		permits OracleInfoAccesserImpl, PostgreSQLInfoAccesserImpl, MySQLInfoAccesserImpl, H2InfoAccesserImpl {

	/** 処理モード */
	protected KagerowDBMode mode;

	/**
	 * デフォルトコンストラクタ
	 * @param mode 処理モード
	 */
	protected AppDBInfoAccesser(KagerowDBMode mode) {
		this.mode = mode;
	}

	/** {@inheritDoc} */
	@Override
	public String toRawDDL(KagerowVirtualFileObject data) {
		DataSet dataSet = toDataSet(data);
		return switch (mode) {
		case ORACLE -> new OracleDDLCreater(dataSet).toSql();
		case H2 -> new H2DDLCreater(dataSet).toSql();
		case MYSQL -> new MySQLDDLCreater(dataSet).toSql();
		case POSTGRESQL -> new PostgreSQLDDLCreater(dataSet).toSql();
		case ILLEGALITY -> throw new UnsupportedOperationException("Unimplemented case: " + mode);
		};
	}

	/** {@inheritDoc} */
	@Override
	public KagerowDataType[] toConvertibleList(KagerowVirtualFileObject data, String colName)
			throws IllegalArgumentException {
		// 元になるデータタイプを取得します
		KagerowDataType baseMode = getColumnType(data, colName);
		return baseMode.toModifiableList();
	}

	/**
	 * データオブジェクトを内部データ表現であるデータセットに反抗します
	 * @param data データオブジェクト
	 * @return 変換後のデータセット
	 */
	protected final DataSet toDataSet(KagerowVirtualFileObject data) {
		return new DataSet(
				data.schema(),
				data.binaryName(),
				data.headerData(),
				data.dataType(),
				data.dataSize());
	}

	/** {@inheritDoc} */
	@Override
	public String toDDL(KagerowVirtualFileObject data) {
		DataSet dataSet = toDataSet(data);
		return switch (mode) {
		case ORACLE -> new OracleDDLCreater(dataSet).toFormattedSql();
		case H2 -> new H2DDLCreater(dataSet).toFormattedSql();
		case MYSQL -> new MySQLDDLCreater(dataSet).toFormattedSql();
		case POSTGRESQL -> new PostgreSQLDDLCreater(dataSet).toFormattedSql();
		case ILLEGALITY -> throw new UnsupportedOperationException("Unimplemented case: " + mode);
		};
	}

	/** {@inheritDoc} */
	@Override
	public String toStandardExpression(KagerowVirtualFileObject data, String colName) throws IllegalArgumentException {
		KagerowDataType type = getColumnType(data, colName);
		long size = getColumnSize(data, colName);
		return switch (type) {
		case BOOLEAN -> isBOOLEAN();
		case DATE -> isDATE(size);
		case DECIMAL -> isDECIMAL(size);
		case NULL -> isNULL();
		case NUMBER -> isNUMBER(size);
		case VARCHAR -> isVARCHAR(size);
		case TIMESTAMP -> isTIMESTAMP(size);
		default -> throw new IllegalArgumentException("Unexpected value: " + type);
		};
	}

	/**
	 * データオブジェクトからデータタイプを検索します
	 * @param data データオブジェクト
	 * @param colName カラム名称
	 * @return データタイプ
	 * @throws IllegalArgumentException 対応するカラムが見つからなかった場合
	 */
	protected KagerowDataType getColumnType(KagerowVirtualFileObject data, String colName)
			throws IllegalArgumentException {
		String[] headerList = data.headerData();
		KagerowDataType[] typeList = data.dataType();
		for (int i = 0; i < headerList.length; i++) {
			if (colName.equals(headerList[i])) {
				return typeList[i];
			}
		}
		throw new IllegalArgumentException();
	}

	/**
	 * データオブジェクトからデータサイズを検索します
	 * @param data データオブジェクト
	 * @param colName カラム名称
	 * @return データサイズ
	 * @throws IllegalArgumentException 対応するカラムが見つからなかった場合
	 */
	protected long getColumnSize(KagerowVirtualFileObject data, String colName)
			throws IllegalArgumentException {
		String[] headerList = data.headerData();
		long[] sizeList = data.dataSize();
		for (int i = 0; i < headerList.length; i++) {
			if (colName.equals(headerList[i])) {
				return sizeList[i];
			}
		}
		throw new IllegalArgumentException();
	}

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
	 * @param size データサイズ
	 * @return DDL
	 */
	public abstract String isNUMBER(long size);

	/**
	 * DataTypeがDECIMALの場合のDDLを生成します
	 * @param size データサイズ
	 * @return DDL
	 */
	public abstract String isDECIMAL(long size);

	/**
	 * DataTypeがDATEの場合のDDLを生成します
	 * @param size データサイズ
	 * @return DDL
	 */
	public abstract String isDATE(long size);

	/**
	 * DataTypeがTIMESTAMPの場合のDDLを生成します
	 * @param size データサイズ
	 * @return DDL
	 */
	public abstract String isTIMESTAMP(long size);

	/**
	 * DataTypeがVARCHARの場合のDDLを生成します
	 * @param size データサイズ
	 * @return DDL
	 */
	public abstract String isVARCHAR(long size);

}

package com.sakulabo.core.Kagerow.Utilities;

import com.sakulabo.core.Kagerow.Contents.KagerowVirtualFileContent.KagerowDataType;
import com.sakulabo.core.Kagerow.Contents.KagerowVirtualFileContent.KagerowVirtualFileObject;
import com.sakulabo.core.Processor.database.AppDBInfoAccesser;
import com.sakulabo.core.Processor.database.impl.H2InfoAccesserImpl;
import com.sakulabo.core.Processor.database.impl.MySQLInfoAccesserImpl;
import com.sakulabo.core.Processor.database.impl.OracleInfoAccesserImpl;
import com.sakulabo.core.Processor.database.impl.PostgreSQLInfoAccesserImpl;

/**
 * KDBモード別の情報整形アクセッサー規定インターフェイスです
 * 
 * @author keeeeeent
 */
public sealed interface KagerowDBInfoAccesser permits AppDBInfoAccesser {

	/**
	 * データオブジェクトを整形済みのDDLへ変換します
	 * @param data データオブジェクト
	 * @return DDL
	 */
	public String toDDL(KagerowVirtualFileObject data);

	/**
	 * データオブジェクトを整形前のDDLへ変換します
	 * @param data データオブジェクト
	 * @return DDL
	 */
	public String toRawDDL(KagerowVirtualFileObject data);

	/**
	 * カラムの変更可能なデータ型一覧を生成します
	 * @param data データオブジェクト
	 * @param colName カラム
	 * @return 変換可能なデータ型一覧リスト
	 * @throws IllegalArgumentException 指定されたカラムが見つからない場合
	 */
	public KagerowDataType[] toConvertibleList(KagerowVirtualFileObject data, String colName)
			throws IllegalArgumentException;

	/**
	 * 型とサイズから生成可能な一般的な文字列表現を生成します
	 * @param data データオブジェクト
	 * @param colName カラム
	 * @return 変換可能なデータ型一覧リスト
	 * @throws IllegalArgumentException 指定されたカラムが見つからない場合
	 */
	public String toStandardExpression(KagerowVirtualFileObject data, String colName) throws IllegalArgumentException;

	/**
	 * 指定されれたモードに対応したアクセッサーを生成するファクトリメソッドです
	 * @param mode DBモード
	 * @return アクセッサー
	 */
	public static KagerowDBInfoAccesser newDBInfoAccesser(KagerowDBMode mode) {
		return switch (mode) {
		case ORACLE -> new OracleInfoAccesserImpl();
		case H2 -> new H2InfoAccesserImpl();
		case MYSQL -> new MySQLInfoAccesserImpl();
		case POSTGRESQL -> new PostgreSQLInfoAccesserImpl();
		case ILLEGALITY -> throw new UnsupportedOperationException("Unimplemented case: " + mode);
		};
	}

}

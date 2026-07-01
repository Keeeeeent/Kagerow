package com.sakulabo.regulation.spi;

import java.io.Serial;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

import javax.sql.rowset.CachedRowSet;

import com.sakulabo.core.Common.StringUtils;
import com.sakulabo.core.Kagerow.Utilities.KagerowDBMode;
import com.sakulabo.regulation.annotation.KagerowPlugin.PluginType;
import com.sakulabo.regulation.spi.PluginAdapter.Param.ParamList;

/**
 * プラグイン実装を規定するためのアダプターインターフェイスです
 * 
 * @author keeeeeent
 */
public interface PluginAdapter extends AutoCloseable {
 
	/**
	 * プラグイン向けのパラメータアノテーションです
	 */
	@Documented
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	@Repeatable(ParamList.class)
	public @interface Param {

		/**
		 * パラメータ名称
		 * @return 名称
		 */
		String value();

		/**
		 * パラメータ名称に関連付けされたデフォルト値
		 * @return デフォルト値 
		 */
		String defaultValue() default StringUtils.EMPTY;

		/**
		 * このパラメータが指定必須かどうか指定します
		 * @return 必須フラグ
		 */
		boolean required() default false;

		/**
		 * Paramのコンテナアノテーションです
		 */
		@Documented
		@Target(ElementType.METHOD)
		@Retention(RetentionPolicy.RUNTIME)
		public @interface ParamList {

			/**
			 * パラメータリスト
			 * @return リスト
			 */
			Param[] value();
		}

	}

	/**
	 * プラグインのバリエーションチェックにて検知したエラーを表す例外クラスです
	 */
	public static class PluginValidationException extends Exception {

		/** シリアルID */
		@Serial
		private static final long serialVersionUID = 3890498236603220041L;

		/** エラー対象となったキー */
		public final String validTarget;

		/**
		 * デフォルトコンストラクタ
		 * @param message エラー内容
		 * @param validTarget エラー対象     
		 */
		public PluginValidationException(String message, String validTarget) {
			super(message);
			this.validTarget = validTarget;
		}

		/**
		 * デフォルトコンストラクタ
		 * @param message エラー内容
		 * @param validTarget エラー対象
		 * @param cause 原因例外      
		 */
		public PluginValidationException(String message, String validTarget, Throwable cause) {
			super(message, cause);
			this.validTarget = validTarget;
		}

	}

	/**
	 * KDBデータ交換の構造体です
	 * 
	 * @param mode kdbモード 
	 * @param id ksqkid
	 * @param name ksql論理名称
	 * @param data ksql実行結果
	 */
	public record KagerowRowSet(
			String mode,
			String id,
			String name,
			CachedRowSet data) {
	}

	/**
	 * データインプット機能を提供します<br/>
	 * 返却したインプットデータはDMLに変換され仮想DBの構築に組み込まれます
	 * 
	 * @param params パラメータリスト 
	 * @param mode 構築モード
	 * @param connection KDBコネクション
	 */
	void input(Map<String, String> params, KagerowDBMode mode, Connection connection);

	/**
	 * データアウトプット機能を提供します<br/>
	 * 
	 * @param data アウトプット可能データ
	 * @param params パラメータリスト
	 */
	void output(Map<String, String> params, List<KagerowRowSet> data);

	/**
	 * プラグイン初期化時処理<br/>
	 * このメソッドはインスタンスが生成されてから1度だけ実行されます<br/>
	 * 実行のタイミングはコンテンツ生成直前です
	 */
	public default void initialize() {
		;
	}

	/**
	 * プラグイン終了時処理<br/>
	 * このメソッドはインスタンスが生成されてから1度だけ実行されます<br/>
	 * 実行のタイミングはJVM終了直前です
	 */
	@Override
	public default void close() throws Exception {
		;
	}

	/**
	 * バリデーションタイプに従ってプラグインのカスタムバリデーション処理を提供します
	 * @param params パラメータ
	 * @param type バリデーションタイプ
	 * @throws PluginValidationException チェックエラー
	 */
	void validation(Map<String, String> params, PluginType type) throws PluginValidationException;

}

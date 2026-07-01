package com.sakulabo.core.Kagerow.Utilities;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import javax.naming.NamingException;
import javax.sql.rowset.CachedRowSet;

import com.sakulabo.core.Common.StringUtils;
import com.sakulabo.core.Kagerow.Adapter.KagerowExecutionPlanAdapter;
import com.sakulabo.core.Kagerow.Contents.KagerowVirtualFileContent.KagerowVirtualFileObject;
import com.sakulabo.core.Kagerow.Exception.AppLogicException;
import com.sakulabo.core.Kagerow.Exception.KFileParseException;
import com.sakulabo.core.Kagerow.Exception.KSQLParseException;
import com.sakulabo.core.Kagerow.Exception.KagerowExecuteException;
import com.sakulabo.core.Kagerow.Utilities.KagerowScriptAccessor.KagerowCmdAccessor;
import com.sakulabo.core.Kagerow.Utilities.KagerowScriptAccessor.KagerowPluginAccessor;
import com.sakulabo.core.Processor.plan.BasicExecutionPlan;
import com.sakulabo.core.Processor.plan.DefaultExecutionPlanBaseAdapter;
import com.sakulabo.core.Processor.plan.ExecutionPlan;
import com.sakulabo.core.Processor.plan.ExecutionPlan.ExecutionPlanHistory;
import com.sakulabo.core.Processor.plan.SecureExecutionPlan;

/**
 * Kagerowスクリプトの実行計画アクセッサインターフェイスです
 * 
 * @author keeeeeent
 */
public sealed interface KagerowExecutionPlanAccessor permits ExecutionPlan {

	/**
	 * 現在の実行計画履歴にアクセス可能なアクセッサーを生成します
	 * @return 実行改革アクセッサー
	 */
	KagerowExecutionPlanHistoryAccessor getCurrentHistory();

	/**
	 * アクセッサーよりスクリプトを解析可能な状態にします
	 * @param script スクリプトアクセッサ
	 * @param planAdapter 実行ステップアダプター
	 * @throws AppLogicException ロジックエラー
	 */
	void lord(KagerowScriptAccessor script, KagerowExecutionPlanAdapter planAdapter) throws AppLogicException;

	/**
	 * 対象スクリプトが実行可能か検証を行います
	 * @return 仮返却変数
	 * @throws IllegalStateException 検証の結果実行が不可能な場合
	 */
	Void validation() throws IllegalStateException;

	/**
	 * ロード済みのスクリプトから実行計画を構築し、実行計画を遂行します
	 * @throws KagerowExecuteException KSQL実行要求失敗
	 */
	void execute() throws KagerowExecuteException;

	/**
	 * 指定したクエリをカレントセッションで実行します
	 * @param ksqlId 実行対象ID
	 * @return 実行結果セット
	 * @throws KagerowExecuteException KSQL実行要求失敗
	 */
	Optional<CachedRowSet> execute(String ksqlId) throws KagerowExecuteException;

	/**
	 * 現在のセッション状態を保存します
	 * @return キャッシュインスタンスID
	 * @throws AppLogicException 状態保存失敗
	 * @throws NamingException キャッシュコンテkスト取得失敗
	 */
	String toCache() throws AppLogicException, NamingException;

	/**
	 * 現在のセッションがキャッシュインスタンスであるかどうか判定します。キャッシュインスタンスの場合trueを返却します
	 * @return 判定結果
	 */
	boolean isCache();

	/**
	 * 現在のセッションがキャッシュインスタンスを最後に作成した日時を返却します。</br>
	 * キャッシュ対応インスタンス出ない場合、このメソッドはnullを返却します
	 * @return 取得結果
	 */
	String getCacheTime();

	/**
	 * 現在のセッションが何らかの理由でキャッシュ機能を無効化されている場合このメソッドはtrueを返却します。
	 * @return 判定結果
	 */
	boolean isIgnoreCashe();

	/**
	 * Kagerowで定められた実行順序によって処理を開始します
	 * @param path スクリプトパス
	 * @return 実行計画アクセッサー
	 * @throws KFileParseException KFile解析エラー
	 * @throws KSQLParseException KSQL解析エラー
	 * @throws AppLogicException アプリケーションロジック不正
	 * @throws NamingException 一時KDB物理ファイル生成失敗、またはキャッシュロードエラー
	 * @throws IOException 一時KDB物理ファイル生成失敗
	 * @throws KagerowExecuteException KSQL実行要求失敗
	 */
	public static KagerowExecutionPlanAccessor execute(Path path)
			throws KFileParseException, KSQLParseException, AppLogicException, NamingException, IOException,
			KagerowExecuteException {
		return KagerowExecutionPlanAccessor.execute(path, null, false);
	}

	/**
	 * 実行計画のライフサイクルフックのデフォルト実装を生成します
	 * @param plan 実行計画インスタンス
	 * @return ライフサイクルフックインスタンス
	 */
	public static KagerowExecutionPlanAdapter createDefaultPlanAdapter(KagerowExecutionPlanAccessor plan) {
		return new DefaultExecutionPlanBaseAdapter((ExecutionPlan<?>) plan);
	}

	/**
	 * Kagerowで定められた実行順序によって処理を開始します
	 * @param path スクリプトパス
	 * @param planAdapter 実行ステップアダプター
	 * @param isSecure セキュア実行フラグ
	 * @return 実行計画アクセッサー
	 * @throws KFileParseException KFile解析エラー
	 * @throws KSQLParseException KSQL解析エラー
	 * @throws AppLogicException アプリケーションロジック不正
	 * @throws NamingException 一時KDB物理ファイル生成失敗、またはキャッシュロードエラー
	 * @throws IOException 一時KDB物理ファイル生成失敗
	 * @throws KagerowExecuteException KSQL実行要求失敗
	 */
	public static KagerowExecutionPlanAccessor execute(
			Path path, KagerowExecutionPlanAdapter planAdapter, boolean isSecure)
			throws KFileParseException, KSQLParseException, AppLogicException, NamingException, IOException,
			KagerowExecuteException {

		// スクリプト生成
		KagerowScriptAccessor script = KagerowScriptAccessor.getInstance(path);

		// 実行インスタンス生成
		ExecutionPlan<? extends KagerowVirtualFileObject> plan;
		if (Objects.equals(script.getCacheId(), StringUtils.DEFAULT)) {
			if (isSecure) {
				// セキュアな実行計画を生成
				plan = new SecureExecutionPlan();
			} else {
				// デフォルトの実行計画を生成
				plan = new BasicExecutionPlan();
			}

		} else {
			if (isSecure) {
				// セキュアな実行計画を生成
				plan = new SecureExecutionPlan(script.getCacheId());
			} else {
				// キャッシュ対応スクリプトの場合、キャッシュをロードした状態で実行計画を初期化
				plan = new BasicExecutionPlan(script.getCacheId());
			}
		}

		if (Objects.isNull(planAdapter)) {
			// 実行ステップアダプターが未指定の場合、デフォルト実装を設定
			planAdapter = new DefaultExecutionPlanBaseAdapter(plan);
		}

		// スクリプトロード
		plan.lord(script, planAdapter);
		// 実行計画スタート
		plan.execute();

		return plan;
	}

	/**
	 * 実行計画履歴のアクセッサーインナーインターフェイスです
	 */
	public sealed interface KagerowExecutionPlanHistoryAccessor permits ExecutionPlanHistory {

		/**
		 * 実行コマンドを取得します
		 * @return 実行コマンドアクセッサー
		 */
		KagerowCmdAccessor getCmd();

		/**
		 * 実行SQL一覧（実行順序順）を取得します
		 * @return 実行SQL一覧
		 */
		Map<String, String> getSqlText();

		/**
		 * 実行入力プラグイン一覧（実行順序順）を取得します
		 * @return 実行入力プラグイン一覧
		 */
		Map<Integer, List<KagerowPluginAccessor>> getInputPlugin();

		/**
		 * 実行出力プラグイン一覧（実行順序順）を取得します
		 * @return 実行出力プラグイン一覧
		 */
		Map<Integer, List<KagerowPluginAccessor>> getOutputPlugin();

		/**
		 * 使用予定のスレッド数を取得します
		 * @return 使用予定のスレッド数
		 */
		int getLordStep();

		/**
		 * トランザクションIDを取得します
		 * @return トランザクションID
		 */
		UUID getTransactionId();

		/**
		 * 実行開始時刻を取得します
		 * @return 実行開始時刻
		 */
		Instant getStartTime();

		/**
		 * 実行終了時刻を取得します
		 * @return 実行終了時刻
		 */
		Instant getEndTime();

		/**
		 * 実行時間を取得します
		 * @return 実行時間
		 */
		Duration getExecutionTime();

		/**
		 * セッションIDを取得します
		 * @return セッションID
		 */
		UUID getSessionId();

		/**
		 * KDB構築先パスを取得します
		 * @return KDB構築先パス
		 */
		Path getKdbPath();

		/**
		 * 現在実行済みの結果セットを取得します
		 * @return 実行結果セット
		 */
		Map<String, CachedRowSet> currentRowSet();

	}
}

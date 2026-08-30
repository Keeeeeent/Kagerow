package com.sakulabo.application.app.rpc.executor;

import com.sakulabo.application.app.rpc.RpcSendParam;
import com.sakulabo.application.app.rpc.datatype.receive.Base64ReceiveDataType;
import com.sakulabo.application.app.rpc.datatype.receive.BooleanReceiveDataType;
import com.sakulabo.application.app.rpc.datatype.receive.CharsetReceiveDataType;
import com.sakulabo.application.app.rpc.datatype.receive.PathReceiveDataType;
import com.sakulabo.application.app.rpc.datatype.receive.StringReceiveDataType;
import com.sakulabo.application.app.rpc.datatype.send.StringSendDataType;
import com.sakulabo.application.app.rpc.exception.RpcRuntmeException;
import com.sakulabo.application.common.spi.RpcTarget;

/**
 * データ取り込み機能のRPCコントローラー規定インターフェイスです
 *
 * @author keeeeeent
 */
public interface DataImportExecutor extends RpcTarget {

	/**
	 * データ取り込み結果返却用データ構造
	 * @param uri KagerowURI
	 */
	public static record ImportResult(
			@RpcSendParam("uri") StringSendDataType uri) {
	};

	/**
	 * @param paramMode     実行モード
	 * @param paramSchema   スキーマファイル名
	 * @param paramPath     入力ファイル
	 * @param paramCharset  入力ファイル文字コード
	 * @param paramIsHeader ヘッダー有無
	 * @param paramSynonym  テーブル名称のシノニム
	 * @param paramIsSecure セキュアフラグ
	 * @return 取り込み結果
	 * @throws RpcRuntmeException メソッド実行失敗
	 */
	public ImportResult dataImport(
			StringReceiveDataType paramMode,
			StringReceiveDataType paramSchema,
			PathReceiveDataType paramPath,
			CharsetReceiveDataType paramCharset,
			BooleanReceiveDataType paramIsHeader,
			StringReceiveDataType paramSynonym,
			BooleanReceiveDataType paramIsSecure)
			throws RpcRuntmeException;

	/**
	 * @param paramMode     実行モード
	 * @param paramSchema   スキーマファイル名
	 * @param paramData     入力ファイル
	 * @param paramCharset  入力ファイル文字コード
	 * @param paramIsHeader ヘッダー有無
	 * @param paramSynonym  テーブル名称のシノニム
	 * @param paramIsSecure セキュアフラグ
	 * @return 取り込み結果
	 * @throws RpcRuntmeException メソッド実行失敗
	 */
	public ImportResult binarydataImport(
			StringReceiveDataType paramMode,
			StringReceiveDataType paramSchema,
			Base64ReceiveDataType paramData,
			CharsetReceiveDataType paramCharset,
			BooleanReceiveDataType paramIsHeader,
			StringReceiveDataType paramSynonym,
			BooleanReceiveDataType paramIsSecure)
			throws RpcRuntmeException;

}

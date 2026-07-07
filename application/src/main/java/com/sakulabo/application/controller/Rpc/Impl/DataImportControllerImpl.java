package com.sakulabo.application.controller.Rpc.Impl;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import javax.naming.NameAlreadyBoundException;

import com.sakulabo.application.app.rpc.RpcMethod;
import com.sakulabo.application.app.rpc.RpcMethodParam;
import com.sakulabo.application.app.rpc.RpcSetting;
import com.sakulabo.application.app.rpc.datatype.receive.BooleanReceiveDataType;
import com.sakulabo.application.app.rpc.datatype.receive.CharsetReceiveDataType;
import com.sakulabo.application.app.rpc.datatype.receive.PathReceiveDataType;
import com.sakulabo.application.app.rpc.datatype.receive.StringReceiveDataType;
import com.sakulabo.application.controller.Rpc.DataImportController;
import com.sakulabo.core.Kagerow.Exception.VirtualFileConstructionFailException;
import com.sakulabo.core.Kagerow.Utilities.KagerowChunkCreater.ChunkCreateMode;
import com.sakulabo.core.Kagerow.Utilities.KagerowVirtualFileCreater;

/**
 * データ取り込み機能のRPCコントローラー実装クラスです
 *
 * @author keeeeeent
 */
@RpcSetting("data")
public class DataImportControllerImpl implements DataImportController {

	/**
	 * @param paramMode     実行モード
	 * @param paramSchema   スキーマファイル名
	 * @param paramPath     入力ファイル
	 * @param paramCharset  入力ファイル文字コード
	 * @param paramIsHeader ヘッダー有無
	 * @param paramSynonym  テーブル名称のシノニム
	 * @param paramIsSecure セキュアフラグ
	 * @throws IOException                          DBクリエイター初期化失敗
	 * @throws NameAlreadyBoundException            既に同等の仮想DB物理ファイルが生成されている場合
	 * @throws VirtualFileConstructionFailException 仮想DB物理ファイル生成失敗
	 */
	@RpcMethod("import")
	public void dataImport(@RpcMethodParam(value = "mode", required = true) StringReceiveDataType paramMode,
			@RpcMethodParam(value = "schema", required = true) StringReceiveDataType paramSchema,
			@RpcMethodParam(value = "path", required = true) PathReceiveDataType paramPath,
			@RpcMethodParam("charset") CharsetReceiveDataType paramCharset,
			@RpcMethodParam("isHeader") BooleanReceiveDataType paramIsHeader,
			@RpcMethodParam("synonym") StringReceiveDataType paramSynonym,
			@RpcMethodParam("isSecure") BooleanReceiveDataType paramIsSecure)
			throws NameAlreadyBoundException, IOException, VirtualFileConstructionFailException {

		// モード変換
		ChunkCreateMode mode = ChunkCreateMode.valueOf(paramMode.getRawType().get().toUpperCase());
		String schema = paramSchema.getRawType().get();
		Path path = paramPath.getRawType().get();
		Charset charset = paramCharset.getRawType().orElse(StandardCharsets.UTF_8);
		boolean isHeader = paramIsHeader.getRawType().orElse(Boolean.FALSE);
		String synonym = paramSynonym.getRawType().orElse(null);
		boolean isSecure = paramIsSecure.getRawType().orElse(Boolean.FALSE);

		// メイン処理呼び出し
		KagerowVirtualFileCreater.constructionKDB(mode, schema, path, charset, isHeader, synonym, isSecure);

	}
}

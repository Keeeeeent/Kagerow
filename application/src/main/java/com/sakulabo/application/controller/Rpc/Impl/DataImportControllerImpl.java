package com.sakulabo.application.controller.Rpc.Impl;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.naming.NameAlreadyBoundException;

import com.sakulabo.application.app.rpc.RpcMethod;
import com.sakulabo.application.app.rpc.RpcMethodParam;
import com.sakulabo.application.app.rpc.RpcSetting;
import com.sakulabo.application.app.rpc.datatype.receive.BooleanReceiveDataType;
import com.sakulabo.application.app.rpc.datatype.receive.CharsetReceiveDataType;
import com.sakulabo.application.app.rpc.datatype.receive.PathReceiveDataType;
import com.sakulabo.application.app.rpc.datatype.receive.StringReceiveDataType;
import com.sakulabo.application.app.rpc.datatype.send.StringSendDataType;
import com.sakulabo.application.app.rpc.exception.RpcIllegalArgumentException;
import com.sakulabo.application.app.rpc.exception.RpcRuntmeException;
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

	/** {@inheritDoc} */
	@Override
	@RpcMethod("import")
	public ImportResult dataImport(
			@RpcMethodParam(value = "mode", required = true) StringReceiveDataType paramMode,
			@RpcMethodParam(value = "schema", required = true) StringReceiveDataType paramSchema,
			@RpcMethodParam(value = "path", required = true) PathReceiveDataType paramPath,
			@RpcMethodParam("charset") CharsetReceiveDataType paramCharset,
			@RpcMethodParam("isHeader") BooleanReceiveDataType paramIsHeader,
			@RpcMethodParam("synonym") StringReceiveDataType paramSynonym,
			@RpcMethodParam("isSecure") BooleanReceiveDataType paramIsSecure)
			throws RpcRuntmeException {

		try {

			// モード変換
			ChunkCreateMode mode;
			try {
				mode = ChunkCreateMode.valueOf(paramMode.getRawType().get().toUpperCase());
			} catch (IllegalArgumentException | NullPointerException e) {
				// 例外翻訳
				throw new RpcIllegalArgumentException(
						"Illegal ChunkCreateMode : " + paramMode.getRawType().get().toUpperCase());
			}

			// 取り込み対象チェック
			Path path = paramPath.getRawType().get();
			if (Files.notExists(path)) {
				throw new RpcIllegalArgumentException("File Not Found : " + path);
			}

			// 残りの引数取得
			String schema = paramSchema.getRawType().get();
			Charset charset = paramCharset.getRawType().orElse(StandardCharsets.UTF_8);
			boolean isHeader = paramIsHeader.getRawType().orElse(Boolean.FALSE);
			String synonym = paramSynonym.getRawType().orElse(null);
			boolean isSecure = paramIsSecure.getRawType().orElse(Boolean.FALSE);

			// メイン処理呼び出し
			URI uri = KagerowVirtualFileCreater.constructionKDB(mode, schema, path, charset, isHeader, synonym,
					isSecure);

			// 結果返却
			StringSendDataType sendUri = new StringSendDataType(uri.toString());
			ImportResult result = new ImportResult(sendUri);
			return result;

		} catch (NameAlreadyBoundException | IOException | VirtualFileConstructionFailException e) {
			// 例外翻訳
			throw new RpcRuntmeException(e.getMessage());
		}

	}
}

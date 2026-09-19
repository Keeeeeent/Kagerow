package com.sakulabo.application.app.rpc.exception;

import java.net.HttpURLConnection;
import java.util.Objects;

import com.sakulabo.application.app.rpc.RpcSendParam;
import com.sakulabo.application.app.rpc.datatype.send.IntegerSendDataType;

/**
 * レスポンス発生時に終了コード対応レスポンスを生成する例外クラスです
 */
public class ExitCodeException extends BaseServerException {

    /** 終了コード */
    @RpcSendParam("exitCode")
    public final IntegerSendDataType exitCode;

    /**
     * デフォルトコンストラクタ
     *
     * @param message  送信メッセージ
     * @param exitCode 終了コード
     * @param cause    原因例外
     */
    public ExitCodeException(String message, int exitCode, Throwable cause) {
        super(message, HttpURLConnection.HTTP_NOT_MODIFIED, cause);
        this.exitCode = new IntegerSendDataType(Objects.requireNonNull(exitCode));
    }

    /**
     * デフォルトコンストラクタ
     *
     * @param exitCode 終了コード
     * @param message  送信メッセージ
     */
    public ExitCodeException(String message, int exitCode) {
        super(message, HttpURLConnection.HTTP_NOT_MODIFIED, null);
        this.exitCode = new IntegerSendDataType(Objects.requireNonNull(exitCode));
    }

}

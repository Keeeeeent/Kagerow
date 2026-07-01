package com.sakulabo.launcher;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.instrument.Instrumentation;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;

import com.sakulabo.core.Common.StringUtils;
import com.sakulabo.launcher.Initer.GraphicalIniter;
import com.sakulabo.launcher.Initer.InitProcessFailedException;
import com.sakulabo.launcher.Initer.InitProcessFailedException.FailType;
import com.sakulabo.launcher.Initer.Initer;
import com.sakulabo.launcher.Inject.LoardDIBeans;

/**
 * 初期化処理を実行するエントリークラスです<br/>
 * このクラスにはアプリ事前初期化、アプリ起動メソッドが実装されています。
 * @author keeeeeent
 */
public final class Main {

	/**
	 * モジュールを保持<br/>
	 * モジュール構成で実行されていない可能性があります
	 */
	public final static Optional<Module> module;
	static {
		// クラスからモジュールを取得
		module = Module.class
				.getModule()
				.getLayer()
				.findModule(StringUtils.MODULE_NAME);
	}

	/**
	 * アタッチエージェントのエントリーです
	 * @param arg 起動オプション
	 */
	public static void agentmain(String arg) {
		InetAddress localhost = InetAddress.getLoopbackAddress();
		try (Socket socket = new Socket(localhost, Integer.valueOf(arg));
				OutputStream output = socket.getOutputStream();
				OutputStreamWriter convert = new OutputStreamWriter(output, StandardCharsets.UTF_8);
				BufferedWriter writer = new BufferedWriter(convert)) {
			// 遅延通信を無効化
			socket.setTcpNoDelay(true);
			// 結果の返却
			for (Object prop : System.getProperties().keySet()) {
				writer.write(Objects.toString(prop));
			}
			// バッファ同期
			writer.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 初期化エージェントのエントリーです
	 * @param arg 初期化オプション
	 * @param inst JVM管理インスタンス
	 * @throws InterruptedException 
	 */
	@SuppressWarnings("exports")
	public static void premain(String arg, Instrumentation inst)
			throws InterruptedException {
		// トランスフォーマーの登録
		LoardDIBeans.init(inst);
		// 引数生成
		String[] args = Objects.isNull(arg) ? new String[0] : arg.split(StringUtils.COMMA_STR, 0);
		// 引数が存在しない場合、初期化処理はスキップ
		String initType = args.length == 0 ? StringUtils.EMPTY : args[0];
		// 初期化プロセスのストラテジーを選択します
		Initer initProcess = switch (Initer.InitType.convertType(initType)) {
		case NO_INIT -> new Initer.EmptyInit();
		case ALL_INIT -> new Initer.AllInit();
		case GUI_INIT -> new GraphicalIniter();
		default -> new Initer.EmptyInit();
		};
		// 初期化処理実行
		init: while (true) {
			try {
				initProcess.doInitProcessAll();
				break init;
			} catch (InitProcessFailedException e) {
				// 例外のスタックトレース出力
				e.printStackTrace();
				// 予期せぬ例外
				final FailType type = e.getType();
				switch (type) {
				case Continue:
					System.out.println(Initer.createMesssage(FailType.class.getSimpleName(), Initer.CONTINUE_MESSAGE));
					break;
				case Skip:
					System.err.println(Initer.createMesssage(FailType.class.getSimpleName(), Initer.SKIP_MESSAGE));
					break init;
				case Reject:
					System.exit(type.getExitCode());
					break;
				}
			}
		}
	}

}

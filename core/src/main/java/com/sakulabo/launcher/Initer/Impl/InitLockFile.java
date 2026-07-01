package com.sakulabo.launcher.Initer.Impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.sakulabo.core.Common.AppPathUtils;
import com.sakulabo.core.Common.StringUtils;
import com.sakulabo.core.Common.VMOption;
import com.sakulabo.launcher.Initer.InitProcessFailedException;
import com.sakulabo.launcher.Initer.InitProcessFailedException.FailType;
import com.sakulabo.launcher.Initer.InitProcessor;
import com.sakulabo.launcher.Initer.Initer;
import com.sun.tools.attach.VirtualMachine;

/**
 * 重複起動制御をする初期化プロセッサー実装クラスです
 * @author keeeeeent
 */
public final class InitLockFile implements InitProcessor {

	/** ファイルパス */
	private final static Path LOCK_FILE_PATH;
	static {
		String userHome = AppPathUtils.getBasePath();
		LOCK_FILE_PATH = Paths.get(userHome, ".kagerow", "app.lock");
	}

	/** ロックファイル（ファイル) */
	private volatile RandomAccessFile file;
	/** ロックファイル（チャネル) */
	private volatile FileChannel channel;
	/** ロックファイル（ロック) */
	private volatile FileLock lock;

	/** シングルトンインスタンス */
	private static InitProcessor instance;

	/** メッセージキー(重複起動) */
	private final static String NO_PID = "no-pid";
	/** メッセージキー(引数不正) */
	private final static String NO_ARG = "no-arg";
	/** メッセージキー(ロック取得失敗) */
	private final static String FAIL_LOCK = "fail-lock";
	/** メッセージキー(Kagerowインスタンス) */
	private final static String DUPLICATE_LAUNCH = "duplicate-launch";

	/** 重複起動フラグ */
	private boolean flug;

	/**
	 * ファクトリメソッド
	 * @return 本クラスのシングルトンインスタンス
	 */
	public final static InitProcessor getInstance() {
		if (Objects.isNull(instance)) {
			synchronized (InitLockFile.class) {
				if (Objects.isNull(instance)) {
					instance = InitProcessor.create(InitLockFile::new);
				}
			}
		}
		return instance;
	}

	/**
	 * インスタンス生成禁止
	 */
	private InitLockFile() {
		;
	}

	/** {@inheritDoc} */
	@Override
	public void init() throws InitProcessFailedException {
		// カレントプロセスのPIDを取得
		final long pid = ProcessHandle.current().pid();
		try {
			// ロックファイルインスタンス生成
			file = new RandomAccessFile(LOCK_FILE_PATH.toString(), StringUtils.RAND_READ_WRITE);
			channel = file.getChannel();
			// 排他制御
			synchronized (InitLockFile.class) {
				// ロックを取得
				lock = channel.tryLock();
				// ロック取得に失敗した場合、例外をスロー
				if (Objects.isNull(lock)) {
					throw new Exception(Initer.createMesssage(Initer.PREFIX, FAIL_LOCK));
				}
			}
			// PIDを書き込むため前回のインスタンスが異常終了などでファイルが残っている可能性がある
			// ファイルがある場合でもすぐ起動中と判断せず、中身を確認し対象のPIDがOSないでアイドルしていないか確認する
			// ファイルにはバイナリ形式でPIDを書き込んでいるが、不正なデータが書き込まれていないか念のため確認する
			if (Files.exists(LOCK_FILE_PATH)
					&& Files.size(LOCK_FILE_PATH) != 0
					&& Files.size(LOCK_FILE_PATH) % 8 == 0) {
				// ファイル内部のPIDを取得
				long p = file.readLong();
				// OSないに対象のPIDをもつ、プロセスがないか確認し取得
				long targetPID = ProcessHandle.allProcesses()
						.map(ProcessHandle::pid)
						.filter(f -> f == p)
						.count();
				// PIDが見つかった場合
				if (targetPID != 0) {
					// PIDの生存確認をする
					ProcessHandle target = ProcessHandle.of(p)
							.filter(ProcessHandle::isAlive)
							.orElseThrow(() -> new Exception(Initer.createMesssage(Initer.PREFIX, NO_PID)));
					// 対象PIDが生存していた場合、Kagerowインスタンスか判別をする
					for (String arg : getArguments(target)) {
						arg = arg.toLowerCase();
						if (arg.contains(VMOption.INSTANCE.toString())) {
							flug = true;
							throw new Exception(Initer.createMesssage(Initer.PREFIX, DUPLICATE_LAUNCH));
						}
					}
				}
			}
			// ロックファイルを切り捨てし、PIDを書き込む準備をする
			channel.truncate(0);
			// ロックファイルにPIDを記録
			file.writeLong(pid);
			// リソースの解放処理をシャットダウントリガーに追加
			Runtime.getRuntime().addShutdownHook(new Thread(this::releaseResource));
		} catch (Exception e) {
			if (flug) {
				// 重複起動の場合、個別エラーフラグを立てる
				throw new InitProcessFailedException(e, FailType.Reject, String.valueOf(pid));
			} else {
				// 例外が発生した場合はリソースを解放し、初期化処理の即時終了を通知する
				releaseResource();
				throw new InitProcessFailedException(e, FailType.Reject);
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public void endLod(Method method, Object[] args, Object result) {
		System.out.println(String.format("Locked By %s", LOCK_FILE_PATH.toAbsolutePath()));
	}

	/**
	 * リソースの解放
	 */
	private final void releaseResource() {
		try {
			if (Objects.nonNull(lock))
				lock.release();
			if (Objects.nonNull(channel))
				channel.close();
			if (Objects.nonNull(file))
				file.close();
			if (Files.exists(LOCK_FILE_PATH))
				Files.delete(LOCK_FILE_PATH);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * VM引数一覧取得
	 * @param target VMインスタンスプロセスハンドラ
	 * @return VM引数一覧
	 * @throws Exception VMインスタンス接続不可/エージェントロードエラー
	 */
	private final List<String> getArguments(ProcessHandle target) throws Exception {
		// PIDの取得
		long pid = target.pid();
		// VMインスタンスへアタッチ
		VirtualMachine vm = VirtualMachine.attach(String.valueOf(pid));
		// 返却リスト初期化
		List<String> result = new ArrayList<>();
		// 受け取りアドレス生成
		// ループバックアドレス取得
		InetAddress localhost = InetAddress.getLoopbackAddress();
		// ローカルホスト上で自動的に選択されたポートを使用
		InetSocketAddress address = new InetSocketAddress(localhost, 0);
		// KeepAliveの待ち時間を生成
		long serverKeepAlive = Duration.ofSeconds(10).toMillis();
		long agentKeepAlive = Duration.ofSeconds(1).toMillis();
		// ますはエージェント実行結果受け取り環境構築
		try (ServerSocket server = new ServerSocket()) {
			// アドレスの設定
			server.bind(address);
			// タイムアウトの設定
			server.setSoTimeout(Long.valueOf(serverKeepAlive).intValue());
			// ポート番号の取得
			int port = server.getLocalPort();
			// エージェント実行
			String agentJar = AppPathUtils
					.createAppDirPath()
					.resolve(VMOption.AGENTJAR.getVMoption())
					.toString();
			vm.loadAgent(agentJar, String.valueOf(port));
			// レスポンス受け取り
			try (
					Socket socket = server.accept();
					InputStream input = socket.getInputStream();
					InputStreamReader convert = new InputStreamReader(input, StandardCharsets.UTF_8);
					BufferedReader reader = new BufferedReader(convert)) {
				// KeepAliveの設定
				socket.setKeepAlive(true);
				// 遅延通信を無効化
				socket.setTcpNoDelay(true);
				// タイムアウトの設定
				socket.setSoTimeout(Long.valueOf(agentKeepAlive).intValue());
				// 結果受け取り
				String line = null;
				while ((line = reader.readLine()) != null) {
					result.add(line);
				}
			}
		}
		// 引数を取得できなかった場合、例外をスロー
		if (result.isEmpty()) {
			throw new Exception(Initer.createMesssage(Initer.PREFIX, NO_ARG));
		}
		return result;
	}

}

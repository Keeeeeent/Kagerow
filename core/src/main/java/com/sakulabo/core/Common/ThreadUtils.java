package com.sakulabo.core.Common;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;

/**
 * Kagerowアプリケーション専用スレッド制御ユーティリティクラスです
 * 
 * @author keeeeeent
 */
public final class ThreadUtils {

	/**
	 * インスタンス生成禁止
	 */
	private ThreadUtils() {
		;
	}

	/** スレッドグループ(プラグイン) */
	public static final ThreadGroup PLUGIN_GROUP = new ThreadGroup("PluginGroup");
	/** スレッドグループ(チャンクローダー) */
	public static final ThreadGroup CHUNKL_ORDER_GROUP = new ThreadGroup("AppChunkLorder");
	/** スレッドグループ(JMX) */
	public static final ThreadGroup JMX_GROUP = new ThreadGroup("JMXThread");
	/** スレッドグループ(実行計画) */
	public static final ThreadGroup EXECUTION_PLUN_GROUP = new ThreadGroup("ExecutionPlan");
	/** 空のRunnable実装 */
	public static final Runnable EMPTY_RUNNABLE = new Runnable() {
		@Override
		public void run() {
			;
		}
	};

	/**
	 * ユーザスレッドを生成するスレッドファクトリインスタンスを生成します
	 * @param group スレッドグループ
	 * @return スレッドファクトリクラスインスタンス
	 */
	public static ThreadFactory newUserThreadFactory(ThreadGroup group) {

		class tmpFactory implements ThreadFactory {
			@Override
			public Thread newThread(Runnable r) {
				Thread thread = new Thread(group, r);
				thread.setName(group.getName());
				return thread;
			}
		}

		return new tmpFactory();
	}

	/**
	 * デーモンスレッドを生成するスレッドファクトリインスタンスを生成します
	 * @param group スレッドグループ
	 * @return スレッドファクトリクラスインスタンス
	 */
	public static ThreadFactory newDemonThreadFactory(ThreadGroup group) {

		class tmpFactory implements ThreadFactory {
			@Override
			public Thread newThread(Runnable r) {
				Thread thread = new Thread(group, r);
				thread.setName(group.getName());
				thread.setDaemon(true);
				return thread;
			}
		}

		return new tmpFactory();
	}

	/**
	 * バーチャルスレッドを生成するスレッドファクトリインスタンスを生成します
	 * @param group スレッドグループ
	 * @return スレッドファクトリクラスインスタンス
	 */
	public static ThreadFactory newVirtualThreadFactory(ThreadGroup group) {

		class tmpFactory implements ThreadFactory {
			@Override
			public Thread newThread(Runnable r) {
				Thread thread = Thread.ofVirtual().unstarted(r);
				thread.setName(group.getName());
				thread.setDaemon(true);
				return thread;
			}
		}

		return new tmpFactory();

	}

	/**
	 * JVMシャットダウンフックにExecutorServiceを終了するよう登録します
	 * @param executor プールインスタンス
	 */
	public static void addShutdownHook(ExecutorService executor) {

		class endHook extends Thread {
			@Override
			public void run() {
				executor.shutdownNow();
			}
		}

		Runtime.getRuntime().addShutdownHook(new endHook());
	}
}

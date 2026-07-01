package com.sakulabo.core.Provides;

import java.util.Hashtable;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

import com.sakulabo.core.Kagerow.Context.Impl.KagerowContextImpl;

/**
 * 独自コンテキスト実装を提供するプロバイダクラスです
 * 
 * @author keeeeeent
 */
public final class InitialContextFactoryProvider extends KagerowContextImpl implements InitialContextFactory {

	/** コンテキストシングルトンインスタンス保持メモリ */
	private static volatile Context context;
	/** シャットダウンフラグ */
	private static final AtomicBoolean endFlug = new AtomicBoolean(false);
	/** シャッドダウンフック */
	private static final Thread shutdownThread;
	static {
		// スレッド生成
		shutdownThread = new Thread(() -> {
			try {
				if (endFlug.compareAndSet(false, true)) {
					context.close();
				}
			} catch (NamingException e) {
				e.printStackTrace();
			}
		});
		// スレッド名設定
		shutdownThread.setName("shutdown");
	}

	/** {@inheritDoc} */
	@Override
	public Context getInitialContext(Hashtable<?, ?> environment) throws NamingException {
		if (Objects.isNull(context)) {
			synchronized (InitialContextFactoryProvider.class) {
				if (Objects.isNull(context)) {
					// コンテキスト生成
					String password = (String) environment.get(KagerowContextImpl.SECURE_KEY);
					context = KagerowContextImpl.getInstance(password);
					// JVM終了処理追加
					Runtime.getRuntime().addShutdownHook(shutdownThread);
				}
			}
		}
		return context;
	}

}

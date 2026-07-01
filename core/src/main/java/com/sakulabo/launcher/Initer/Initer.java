package com.sakulabo.launcher.Initer;

import java.lang.StackWalker.Option;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;

import com.sakulabo.core.Common.StringUtils;
import com.sakulabo.launcher.Main;
import com.sakulabo.launcher.Initer.InitProcessFailedException.FailType;
import com.sakulabo.launcher.Initer.Initer.AllInit;
import com.sakulabo.launcher.Initer.Initer.EmptyInit;
import com.sakulabo.launcher.Initer.Impl.ArchiveDataCleanner;
import com.sakulabo.launcher.Initer.Impl.CheckArgument;
import com.sakulabo.launcher.Initer.Impl.CreateRumtimeEnv;
import com.sakulabo.launcher.Initer.Impl.InitLockFile;
import com.sakulabo.launcher.Initer.Impl.TmpFolderCleaner;

/**
 * 初期化処理の機能を提供する基底クラスです
 * 
 * @author keeeeeent
 */
public sealed abstract class Initer permits EmptyInit, AllInit, GraphicalIniter {

	/**
	 * メッセージプレフィック対象
	 */
	@Documented
	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface TargetKey {
		/**
		 * ログメッセージ
		 * @return ログメッセージ
		 */
		String value();
	}

	/**
	 * 初期化識別種別
	 */
	public static enum InitType {
		/** 初期化スキップ */
		NO_INIT,
		/** 全量初期化（デフォルト） */
		ALL_INIT,
		/** GUI対応 */
		GUI_INIT,
		/** 不明な場合 */
		UNKNOWN;

		/**
		 * 文字列を初期化識別インスタンスに変換します<br/>
		 * このメソッドは大文字/小文字を区別しません
		 * @param type 初期化識別子を表す文字列
		 * @return 初期化識別子インスタンス
		 */
		public static InitType convertType(String type) {
			InitType result = UNKNOWN;
			type = type.toUpperCase();
			for (InitType initType : values()) {
				if (initType.name().equals(type))
					result = initType;
			}
			return result;
		}
	}

	/** メッセージプレフィックス */
	public final static String PREFIX = "init";

	/** メッセージキー(継続) */
	public final static String CONTINUE_MESSAGE = "continue-message";
	/** メッセージキー(スキップ) */
	public final static String SKIP_MESSAGE = "skip-message";
	/** メッセージキー(モジュール不明) */
	public final static String NO_MODULE = "no-module";
	/** メッセージキー(最大リトライ到達) */
	private final static String MAX_RETRY_COUNT = "max-retry-count";

	/** メッセージ一覧取得 */
	protected final static ResourceBundle messages;
	static {
		// モジュール取得
		// モジュール構成で実行されていない場合、例外をスローしシステム終了
		Module module = Main.module.orElseThrow(
				() -> new RuntimeException(createMesssage(PREFIX, NO_MODULE)));
		// リソースバンドル取得
		messages = ResourceBundle.getBundle(
				"config.message.Initer-Message", Locale.getDefault(), module);
	}

	/** 初期化処理を保持するリスト */
	protected final static List<InitProcessor> initProcessList = Collections
			.unmodifiableList(new ArrayList<InitProcessor>() {
				{

					/**
					 * ここに登録した順番で初期化処理が実行されます
					 */

					// コマンドライン必須引数チェック
					add(InitProcessor.create(CheckArgument::new));

					// 実行環境構築
					add(InitProcessor.create(CreateRumtimeEnv::new));

					// プラグインファイルシンボリックリンク生成
					// add(InitProcessor.create(CreatePluginLink::new));

					// 重複起動チェック
					add(InitLockFile.getInstance());

					// アーカイブデータクリーンアップ
					add(InitProcessor.create(ArchiveDataCleanner::new));

					// 一時フォルダクリーンアップ
					add(InitProcessor.create(TmpFolderCleaner::new));

				}
			});

	/**
	 * 初期化メッセージ生成メソッド
	 * @param prefix メッセージグループプレフィックス
	 * @param type メッセージタイプ
	 * @param options メッセージ引数
	 * @return メッセージ
	 */
	public final static String createMesssage(String prefix, String type, Object... options) {
		Class<?> clazz = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).getCallerClass();
		return createMesssage(clazz, prefix, type, options);
	}

	/**
	 * 初期化メッセージ生成メソッド
	 * @param clazz 対象クラス情報
	 * @param prefix メッセージグループプレフィックス
	 * @param type メッセージタイプ
	 * @param options メッセージ引数
	 * @return メッセージ
	 */
	public final static String createMesssage(Class<?> clazz, String prefix, String type, Object... options) {
		TargetKey targetKey = clazz.getAnnotation(TargetKey.class);
		String target = Objects.isNull(targetKey) ? clazz.getSimpleName() : targetKey.value();
		String result = messages.getString(String.join(StringUtils.DOT_STR, target, prefix, type));
		if (0 < options.length) {
			result = MessageFormat.format(result, options);
		}
		return result;
	}

	/**
	 * 全ての初期化処理を実行します
	 * @throws InitProcessFailedException 初期化処理にて発生した例外
	 */
	public abstract void doInitProcessAll() throws InitProcessFailedException;

	/**
	 * 空の初期化処理を構築します
	 */
	public final static class EmptyInit extends Initer {

		/**
		 * デフォルトコンストラクタ
		 */
		public EmptyInit() {
			super();
		}

		/** {@inheritDoc} */
		@Override
		public void doInitProcessAll() throws InitProcessFailedException {
			;
		}
	}

	/**
	 * 全ての初期化処理を実行するIniterを構築します<br/>
	 * 途中で初期化処理が中断した場合、再度初めから実行し直します<br />
	 * 尚、初期化処理が失敗した場合最大3回まで再実行されます
	 */
	public final static class AllInit extends Initer {

		/** 初期化処理の実行回数を保持します */
		private int round;

		/**
		 * デフォルトコンストラクタ
		 */
		public AllInit() {
			super();
		}

		/** {@inheritDoc} */
		@Override
		public void doInitProcessAll() throws InitProcessFailedException {
			if (3 < round++)
				throw new InitProcessFailedException(createMesssage(PREFIX, MAX_RETRY_COUNT), FailType.Reject);
			for (InitProcessor processer : initProcessList)
				processer.init();
		}
	}

}

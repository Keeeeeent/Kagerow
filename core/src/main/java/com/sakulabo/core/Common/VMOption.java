package com.sakulabo.core.Common;

import java.util.Objects;

/**
 * KagerowアプリケーションのVMOption列挙クラスです
 * 
 * @author keeeeeent
 */
public enum VMOption {

	// ###############################################
	// # 固有プロパティー
	// ###############################################

	/** アプリケーション識別マーカー */
	INSTANCE("instance"),
	/** アプリケーション一時パス */
	APP_IO_TMPDIR("app.io.tmpdir"),
	/** アプリケーションエージェントパス */
	AGENTJAR("agentjar"),
	/** アプリケーションアーカイブデータパス */
	APP_IO_ARCHIVEDATADIR("app.io.archivedatadir"),
	/** アプリケーションログパス */
	APP_LOGSDIR("app.logs.dir"),
	/** DIプロバイダー */
	LOARDDIBEANS_ADAPTER("LoardDIBeans.Adapter"),
	/** アプリケーションバージョン */
	APP_VERSION("app.version"),
	/** ランチャー設定ファイルシステムプロパティーキー */
	LAUNCHER_FILE_KEY("app.launcher.config"),
	/** アプリケーション起動モード */
	APP_INIT_MODE("app.init.mode"),
	/** アプリケーションホーム・ディレクトリ */
	APP_HOME("app.home"),

	// ###############################################
	// # 標準プロパティー
	// ###############################################

	/** Java Runtime Environmentのバージョン */
	JAVA_VERSION("java.version"),
	/** Java Runtime Environmentのベンダー */
	JAVA_VENDOR("java.vendor"),
	/** JavaベンダーのURL */
	JAVA_VENDOR_URL("java.vendor.url"),
	/** Javaのインストール先ディレクトリ */
	JAVA_HOME("java.home"),
	/** Java仮想マシンの仕様バージョン */
	JAVA_VM_SPECIFICATION_VERSION("java.vm.specification.version"),
	/** Java仮想マシンの仕様のベンダー */
	JAVA_VM_SPECIFICATION_VENDOR("java.vm.specification.vendor"),
	/** Java仮想マシンの仕様名 */
	JAVA_VM_SPECIFICATION_NAME("java.vm.specification.name"),
	/** Java仮想マシンの実装バージョン */
	JAVA_VM_VERSION("java.vm.version"),
	/** Java仮想マシンの実装のベンダー */
	JAVA_VM_VENDOR("java.vm.vendor"),
	/** Java仮想マシンの実装名 */
	JAVA_VM_NAME("java.vm.name"),
	/** Java Runtime Environmentの仕様バージョン */
	JAVA_SPECIFICATION_VERSION("java.specification.version"),
	/** Java Runtime Environmentの仕様のベンダー */
	JAVA_SPECIFICATION_VENDOR("java.specification.vendor"),
	/** Java Runtime Environmentの仕様名 */
	JAVA_SPECIFICATION_NAME("java.specification.name"),
	/** Javaクラスの形式のバージョン番号 */
	JAVA_CLASS_VERSION("java.class.version"),
	/** Javaクラス・パス */
	JAVA_CLASS_PATH("java.class.path"),
	/** ライブラリのロード時に検索するパスのリスト */
	JAVA_LIBRARY_PATH("java.library.path"),
	/** デフォルト一時ファイルのパス */
	JAVA_IO_TMPDIR("java.io.tmpdir"),
	/** 使用するJITコンパイラの名前 */
	JAVA_COMPILER("java.compiler"),
	/** 拡張ディレクトリ (Deprecated) */
	JAVA_EXT_DIRS("java.ext.dirs"),
	/** オペレーティング・システム名 */
	OS_NAME("os.name"),
	/** オペレーティング・システムのアーキテクチャ */
	OS_ARCH("os.arch"),
	/** オペレーティング・システムのバージョン */
	OS_VERSION("os.version"),
	/** ファイル区切り文字 (UNIXでは"/") */
	FILE_SEPARATOR("file.separator"),
	/** パス区切り文字 (UNIXでは":") */
	PATH_SEPARATOR("path.separator"),
	/** 行区切り文字 (UNIXでは"\n") */
	LINE_SEPARATOR("line.separator"),
	/** ユーザーのアカウント名 */
	USER_NAME("user.name"),
	/** ユーザーのホーム・ディレクトリ */
	USER_HOME("user.home"),
	/** ユーザーの現在の作業ディレクトリ */
	USER_DIR("user.dir"),

	// ###############################################
	// # 特殊プロパティー
	// ###############################################

	/** その他 */
	@Deprecated
	UNKNOWN(null) {

		/**
		 * 常にnullを返却します
		 */
		@Override
		public String getVMoption(String systemValue) {
			return null;
		}

		/**
		 * 常にnullを返却します
		 */
		@Override
		public String toVMOption() {
			return null;
		}

		/**
		 * 常にnullを返却します
		 */
		@Override
		public String getVMoption() {
			return null;
		}

	};

	/** VMオプション文字列表現 */
	private final String vmoption;

	private VMOption(String vmoption) {
		this.vmoption = vmoption;
	}

	/**
	 * VMOptionの文字列表現を取得します
	 * 
	 * @return VMOption文字列表現
	 */
	public String toVMOption() {
		String option = "-D" + vmoption;
		return vmoption.isBlank() ? null : option;
	}

	/**
	 * JVMからVMOptionに関連付けられた値を取得します
	 * 
	 * @return 関連付けられた値
	 */
	public String getVMoption() {
		return Objects.isNull(vmoption) ? null : System.getProperty(vmoption);
	}

	/**
	 * JVMからVMOptionに関連付けられた値を取得します
	 * 
	 * @param defaultValue デフォルト値
	 * @return 関連付けられた値
	 */
	public String getVMoption(String defaultValue) {
		return Objects.toString(getVMoption(), defaultValue);
	}

	/**
	 * JVMにVMOptionとして値を関連付けします
	 * 
	 * @param vmoption 設定値
	 */
	public void setVMoption(String vmoption) {
		System.setProperty(this.vmoption, vmoption);
	}

	/**
	 * 文字列表現をVMOptionに変換します<br/>
	 * VMOption文字列表現が存在しない場合、UNKNOWNを返却します
	 * 
	 * @param vmoption VMOption文字列表現
	 * @return VMOptionインスタンス
	 */
	public static final VMOption toVMOption(String vmoption) {
		VMOption option = UNKNOWN;
		for (VMOption opt : values()) {
			if (opt.vmoption.equals(vmoption))
				option = opt;
		}
		return option;
	}

	/**
	 * 文字列表現をVMOptionに変換します<br/>
	 * VMOption文字列表現が存在しない場合、UNKNOWNを返却します
	 * 
	 * @param vmoption     VMOption文字列表現
	 * @param defaultValue デフォルト値
	 * @return VMOptionインスタンス
	 */
	public static final VMOption toVMOption(String vmoption, VMOption defaultValue) {
		VMOption option = toVMOption(vmoption);
		return option == UNKNOWN ? defaultValue : option;
	}

	/**
	 * 与えられた値が紐づけられているVMOptionを返却します<br/>
	 * 関連付けられたキーが存在しない場合、UNKNOWNを返却します
	 * 
	 * @param value 検索値
	 * @return 関連付けられたキー
	 */
	public static final VMOption searchVMOption(String value) {
		VMOption option = UNKNOWN;
		for (VMOption opt : values()) {
			String v = opt.getVMoption();
			if (value.equals(v))
				option = opt;
		}
		return option;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return vmoption;
	}

}

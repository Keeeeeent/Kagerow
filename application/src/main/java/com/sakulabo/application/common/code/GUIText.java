package com.sakulabo.application.common.code;

import java.awt.event.KeyEvent;
import java.text.MessageFormat;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * GUIで表示するテキストを管理するコード値クラスです
 *
 * @author keeeeeent
 */
public enum GUIText {

	/** 空の実装です */
	EMPTY(ApplicationConstProperty.DEFAULT_MNEMONIC) {

		/** {@inheritDoc} */
		@Override
		public String toString() {
			return "";
		}

		/** {@inheritDoc} */
		@Override
		public String toString(Object[] params) {
			return "";
		}
	},

	/** MainFrame(メイン画面免責事項ダイアログタイトル) */
	MainFrame_001(ApplicationConstProperty.DEFAULT_MNEMONIC),

	/** FileMenu(メニュータイトル) */
	FileMenu_001(KeyEvent.VK_F),
	/** FileMenu(スクリプト追加) */
	FileMenu_002(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** FileMenu(スクリプト保存) */
	FileMenu_003(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** FileMenu(データインポート) */
	FileMenu_004(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** FileMenu(データエクスポート) */
	FileMenu_005(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** FileMenu(スクリプト編集) */
	FileMenu_006(ApplicationConstProperty.DEFAULT_MNEMONIC),

	/** NoticePanel(バージョン情報フォーマット) */
	NoticePanel_001(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** NoticePanel(CPU使用率) */
	NoticePanel_002(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** NoticePanel(メモリ使用率) */
	NoticePanel_003(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** NoticePanel(ストレージ使用率) */
	NoticePanel_004(ApplicationConstProperty.DEFAULT_MNEMONIC),

	/** HelpMenu(メニュータイトル) */
	HelpMenu_001(KeyEvent.VK_H),
	/** HelpMenu(アプリケーションライセンス情報) */
	HelpMenu_002(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** HelpMenu(サードパーティライセンス情報) */
	HelpMenu_003(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** HelpMenu(リリースノート) */
	HelpMenu_004(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** HelpMenu(免責事項) */
	HelpMenu_005(ApplicationConstProperty.DEFAULT_MNEMONIC),

	/** SettingMenu(メニュータイトル) */
	SettingMenu_001(KeyEvent.VK_S),
	/** SettingMenu(セキュアブート) */
	SettingMenu_002(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** SettingMenu(パスワード再設定) */
	SettingMenu_003(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** SettingMenu(データリセット) */
	SettingMenu_004(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** SettingMenu(バックアップ作成) */
	SettingMenu_005(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** SettingMenu(バックアップ取込) */
	SettingMenu_006(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** SettingMenu(詳細設定) */
	SettingMenu_007(ApplicationConstProperty.DEFAULT_MNEMONIC),

	/** ScriptPanel(追加) */
	ScriptPanel_001(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** ScriptPanel(削除) */
	ScriptPanel_002(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** ScriptPanel(個別実行) */
	ScriptPanel_003(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** ScriptPanel(作成) */
	ScriptPanel_004(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** ScriptPanel(更新) */
	ScriptPanel_005(ApplicationConstProperty.DEFAULT_MNEMONIC),

	/** QuickStartPanel(クイックスタート) */
	QuickStartPanel_001(ApplicationConstProperty.DEFAULT_MNEMONIC),

	/** NonComponentResultViewPanel ツールバーボタン（スクリプト実行） */
	NonComponentResultViewPanel_001(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** NonComponentResultViewPanel ツールバーボタン（CSV出力） */
	NonComponentResultViewPanel_002(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** NonComponentResultViewPanel ツールバーボタン（TSV出力） */
	NonComponentResultViewPanel_003(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** NonComponentResultViewPanel 内部パネルボタン（参照） */
	NonComponentResultViewPanel_004(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** NonComponentResultViewPanel 内部パネルボタン（出力先） */
	NonComponentResultViewPanel_005(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** NonComponentResultViewPanel 内部パネルボタン（出力形式） */
	NonComponentResultViewPanel_006(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** NonComponentResultViewPanel 内部パネルボタン（取消） */
	NonComponentResultViewPanel_007(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** NonComponentResultViewPanel 内部パネルボタン（出力） */
	NonComponentResultViewPanel_008(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** NonComponentResultViewPanel 内部パネルボタン（日付フォーマット） */
	NonComponentResultViewPanel_009(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** NonComponentResultViewPanel 内部パネルボタン（文字コード） */
	NonComponentResultViewPanel_010(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** NonComponentResultViewPanel 内部パネルボタン（出力対象） */
	NonComponentResultViewPanel_011(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** NonComponentResultViewPanel 内部パネルボタン（エスケープ有無） */
	NonComponentResultViewPanel_012(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** NonComponentResultViewPanel 内部パネルボタン（ヘッダー有無） */
	NonComponentResultViewPanel_013(ApplicationConstProperty.DEFAULT_MNEMONIC),

	/** NonComponentSscriptEditPanel ツールバーボタン（共通画面切替） */
	NonComponentSscriptEditPanel_001(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** NonComponentSscriptEditPanel ツールバーボタン（KSQL画面切替） */
	NonComponentSscriptEditPanel_002(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** NonComponentSscriptEditPanel ツールバーボタン（プラグイン画面切替） */
	NonComponentSscriptEditPanel_003(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** NonComponentSscriptEditPanel ツールバーボタン（コマンド画面切替） */
	NonComponentSscriptEditPanel_004(ApplicationConstProperty.DEFAULT_MNEMONIC),

	/** NonComponentDataImportJPanel 内部パネルボタン（取消） */
	NonComponentDataImportJPanel_001(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** NonComponentDataImportJPanel 内部パネルボタン（実行） */
	NonComponentDataImportJPanel_002(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** NonComponentDataImportJPanel 内部パネルボタン（シノニム） */
	NonComponentDataImportJPanel_003(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** NonComponentDataImportJPanel 内部パネルボタン（格納先スキーマ） */
	NonComponentDataImportJPanel_004(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** NonComponentDataImportJPanel 内部パネルボタン（文字コード） */
	NonComponentDataImportJPanel_005(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** NonComponentDataImportJPanel 内部パネルボタン（実行モード） */
	NonComponentDataImportJPanel_006(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** NonComponentDataImportJPanel 内部パネルボタン（セキュア） */
	NonComponentDataImportJPanel_007(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** NonComponentDataImportJPanel 内部パネルボタン（ヘッダー） */
	NonComponentDataImportJPanel_008(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** NonComponentDataImportJPanel 内部パネルボタン（取り込み中） */
	NonComponentDataImportJPanel_009(ApplicationConstProperty.DEFAULT_MNEMONIC),

	/** NonComponentCommonEditPanel_001 内部パネルラベル（実行モード） */
	NonComponentCommonEditPanel_001(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** NonComponentCommonEditPanel_002 内部パネルタイトル（共通設定） */
	NonComponentCommonEditPanel_002(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** NonComponentCommonEditPanel_003 内部パネルタイトル（環境変数） */
	NonComponentCommonEditPanel_003(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** NonComponentCommonEditPanel_004 内部パネルラベル（スクリプト名称） */
	NonComponentCommonEditPanel_004(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** NonComponentCommonEditPanel_005 内部パネルラベル（スクリプト概要） */
	NonComponentCommonEditPanel_005(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** NonComponentCommonEditPanel_006 内部パネルラベル（カレントスキーマ） */
	NonComponentCommonEditPanel_006(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** NonComponentCommonEditPanel_007 内部パネルラベル（環境変数を追加） */
	NonComponentCommonEditPanel_007(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** NonComponentCommonEditPanel_008 内部パネルラベル（追加する環境変数の名称を入力してください） */
	NonComponentCommonEditPanel_008(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** NonComponentCommonEditPanel_009 内部パネルタイトル（キャッシュ） */
	NonComponentCommonEditPanel_009(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** NonComponentCommonEditPanel_010 内部パネルタイトル（キャッシュID） */
	NonComponentCommonEditPanel_010(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** NonComponentCommonEditPanel_011 内部パネルタイトル（項目名） */
	NonComponentCommonEditPanel_011(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** NonComponentCommonEditPanel_012 内部パネルタイトル（設定値） */
	NonComponentCommonEditPanel_012(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** NonComponentCommonEditPanel_013 内部パネルタイトル（最終更新日時） */
	NonComponentCommonEditPanel_013(ApplicationConstProperty.DEFAULT_MNEMONIC),


	/** NonComponentCommandEditPanel_001 内部パネルラベル（コマンド設定） */
	NonComponentCommandEditPanel_001(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** NonComponentCommandEditPanel_001 内部パネルラベル（コマンド環境変数） */
	NonComponentCommandEditPanel_002(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** NonComponentCommandEditPanel_001 内部パネルラベル（コマンド） */
	NonComponentCommandEditPanel_003(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** NonComponentCommonEditPanel_005 内部パネルラベル（環境変数を追加） */
	NonComponentCommandEditPanel_004(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** NonComponentCommonEditPanel_005 内部パネルラベル（追加する環境変数の名称を入力してください） */
	NonComponentCommandEditPanel_005(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** NonComponentCommonEditPanel_006 内部パネルラベル（実行モード設定） */
	NonComponentCommandEditPanel_006(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** NonComponentCommonEditPanel_007 内部パネルラベル（コマンド実行モード） */
	NonComponentCommandEditPanel_007(ApplicationConstProperty.DEFAULT_MNEMONIC),

	/** NonComponentSqlEditPanel_001 内部パネルラベル（KSQL一覧） */
	NonComponentSqlEditPanel_001(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** NonComponentSqlEditPanel_002 内部パネルラベル（編集） */
	NonComponentSqlEditPanel_002(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** NonComponentSqlEditPanel_003 内部パネルラベル（KSQLを追加） */
	NonComponentSqlEditPanel_003(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** NonComponentSqlEditPanel_004 内部パネルラベル（追加するKSQLの名称を入力してください） */
	NonComponentSqlEditPanel_004(ApplicationConstProperty.DEFAULT_MNEMONIC),

	/** NonComponentKsqlPopupEditPanel_001 内部ポップアップパネルラベル（KSQ設定） */
	NonComponentKsqlPopupEditPanel_001(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** NonComponentKsqlPopupEditPanel_002 内部ポップアップパネルラベル（置換変数） */
	NonComponentKsqlPopupEditPanel_002(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** NonComponentKsqlPopupEditPanel_003 内部ポップアップパネルラベル（置換変数を追加） */
	NonComponentKsqlPopupEditPanel_003(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** NonComponentKsqlPopupEditPanel_004 内部ポップアップパネルラベル（追加する置換変数の名称を入力してください） */
	NonComponentKsqlPopupEditPanel_004(ApplicationConstProperty.DEFAULT_MNEMONIC),

	/** NonComponentPluginEditPanel_001 内部パネルラベル（入力プラグイン一覧） */
	NonComponentPluginEditPanel_001(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** NonComponentPluginEditPanel_002 内部パネルラベル（出力プラグイン一覧） */
	NonComponentPluginEditPanel_002(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** NonComponentPluginEditPanel_003 内部パネルラベル（パラメータ一覧） */
	NonComponentPluginEditPanel_003(ApplicationConstProperty.DEFAULT_MNEMONIC),

	/** DisclaimerPanel_001 免責事項パネルタイトル */
	DisclaimerPanel_001(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** DisclaimerPanel_002 免責事項同意ボタン */
	DisclaimerPanel_002(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** DisclaimerPanel_003 免責事項終了ボタン */
	DisclaimerPanel_003(ApplicationConstProperty.DEFAULT_MNEMONIC),

	/** VirtualFileSchemaSubContextPanel_001 操作ボタン */
	VirtualFileSchemaSubContextPanel_001(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** VirtualFileSchemaSubContextPanel_002 情報ボタン */
	VirtualFileSchemaSubContextPanel_002(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** VirtualFileSchemaSubContextPanel_003 削除ボタン */
	VirtualFileSchemaSubContextPanel_003(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** VirtualFileSchemaSubContextPanel_004 スキーマサイズ */
	VirtualFileSchemaSubContextPanel_004(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** VirtualFileSchemaSubContextPanel_005 テーブル数 */
	VirtualFileSchemaSubContextPanel_005(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** VirtualFileSchemaSubContextPanel_006 最終更新 */
	VirtualFileSchemaSubContextPanel_006(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** VirtualFileSchemaSubContextPanel_007 スキーマ削除 */
	VirtualFileSchemaSubContextPanel_007(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** VirtualFileSchemaSubContextPanel_008 スキーマ削除説明 */
	VirtualFileSchemaSubContextPanel_008(ApplicationConstProperty.DEFAULT_MNEMONIC),

	/** VirtualFileSchemaMapTableModel_001 情報項目名称 */
	VirtualFileSchemaMapTableModel_001(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** VirtualFileSchemaMapTableModel_002 情報 */
	VirtualFileSchemaMapTableModel_002(ApplicationConstProperty.DEFAULT_MNEMONIC),

	/** VirtualFileTableInfoMapTableModel_001 情報項目名称 */
	VirtualFileTableInfoMapTableModel_001(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** VirtualFileTableInfoMapTableModel_002 情報 */
	VirtualFileTableInfoMapTableModel_002(ApplicationConstProperty.DEFAULT_MNEMONIC),

	/** VirtualFileTableSubContextPanel_001 情報ボタン */
	VirtualFileTableSubContextPanel_001(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** VirtualFileTableSubContextPanel_002 データタイプ */
	VirtualFileTableSubContextPanel_002(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** VirtualFileTableSubContextPanel_003 世代 */
	VirtualFileTableSubContextPanel_003(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** VirtualFileTableSubContextPanel_004 DDL */
	VirtualFileTableSubContextPanel_004(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** VirtualFileTableSubContextPanel_005 操作 */
	VirtualFileTableSubContextPanel_005(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** VirtualFileTableSubContextPanel_006 テーブル削除説明 */
	VirtualFileTableSubContextPanel_006(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** VirtualFileTableSubContextPanel_007 テーブル削除 */
	VirtualFileTableSubContextPanel_007(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** VirtualFileTableSubContextPanel_008 削除 */
	VirtualFileTableSubContextPanel_008(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** VirtualFileTableSubContextPanel_009 テーブル物理名 */
	VirtualFileTableSubContextPanel_009(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** VirtualFileTableSubContextPanel_010 kagerowURI */
	VirtualFileTableSubContextPanel_010(ApplicationConstProperty.DEFAULT_MNEMONIC),

	/** VirtualFileTableDataTypeMapTableModel_001 カラム名称 */
	VirtualFileTableDataTypeMapTableModel_001(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** VirtualFileTableDataTypeMapTableModel_002 データタイプ */
	VirtualFileTableDataTypeMapTableModel_002(ApplicationConstProperty.DEFAULT_MNEMONIC),

	/** VirtualFileTableGenerationListTableModel_001 インデックス */
	VirtualFileTableGenerationListTableModel_001(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** VirtualFileTableGenerationListTableModel_002 取り込み日付 */
	VirtualFileTableGenerationListTableModel_002(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** VirtualFileTableGenerationListTableModel_003 サイズ */
	VirtualFileTableGenerationListTableModel_003(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** VirtualFileTableGenerationListTableModel_004 アクセスキーワード */
	VirtualFileTableGenerationListTableModel_004(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** VirtualFileTableGenerationListTableModel_005 操作 */
	VirtualFileTableGenerationListTableModel_005(ApplicationConstProperty.DEFAULT_MNEMONIC),

	/** PluginSubContextPanel_001 操作 */
	PluginSubContextPanel_001(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** PluginSubContextPanel_002 プラグイン無効化説明 */
	PluginSubContextPanel_002(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** PluginSubContextPanel_003 プラグイン無効化 */
	PluginSubContextPanel_003(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** PluginSubContextPanel_004 無効化 */
	PluginSubContextPanel_004(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** PluginSubContextPanel_005 プラグイン有効化 */
	PluginSubContextPanel_005(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** PluginSubContextPanel_006 有効化 */
	PluginSubContextPanel_006(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** PluginSubContextPanel_007 プラグイン有効化説明 */
	PluginSubContextPanel_007(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** PluginSubContextPanel_008 情報 */
	PluginSubContextPanel_008(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** PluginSubContextPanel_009 入力 */
	PluginSubContextPanel_009(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** PluginSubContextPanel_010 出力 */
	PluginSubContextPanel_010(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** PluginSubContextPanel_011 パッケージ */
	PluginSubContextPanel_011(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** PluginSubContextPanel_012 プラグイン */
	PluginSubContextPanel_012(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** PluginSubContextPanel_013 バージョン */
	PluginSubContextPanel_013(ApplicationConstProperty.DEFAULT_MNEMONIC),

	/** PluginPkgContextPanel_001 操作 */
	PluginPkgContextPanel_001(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** PluginPkgContextPanel_002 プラグインパッケージ削除説明 */
	PluginPkgContextPanel_002(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** PluginPkgContextPanel_003 プラグインパッケージ削除 */
	PluginPkgContextPanel_003(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** PluginPkgContextPanel_004 削除 */
	PluginPkgContextPanel_004(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** PluginPkgContextPanel_005 プラグインパッケージ無効化説明 */
	PluginPkgContextPanel_005(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** PluginPkgContextPanel_006 プラグインパッケージ無効化 */
	PluginPkgContextPanel_006(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** PluginPkgContextPanel_007 無効化 */
	PluginPkgContextPanel_007(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** PluginPkgContextPanel_008 プラグインパッケージ有効化 */
	PluginPkgContextPanel_008(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** PluginPkgContextPanel_009 有効化 */
	PluginPkgContextPanel_009(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** PluginPkgContextPanel_010 プラグインパッケージ有効化説明 */
	PluginPkgContextPanel_010(ApplicationConstProperty.DEFAULT_MNEMONIC),

	/** PluginInfoMapTableModel_001 情報項目名称 */
	PluginInfoMapTableModel_001(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** PluginInfoMapTableModel_002 情報 */
	PluginInfoMapTableModel_002(ApplicationConstProperty.DEFAULT_MNEMONIC),

	/** PluginParamInfoListTableModel_001 パラメータ項目名称 */
	PluginParamInfoListTableModel_001(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** PluginParamInfoListTableModel_002 デフォルト値 */
	PluginParamInfoListTableModel_002(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** PluginParamInfoListTableModel_003 必須フラグ */
	PluginParamInfoListTableModel_003(ApplicationConstProperty.DEFAULT_MNEMONIC),

	/** AdvancedSettingsPanel_001 KSQL関連 */
	AdvancedSettingsPanel_001(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** AdvancedSettingsPanel_002 CSV取込関連 */
	AdvancedSettingsPanel_002(ApplicationConstProperty.DEFAULT_MNEMONIC),

	/** SettingInfoMapTableModel_001 項目名称 */
	SettingInfoMapTableModel_001(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** SettingInfoMapTableModel_002 設定値 */
	SettingInfoMapTableModel_002(ApplicationConstProperty.DEFAULT_MNEMONIC),

	/** CommonSettingPanel_001 適用 */
	CommonSettingPanel_001(ApplicationConstProperty.DEFAULT_MNEMONIC),

	/** CacheSubContextPanelText_001 操作ボタン */
	CacheSubContextPanelText_001(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** CacheSubContextPanelText_002 削除ボタン */
	CacheSubContextPanelText_002(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** CacheSubContextPanelText_003 キャッシュ削除 */
	CacheSubContextPanelText_003(ApplicationConstProperty.DEFAULT_MNEMONIC),
	/** CacheSubContextPanelText_004 キャッシュ削除説明 */
	CacheSubContextPanelText_004(ApplicationConstProperty.DEFAULT_MNEMONIC),

	;

	/** リソースバンドル */
	private static final ResourceBundle message;
	static {
		message = BaseCode.createResourceBundle();
	}

	/** ニーモック */
	private final int mnemonic;

	/**
	 * デフォルトコンストラクタ
	 * @param mnemonic ニーモック
	 */
	private GUIText(int mnemonic) {
		this.mnemonic = mnemonic;
	}

	/**
	 * 関連付けられたニーモックを取得します
	 * @return ニーモック
	 */
	public int toMnemonic() {
		return mnemonic;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		if (Objects.isNull(message)) {
			BaseCode.createException();
		}
		return message.getString(name());
	}

	/**
	 * 指定されたパラメータを埋込文字として利用した文字列を生成します
	 * @param params 埋込文字リスト
	 * @return 生成文字列
	 */
	public String toString(Object[] params) {
		if (Objects.isNull(message)) {
			BaseCode.createException();
		}
		String msg = message.getString(name());
		msg = MessageFormat.format(msg, params);
		return msg;
	}

	/**
	 * 改行を含む文字列をGUI上で扱えるようした文字列を返却します
	 * @return 生成文字列
	 */
	public String toLineString() {
		if (Objects.isNull(message)) {
			BaseCode.createException();
		}
		return message.getString(name()).replace("\\n", "\n");
	}

}

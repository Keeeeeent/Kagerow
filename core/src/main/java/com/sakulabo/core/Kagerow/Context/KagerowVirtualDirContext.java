package com.sakulabo.core.Kagerow.Context;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import javax.naming.Name;
import javax.naming.NamingException;

import com.sakulabo.core.Kagerow.Contents.KagerowVirtualFileContent;
import com.sakulabo.core.Kagerow.Context.Impl.KagerowVirtualDirContextImpl;

public sealed interface KagerowVirtualDirContext extends KagerowContexts<KagerowVirtualFileContent>
		permits KagerowVirtualDirContextImpl {

	/** コンテキスト名称 */
	public static final String _NAME = "KagerowVirtualDir";

	/**
	 * KagerowVirtualFileContextが管理しているファイルパスを返却します
	 * @return ファイルパス
	 */
	public abstract Path getPath();

	/**
	 * このコンテキストの親コンテキストを返却します
	 * @return 親コンテキスト
	 */
	public abstract KagerowVirtualFileContext getParent();

	/** {@inheritDoc} */
	@Override
	public KagerowVirtualFileContent createSubcontext(Name name) throws NamingException;

	/** {@inheritDoc} */
	@Override
	public KagerowVirtualFileContent createSubcontext(String name) throws NamingException;

	/**
	 * 事前マッピングされたシノニムマッピングリストを返却します
	 * @return マッピングリスト
	 */
	public Map<String, String> getSynonymMapList();

	/**
	 * マッピングリストに新しいシノニムを追加します
	 * @param binaryHash 物理名称
	 * @param synonym 論理名称
	 * @throws NamingException マップ更新・取得処理失敗
	 */
	public void addSynonymMapList(String binaryHash, String synonym) throws NamingException;

	/**
	 * シノニムリストから対象のシノニムを削除します
	 * @param synonym 論理名称
	 * @throws NamingException マップ更新・取得処理失敗
	 */
	public void removeSynonymMapList(String synonym) throws NamingException;

	/**
	 * シノニムリストの対象をシノニム新たな名称で登録します
	 * @param oldSynonym 古い論理名称
	 * @param newSynonym 新しい論理名称
	 * @throws NamingException マップ更新・取得処理失敗
	 */
	public void renameSynonymMapList(String oldSynonym, String newSynonym) throws NamingException;

	/**
	 * 指定シノニムが別オブジェクトでバインド済みか判定します
	 * @param synonym 論理名称
	 * @param binaryHash 物理名称
	 * @return 判定結果
	 */
	public boolean isExistSynonym(String synonym, String binaryHash);

	/**
	 * ファイルシステムにて認識されているスキーマデータ量をKB単位のサイズで返却します
	 * @return サイズ
	 * @throws IOException サイズ取得失敗
	 */
	public long getSchemaContextSize() throws IOException;

}

package com.sakulabo.core.Kagerow.Utilities;

/**
 * Kagerowスクリプトファイルの状態管理を行う列挙クラスです
 * 
 * @author keeeeeent
 */
public enum KagerowFileState {

	/** 次のアクションが可能であることを現します */
	Ready,
	/** ファイルが編集中であることを現します */
	Editing,
	/** ファイルが保存処理中であることを現します */
	Saving,
	/** ファイルの状態が表せないことを現します */
	Fail;

}

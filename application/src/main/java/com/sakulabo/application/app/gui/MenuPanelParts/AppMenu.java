package com.sakulabo.application.app.gui.MenuPanelParts;

import java.awt.event.ActionListener;

import javax.swing.JMenu;

import com.sakulabo.application.common.code.ApplicationConstProperty;
import com.sakulabo.application.common.code.GUIText;

/**
 * メニュー基底クラスです
 * 
 * @author keeeeeent
 */
public abstract class AppMenu extends JMenu implements ActionListener {

	/**
	 * デフォルトコンストラクタ
	 * @param text メニュー名称
	 */
	public AppMenu(GUIText text) {
		// 共通コンストラクタ
		super(text.toString());
		// ニーモック設定
		int mnemonic = text.toMnemonic();
		if (ApplicationConstProperty.DEFAULT_MNEMONIC != mnemonic) {
			// ニーモックの指定がある場合のみ設定を適用する
			setMnemonic(mnemonic);
		}

	}

}

package com.sakulabo.application.common.image;

import java.net.URL;
import java.util.Objects;
import java.util.Optional;

import javax.swing.ImageIcon;

import com.sakulabo.application.common.code.ApplicationConstProperty;

/**
 * アプリケーション共通で使用する画像イメージ定数値クラスです
 * 
 * @author keeeeeent
 */
public enum ButtonImage {

	/** 空のアイコン　*/
	EMPTY(null),
	/** 閉じるボタン */
	CLOSE("close.png");

	/** イメージ */
	private final ImageIcon icon;

	/**
	 * デフォルトコンストラクタ
	 * @param fileName ファイル名称
	 */
	private ButtonImage(String fileName) {

		// モジュール取得
		Optional<Module> module = ButtonImage.class
				.getModule()
				.getLayer()
				.findModule(ApplicationConstProperty.MODULE_NAME);

		if (Objects.isNull(fileName)) {
			icon = null;
		} else if (module.isPresent()) {
			URL url = module.get().getClassLoader().getResource("image-icon/" + fileName);
			icon = new ImageIcon(url);
		} else {
			icon = null;
			System.err.println("not find image file " + fileName);
		}

	}

	/**
	 * イメージを返却します
	 * @return イメージインスタンス
	 */
	public ImageIcon toImageIcon() {
		return icon;
	}

}

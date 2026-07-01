package com.sakulabo.application.app.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.List;
import java.util.Objects;
import java.util.Vector;

import javax.swing.JPanel;

import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;
import com.sakulabo.regulation.annotation.KagerowInject;

/**
 * GUIアプリケーションのメインパネル基底クラスです
 * 
 * @author keeeeeent
 */
public abstract class AppPanel extends JPanel {
	
	/** ロガー */
	@KagerowInject
	protected KagerowLogger logger;

	/** コンポーネント固有のオブザーバーリスト */
	protected final List<Runnable> OBSERVER = new Vector<>();

	/**
	 * デフォルトコンストラクタ
	 */
	protected AppPanel() {
		super();
		setLayout(new BorderLayout());
	}

	/**
	 * GUIの初期化処理を実行します
	 */
	public final void initialize() {
		add(getComponent(), BorderLayout.CENTER);
	};

	/**
	 * オブザーバーをパネルに追加します
	 * @param observer オブザーバーインスタンス
	 */
	public final void addObserver(Runnable observer) {
		OBSERVER.add(Objects.requireNonNull(observer));
	}

	/**
	 * 登録済みの全てのオブザーバーに通知を行います
	 */
	public final void noticeObserver() {
		OBSERVER.forEach(Runnable::run);
	}

	/**
	 * 管理中のコンポーネントを返却します
	 * @return コンポーネント
	 */
	public abstract Component getComponent();

}

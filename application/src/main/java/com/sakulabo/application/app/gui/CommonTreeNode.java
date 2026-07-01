package com.sakulabo.application.app.gui;

import java.awt.event.MouseEvent;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * 共通ノードクラスです
 * 
 * @author keeeeeent
 */
public abstract class CommonTreeNode extends DefaultMutableTreeNode {

	/** ノード名称 */
	protected final String name;

	/** アクション実行フラグ */
	protected final AtomicBoolean isAction = new AtomicBoolean();

	/**
	 * アクション実行処理
	 * @param e イベント
	 */
	protected abstract void doMouseClicked(MouseEvent e);

	/**
	 * マウスクリック事前アクション
	 * @param e イベント
	 */
	protected abstract void doMousePressed(MouseEvent e);

	/**
	 * マウスクリック事後アクション
	 * @param e イベント
	 */
	protected abstract void doMouseReleased(MouseEvent e);

	/**
	 * 共通コンストラクタ
	 * @param name ノード名称
	 */
	protected CommonTreeNode(String name) {
		this.name = Objects.requireNonNull(name);
		setUserObject(this);
	}

	/**
	 * アクション実行処理
	 * @param e イベント
	 */
	public void mouseClicked(MouseEvent e) {
		// 排他制御
		if (isAction.compareAndExchange(false, true)) {
			return;
		}
		// アクション実行
		doMouseClicked(e);
		// 排他制御解除
		isAction.set(false);
	}

	/**
	 * マウスクリック事前アクション
	 * @param e イベント
	 */
	public void mousePressed(MouseEvent e) {
		// 排他制御
		if (isAction.compareAndExchange(false, true)) {
			return;
		}
		// アクション実行
		doMousePressed(e);
		// 排他制御解除
		isAction.set(false);
	}

	/**
	 * マウスクリック事後アクション
	 * @param e イベント
	 */
	public void mouseReleased(MouseEvent e) {
		// 排他制御
		if (isAction.compareAndExchange(false, true)) {
			return;
		}
		// アクション実行
		doMouseReleased(e);
		// 排他制御解除
		isAction.set(false);
	}

	/**
	 * 現在アクションによるポップアップを表示中か判定します
	 * @return 判定結果
	 */
	public final boolean isShow() {
		return isAction.get();
	}

	/** {@inheritDoc} */
	@Override
	public final String toString() {
		return name;
	}

}

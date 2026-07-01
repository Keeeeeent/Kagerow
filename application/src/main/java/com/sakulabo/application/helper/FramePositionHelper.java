package com.sakulabo.application.helper;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.lang.StackWalker.Option;
import java.util.Objects;

import javax.naming.NamingException;
import javax.swing.JFrame;

import com.sakulabo.application.common.initializer.GraphicComponent;
import com.sakulabo.core.Kagerow.Utilities.KagerowUtilities;
import com.sakulabo.regulation.annotation.KagerowComponent;
import com.sakulabo.regulation.annotation.UseJITCompiler;
import com.sakulabo.regulation.annotation.UseJITCompiler.JITCompilerOption;

/**
 * Frameのポジションを管理するヘルパーです
 * 
 * @author keeeeeent
 */
@KagerowComponent
@GraphicComponent
public final class FramePositionHelper {

	/** ウィンドウ（X軸） */
	private final static String WIN_X = "winX";
	/** ウィンドウ（Y軸） */
	private final static String WIN_Y = "winY";
	/** ウィンドウ（幅） */
	private final static String WIN_W = "winW";
	/** ウィンドウ（高さ） */
	private final static String WIN_H = "winH";

	/** モニター（X） */
	private final static String MON_X = "monX";
	/** モニター（Y） */
	private final static String MON_Y = "monY";
	/** モニター（幅） */
	private final static String MON_W = "monW";
	/** モニター（高さ） */
	private final static String MON_H = "monH";

	/**
	 * JFrameの初期表示位置とサイズを保存します
	 * @param frame フレーム
	 * @throws NamingException 
	 */
	@UseJITCompiler(JITCompilerOption.DONTINLINE)
	public void saveFramePosition(JFrame frame) throws NamingException {

		// 呼び出し元クラスを取得
		Class<?> clazz = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).getCallerClass();
		// モニターの設定を取得
		GraphicsConfiguration graphicsConfiguration = frame.getGraphicsConfiguration();
		Rectangle monRectangle = graphicsConfiguration.getBounds();
		// フレームの設定を取得
		Rectangle winRectangle = frame.getBounds();

		// フレーム設定値を保存変数を初期化
		String winX, winY, winW, winH;
		// モニター設定値を保存変数を初期化
		String monX, monY, monW, monH;

		// フレーム設定値を設定
		winX = String.valueOf(winRectangle.x);
		winY = String.valueOf(winRectangle.y);
		winW = String.valueOf(winRectangle.width);
		winH = String.valueOf(winRectangle.height);

		// モニター設定値を設定
		monX = String.valueOf(monRectangle.x);
		monY = String.valueOf(monRectangle.y);
		monW = String.valueOf(monRectangle.width);
		monH = String.valueOf(monRectangle.height);

		// フレーム設定値を保存
		String namespace = clazz.getName();
		KagerowUtilities.setSetting(namespace, WIN_X, winX);
		KagerowUtilities.setSetting(namespace, WIN_Y, winY);
		KagerowUtilities.setSetting(namespace, WIN_W, winW);
		KagerowUtilities.setSetting(namespace, WIN_H, winH);
		KagerowUtilities.setSetting(namespace, MON_X, monX);
		KagerowUtilities.setSetting(namespace, MON_Y, monY);
		KagerowUtilities.setSetting(namespace, MON_W, monW);
		KagerowUtilities.setSetting(namespace, MON_H, monH);

	}

	/**
	 * JFrameの初期表示位置とサイズを設定します
	 * @param frame フレーム
	 * @throws NamingException 
	 */
	@UseJITCompiler(JITCompilerOption.DONTINLINE)
	public void lordFramePosition(JFrame frame) throws NamingException {

		// 呼び出し元クラスを取得
		Class<?> clazz = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).getCallerClass();

		// フレーム設定値を保存変数を初期化
		String winX, winY, winW, winH;
		// モニター設定値を保存変数を初期化
		String monX, monY, monW, monH;

		// フレーム設定値を保存
		String namespace = clazz.getName();
		winX = KagerowUtilities.getSetting(namespace, WIN_X);
		winY = KagerowUtilities.getSetting(namespace, WIN_Y);
		winW = KagerowUtilities.getSetting(namespace, WIN_W);
		winH = KagerowUtilities.getSetting(namespace, WIN_H);
		monX = KagerowUtilities.getSetting(namespace, MON_X);
		monY = KagerowUtilities.getSetting(namespace, MON_Y);
		monW = KagerowUtilities.getSetting(namespace, MON_W);
		monH = KagerowUtilities.getSetting(namespace, MON_H);

		if (Objects.isNull(winX) ||
				Objects.isNull(winY) ||
				Objects.isNull(winW) ||
				Objects.isNull(winH) ||
				Objects.isNull(monX) ||
				Objects.isNull(monY) ||
				Objects.isNull(monW) ||
				Objects.isNull(monH)) {

			// 前回設定を設定していない場合、デフォルトモニターの中央に表示
			frame.setLocationRelativeTo(null);

		} else {

			// 設定値が全て保存済みの場合、モニターの位置を設定

			// ウィンドウの位置生成
			Rectangle savedWin = new Rectangle(
					Integer.parseInt(winX),
					Integer.parseInt(winY),
					Integer.parseInt(winW),
					Integer.parseInt(winH));
			// モニター位置生成
			Rectangle savedMon = new Rectangle(
					Integer.parseInt(monX),
					Integer.parseInt(monY),
					Integer.parseInt(monW),
					Integer.parseInt(monH));

			// モニター環境を取得
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			// 全てのモニターを取得
			for (GraphicsDevice device : ge.getScreenDevices()) {
				// モニターの設定を取得
				Rectangle currentMon = device.getDefaultConfiguration().getBounds();
				// モニターが一致していれば（位置・サイズで比較）
				if (currentMon.equals(savedMon)) {
					// savedWin がこのモニター内に収まっていれば使う
					if (currentMon.contains(savedWin)) {
						frame.setBounds(savedWin);
					}
					break;
				}
			}
		}
	}

}

package com.sakulabo.application.common.mixin;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.lang.StackWalker.Option;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.ref.Cleaner;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;
import javax.swing.undo.UndoManager;

import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;
import com.sakulabo.regulation.annotation.UseJITCompiler;
import com.sakulabo.regulation.annotation.UseJITCompiler.JITCompilerOption;

/**
 * GUI向けJTextComponentミックスイン実装インターフェイスです
 * 
 * @author keeeeeent
 */
public interface JTextComponentMixin extends AppMixin {

	/** ハンドラーキャッシュインスタンス（undo） */
	public static Map<JTextComponentMixin, Map<JTextComponent, UndoManager>> HANDLER = new ConcurrentHashMap<>();
	/** ハンドラーキャッシュインスタンス（ショートカット） */
	public static Map<JTextComponentMixin, List<JTextComponent>> SHORT_CUT_HANDLER = new ConcurrentHashMap<>();
	/** クリーナーインスタンス */
	public static Cleaner CLEANER = Cleaner.create();
	/** Undoアクション名称 */
	public static final String UNDO_ACTION_NAME = "Undo";
	/** Undoアクションマップ名称 */
	public static final String UNDO_ACTION_MAP_NAME = "customUndo";

	/**
	 * JTextComponentMixinの設定を指定します
	 */
	@Documented
	@Target({ ElementType.FIELD })
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Setting {
	}

	/**
	 * デフォルトショートカットを設定します
	 */
	@UseJITCompiler(JITCompilerOption.DONTINLINE)
	public default void applyMacKeyBindings() {

		// 呼び出し元クラス情報取得
		Class<?> clazz = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).getCallerClass();

		for (Field f : clazz.getDeclaredFields()) {
			if (f.isAnnotationPresent(JTextComponentMixin.Setting.class)) {
				try {
					// アクセス制限解除
					f.setAccessible(true);
					// インスタンス取得
					Object target = f.get(this);
					// 管理対象へ追加
					List<JTextComponent> targetList = SHORT_CUT_HANDLER.computeIfAbsent(this, _ -> new Vector<>());
					if (target instanceof JTextComponent component) {
						// 既に登録済みの場合早期リターン
						if (targetList.contains(component)) {
							continue;
						}
						// コンポーネントの設定実施
						applyMacKeyBindings(component);
					}
				} catch (Exception e) {
					KagerowLogger.newAppLogger().err(e);
				}
			}
		}

	}

	/**
	 * Undoを設定します
	 */
	@UseJITCompiler(JITCompilerOption.DONTINLINE)
	public default void setUndo() {

		// 呼び出し元クラス情報取得
		Class<?> clazz = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).getCallerClass();

		for (Field f : clazz.getDeclaredFields()) {
			if (f.isAnnotationPresent(JTextComponentMixin.Setting.class)) {
				try {
					// アクセス制限解除
					f.setAccessible(true);
					// インスタンス取得
					Object target = f.get(this);
					// 管理対象へ追加
					Map<JTextComponent, UndoManager> targetList = HANDLER.computeIfAbsent(this, _ -> new HashMap<>());
					if (target instanceof JTextComponent component) {
						// 既に登録済みの場合早期リターン
						if (targetList.containsKey(component)) {
							continue;
						}
						// コンポーネントの設定実施
						setting(component);
					}
				} catch (Exception e) {
					KagerowLogger.newAppLogger().err(e);
				}
			}
		}

	}

	/**
	 * コンポーネントのショートカット設定処理を行います
	 * @param target 対象インスタンス
	 */
	private void applyMacKeyBindings(JTextComponent target) {

		// クロスプラットフォーム向けのマスク取得
		int mask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
		// 登録済みショートカット取得
		InputMap im = target.getInputMap();
		// 登録済みアクションを取得
		ActionMap am = target.getActionMap();
		// ショートカット上書き
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, mask), DefaultEditorKit.copyAction);
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, mask), DefaultEditorKit.pasteAction);
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, mask), DefaultEditorKit.cutAction);
		// アクションを上書き
		am.put(DefaultEditorKit.copyAction, new DefaultEditorKit.CopyAction());
		am.put(DefaultEditorKit.pasteAction, new DefaultEditorKit.PasteAction());
		am.put(DefaultEditorKit.cutAction, new DefaultEditorKit.CutAction());

	}

	/**
	 * コンポーネントのUndo設定処理を行います
	 * @param target 対象インスタンス
	 */
	private void setting(JTextComponent target) {

		// Undoインスタンス生成
		final UndoManager undoManager = new UndoManager();

		// Documentの変更をUndoManagerに登録
		target.getDocument().addUndoableEditListener(undoManager);

		// Undoアクション生成
		AbstractAction undoAction = new AbstractAction(UNDO_ACTION_NAME) {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (undoManager.canUndo()) {
					undoManager.undo();
				}
			}
		};

		// Undoショートカット生成（Ctrl+Z）
		KeyStroke undoKey = KeyStroke.getKeyStroke(
				KeyEvent.VK_Z,
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());

		// アクションの登録
		target.getInputMap(JComponent.WHEN_FOCUSED).put(undoKey, UNDO_ACTION_MAP_NAME);
		target.getActionMap().put(UNDO_ACTION_MAP_NAME, undoAction);

		// キャッシュを取得
		Map<JTextComponent, UndoManager> cache = HANDLER.get(this);

		// キャッシュにUndoを追加
		cache.put(target, undoManager);

		// クリーナー設定
		CLEANER.register(target, () -> cache.remove(target));

	}

	/**
	 * Undoの無効化を行い、UIリフレッシュ処理を実行します
	 * UIリフレッシュ後、Undoは再度有効化されます
	 * @param ignore Undo無効化対象外インスタンス
	 */
	public default void refresh(JTextComponent... ignore) {

		// 対象がセット生成
		final Set<JTextComponent> ignoreList = new HashSet<>(Arrays.asList(ignore));
		// キャッシュを取得
		final Map<JTextComponent, UndoManager> cache = HANDLER.get(this);

		try {
			// UndoManager無効化
			for (JTextComponent undoTarget : cache.keySet()) {
				if (!ignoreList.contains(undoTarget)) {
					// 対象を取得
					UndoManager undo = cache.get(undoTarget);
					if (Objects.nonNull(undo)) {
						// DocumentからUndoManagerを削除
						undoTarget.getDocument().removeUndoableEditListener(undo);
					}
				}
			}
			// UIリフレッシュ処理
			refreshUndo();
		} catch (Exception e) {
			KagerowLogger.newAppLogger().err(e);
		} finally {
			// UndoManager有効化
			for (JTextComponent undoTarget : cache.keySet()) {
				if (!ignoreList.contains(undoTarget)) {
					// 対象を取得
					UndoManager undo = cache.get(undoTarget);
					if (Objects.nonNull(undo)) {
						// DocumentからUndoManagerを削除
						undoTarget.getDocument().addUndoableEditListener(undo);
					}
				}
			}
		}

	}

	/**
	 * 指定されたコンポーネントのUndoをリセットします
	 * @param ignore Undo無効化対象外インスタンス
	 */
	public default void resetUndo(JTextComponent... ignore) {

		// 対象がセット生成
		final Set<JTextComponent> ignoreList = new HashSet<>(Arrays.asList(ignore));
		// キャッシュを取得
		final Map<JTextComponent, UndoManager> cache = HANDLER.get(this);

		// UndoManagerリセット
		for (JTextComponent undoTarget : cache.keySet()) {
			if (!ignoreList.contains(undoTarget)) {
				// 対象を取得
				UndoManager undo = cache.get(undoTarget);
				if (Objects.nonNull(undo)) {
					undo.discardAllEdits();
				}
			}
		}

	}

	/**
	 * UIリフレッシュ処理を提供します
	 * @throws Exception 予期せぬ例外
	 */
	public void refreshUndo() throws Exception;

}

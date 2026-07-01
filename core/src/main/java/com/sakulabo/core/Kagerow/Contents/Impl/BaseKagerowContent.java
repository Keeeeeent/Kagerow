package com.sakulabo.core.Kagerow.Contents.Impl;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.naming.Context;
import javax.naming.NamingException;

import com.sakulabo.core.Common.StringUtils;
import com.sakulabo.core.Kagerow.Contents.KagerowContents.KagerowContentEventInfo;
import com.sakulabo.core.Kagerow.Contents.KagerowContents.KagerowContentEventKind;
import com.sakulabo.core.Kagerow.Contents.KagerowContents.KagerowContentEventListener;

/**
 * Kagerowアプリケーションのコンテンツ基底クラスです
 * 
 * @author keeeeeent
 */
abstract class BaseKagerowContent implements Context {

	/** リスナー格納メモリ */
	protected final Map<KagerowContentEventKind, List<KagerowContentEventListener>> _LISTENER = new ConcurrentHashMap<>();

	/**
	 * リスナーの登録を行います
	 * リスナーがnullの場合このメソッドは例外をスローします
	 * @param kind 登録種別
	 * @param listener 登録リスナー
	 */
	public void addListener(KagerowContentEventKind kind, KagerowContentEventListener listener) {
		Objects.requireNonNull(kind);
		Objects.requireNonNull(listener);
		_LISTENER.computeIfAbsent(kind, _ -> new CopyOnWriteArrayList<>()).add(listener);
	}

	/**
	 * イベントリスナー起動処理
	 * @param kind イベント種別
	 * @param content 対象となったコンテンツ
	 */
	protected void callListener(KagerowContentEventKind kind, Object content) {
		List<KagerowContentEventListener> listeners = _LISTENER.get(kind);
		String name = StringUtils.DEFAULT;
		try {
			name = getNameInNamespace();
		} catch (NamingException ignore) {
		}
		KagerowContentEventInfo info = new KagerowContentEventInfo(kind, name, content);
		if (Objects.nonNull(listeners)) {
			for (KagerowContentEventListener listener : listeners) {
				try {
					listener.accept(info);
				} catch (Throwable e) {
					listener.accept(e, info);
				}
			}
		}
	}

}

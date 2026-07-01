package com.sakulabo.core.Kagerow.Context.Impl;

import java.lang.StackWalker.Option;
import java.lang.StackWalker.StackFrame;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.naming.Binding;
import javax.naming.CannotProceedException;
import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.NoPermissionException;
import javax.naming.NotContextException;
import javax.naming.OperationNotSupportedException;
import javax.naming.PartialResultException;
import javax.naming.directory.SchemaViolationException;

import com.sakulabo.core.Common.NamingEnumerationImpl;
import com.sakulabo.core.Common.URINameParser;
import com.sakulabo.core.Kagerow.Contents.KagerowContents;
import com.sakulabo.core.Kagerow.Context.KagerowContexts;
import com.sakulabo.core.Kagerow.Context.KagerowContexts.KagerowContextEventInfo;
import com.sakulabo.core.Kagerow.Context.KagerowContexts.KagerowContextEventKind;
import com.sakulabo.core.Kagerow.Context.KagerowContexts.KagerowContextEventListener;
import com.sakulabo.core.Kagerow.Exception.ApplicationError;
import com.sakulabo.core.Processor.jmx.AppJMXInitializer;
import com.sakulabo.core.Processor.jmx.BaseKagerowJMX;
import com.sakulabo.regulation.annotation.UseJITCompiler;
import com.sakulabo.regulation.annotation.UseJITCompiler.JITCompilerOption;

/**
 * Kagerowアプリケーションのコンテキスト基底クラスです
 * 
 * @author keeeeeent
 * @param <T> コンテキストの型情報
 */
abstract class BaseKagerowContext<T extends Context> implements Context {

	/** コンテキストメモリ */
	protected final Map<Name, T> _CONTEXT;
	/** コンテキスト環境変数メモリ */
	protected final Map<String, String> _ENV;
	/** コンテキスト名称 */
	protected Name _NAME;
	/** リスナー格納メモリ */
	protected final Map<KagerowContextEventKind, List<KagerowContextEventListener>> _LISTENER = new ConcurrentHashMap<>();

	/**
	 * 共通コンストラクタ
	 * @param _CONTEXT コンテキストメモリ参照先
	 * @param _ENV コンテキスト環境変数メモリ参照先
	 * @param _NAME コンテキスト名称
	 * @throws ApplicationError 名称生成失敗
	 */
	protected BaseKagerowContext(Map<Name, T> _CONTEXT, Map<String, String> _ENV, String _NAME) {
		// コンテキスト登録
		this._CONTEXT = _CONTEXT;
		// コンテキスト変数登録
		this._ENV = _ENV;
		try {
			// 名称の登録
			this._NAME = new CompositeName(_NAME);
		} catch (Exception e) {
			throw new ApplicationError(e);
		}
	}

	/**
	 * MXBeanの登録をします
	 * @param target コンテキストインスタンス
	 */
	protected final void registMXBean(BaseKagerowJMX target) {
		try {
			// JMXの登録
			AppJMXInitializer.registMXBean(target);
		} catch (Exception e) {
			throw new ApplicationError(e);
		}
	}

	/**
	 * 対象モジュールが許可されたモジュールか確認します
	 * @throws NoPermissionException 呼び出しモジュール不正
	 */
	@UseJITCompiler(JITCompilerOption.DONTINLINE)
	protected void checkModule() throws NoPermissionException {
		// 呼び出し元の取得
		StackWalker walker = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE);
		Class<?> target = walker.walk(stream -> {
			return stream.skip(2).findFirst()
					.map(StackFrame::getDeclaringClass)
					.orElse(null);
		});
		// モジュールの取得
		Module targetModule = target.getModule();
		Module currentModule = KagerowContextImpl.class.getModule();
		// モジュールの同一性をレイヤー含め確認
		if (Objects.equals(targetModule, currentModule)
				&& Objects.equals(targetModule.getLayer(), currentModule.getLayer())) {
			return;
		}
		throw new NoPermissionException();
	}

	/** {@inheritDoc} */
	@Override
	public String getNameInNamespace() throws NamingException {
		return Objects.toString(_NAME);
	}

	/** {@inheritDoc} */
	@Override
	public Object lookup(Name name) throws NamingException {
		Object result = _CONTEXT.get(name);
		if (Objects.isNull(result)) {
			throw new NotContextException(Objects.toString(name));
		}
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public Object lookup(String name) throws NamingException {
		Name named = new CompositeName(name);
		return lookup(named);
	}

	/** {@inheritDoc} */
	@Override
	@SuppressWarnings({ "unchecked" })
	@UseJITCompiler(JITCompilerOption.DONTINLINE)
	public void bind(Name name, Object obj) throws NamingException {

		// 呼び出し元チェック
		checkModule();

		Object result = _CONTEXT.get(name);
		if (Objects.nonNull(result)) {
			// 既にバインド済みの場合、例外をスロー
			throw new NameAlreadyBoundException(Objects.toString(name));
		} else if (obj instanceof KagerowContexts content) {
			// バインドされていない場合、インスタンスを検証しセット
			((Map<Name, KagerowContexts<T>>) _CONTEXT).put(name, content);
		} else if (obj instanceof KagerowContents content) {
			// バインドされていない場合、インスタンスを検証しセット
			((Map<Name, KagerowContents>) _CONTEXT).put(name, content);
		} else {
			// 上記以外の場合、バインド不可として例外をスロー
			throw new SchemaViolationException(obj.getClass().getCanonicalName());
		}

		// リスナー起動
		callListener(KagerowContextEventKind.CREATE);

	}

	/** {@inheritDoc} */
	@Override
	@UseJITCompiler(JITCompilerOption.DONTINLINE)
	public void bind(String name, Object obj) throws NamingException {

		// 呼び出し元チェック
		checkModule();

		Name named = new CompositeName(name);
		bind(named, obj);
	}

	/** {@inheritDoc} */
	@Override
	@SuppressWarnings({ "unchecked" })
	@UseJITCompiler(JITCompilerOption.DONTINLINE)
	public void rebind(Name name, Object obj) throws NamingException {

		// 呼び出し元チェック
		checkModule();

		Object result = _CONTEXT.get(name);
		if (Objects.isNull(result)) {
			// 未バインドの場合、例外をスロー
			throw new NameNotFoundException(Objects.toString(name));
		} else if (obj instanceof KagerowContexts content) {
			// バインドされていない場合、インスタンスを検証しセット
			((Map<Name, KagerowContexts<T>>) _CONTEXT).put(name, content);
		} else if (obj instanceof KagerowContents content) {
			// バインドされていない場合、インスタンスを検証しセット
			((Map<Name, KagerowContents>) _CONTEXT).put(name, content);
		} else {
			// 上記以外の場合、バインド不可として例外をスロー
			throw new SchemaViolationException();
		}

		// リスナー起動
		callListener(KagerowContextEventKind.RENAME);

	}

	/** {@inheritDoc} */
	@Override
	@UseJITCompiler(JITCompilerOption.DONTINLINE)
	public void rebind(String name, Object obj) throws NamingException {

		// 呼び出し元チェック
		checkModule();

		Name named = new CompositeName(name);
		rebind(named, obj);
	}

	/** {@inheritDoc} */
	@Override
	@UseJITCompiler(JITCompilerOption.DONTINLINE)
	public void unbind(Name name) throws NamingException {

		// 呼び出し元チェック
		checkModule();

		Object result = _CONTEXT.get(name);
		if (Objects.isNull(result)) {
			// 未バインドの場合、例外をスロー
			throw new NameNotFoundException(Objects.toString(name));
		} else {
			// バインドされている合、インスタンス消去
			_CONTEXT.remove(name);
		}

		// リスナー起動
		callListener(KagerowContextEventKind.DELETE);

	}

	/** {@inheritDoc} */
	@Override
	@UseJITCompiler(JITCompilerOption.DONTINLINE)
	public void unbind(String name) throws NamingException {

		// 呼び出し元チェック
		checkModule();

		Name named = new CompositeName(name);
		unbind(named);
	}

	/** {@inheritDoc} */
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@UseJITCompiler(JITCompilerOption.DONTINLINE)
	public void rename(Name oldName, Name newName) throws NamingException {

		// 呼び出し元チェック
		checkModule();

		if (!_CONTEXT.containsKey(oldName)) {
			// 未バインドの場合、例外をスロー
			throw new NameNotFoundException(Objects.toString(oldName));
		} else if (_CONTEXT.containsKey(newName)) {
			// リネーム先の名称が既にバインド済みの場合、例外をスロー
			throw new NameAlreadyBoundException(Objects.toString(oldName));
		} else {
			// 上記以外の場合、リネームを実行
			// まずは一度バインド済みのインスタンスを削除
			Context instance = _CONTEXT.remove(oldName);
			if (instance instanceof KagerowContexts content) {
				// 新しいキーにてインスタンスを再設定
				((Map<Name, KagerowContexts>) _CONTEXT).put(newName, content);
			} else if (instance instanceof KagerowContents content) {
				// 新しいキーにてインスタンスを再設定
				((Map<Name, KagerowContents>) _CONTEXT).put(newName, content);
			} else {
				// 非対応インスタンスの場合はサポート対象外として例外通知
				throw new OperationNotSupportedException();
			}
		}

		// リスナー起動
		callListener(KagerowContextEventKind.UPDATE);

	}

	/** {@inheritDoc} */
	@Override
	@UseJITCompiler(JITCompilerOption.DONTINLINE)
	public void rename(String oldName, String newName) throws NamingException {

		// 呼び出し元チェック
		checkModule();

		Name oldNamed = new CompositeName(oldName);
		Name newNamed = new CompositeName(newName);
		rename(oldNamed, newNamed);
	}

	/** {@inheritDoc} */
	@Override
	public NamingEnumeration<NameClassPair> list(Name name) throws NamingException {
		List<NameClassPair> tmpList = _CONTEXT.entrySet().stream()
				.map(m -> new NameClassPair(m.getKey().toString(), m.getValue().getClass().getCanonicalName()))
				.toList();
		return new NamingEnumerationImpl<>(tmpList);
	}

	/** {@inheritDoc} */
	@Override
	public NamingEnumeration<NameClassPair> list(String name) throws NamingException {
		Name named = new CompositeName(name);
		return list(named);
	}

	/** {@inheritDoc} */
	@Override
	public NamingEnumeration<Binding> listBindings(Name name) throws NamingException {
		List<Binding> tmpList = _CONTEXT.entrySet().stream()
				.map(m -> new Binding(m.getKey().toString(), m.getValue().getClass().getCanonicalName(), m.getValue()))
				.toList();
		return new NamingEnumerationImpl<>(tmpList);
	}

	/** {@inheritDoc} */
	@Override
	public NamingEnumeration<Binding> listBindings(String name) throws NamingException {
		Name named = new CompositeName(name);
		return listBindings(named);
	}

	/** {@inheritDoc} */
	@Override
	@Deprecated
	public void destroySubcontext(Name name) throws NamingException {
		throw new OperationNotSupportedException();
	}

	/** {@inheritDoc} */
	@Override
	@Deprecated
	public void destroySubcontext(String name) throws NamingException {
		throw new OperationNotSupportedException();
	}

	/** {@inheritDoc} */
	@Override
	@Deprecated
	public Context createSubcontext(Name name) throws NamingException {
		throw new OperationNotSupportedException();
	}

	/** {@inheritDoc} */
	@Override
	@Deprecated
	public Context createSubcontext(String name) throws NamingException {
		throw new OperationNotSupportedException();
	}

	/** {@inheritDoc} */
	@Override
	public Object lookupLink(Name name) throws NamingException {
		throw new OperationNotSupportedException();
	}

	/** {@inheritDoc} */
	@Override
	public Object lookupLink(String name) throws NamingException {
		throw new OperationNotSupportedException();
	}

	/** {@inheritDoc} */
	@Override
	public NameParser getNameParser(Name name) throws NamingException {
		return new URINameParser(name);
	}

	/** {@inheritDoc} */
	@Override
	public NameParser getNameParser(String name) throws NamingException {
		return new URINameParser(name);
	}

	/** {@inheritDoc} */
	@Override
	@Deprecated
	public Name composeName(Name name, Name prefix) throws NamingException {
		throw new OperationNotSupportedException();
	}

	/** {@inheritDoc} */
	@Override
	@Deprecated
	public String composeName(String name, String prefix) throws NamingException {
		throw new OperationNotSupportedException();
	}

	/** {@inheritDoc} */
	@Override
	@UseJITCompiler(JITCompilerOption.DONTINLINE)
	public Object addToEnvironment(String propName, Object propVal) throws NamingException {

		// 呼び出し元チェック
		checkModule();

		if (Objects.isNull(propName) || Objects.isNull(propVal)) {
			throw new CannotProceedException();
		}

		// リスナー起動
		callListener(KagerowContextEventKind.ADD_ENV);

		return _ENV.put(propName, Objects.toString(propVal));
	}

	/** {@inheritDoc} */
	@Override
	@UseJITCompiler(JITCompilerOption.DONTINLINE)
	public Object removeFromEnvironment(String propName) throws NamingException {

		// 呼び出し元チェック
		checkModule();

		if (Objects.isNull(propName)) {
			throw new CannotProceedException();
		}

		// リスナー起動
		callListener(KagerowContextEventKind.REMOVE_ENV);

		return _ENV.remove(propName);
	}

	/** {@inheritDoc} */
	@Override
	public Hashtable<?, ?> getEnvironment() throws NamingException {
		return new Hashtable<>(_ENV);
	}

	/** {@inheritDoc} */
	@Override
	@UseJITCompiler(JITCompilerOption.DONTINLINE)
	public void close() throws NamingException {

		// 呼び出し元チェック
		checkModule();

		_ENV.clear();
		List<Exception> exceptions = new ArrayList<>();
		for (Map.Entry<Name, ? extends Context> entry : _CONTEXT.entrySet()) {
			try {
				entry.getValue().close();
			} catch (Exception e) {
				exceptions.add(e);
			}
		}
		if (!exceptions.isEmpty()) {
			PartialResultException e = new PartialResultException();
			exceptions.stream().forEach(e::addSuppressed);
			throw e;
		}

		// リスナー起動
		callListener(KagerowContextEventKind.CLOSE);

	}

	/**
	 * コンテキストに指定された名称のエントリが存在するか確認します
	 * Nameインスタンスへ変換不可能な文字列が指定された場合、このメソッドはfalseを返却します
	 * nameがnullの場合もfalseを返却します
	 * @param name エントリ名称
	 * @return 存在有無
	 */
	public final boolean isExist(Name name) {
		if (Objects.isNull(name)) {
			return false;
		}
		return _CONTEXT.containsKey(name);
	}

	/**
	 * コンテキストに指定された名称のエントリが存在するか確認します
	 * このメソッドはnameがnullの場合falseを返却します
	 * @param name エントリ名称
	 * @return 存在有無
	 */
	public final boolean isExist(String name) {
		if (Objects.isNull(name)) {
			return false;
		}
		Name named;
		try {
			named = new CompositeName(name);
		} catch (InvalidNameException e) {
			return false;
		}
		return isExist(named);
	}

	/**
	 * リスナーの登録を行います
	 * リスナーがnullの場合このメソッドは例外をスローします
	 * @param kind 登録種別
	 * @param listener 登録リスナー
	 */
	public void addListener(KagerowContextEventKind kind, KagerowContextEventListener listener) {
		Objects.requireNonNull(kind);
		Objects.requireNonNull(listener);
		_LISTENER.computeIfAbsent(kind, _ -> new CopyOnWriteArrayList<>()).add(listener);
	}

	/**
	 * イベントリスナー起動処理
	 * @param kind イベント種別
	 */
	protected void callListener(KagerowContextEventKind kind) {
		List<KagerowContextEventListener> listeners = _LISTENER.get(kind);
		KagerowContextEventInfo info = new KagerowContextEventInfo(
				kind, _NAME.toString());
		if (Objects.nonNull(listeners)) {
			for (KagerowContextEventListener listener : listeners) {
				try {
					listener.call();
				} catch (Throwable e) {
					listener.accept(e, info);
				}
			}
		}
	}

}

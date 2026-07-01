package com.sakulabo.core.Kagerow.Context.Impl;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import javax.naming.Name;
import javax.naming.NamingException;

import com.sakulabo.core.Kagerow.Context.KagerowContexts;
import com.sakulabo.core.Kagerow.Context.KagerowSettingContext;
import com.sakulabo.core.Kagerow.Context.KagerowVirtualFileContext;
import com.sakulabo.core.Processor.config.ContextConfigurationLorder;
import com.sakulabo.core.Provides.InitialContextFactoryProvider;

/**
 * Kagerowアプリケーションのコンテキスト実装クラスです
 * 
 * @author keeeeeent
 */
public sealed class KagerowContextImpl extends BaseKagerowContext<KagerowContexts<?>>
		implements KagerowContexts<KagerowContexts<?>>
		permits InitialContextFactoryProvider {

	/** セキュリティコンテキスト生成キー */
	public static final String SECURE_KEY = "KagerowContextImpl.secure.key";
	/** コンテキスト環境変数メモリ */
	private static final Map<String, String> _ENV;
	static {
		_ENV = ContextConfigurationLorder.getInstance().CONTEXT_ENV();
	}
	/** ファクトリクラス名称 */
	public static final String FACTORY_SPI_NAME = InitialContextFactoryProvider.class.getName();

	/**
	 * ファクトリSPI向けコンストラクタ
	 */
	protected KagerowContextImpl() {
		// 初期化
		super(new ConcurrentHashMap<>(), _ENV, KagerowContexts._NAME);
	}

	/**
	 * ファクトリSPI向けファクトリメソッド
	 * @param password アプリケーション暗号化解除パスワード
	 * @return コンテキスト
	 * @throws NamingException コンテキスト生成失敗
	 */
	protected final static KagerowContextImpl getInstance(String password) throws NamingException {

		// Kagerowコンテキスト生成
		KagerowContextImpl context = new KagerowContextImpl();

		// Kagerowセッテイング管理システム起動&登録
		{
			KagerowSettingContextImpl cont = new KagerowSettingContextImpl();
			regist(context, cont);

			// Kagerowセキュリティシステム起動&登録
			if (Objects.nonNull(password)) {
				regist(context, new KagerowSecurityContextImpl(password, cont));
			}
		}

		// Kagerowバーチャルファイルシステム起動&登録
		regist(context, new KagerowVirtualFileContextImpl());

		// Kagerowプラグインシステム起動&登録
		regist(context, new KagerowPluginPackageContextImpl());

		// Kagerowキャッシュシステム起動&登録
		regist(context, new KagerowCacheContextImpl());

		return context;
	}

	/**
	 * セキュアブートに変更します
	 * @param password パスワード
	 * @throws NamingException コンテキスト生成失敗
	 */
	public void changeToSecureBoot(String password) throws NamingException {

		// セキュア実行中の場合は何もせず処理を終了する
		if (KagerowSettingContextImpl.isSecure()) {
			return;
		}

		// Kagerowセッテイング管理システム起動&登録
		KagerowSettingContextImpl settingContextImpl = (KagerowSettingContextImpl) lookup(KagerowSettingContext._NAME);

		// Kagerowセキュリティシステム起動&登録
		KagerowSecurityContextImpl securityContextImpl = new KagerowSecurityContextImpl(
				Objects.requireNonNull(password), settingContextImpl);
		regist(this, securityContextImpl);

		// Kagerowセキュリテイシステム初期化
		KagerowVirtualFileContextImpl fileContextImpl = (KagerowVirtualFileContextImpl) lookup(
				KagerowVirtualFileContext._NAME);
		fileContextImpl.changeToSecureBoot();

	}

	/**
	 * コンテンツを登録します
	 * @param context コンテキスト
	 * @param content コンテンツコンテキスト
	 * @throws NamingException 登録失敗
	 */
	private static void regist(KagerowContextImpl context, KagerowContexts<?> content) throws NamingException {
		// コンテンツの登録
		context.bind(content.getNameInNamespace(), content);
	}

	/** {@inheritDoc} */
	@Override
	public String getNameInNamespace() throws NamingException {
		return KagerowContexts._NAME;
	}

	/** {@inheritDoc} */
	@Override
	public KagerowContexts<?> lookup(Name name) throws NamingException {
		if (super._NAME.equals(name)) {
			return this;
		}
		return (KagerowContexts<?>) super.lookup(name);
	}

	/** {@inheritDoc} */
	@Override
	public KagerowContexts<?> lookup(String name) throws NamingException {
		if (super._NAME.toString().equals(name)) {
			return this;
		}
		return (KagerowContexts<?>) super.lookup(name);
	}

	/**
	 * このコンテキストの名称インスタンスを取得します
	 * @return 名称インスタンス
	 */
	public Name getName() {
		return super._NAME;
	}

}

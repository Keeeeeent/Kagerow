package com.sakulabo.library.common;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;

import com.sakulabo.regulation.spi.PluginAdapter.PluginValidationException;

/**
 * プラグイン共通処理バンドル抽象クラス
 * 
 * @author keeeeeent
 */
public abstract class DefaultPlugin implements DataSelecter {

	/** プラグイン共通で参照可能なパラメータです */
	public static final String ID_KEY = "id";

	/**
	 * 指定されたパラメータからパスを生成します
	 * @param params パラメーター
	 * @param name パラメーター名称
	 * @return 生成パス
	 */
	protected final Path getPath(Map<String, String> params, String name) {
		Path result = null;
		try {
			String param = params.get(name);
			if (Objects.nonNull(param)) {
				result = Paths.get(param);
			}
		} catch (Exception e) {
			;
		}
		return result;
	}

	/**
	 * 指定されたパラメータから数値を生成します
	 * @param params パラメーター
	 * @param name パラメーター名称
	 * @return 数値
	 */
	protected final BigDecimal getNumber(Map<String, String> params, String name) {
		BigDecimal result = null;
		try {
			String param = params.get(name);
			if (Objects.nonNull(param)) {
				result = new BigDecimal(param);
			}
		} catch (Exception e) {
			;
		}
		return result;
	}

	/**
	 * 指定されたパラメータから真偽値を生成します
	 * @param params パラメーター
	 * @param name パラメーター名称
	 * @return 真偽値
	 */
	protected final Boolean getBoolean(Map<String, String> params, String name) {
		Boolean result = null;
		try {
			String param = params.get(name);
			if (Objects.nonNull(param)) {
				result = Boolean.valueOf(param);
			}
		} catch (Exception e) {
			;
		}
		return result;
	}

	/**
	 * 指定されたパラメータから文字コードを生成します
	 * @param params パラメーター
	 * @param name パラメーター名称
	 * @return 文字コード
	 */
	protected final Charset getCharset(Map<String, String> params, String name) {
		Charset result = null;
		try {
			String param = params.get(name);
			if (Objects.nonNull(param)) {
				result = Charset.forName(param);
			}
		} catch (Exception e) {
			;
		}
		return result;
	}

	/**
	 * 指定されたパラメータから生成可能な文字コードが使用可能か判定します
	 * @param params パラメーター
	 * @param name パラメーター名称
	 * @throws PluginValidationException バリデーションエラー
	 */
	protected final void validCharset(Map<String, String> params, String name) throws PluginValidationException {
		Object result = getCharset(params, name);
		if (Objects.isNull(result)) {
			throw new DefaultPluginValidationException(DefaultPluginMessage.E0001, new Object[] { name }, name);
		}
	}

	/**
	 * 指定されたパラメータから生成可能なパスが使用可能か判定します
	 * @param params パラメーター
	 * @param name パラメーター名称
	 * @throws PluginValidationException バリデーションエラー
	 */
	protected final void validPath(Map<String, String> params, String name) throws PluginValidationException {
		Path result = getPath(params, name);
		if (Objects.isNull(result)) {
			throw new DefaultPluginValidationException(DefaultPluginMessage.E0002, new Object[] { name }, name);
		}
		if (Files.notExists(result)) {
			throw new DefaultPluginValidationException(DefaultPluginMessage.E0004,
					new Object[] { result.toAbsolutePath() }, name);
		}
	}

	/**
	 * 指定されたパラメータから親ディレクトリが有効か判定します
	 * @param params パラメーター
	 * @param name パラメーター名称
	 * @throws PluginValidationException バリデーションエラー
	 */
	protected final void validParentPath(Map<String, String> params, String name) throws PluginValidationException {
		Path result = getPath(params, name);
		if (Objects.isNull(result)) {
			throw new DefaultPluginValidationException(DefaultPluginMessage.E0002, new Object[] { name }, name);
		}
		if (Objects.isNull(result.getParent())) {
			throw new DefaultPluginValidationException(DefaultPluginMessage.E0005, new Object[] { result }, name);
		}
		result = result.getParent();
		if (Files.notExists(result)) {
			throw new DefaultPluginValidationException(DefaultPluginMessage.E0004,
					new Object[] { result.toAbsolutePath() }, name);
		}
	}

	/**
	 * 指定されたパラメータから生成可能な数値が使用可能か判定します
	 * @param params パラメーター
	 * @param name パラメーター名称
	 * @throws PluginValidationException バリデーションエラー
	 */
	protected final void validNumber(Map<String, String> params, String name) throws PluginValidationException {
		Object result = getNumber(params, name);
		if (Objects.isNull(result)) {
			throw new DefaultPluginValidationException(DefaultPluginMessage.E0003, new Object[] { name }, name);
		}
	}

	/**
	 * プラグインIDを取得します
	 * @param params パラメーター
	 * @return プラグインID
	 */
	protected final String getId(Map<String, String> params) {
		return params.get(ID_KEY);
	}

}

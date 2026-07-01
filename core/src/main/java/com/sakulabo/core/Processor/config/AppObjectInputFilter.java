package com.sakulabo.core.Processor.config;

import java.io.ObjectInputFilter;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;

import com.sakulabo.core.Common.ErrorMessage;
import com.sakulabo.core.Kagerow.Contents.KagerowCacheContent.KagerowCacheObject.KagerowExecutionCache;
import com.sakulabo.core.Kagerow.Contents.KagerowVirtualFileContent.KagerowDataType;
import com.sakulabo.core.Kagerow.Contents.KagerowVirtualFileContent.KagerowVirtualFileObject.BasicFileObject;
import com.sakulabo.core.Kagerow.Contents.KagerowVirtualFileContent.KagerowVirtualFileObject.SecureFileObject;
import com.sakulabo.core.Kagerow.Exception.ApplicationError;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;

/**
 * アーカイブファイル群デシリアライズ時のホワイトリストを定義します
 * 
 * @author keeeeeent
 */
public final class AppObjectInputFilter implements ObjectInputFilter {

	/** デシリアライズ許可対象クラス情報 */
	private final static Set<String> ALLOWED_CLASS_SET = Set.of(
			BasicFileObject.class.getName(),
			SecureFileObject.class.getName(),
			KagerowExecutionCache.class.getName(),
			KagerowDataType[].class.getName(),
			KagerowDataType.class.getName(),
			"com.sakulabo.core.Kagerow.Contents.Impl.KagerowVirtualFileContentImpl$SecureFileObjectProxy",
			"com.sakulabo.core.Kagerow.Contents.Impl.KagerowVirtualFileContentImpl$SecureFileObjectProxy$SecureFileObjectProxy__Impl__");

	/** デシリアライズコンフィギュレーション */
	private final AppObjectInputFilterConfiguration CONFIG;

	/** グローバルフィルタインスタンス */
	private static final AppObjectInputFilter GLOBAL_FILTER = new AppObjectInputFilter();

	/** 外部からのインスタンス生成禁止 */
	private AppObjectInputFilter() {
		CONFIG = AppObjectInputFilterConfigurationLorder.getConfig();
	}

	/** {@inheritDoc} */
	@Override
	public Status checkInput(FilterInfo filterInfo) {

		// 深さ制限
		if (CONFIG.MAX_DEPTH() < filterInfo.depth()) {
			KagerowLogger.newAppLogger().log(
					Level.SEVERE,
					ErrorMessage.CODE_020.getMessage(String.valueOf(filterInfo.depth())),
					new Object[0]);
			return Status.REJECTED;
		}

		// 配列サイズ制限
		if (CONFIG.MAX_ARRAY_LENGTH() < filterInfo.arrayLength()) {
			KagerowLogger.newAppLogger().log(
					Level.SEVERE,
					ErrorMessage.CODE_021.getMessage(String.valueOf(filterInfo.arrayLength())),
					new Object[0]);
			return Status.REJECTED;
		}

		// クラスの制限
		Class<?> clazz = filterInfo.serialClass();
		if (Objects.isNull(clazz)) {
			return Status.UNDECIDED;
		} else {
			if (ALLOWED_CLASS_SET.contains(clazz.getName())
					|| CONFIG.ALLOWED_CLASS_LIST().contains(clazz.getName())
					|| clazz.getPackageName().startsWith("java.")
					|| clazz.getPackageName().startsWith("javax.")) {
				return Status.ALLOWED;
			}
		}

		// 対象外クラスを書き出し
		KagerowLogger.newAppLogger().log(
				Level.SEVERE,
				ErrorMessage.CODE_019.getMessage(clazz.getName()),
				new Object[0]);

		return Status.REJECTED;
	}

	/**
	 * デシリアライズ処理のホワイトリスト設定を行います
	 */
	public synchronized static void initialize() {
		// ブローバルフィルタが設定済みか確認
		ObjectInputFilter current = ObjectInputFilter.Config.getSerialFilter();
		// 未設定の場合フィルターを設定
		if (Objects.isNull(current)) {
			ObjectInputFilter.Config.setSerialFilter(GLOBAL_FILTER);
		} else {
			// 設定済みの場合、グローバルフィルタか判定
			if (current != GLOBAL_FILTER) {
				throw new ApplicationError(new IllegalStateException(ErrorMessage.CODE_018.getMessage()));
			}
		}
	}

}

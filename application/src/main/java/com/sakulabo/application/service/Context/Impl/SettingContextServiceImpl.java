package com.sakulabo.application.service.Context.Impl;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import javax.naming.NamingException;

import com.sakulabo.application.app.gui.CenterPanelParts.ScriptPanelParts.ScriptPanel;
import com.sakulabo.application.service.BaseService;
import com.sakulabo.application.service.Context.SettingContextService;
import com.sakulabo.core.Kagerow.Contents.KagerowSettingContent;
import com.sakulabo.core.Kagerow.Context.KagerowSettingContext;
import com.sakulabo.core.Kagerow.Utilities.KagerowUtilities;
import com.sakulabo.regulation.annotation.KagerowComponent;

/**
 * 設定コンテキストサービスの実装クラスです
 * 
 * @author keeeeeent
 */
@KagerowComponent
public class SettingContextServiceImpl extends BaseService implements SettingContextService {

	/** {@inheritDoc} */
	@Override
	public List<String> getSettingList(String nameSpace) throws NamingException {

		// コンテンツ取得
		KagerowSettingContent settingContent = getAndCreate(nameSpace);

		return settingContent.settingList();
	}

	/** {@inheritDoc} */
	@Override
	public String getSetting(String nameSpace, String propName) throws NamingException {

		// コンテンツ取得
		KagerowSettingContent settingContent = getAndCreate(nameSpace);

		return settingContent.lookup(propName);
	}

	/** {@inheritDoc} */
	@Override
	public KagerowSettingContent getAndCreate(String nameSpace) throws NamingException {

		// 結果返却用変数初期化
		KagerowSettingContent settingContent;

		// コンテキスト取得
		KagerowSettingContext context = KagerowUtilities.getContext(KagerowSettingContext._NAME);

		// コンテンツ取得
		try {
			settingContent = context.lookup(nameSpace);
		} catch (NamingException e) {
			settingContent = context.createSubcontext(nameSpace);
		}

		return settingContent;

	}

	/** {@inheritDoc} */
	@Override
	public UUID removePath(String path) throws NamingException {

		// 返却用変数
		UUID result = null;
		// 結果返却用変数初期化
		KagerowSettingContent settingContent;

		// コンテキスト取得
		KagerowSettingContext context = KagerowUtilities.getContext(KagerowSettingContext._NAME);

		// コンテンツ取得
		try {
			settingContent = context.lookup(ScriptPanel.class.getName());
		} catch (NamingException e) {
			settingContent = null;
		}

		// 対象を削除
		if (Objects.nonNull(settingContent)) {
			List<String> entryList = settingContent.settingKeySet();
			for (String entry : entryList) {
				String dat = settingContent.lookup(entry);
				if (dat.equals(path)) {
					settingContent.unbind(entry);
					result = UUID.fromString(entry);
					break;
				}
			}

		}

		return result;

	}

}

package com.sakulabo.core.Processor.archive;

import java.io.IOException;
import java.nio.file.Path;

import javax.crypto.SecretKey;
import javax.naming.NamingException;

import com.sakulabo.core.Kagerow.KagerowApplication;
import com.sakulabo.core.Kagerow.Contents.KagerowSecurityContent;
import com.sakulabo.core.Kagerow.Contents.KagerowSecurityContent.SecureObject;
import com.sakulabo.core.Kagerow.Contents.KagerowVirtualFileContent.KagerowVirtualFileObject;
import com.sakulabo.core.Kagerow.Contents.KagerowVirtualFileContent.KagerowVirtualFileObject.BasicFileObject;
import com.sakulabo.core.Kagerow.Contents.KagerowVirtualFileContent.KagerowVirtualFileObject.SecureFileObject;
import com.sakulabo.core.Kagerow.Context.KagerowSecurityContext;
import com.sakulabo.core.Kagerow.Exception.AppLogicException;
import com.sakulabo.core.Kagerow.Utilities.KagerowChunkLorder;
import com.sakulabo.core.Kagerow.Utilities.KagerowDBMode;

/**
 * 指定された範囲のデータを、アーカイブファイル群をから読み込みを行う機能を提供します
 * 
 * @author keeeeeent
 */
public final class SecureChunkLorder extends AppChunkLorder<KagerowVirtualFileObject>
		implements KagerowChunkLorder<KagerowVirtualFileObject> {

	/**
	 * デフォルトコンストラクタ
	 * @param mode ロードモード
	 * @param path KDBファイル出力先
	 */
	public SecureChunkLorder(KagerowDBMode mode, Path path) {
		super(mode, path);
	}

	/** {@inheritDoc} */
	@Override
	protected ChunkReader createChunkReader(KagerowVirtualFileObject chunk, long start, long end, long max)
			throws AppLogicException, IOException {

		ChunkReader result = switch (chunk) {
		case BasicFileObject basicFileObject -> {
			// 結果返却
			yield new BasicChunkReader(basicFileObject, start, end, max);
		}
		case SecureFileObject secureFileObject -> {
			try {
				// セキュリティコンテキスト取得
				KagerowSecurityContext kagerowSecurityContext = (KagerowSecurityContext) KagerowApplication
						.getInstance()
						.getContext().lookup(KagerowSecurityContext._NAME);
				// セキュリティコンテンツ取得
				KagerowSecurityContent kagerowSecurityContent = kagerowSecurityContext
						.lookup(secureFileObject.schema());
				// キー取得
				SecureObject secureObject = kagerowSecurityContent.lookup(secureFileObject);
				// 結果返却
				yield new SecureChunkReader(secureFileObject, (SecretKey) secureObject.resultKey(), start, end, max);
			} catch (NamingException e) {
				throw new AppLogicException("Context or Content not found", e);
			}
		}
		};

		return result;
	}

}

package com.sakulabo.core.Processor.archive;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.function.Consumer;

import com.sakulabo.core.Kagerow.Contents.KagerowVirtualFileContent.KagerowVirtualFileObject;
import com.sakulabo.core.Kagerow.Contents.KagerowVirtualFileContent.KagerowVirtualFileObject.SecureFileObject;
import com.sakulabo.core.Kagerow.Exception.AppLogicException;
import com.sakulabo.core.Kagerow.Utilities.KagerowChunkCreater;
import com.sakulabo.core.Kagerow.Utilities.KagerowChunkCreater.ChunkCreateMode;
import com.sakulabo.core.Kagerow.Utilities.KagerowVirtualFileCreater;

/**
 * Kagerow専用セキュア仮想DB生成実装クラスです
 * 
 * @author keeeeeent
 */
public final class SecureVirtualFileCreater extends AppVirtualFileCreater implements KagerowVirtualFileCreater {

	/**
	 * デフォルトコンストラクタ
	 * @param mode 実行モード
	 * @param schema スキーマファイル名
	 * @param path 入力ファイル
	 * @param charset 入力ファイル文字コード
	 * @param isHeader ヘッダー有無
	 * @param synonym テーブル名称のシノニム
	 * @param observer 進捗更新オブザーバー
	 * @throws IOException ファイル読み込み失敗、文字コード判定不可
	 */
	public SecureVirtualFileCreater(
			ChunkCreateMode mode,
			String schema,
			Path path,
			Charset charset,
			boolean isHeader,
			String synonym,
			Consumer<Double> observer) throws IOException {
		super(mode, schema, path, charset, isHeader, synonym, observer);
	}

	/** {@inheritDoc} */
	@Override
	public KagerowVirtualFileObject createVirtualFileObject() throws AppLogicException, IOException {
		// バインドオブジェクト生成
		KagerowChunkCreater<SecureFileObject> creater = KagerowChunkCreater.newSecureInstance(
				mode,
				schema,
				path,
				charset,
				isHeader);
		// オブザーバーの設定
		creater.setObserver(observer);
		// データ生成処理実行
		KagerowVirtualFileObject data = creater.create(synonym);
		return data;
	}

}
package com.sakulabo.application.common.mixin;

import java.io.File;
import java.lang.StackWalker.Option;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Objects;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.sakulabo.application.common.code.JFileChooserMixinProperty;
import com.sakulabo.regulation.annotation.UseJITCompiler;
import com.sakulabo.regulation.annotation.UseJITCompiler.JITCompilerOption;

/**
 * GUI向けJFileChooserミックスイン実装インターフェイスです
 * 
 * @author keeeeeent
 */
public interface JFileChooserMixin extends AppMixin {

	/** KSQLファイル拡張子リスト */
	public static final String[] KSQL_FILE_EXTENSIONS = { "ksql", "sksql" };

	/**
	 * JFileChooserの設定を指定します
	 */
	@Documented
	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	@Repeatable(JFileChooserMixin.Setting.SettingList.class)
	public @interface Setting {

		/**
		 * フィルタする拡張子の設定
		 * @return 拡張子文字列表現リスト
		 */
		JFileChooserMixinProperty value();

		/**
		 * フィルター有効化の設定
		 * @return 設定値
		 */
		boolean isPrimary();

		/**
		 * フィルター名称オプションの設定
		 * @return オプション設定文字列
		 */
		String option() default "";

		/**
		 * JFileChooserの設定コンテナです
		 */
		@Documented
		@Target(ElementType.TYPE)
		@Retention(RetentionPolicy.RUNTIME)
		public @interface SettingList {

			/**
			 * JFileChooserの設定項目保管コンテナ
			 * @return コンテナ
			 */
			Setting[] value();
		}

	}

	/**
	 * オーブン先のパスを取得します
	 * @return パス
	 */
	public File getPath();

	/**
	 * JFileChooserをJMenuに追加します
	 * @return JFileChooserインスタンス
	 */
	@UseJITCompiler(JITCompilerOption.DONTINLINE)
	public default JFileChooser createJFileChooser() {

		// 呼び出し元クラス情報取得
		Class<?> clazz = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).getCallerClass();

		// パス生成
		File targetPath = getPath();
		// ファイル選択インスタンス生成
		JFileChooser fileChooser;
		if (Objects.isNull(targetPath)) {
			fileChooser = new JFileChooser();
		} else {
			fileChooser = new JFileChooser(targetPath);
		}

		// アノテーション確認
		JFileChooserMixin.Setting[] settings = clazz.getDeclaredAnnotationsByType(JFileChooserMixin.Setting.class);
		for (JFileChooserMixin.Setting setting : settings) {

			// アノテーション解析
			JFileChooserMixinProperty extensions = setting.value();
			boolean isPrimary = setting.isPrimary();
			String option = setting.option();
			String[] extensionList = extensions.toExtensions();

			// アノテーション解析&オプション選択
			String overview;
			if (option.isEmpty()) {
				overview = extensions.toOverview();
			} else {
				overview = extensions.toOverview(option);
			}

			// フィルター生成
			FileFilter fileFilter = new FileNameExtensionFilter(overview, extensionList);
			// フィルター追加
			fileChooser.addChoosableFileFilter(fileFilter);
			// フィルター有効化（デフォルト）
			if (isPrimary) {
				fileChooser.setFileFilter(fileFilter);
			}

		}

		return fileChooser;

	}

}

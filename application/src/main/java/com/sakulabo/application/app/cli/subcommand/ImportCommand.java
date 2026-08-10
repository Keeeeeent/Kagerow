package com.sakulabo.application.app.cli.subcommand;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import javax.naming.NameAlreadyBoundException;

import com.sakulabo.application.app.cli.converter.ExistingFilePathConverter;
import com.sakulabo.application.model.Data.DataImportModel;
import com.sakulabo.application.service.Data.DataService;
import com.sakulabo.core.Kagerow.Utilities.KagerowUtilities;

import com.sakulabo.core.Kagerow.Utilities.KagerowChunkCreater.ChunkCreateMode;
import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * データ取り込み機能実装クラスです
 *
 * @author keeeeeent
 */
@Command(name = "import", mixinStandardHelpOptions = true)
public class ImportCommand implements Callable<Integer> {

	/** セキュアモード */
	@Option(names = { "--secure", "-s" }, negatable = true, description = "Secure mode. Default: ${DEFAULT-VALUE}")
	public boolean isSecure = false;
	/** KagerowChunkCreater実行モード */
	@Option(names = { "--mode", "-m" }, description = "Chunk create mode. Default: ${DEFAULT-VALUE}")
	public ChunkCreateMode mode = ChunkCreateMode.CSV;
	/** 格納先スキーマ */
	@Option(names = { "--schema", "-d" }, required = true, description = "Destination schema.")
	public String schema;
	/** インポートファイルパス */
	@Option(names = { "--path",
			"-p" }, required = true, description = "Import file path.", converter = ExistingFilePathConverter.class)
	public Path path;
	/** インポートファイル文字コード */
	@Option(names = { "--charset", "-c" }, description = "Import file charset. Default: ${DEFAULT-VALUE}")
	public Charset charset = StandardCharsets.UTF_8;
	/** インポートファイルヘッダーフラグ */
	@Option(names = { "--header",
			"-h" }, negatable = true, description = "The import file has a header. Default: ${DEFAULT-VALUE}")
	public boolean isHeader = true;
	/** インポートデータシノニム */
	@Option(names = { "--synonym", "-n" }, required = true, description = "Import data synonym.")
	public String synonym;

	/**
	 * 取り込み実行モデル
	 */
	private class model extends DataImportModel implements Consumer<Double> {

		/**
		 * デフォルトコンストラクタ
		 * @throws UnsupportedEncodingException TUIメインフレーム作成失敗
		 */
		model() throws UnsupportedEncodingException {
			// フィールド初期化
			this.isSecure = ImportCommand.this.isSecure;
			this.mode = ImportCommand.this.mode;
			this.schema = ImportCommand.this.schema;
			this.path = ImportCommand.this.path;
			this.charset = ImportCommand.this.charset;
			this.isHeader = ImportCommand.this.isHeader;
			this.synonym = ImportCommand.this.synonym;
			this.observer = this;
		}

		/** {@inheritDoc} */
		@Override
		public void accept(Double t) {
			int percent = t.intValue();
			int width = 31;
			int completed = width * percent / 100;
			String progressBar = String.format(
					"[%-30s] %3d%%",
					"=".repeat(completed),
					percent).replace(" ", "-");
			System.out.print("\r" + progressBar);
		}

	}

	/** {@inheritDoc} */
	@Override
	public Integer call() throws Exception {
		try {
			model instance = new model();
			DataService service = KagerowUtilities.getBean(DataService.class, null).get();
			service.importData(instance);
			return Integer.valueOf(0);
		} catch (NameAlreadyBoundException _) {
			System.err.print("\r" + "This file has already been imported");
			return Integer.valueOf(2);
		} catch (Exception e) {
			System.out.print("\r");
			KagerowLogger.newAppLogger().err(e);
			return Integer.valueOf(1);
		}

	}

}

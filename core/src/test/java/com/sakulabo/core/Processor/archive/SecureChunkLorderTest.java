package com.sakulabo.core.Processor.archive;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.sakulabo.BaseTest;
import com.sakulabo.BaseTest.KagerowContainerRunner.KagerowSecureContainerRunner;
import com.sakulabo.BaseTest.KagerowContainerRunner.ResetCtx;
import com.sakulabo.BaseTest.KagerowSchemaCreateRunner;
import com.sakulabo.BaseTest.KagerowSchemaCreateRunner.NeedsKagerowSchema;
import com.sakulabo.core.Common.AppPathUtils;
import com.sakulabo.core.Kagerow.Contents.KagerowVirtualFileContent.KagerowVirtualFileObject.SecureFileObject;
import com.sakulabo.core.Kagerow.Exception.AppLogicException;
import com.sakulabo.core.Kagerow.Utilities.KagerowDBMode;
import com.sakulabo.core.Kagerow.Utilities.KagerowTransaction;
import com.sakulabo.core.Processor.database.DDLConnectionHandler;

@ExtendWith(KagerowSecureContainerRunner.class)
@ExtendWith(KagerowSchemaCreateRunner.class)
public class SecureChunkLorderTest extends BaseTest {

        /** テスト対象 */
        private SecureChunkLorder testTarget;

        /**
         * [試験観点] : チャンク読み取り
         * [期待される結果] : 正常終了すること
         */
        @Test
        @Disabled
        @ResetCtx
        @NeedsKagerowSchema("test")
        public void Test001() throws Throwable {

                try (KagerowTransaction tran = KagerowTransaction.getTransactionFromSchemaName("test")) {
                        // インスタンス初期化
                        Path path = getTestDir().resolve("test1.csv");
                        SecureChunkCreater creater = new SecureChunkCreater("test", path,
                                        StandardCharsets.UTF_8, true,
                                        new CSVFileReaderFactory());
                        SecureFileObject result = creater.create("Test001");

                        // KDB出力先初期化
                        Path kdbPath = Files.createTempFile(AppPathUtils.createTemporaryDirPath(),
                                        AppPathUtils.KDB_FILE_PREFIX,
                                        AppPathUtils.KDB_FILE_EXT);

                        // KDB初期化処理実行
                        DDLConnectionHandler.initDDLConnectionHandler(kdbPath, KagerowDBMode.ORACLE,
                                        Set.of(result));

                        testTarget = new SecureChunkLorder(KagerowDBMode.ORACLE, kdbPath);
                        testTarget.lord(result);

                        tran.commit();

                }

        }

        /**
         * [試験観点] : チャンク読み取り,2コア
         * [期待される結果] : 正常終了すること
         */
        @Test
        @Disabled
        @ResetCtx
        @NeedsKagerowSchema("test")
        public void Test002() throws Throwable {

                // インスタンス初期化
                Path path = getTestDir().resolve("test1.csv");
                SecureChunkCreater creater = new SecureChunkCreater("test", path, StandardCharsets.UTF_8, true,
                                new CSVFileReaderFactory());
                SecureFileObject result = creater.create("Test001");

                // KDB出力先初期化
                Path kdbPath = Files.createTempFile(AppPathUtils.createTemporaryDirPath(),
                                AppPathUtils.KDB_FILE_PREFIX,
                                AppPathUtils.KDB_FILE_EXT);

                // KDB初期化処理実行
                DDLConnectionHandler.initDDLConnectionHandler(kdbPath, KagerowDBMode.ORACLE, Set.of(result));

                // モック設定
                testTarget = new SecureChunkLorder(KagerowDBMode.ORACLE, kdbPath);
                testTarget = spy(testTarget);
                doReturn(2).when(testTarget).canSeparate(any());

                // テスト実施
                testTarget.lord(result);

        }

        /**
         * [試験観点] : チャンク読み取り,4コア
         * [期待される結果] : 正常終了すること
         */
        @Test
        @Disabled
        @ResetCtx
        @NeedsKagerowSchema("test")
        public void Test003() throws Throwable {

                // インスタンス初期化
                Path path = getTestDir().resolve("test1.csv");
                SecureChunkCreater creater = new SecureChunkCreater("test", path, StandardCharsets.UTF_8, true,
                                new CSVFileReaderFactory());
                SecureFileObject result = creater.create("Test001");

                // KDB出力先初期化
                Path kdbPath = Files.createTempFile(AppPathUtils.createTemporaryDirPath(),
                                AppPathUtils.KDB_FILE_PREFIX,
                                AppPathUtils.KDB_FILE_EXT);

                // KDB初期化処理実行
                DDLConnectionHandler.initDDLConnectionHandler(kdbPath, KagerowDBMode.ORACLE, Set.of(result));

                // モック設定
                testTarget = new SecureChunkLorder(KagerowDBMode.ORACLE, kdbPath);
                testTarget = spy(testTarget);
                doReturn(4).when(testTarget).canSeparate(any());

                // テスト実施
                testTarget.lord(result);

        }

        /**
         * [試験観点] : チャンク読み取り,8コア
         * [期待される結果] : 正常終了すること
         */
        @Test
        @Disabled
        @ResetCtx
        @NeedsKagerowSchema("test")
        public void Test004() throws Throwable {

                // インスタンス初期化
                Path path = getTestDir().resolve("test1.csv");
                SecureChunkCreater creater = new SecureChunkCreater("test", path, StandardCharsets.UTF_8, true,
                                new CSVFileReaderFactory());
                SecureFileObject result = creater.create("Test001");

                // KDB出力先初期化
                Path kdbPath = Files.createTempFile(AppPathUtils.createTemporaryDirPath(),
                                AppPathUtils.KDB_FILE_PREFIX,
                                AppPathUtils.KDB_FILE_EXT);

                // KDB初期化処理実行
                DDLConnectionHandler.initDDLConnectionHandler(kdbPath, KagerowDBMode.ORACLE, Set.of(result));

                // モック設定
                testTarget = new SecureChunkLorder(KagerowDBMode.ORACLE, kdbPath);
                testTarget = spy(testTarget);
                doReturn(8).when(testTarget).canSeparate(any());

                // テスト実施
                testTarget.lord(result);

        }

        /**
         * [試験観点] : チャンク読み取り,16コア
         * [期待される結果] : 正常終了すること
         */
        @Test
        @Disabled
        @ResetCtx
        @NeedsKagerowSchema("test")
        public void Test005() throws Throwable {

                // インスタンス初期化
                Path path = getTestDir().resolve("test1.csv");
                SecureChunkCreater creater = new SecureChunkCreater("test", path, StandardCharsets.UTF_8, true,
                                new CSVFileReaderFactory());
                SecureFileObject result = creater.create("Test001");

                // KDB出力先初期化
                Path kdbPath = Files.createTempFile(AppPathUtils.createTemporaryDirPath(),
                                AppPathUtils.KDB_FILE_PREFIX,
                                AppPathUtils.KDB_FILE_EXT);

                // KDB初期化処理実行
                DDLConnectionHandler.initDDLConnectionHandler(kdbPath, KagerowDBMode.ORACLE, Set.of(result));

                // モック設定
                testTarget = new SecureChunkLorder(KagerowDBMode.ORACLE, kdbPath);
                testTarget = spy(testTarget);
                doReturn(16).when(testTarget).canSeparate(any());

                // テスト実施
                testTarget.lord(result);

        }

        /**
         * [試験観点] : チャンク読み取り,全てのパターンのデータ
         * [期待される結果] : 正常終了すること
         */
        @Test
        @Disabled
        @ResetCtx
        @NeedsKagerowSchema("test")
        public void Test006() throws Throwable {

                // インスタンス初期化
                Path path = getTestDir().resolve("test6.csv");
                SecureChunkCreater creater = new SecureChunkCreater("test", path, StandardCharsets.UTF_8, true,
                                new CSVFileReaderFactory());
                SecureFileObject result = creater.create("Test001");

                // KDB出力先初期化
                Path kdbPath = Files.createTempFile(AppPathUtils.createTemporaryDirPath(),
                                AppPathUtils.KDB_FILE_PREFIX,
                                AppPathUtils.KDB_FILE_EXT);

                // KDB初期化処理実行
                DDLConnectionHandler.initDDLConnectionHandler(kdbPath, KagerowDBMode.ORACLE, Set.of(result));

                // モック設定
                testTarget = new SecureChunkLorder(KagerowDBMode.ORACLE, kdbPath);

                // テスト実施
                testTarget.lord(result);

        }

        /**
         * [試験観点] : チャンク読み取り中に例外が発生
         * [期待される結果] : AppLogicExceptionがスローされること
         */
        @Test
        @Disabled
        @ResetCtx
        @NeedsKagerowSchema("test")
        public void Test007() throws Throwable {

                // インスタンス初期化
                Path path = getTestDir().resolve("test6.csv");
                SecureChunkCreater creater = new SecureChunkCreater("test", path, StandardCharsets.UTF_8, true,
                                new CSVFileReaderFactory());
                SecureFileObject result = creater.create("Test001");

                // KDB出力先初期化
                Path kdbPath = Files.createTempFile(AppPathUtils.createTemporaryDirPath(),
                                AppPathUtils.KDB_FILE_PREFIX,
                                AppPathUtils.KDB_FILE_EXT);

                // KDB初期化処理実行
                DDLConnectionHandler.initDDLConnectionHandler(kdbPath, KagerowDBMode.ORACLE, Set.of(result));

                // モック設定
                ChunkReader reader = mock(BasicChunkReader.class);
                testTarget = spy(new SecureChunkLorder(KagerowDBMode.ORACLE, kdbPath));
                doThrow(new IOException()).when(reader).readData();
                doReturn(reader).when(testTarget).createChunkReader(any(), anyLong(), anyLong(), anyLong());

                // テスト実施
                assertThrows(AppLogicException.class, () -> {
                        testTarget.lord(result);
                });

        }

}

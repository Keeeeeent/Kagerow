package com.sakulabo.core.Common;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.sakulabo.BaseTest;
import com.sakulabo.BaseTest.KagerowContainerRunner;
import com.sakulabo.core.Kagerow.Exception.ApplicationError;

@ExtendWith(KagerowContainerRunner.class)
public class ErrorMessageTest extends BaseTest {

    /**
     * [試験観点] : Enum定義確認
     * [期待される結果] : 期待通りの結果セットであること
     */
    @ParameterizedTest
    @CsvSource({
            "CODE_001,DONT_HEADER_NAME",
            "CODE_002,DONT_TYPE_NAME",
            "CODE_003,DONT_SET_ALGORITHM",
            "CODE_004,FAIL_LOGIC",
            "CODE_005,NOT_FOUND_FILE",
            "CODE_006,INVALID_NAME",
            "CODE_007,ILLEGAL_ELEMENT_COUNT",
            "CODE_008,DONT_USING_PARAMS",
            "CODE_009,NOT_FOUND_CONNECTION",
            "CODE_010,",
            "CODE_011,",
            "CODE_012,",
            "CODE_013,",
            "CODE_014,",
            "CODE_015,",
            "CODE_016,",
            "CODE_017,",
            "CODE_018,",
            "CODE_019,",
            "CODE_020,",
            "CODE_021,",
            "CODE_022,",
            "CODE_023,",
            "CODE_024,",
            "CODE_025,",
            "CODE_026,",
            "CODE_027,",
            "CODE_028,",
            "CODE_029,",
            "CODE_030,",
            "CODE_031,",
            "CODE_032,",
            "CODE_033,",
            "CODE_034,",
            "CODE_035,",
            "CODE_900,",
            "CODE_901,",
            "CODE_902,",
            "CODE_903,",
            "CODE_904,",
            "CODE_905,",
    })
    public void tosynonymTest(String name, @ConvertWith(ToNotNullableString.class) String synonym) {
        assertThat(ErrorMessage.valueOf(name).tosynonym(), is(synonym));
    }

    /**
     * [試験観点] : ディクショナリー登録済みのEnum、パラメータあり
     * [期待される結果] : 期待通りのメッセージが生成されること
     */
    @ParameterizedTest
    @CsvSource({
            "DONT_HEADER_NAME,test,ヘッダーの型情報が不正です",
            "DONT_TYPE_NAME,test,ヘッダーの名称情報が不正です",
            "DONT_SET_ALGORITHM,test,アルゴリズムの指定は必須です",
            "FAIL_LOGIC,test,testに失敗しました",
            "NOT_FOUND_FILE,test,ファイルが見つかりません【パス:test】",
            "INVALID_NAME,test,testは不正な名称です",
            "ILLEGAL_ELEMENT_COUNT,test,要素数が不正です",
            "DONT_USING_PARAMS,test,引数が指定されていますが使用されません【対象】: test",
            "NOT_FOUND_CONNECTION,test,コネクションが見つかりません",
    })
    public void getMessageTest_001(String name, String param, @ConvertWith(ToNotNullableString.class) String expect) {
        ErrorMessageSynonym dict = ErrorMessageSynonym.valueOf(name);
        String actual = ErrorMessage.getMessage(dict, param);
        assertThat(actual, is(expect));
    }

    /**
     * [試験観点] : ディクショナリー登録済みのEnum、パラメータnull
     * [期待される結果] : 期待通りのメッセージが生成されること
     */
    @ParameterizedTest
    @CsvSource({
            "DONT_HEADER_NAME,,ヘッダーの型情報が不正です",
            "DONT_TYPE_NAME,,ヘッダーの名称情報が不正です",
            "DONT_SET_ALGORITHM,,アルゴリズムの指定は必須です",
            "FAIL_LOGIC,,nullに失敗しました",
            "NOT_FOUND_FILE,,ファイルが見つかりません【パス:null】",
            "INVALID_NAME,,nullは不正な名称です",
            "ILLEGAL_ELEMENT_COUNT,,要素数が不正です",
            "DONT_USING_PARAMS,,引数が指定されていますが使用されません【対象】: null",
            "NOT_FOUND_CONNECTION,,コネクションが見つかりません",
    })
    public void getMessageTest_002(String name, String param, @ConvertWith(ToNotNullableString.class) String expect) {
        ErrorMessageSynonym dict = ErrorMessageSynonym.valueOf(name);
        String actual = ErrorMessage.getMessage(dict, param);
        assertThat(actual, is(expect));
    }

    /**
     * [試験観点] : ディクショナリー登録済みのEnum、パラメータなし
     * [期待される結果] : 期待通りのメッセージが生成されること
     */
    @ParameterizedTest
    @CsvSource({
            "DONT_HEADER_NAME,ヘッダーの型情報が不正です",
            "DONT_TYPE_NAME,ヘッダーの名称情報が不正です",
            "DONT_SET_ALGORITHM,アルゴリズムの指定は必須です",
            "FAIL_LOGIC,{0}に失敗しました",
            "NOT_FOUND_FILE,ファイルが見つかりません【パス:{0}】",
            "INVALID_NAME,{0}は不正な名称です",
            "ILLEGAL_ELEMENT_COUNT,要素数が不正です",
            "DONT_USING_PARAMS,引数が指定されていますが使用されません【対象】: {0}",
            "NOT_FOUND_CONNECTION,コネクションが見つかりません",
    })
    public void getMessageTest_003(String name, @ConvertWith(ToNotNullableString.class) String expect) {
        ErrorMessageSynonym dict = ErrorMessageSynonym.valueOf(name);
        String actual = ErrorMessage.getMessage(dict);
        assertThat(actual, is(expect));
    }

    /**
     * [試験観点] : 存在しないEnumの場合
     * [期待される結果] : ApplicationErrorが発生すること
     */
    @Test
    public void getMessageTest_004() {
        ErrorMessageSynonym dict = createEnum(ErrorMessageSynonym.class);
        assertThrows(ApplicationError.class, () -> ErrorMessage.getMessage(dict));
    }

    /**
     * [試験観点] : パラメータなし
     * [期待される結果] : メッセージが期待通り生成されること
     */
    @ParameterizedTest
    @ValueSource(strings = {
            "CODE_001,ヘッダーの型情報が不正です",
            "CODE_002,ヘッダーの名称情報が不正です",
            "CODE_003,アルゴリズムの指定は必須です",
            "CODE_004,{0}に失敗しました",
            "CODE_005,ファイルが見つかりません【パス:{0}】",
            "CODE_006,{0}は不正な名称です",
            "CODE_007,要素数が不正です",
            "CODE_008,引数が指定されていますが使用されません【対象】: {0}",
            "CODE_009,コネクションが見つかりません",
            "CODE_010,パラメータ [{0}] は指定必須です",
            "CODE_011,エグゼキューターの取得に失敗しました [必要数] : {0}",
            "CODE_012,キャッシュの取込に失敗しました [原因] : {0}",
            "CODE_013,終端{0}の指定は必須です",
            "CODE_014,セキュアブートが行われていません",
            "CODE_015,キャッシュ非対応インスタンスです",
            "CODE_016,キャッシュIDの生成に失敗しました",
            "CODE_017,既に存在するシノニムです【対象】: {0}",
            "CODE_018,アプリケーション専用のObjectInputFilter登録に失敗しました",
            "CODE_019,アプリケーションでデシリアライズが許可されてないクラスです【対象】: {0}",
            "CODE_020,アプリケーションでデシリアライズが許可されてないネスト回数です【回数】: {0}",
            "CODE_021,アプリケーションでデシリアライズが許可されてない配列要素数です【回数】: {0}",
            "CODE_022,Kagerowライブラリが未初期化です",
            "CODE_023,KFile向けXSDファイルの読み込みに失敗しました",
            "CODE_024,KSQLファイルの読み込みに失敗しました",
            "CODE_025,XMLエラー（{0}行目, {1}列目）: <{2}> はこの位置では使用できません。<{3}> が必要です",
            "CODE_026,XMLエラー（{0}行目, {1}列目）: {2}",
            "CODE_027,KSQLファイルの書き出しに失敗しました",
            "CODE_028,KSQLファイルへの要素追加に失敗しました",
            "CODE_029,予期せぬエラーによりKSQL実行に失敗しました",
            "CODE_030,文字数が長すぎます 【詳細】: {0}",
            "CODE_031,数値が大きすぎます 【詳細】: {0}",
            "CODE_032,日付の形式が不正です 【詳細】: {0}",
            "CODE_033,一意制約違反 【詳細】: {0}",
            "CODE_034,外部キー制約違反 【詳細】: {0}",
            "CODE_035,NULL制約違反 【詳細】: {0}",
            "CODE_900,スクリプト環境変数<{0}>が見つかりませんでした",
            "CODE_901,置換変数<{0}>が見つかりませんでした",
            "CODE_902,スキーマ<{0}>が見つかりませんでした",
            "CODE_903,テーブル<{0}>が見つかりませんでした",
            "CODE_904,テーブル<{0}>の世代[{1}]が見つかりませんでした",
            "CODE_905,合成テーブルビューの指定された範囲が不正です。【開始】: {0}【終了】: {1}",
    })
    public void getMessageTest_005(String target) {
        String[] testData = target.split(",", 2);
        String actual = ErrorMessage.valueOf(testData[0]).getMessage();
        assertThat(actual, is(testData[1]));
    }

    /**
     * [試験観点] : パラメータあり
     * [期待される結果] : メッセージが期待通り生成されること
     */
    @ParameterizedTest
    @ValueSource(strings = {
            "CODE_001,ヘッダーの型情報が不正です",
            "CODE_002,ヘッダーの名称情報が不正です",
            "CODE_003,アルゴリズムの指定は必須です",
            "CODE_004,test1に失敗しました",
            "CODE_005,ファイルが見つかりません【パス:test1】",
            "CODE_006,test1は不正な名称です",
            "CODE_007,要素数が不正です",
            "CODE_008,引数が指定されていますが使用されません【対象】: test1",
            "CODE_009,コネクションが見つかりません",
            "CODE_010,パラメータ [test1] は指定必須です",
            "CODE_011,エグゼキューターの取得に失敗しました [必要数] : test1",
            "CODE_012,キャッシュの取込に失敗しました [原因] : test1",
            "CODE_013,終端test1の指定は必須です",
            "CODE_014,セキュアブートが行われていません",
            "CODE_015,キャッシュ非対応インスタンスです",
            "CODE_016,キャッシュIDの生成に失敗しました",
            "CODE_017,既に存在するシノニムです【対象】: test1",
            "CODE_018,アプリケーション専用のObjectInputFilter登録に失敗しました",
            "CODE_019,アプリケーションでデシリアライズが許可されてないクラスです【対象】: test1",
            "CODE_020,アプリケーションでデシリアライズが許可されてないネスト回数です【回数】: test1",
            "CODE_021,アプリケーションでデシリアライズが許可されてない配列要素数です【回数】: test1",
            "CODE_022,Kagerowライブラリが未初期化です",
            "CODE_023,KFile向けXSDファイルの読み込みに失敗しました",
            "CODE_024,KSQLファイルの読み込みに失敗しました",
            "CODE_025,XMLエラー（test1行目, test2列目）: <test3> はこの位置では使用できません。<test4> が必要です",
            "CODE_026,XMLエラー（test1行目, test2列目）: test3",
            "CODE_027,KSQLファイルの書き出しに失敗しました",
            "CODE_028,KSQLファイルへの要素追加に失敗しました",
            "CODE_029,予期せぬエラーによりKSQL実行に失敗しました",
            "CODE_030,文字数が長すぎます 【詳細】: test1",
            "CODE_031,数値が大きすぎます 【詳細】: test1",
            "CODE_032,日付の形式が不正です 【詳細】: test1",
            "CODE_033,一意制約違反 【詳細】: test1",
            "CODE_034,外部キー制約違反 【詳細】: test1",
            "CODE_035,NULL制約違反 【詳細】: test1",
            "CODE_900,スクリプト環境変数<test1>が見つかりませんでした",
            "CODE_901,置換変数<test1>が見つかりませんでした",
            "CODE_902,スキーマ<test1>が見つかりませんでした",
            "CODE_903,テーブル<test1>が見つかりませんでした",
            "CODE_904,テーブル<test1>の世代[test2]が見つかりませんでした",
            "CODE_905,合成テーブルビューの指定された範囲が不正です。【開始】: test1【終了】: test2",
    })
    public void getMessageTest_006(String target) {
        String[] testData = target.split(",", 2);
        String actual = ErrorMessage.valueOf(testData[0]).getMessage("test1", "test2", "test3", "test4");
        assertThat(actual, is(testData[1]));
    }

}

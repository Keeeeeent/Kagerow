package com.sakulabo.core.Common;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.provider.CsvSource;

import com.sakulabo.BaseTest;
import com.sakulabo.BaseTest.KagerowContainerRunner;
import com.sakulabo.core.Kagerow.Exception.ApplicationError;

@ExtendWith(KagerowContainerRunner.class)
public class ApplicationWordDictionaryTest extends BaseTest {

    /**
     * [試験観点] : Enum定義確認
     * [期待される結果] : 期待通りの結果セットであること
     */
    @ParameterizedTest
    @CsvSource({
            "WCD_0001,KDB",
            "WCD_0002,BOM_ANALYSIS",
            "WCD_0003,",
            "WCD_0004,",
            "WCD_0005,",
            "WCD_0006,",
            "WCD_0007,",
    })
    public void tosynonymTest(String name, @ConvertWith(ToNotNullableString.class) String synonym) {
        assertThat(ApplicationWordDictionary.valueOf(name).tosynonym(), is(synonym));
    }

    /**
     * [試験観点] : ディクショナリー登録済みのEnum
     * [期待される結果] : 期待通りのメッセージが生成されること
     */
    @ParameterizedTest
    @CsvSource({
            "KDB,KDBの生成",
            "BOM_ANALYSIS,文字コードの解析"
    })
    public void getMessageTest_001(String name, @ConvertWith(ToNotNullableString.class) String expect) {
        ApplicationWordDictionarySynonym dict = ApplicationWordDictionarySynonym.valueOf(name);
        String actual = ApplicationWordDictionary.getMessage(dict);
        assertThat(actual, is(expect));
    }

    /**
     * [試験観点] : 存在しないEnumの場合
     * [期待される結果] : ApplicationErrorが発生すること
     */
    @Test
    public void getMessageTest_002() {
        ApplicationWordDictionarySynonym dict = createEnum(ApplicationWordDictionarySynonym.class);
        assertThrows(ApplicationError.class, () -> ApplicationWordDictionary.getMessage(dict));
    }

    /**
     * [試験観点] : パラメータなし
     * [期待される結果] : メッセージが期待通り生成されること
     */
    @ParameterizedTest
    @CsvSource({
            "WCD_0001,KDBの生成",
            "WCD_0002,文字コードの解析",
            "WCD_0003,不正なキャッシュID",
            "WCD_0004,KSQL",
            "WCD_0005,プラグイン",
            "WCD_0006,KagerowDDLPlugin",
            "WCD_0007,DDL",
    })
    public void getMessageTest_003(String code, String msg) {
        String actual = ApplicationWordDictionary.valueOf(code).getMessage();
        assertThat(actual, is(msg));
    }

    /**
     * [試験観点] : パラメータあり
     * [期待される結果] : メッセージが期待通り生成されること
     */
    @ParameterizedTest
    @CsvSource({
            "WCD_0001,KDBの生成",
            "WCD_0002,文字コードの解析",
            "WCD_0003,不正なキャッシュID",
            "WCD_0004,KSQL",
            "WCD_0005,プラグイン",
            "WCD_0006,KagerowDDLPlugin",
            "WCD_0007,DDL",
    })
    public void getMessageTest_004(String code, String msg) {
        String actual = ApplicationWordDictionary.valueOf(code).getMessage("test");
        assertThat(actual, is(msg));
    }

}

package com.sakulabo.application.common.code;

public enum EnvOption {

    /** 環境変数パスワード */
    KGPASSWORD("KGPASSWORD");

    /** 環境変数名 */
    private String env;

    private EnvOption(String env) {
        this.env = env;
    }

    /**
     * 環境変数に指定された設定値を取得します
     * 
     * @return 設定値
     */
    public String toEnvValue() {
        return System.getenv(env);
    }

}

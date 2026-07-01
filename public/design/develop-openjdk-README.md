# OpneJDK21の環境構築

## 📖 目次  
- [モジュールの取得](#-モジュールの取得)  
- [モジュールの展開](#-モジュールの展開)  
- [JDK の zip ファイル/tarファイルを解凍する](#-JDKのzipファイル/tarファイルを解凍する)  
    - [zipファイルの場合](#-zipファイルの場合)  
    - [tarファイルの場合](#-tarファイルの場合)  
- [Eclipseの設定](#-Eclipseの設定)  

## モジュールの取得
下記URLよりマシンにあったJDKをダウンロードする。
(JDK 21.0.2)

- [Mac/AArch64](https://download.java.net/java/GA/jdk21.0.2/f2283984656d49d69e91c558476027ac/13/GPL/openjdk-21.0.2_macos-aarch64_bin.tar.gz)
- [Mac/x64](https://download.java.net/java/GA/jdk21.0.2/f2283984656d49d69e91c558476027ac/13/GPL/openjdk-21.0.2_macos-x64_bin.tar.gz)
- [Linux/AArch64](https://download.java.net/java/GA/jdk21.0.1/415e3f918a1f4062a0074a2794853d0d/12/GPL/openjdk-21.0.1_linux-aarch64_bin.tar.gz)
- [Linux/x64](https://download.java.net/java/GA/jdk21.0.1/415e3f918a1f4062a0074a2794853d0d/12/GPL/openjdk-21.0.1_linux-x64_bin.tar.gz)
- [Windows](https://download.java.net/java/GA/jdk21.0.2/f2283984656d49d69e91c558476027ac/13/GPL/openjdk-21.0.2_windows-x64_bin.zip)

## モジュールの展開

ターミナルを開き、プロジェクトルート/public/openjdk/inuse に移動する
```
cd ./public/openjdk/inuse 
```

移行が完了したら、解凍するディストリビューションを展開先フォルダ（移動したフォルダ）にコピーする

## JDKのzipファイル/tarファイルを解凍する

### zipファイルの場合
```
unzip *.zip
```

### tarファイルの場合
```
tar -xvzf *.tar
```

## Eclipseの設定

設定->Java->インストール済みの JRE->追加

- JRE ホーム：先ほど展開したフォルダの Contents/Home
- JRE 名：OpenJDK21
- JRE システム・ライブラリー：先ほど展開したフォルダの /Home を選択

※OSによって親ディレクトリは異なるが、homeフォルダをしてできれば問題なし

この設定で完了を押し、追加した JRE を選択し適用をする。

ビルドパスの構成->Javaのビルド・パス->モジュールパスのJREを選択->編集->代替JER->OpenJDK21 を選択->適用

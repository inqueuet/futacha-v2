# ふたちゃ

ふたば☆ちゃんねると、あいもげ（nijiurachan.net）を読み書きする Android 用の専用ブラウザです。Kotlin と Jetpack Compose（Compose Multiplatform のライブラリを含む）で作っています。Wear OS には、スマートフォンの履歴や監視の結果を見る連携アプリがあります。

## 対応環境

- Android：Android 8.0（API 26）以降。ブラウザ本体。
- Wear OS：API 26 以降の Wear OS 端末。Android 版との連携アプリ。

## 4つのモード

同じデータを、見た目と操作の違う4つのモードで使えます。設定の「モード」から切り替えます。

- ふたちゃ：このアプリ独自の画面。
- としあき（仮）：参照アプリの画面と操作に合わせたモード。
- ふたばー風：iOS の futaber の見た目と操作に合わせたモード。
- ふたったー風：ふたったーの見た目と操作に合わせたモード。

ふたばー風・ふたったー風は元のアプリと同じ見た目と操作を基本にし、他のモードの機能は「設定 → このアプリ → 拡張機能」で足します。追加の項目は、長押しのシートや操作メニューの最後の「その他の機能」にまとめています。元のアプリと違う以前の動きは、設定の「従来の動き」で戻せます。

板・履歴・タブ・NG・下書きなどは4モードで共有します。どのモードで開いたスレッドも、切り替えずに他のモードの履歴とタブに出ます。

## あいもげ対応

- 板の追加：ふたばと同じく「板を追加」のURL欄にあいもげの板のURL（`https://nijiurachan.net/b/{板}/`）を入れます。スレ・カタログのURLや http も板のURLに直します。名前はあいもげの板一覧から取り、R18 の板は追加の時に確認します。ふたばの板と同じ一覧・タブ・履歴・バックアップに入ります。
- 読む：カタログ・スレッド・画像と動画（1レス3枚まで）・保存・過去ログ（あいもげの過去ログと今あるスレのレスの検索を含む）。ダイスの太字、伏せ字、アンケート、消滅予告、閉じたスレ、R18 の印も出します。
- 承認と書き込み：設定の「あいもげ」からブラウザで承認すると、返信・スレ立て・添付・手書き・そうだね・del・通報・本人削除・アンケートの投票ができます。承認の後に何かを自動で送ることはありません。
- 削除キー：ふたばとは別のキーです。設定の「あいもげ」で確認・変更でき、最初に送る時に同意を確かめます。
- 結果が分からない書き込みは自動で送り直しません。送り直す時は同じ書き込みとして扱います。
- 裏の更新・巡回・通知もあいもげのスレッドに対応し、通知からあいもげのスレッドを開けます。
- あいもげを使えない版では、あいもげの板・タブ・履歴を消さずに隠します。

## 主な機能

### 板・カタログ

- 板の追加（公式の板一覧からまとめて追加、URL欄からのあいもげの板の追加）、改名、削除、ピン留め、並べ替え
- グリッド／リスト、列数、カタログのモードと並び順
- 検索、NG、監視ワード、板ごとの設定
- 更新、更新前に戻す、消えたスレッドの確認、スレッド作成

### スレッド

- 取得・更新、既読位置の復元、「新着レス ○件」の帯と新着への移動
- 本文検索（右端の×で入力を消せます）、引用の吹き出し、引用返信、抽出、ツリー表示
- 返信・スレ立て、画像と動画の添付、手書き、あぷ・あぷ小（3000KB を超える写真は縮小して上げます）、音声入力
- 下書き、そうだね、del・通報、本人削除
- 読み上げ（端末の音声、または Android 10 以降の64bit で任意に取得する「ずんだもん」）、自動スクロール
- キャッシュ検索、落ちたスレッドの過去ログ取得

### タブと履歴

- タブと履歴は4モードで共有します。タブを閉じるとすべてのモードから消え、履歴は残ります。履歴を消すとすべてのモードの履歴から消え、タブは残ります。
- としあき（仮）では「タブを閉じたら履歴も消す」「履歴を消したらタブも閉じる」を選べます（設定 → 操作、最初はOFF）。
- タブの上限は最初200件で、20〜990件に変えられます（ふたちゃの「設定 → タブ」、ふたったー風の拡張機能）。★の付いたタブと開いているタブは整理しません。
- 閉じたタブは20回分を24時間記録し、そのモードで閉じたか表示していたタブを戻せます。
- ふたばー風のお気に入りは★とは別に持ち、タブを閉じても残ります。

### メディア・保存

- 画像・GIF・WebP・APNG・動画の表示、画像一覧、拡大、画像検索
- スレッドの保存（HTML・MHT など）、メディアだけの保存、保存済みスレッドのオフライン閲覧
- 自動保存と、通信できない時の保存したコピーの表示
- 画像・動画のモザイクと黒塗り（自動検出・追尾・輪郭）、生成情報・プロンプトの表示（どれも最初はOFF）

### 設定・連携

- テーマ・表示・操作・NG・Cookie・キャッシュ・保存先・通知などの設定。配色はモードごと
- 設定のバックアップと復元、旧版データ・ふたったーの `.ftt` の取り込み
- 起動ロック、プライバシー表示
- キーワード巡回と通知（Android の外部にじろぐ連携も）
- AI（端末内の要約、OpenAI による荒らし判定。キーの登録と同意が必要）、AI コマンド・ディープリンク
- Wear OS への履歴・監視の結果・読み上げの同期

使い方はアプリ内の「ヘルプ」で検索でき、各モードの「操作ガイド」で投稿・添付・削除キー・ジェスチャーの代わりの操作を確かめられます。変更履歴はアプリの設定から見られます（[変更履歴データ](shared/src/main/kotlin/ui/compat/CompatibilityReferenceInfoData.kt)）。

## プロジェクト構成

```text
.
├── app-android/     Android ホストアプリ、WorkManager、通知、端末連携
├── app-wear/        Wear OS アプリ、Tile、Data Layer 連携
├── core-wearable/   スマートフォンと時計で共有する同期の形式
├── baselineprofile/ 起動最適化プロファイルの生成・性能計測
├── buildSrc/        文字選択の補強などのビルド処理
├── shared/          画面、状態、通信、解析、保存（Android ライブラリ）
└── tools/           ネイティブ依存の準備、配布物の検証スクリプト
```

署名鍵・ローカル設定・生成物・検証ログは Git の管理対象外です。

- `shared/src/main`：画面、モデル、状態管理、通信、パーサー、Repository、Service、Android 向けの実装
- `shared/src/test`：単体テスト
- 主な画面の入口：`shared/src/main/kotlin/ui/FutachaApp.kt`（ふたちゃ）、`shared/src/main/kotlin/ui/compat/CompatibilityApp.kt`（としあき（仮））

Release ビルドでは `baselineprofile/` が専用の API 35 エミュレーターで最適化プロファイルを作ります。4モードの起動と、板一覧からカタログ・スレッドを開く操作を集めます（ふたばー風・ふたったー風は画像一覧も）。

## 主な技術要素

- Kotlin 2.5 / Jetpack Compose / Compose Multiplatform のライブラリ
- Ktor Client、kotlinx.coroutines、kotlinx.serialization
- DataStore、SQLite
- Coil 3、Media3
- ONNX Runtime、OpenCV（メディア編集・検出・追尾）、VOICEVOX CORE（ずんだもん）
- WorkManager、Wear OS Data Layer
- Firebase Analytics / Crashlytics / Performance（設定ファイルがある時だけ初期化）

依存の版数は [バージョンカタログ](gradle/libs.versions.toml) と各モジュールの `build.gradle.kts` を見てください。

## 必要な環境

- Git、JDK 17、Python 3、初回の依存取得用のネットワーク
- Gradle デーモン：JDK 21（[デーモン JVM 設定](gradle/gradle-daemon-jvm.properties) に取得先を指定）
- Android SDK 37.2、Build Tools 37.0.0、NDK `30.0.16248370`、SDK の CMake `4.1.2`

Gradle Wrapper を同梱しているので、Gradle を個別に入れる必要はありません。

## セットアップ

### 1. Clone

```bash
git clone https://github.com/inqueuet/futacha-v2.git
cd futacha-v2
```

### 2. Android SDK

Android Studio で開くか、ルートの `local.properties` に SDK の場所を書きます。

```properties
sdk.dir=/path/to/Android/sdk
```

### 3. Android

```bash
./gradlew :app-android:assembleDebug
./gradlew :app-android:installDebug   # 接続中の端末・エミュレーターへ
```

Debug APK は `app-android/build/outputs/apk/debug/` に出ます。

Release AAB を署名するには、次の値を環境変数、Gradle プロパティ、または Git 管理外の `local.properties` に設定します（`local.properties` → Gradle プロパティ → 環境変数の順に読みます）。

- `FUTACHA_RELEASE_STORE_FILE`：キーストアのパス（相対パスはリポジトリ直下から）
- `FUTACHA_RELEASE_STORE_PASSWORD`：キーストアのパスワード
- `FUTACHA_RELEASE_KEY_ALIAS`：署名鍵のエイリアス
- `FUTACHA_RELEASE_KEY_PASSWORD`：署名鍵のパスワード

```bash
./gradlew :app-android:bundleRelease
```

出力は `app-android/build/outputs/bundle/release/app-android-release.aab` です。Release ビルドは Gradle Managed Device `futachaBaselineApi35` で Baseline Profile を作るため、Android Emulator と API 35 の AOSP システムイメージが要ります。Release の ABI は arm64-v8a と armeabi-v7a です。

### 4. Wear OS

```bash
./gradlew :app-wear:assembleDebug
```

スマートフォン版との同期には、ペアリング済みの Wear OS 端末またはエミュレーターが要ります。スマートフォンと時計の版数は、それぞれのビルド設定で管理しています。

## Firebase（任意）

Firebase の設定ファイルが無くてもビルド・起動できます。Analytics / Crashlytics / Performance を使う時は、Firebase Console から取得した `google-services.json` を `app-android/google-services.json` に置きます。設定ファイル・署名情報・実行時のユーザーデータは Git の管理対象外です。値をソースに直接書かないでください。

## テスト

```bash
# 識別子の固定、shared の検査、Android の単体テスト・Lint・Debug APK
./gradlew qualityGate

./gradlew :shared:check                          # shared の単体テストなど
./gradlew :app-android:testDebugUnitTest         # Android の単体テスト
./gradlew :app-android:connectedDebugAndroidTest # 端末・エミュレーターの UI テスト
```

GitHub Actions は使わず、開発者の環境で検査します。

## データとネットワークについて

- 設定、履歴、Cookie、保存したスレッドなどは、アプリの領域か、利用者が選んだ保存先（SAF）に保存します。
- 投稿、そうだね、削除に関わる操作は、接続先（ふたば・あいもげ）へ実際に送ります。
- あいもげの資格と削除キーは端末の安全な保存領域に置き、画面やログには出しません。接続先のルールと利用環境を確かめて使ってください。
- HTML やメディアの取得には、大きさ・件数・時間の上限を設けています。
- バックグラウンドの定期更新は、省電力の設定などで遅れることがあります。

## ライセンスとプロジェクト方針


本プロジェクトでは、コードの修正や機能追加を歓迎しています。フォーク、改変、派生版の公開・再配布、および各種アプリストアでの公開も可能です。

派生版を公開・配布する場合は、配布者自身の責任において、品質、安全性、プライバシー、接続先および配布先の規約を確認し、利用者へのサポートを含む継続的かつ責任ある開発・運用に取り組んでください。

ソースコードを公開する目的は、ふたば専用ブラウザの開発者が減少するなかで、特定のアプリに選択肢が集中する状況や、保守されていないアプリしか利用できない状況を避け、利用者が継続的に複数の選択肢を持てる環境を支えることにあります。この趣旨に賛同する開発者による改善、提案、新たな実装を歓迎します。

なお、本リポジトリで使用している第三者ライブラリ、サービス、名称、ロゴその他の素材には、それぞれの権利者が定めるライセンスや利用条件が適用されます。派生版の公開・配布時には、各条件を個別に確認してください。

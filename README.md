# Now

Fixed current time.  

時間に依存したAPIの振る舞いをテストする場合, 現在時刻を固定すればテストをRepeatableに保てます.  
現在時刻を固定する次の2通りの方法を実装しました.  

  1. `java.time`を使った方法（Java8以降）
  2. `java.util`を使った方法（Java7以前）

テストの際に現在時刻を固定化する下準備として, 現在時刻を返すAPIをラップしたメソッドを用意します.  

```java
public static long now() {
    return <現在時刻を返すAPI>
}
```

テストの際には固定時刻を返すように振る舞いを変更してやれば, このAPIに依存しているモジュールからするとあたかも現在時刻が固定化されているように見えます.  

目的達成のためには, `now()`メソッドが常にダミーのepoch timeを返すように振る舞いを差し替える仕組みを用意する必要があります.  
今回はJava7以前で使えるアプローチと, Java8以降で使えるアプローチを実装しました.  


## java.time

Java8からは`java.time`パッケージが追加され, 日付関係のユーティリティが強化されました（JSR-310）.  
現在時刻のepoch timeを取得するために`System.currentTimeMillis()`を使用する代わりに`clock.millis()`が使えます.  

 - [Clock](https://docs.oracle.com/javase/jp/8/docs/api/java/time/Clock.html)クラスを使用します.  

`Clock`が指す時刻は[Clock.fixed](https://docs.oracle.com/javase/jp/8/docs/api/java/time/Clock.html#fixed-java.time.Instant-java.time.ZoneId-)や[Clock.offset](https://docs.oracle.com/javase/jp/8/docs/api/java/time/Clock.html#offset-java.time.Clock-java.time.Duration-)を使って自由に変更できます.  

現在時刻を取得するAPIに`Clock`を使うことで, 現在時刻を固定化することも可能です.  

 - [GitHub - Now / YukiMatsumura](https://github.com/YukiMatsumura/Now/blob/master/app/src/main/java/yuki/m/android/now/time/Jsr310Time.java)

下記は`Clock`を使った現在時刻のepoch timeを返すメソッドと, それを固定化するメソッドです.  

```java
// 現在時刻のepoch timeを返す
public static long now() {
    return clock.millis();
}

// 現在時刻を固定する
@VisibleForTesting
protected static void fixedCurrentTime(@NonNull Clock clock) {
    clock = clock;
}

// 現在時刻の固定を解除する
@VisibleForTesting
protected static void tickCurrentTime() {
    clock = Clock.systemUTC();
}
```

 - [Jsr310Time.java](https://github.com/YukiMatsumura/Now/blob/master/app/src/main/java/yuki/m/android/now/time/Jsr310Time.java)

固定したい日時を設定した`Clock`を`fixedCurrentTime()`に渡せば, `now()`が返す値が固定されます.  
固定化を解除したい場合は`tickCurrentTime`を呼びます.  



## java.util

`Clock`はJava8で導入された`java.time`パッケージが提供するAPIです.  
そのため, Java7以前の環境では別の方法をとるか, JSR-310のバックポートライブラリを使う必要があります.  

バックポートライブラリには下記が使えます.

 - [`org.threeten:threetenbp`](https://github.com/ThreeTen/threetenbp)
 - [`com.jakewharton.threetenabp:threetenabp`](https://github.com/JakeWharton/ThreeTenABP)

今回はこれらを使用せず別の方法をとりました.  
Java7以前でもライブラリの追加無しでとれる方法です.  

まず, `Clock`に代わる現在時刻のepoch timeを返すインタフェース`NowProvider`を用意します.  
現在時刻を返すには`System.currentTimeMillis()`を呼び出します.  

```java
interface NowProvider {
    long now();
}

private static NowProvider systemCurrentTimeProvider = System::currentTimeMillis;

// nowで返すepoch timeを決定するプロバイダ
private static NowProvider nowProvider = systemCurrentTimeProvider;

// 現在時刻のepoch timeを返す
public static long now() {
    return nowProvider.now();
}
```

次に現在時刻を返す`NowProvider`を差し替える仕組みを用意します.  

```java
// 現在時刻を固定する
@VisibleForTesting
protected static void fixedCurrentTime(NowProvider provider) {
    nowProvider = provider;
}

// 現在時刻の固定を解除する
@VisibleForTesting
protected static void tickCurrentTime() {
    nowProvider = systemCurrentTimeProvider;
}
```

 - [LegacyTime.java](https://github.com/YukiMatsumura/Now/blob/master/app/src/main/java/yuki/m/android/now/time/LegacyTime.java)

固定したい日時を設定した`NowProvider`を`fixedCurrentTime()`に渡せば, `now()`が返す値が固定されます.  
固定化を解除したい場合は`tickCurrentTime`を呼びます.  



## TestRule

現在時刻を固定するには固定と固定解除のメソッドを対で呼び出す必要があります.  
固定解除のコードが実行されないと他のテストケースに意図しない影響を及ぼします.  

テストケースごとにtry-finallyで時刻固定-解除のロジックを書くのも骨が折れます.  
そこで`JUnit`の[`Test Rule`](https://github.com/junit-team/junit4/wiki/Rules)を使って簡便化します.  

```java
// Java8 or after
public class Jsr310TimeTest {

    @Rule
    public Jsr310TimeRule jsr310TimeRule = new Jsr310TimeRule();

    @Test
    @Now("2000-01-01T00:00:00Z")
    public void テスト() throws Exception {
        assertThat(Jsr310Time.now()).isEqualTo(
                Jsr310Time.parseIso8601Z("2000-01-01T00:00:00Z"));
    }
}

// Java7 or before
public class LegacyTimeTest {

    @Rule
    public LegacyTimeRule legacyTimeRule = new LegacyTimeRule();

    @Test
    @Now("2000-01-01T00:00:00Z")
    public void テスト() throws Exception {
        assertThat(LegacyTime.now()).isEqualTo(
                LegacyTime.parseIso8601Z("2000-01-01T00:00:00Z"));
    }
}
```

`Jsr310TimeRule`/`LegacyTimeRule`はテストメソッドが`Now`でアノテートされているとテスト開始時に現在時刻を固定し, テスト終了時に解除します.  
`@Now`を宣言するだけで現在時刻が固定化されて便利です.  

 - [Jsr310TimeRule.java](https://github.com/YukiMatsumura/Now/blob/master/app/src/test/java/yuki/m/android/now/time/Jsr310TimeRule.java)
 - [LegacyTimeRule.java](https://github.com/YukiMatsumura/Now/blob/master/app/src/test/java/yuki/m/android/now/time/LegacyTimeRule.java)


## 蛇足

テストクラスに含まれる全てのテストで現在時刻を固定したいのであれば, `Now`アノテーションで[`ElementType.Type`](https://docs.oracle.com/javase/jp/8/docs/api/java/lang/annotation/ElementType.html#TYPE)をサポートし, テストランナーを[`@ClassRule`](https://github.com/junit-team/junit4/wiki/Rules#classrule)で宣言すれば実現できま 
す.  
ただ, テストケースで現在時刻が固定されていることに気付き辛くなるため実装しませんでした.  


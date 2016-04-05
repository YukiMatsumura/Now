/*
 * Copyright 2016 yuki312 All Right Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package yuki.m.android.now.time;

import android.support.annotation.NonNull;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static yuki.m.android.now.time.Time.parseIso8601z;

/**
 * テスト時の現在日時を固定するテストルール.
 * <p>
 * {@link Now}でアノテートされたテストは{@link Time#now()}が指定の日時を返すようになる.
 * {@link Now}でアノテートされていないテストは{@code DateTimeRule}の初期化方法に依存して現在日時が決定される.
 * <p>
 * このテストルールはスレッドセーフではない.
 * <p>
 * Created by YukiMatsumura on 2016/04/05.
 */
public class DateTimeRule implements TestRule {

    /**
     * 現在日時を{@code value}値で固定する.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD})
    @interface Now {

        /**
         * 固定する日時を指定する.
         * <p>
         * 日時フォーマットはISO8601形式, TimeZone UTC±00:00.
         */
        String value() default "2000-01-01T00:00:00Z";
    }

    // 多重lock/unlockを検知するためのフラグ. スレッドセーフは保証しない.
    private static boolean locked = false;

    private final Time.NowProvider defaultProvider;

    /**
     * 固定の現在日時を返すテストルール.
     * <p>
     * このコンストラクタで初期化されたテストルールは, {@link Now}でアノテーションされていない
     * テストケースの現在日時を{@code defaultProvider}が提供する日時で固定化する.
     *
     * @param defaultProvider テストケースにアノテーションが指定されていない場合の日時プロバイダ.
     *                        Nullが指定された場合は
     */
    public DateTimeRule(@NonNull Time.NowProvider defaultProvider) {
        requireNonNull(defaultProvider);
        this.defaultProvider = defaultProvider;
    }

    /**
     * 固定の現在日時を返すテストルール.
     * <p>
     * このコンストラクタで初期化されたテストルールは, {@link Now}でアノテーションされていない
     * テストケースの現在日時を{@code defaultTime}で固定化する.
     *
     * @param defaultTime テストケースにアノテーションが指定されていない場合の固定日時.
     *                    固定日時はISO8601-UTC形式で指定すること(e.g.2000-01-01T00:00:00Z).
     */
    public DateTimeRule(@NonNull String defaultTime) {
        requireNonNull(defaultTime);
        this.defaultProvider = () -> parseIso8601z(defaultTime);
    }

    /**
     * 固定の現在日時を返すテストルール.
     * <p>
     * このコンストラクタで初期化されたテストルールは, {@link Now}でアノテーションされていない
     * テストケースの現在日時を固定化しない.
     * <p>
     * デフォルトの振る舞いを変更したい場合は{@link DateTimeRule (Time.NowProvider)}を使用する.
     */
    public DateTimeRule() {
        this(Time.currentTimeProvider);
    }

    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                Now annotation = description.getAnnotation(Now.class);
                Time.NowProvider provider = (annotation != null)
                        ? () -> parseIso8601z(annotation.value())
                        : defaultProvider;

                try {
                    DateTimeRule.lock(provider);
                    base.evaluate();
                } finally {
                    DateTimeRule.unlock();
                }
            }
        };
    }

    /**
     * 現在日時を{@code provider}の提供する日時で固定化する.
     * <p>
     * ダミー日時は {@code Time.NowProvider} が提供する日時に固定化される.
     * ダミー日時に置き換えたオブジェクトは後処理として必ず{@link #unlock()}を呼ぶこと.
     *
     * @param provider ダミーの現在日時を返すプロバイダー
     * @throws IllegalStateException ロックが取得できなかった場合
     */
    private static void lock(@NonNull Time.NowProvider provider) {
        if (DateTimeRule.locked) {
            throw new IllegalStateException("NowProvider is already locked.");
        }
        DateTimeRule.locked = true;
        Time.setNowProvider(provider);
    }

    /**
     * 現在日時の固定化を解除する.
     * <p>
     * 日時の固定化を解除すると, 現在日時が提供される{@code NowProvider}に戻され,
     * {@link Time#now()}は現在日時を返すようになる.
     *
     * @throws IllegalMonitorStateException 現在のスレッドがロックを保持しない場合
     * @throws AssertionError               未ロック状態の場合にスローされる.
     */
    private static void unlock() {
        if (!DateTimeRule.locked) {
            throw new IllegalStateException("NowProvider is already unlocked.");
        }
        DateTimeRule.locked = false;
        Time.setNowProvider(Time.currentTimeProvider);
    }

    /**
     * Returns {@code o} if non-null, or throws {@code NullPointerException}.
     */
    private void requireNonNull(Object o) {
        if (o == null) {
            throw new NullPointerException();
        }
    }
}

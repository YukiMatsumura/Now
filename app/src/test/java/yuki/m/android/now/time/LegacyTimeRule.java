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

import java.lang.annotation.AnnotationFormatError;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * テスト時の現在日時を固定するテストルール.
 * <p>
 * {@link Now}でアノテートされたテストは{@link Jsr310Time#now()}が指定の日時を返すようになる.
 * {@link Now}でアノテートされていないテストは{@code DateTimeRule}の初期化方法に依存して現在日時が決定される.
 * <p>
 * このテストルールはスレッドセーフではない.
 * <p>
 * Created by YukiMatsumura on 2016/04/05.
 */
public class LegacyTimeRule implements TestRule {

    /**
     * 現在日時を{@code value}値で固定する.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface Now {

        /**
         * 固定する日時を指定する.
         * 日時フォーマットはISO8601形式, TimeZone UTC±00:00.
         */
        String value() default "2000-01-01T00:00:00Z";
    }

    // 多重lock/unlockを検知するためのフラグ. スレッドセーフは保証しない.
    private static boolean locked = false;

    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                Now annotation = description.getAnnotation(Now.class);
                if (annotation == null) {
                    base.evaluate();  // 現在時刻を固定しない
                    return;
                }

                try {
                    lockCurrentTime(() -> parse(annotation.value()));
                    base.evaluate();
                } finally {
                    unlockCurrentTime();
                }
            }
        };
    }

    private void lockCurrentTime(@NonNull LegacyTime.NowProvider provider) {
        if (LegacyTimeRule.locked) {
            throw new IllegalMonitorStateException("CurrentTimeProvider is locked.");
        }
        LegacyTimeRule.locked = true;
        LegacyTime.fixedCurrentTime(provider);
    }

    private void unlockCurrentTime() {
        if (!LegacyTimeRule.locked) {
            throw new IllegalMonitorStateException("CurrentTimeProvider is unlocked.");
        }
        LegacyTime.tickCurrentTime();
        LegacyTimeRule.locked = false;
    }

    private long parse(String iso8601z) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:dd'Z'");
            format.setTimeZone(TimeZone.getTimeZone("UTC"));
            return format.parse(iso8601z).getTime();
        } catch (ParseException e) {
            throw new AnnotationFormatError(e);
        }
    }
}

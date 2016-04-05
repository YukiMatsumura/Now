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

import android.support.annotation.VisibleForTesting;

import org.threeten.bp.Instant;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import static org.threeten.bp.Instant.ofEpochMilli;

public abstract class Time {

    public static ZoneId TIME_ZONE = ZoneOffset.UTC;

    interface NowProvider {

        long now();
    }

    @VisibleForTesting
    protected static NowProvider currentTimeProvider = System::currentTimeMillis;

    private static NowProvider nowProvider = currentTimeProvider;

    @VisibleForTesting
    protected static void setNowProvider(NowProvider provider) {
        nowProvider = provider;
    }

    public static long now() {
        return nowProvider.now();
    }

    public static String toIso8601z(long epoch) {
        return ZonedDateTime
                .ofInstant(ofEpochMilli(epoch), TIME_ZONE)
                .format(DateTimeFormatter.ISO_INSTANT.withZone(Time.TIME_ZONE));
    }

    public static long parseIso8601z(String iso8601z) {
        return ZonedDateTime
                .parse(iso8601z,
                        DateTimeFormatter.ISO_INSTANT.withZone(Time.TIME_ZONE))
                .toInstant()
                .toEpochMilli();
    }

    public static long beforeDays(long days) {
        return Instant.ofEpochMilli(Time.now())
                .atZone(TIME_ZONE)
                .minusDays(days)
                .toInstant()
                .toEpochMilli();
    }

    public static long afterDays(long days) {
        return Instant.ofEpochMilli(Time.now())
                .atZone(TIME_ZONE)
                .plusDays(days)
                .toInstant()
                .toEpochMilli();
    }
}

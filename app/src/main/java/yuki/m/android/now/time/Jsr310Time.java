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
import android.support.annotation.VisibleForTesting;

import org.threeten.bp.Clock;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.format.DateTimeFormatter;

public abstract class Jsr310Time {

    public static ZoneId TIME_ZONE = ZoneOffset.UTC;

    private static Clock clock = Clock.systemUTC();

    @VisibleForTesting
    protected static void fixedCurrentTime(@NonNull Clock clock) {
        Jsr310Time.clock = clock;
    }

    @VisibleForTesting
    protected static void tickCurrentTime() {
        Jsr310Time.clock = Clock.systemUTC();
    }

    public static long now() {
        return Jsr310Time.clock.millis();
    }

    /**
     * ISO8601(UTC)形式の文字列をepoch値に変換
     */
    public static long parseIso8601Z(String iso8601Z) {
        return ZonedDateTime
                .parse(iso8601Z,
                        DateTimeFormatter.ISO_INSTANT.withZone(TIME_ZONE))
                .toInstant()
                .toEpochMilli();
    }
}

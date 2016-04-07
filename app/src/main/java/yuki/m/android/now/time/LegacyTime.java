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

import android.annotation.SuppressLint;
import android.support.annotation.VisibleForTesting;

import java.lang.annotation.AnnotationFormatError;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * Created by YukiMatsumura on 16/04/06.
 */
public class LegacyTime {

    interface NowProvider {

        long now();
    }

    private static NowProvider systemCurrentTimeProvider = System::currentTimeMillis;

    private static NowProvider nowProvider = systemCurrentTimeProvider;

    @VisibleForTesting
    protected static void fixedCurrentTime(NowProvider provider) {
        nowProvider = provider;
    }

    @VisibleForTesting
    protected static void tickCurrentTime() {
        nowProvider = systemCurrentTimeProvider;
    }

    public static long now() {
        return nowProvider.now();
    }

    /**
     * ISO8601(UTC)形式の文字列をepoch timeに変換
     */
    @SuppressLint("SimpleDateFormat")
    public static long parseIso8601Z(String iso8601z) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:dd'Z'");
            format.setTimeZone(TimeZone.getTimeZone("UTC"));
            return format.parse(iso8601z).getTime();
        } catch (ParseException e) {
            throw new AnnotationFormatError(e);
        }
    }
}

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

import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import yuki.m.android.now.time.DateTimeRule.Now;

import static org.assertj.core.api.Assertions.assertThat;
import static yuki.m.android.now.time.Time.parseIso8601z;

/**
 * Created by YukiMatsumura on 16/04/06.
 */
@RunWith(Enclosed.class)
public class TimeTest {

    public static class DateTimeRuleで現在日時を固定化しないケース {

        @Rule
        public DateTimeRule dateTimeRule = new DateTimeRule();

        @Test
        public void CurrentTime_Now指定なし() throws Exception {
            // @Nowの日時指定がない＆DateTimeRuleの引数なし初期化で現在日時が固定化されていないことを試験する.
            // 厳密には現在日時が2000-01-01T00:00:00Zを指す可能性はあるが、
            // 過去の日時が現在の日時と恒久的に等しくならない性質から, 下記で良しとする.
            assertThat(Time.now()).isNotEqualTo(parseIso8601z("2000-01-01T00:00:00Z"));
        }

        @Test
        @Now("2000-01-01T00:00:00Z")
        public void CurrentTime_Now指定あり() throws Exception {
            // @Nowの日時で現在日時が固定化されていることを試験する.
            // 厳密には現在日時が2000-01-01T00:00:00Zを指す可能性はあるが、
            // 過去の日時が現在の日時と恒久的に等しくならない性質から, 下記で良しとする.
            assertThat(Time.now()).isEqualTo(parseIso8601z("2000-01-01T00:00:00Z"));
        }

        @Test
        @Now("2000-01-02T00:00:00Z")
        public void 日時加算_Now指定あり() throws Exception {
            // @Nowで指定した日時の13日後の日時が計算されることを試験する
            assertThat(Time.afterDays(13)).isEqualTo(parseIso8601z("2000-01-15T00:00:00Z"));
        }

        @Test
        public void 日時加算_Now指定なし() throws Exception {
            // アノテーション指定無しで13日後の日時が計算されることを試験する.
            // このテストケースは現在日時が"2000-01-01T00:00:00Z"で固定化されていないことを期待する.
            assertThat(Time.afterDays(13)).isNotEqualTo(parseIso8601z("2000-01-13T00:00:00Z"));
            assertThat(Time.afterDays(13)).isNotEqualTo(parseIso8601z("2000-01-14T00:00:00Z"));
        }
    }
    public static class DateTimeRuleで現在日時を固定化するケース {

        @Rule
        public DateTimeRule dateTimeRule = new DateTimeRule("2000-02-01T00:00:00Z");

        @Test
        public void CurrentTime_Now指定なし() throws Exception {
            // @Nowの日時指定がない＆DateTimeRuleの引数あり初期化で現在日時が固定化されていることを試験する.
            // 厳密には現在日時が2000-02-01T00:00:00Zを指す可能性はあるが、
            // 過去の日時が現在の日時と恒久的に等しくならない性質から, 下記で良しとする.
            assertThat(Time.now()).isEqualTo(parseIso8601z("2000-02-01T00:00:00Z"));
        }

        @Test
        @Now("2000-03-01T00:00:00Z")
        public void CurrentTime_Now指定あり() throws Exception {
            // @Nowの日時で現在日時が固定化されていることを試験する.
            // 厳密には現在日時が2000-03-01T00:00:00Zを指す可能性はあるが、
            // 過去の日時が現在の日時と恒久的に等しくならない性質から, 下記で良しとする.
            assertThat(Time.now()).isEqualTo(parseIso8601z("2000-03-01T00:00:00Z"));
        }

        @Test
        @Now("2000-03-02T00:00:00Z")
        public void 日時加算_Now指定あり() throws Exception {
            // @Nowで指定した日時の13日後の日時が計算されることを試験する
            // このテストケースは現在日時が@Nowにより"2000-03-01T00:00:00Z"で固定化されていることを期待する.
            assertThat(Time.afterDays(13)).isEqualTo(parseIso8601z("2000-03-15T00:00:00Z"));
        }

        @Test
        public void 日時加算_Now指定なし() throws Exception {
            // アノテーション指定無しで13日後の日時が計算されることを試験する.
            // このテストケースは現在日時がDateTimeRuleの初期化により"2000-02-01T00:00:00Z"で
            // 固定化されていることを期待する.
            assertThat(Time.afterDays(13)).isEqualTo(parseIso8601z("2000-02-14T00:00:00Z"));
        }
    }
}

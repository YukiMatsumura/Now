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

import yuki.m.android.now.time.LegacyTimeRule.Now;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by YukiMatsumura on 16/04/07.
 */
public class LegacyTimeTest {

    @Rule
    public LegacyTimeRule legacyTimeRule = new LegacyTimeRule();

    @Test
    public void Now指定なし() throws Exception {
        assertThat(LegacyTime.now()).isNotEqualTo(
                LegacyTime.parseIso8601Z("2000-01-01T00:00:00Z"));
    }

    @Test
    @Now("2017-01-01T00:00:00Z")
    public void Now指定あり() throws Exception {
        assertThat(LegacyTime.now()).isEqualTo(
                LegacyTime.parseIso8601Z("2017-01-01T00:00:00Z"));
    }
}

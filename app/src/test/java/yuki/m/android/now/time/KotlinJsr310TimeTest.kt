package yuki.m.android.now.time

import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import yuki.m.android.now.time.KotlinJsr310TimeRule.Now

class KotlinJsr310TimeTest {

    @Rule @JvmField val kotlinJsr310TimeRule = KotlinJsr310TimeRule()

    @Test
    fun Now指定なし() {
        assertThat(Jsr310Time.now()).isNotEqualTo(
                Jsr310Time.parseIso8601Z("2000-01-01T00:00:00Z"))
    }

    @Test
    @Now(2017, 1, 1, 0, 0, 0)
    fun Now指定あり() {
        assertThat(Jsr310Time.now()).isEqualTo(
                Jsr310Time.parseIso8601Z("2017-01-01T00:00:00Z"))
    }

}
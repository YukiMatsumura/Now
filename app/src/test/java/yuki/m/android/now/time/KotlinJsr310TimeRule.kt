package yuki.m.android.now.time

import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import org.threeten.bp.Clock
import org.threeten.bp.Instant.ofEpochMilli
import org.threeten.bp.ZoneId
import org.threeten.bp.ZoneOffset
import org.threeten.bp.ZonedDateTime

/**
 * テスト時の現在日時を固定するテストルール.
 * <p>
 * [Now]でアノテートされたテストは[Jsr310Time.now]が指定の日時を返すようになる.
 * [Now]でアノテートされていないテストはDateTimeRuleの初期化方法に依存して現在日時が決定される.
 * <p>
 * このテストルールはスレッドセーフではない.
 */
class KotlinJsr310TimeRule : TestRule {

    /**
     * 現在日時を与えられた引数の値で固定する.
     */
    @Target(AnnotationTarget.FUNCTION)
    annotation class Now(
            val year: Int,
            val month: Int,
            val dayOfMonth: Int,
            val hour: Int,
            val minute: Int,
            val second: Int = 0
    )

    companion object {

        // 多重lock/unlockを検知するためのフラグ. スレッドセーフは保証しない.
        private var locked = false
    }

    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            override fun evaluate() {
                val annotation = description.getAnnotation(Now::class.java)
                if (annotation == null) {
                    base.evaluate()  // 現在時刻を固定しない
                    return
                }

                try {
                    lockCurrentTime(LegacyTime.NowProvider { parse(annotation) })
                    base.evaluate()
                } finally {
                    unlockCurrentTime()
                }
            }
        }
    }

    private fun lockCurrentTime(provider: LegacyTime.NowProvider) {
        if (KotlinJsr310TimeRule.locked) {
            throw IllegalMonitorStateException("Clock is locked.")
        }
        KotlinJsr310TimeRule.locked = true
        Jsr310Time.fixedCurrentTime(
                Clock.fixed(ofEpochMilli(provider.now()), ZoneOffset.UTC))
    }

    private fun unlockCurrentTime() {
        if (!KotlinJsr310TimeRule.locked) {
            throw IllegalMonitorStateException("Clock is unlocked.")
        }
        Jsr310Time.tickCurrentTime()
        KotlinJsr310TimeRule.locked = false
    }

    private fun parse(a: Now): Long {
        val utcZoneId = ZoneId.of("Z")
        val time = ZonedDateTime.of(a.year, a.month, a.dayOfMonth, a.hour, a.minute, a.second, 0, utcZoneId)
        return time.toInstant().toEpochMilli()
    }
}
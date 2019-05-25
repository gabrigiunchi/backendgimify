package com.gabrigiunchi.backendtesi.model

import com.gabrigiunchi.backendtesi.model.type.RepetitionType
import org.assertj.core.api.Assertions
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalTime

class RepeatedIntervalTest {

    @Test(expected = IllegalArgumentException::class)
    fun `Should not create an interval with start after the end`() {
        RepeatedInterval("2019-05-23T12:00:00", "2019-05-23T11:00:00", RepetitionType.daily)
    }

    @Test
    fun `Should say if it contains a date (no repetition)`() {
        val interval = RepeatedInterval("2019-05-24T10:00:00", "2019-05-24T12:00:00", RepetitionType.none)
        Assertions.assertThat(interval.contains("2019-05-24T10:00:00")).isTrue()
        Assertions.assertThat(interval.contains("2019-05-24T11:00:00")).isTrue()
        Assertions.assertThat(interval.contains("2019-05-24T12:00:00")).isTrue()

        Assertions.assertThat(interval.contains("2019-05-24T09:59:59")).isFalse()
        Assertions.assertThat(interval.contains("2019-05-24T08:00:00")).isFalse()
        Assertions.assertThat(interval.contains("2019-05-24T12:00:01")).isFalse()
    }

    @Test
    fun `Should say if it contains a date (daily repetition)`() {
        val interval = RepeatedInterval("2019-05-23T10:00:00", "2019-05-23T12:00:00", RepetitionType.daily)
        Assertions.assertThat(interval.contains("2019-05-24T10:00:00")).isTrue()
        Assertions.assertThat(interval.contains("2019-05-24T11:00:00")).isTrue()
        Assertions.assertThat(interval.contains("2019-05-24T12:00:00")).isTrue()

        Assertions.assertThat(interval.contains("2019-05-24T09:59:59")).isFalse()
        Assertions.assertThat(interval.contains("2019-05-24T08:00:00")).isFalse()
        Assertions.assertThat(interval.contains("2019-05-24T12:00:01")).isFalse()
    }

    @Test
    fun `Should say if it contains a date (weekly repetition)`() {
        val interval = RepeatedInterval.create(DayOfWeek.THURSDAY, LocalTime.parse("10:00"), LocalTime.parse("12:00"))
        Assertions.assertThat(interval.contains("2019-05-30T10:00:00")).isTrue()
        Assertions.assertThat(interval.contains("2019-05-30T11:00:00")).isTrue()
        Assertions.assertThat(interval.contains("2019-05-30T12:00:00")).isTrue()

        Assertions.assertThat(interval.contains("2019-05-30T09:59:59")).isFalse()
        Assertions.assertThat(interval.contains("2019-05-30T08:00:00")).isFalse()
        Assertions.assertThat(interval.contains("2019-05-30T12:00:01")).isFalse()

        Assertions.assertThat(interval.contains("2019-05-29T11:00:00")).isFalse()
    }

    @Test
    fun `Should say if it contains a date (monthly repetition)`() {
        val interval = RepeatedInterval("2019-05-23T10:00:00", "2019-05-23T12:00:00", RepetitionType.monthly)
        Assertions.assertThat(interval.contains("2019-06-23T10:00:00")).isTrue()
        Assertions.assertThat(interval.contains("2019-06-23T11:00:00")).isTrue()
        Assertions.assertThat(interval.contains("2019-06-23T12:00:00")).isTrue()

        Assertions.assertThat(interval.contains("2019-06-23T09:59:59")).isFalse()
        Assertions.assertThat(interval.contains("2019-06-23T08:00:00")).isFalse()
        Assertions.assertThat(interval.contains("2019-06-23T12:00:01")).isFalse()

        Assertions.assertThat(interval.contains("2019-05-30T11:00:00")).isFalse()
    }

    @Test
    fun `Should say if it contains a date (yearly repetition)`() {
        val interval = RepeatedInterval("2019-05-23T10:00:00", "2019-05-23T12:00:00", RepetitionType.yearly)
        Assertions.assertThat(interval.contains("2020-05-23T10:00:00")).isTrue()
        Assertions.assertThat(interval.contains("2020-05-23T11:00:00")).isTrue()
        Assertions.assertThat(interval.contains("2020-05-23T12:00:00")).isTrue()

        Assertions.assertThat(interval.contains("2020-05-23T09:59:59")).isFalse()
        Assertions.assertThat(interval.contains("2020-05-23T08:00:00")).isFalse()
        Assertions.assertThat(interval.contains("2020-05-23T12:00:01")).isFalse()

        Assertions.assertThat(interval.contains("2019-05-24T11:00:00")).isFalse()
    }

    @Test
    fun `Should say if it contains a date (weekly repetition and repetition end)`() {
        val interval = RepeatedInterval(
                "2019-05-23T10:00:00",
                "2019-05-23T12:00:00",
                RepetitionType.weekly,
                "2019-07-18T00:00:00")

        Assertions.assertThat(interval.contains("2019-07-11T11:00:00")).isTrue()
        Assertions.assertThat(interval.contains("2019-07-18T11:00:00")).isFalse()
        Assertions.assertThat(interval.contains("2019-07-25T11:00:00")).isFalse()
    }
}

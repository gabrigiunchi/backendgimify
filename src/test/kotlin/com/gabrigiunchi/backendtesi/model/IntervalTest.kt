package com.gabrigiunchi.backendtesi.model

import org.assertj.core.api.Assertions
import org.junit.Test
import java.time.OffsetDateTime
import java.time.ZoneId

class IntervalTest {

    @Test(expected = IllegalArgumentException::class)
    fun `Should not create an interval with start after the end`() {
        Interval("2019-05-23T12:00:00", "2019-05-23T11:00:00")
    }

    @Test
    fun `Should say if it contains a date`() {
        val interval = Interval("2019-05-23T10:00:00", "2019-05-23T12:00:00")
        Assertions.assertThat(interval.contains("2019-05-23T10:00:00")).isTrue()
        Assertions.assertThat(interval.contains("2019-05-23T11:00:00")).isTrue()
        Assertions.assertThat(interval.contains("2019-05-23T12:00:00")).isTrue()

        Assertions.assertThat(interval.contains("2019-05-23T09:59:59")).isFalse()
        Assertions.assertThat(interval.contains("2019-05-23T08:00:00")).isFalse()
        Assertions.assertThat(interval.contains("2019-05-23T12:00:01")).isFalse()
    }

    @Test
    fun `Should say if it contains an interval`() {
        val interval = Interval("2019-05-23T10:00:00", "2019-05-23T12:00:00")

        Assertions.assertThat(interval.contains(Interval("2019-05-23T08:00:00", "2019-05-23T11:00:00"))).isFalse()
        Assertions.assertThat(interval.contains(Interval("2019-05-23T10:00:00", "2019-05-23T12:00:00"))).isTrue()
        Assertions.assertThat(interval.contains(Interval("2019-05-23T11:00:00", "2019-05-23T12:00:01"))).isFalse()
        Assertions.assertThat(interval.contains(Interval("2019-05-23T08:00:00", "2019-05-23T16:00:10"))).isFalse()

        Assertions.assertThat(interval.contains(Interval("2019-05-23T08:00:00", "2019-05-23T09:59:59"))).isFalse()
        Assertions.assertThat(interval.contains(Interval("2019-05-23T12:00:10", "2019-05-23T16:00:00"))).isFalse()
    }

    @Test
    fun `Should say if it overlaps an interval`() {
        val interval = Interval("2019-05-23T10:00:00", "2019-05-23T12:00:00")

        Assertions.assertThat(interval.overlaps(Interval("2019-05-23T08:00:00", "2019-05-23T11:00:00"))).isTrue()
        Assertions.assertThat(interval.overlaps(Interval("2019-05-23T10:00:00", "2019-05-23T12:00:00"))).isTrue()
        Assertions.assertThat(interval.overlaps(Interval("2019-05-23T11:00:00", "2019-05-23T12:00:01"))).isTrue()
        Assertions.assertThat(interval.overlaps(Interval("2019-05-23T08:00:00", "2019-05-23T16:00:10"))).isTrue()

        Assertions.assertThat(interval.overlaps(Interval("2019-05-23T08:00:00", "2019-05-23T09:59:59"))).isFalse()
        Assertions.assertThat(interval.overlaps(Interval("2019-05-23T12:00:10", "2019-05-23T16:00:00"))).isFalse()
    }
}
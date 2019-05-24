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
    fun `Should say if it contains a local date`() {
        val interval = Interval("2019-05-23T10:00:00", "2019-05-23T12:00:00")
        Assertions.assertThat(interval.contains("2019-05-23T10:00:00")).isTrue()
        Assertions.assertThat(interval.contains("2019-05-23T11:00:00")).isTrue()
        Assertions.assertThat(interval.contains("2019-05-23T12:00:00")).isTrue()

        Assertions.assertThat(interval.contains("2019-05-23T09:59:59")).isFalse()
        Assertions.assertThat(interval.contains("2019-05-23T08:00:00")).isFalse()
        Assertions.assertThat(interval.contains("2019-05-23T12:00:01")).isFalse()
    }

    @Test
    fun `Should say if it overlaps a date interval`() {
        val interval = Interval("2019-05-23T10:00:00", "2019-05-23T12:00:00")

        Assertions.assertThat(interval.overlaps(Interval("2019-05-23T08:00:00", "2019-05-23T11:00:00"))).isTrue()
        Assertions.assertThat(interval.overlaps(Interval("2019-05-23T10:00:00", "2019-05-23T12:00:00"))).isTrue()
        Assertions.assertThat(interval.overlaps(Interval("2019-05-23T11:00:00", "2019-05-23T12:00:01"))).isTrue()
        Assertions.assertThat(interval.overlaps(Interval("2019-05-23T08:00:00", "2019-05-23T16:00:10"))).isTrue()

        Assertions.assertThat(interval.overlaps(Interval("2019-05-23T08:00:00", "2019-05-23T09:59:59"))).isFalse()
        Assertions.assertThat(interval.overlaps(Interval("2019-05-23T12:00:10", "2019-05-23T16:00:00"))).isFalse()
    }


    @Test
    fun `Should say if it contains a zoned date`() {
        val utc = ZoneId.of("UTC")
        val newYork = ZoneId.of("America/New_York")
        val interval = Interval("2019-05-23T10:00:00", "2019-05-23T12:00:00")

        // UTC
        Assertions.assertThat(interval.contains(OffsetDateTime.parse("2019-05-23T10:00:00+00:00"), utc)).isTrue()
        Assertions.assertThat(interval.contains(OffsetDateTime.parse("2019-05-23T12:00:00+00:00"), utc)).isTrue()
        Assertions.assertThat(interval.contains(OffsetDateTime.parse("2019-05-23T09:59:59+00:00"), utc)).isFalse()
        Assertions.assertThat(interval.contains(OffsetDateTime.parse("2019-05-23T08:00:00+00:00"), utc)).isFalse()
        Assertions.assertThat(interval.contains(OffsetDateTime.parse("2019-05-23T12:00:01+00:00"), utc)).isFalse()


        // New York
        Assertions.assertThat(interval.contains(OffsetDateTime.parse("2019-05-23T10:00:00-04:00"), newYork)).isTrue()
        Assertions.assertThat(interval.contains(OffsetDateTime.parse("2019-05-23T11:00:00-04:00"), newYork)).isTrue()
        Assertions.assertThat(interval.contains(OffsetDateTime.parse("2019-05-23T12:00:00-04:00"), newYork)).isTrue()
        Assertions.assertThat(interval.contains(OffsetDateTime.parse("2019-05-23T09:59:59-04:00"), newYork)).isFalse()
        Assertions.assertThat(interval.contains(OffsetDateTime.parse("2019-05-23T12:00:01-04:00"), newYork)).isFalse()
    }

    @Test
    fun `Should say if it contains a zoned interval`() {
        val utc = ZoneId.of("UTC")
        val newYork = ZoneId.of("America/New_York")
        val interval = Interval("2019-05-23T10:00:00", "2019-05-23T12:00:00")

        // UTC
        val i1 = ZonedInterval("2019-05-23T10:00:00+00:00", "2019-05-23T12:00:00+00:00")
        val i2 = ZonedInterval("2019-05-23T09:59:59+00:00", "2019-05-23T12:00:00+00:00")
        val i3 = ZonedInterval("2019-05-23T10:00:00+00:00", "2019-05-23T12:00:01+00:00")
        val i4 = ZonedInterval("2019-05-23T09:59:59+00:00", "2019-05-23T12:00:01+00:00")

        Assertions.assertThat(interval.contains(i1, utc)).isTrue()
        Assertions.assertThat(interval.contains(i2, utc)).isFalse()
        Assertions.assertThat(interval.contains(i3, utc)).isFalse()
        Assertions.assertThat(interval.contains(i4, utc)).isFalse()

        // New York
        val i5 = ZonedInterval("2019-05-23T10:00:00-04:00", "2019-05-23T12:00:00-04:00")
        val i6 = ZonedInterval("2019-05-23T09:59:59-04:00", "2019-05-23T12:00:00-04:00")
        val i7 = ZonedInterval("2019-05-23T10:00:00-04:00", "2019-05-23T12:00:01-04:00")
        val i8 = ZonedInterval("2019-05-23T09:59:59-04:00", "2019-05-23T12:00:01-04:00")

        Assertions.assertThat(interval.contains(i5, newYork)).isTrue()
        Assertions.assertThat(interval.contains(i6, newYork)).isFalse()
        Assertions.assertThat(interval.contains(i7, newYork)).isFalse()
        Assertions.assertThat(interval.contains(i8, newYork)).isFalse()
    }
}
package com.gabrigiunchi.backendtesi.model

import com.gabrigiunchi.backendtesi.model.time.ZonedInterval
import org.assertj.core.api.Assertions
import org.junit.Test
import java.time.OffsetDateTime
import java.time.ZoneId

class ZonedIntervalTest {

    @Test(expected = IllegalArgumentException::class)
    fun `Should not create an interval with start after the end`() {
        ZonedInterval("2019-05-23T12:00:00+00:00", "2019-05-23T11:00:00+00:00")
    }

    @Test
    fun `Should say if it contains a date`() {
        val interval = ZonedInterval("2019-10-10T10:00:00+00:00", "2019-10-10T10:30:00+00:00")

        Assertions.assertThat(interval.contains(OffsetDateTime.parse("2019-10-10T09:59:59+00:00"))).isFalse()
        Assertions.assertThat(interval.contains(OffsetDateTime.parse("2019-10-10T10:00:00+00:00"))).isTrue()
        Assertions.assertThat(interval.contains(OffsetDateTime.parse("2019-10-10T10:30:00+00:00"))).isTrue()
        Assertions.assertThat(interval.contains(OffsetDateTime.parse("2019-10-10T10:30:01+00:00"))).isFalse()
        Assertions.assertThat(interval.contains(OffsetDateTime.parse("2019-10-10T12:00:00+02:00"))).isTrue()
    }

    @Test
    fun `Should say if it contains a zoned interval`() {
        val interval = ZonedInterval("2019-05-23T10:00:00+00:00", "2019-05-23T12:00:00+00:00")

        Assertions.assertThat(interval.contains(ZonedInterval("2019-05-23T08:00:00+00:00", "2019-05-23T11:00:00+00:00"))).isFalse()
        Assertions.assertThat(interval.contains(ZonedInterval("2019-05-23T10:00:00+00:00", "2019-05-23T12:00:00+00:00"))).isTrue()
        Assertions.assertThat(interval.contains(ZonedInterval("2019-05-23T11:00:00+00:00", "2019-05-23T12:00:01+00:00"))).isFalse()
        Assertions.assertThat(interval.contains(ZonedInterval("2019-05-23T08:00:00+00:00", "2019-05-23T16:00:10+00:00"))).isFalse()

        Assertions.assertThat(interval.contains(ZonedInterval("2019-05-23T08:00:00+00:00", "2019-05-23T09:59:59+00:00"))).isFalse()
        Assertions.assertThat(interval.contains(ZonedInterval("2019-05-23T12:00:10+00:00", "2019-05-23T16:00:00+00:00"))).isFalse()
    }

    @Test
    fun `Should say if it overlaps a zoned interval`() {
        val interval = ZonedInterval("2019-05-23T10:00:00+00:00", "2019-05-23T12:00:00+00:00")

        Assertions.assertThat(interval.overlaps(ZonedInterval("2019-05-23T08:00:00+00:00", "2019-05-23T11:00:00+00:00"))).isTrue()
        Assertions.assertThat(interval.overlaps(ZonedInterval("2019-05-23T10:00:00+00:00", "2019-05-23T12:00:00+00:00"))).isTrue()
        Assertions.assertThat(interval.overlaps(ZonedInterval("2019-05-23T11:00:00+00:00", "2019-05-23T12:00:01+00:00"))).isTrue()
        Assertions.assertThat(interval.overlaps(ZonedInterval("2019-05-23T08:00:00+00:00", "2019-05-23T16:00:10+00:00"))).isTrue()

        Assertions.assertThat(interval.overlaps(ZonedInterval("2019-05-23T08:00:00+00:00", "2019-05-23T09:59:59+00:00"))).isFalse()
        Assertions.assertThat(interval.overlaps(ZonedInterval("2019-05-23T12:00:10+00:00", "2019-05-23T16:00:00+00:00"))).isFalse()
    }

    @Test
    fun `Should convert into a local interval`() {
        val interval = ZonedInterval("2019-05-23T10:00:00+00:00", "2019-05-23T12:00:00+00:00")

        var localInterval = interval.toLocalInterval(ZoneId.of("UTC"))
        Assertions.assertThat(localInterval.start.toString()).isEqualTo("2019-05-23T10:00")
        Assertions.assertThat(localInterval.end.toString()).isEqualTo("2019-05-23T12:00")

        localInterval = interval.toLocalInterval(ZoneId.of("Europe/Rome"))
        Assertions.assertThat(localInterval.start.toString()).isEqualTo("2019-05-23T12:00")
        Assertions.assertThat(localInterval.end.toString()).isEqualTo("2019-05-23T14:00")

        localInterval = interval.toLocalInterval(ZoneId.of("America/New_York"))
        Assertions.assertThat(localInterval.start.toString()).isEqualTo("2019-05-23T06:00")
        Assertions.assertThat(localInterval.end.toString()).isEqualTo("2019-05-23T08:00")

        localInterval = interval.toLocalInterval(ZoneId.of("America/Los_Angeles"))
        Assertions.assertThat(localInterval.start.toString()).isEqualTo("2019-05-23T03:00")
        Assertions.assertThat(localInterval.end.toString()).isEqualTo("2019-05-23T05:00")
    }
}
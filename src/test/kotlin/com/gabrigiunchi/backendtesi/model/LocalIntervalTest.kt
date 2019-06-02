package com.gabrigiunchi.backendtesi.model

import com.gabrigiunchi.backendtesi.model.time.LocalInterval
import org.assertj.core.api.Assertions
import org.junit.Test

class LocalIntervalTest {

    @Test(expected = IllegalArgumentException::class)
    fun `Should not create an interval with start after the end`() {
        LocalInterval("2019-05-23T12:00:00", "2019-05-23T11:00:00")
    }

    @Test
    fun `Should say if it contains a date`() {
        val interval = LocalInterval("2019-05-23T10:00:00", "2019-05-23T12:00:00")
        Assertions.assertThat(interval.contains("2019-05-23T10:00:00")).isTrue()
        Assertions.assertThat(interval.contains("2019-05-23T11:00:00")).isTrue()
        Assertions.assertThat(interval.contains("2019-05-23T12:00:00")).isTrue()

        Assertions.assertThat(interval.contains("2019-05-23T09:59:59")).isFalse()
        Assertions.assertThat(interval.contains("2019-05-23T08:00:00")).isFalse()
        Assertions.assertThat(interval.contains("2019-05-23T12:00:01")).isFalse()
    }

    @Test
    fun `Should say if it contains an interval`() {
        val interval = LocalInterval("2019-05-23T10:00:00", "2019-05-23T12:00:00")

        Assertions.assertThat(interval.contains(LocalInterval("2019-05-23T08:00:00", "2019-05-23T11:00:00"))).isFalse()
        Assertions.assertThat(interval.contains(LocalInterval("2019-05-23T10:00:00", "2019-05-23T12:00:00"))).isTrue()
        Assertions.assertThat(interval.contains(LocalInterval("2019-05-23T11:00:00", "2019-05-23T12:00:01"))).isFalse()
        Assertions.assertThat(interval.contains(LocalInterval("2019-05-23T08:00:00", "2019-05-23T16:00:10"))).isFalse()

        Assertions.assertThat(interval.contains(LocalInterval("2019-05-23T08:00:00", "2019-05-23T09:59:59"))).isFalse()
        Assertions.assertThat(interval.contains(LocalInterval("2019-05-23T12:00:10", "2019-05-23T16:00:00"))).isFalse()
    }

    @Test
    fun `Should say if it overlaps an interval`() {
        val interval = LocalInterval("2019-05-23T10:00:00", "2019-05-23T12:00:00")

        Assertions.assertThat(interval.overlaps(LocalInterval("2019-05-23T08:00:00", "2019-05-23T11:00:00"))).isTrue()
        Assertions.assertThat(interval.overlaps(LocalInterval("2019-05-23T10:00:00", "2019-05-23T12:00:00"))).isTrue()
        Assertions.assertThat(interval.overlaps(LocalInterval("2019-05-23T11:00:00", "2019-05-23T12:00:01"))).isTrue()
        Assertions.assertThat(interval.overlaps(LocalInterval("2019-05-23T08:00:00", "2019-05-23T16:00:10"))).isTrue()

        Assertions.assertThat(interval.overlaps(LocalInterval("2019-05-23T08:00:00", "2019-05-23T09:59:59"))).isFalse()
        Assertions.assertThat(interval.overlaps(LocalInterval("2019-05-23T12:00:00", "2019-05-23T16:00:00"))).isFalse()
        Assertions.assertThat(interval.overlaps(LocalInterval("2019-05-23T12:00:01", "2019-05-23T16:00:00"))).isFalse()
    }

    @Test
    fun `Should say if it is within the same day`() {
        Assertions.assertThat(LocalInterval("2019-05-23T00:00:00", "2019-05-23T23:59:59").isWithinSameDay()).isTrue()
        Assertions.assertThat(LocalInterval("2019-05-23T23:59:59", "2019-05-24T00:00:00").isWithinSameDay()).isFalse()
        Assertions.assertThat(LocalInterval("2019-05-23T08:00:00", "2019-05-25T11:00:00").isWithinSameDay()).isFalse()
    }

}
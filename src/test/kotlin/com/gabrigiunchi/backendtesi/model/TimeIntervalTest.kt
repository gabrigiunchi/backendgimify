package com.gabrigiunchi.backendtesi.model

import com.gabrigiunchi.backendtesi.util.DateDecorator
import org.assertj.core.api.Assertions
import org.junit.Test
import java.time.OffsetTime

class TimeIntervalTest {

    @Test(expected = IllegalArgumentException::class)
    fun `Should raise an exception if the start is before the end`() {
        TimeInterval(OffsetTime.parse("10:00:00+00:00"), OffsetTime.parse("08:00:00+00:00"))
    }


    @Test
    fun `Should say if a time intervals contains a date`() {
        val date = DateDecorator.of("2018-10-10T10:00:00+0000").date
        val timeInterval = TimeInterval(OffsetTime.parse("08:00+00:00"), OffsetTime.parse("16:00+00:00"))
        Assertions.assertThat(timeInterval.contains(date)).isTrue()
    }

    @Test
    fun `Should say if a time intervals contains a date considering timeZone`() {
        val date = DateDecorator.of("2018-10-10T16:00:00+0002").date
        val timeInterval = TimeInterval(OffsetTime.parse("10:00+00:00"), OffsetTime.parse("16:00+00:00"))
        Assertions.assertThat(timeInterval.contains(date)).isTrue()
    }

    @Test
    fun `Should say if a time intervals contains a date if date == intervalStart`() {
        val date = DateDecorator.of("2018-10-10T10:00:00+0000").date
        val timeInterval = TimeInterval(OffsetTime.parse("10:00+00:00"), OffsetTime.parse("16:00+00:00"))
        Assertions.assertThat(timeInterval.contains(date)).isTrue()
    }


    @Test
    fun `Should say if a time intervals contains a date if date == intervalEnd`() {
        val date = DateDecorator.of("2018-10-10T16:00:00+0000").date
        val timeInterval = TimeInterval(OffsetTime.parse("10:00+00:00"), OffsetTime.parse("16:00+00:00"))
        Assertions.assertThat(timeInterval.contains(date)).isTrue()
    }

    @Test
    fun `Should say if a time intervals contains a date if date == intervalEnd and considering timeZone`() {
        val date = DateDecorator.of("2018-10-10T12:00:00+0002").date
        val timeInterval = TimeInterval(OffsetTime.parse("10:00+00:00"), OffsetTime.parse("16:00+00:00"))
        Assertions.assertThat(timeInterval.contains(date)).isTrue()
    }
}
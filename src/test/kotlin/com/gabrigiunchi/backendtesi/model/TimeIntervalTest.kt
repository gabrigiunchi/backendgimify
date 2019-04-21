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
        val date = DateDecorator.of("2018-10-10T16:00:00+0200").date
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
        val date = DateDecorator.of("2018-10-10T12:00:00+0200").date
        val timeInterval = TimeInterval(OffsetTime.parse("10:00+00:00"), OffsetTime.parse("16:00+00:00"))
        Assertions.assertThat(timeInterval.contains(date)).isTrue()
    }

    /************************************ CONTAINS A DATE INTERVAL *********************************************************/

    @Test
    fun `Should say if contains a date interval`() {
        val dateInterval = DateInterval(DateDecorator.of("2019-01-01T12:00:00+0000").date,
                DateDecorator.of("2019-01-01T14:00:00+0000").date)
        val timeInterval = TimeInterval(OffsetTime.parse("10:00+00:00"), OffsetTime.parse("16:00+00:00"))

        Assertions.assertThat(timeInterval.contains(dateInterval)).isTrue()
    }

    @Test
    fun `Should say if contains a date interval (edge case with start time)`() {
        val dateInterval = DateInterval(DateDecorator.of("2019-01-01T10:00:00+0000").date,
                DateDecorator.of("2019-01-01T12:00:00+0000").date)
        val timeInterval = TimeInterval(OffsetTime.parse("10:00+00:00"), OffsetTime.parse("16:00+00:00"))

        Assertions.assertThat(timeInterval.contains(dateInterval)).isTrue()
    }

    @Test
    fun `Should say if contains a date interval (edge case with end time)`() {
        val dateInterval = DateInterval(DateDecorator.of("2019-01-01T12:00:00+0000").date,
                DateDecorator.of("2019-01-01T16:00:00+0000").date)
        val timeInterval = TimeInterval(OffsetTime.parse("10:00+00:00"), OffsetTime.parse("16:00+00:00"))

        Assertions.assertThat(timeInterval.contains(dateInterval)).isTrue()
    }

    @Test
    fun `Should say if contains a date interval (edge case with start time and end time)`() {
        val dateInterval = DateInterval(DateDecorator.of("2019-01-01T10:00:00+0000").date,
                DateDecorator.of("2019-01-01T16:00:00+0000").date)
        val timeInterval = TimeInterval(OffsetTime.parse("10:00+00:00"), OffsetTime.parse("16:00+00:00"))

        Assertions.assertThat(timeInterval.contains(dateInterval)).isTrue()
    }

    @Test
    fun `Should say if contains a date interval (edge case with timezone)`() {
        val dateInterval = DateInterval(DateDecorator.of("2019-01-01T11:00:00+0100").date,
                DateDecorator.of("2019-01-01T17:00:00+0100").date)
        val timeInterval = TimeInterval(OffsetTime.parse("10:00+00:00"), OffsetTime.parse("16:00+00:00"))

        Assertions.assertThat(timeInterval.contains(dateInterval)).isTrue()
    }

    @Test
    fun `Should say if does not contain a date interval if the start is invalid`() {
        val dateInterval = DateInterval(DateDecorator.of("2019-01-01T08:00:00+0000").date,
                DateDecorator.of("2019-01-01T15:00:00+0000").date)
        val timeInterval = TimeInterval(OffsetTime.parse("10:00+00:00"), OffsetTime.parse("16:00+00:00"))

        Assertions.assertThat(timeInterval.contains(dateInterval)).isFalse()
    }

    @Test
    fun `Should say if does not contain a date interval if the end is invalid`() {
        val dateInterval = DateInterval(DateDecorator.of("2019-01-01T11:00:00+0000").date,
                DateDecorator.of("2019-01-01T17:00:00+0000").date)
        val timeInterval = TimeInterval(OffsetTime.parse("10:00+00:00"), OffsetTime.parse("16:00+00:00"))

        Assertions.assertThat(timeInterval.contains(dateInterval)).isFalse()
    }

    @Test
    fun `Should say if does not contain a date interval if both the start and the end are invalid`() {
        val timeInterval = TimeInterval(OffsetTime.parse("10:00+00:00"), OffsetTime.parse("16:00+00:00"))

        Assertions.assertThat(timeInterval.contains(
                DateInterval(
                        DateDecorator.of("2019-01-01T18:00:00+0000").date,
                        DateDecorator.of("2019-01-01T19:00:00+0000").date))
        ).isFalse()

        Assertions.assertThat(timeInterval.contains(
                DateInterval(
                        DateDecorator.of("2019-01-01T07:00:00+0000").date,
                        DateDecorator.of("2019-01-01T08:00:00+0000").date))
        ).isFalse()
    }

    @Test
    fun `Should say if does not contain a date interval if the date interval is not within the same day`() {
        val timeInterval = TimeInterval(OffsetTime.parse("10:00+00:00"), OffsetTime.parse("16:00+00:00"))

        Assertions.assertThat(timeInterval.contains(
                DateInterval(
                        DateDecorator.of("2019-04-21T11:00:00+0000").date,
                        DateDecorator.of("2019-04-22T14:00:00+0000").date))
        ).isFalse()
    }
}
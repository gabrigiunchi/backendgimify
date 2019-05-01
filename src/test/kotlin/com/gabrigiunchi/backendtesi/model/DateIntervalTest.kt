package com.gabrigiunchi.backendtesi.model

import com.gabrigiunchi.backendtesi.MockEntities
import com.gabrigiunchi.backendtesi.util.DateDecorator
import org.assertj.core.api.Assertions
import org.junit.Test

class DateIntervalTest {

    @Test(expected = IllegalArgumentException::class)
    fun `Should raise an exception if the start is before the end`() {
        DateInterval(DateDecorator.of("2019-01-01T10:00:00+0000").date,
                DateDecorator.of("2018-10-10T09:00:00+0000").date)
    }

    @Test
    fun `Should say if a date is contained`() {
        val dateInterval = DateInterval(
                DateDecorator.createDate("2019-01-01").date,
                DateDecorator.createDate("2019-01-30").date
        )

        Assertions.assertThat(dateInterval.contains(DateDecorator.of("2019-01-01T01:00:00+0000").date)).isTrue()
        Assertions.assertThat(dateInterval.contains(DateDecorator.createDate("2019-01-01").date)).isTrue()
        Assertions.assertThat(dateInterval.contains(DateDecorator.createDate("2019-01-02").date)).isTrue()
        Assertions.assertThat(dateInterval.contains(DateDecorator.createDate("2019-01-03").date)).isTrue()
        Assertions.assertThat(dateInterval.contains(DateDecorator.createDate("2019-01-04").date)).isTrue()
        Assertions.assertThat(dateInterval.contains(DateDecorator.createDate("2019-01-20").date)).isTrue()

    }

    @Test
    fun `Should say if a date is NOT contained`() {
        val dateInterval = DateInterval(
                DateDecorator.createDate("2019-01-01").date,
                DateDecorator.createDate("2019-01-30").date
        )

        Assertions.assertThat(dateInterval.contains(DateDecorator.createDate("2018-01-01").date)).isFalse()
        Assertions.assertThat(dateInterval.contains(DateDecorator.createDate("2020-01-02").date)).isFalse()
        Assertions.assertThat(dateInterval.contains(DateDecorator.createDate("2019-02-03").date)).isFalse()
        Assertions.assertThat(dateInterval.contains(DateDecorator.createDate("2019-03-04").date)).isFalse()
        Assertions.assertThat(dateInterval.contains(DateDecorator.createDate("2019-04-20").date)).isFalse()
        Assertions.assertThat(dateInterval.contains(DateDecorator.createDate("2020-01-30").date)).isFalse()
        Assertions.assertThat(dateInterval.contains(DateDecorator.of("2019-01-30T01:00:00+0000").date)).isFalse()
    }

    /************************************ CONTAINS A DATE INTERVAL *********************************************************/

    @Test
    fun `Should say if contains a date interval`() {
        val dateInterval = DateInterval(
                DateDecorator.of("2019-01-01T08:00:00+0000").date,
                DateDecorator.of("2019-01-01T16:00:00+0000").date
        )

        Assertions.assertThat(dateInterval.contains(
                DateInterval(
                        DateDecorator.of("2019-01-01T12:00:00+0000").date,
                        DateDecorator.of("2019-01-01T14:00:00+0000").date))
        ).isTrue()
    }

    @Test
    fun `Should say if contains a date interval (edge case with start time)`() {
        val dateInterval = DateInterval(
                DateDecorator.of("2019-01-01T08:00:00+0000").date,
                DateDecorator.of("2019-01-01T16:00:00+0000").date
        )

        Assertions.assertThat(dateInterval.contains(
                DateInterval(
                        DateDecorator.of("2019-01-01T08:00:00+0000").date,
                        DateDecorator.of("2019-01-01T14:00:00+0000").date))
        ).isTrue()
    }

    @Test
    fun `Should say if contains a date interval (edge case with end time)`() {
        val dateInterval = DateInterval(
                DateDecorator.of("2019-01-01T08:00:00+0000").date,
                DateDecorator.of("2019-01-01T16:00:00+0000").date
        )

        Assertions.assertThat(dateInterval.contains(
                DateInterval(
                        DateDecorator.of("2019-01-01T12:00:00+0000").date,
                        DateDecorator.of("2019-01-01T16:00:00+0000").date))
        ).isTrue()
    }

    @Test
    fun `Should say if contains a date interval (edge case with start time and end time)`() {
        val dateInterval = DateInterval(
                DateDecorator.of("2019-01-01T08:00:00+0000").date,
                DateDecorator.of("2019-01-01T16:00:00+0000").date
        )

        Assertions.assertThat(dateInterval.contains(
                DateInterval(
                        DateDecorator.of("2019-01-01T08:00:00+0000").date,
                        DateDecorator.of("2019-01-01T16:00:00+0000").date))
        ).isTrue()
    }

    @Test
    fun `Should say if contains a date interval (edge case with timezone)`() {
        val dateInterval = DateInterval(
                DateDecorator.of("2019-01-01T08:00:00+0000").date,
                DateDecorator.of("2019-01-01T16:00:00+0000").date
        )

        Assertions.assertThat(dateInterval.contains(
                DateInterval(
                        DateDecorator.of("2019-01-01T10:00:00+0200").date,
                        DateDecorator.of("2019-01-01T18:00:00+0200").date))
        ).isTrue()
    }

    @Test
    fun `Should say if does not contain a date interval if the start is invalid`() {
        val dateInterval = DateInterval(
                DateDecorator.of("2019-01-01T08:00:00+0000").date,
                DateDecorator.of("2019-01-01T16:00:00+0000").date
        )

        Assertions.assertThat(dateInterval.contains(
                DateInterval(
                        DateDecorator.of("2019-01-01T07:59:59+0000").date,
                        DateDecorator.of("2019-01-01T14:00:00+0000").date))
        ).isFalse()
    }

    @Test
    fun `Should say if does not contain a date interval if the end is invalid`() {
        val dateInterval = DateInterval(
                DateDecorator.of("2019-01-01T08:00:00+0000").date,
                DateDecorator.of("2019-01-01T16:00:00+0000").date
        )

        Assertions.assertThat(dateInterval.contains(
                DateInterval(
                        DateDecorator.of("2019-01-01T12:00:00+0000").date,
                        DateDecorator.of("2019-01-01T16:00:01+0000").date))
        ).isFalse()
    }

    @Test
    fun `Should say if does not contain a date interval if both the start and the end are invalid`() {
        val dateInterval = DateInterval(
                DateDecorator.of("2019-01-01T08:00:00+0000").date,
                DateDecorator.of("2019-01-01T16:00:00+0000").date
        )

        Assertions.assertThat(dateInterval.contains(
                DateInterval(
                        DateDecorator.of("2019-01-01T06:00:00+0000").date,
                        DateDecorator.of("2019-01-01T07:00:00+0000").date))
        ).isFalse()

        Assertions.assertThat(dateInterval.contains(
                DateInterval(
                        DateDecorator.of("2019-01-01T16:00:01+0000").date,
                        DateDecorator.of("2019-01-01T18:00:00+0000").date))
        ).isFalse()
    }

    @Test
    fun `Should say if the interval is within the same day`() {
        Assertions.assertThat(
                DateInterval(
                        DateDecorator.of("2019-01-01T08:00:00+0000").date,
                        DateDecorator.of("2019-01-01T23:59:59+0000").date
                ).isWithinSameDay()
        ).isTrue()
    }

    @Test
    fun `Should say if the interval is within the same day considering timezone`() {
        val d1 = DateDecorator.of("2019-04-30T00:10:00+0200")
        val d2 = DateDecorator.of("2019-04-30T10:00:00+0200")
        val interval = DateInterval(d1.date, d2.date)
        Assertions.assertThat(interval.isWithinSameDay(MockEntities.ROME)).isTrue()
        Assertions.assertThat(interval.isWithinSameDay(MockEntities.UTC)).isFalse()
    }

    @Test
    fun `Should say if the interval is NOT within the same day`() {
        Assertions.assertThat(
                DateInterval(
                        DateDecorator.of("2019-01-01T08:00:00+0000").date,
                        DateDecorator.of("2019-01-02T16:00:00+0000").date
                ).isWithinSameDay(MockEntities.UTC)
        ).isFalse()
    }

    /************************************ OVERLAPS ************************************************************/

    @Test
    fun `Should say if a date interval overlaps another one (i2 start between i1)`() {
        val i1 = DateInterval(
                DateDecorator.of("2019-04-10T08:00:00+0000").date,
                DateDecorator.of("2019-04-10T16:00:00+0000").date
        )

        val i2 = DateInterval(
                DateDecorator.of("2019-04-10T10:00:00+0000").date,
                DateDecorator.of("2019-04-10T18:00:00+0000").date
        )

        Assertions.assertThat(i1.overlaps(i2)).isTrue()
        Assertions.assertThat(i2.overlaps(i1)).isTrue()
        Assertions.assertThat(i1.overlaps(i1)).isTrue()
    }

    @Test
    fun `Should say if a date interval overlaps another one (i2 end between i1)`() {
        val i1 = DateInterval(
                DateDecorator.of("2019-04-10T08:00:00+0000").date,
                DateDecorator.of("2019-04-10T16:00:00+0000").date
        )

        val i2 = DateInterval(
                DateDecorator.of("2019-04-10T05:00:00+0000").date,
                DateDecorator.of("2019-04-10T10:00:00+0000").date
        )

        Assertions.assertThat(i1.overlaps(i2)).isTrue()
        Assertions.assertThat(i2.overlaps(i1)).isTrue()
        Assertions.assertThat(i1.overlaps(i1)).isTrue()
    }

    @Test
    fun `Should say if a date interval overlaps another one (i1 start between i2)`() {
        val i2 = DateInterval(
                DateDecorator.of("2019-04-10T08:00:00+0000").date,
                DateDecorator.of("2019-04-10T16:00:00+0000").date
        )

        val i1 = DateInterval(
                DateDecorator.of("2019-04-10T10:00:00+0000").date,
                DateDecorator.of("2019-04-10T18:00:00+0000").date
        )

        Assertions.assertThat(i1.overlaps(i2)).isTrue()
        Assertions.assertThat(i2.overlaps(i1)).isTrue()
        Assertions.assertThat(i1.overlaps(i1)).isTrue()
    }

    @Test
    fun `Should say if a date interval overlaps another one (i1 end between i2)`() {
        val i2 = DateInterval(
                DateDecorator.of("2019-04-10T08:00:00+0000").date,
                DateDecorator.of("2019-04-10T16:00:00+0000").date
        )

        val i1 = DateInterval(
                DateDecorator.of("2019-04-10T05:00:00+0000").date,
                DateDecorator.of("2019-04-10T10:00:00+0000").date
        )

        Assertions.assertThat(i1.overlaps(i2)).isTrue()
        Assertions.assertThat(i2.overlaps(i1)).isTrue()
        Assertions.assertThat(i1.overlaps(i1)).isTrue()
    }

    @Test
    fun `Should say if a date interval overlaps another one (i2 contains i1)`() {
        val i1 = DateInterval(
                DateDecorator.of("2019-04-10T10:00:00+0000").date,
                DateDecorator.of("2019-04-10T12:00:00+0000").date
        )

        val i2 = DateInterval(
                DateDecorator.of("2019-04-10T08:00:00+0000").date,
                DateDecorator.of("2019-04-10T16:00:00+0000").date
        )

        Assertions.assertThat(i1.overlaps(i2)).isTrue()
        Assertions.assertThat(i2.overlaps(i1)).isTrue()
        Assertions.assertThat(i1.overlaps(i1)).isTrue()
    }

    @Test
    fun `Should say if a date interval overlaps another one (i1 contains i2)`() {
        val i2 = DateInterval(
                DateDecorator.of("2019-04-10T10:00:00+0000").date,
                DateDecorator.of("2019-04-10T12:00:00+0000").date
        )

        val i1 = DateInterval(
                DateDecorator.of("2019-04-10T08:00:00+0000").date,
                DateDecorator.of("2019-04-10T16:00:00+0000").date
        )

        Assertions.assertThat(i1.overlaps(i2)).isTrue()
        Assertions.assertThat(i2.overlaps(i1)).isTrue()
        Assertions.assertThat(i1.overlaps(i1)).isTrue()
    }

    @Test
    fun `Should say if a date interval does not overlap another one`() {
        val i1 = DateInterval(
                DateDecorator.of("2019-04-10T08:00:00+0000").date,
                DateDecorator.of("2019-04-10T16:00:00+0000").date
        )

        val i2 = DateInterval(
                DateDecorator.of("2019-04-10T05:00:00+0000").date,
                DateDecorator.of("2019-04-10T07:00:00+0000").date
        )

        val i3 = DateInterval(
                DateDecorator.of("2019-04-10T18:00:00+0000").date,
                DateDecorator.of("2019-04-10T20:00:00+0000").date
        )

        Assertions.assertThat(i1.overlaps(i2)).isFalse()
        Assertions.assertThat(i1.overlaps(i3)).isFalse()

        Assertions.assertThat(i2.overlaps(i1)).isFalse()
        Assertions.assertThat(i2.overlaps(i3)).isFalse()
    }

}
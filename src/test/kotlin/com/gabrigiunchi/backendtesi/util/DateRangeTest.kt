package com.gabrigiunchi.backendtesi.util

import org.assertj.core.api.Assertions
import org.junit.Test

class DateRangeTest {

    @Test
    fun `Should say if a date is contained in a range of dates`() {
        val range = DateRange(
                DateDecorator.of("2019-10-10", "yyyy-MM-dd").date,
                DateDecorator.of("2019-10-12", "yyyy-MM-dd").date)

        Assertions.assertThat(range.contains(DateDecorator.of("2019-10-10", "yyyy-MM-dd").date)).isEqualTo(true)
        Assertions.assertThat(range.contains(DateDecorator.of("2019-10-11", "yyyy-MM-dd").date)).isEqualTo(true)
        Assertions.assertThat(range.contains(DateDecorator.of("2019-10-12", "yyyy-MM-dd").date)).isEqualTo(true)
        Assertions.assertThat(range.contains(DateDecorator.of("2019-10-10T10:00:00+0000").date)).isEqualTo(true)
        Assertions.assertThat(range.contains(DateDecorator.of("2019-10-11T23:59:59+0000").date)).isEqualTo(true)
    }

    @Test
    fun `Should say if a date is contained in a range of dates with second precision`() {
        val range = DateRange(
                DateDecorator.of("2019-10-10T10:00:00+0000").date,
                DateDecorator.of("2019-10-12T16:00:00+0000").date)

        Assertions.assertThat(range.contains(DateDecorator.of("2019-10-10", "yyyy-MM-dd").date)).isEqualTo(false)
        Assertions.assertThat(range.contains(DateDecorator.of("2019-10-11", "yyyy-MM-dd").date)).isEqualTo(true)
        Assertions.assertThat(range.contains(DateDecorator.of("2019-10-12T15:00:00+0000").date)).isEqualTo(true)
        Assertions.assertThat(range.contains(DateDecorator.of("2019-10-12T16:00:00+0000").date)).isEqualTo(true)
        Assertions.assertThat(range.contains(DateDecorator.of("2019-10-12T15:59:59+0000").date)).isEqualTo(true)
        Assertions.assertThat(range.contains(DateDecorator.of("2019-10-12T16:00:01+0000").date)).isEqualTo(false)
        Assertions.assertThat(range.contains(DateDecorator.of("2019-10-10T10:00:00+0000").date)).isEqualTo(true)
        Assertions.assertThat(range.contains(DateDecorator.of("2019-10-10T09:59:59+0000").date)).isEqualTo(false)
        Assertions.assertThat(range.contains(DateDecorator.of("2019-10-10T10:30:00+0000").date)).isEqualTo(true)
    }

    @Test
    fun `Should say if a date is NOT contained in a range of dates`() {
        val range = DateRange(
                DateDecorator.of("2019-10-10", "yyyy-MM-dd").date,
                DateDecorator.of("2019-10-12", "yyyy-MM-dd").date)

        Assertions.assertThat(range.contains(DateDecorator.of("2019-10-09T23:59:59+0000").date)).isEqualTo(false)
        Assertions.assertThat(range.contains(DateDecorator.of("2019-10-12T00:00:01+0000").date)).isEqualTo(false)
        Assertions.assertThat(range.contains(DateDecorator.of("2019-10-13", "yyyy-MM-dd").date)).isEqualTo(false)
        Assertions.assertThat(range.contains(DateDecorator.of("2019-10-14", "yyyy-MM-dd").date)).isEqualTo(false)
        Assertions.assertThat(range.contains(DateDecorator.of("2018-10-11", "yyyy-MM-dd").date)).isEqualTo(false)
        Assertions.assertThat(range.contains(DateDecorator.of("2020-10-11", "yyyy-MM-dd").date)).isEqualTo(false)
    }
}
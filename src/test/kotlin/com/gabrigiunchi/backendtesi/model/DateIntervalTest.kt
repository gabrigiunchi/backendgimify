package com.gabrigiunchi.backendtesi.model

import com.gabrigiunchi.backendtesi.util.DateDecorator
import org.assertj.core.api.Assertions
import org.junit.Test

class DateIntervalTest {

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
}
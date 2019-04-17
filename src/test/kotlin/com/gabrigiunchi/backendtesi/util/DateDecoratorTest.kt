package com.gabrigiunchi.backendtesi.util

import org.assertj.core.api.Assertions
import org.junit.Test
import java.util.*

class DateDecoratorTest {

    @Test
    fun `Should create from a date object`()
    {
        val dateObject = Date()
        val decorator = DateDecorator.of(dateObject)
        Assertions.assertThat(decorator.date.time).isEqualTo(dateObject.time)
    }

    @Test
    fun `Should create from a string`()
    {
        val d = "2018-01-10T10:03:08+0100"
        val date = DateDecorator.of(d)

        System.out.println(date)

        Assertions.assertThat(date.year).isEqualTo(2018)
        Assertions.assertThat(date.month).isEqualTo(0)
        Assertions.assertThat(date.day).isEqualTo(10)
        Assertions.assertThat(date.minutes).isEqualTo(3)
        Assertions.assertThat(date.seconds).isEqualTo(8)
        Assertions.assertThat(date.format()).isEqualTo("2018-01-10T09:03:08+0000")
    }

    @Test
    fun `Should add minutes`()
    {
        val date = DateDecorator.of("2018-02-10T10:50:00+0100")
        val d2 = date.plusMinutes(15)
        Assertions.assertThat(d2.year).isEqualTo(2018)
        Assertions.assertThat(d2.month).isEqualTo(1)
        Assertions.assertThat(d2.day).isEqualTo(10)
        Assertions.assertThat(d2.minutes).isEqualTo(5)
        Assertions.assertThat(d2.seconds).isEqualTo(0)
        Assertions.assertThat(d2.format()).isEqualTo("2018-02-10T10:05:00+0000")
    }

    @Test
    fun `Should subtract minutes`()
    {
        val date = DateDecorator.of("2018-03-10 10:00:00", "yyyy-MM-dd HH:mm:ss")
        val d2 = date.minusMinutes(15)
        Assertions.assertThat(d2.year).isEqualTo(2018)
        Assertions.assertThat(d2.month).isEqualTo(2)
        Assertions.assertThat(d2.day).isEqualTo(10)
        Assertions.assertThat(d2.minutes).isEqualTo(45)
        Assertions.assertThat(d2.seconds).isEqualTo(0)
        Assertions.assertThat(d2.format()).isEqualTo("2018-03-10T09:45:00+0000")
    }

    @Test
    fun `Should say if two instances are equals`()
    {
        val d1 = DateDecorator.of("2018-03-10T10:00:00+0000")
        val d2 = DateDecorator.of("2018-03-10T10:00:00+0000")

        Assertions.assertThat(d1 == d2).isTrue()
    }

    @Test
    fun `Should say if two instances are not equals`()
    {
        val d1 = DateDecorator.of("2018-03-10T10:00:00+0000")
        val d2 = DateDecorator.of("2018-03-10T11:00:00+0000")

        Assertions.assertThat(d1 == d2).isFalse()
    }

    @Test
    fun `Should handle different timezones`()
    {
        val d1 = DateDecorator.of("2018-03-10T10:00:00+0000")
        val d2 = DateDecorator.of("2018-03-10T11:00:00+0100")

        Assertions.assertThat(d1 == d2).isTrue()

        val d3 = DateDecorator.of("2018-03-10T10:00:00+0000")
        val d4 = DateDecorator.of("2018-03-10T10:00:00+0100")

        Assertions.assertThat(d3 == d4).isFalse()
    }

    @Test
    fun `Should return the maximum date possible`()
    {
        val max = DateDecorator.max()
        Assertions.assertThat(max.date.time).isEqualTo(Long.MAX_VALUE)
        Assertions.assertThat(max.date.after(DateDecorator.now().date)).isTrue()
    }

    @Test
    fun `Should add days`() {
        val d1 = DateDecorator.of("2018-03-10T10:00:00+0000")
        Assertions.assertThat(d1.plusDays(1).format("yyyy-MM-dd")).isEqualTo("2018-03-11")
        Assertions.assertThat(d1.plusDays(2).format("yyyy-MM-dd")).isEqualTo("2018-03-12")
        Assertions.assertThat(d1.plusDays(5).format("yyyy-MM-dd")).isEqualTo("2018-03-15")
        Assertions.assertThat(d1.plusDays(7).format("yyyy-MM-dd")).isEqualTo("2018-03-17")
        Assertions.assertThat(d1.plusDays(20).format("yyyy-MM-dd")).isEqualTo("2018-03-30")
        Assertions.assertThat(d1.plusDays(20).format("yyyy-MM-dd")).isEqualTo("2018-03-30")
        Assertions.assertThat(d1.plusDays(30).format("yyyy-MM-dd")).isEqualTo("2018-04-09")
        Assertions.assertThat(d1.plusDays(365).format("yyyy-MM-dd")).isEqualTo("2019-03-10")
    }
}
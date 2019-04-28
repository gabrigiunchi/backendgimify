package com.gabrigiunchi.backendtesi.util

import org.assertj.core.api.Assertions
import org.junit.Test
import java.time.DayOfWeek
import java.util.*

class DateDecoratorTest {

    @Test
    fun `Should create from a date object`() {
        val dateObject = Date()
        val decorator = DateDecorator.of(dateObject)
        Assertions.assertThat(decorator.date.time).isEqualTo(dateObject.time)
    }

    @Test
    fun `Should create from a string`() {
        val date = DateDecorator.of("2018-01-10T10:03:08+0100")
        Assertions.assertThat(date.year).isEqualTo(2018)
        Assertions.assertThat(date.month).isEqualTo(0)
        Assertions.assertThat(date.day).isEqualTo(10)
        Assertions.assertThat(date.minutes).isEqualTo(3)
        Assertions.assertThat(date.seconds).isEqualTo(8)
        Assertions.assertThat(date.format()).isEqualTo("2018-01-10T09:03:08+0000")
    }

    @Test
    fun `Should format strings with the english locale`() {
        val date = DateDecorator.of("2019-04-28T10:03:08+0000")
        Assertions.assertThat(date.format("EEEE dd MMMM yyyy")).isEqualTo("Sunday 28 April 2019")
    }

    @Test
    fun `Should add minutes`() {
        val date = DateDecorator.of("2018-02-10T10:50:00+0100")
        val d2 = date.plusMinutes(15)
        Assertions.assertThat(d2.year).isEqualTo(2018)
        Assertions.assertThat(d2.month).isEqualTo(1)
        Assertions.assertThat(d2.day).isEqualTo(10)
        Assertions.assertThat(d2.hour).isEqualTo(10)
        Assertions.assertThat(d2.minutes).isEqualTo(5)
        Assertions.assertThat(d2.seconds).isEqualTo(0)
        Assertions.assertThat(d2.format()).isEqualTo("2018-02-10T10:05:00+0000")
    }

    @Test
    fun `Should subtract minutes`() {
        val date = DateDecorator.of("2018-03-10T10:00:00+0000")
        val d2 = date.minusMinutes(15)
        Assertions.assertThat(d2.year).isEqualTo(2018)
        Assertions.assertThat(d2.month).isEqualTo(2)
        Assertions.assertThat(d2.day).isEqualTo(10)
        Assertions.assertThat(d2.hour).isEqualTo(9)
        Assertions.assertThat(d2.minutes).isEqualTo(45)
        Assertions.assertThat(d2.seconds).isEqualTo(0)
        Assertions.assertThat(d2.format()).isEqualTo("2018-03-10T09:45:00+0000")
    }

    @Test
    fun `Should say if two instances are equals`() {
        val d1 = DateDecorator.of("2018-03-10T10:00:00+0000")
        val d2 = DateDecorator.of("2018-03-10T10:00:00+0000")
        Assertions.assertThat(d1 == d2).isTrue()
    }

    @Test
    fun `Should say if two instances are not equals`() {
        val d1 = DateDecorator.of("2018-03-10T10:00:00+0000")
        val d2 = DateDecorator.of("2018-03-10T11:00:00+0000")
        Assertions.assertThat(d1 == d2).isFalse()
    }

    @Test
    fun `Should handle different timezones`() {
        val d1 = DateDecorator.of("2018-03-10T10:00:00+0000")
        val d2 = DateDecorator.of("2018-03-10T11:00:00+0100")
        Assertions.assertThat(d1 == d2).isTrue()

        val d3 = DateDecorator.of("2018-03-10T10:00:00+0000")
        val d4 = DateDecorator.of("2018-03-10T10:00:00+0100")
        Assertions.assertThat(d3 == d4).isFalse()
    }

    @Test
    fun `Should return the maximum date possible`() {
        val max = DateDecorator.max()
        Assertions.assertThat(max.date.time).isEqualTo(Long.MAX_VALUE)
        Assertions.assertThat(max.date > DateDecorator.now().date).isTrue()
    }

    @Test
    fun `Should add days`() {
        val d1 = DateDecorator.of("2018-03-10T10:00:00+0000")
        Assertions.assertThat(d1.plusDays(1).format("yyyy-MM-dd")).isEqualTo("2018-03-11")
        Assertions.assertThat(d1.plusDays(2).format("yyyy-MM-dd")).isEqualTo("2018-03-12")
        Assertions.assertThat(d1.plusDays(5).format("yyyy-MM-dd")).isEqualTo("2018-03-15")
        Assertions.assertThat(d1.plusDays(7).format("yyyy-MM-dd")).isEqualTo("2018-03-17")
        Assertions.assertThat(d1.plusDays(20).format("yyyy-MM-dd")).isEqualTo("2018-03-30")
        Assertions.assertThat(d1.plusDays(30).format("yyyy-MM-dd")).isEqualTo("2018-04-09")
        Assertions.assertThat(d1.plusDays(365).format("yyyy-MM-dd")).isEqualTo("2019-03-10")
    }

    @Test
    fun `Should subtract days`() {
        val d1 = DateDecorator.of("2018-03-10T10:00:00+0000")
        Assertions.assertThat(d1.minusDays(1).format("yyyy-MM-dd")).isEqualTo("2018-03-09")
        Assertions.assertThat(d1.minusDays(2).format("yyyy-MM-dd")).isEqualTo("2018-03-08")
        Assertions.assertThat(d1.minusDays(5).format("yyyy-MM-dd")).isEqualTo("2018-03-05")
        Assertions.assertThat(d1.minusDays(7).format("yyyy-MM-dd")).isEqualTo("2018-03-03")
        Assertions.assertThat(d1.minusDays(20).format("yyyy-MM-dd")).isEqualTo("2018-02-18")
        Assertions.assertThat(d1.minusDays(30).format("yyyy-MM-dd")).isEqualTo("2018-02-08")
        Assertions.assertThat(d1.minusDays(365).format("yyyy-MM-dd")).isEqualTo("2017-03-10")
    }

    @Test
    fun `Should return the day of the week`() {
        // MONDAY
        val monday = DateDecorator.createDate("2019-04-15")
        Assertions.assertThat(monday.dayOfWeek).isEqualTo(DayOfWeek.MONDAY.value)

        // TUESDAY
        val tuesday = DateDecorator.createDate("2019-04-16")
        Assertions.assertThat(tuesday.dayOfWeek).isEqualTo(DayOfWeek.TUESDAY.value)

        // WEDNESDAY
        val wednesday = DateDecorator.createDate("2019-04-17")
        Assertions.assertThat(wednesday.dayOfWeek).isEqualTo(DayOfWeek.WEDNESDAY.value)

        // THURSDAY
        val thursday = DateDecorator.createDate("2019-04-18")
        Assertions.assertThat(thursday.dayOfWeek).isEqualTo(DayOfWeek.THURSDAY.value)

        // FRIDAY
        val friday = DateDecorator.createDate("2019-04-19")
        Assertions.assertThat(friday.dayOfWeek).isEqualTo(DayOfWeek.FRIDAY.value)

        // SATURDAY
        val saturday = DateDecorator.createDate("2019-04-20")
        Assertions.assertThat(saturday.dayOfWeek).isEqualTo(DayOfWeek.SATURDAY.value)

        // SUNDAY
        val sunday = DateDecorator.createDate("2019-04-21")
        Assertions.assertThat(sunday.dayOfWeek).isEqualTo(DayOfWeek.SUNDAY.value)
    }

    @Test
    fun `Should say if two dates are the same day`() {
        Assertions.assertThat(
                DateDecorator.createDate("2019-01-01").isSameDay(DateDecorator.createDate("2019-01-01"))
        ).isTrue()

        Assertions.assertThat(
                DateDecorator.of("2019-01-01T10:00:00+0000").isSameDay(DateDecorator.of("2019-01-01T23:59:59+0000"))
        ).isTrue()
    }

    @Test
    fun `Should say if two dates are NOT the same day`() {
        Assertions.assertThat(
                DateDecorator.createDate("2019-01-01").isSameDay(DateDecorator.createDate("2019-01-02").date)
        ).isFalse()

        Assertions.assertThat(
                DateDecorator.of("2019-01-01T10:00:00+0000").isSameDay(DateDecorator.of("2019-01-02T16:00:00+0000"))
        ).isFalse()
    }

    @Test
    fun `Should return the start of the current date`() {
        val today = DateDecorator.startOfToday()
        Assertions.assertThat(today.format("yyyy-MM-dd")).isEqualTo(DateDecorator.now().format("yyyy-MM-dd"))
        Assertions.assertThat(today.format("HH:mm:ss")).isEqualTo("00:00:00")
    }

    @Test
    fun `Should return the end of the current date`() {
        val today = DateDecorator.endOfToday()
        Assertions.assertThat(today.format("yyyy-MM-dd")).isEqualTo(DateDecorator.now().plusDays(1).format("yyyy-MM-dd"))
        Assertions.assertThat(today.format("HH:mm:ss")).isEqualTo("00:00:00")
    }

    @Test
    fun `Should override toString`() {
        val date = Date()
        val decorator = DateDecorator.of(date)
        Assertions.assertThat(date.toString()).isEqualTo(decorator.toString())
    }

    @Test
    fun `Should override hashCode`() {
        val date = Date()
        val decorator = DateDecorator.of(date)
        Assertions.assertThat(date.hashCode()).isEqualTo(decorator.hashCode())
    }

    @Test
    fun `Should override equals`() {
        val date1 = Date()
        val date2 = Date()
        val decorator1 = DateDecorator.of(date1)
        val decorator2 = DateDecorator.of(date2)
        Assertions.assertThat(decorator1 == decorator2).isTrue()
        Assertions.assertThat(decorator1 == DateDecorator.now().plusDays(1)).isFalse()
        val decorator3: DateDecorator? = null
        Assertions.assertThat(decorator1 == decorator3).isFalse()
    }
}
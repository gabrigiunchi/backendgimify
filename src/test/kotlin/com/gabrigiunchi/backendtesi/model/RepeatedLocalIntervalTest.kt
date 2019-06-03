package com.gabrigiunchi.backendtesi.model

import com.gabrigiunchi.backendtesi.model.time.LocalInterval
import com.gabrigiunchi.backendtesi.model.time.RepeatedLocalInterval
import com.gabrigiunchi.backendtesi.model.type.RepetitionType
import org.assertj.core.api.Assertions
import org.junit.Test
import java.time.*
import java.time.format.DateTimeFormatter

class RepeatedLocalIntervalTest {

    @Test(expected = IllegalArgumentException::class)
    fun `Should not create an interval with start after the end`() {
        RepeatedLocalInterval("2019-05-23T12:00:00", "2019-05-23T11:00:00", RepetitionType.DAILY)
    }

    @Test
    fun `Should say if it contains a date (no repetition)`() {
        val interval = RepeatedLocalInterval("2019-05-24T10:00:00", "2019-05-24T12:00:00", RepetitionType.NONE)
        Assertions.assertThat(interval.contains("2019-05-24T10:00:00")).isTrue()
        Assertions.assertThat(interval.contains("2019-05-24T11:00:00")).isTrue()
        Assertions.assertThat(interval.contains("2019-05-24T12:00:00")).isTrue()

        Assertions.assertThat(interval.contains("2019-05-24T09:59:59")).isFalse()
        Assertions.assertThat(interval.contains("2019-05-24T08:00:00")).isFalse()
        Assertions.assertThat(interval.contains("2019-05-24T12:00:01")).isFalse()
    }

    @Test
    fun `Should say if it contains a date (daily repetition)`() {
        val interval = RepeatedLocalInterval("2019-05-23T10:00:00", "2019-05-23T12:00:00", RepetitionType.DAILY)
        Assertions.assertThat(interval.contains("2019-05-24T10:00:00")).isTrue()
        Assertions.assertThat(interval.contains("2019-05-24T11:00:00")).isTrue()
        Assertions.assertThat(interval.contains("2019-05-24T12:00:00")).isTrue()

        Assertions.assertThat(interval.contains("2019-05-24T09:59:59")).isFalse()
        Assertions.assertThat(interval.contains("2019-05-24T08:00:00")).isFalse()
        Assertions.assertThat(interval.contains("2019-05-24T12:00:01")).isFalse()
    }

    @Test
    fun `Should say if it contains a date (weekly repetition)`() {
        val interval = RepeatedLocalInterval.create(DayOfWeek.THURSDAY, "10:00", "12:00")
        Assertions.assertThat(interval.contains("2019-05-30T10:00:00")).isTrue()
        Assertions.assertThat(interval.contains("2019-05-30T11:00:00")).isTrue()
        Assertions.assertThat(interval.contains("2019-05-30T12:00:00")).isTrue()

        Assertions.assertThat(interval.contains("2019-05-30T09:59:59")).isFalse()
        Assertions.assertThat(interval.contains("2019-05-30T08:00:00")).isFalse()
        Assertions.assertThat(interval.contains("2019-05-30T12:00:01")).isFalse()

        Assertions.assertThat(interval.contains("2019-05-29T11:00:00")).isFalse()
    }

    @Test
    fun `Should say if it contains a date (monthly repetition)`() {
        val interval = RepeatedLocalInterval("2019-05-23T10:00:00", "2019-05-23T12:00:00", RepetitionType.MONTHLY)
        Assertions.assertThat(interval.contains("2019-06-23T10:00:00")).isTrue()
        Assertions.assertThat(interval.contains("2019-06-23T11:00:00")).isTrue()
        Assertions.assertThat(interval.contains("2019-06-23T12:00:00")).isTrue()

        Assertions.assertThat(interval.contains("2019-06-23T09:59:59")).isFalse()
        Assertions.assertThat(interval.contains("2019-06-23T08:00:00")).isFalse()
        Assertions.assertThat(interval.contains("2019-06-23T12:00:01")).isFalse()

        Assertions.assertThat(interval.contains("2019-05-30T11:00:00")).isFalse()
    }

    @Test
    fun `Should say if it contains a date (yearly repetition)`() {
        val interval = RepeatedLocalInterval("2019-05-23T10:00:00", "2019-05-23T12:00:00", RepetitionType.YEARLY)
        Assertions.assertThat(interval.contains("2020-05-23T10:00:00")).isTrue()
        Assertions.assertThat(interval.contains("2020-05-23T11:00:00")).isTrue()
        Assertions.assertThat(interval.contains("2020-05-23T12:00:00")).isTrue()

        Assertions.assertThat(interval.contains("2020-05-23T09:59:59")).isFalse()
        Assertions.assertThat(interval.contains("2020-05-23T08:00:00")).isFalse()
        Assertions.assertThat(interval.contains("2020-05-23T12:00:01")).isFalse()

        Assertions.assertThat(interval.contains("2019-05-24T11:00:00")).isFalse()
    }

    @Test
    fun `Should say if it contains a date (weekly repetition and repetition end)`() {
        val interval = RepeatedLocalInterval(
                "2019-05-23T10:00:00",
                "2019-05-23T12:00:00",
                RepetitionType.WEEKLY,
                "2019-07-18T00:00:00")

        Assertions.assertThat(interval.contains("2019-07-11T11:00:00")).isTrue()
        Assertions.assertThat(interval.contains("2019-07-18T11:00:00")).isFalse()
        Assertions.assertThat(interval.contains("2019-07-25T11:00:00")).isFalse()
    }

    /******************************* CONTAINS INTERVAL ******************************************/
    @Test
    fun `Should say if it contains a interval(no repetition)`() {
        val interval = RepeatedLocalInterval("2019-05-23T10:00:00", "2019-05-23T12:00:00")
        Assertions.assertThat(interval.contains(LocalInterval("2019-05-23T08:00:00", "2019-05-23T11:00:00"))).isFalse()
        Assertions.assertThat(interval.contains(LocalInterval("2019-05-23T10:00:00", "2019-05-23T12:00:00"))).isTrue()
        Assertions.assertThat(interval.contains(LocalInterval("2019-05-23T11:00:00", "2019-05-23T12:00:01"))).isFalse()
        Assertions.assertThat(interval.contains(LocalInterval("2019-05-23T08:00:00", "2019-05-23T16:00:10"))).isFalse()
        Assertions.assertThat(interval.contains(LocalInterval("2019-05-23T08:00:00", "2019-05-23T09:59:59"))).isFalse()
        Assertions.assertThat(interval.contains(LocalInterval("2019-05-23T12:00:01", "2019-05-23T16:00:00"))).isFalse()
    }

    @Test
    fun `Should say if it contains a interval(daily repetition)`() {
        val interval = RepeatedLocalInterval("2019-05-16T10:00:00", "2019-05-16T12:00:00", RepetitionType.DAILY)
        Assertions.assertThat(interval.contains(LocalInterval("2019-05-23T08:00:00", "2019-05-23T11:00:00"))).isFalse()
        Assertions.assertThat(interval.contains(LocalInterval("2019-05-23T10:00:00", "2019-05-23T12:00:00"))).isTrue()
        Assertions.assertThat(interval.contains(LocalInterval("2019-05-23T11:00:00", "2019-05-23T12:00:01"))).isFalse()
        Assertions.assertThat(interval.contains(LocalInterval("2019-05-23T08:00:00", "2019-05-23T16:00:10"))).isFalse()
        Assertions.assertThat(interval.contains(LocalInterval("2019-05-23T08:00:00", "2019-05-23T09:59:59"))).isFalse()
        Assertions.assertThat(interval.contains(LocalInterval("2019-05-23T12:00:01", "2019-05-23T16:00:00"))).isFalse()
    }

    @Test
    fun `Should say if it contains a interval (weekly repetition)`() {
        val interval = RepeatedLocalInterval.create(DayOfWeek.THURSDAY, "10:00", "12:00")
        Assertions.assertThat(interval.contains(LocalInterval("2019-05-23T08:00:00", "2019-05-23T11:00:00"))).isFalse()
        Assertions.assertThat(interval.contains(LocalInterval("2019-05-23T10:00:00", "2019-05-23T12:00:00"))).isTrue()
        Assertions.assertThat(interval.contains(LocalInterval("2019-05-23T11:00:00", "2019-05-23T12:00:01"))).isFalse()
        Assertions.assertThat(interval.contains(LocalInterval("2019-05-23T08:00:00", "2019-05-23T16:00:10"))).isFalse()
        Assertions.assertThat(interval.contains(LocalInterval("2019-05-23T08:00:00", "2019-05-23T09:59:59"))).isFalse()
        Assertions.assertThat(interval.contains(LocalInterval("2019-05-23T12:00:01", "2019-05-23T16:00:00"))).isFalse()
        Assertions.assertThat(interval.contains(LocalInterval("2019-05-22T10:00:00", "2019-05-22T11:00:00"))).isFalse()
    }

    @Test
    fun `Should say if it contains a interval (monthly repetition)`() {
        val interval = RepeatedLocalInterval("2019-05-23T10:00:00", "2019-05-23T12:00:00", RepetitionType.MONTHLY)
        Assertions.assertThat(interval.contains(LocalInterval("2019-06-23T10:00:00", "2019-06-23T11:00:00"))).isTrue()
        Assertions.assertThat(interval.contains(LocalInterval("2020-06-23T10:00:00", "2020-06-23T11:00:00"))).isTrue()
    }

    @Test
    fun `Should say if it contains a interval (yearly repetition)`() {
        val interval = RepeatedLocalInterval("2019-05-23T10:00:00", "2019-05-23T12:00:00", RepetitionType.YEARLY)
        Assertions.assertThat(interval.contains(LocalInterval("2019-05-23T10:00:00", "2019-05-23T11:00:00"))).isTrue()
        Assertions.assertThat(interval.contains(LocalInterval("2020-05-23T08:00:00", "2020-05-23T11:00:00"))).isFalse()
    }

    @Test
    fun `Should not contains a interval if it is beyond the repetition end`() {
        val interval = RepeatedLocalInterval(
                start = "2019-05-16T10:00:00",
                end = "2019-05-16T12:00:00",
                repetitionType = RepetitionType.DAILY,
                repetitionEnd = "2019-10-10T00:00:00")

        Assertions.assertThat(interval.contains(LocalInterval("2019-10-09T10:00:00", "2019-10-09T11:00:00"))).isTrue()
        Assertions.assertThat(interval.contains(LocalInterval("2019-10-10T10:00:00", "2019-10-10T11:00:00"))).isFalse()
    }


    /******************************* OVERLAPS **************************************************/
    @Test
    fun `Should say if it overlaps a date interval(no repetition)`() {
        val interval = RepeatedLocalInterval("2019-05-23T10:00:00", "2019-05-23T12:00:00", RepetitionType.NONE)

        Assertions.assertThat(interval.overlaps(LocalInterval("2019-05-23T08:00:00", "2019-05-23T11:00:00"))).isTrue()
        Assertions.assertThat(interval.overlaps(LocalInterval("2019-05-23T10:00:00", "2019-05-23T12:00:00"))).isTrue()
        Assertions.assertThat(interval.overlaps(LocalInterval("2019-05-23T11:00:00", "2019-05-23T12:00:01"))).isTrue()
        Assertions.assertThat(interval.overlaps(LocalInterval("2019-05-23T08:00:00", "2019-05-23T16:00:10"))).isTrue()

        Assertions.assertThat(interval.overlaps(LocalInterval("2019-05-23T08:00:00", "2019-05-23T09:59:59"))).isFalse()
        Assertions.assertThat(interval.overlaps(LocalInterval("2019-05-23T12:00:10", "2019-05-23T16:00:00"))).isFalse()
    }

    @Test
    fun `Should say if it overlaps a date interval(daily repetition)`() {
        val interval = RepeatedLocalInterval("2019-05-16T10:00:00", "2019-05-16T12:00:00", RepetitionType.DAILY)

        Assertions.assertThat(interval.overlaps(LocalInterval("2019-05-23T08:00:00", "2019-05-23T11:00:00"))).isTrue()
        Assertions.assertThat(interval.overlaps(LocalInterval("2019-05-23T10:00:00", "2019-05-23T12:00:00"))).isTrue()
        Assertions.assertThat(interval.overlaps(LocalInterval("2019-05-23T11:00:00", "2019-05-23T12:00:01"))).isTrue()
        Assertions.assertThat(interval.overlaps(LocalInterval("2019-05-23T08:00:00", "2019-05-23T16:00:10"))).isTrue()

        Assertions.assertThat(interval.overlaps(LocalInterval("2019-05-23T08:00:00", "2019-05-23T09:59:59"))).isFalse()
        Assertions.assertThat(interval.overlaps(LocalInterval("2019-05-23T12:00:10", "2019-05-23T16:00:00"))).isFalse()
    }

    @Test
    fun `Should say if it overlaps a date interval (weekly repetition)`() {
        val interval = RepeatedLocalInterval("2019-05-16T10:00:00", "2019-05-16T12:00:00", RepetitionType.WEEKLY)

        Assertions.assertThat(interval.overlaps(LocalInterval("2019-05-23T08:00:00", "2019-05-23T11:00:00"))).isTrue()
        Assertions.assertThat(interval.overlaps(LocalInterval("2019-05-23T10:00:00", "2019-05-23T12:00:00"))).isTrue()
        Assertions.assertThat(interval.overlaps(LocalInterval("2019-05-23T11:00:00", "2019-05-23T12:00:01"))).isTrue()
        Assertions.assertThat(interval.overlaps(LocalInterval("2019-05-23T08:00:00", "2019-05-23T16:00:10"))).isTrue()

        Assertions.assertThat(interval.overlaps(LocalInterval("2019-05-23T08:00:00", "2019-05-23T09:59:59"))).isFalse()
        Assertions.assertThat(interval.overlaps(LocalInterval("2019-05-23T12:00:10", "2019-05-23T16:00:00"))).isFalse()
    }

    @Test
    fun `Should say if it overlaps a date interval (monthly repetition)`() {
        val interval = RepeatedLocalInterval("2019-05-16T10:00:00", "2019-05-16T12:00:00", RepetitionType.MONTHLY)

        Assertions.assertThat(interval.overlaps(LocalInterval("2019-06-16T08:00:00", "2019-06-16T11:00:00"))).isTrue()
        Assertions.assertThat(interval.overlaps(LocalInterval("2019-06-16T10:00:00", "2019-06-16T12:00:00"))).isTrue()
        Assertions.assertThat(interval.overlaps(LocalInterval("2019-06-16T11:00:00", "2019-06-16T12:00:01"))).isTrue()
        Assertions.assertThat(interval.overlaps(LocalInterval("2019-06-16T08:00:00", "2019-06-16T16:00:10"))).isTrue()

        Assertions.assertThat(interval.overlaps(LocalInterval("2019-06-16T08:00:00", "2019-06-16T09:59:59"))).isFalse()
        Assertions.assertThat(interval.overlaps(LocalInterval("2019-06-16T12:00:10", "2019-06-16T16:00:00"))).isFalse()
    }

    @Test
    fun `Should say if it overlaps a date interval (yearly repetition)`() {
        val interval = RepeatedLocalInterval("2019-05-16T10:00:00", "2019-05-16T12:00:00", RepetitionType.YEARLY)

        Assertions.assertThat(interval.overlaps(LocalInterval("2020-05-16T08:00:00", "2020-05-16T11:00:00"))).isTrue()
        Assertions.assertThat(interval.overlaps(LocalInterval("2020-05-15T10:00:00", "2020-05-16T12:00:00"))).isTrue()
        Assertions.assertThat(interval.overlaps(LocalInterval("2020-05-16T11:00:00", "2020-05-16T12:00:01"))).isTrue()
        Assertions.assertThat(interval.overlaps(LocalInterval("2020-05-16T08:00:00", "2020-05-16T16:00:10"))).isTrue()

        Assertions.assertThat(interval.overlaps(LocalInterval("2020-05-16T08:00:00", "2020-05-16T09:59:59"))).isFalse()
        Assertions.assertThat(interval.overlaps(LocalInterval("2020-05-16T12:00:10", "2020-05-16T16:00:00"))).isFalse()
    }

    @Test
    fun `Should not overlaps an interval if it is beyond the repetition end`() {
        val interval = RepeatedLocalInterval(
                start = "2019-05-16T10:00:00",
                end = "2019-05-16T12:00:00",
                repetitionType = RepetitionType.DAILY,
                repetitionEnd = "2019-10-10T00:00:00")

        Assertions.assertThat(interval.overlaps(LocalInterval("2019-10-09T08:00:00", "2019-10-09T11:00:00"))).isTrue()
        Assertions.assertThat(interval.overlaps(LocalInterval("2019-10-10T08:00:00", "2019-10-10T11:00:00"))).isFalse()
    }

    @Test
    fun `Should convert into a repeated zoned interval`() {
        val localInterval = RepeatedLocalInterval(10,
                LocalDateTime.parse("2019-05-23T00:00:00"),
                LocalDateTime.parse("2019-05-23T23:59:59"),
                RepetitionType.DAILY,
                null)

        val zonedIntervalNewYork = localInterval.toRepeatedZonedInterval(ZoneId.of("America/New_York"))
        Assertions.assertThat(zonedIntervalNewYork.id).isEqualTo(localInterval.id)
        Assertions.assertThat(zonedIntervalNewYork.start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssZ")))
                .isEqualTo("2019-05-23 00:00:00-0400")

        Assertions.assertThat(zonedIntervalNewYork.end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssZ")))
                .isEqualTo("2019-05-23 23:59:59-0400")

        Assertions.assertThat(zonedIntervalNewYork.repetitionType).isEqualTo(RepetitionType.DAILY)


        val zonedIntervalRome = localInterval.toRepeatedZonedInterval(ZoneId.of("Europe/Rome"))
        Assertions.assertThat(zonedIntervalRome.start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssZ")))
                .isEqualTo("2019-05-23 00:00:00+0200")

        Assertions.assertThat(zonedIntervalRome.end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssZ")))
                .isEqualTo("2019-05-23 23:59:59+0200")

        Assertions.assertThat(zonedIntervalRome.repetitionType).isEqualTo(RepetitionType.DAILY)
    }

    @Test
    fun `Stress test`() {
        val interval = RepeatedLocalInterval(
                start = "2000-05-16T10:00:00",
                end = "2000-05-16T12:00:00",
                repetitionType = RepetitionType.DAILY)

        val testYears = 50L
        val t1 = Instant.now()
        val result = interval.overlaps(
                LocalInterval(interval.start.plusYears(testYears), interval.end.plusYears(testYears)))

        val t2 = Instant.now()
        Assertions.assertThat(result).isTrue()
        val duration = Duration.between(t1, t2)
        System.out.println(duration.toMillis())
        Assertions.assertThat(duration).isLessThan(Duration.ofMillis(50))
    }
}

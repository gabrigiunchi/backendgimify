package com.gabrigiunchi.backendtesi

import com.gabrigiunchi.backendtesi.model.DateInterval
import com.gabrigiunchi.backendtesi.model.Schedule
import com.gabrigiunchi.backendtesi.model.TimeInterval
import com.gabrigiunchi.backendtesi.util.DateDecorator
import java.time.DayOfWeek
import java.time.OffsetTime

object MockEntities {

    val mockDateIntervals = setOf(
            DateInterval(DateDecorator.of("2018-10-10T10:00:00+0000").date, DateDecorator.of("2018-10-10T12:00:00+0000").date),
            DateInterval(DateDecorator.of("2019-10-10T10:00:00+0000").date, DateDecorator.of("2019-10-10T12:00:00+0000").date),
            DateInterval(DateDecorator.of("2020-10-10T10:00:00+0000").date, DateDecorator.of("2020-10-10T12:00:00+0000").date),
            DateInterval(DateDecorator.of("2021-10-10T10:00:00+0000").date, DateDecorator.of("2021-10-10T12:00:00+0000").date)

    )

    val mockTimeIntervals = listOf(
            TimeInterval(OffsetTime.parse("10:00:00+00:00"), OffsetTime.parse("12:00:00+00:00")),
            TimeInterval(OffsetTime.parse("12:00:00+00:00"), OffsetTime.parse("14:00:00+00:00")),
            TimeInterval(OffsetTime.parse("14:00:00+00:00"), OffsetTime.parse("16:00:00+00:00")),
            TimeInterval(OffsetTime.parse("16:00:00+00:00"), OffsetTime.parse("18:00:00+00:00"))
    )

    val mockSchedules = setOf(
            Schedule(DayOfWeek.MONDAY, this.mockTimeIntervals.take(2).toSet()),
            Schedule(DayOfWeek.TUESDAY, setOf(this.mockTimeIntervals[2], this.mockTimeIntervals[3])),
            Schedule(DayOfWeek.FRIDAY),
            Schedule(DayOfWeek.WEDNESDAY)
    )
}
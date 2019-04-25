package com.gabrigiunchi.backendtesi

import com.gabrigiunchi.backendtesi.model.*
import com.gabrigiunchi.backendtesi.model.type.AssetKindEnum
import com.gabrigiunchi.backendtesi.model.type.CityEnum
import com.gabrigiunchi.backendtesi.util.DateDecorator
import java.time.DayOfWeek
import java.time.MonthDay
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

    val mockMonthDays = setOf(
            MonthDay.of(12, 25),
            MonthDay.of(12, 26),
            MonthDay.of(12, 30),
            MonthDay.of(1, 1)
    )

    val mockSchedules = setOf(
            Schedule(DayOfWeek.MONDAY, this.mockTimeIntervals.take(2).toSet(), this.mockMonthDays),
            Schedule(DayOfWeek.TUESDAY, setOf(this.mockTimeIntervals[2], this.mockTimeIntervals[3]))
    )

    val mockCities = CityEnum.values().map { City(it) }
    val assetKinds = AssetKindEnum.values().map { AssetKind(it, 20) }
}
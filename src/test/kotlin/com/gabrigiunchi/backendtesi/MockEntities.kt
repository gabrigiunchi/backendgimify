package com.gabrigiunchi.backendtesi

import com.gabrigiunchi.backendtesi.model.*
import com.gabrigiunchi.backendtesi.model.type.AssetKindEnum
import com.gabrigiunchi.backendtesi.model.type.CityEnum
import com.gabrigiunchi.backendtesi.util.DateDecorator
import java.time.DayOfWeek
import java.time.MonthDay
import java.time.ZoneId

object MockEntities {

    val mockDateIntervals = setOf(
            DateInterval(DateDecorator.of("2018-10-10T10:00:00+0000").date, DateDecorator.of("2018-10-10T12:00:00+0000").date),
            DateInterval(DateDecorator.of("2019-10-10T10:00:00+0000").date, DateDecorator.of("2019-10-10T12:00:00+0000").date),
            DateInterval(DateDecorator.of("2020-10-10T10:00:00+0000").date, DateDecorator.of("2020-10-10T12:00:00+0000").date),
            DateInterval(DateDecorator.of("2021-10-10T10:00:00+0000").date, DateDecorator.of("2021-10-10T12:00:00+0000").date)
    )

    val mockTimeIntervals = listOf(
            TimeInterval("10:00", "12:00"),
            TimeInterval("12:00", "14:00"),
            TimeInterval("14:00", "16:00"),
            TimeInterval("16:00", "18:00")
    )

    val mockHolidays = setOf(
            MonthDay.of(12, 25),
            MonthDay.of(12, 26),
            MonthDay.of(12, 30),
            MonthDay.of(1, 1)
    )

    val mockSchedules = setOf(
            Schedule(DayOfWeek.MONDAY, this.mockTimeIntervals.take(2).toSet()),
            Schedule(DayOfWeek.TUESDAY, setOf(this.mockTimeIntervals[2], this.mockTimeIntervals[3]))
    )

    val mockCities = CityEnum.values().map { City(it) }
    val assetKinds = AssetKindEnum.values().map { AssetKind(it, 20) }
    val wildcardSchedules = DayOfWeek.values().map { Schedule(it, setOf(TimeInterval("00:01", "23:59"))) }.toSet()

    val ROME: ZoneId = ZoneId.of("Europe/Rome")
    val UTC: ZoneId = ZoneId.of("UTC")
    val LOS_ANGELES: ZoneId = ZoneId.of("America/Los_Angeles")
}
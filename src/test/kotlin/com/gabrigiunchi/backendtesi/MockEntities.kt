package com.gabrigiunchi.backendtesi

import com.gabrigiunchi.backendtesi.model.AssetKind
import com.gabrigiunchi.backendtesi.model.City
import com.gabrigiunchi.backendtesi.model.RepeatedInterval
import com.gabrigiunchi.backendtesi.model.type.AssetKindEnum
import java.time.DayOfWeek
import java.time.ZoneId

object MockEntities {

    val mockSchedules = setOf(
            RepeatedInterval.create(DayOfWeek.MONDAY, "08:00", "12:00"),
            RepeatedInterval.create(DayOfWeek.TUESDAY, "08:00", "12:00"),
            RepeatedInterval.create(DayOfWeek.WEDNESDAY, "08:00", "12:00"),
            RepeatedInterval.create(DayOfWeek.THURSDAY, "08:00", "12:00"),
            RepeatedInterval.create(DayOfWeek.FRIDAY, "08:00", "12:00")
    )

    val wildcardSchedules = DayOfWeek.values().map { RepeatedInterval.create(it, "00:00", "23:59") }.toSet()

    val mockCities = MockCityEnum.values().map { City(-1, it.name, ZoneId.of(it.zoneId)) }
    val assetKinds = AssetKindEnum.values().map { AssetKind(it, 20) }

    val ROME: ZoneId = ZoneId.of("Europe/Rome")
    val UTC: ZoneId = ZoneId.of("UTC")
    val LOS_ANGELES: ZoneId = ZoneId.of("America/Los_Angeles")

    enum class MockCityEnum(val zoneId: String) {
        MILANO("UTC"),
        TORINO("UTC"),
        BOLOGNA("UTC"),
        TRIESTE("UTC"),
        BERGAMO("UTC"),
        FORLI("UTC"),
        RIMINI("UTC")
    }
}
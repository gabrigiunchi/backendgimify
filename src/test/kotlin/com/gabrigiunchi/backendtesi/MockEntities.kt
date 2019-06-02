package com.gabrigiunchi.backendtesi

import com.gabrigiunchi.backendtesi.model.AssetKind
import com.gabrigiunchi.backendtesi.model.City
import com.gabrigiunchi.backendtesi.model.RepeatedLocalInterval
import com.gabrigiunchi.backendtesi.model.type.AssetKindEnum
import com.gabrigiunchi.backendtesi.model.type.RepetitionType
import java.time.DayOfWeek
import java.time.ZoneId

object MockEntities {

    val mockOpenings = setOf(
            RepeatedLocalInterval.create(DayOfWeek.MONDAY, "08:00", "12:00"),
            RepeatedLocalInterval.create(DayOfWeek.TUESDAY, "08:00", "12:00"),
            RepeatedLocalInterval.create(DayOfWeek.WEDNESDAY, "08:00", "12:00"),
            RepeatedLocalInterval.create(DayOfWeek.THURSDAY, "08:00", "12:00"),
            RepeatedLocalInterval.create(DayOfWeek.FRIDAY, "08:00", "12:00")
    )

    val wildcardOpenings = setOf(RepeatedLocalInterval("2019-01-01T00:00:00", "2019-01-02T00:00:00", RepetitionType.DAILY))

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
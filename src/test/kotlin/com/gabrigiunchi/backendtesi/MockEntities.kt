package com.gabrigiunchi.backendtesi

import com.gabrigiunchi.backendtesi.model.AssetKind
import com.gabrigiunchi.backendtesi.model.City
import com.gabrigiunchi.backendtesi.model.type.AssetKindEnum
import java.time.MonthDay
import java.time.ZoneId

object MockEntities {

    val mockHolidays = setOf(
            MonthDay.of(12, 25),
            MonthDay.of(12, 26),
            MonthDay.of(12, 30),
            MonthDay.of(1, 1)
    )

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
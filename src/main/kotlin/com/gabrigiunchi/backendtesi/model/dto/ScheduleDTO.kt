package com.gabrigiunchi.backendtesi.model.dto

import com.fasterxml.jackson.databind.ObjectMapper
import com.gabrigiunchi.backendtesi.model.DateInterval
import com.gabrigiunchi.backendtesi.model.Interval
import java.time.DayOfWeek

class ScheduleDTO(val dayOfWeek: DayOfWeek, val intervals: Set<Interval>, val exceptions: Set<DateInterval>) {
    fun toJson(): String {
        return ObjectMapper().writeValueAsString(mapOf(
                Pair("dayOfWeek", this.dayOfWeek.toString()),
                Pair("intervals", this.intervals.map { it.toMap() }),
                Pair("exceptions", this.exceptions)))
    }
}
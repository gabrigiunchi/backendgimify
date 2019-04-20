package com.gabrigiunchi.backendtesi.model.dto

import com.fasterxml.jackson.databind.ObjectMapper
import com.gabrigiunchi.backendtesi.model.TimeInterval
import java.time.DayOfWeek

class ScheduleDTO(val dayOfWeek: DayOfWeek, val timeIntervals: Set<TimeInterval>) {
    fun toJson(): String {
        return ObjectMapper().writeValueAsString(mapOf(
                Pair("dayOfWeek", this.dayOfWeek.toString()),
                Pair("timeIntervals", this.timeIntervals.map { it.toMap() })))
    }
}
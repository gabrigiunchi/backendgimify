package com.gabrigiunchi.backendtesi.model.dto.input

import com.fasterxml.jackson.databind.ObjectMapper
import com.gabrigiunchi.backendtesi.model.TimeInterval
import java.time.DayOfWeek
import java.time.MonthDay

class ScheduleDTO(val dayOfWeek: DayOfWeek, val timeIntervals: Set<TimeInterval>, val exceptions: Set<MonthDay>) {
    fun toJson(): String {
        return ObjectMapper().writeValueAsString(mapOf(
                Pair("dayOfWeek", this.dayOfWeek.toString()),
                Pair("timeIntervals", this.timeIntervals.map { it.toMap() }),
                Pair("exceptions", this.exceptions.map { it.toString() })))
    }
}
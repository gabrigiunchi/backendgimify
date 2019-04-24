package com.gabrigiunchi.backendtesi.model.dto.input

import com.fasterxml.jackson.databind.ObjectMapper
import com.gabrigiunchi.backendtesi.model.DateInterval
import com.gabrigiunchi.backendtesi.model.Schedule

class TimetableDTO(
        val gymId: Int,
        val openings: Set<Schedule>,
        val closingDays: Set<DateInterval>,
        val openingExceptions: Set<DateInterval>) {
    fun toJson(): String {
        return ObjectMapper().writeValueAsString(mapOf(
                Pair("gymId", this.gymId.toString()),
                Pair("closingDays", this.closingDays),
                Pair("openingExceptions", this.openingExceptions),
                Pair("openings", this.openings.map { it.toMap() })))
    }
}
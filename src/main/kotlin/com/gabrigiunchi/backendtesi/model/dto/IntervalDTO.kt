package com.gabrigiunchi.backendtesi.model.dto

import com.fasterxml.jackson.databind.ObjectMapper
import java.time.OffsetTime


class IntervalDTO(val start: OffsetTime, val end: OffsetTime) {
    fun toJson(): String {
        return ObjectMapper().writeValueAsString(mapOf(
                Pair("start", this.start.toString()),
                Pair("end", this.end.toString())))
    }
}
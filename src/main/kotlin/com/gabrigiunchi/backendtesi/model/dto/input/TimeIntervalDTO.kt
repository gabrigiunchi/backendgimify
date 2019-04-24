package com.gabrigiunchi.backendtesi.model.dto.input

import com.fasterxml.jackson.databind.ObjectMapper
import java.time.OffsetTime


class TimeIntervalDTO(val start: OffsetTime, val end: OffsetTime) {
    fun toJson(): String {
        return ObjectMapper().writeValueAsString(mapOf(
                Pair("start", this.start.toString()),
                Pair("end", this.end.toString())))
    }
}
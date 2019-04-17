package com.gabrigiunchi.backendtesi.model

import com.gabrigiunchi.backendtesi.model.dto.IntervalDTO
import java.time.OffsetTime
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
class Interval(
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        val id: Int,
        val start: OffsetTime,
        val end: OffsetTime
) {
    constructor(start: OffsetTime, end: OffsetTime) : this(-1, start, end)
    constructor(intervalDTO: IntervalDTO) : this(-1, intervalDTO.start, intervalDTO.end)

    init {
        if (this.start.isAfter(this.end)) {
            throw IllegalArgumentException("start is after the end")
        }
    }

    fun toMap(): Map<String, String> {
        return mapOf(
                Pair("id", this.id.toString()),
                Pair("start", this.start.toString()),
                Pair("end", this.end.toString())
        )
    }
}
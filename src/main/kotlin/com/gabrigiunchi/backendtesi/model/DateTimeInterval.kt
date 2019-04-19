package com.gabrigiunchi.backendtesi.model

import com.gabrigiunchi.backendtesi.model.dto.DateTimeIntervalDTO
import java.util.*
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
class DateTimeInterval(
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        val id: Int,
        val start: Date,
        val end: Date
) {
    constructor(start: Date, end: Date) : this(-1, start, end)
    constructor(dateTimeIntervalDTO: DateTimeIntervalDTO): this(-1, dateTimeIntervalDTO.start, dateTimeIntervalDTO.end)

    init {
        if (this.start.after(this.end)) {
            throw IllegalArgumentException("start is after the end")
        }
    }
}
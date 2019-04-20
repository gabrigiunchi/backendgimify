package com.gabrigiunchi.backendtesi.model

import com.gabrigiunchi.backendtesi.model.dto.DateIntervalDTO
import com.gabrigiunchi.backendtesi.util.DateDecorator
import java.util.*
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
class DateInterval(
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        val id: Int,
        val start: Date,
        val end: Date
) {
    constructor(start: Date, end: Date) : this(-1, start, end)
    constructor(dateIntervalDTO: DateIntervalDTO): this(-1, dateIntervalDTO.start, dateIntervalDTO.end)

    fun contains(date: Date): Boolean = date in this.start..this.end

    init {
        if (this.start.after(this.end)) {
            throw IllegalArgumentException("start is after the end")
        }
    }
}